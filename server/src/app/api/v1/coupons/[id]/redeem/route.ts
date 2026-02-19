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

    // 필요 스탬프 수 확인
    const stampCount = await prisma.userStamp.count({ where: { userId } });
    if (stampCount < coupon.requiredStamps) {
      return errorResponse('INSUFFICIENT_STAMPS', `스탬프 ${coupon.requiredStamps}개가 필요합니다 (현재: ${stampCount}개)`);
    }

    // 쿠폰 코드 생성
    const code = `FEST-${Date.now().toString(36).toUpperCase()}-${Math.random().toString(36).substring(2, 6).toUpperCase()}`;

    const userCoupon = await prisma.userCoupon.create({
      data: { userId, couponId, code, status: 'issued' },
    });

    return successResponse(userCoupon, '쿠폰이 발급되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
