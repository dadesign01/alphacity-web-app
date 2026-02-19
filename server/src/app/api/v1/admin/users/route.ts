import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const search = searchParams.get('search');

    const where = search
      ? {
          OR: [
            { nickname: { contains: search } },
            { email: { contains: search } },
          ],
        }
      : {};

    const users = await prisma.user.findMany({
      where,
      include: {
        _count: {
          select: { userStamps: true, userCoupons: true },
        },
      },
      orderBy: { createdAt: 'desc' },
    });

    const [totalUsers, todayUsers, activeUsers] = await Promise.all([
      prisma.user.count(),
      prisma.user.count({
        where: { createdAt: { gte: new Date(new Date().toISOString().split('T')[0]) } },
      }),
      prisma.user.count({ where: { isActive: true } }),
    ]);

    return successResponse({
      users,
      stats: { totalUsers, todayUsers, activeUsers },
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
