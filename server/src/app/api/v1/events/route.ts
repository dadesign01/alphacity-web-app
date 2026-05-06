import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const festivalIdRaw = searchParams.get('festivalId');
    const where: Record<string, unknown> = { isVisible: true };
    if (festivalIdRaw && festivalIdRaw !== 'all') {
      const festivalId = Number(festivalIdRaw);
      if (Number.isInteger(festivalId)) where.festivalId = festivalId;
    }

    const events = await prisma.event.findMany({
      where,
      include: {
        program: { select: { name: true } },
        _count: { select: { participants: true } },
      },
      orderBy: [{ sortOrder: 'asc' }, { startDate: 'asc' }],
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
