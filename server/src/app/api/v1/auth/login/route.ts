
import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { signToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';
import bcrypt from 'bcryptjs';

export async function POST(request: NextRequest) {
  try {
    console.log('[LOGIN] 1. request start');

    const body = await request.json();

    console.log('[LOGIN] 2. body received:', {
      email: body?.email,
      hasPassword: !!body?.password,
    });

    const { email, password } = body;

    if (!email || !password) {
      console.log('[LOGIN] 3. invalid input');

      return errorResponse(
        'INVALID_INPUT',
        '이메일과 비밀번호를 입력하세요'
      );
    }

    console.log('[LOGIN] 4. checking database connection');

    await prisma.$queryRaw`SELECT 1`;

    console.log('[LOGIN] 5. database connection OK');

    console.log('[LOGIN] 6. find user:', email);

    const user = await prisma.user.findUnique({
      where: { email },
    });

    console.log('[LOGIN] 7. find user result:', {
      found: !!user,
      userId: user?.id,
      hasPasswordHash: !!user?.passwordHash,
    });

    if (!user) {
      console.log('[LOGIN] 8. user not found');

      return errorResponse(
        'INVALID_CREDENTIALS',
        '이메일 또는 비밀번호가 올바르지 않습니다',
        401
      );
    }

    if (!user.passwordHash) {
      console.log('[LOGIN] 9. social account');

      return errorResponse(
        'SOCIAL_ACCOUNT',
        '소셜 로그인으로 가입한 계정입니다',
        401
      );
    }

    console.log('[LOGIN] 10. bcrypt compare start');

    const isValid = await bcrypt.compare(
      password,
      user.passwordHash
    );

    console.log('[LOGIN] 11. bcrypt compare result:', isValid);

    if (!isValid) {
      console.log('[LOGIN] 12. invalid password');

      return errorResponse(
        'INVALID_CREDENTIALS',
        '이메일 또는 비밀번호가 올바르지 않습니다',
        401
      );
    }

    console.log('[LOGIN] 13. sign access token');

    const token = await signToken({
      userId: user.id,
      email: user.email,
    });

    console.log('[LOGIN] 14. access token OK');

    console.log('[LOGIN] 15. sign refresh token');

    const refreshToken = await signToken(
      {
        userId: user.id,
        type: 'refresh',
      },
      '30d'
    );

    console.log('[LOGIN] 16. refresh token OK');

    console.log('[LOGIN] 17. login success');

    return successResponse({
      token,
      refreshToken,
      user: {
        id: user.id,
        email: user.email,
        nickname: user.nickname,
        profileImage: user.profileImage,
      },
    });
  } catch (error) {
    console.error('[LOGIN ERROR]', error);

    return Response.json(
      {
        success: false,
        debug: true,
        error:
          error instanceof Error
            ? {
                name: error.name,
                message: error.message,
                stack: error.stack,
              }
            : String(error),
      },
      { status: 500 }
    );
  }
}

