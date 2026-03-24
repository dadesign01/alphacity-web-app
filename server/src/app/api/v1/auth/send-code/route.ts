import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import { sendSMS, saveCode, generateCode } from '@/lib/sms';

// 비밀번호 찾기용 인메모리 코드 저장 (email 키)
const verificationCodes = new Map<string, { code: string; expiresAt: number }>();

export { verificationCodes };

export async function POST(request: NextRequest) {
  try {
    const { email, phone } = await request.json();

    if (!phone) {
      return errorResponse('INVALID_INPUT', '전화번호를 입력하세요');
    }

    const code = generateCode();

    // email이 있으면 비밀번호 찾기 용도
    if (email) {
      const user = await prisma.user.findUnique({ where: { email } });
      if (!user) {
        return errorResponse('USER_NOT_FOUND', '등록되지 않은 이메일입니다', 404);
      }

      if (user.provider && !user.passwordHash) {
        return errorResponse('SOCIAL_ACCOUNT', '소셜 로그인 계정은 비밀번호를 변경할 수 없습니다', 400);
      }

      // email 기반 저장 (비밀번호 찾기)
      verificationCodes.set(email, { code, expiresAt: Date.now() + 5 * 60 * 1000 });
    }

    // phone 기반 저장 (회원가입/프로필 수정)
    saveCode(phone, code);

    // NCP SENS SMS 발송
    const sent = await sendSMS(phone, `[알파스탬프] 인증번호: ${code}`);
    if (!sent) {
      return errorResponse('SMS_FAILED', 'SMS 발송에 실패했습니다. 잠시 후 다시 시도해주세요.', 500);
    }

    return successResponse({ codeSent: true }, '인증코드가 발송되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
