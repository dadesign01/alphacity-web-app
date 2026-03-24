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
    const { userCouponId, storeId } = body;

    if (!userCouponId) return errorResponse('INVALID_INPUT', '쿠폰 ID는 필수입니다');

    const userCoupon = await prisma.userCoupon.findUnique({ where: { id: userCouponId } });
    if (!userCoupon) return errorResponse('NOT_FOUND', '쿠폰을 찾을 수 없습니다', 404);
    if (userCoupon.userId !== userId) return errorResponse('FORBIDDEN', '본인의 쿠폰만 사용할 수 있습니다', 403);
    if (userCoupon.status !== 'issued') return errorResponse('INVALID_STATUS', '사용할 수 없는 쿠폰입니다');

    const updated = await prisma.userCoupon.update({
      where: { id: userCouponId },
      data: { status: 'used', storeId, requestedAt: new Date(), usedAt: new Date() },
    });

    return successResponse(updated, '쿠폰이 사용되었습니다.');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
