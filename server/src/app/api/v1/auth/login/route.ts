import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { signToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';
import bcrypt from 'bcryptjs';

export async function POST(request: NextRequest) {
  try {
    const { email, password } = await request.json();

    if (!email || !password) {
      return errorResponse('INVALID_INPUT', '이메일과 비밀번호를 입력하세요');
    }

    const user = await prisma.user.findUnique({ where: { email } });
    if (!user) {
      return errorResponse('INVALID_CREDENTIALS', '이메일 또는 비밀번호가 올바르지 않습니다', 401);
    }

    const isValid = await bcrypt.compare(password, user.passwordHash);
    if (!isValid) {
      return errorResponse('INVALID_CREDENTIALS', '이메일 또는 비밀번호가 올바르지 않습니다', 401);
    }

    const token = await signToken({ userId: user.id, email: user.email });
    const refreshToken = await signToken({ userId: user.id, type: 'refresh' }, '30d');

    return successResponse({
      token,
      refreshToken,
      user: { id: user.id, email: user.email, nickname: user.nickname, profileImage: user.profileImage },
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
