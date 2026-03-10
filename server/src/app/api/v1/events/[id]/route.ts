import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const eventId = Number(id);

    const event = await prisma.event.findUnique({
      where: { id: eventId },
      include: {
        program: { select: { name: true } },
        _count: { select: { participants: true } },
      },
    });

    if (!event) return errorResponse('NOT_FOUND', '이벤트를 찾을 수 없습니다', 404);

    let isParticipated = false;
    let myRaffleNumber: number | null = null;

    const token = request.headers.get('authorization')?.replace('Bearer ', '');
    if (token) {
      try {
        const payload = await verifyToken(token);
        const userId = payload.userId as number;
        const participant = await prisma.eventParticipant.findUnique({
          where: { eventId_userId: { eventId, userId } },
        });
        if (participant) {
          isParticipated = true;
          myRaffleNumber = participant.raffleNumber;
        }
      } catch {
        // 토큰 검증 실패 시 무시 (비로그인 상태로 처리)
      }
    }

    return successResponse({
      ...event,
      participantCount: event._count.participants,
      _count: undefined,
      isParticipated,
      myRaffleNumber,
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
