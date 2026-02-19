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
    const body = await request.json();
    const { stampId, latitude, longitude } = body;

    if (!stampId) return errorResponse('INVALID_INPUT', '스탬프 ID는 필수입니다');

    const stamp = await prisma.stamp.findUnique({ where: { id: stampId } });
    if (!stamp) return errorResponse('NOT_FOUND', '스탬프를 찾을 수 없습니다', 404);

    const userStamp = await prisma.userStamp.create({
      data: { userId, stampId, latitude, longitude },
    });

    return successResponse(userStamp, '스탬프를 수집했습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
