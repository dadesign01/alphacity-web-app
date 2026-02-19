import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { signToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';
import bcrypt from 'bcryptjs';

export async function POST(request: NextRequest) {
  try {
    const { email, password, nickname } = await request.json();

    if (!email || !password || !nickname) {
      return errorResponse('INVALID_INPUT', '이메일, 비밀번호, 닉네임은 필수입니다');
    }

    const existing = await prisma.user.findUnique({ where: { email } });
    if (existing) {
      return errorResponse('DUPLICATE_EMAIL', '이미 등록된 이메일입니다', 409);
    }

    const passwordHash = await bcrypt.hash(password, 10);
    const user = await prisma.user.create({
      data: { email, passwordHash, nickname },
    });

    const token = await signToken({ userId: user.id, email: user.email });
    const refreshToken = await signToken({ userId: user.id, type: 'refresh' }, '30d');

    return successResponse({
      token,
      refreshToken,
      user: { id: user.id, email: user.email, nickname: user.nickname },
    }, '회원가입이 완료되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
