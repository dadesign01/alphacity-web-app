import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const events = await prisma.event.findMany({
      include: {
        program: { select: { name: true } },
        _count: { select: { participants: true } },
      },
      orderBy: { startDate: 'asc' },
    });

    const mapped = events.map((e) => ({
      ...e,
      participantCount: e._count.participants,
      _count: undefined,
    }));

    return successResponse(mapped);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
