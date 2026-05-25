import { NextRequest } from 'next/server';
import { successResponse, errorResponse } from '@/lib/api-response';
import { verificationCodes } from '../send-code/route';
import { verifyCode as verifyPhoneCode } from '@/lib/sms';

export async function POST(request: NextRequest) {
  try {
    const { email, phone, code } = await request.json();

    if (!code || (!email && !phone)) {
      return errorResponse('INVALID_INPUT', '이메일 또는 전화번호와 인증번호를 입력하세요');
    }

    // 전화번호 기반 검증 (회원가입 / 프로필 수정)
    if (phone) {
      const ok = verifyPhoneCode(phone, code);
      if (!ok) {
        return errorResponse('INVALID_CODE', '인증번호가 올바르지 않거나 만료되었습니다', 400);
      }
      return successResponse(null, '인증이 완료되었습니다');
    }

    // 이메일 기반 검증 (비밀번호 찾기)
    const stored = verificationCodes.get(email);
    if (!stored) {
      return errorResponse('CODE_NOT_FOUND', '인증코드를 먼저 발송하세요', 400);
    }
    if (Date.now() > stored.expiresAt) {
      verificationCodes.delete(email);
      return errorResponse('CODE_EXPIRED', '인증코드가 만료되었습니다. 다시 발송해주세요', 400);
    }
    if (stored.code !== code) {
      return errorResponse('INVALID_CODE', '인증코드가 올바르지 않습니다', 400);
    }

    return successResponse(null, '인증이 완료되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
