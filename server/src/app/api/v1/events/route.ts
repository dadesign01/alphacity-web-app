import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const events = await prisma.event.findMany({
      where: { status: { in: ['scheduled', 'in_progress'] } },
      include: {
        program: { select: { name: true } },
        _count: { select: { participants: true } },
      },
      orderBy: { startDate: 'asc' },
    });
    return successResponse(events);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
