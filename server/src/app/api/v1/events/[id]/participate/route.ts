import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function POST(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const token = request.headers.get('authorization')?.replace('Bearer ', '');
    if (!token) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

    const payload = await verifyToken(token);
    const userId = payload.userId as number;
    const { id } = await params;
    const eventId = Number(id);

    const event = await prisma.event.findUnique({
      where: { id: eventId },
      include: { _count: { select: { participants: true } } },
    });

    if (!event) return errorResponse('NOT_FOUND', '이벤트를 찾을 수 없습니다', 404);

    if (event.participantLimit > 0 && event._count.participants >= event.participantLimit) {
      return errorResponse('EVENT_FULL', '참여 인원이 초과되었습니다');
    }

    const existing = await prisma.eventParticipant.findUnique({
      where: { eventId_userId: { eventId, userId } },
    });
    if (existing) return errorResponse('ALREADY_PARTICIPATED', '이미 참여한 이벤트입니다');

    const participant = await prisma.eventParticipant.create({
      data: { eventId, userId },
    });

    return successResponse(participant, '이벤트에 참여했습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
