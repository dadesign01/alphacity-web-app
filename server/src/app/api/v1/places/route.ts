import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const places = await prisma.place.findMany({
      where: { isActive: true },
      include: { missions: { select: { id: true, name: true, type: true } } },
      orderBy: { name: 'asc' },
    });
    return successResponse(places);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
