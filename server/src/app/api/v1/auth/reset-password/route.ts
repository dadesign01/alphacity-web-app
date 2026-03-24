import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import bcrypt from 'bcryptjs';
import { verificationCodes } from '../send-code/route';

export async function POST(request: NextRequest) {
  try {
    const { email, code, newPassword } = await request.json();

    if (!email || !newPassword) {
      return errorResponse('INVALID_INPUT', '이메일과 새 비밀번호를 입력하세요');
    }

    if (!code) {
      return errorResponse('INVALID_INPUT', '인증코드를 입력하세요');
    }

    if (newPassword.length < 8) {
      return errorResponse('INVALID_INPUT', '비밀번호는 8자 이상이어야 합니다');
    }

    // 인증코드 검증
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

    const user = await prisma.user.findUnique({ where: { email } });
    if (!user) {
      return errorResponse('USER_NOT_FOUND', '등록되지 않은 이메일입니다', 404);
    }

    if (user.provider && !user.passwordHash) {
      return errorResponse('SOCIAL_ACCOUNT', '소셜 로그인 계정은 비밀번호를 변경할 수 없습니다', 400);
    }

    const passwordHash = await bcrypt.hash(newPassword, 10);
    await prisma.user.update({
      where: { email },
      data: { passwordHash },
    });

    verificationCodes.delete(email);

    return successResponse(null, '비밀번호가 변경되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
