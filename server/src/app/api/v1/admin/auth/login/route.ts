import { NextRequest, NextResponse } from 'next/server';
import { prisma } from '@/lib/prisma';
import { signToken } from '@/lib/auth';
import { errorResponse } from '@/lib/api-response';
import bcrypt from 'bcryptjs';

export async function POST(request: NextRequest) {
  try {
    const { email, password } = await request.json();

    if (!email || !password) {
      return errorResponse('INVALID_INPUT', '이메일과 비밀번호를 입력하세요');
    }

    const admin = await prisma.admin.findUnique({ where: { email } });
    if (!admin) {
      return errorResponse('INVALID_CREDENTIALS', '이메일 또는 비밀번호가 올바르지 않습니다', 401);
    }

    const isValid = await bcrypt.compare(password, admin.password);
    if (!isValid) {
      return errorResponse('INVALID_CREDENTIALS', '이메일 또는 비밀번호가 올바르지 않습니다', 401);
    }

    const token = await signToken({ adminId: admin.id, email: admin.email });

    const response = NextResponse.json({
      success: true,
      data: {
        token,
        admin: { id: admin.id, email: admin.email, name: admin.name },
      },
    });

    // HTTPS가 아닌 환경(HTTP)에서도 쿠키가 저장되도록 secure는 실제 프로토콜 기반으로 설정
    const isHttps = request.nextUrl.protocol === 'https:';
    response.cookies.set('admin_token', token, {
      httpOnly: true,
      secure: isHttps,
      sameSite: 'lax',
      maxAge: 60 * 60 * 24 * 7, // 7 days
      path: '/',
    });
    console.log(`[Admin Login] ${admin.email} 로그인 성공 (secure=${isHttps})`);

    return response;
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
