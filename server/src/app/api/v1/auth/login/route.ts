import { NextRequest, NextResponse } from 'next/server';
import { prisma } from '@/lib/prisma';
import { signToken } from '@/lib/auth';
import { errorResponse } from '@/lib/api-response';
import bcrypt from 'bcryptjs';

export async function POST(request: NextRequest) {
  try {
    console.log('[LOGIN] 1. request start');

    const { email, password } = await request.json();

    console.log('[LOGIN] 2. body received:', {
      email,
      hasPassword: !!password,
    });

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

    console.log('[LOGIN] 6. find admin:', email);

    const admin = await prisma.admin.findUnique({
      where: { email },
    });

    console.log('[LOGIN] 7. find admin result:', {
      found: !!admin,
      adminId: admin?.id,
      hasPassword: !!admin?.password,
    });

    if (!admin) {
      console.log('[LOGIN] 8. admin not found');

      return errorResponse(
        'INVALID_CREDENTIALS',
        '이메일 또는 비밀번호가 올바르지 않습니다',
        401
      );
    }

    console.log('[LOGIN] 9. bcrypt compare start');

    const isValid = await bcrypt.compare(
      password,
      admin.password
    );

    console.log('[LOGIN] 10. bcrypt compare result:', isValid);

    if (!isValid) {
      console.log('[LOGIN] 11. invalid password');

      return errorResponse(
        'INVALID_CREDENTIALS',
        '이메일 또는 비밀번호가 올바르지 않습니다',
        401
      );
    }

    console.log('[LOGIN] 12. sign token');

    const token = await signToken({
      adminId: admin.id,
      email: admin.email,
    });

    console.log('[LOGIN] 13. token created');

    const response = NextResponse.json({
      success: true,
      data: {
        token,
        admin: {
          id: admin.id,
          email: admin.email,
          name: admin.name,
        },
      },
    });

    const isHttps = request.nextUrl.protocol === 'https:';

    response.cookies.set('admin_token', token, {
      httpOnly: true,
      secure: isHttps,
      sameSite: 'lax',
      maxAge: 60 * 60 * 24 * 7,
      path: '/',
    });

    console.log(
      `[LOGIN] 14. login success: ${admin.email} (secure=${isHttps})`
    );

    return response;
  } catch (error) {
    console.error('[LOGIN ERROR]', error);

    return NextResponse.json(
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