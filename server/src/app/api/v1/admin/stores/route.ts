import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const festivalIdRaw = searchParams.get('festivalId');
    const where: Record<string, unknown> = {};
    if (festivalIdRaw && festivalIdRaw !== 'all') {
      const festivalId = Number(festivalIdRaw);
      if (Number.isInteger(festivalId)) where.festivalId = festivalId;
    }

    const stores = await prisma.store.findMany({
      where,
      orderBy: { createdAt: 'desc' },
      include: {
        festival: { select: { id: true, name: true } },
        program: { select: { id: true, name: true } },
        mission: { select: { id: true, name: true, type: true } },
        storeCoupons: {
          include: { coupon: { select: { id: true, name: true } } },
        },
      },
    });

    const [totalCount, pendingCount, approvedCount, rejectedCount] = await Promise.all([
      prisma.store.count({ where }),
      prisma.store.count({ where: { ...where, status: 'pending' } }),
      prisma.store.count({ where: { ...where, status: 'approved' } }),
      prisma.store.count({ where: { ...where, status: 'rejected' } }),
    ]);

    return successResponse({
      stores,
      stats: { totalCount, pendingCount, approvedCount, rejectedCount },
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
