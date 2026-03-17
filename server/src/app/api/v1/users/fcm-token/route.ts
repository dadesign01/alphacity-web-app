import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization')?.replace('Bearer ', '');
    if (!token) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

    const payload = await verifyToken(token);
    const userId = payload.userId as number;

    const { fcmToken } = await request.json();
    if (!fcmToken) return errorResponse('INVALID_INPUT', 'fcmToken은 필수입니다');

    await prisma.user.update({
      where: { id: userId },
      data: { fcmToken },
    });

    return successResponse(null, 'FCM 토큰이 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(request: NextRequest) {
  try {
    const token = request.headers.get('authorization')?.replace('Bearer ', '');
    if (!token) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

    const payload = await verifyToken(token);
    const userId = payload.userId as number;

    await prisma.user.update({
      where: { id: userId },
      data: { fcmToken: null },
    });

    return successResponse(null, 'FCM 토큰이 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
