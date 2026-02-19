import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const usageRequests = await prisma.userCoupon.findMany({
      where: { status: { in: ['pending', 'used'] } },
      include: {
        user: { select: { nickname: true, email: true } },
        coupon: { select: { name: true } },
        store: { select: { name: true } },
      },
      orderBy: { requestedAt: 'desc' },
    });

    const [todayRequests, pendingCount, completedCount] = await Promise.all([
      prisma.userCoupon.count({
        where: {
          requestedAt: { gte: new Date(new Date().toISOString().split('T')[0]) },
        },
      }),
      prisma.userCoupon.count({ where: { status: 'pending' } }),
      prisma.userCoupon.count({ where: { status: 'used' } }),
    ]);

    return successResponse({
      requests: usageRequests,
      stats: { todayRequests, pendingCount, completedCount },
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
