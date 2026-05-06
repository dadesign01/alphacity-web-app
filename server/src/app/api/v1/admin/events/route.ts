import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import { EventStatus } from '@prisma/client';

function computeEventStatus(startDate: Date, endDate: Date): EventStatus {
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

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const festivalIdRaw = searchParams.get('festivalId');
    const where: Record<string, unknown> = {};
    if (festivalIdRaw && festivalIdRaw !== 'all') {
      const festivalId = Number(festivalIdRaw);
      if (Number.isInteger(festivalId)) where.festivalId = festivalId;
    }

    const events = await prisma.event.findMany({
      where,
      include: {
        festival: { select: { id: true, name: true } },
        program: { select: { name: true } },
        _count: { select: { participants: true } },
      },
      orderBy: [{ sortOrder: 'asc' }, { createdAt: 'desc' }],
    });

    const eventsWithStatus = events.map(e => ({
      ...e,
      status: computeEventStatus(e.startDate, e.endDate),
    }));

    return successResponse(eventsWithStatus);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { festivalId, programId, name, description, imageUrl, type, startDate, endDate, reward, winnerCount, participantLimit, price, duration, capacity, location, sortOrder, isVisible } = body;

    if (!festivalId || !programId || !name || !type || !startDate || !endDate) {
      return errorResponse('INVALID_INPUT', '필수 항목을 모두 입력하세요');
    }

    const event = await prisma.event.create({
      data: {
        festivalId: Number(festivalId),
        programId: Number(programId),
        name,
        description: description || null,
        imageUrl: imageUrl || null,
        type,
        startDate: new Date(startDate),
        endDate: new Date(endDate),
        reward: reward || null,
        winnerCount: winnerCount || 0,
        participantLimit: participantLimit || 0,
        status: computeEventStatus(new Date(startDate), new Date(endDate)),
        price: price || null,
        duration: duration || null,
        capacity: capacity || null,
        location: location || null,
        sortOrder: sortOrder ?? 0,
        isVisible: isVisible ?? true,
      },
    });

    return successResponse(event, '이벤트가 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
