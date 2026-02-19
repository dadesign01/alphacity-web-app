import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const missions = await prisma.mission.findMany({
      include: { place: { select: { name: true, latitude: true, longitude: true } } },
      orderBy: { createdAt: 'desc' },
    });
    return successResponse(missions);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
