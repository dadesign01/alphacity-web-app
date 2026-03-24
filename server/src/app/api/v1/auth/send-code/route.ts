import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

// 간단 인메모리 코드 저장 (프로덕션에서는 Redis 사용 권장)
const verificationCodes = new Map<string, { code: string; expiresAt: number }>();

export { verificationCodes };

export async function POST(request: NextRequest) {
  try {
    const { email, phone } = await request.json();

    if (!email || !phone) {
      return errorResponse('INVALID_INPUT', '이메일과 전화번호를 입력하세요');
    }

    const user = await prisma.user.findUnique({ where: { email } });
    if (!user) {
      return errorResponse('USER_NOT_FOUND', '등록되지 않은 이메일입니다', 404);
    }

    if (user.provider && !user.passwordHash) {
      return errorResponse('SOCIAL_ACCOUNT', '소셜 로그인 계정은 비밀번호를 변경할 수 없습니다', 400);
    }

    // 6자리 인증코드 생성
    const code = String(Math.floor(100000 + Math.random() * 900000));
    verificationCodes.set(email, { code, expiresAt: Date.now() + 5 * 60 * 1000 }); // 5분 유효

    // TODO: 실제 SMS 발송 연동 시 여기에 구현
    console.log(`[인증코드] ${phone}: ${code}`);

    return successResponse({ codeSent: true }, '인증코드가 발송되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
