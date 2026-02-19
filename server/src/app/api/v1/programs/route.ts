import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const programs = await prisma.program.findMany({
      where: { status: { in: ['scheduled', 'in_progress'] } },
      include: {
        events: {
          include: { _count: { select: { participants: true } } },
        },
      },
      orderBy: { startDate: 'asc' },
    });
    return successResponse(programs);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
