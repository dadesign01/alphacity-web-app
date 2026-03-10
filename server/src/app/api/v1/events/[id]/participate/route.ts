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
    });

    if (!event) return errorResponse('NOT_FOUND', '이벤트를 찾을 수 없습니다', 404);
    if (event.status === 'ended') return errorResponse('EVENT_ENDED', '종료된 이벤트입니다');

    const result = await prisma.$transaction(async (tx) => {
      const existing = await tx.eventParticipant.findUnique({
        where: { eventId_userId: { eventId, userId } },
      });
      if (existing) throw new Error('ALREADY_PARTICIPATED');

      const currentCount = await tx.eventParticipant.count({
        where: { eventId },
      });

      if (event.participantLimit > 0 && currentCount >= event.participantLimit) {
        throw new Error('EVENT_FULL');
      }

      const raffleNumber = currentCount + 1;

      return tx.eventParticipant.create({
        data: { eventId, userId, raffleNumber },
      });
    });

    return successResponse(result, '이벤트에 참여했습니다');
  } catch (error) {
    if (error instanceof Error) {
      if (error.message === 'ALREADY_PARTICIPATED') {
        return errorResponse('ALREADY_PARTICIPATED', '이미 참여한 이벤트입니다');
      }
      if (error.message === 'EVENT_FULL') {
        return errorResponse('EVENT_FULL', '참여 인원이 초과되었습니다');
      }
    }
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
