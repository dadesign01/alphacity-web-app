import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import { FestivalStatus } from '@prisma/client';

function computeStatus(startDate: Date, endDate: Date): FestivalStatus {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const start = new Date(startDate);
  start.setHours(0, 0, 0, 0);
  const end = new Date(endDate);
  end.setHours(0, 0, 0, 0);
  if (today < start) return 'scheduled';
  if (today > end) return 'ended';
  return 'in_progress';
}

export async function GET(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const festival = await prisma.festival.findUnique({
      where: { id: Number(id) },
      include: {
        programs: {
          orderBy: { startDate: 'asc' },
          select: { id: true, name: true, category: true, imageUrl: true, startDate: true, endDate: true, status: true, location: true, latitude: true, longitude: true },
        },
      },
    });
    if (!festival) return errorResponse('NOT_FOUND', '축제를 찾을 수 없습니다', 404);
    return successResponse({ ...festival, status: computeStatus(festival.startDate, festival.endDate) });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
