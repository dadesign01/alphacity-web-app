import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);
    const my = searchParams.get('my');

    // 내 쿠폰 조회 (발급된 쿠폰만)
    if (my === 'true') {
      const token = request.headers.get('authorization')?.replace('Bearer ', '');
      if (!token) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

      const payload = await verifyToken(token);
      const userId = payload.userId as number;

      const userCoupons = await prisma.userCoupon.findMany({
        where: { userId },
        include: {
          coupon: true,
          store: true,
        },
        orderBy: { createdAt: 'desc' },
      });

      // 클라이언트에서 사용하기 편한 형태로 변환
      const result = userCoupons.map((uc) => ({
        id: uc.id,
        couponId: uc.couponId,
        name: uc.coupon.name,
        description: uc.coupon.description,
        imageUrl: uc.coupon.imageUrl,
        code: uc.code,
        status: uc.status,
        validUntil: uc.coupon.validUntil.toISOString(),
        storeName: uc.store?.name ?? null,
        requestedAt: uc.requestedAt?.toISOString() ?? null,
        usedAt: uc.usedAt?.toISOString() ?? null,
        createdAt: uc.createdAt.toISOString(),
      }));

      return successResponse(result);
    }

    // 전체 쿠폰 목록 (교환 가능한 쿠폰)
    const coupons = await prisma.coupon.findMany({
      where: { validUntil: { gte: new Date() } },
      orderBy: { requiredStamps: 'asc' },
    });
    return successResponse(coupons);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
