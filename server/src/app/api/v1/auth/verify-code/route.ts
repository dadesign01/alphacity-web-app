import { NextRequest } from 'next/server';
import { successResponse, errorResponse } from '@/lib/api-response';
import { verifyCode } from '@/lib/sms';

export async function POST(request: NextRequest) {
  try {
    const { phone, code } = await request.json();

    if (!phone || !code) {
      return errorResponse('INVALID_INPUT', '휴대폰 번호와 인증번호를 입력하세요');
    }

    const isValid = verifyCode(phone, code);

    if (!isValid) {
      return errorResponse('INVALID_CODE', '인증번호가 올바르지 않거나 만료되었습니다', 400);
    }

    return successResponse(null, '인증이 완료되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
