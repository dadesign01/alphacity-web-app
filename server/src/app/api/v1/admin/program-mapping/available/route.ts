import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export const dynamic = 'force-dynamic';

export async function GET() {
  try {
    const [places, missions, stamps] = await Promise.all([
      prisma.place.findMany({
        where: { isActive: true },
        orderBy: { name: 'asc' },
      }),
      prisma.mission.findMany({
        include: { place: { select: { id: true, name: true } } },
        orderBy: { createdAt: 'desc' },
      }),
      prisma.stamp.findMany({
        include: { place: { select: { id: true, name: true } } },
        orderBy: { createdAt: 'desc' },
      }),
    ]);

    return successResponse({ places, missions, stamps });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
