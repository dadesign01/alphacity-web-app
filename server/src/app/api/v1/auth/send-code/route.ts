import { NextRequest } from 'next/server';
import { successResponse, errorResponse } from '@/lib/api-response';
import { sendSMS, generateCode, saveCode } from '@/lib/sms';

export async function POST(request: NextRequest) {
  try {
    const { phone } = await request.json();

    if (!phone) {
      return errorResponse('INVALID_INPUT', '휴대폰 번호를 입력하세요');
    }

    const code = generateCode();
    saveCode(phone, code);

    const sent = await sendSMS(phone, `[알파시티스탬프투어] 인증번호: ${code}`);

    if (!sent) {
      return errorResponse('SMS_FAILED', 'SMS 전송에 실패했습니다', 500);
    }

    return successResponse(null, '인증번호가 전송되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
