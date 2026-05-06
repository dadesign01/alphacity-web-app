import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);
    const my = searchParams.get('my');
    const festivalIdRaw = searchParams.get('festivalId');
    let festivalIdFilter: number | null = null;
    if (festivalIdRaw && festivalIdRaw !== 'all') {
      const fid = Number(festivalIdRaw);
      if (Number.isInteger(fid)) festivalIdFilter = fid;
    }

    // 내 쿠폰 조회 (발급된 쿠폰만)
    if (my === 'true') {
      const token = request.headers.get('authorization')?.replace('Bearer ', '');
      if (!token) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

      const payload = await verifyToken(token);
      const userId = payload.userId as number;

      const userCoupons = await prisma.userCoupon.findMany({
        where: { userId, ...(festivalIdFilter !== null && { coupon: { festivalId: festivalIdFilter } }) },
        include: {
          coupon: true,
          store: true,
        },
        orderBy: { createdAt: 'desc' },
      });

      const result = userCoupons.map((uc) => ({
        id: uc.id,
        couponId: uc.couponId,
        festivalId: uc.coupon.festivalId,
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
      where: {
        validUntil: { gte: new Date() },
        ...(festivalIdFilter !== null && { festivalId: festivalIdFilter }),
      },
      orderBy: { requiredStamps: 'asc' },
    });

    // 로그인 사용자면 이미 교환한 쿠폰 ID 목록 + 교환 가능 스탬프 수 반환 (축제별)
    let redeemedCouponIds: number[] = [];
    let availableStamps = 0;
    const token = request.headers.get('authorization')?.replace('Bearer ', '');
    if (token) {
      try {
        const payload = await verifyToken(token);
        const userId = payload.userId as number;
        const [userCoupons, totalStamps] = await Promise.all([
          prisma.userCoupon.findMany({
            where: { userId, ...(festivalIdFilter !== null && { coupon: { festivalId: festivalIdFilter } }) },
            include: { coupon: { select: { requiredStamps: true } } },
          }),
          prisma.userStamp.count({
            where: {
              userId,
              ...(festivalIdFilter !== null && { stamp: { festivalId: festivalIdFilter } }),
            },
          }),
        ]);
        redeemedCouponIds = userCoupons.map((uc) => uc.couponId);
        const usedStamps = userCoupons.reduce((sum, uc) => sum + uc.coupon.requiredStamps, 0);
        availableStamps = totalStamps - usedStamps;
      } catch {
        // 토큰 검증 실패 시 무시 (비로그인 상태)
      }
    }

    return successResponse({ coupons, redeemedCouponIds, availableStamps });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
