import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const stores = await prisma.store.findMany({
      where: { status: 'approved' },
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
