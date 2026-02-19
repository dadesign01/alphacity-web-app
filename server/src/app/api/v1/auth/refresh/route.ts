import { NextRequest } from 'next/server';
import { signToken, verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function POST(request: NextRequest) {
  try {
    const { refreshToken } = await request.json();

    if (!refreshToken) {
      return errorResponse('INVALID_INPUT', '리프레시 토큰이 필요합니다');
    }

    const payload = await verifyToken(refreshToken);
    if (payload.type !== 'refresh') {
      return errorResponse('INVALID_TOKEN', '유효하지 않은 토큰입니다', 401);
    }

    const newToken = await signToken({ userId: payload.userId, email: payload.email });
    const newRefreshToken = await signToken({ userId: payload.userId, type: 'refresh' }, '30d');

    return successResponse({ token: newToken, refreshToken: newRefreshToken });
  } catch {
    return errorResponse('TOKEN_EXPIRED', '토큰이 만료되었습니다', 401);
  }
}
