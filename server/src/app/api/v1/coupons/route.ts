import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const coupons = await prisma.coupon.findMany({
      where: { validUntil: { gte: new Date() } },
      orderBy: { requiredStamps: 'asc' },
    });
    return successResponse(coupons);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
