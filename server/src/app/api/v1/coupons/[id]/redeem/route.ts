import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function POST(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const token = request.headers.get('authorization')?.replace('Bearer ', '');
    if (!token) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

    const payload = await verifyToken(token);
    const userId = payload.userId as number;
    const { id } = await params;
    const couponId = Number(id);

    const coupon = await prisma.coupon.findUnique({ where: { id: couponId } });
    if (!coupon) return errorResponse('NOT_FOUND', '쿠폰을 찾을 수 없습니다', 404);

    // 이미 발급받은 쿠폰인지 확인
    const existingCoupon = await prisma.userCoupon.findFirst({
      where: { userId, couponId },
    });
    if (existingCoupon) {
      return errorResponse('ALREADY_REDEEMED', '이미 발급받은 쿠폰입니다', 400);
    }

    // 교환 가능 스탬프 수 확인 (총 수집 - 이미 교환에 사용한 스탬프)
    const totalStamps = await prisma.userStamp.count({ where: { userId } });
    const usedCoupons = await prisma.userCoupon.findMany({
      where: { userId },
      include: { coupon: { select: { requiredStamps: true } } },
    });
    const usedStamps = usedCoupons.reduce((sum, uc) => sum + uc.coupon.requiredStamps, 0);
    const availableStamps = totalStamps - usedStamps;
    if (availableStamps < coupon.requiredStamps) {
      return errorResponse('INSUFFICIENT_STAMPS', `스탬프 ${coupon.requiredStamps}개가 필요합니다 (교환 가능: ${availableStamps}개)`);
    }

    // 쿠폰 코드 생성
    const code = `FEST-${Date.now().toString(36).toUpperCase()}-${Math.random().toString(36).substring(2, 6).toUpperCase()}`;

    // 쿠폰 발급
    const userCoupon = await prisma.userCoupon.create({
      data: { userId, couponId, code, status: 'issued' },
    });

    return successResponse(userCoupon, '쿠폰이 발급되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
