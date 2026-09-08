import { NextRequest, NextResponse } from 'next/server';
import { jwtVerify } from 'jose';

const JWT_SECRET = new TextEncoder().encode(
  process.env.JWT_SECRET || 'fallback-secret'
);

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // =========================================================
  // API CORS
  // =========================================================
  if (pathname.startsWith('/api/v1')) {
    // Preflight
    if (request.method === 'OPTIONS') {
      return new NextResponse(null, {
        status: 204,
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Methods':
            'GET, POST, PUT, DELETE, OPTIONS',
          'Access-Control-Allow-Headers':
            'Content-Type, Authorization',
        },
      });
    }

    const response = NextResponse.next();

    response.headers.set(
      'Access-Control-Allow-Origin',
      '*'
    );

    response.headers.set(
      'Access-Control-Allow-Methods',
      'GET, POST, PUT, DELETE, OPTIONS'
    );

    response.headers.set(
      'Access-Control-Allow-Headers',
      'Content-Type, Authorization'
    );

    return response;
  }

  // =========================================================
  // 관리자 페이지
  // =========================================================
  if (
    pathname.startsWith('/admin') &&
    pathname !== '/admin/login'
  ) {
    const token = request.cookies.get('admin_token')?.value;

    if (!token) {
      return NextResponse.redirect(
        new URL('/admin/login', request.url)
      );
    }

    try {
      await jwtVerify(token, JWT_SECRET);
      return NextResponse.next();
    } catch {
      return NextResponse.redirect(
        new URL('/admin/login', request.url)
      );
    }
  }

  // =========================================================
  // 관리자 API
  // =========================================================
  if (
    pathname.startsWith('/api/v1/admin') &&
    !pathname.includes('/auth/')
  ) {
    const authHeader = request.headers.get('authorization');

    const token =
      authHeader?.replace('Bearer ', '') ||
      request.cookies.get('admin_token')?.value;

    if (!token) {
      return NextResponse.json(
        {
          success: false,
          error: {
            code: 'UNAUTHORIZED',
            message: '인증이 필요합니다',
          },
        },
        { status: 401 }
      );
    }

    try {
      await jwtVerify(token, JWT_SECRET);
      return NextResponse.next();
    } catch {
      return NextResponse.json(
        {
          success: false,
          error: {
            code: 'TOKEN_EXPIRED',
            message: '토큰이 만료되었습니다',
          },
        },
        { status: 401 }
      );
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/admin/:path*',
    '/api/v1/:path*',
  ],
};