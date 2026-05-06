import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const festivalIdRaw = searchParams.get('festivalId');
    const where: Record<string, unknown> = { status: 'approved' };
    if (festivalIdRaw && festivalIdRaw !== 'all') {
      const festivalId = Number(festivalIdRaw);
      if (Number.isInteger(festivalId)) where.festivalId = festivalId;
    }

    const stores = await prisma.store.findMany({
      where,
      orderBy: { name: 'asc' },
      include: {
        mission: { select: { id: true, name: true, type: true } },
        storeCoupons: {
          include: { coupon: { select: { id: true, name: true, description: true, imageUrl: true } } },
        },
        program: { select: { id: true, name: true } },
      },
    });
    return successResponse(stores);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
