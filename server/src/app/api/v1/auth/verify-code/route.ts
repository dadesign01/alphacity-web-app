import { NextRequest } from 'next/server';
import { successResponse, errorResponse } from '@/lib/api-response';
import { verificationCodes } from '../send-code/route';

export async function POST(request: NextRequest) {
  try {
    const { email, code } = await request.json();

    if (!email || !code) {
      return errorResponse('INVALID_INPUT', '이메일과 인증번호를 입력하세요');
    }

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
