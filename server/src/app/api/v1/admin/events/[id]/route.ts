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

export async function GET(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const event = await prisma.event.findUnique({
      where: { id: Number(id) },
      include: { program: true, _count: { select: { participants: true } } },
    });
    if (!event) return errorResponse('NOT_FOUND', '이벤트를 찾을 수 없습니다', 404);

    // 날짜 기반으로 상태 자동 계산
    const eventWithStatus = {
      ...event,
      status: computeEventStatus(event.startDate, event.endDate),
    };

    return successResponse(eventWithStatus);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await request.json();

    // 날짜가 변경되면 새 날짜 기준으로, 변경되지 않으면 기존 날짜 기준으로 상태 계산
    const existing = await prisma.event.findUnique({ where: { id: Number(id) } });
    if (!existing) return errorResponse('NOT_FOUND', '이벤트를 찾을 수 없습니다', 404);

    const newStartDate = body.startDate ? new Date(body.startDate) : existing.startDate;
    const newEndDate = body.endDate ? new Date(body.endDate) : existing.endDate;

    const event = await prisma.event.update({
      where: { id: Number(id) },
      data: {
        ...(body.festivalId && { festivalId: Number(body.festivalId) }),
        ...(body.programId && { programId: Number(body.programId) }),
        ...(body.name && { name: body.name }),
        ...(body.description !== undefined && { description: body.description }),
        ...(body.imageUrl !== undefined && { imageUrl: body.imageUrl }),
        ...(body.type && { type: body.type }),
        ...(body.startDate && { startDate: new Date(body.startDate) }),
        ...(body.endDate && { endDate: new Date(body.endDate) }),
        ...(body.reward !== undefined && { reward: body.reward }),
        ...(body.winnerCount !== undefined && { winnerCount: body.winnerCount }),
        ...(body.participantLimit !== undefined && { participantLimit: body.participantLimit }),
        status: computeEventStatus(newStartDate, newEndDate),
        ...(body.price !== undefined && { price: body.price || null }),
        ...(body.duration !== undefined && { duration: body.duration || null }),
        ...(body.capacity !== undefined && { capacity: body.capacity || null }),
        ...(body.location !== undefined && { location: body.location || null }),
        ...(body.sortOrder !== undefined && { sortOrder: body.sortOrder }),
        ...(body.isVisible !== undefined && { isVisible: body.isVisible }),
      },
    });
    return successResponse(event, '이벤트가 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    await prisma.event.delete({ where: { id: Number(id) } });
    return successResponse(null, '이벤트가 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
