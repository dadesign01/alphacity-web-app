import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const userId = parseInt(id);

    const user = await prisma.user.findUnique({
      where: { id: userId },
      include: {
        userStamps: {
          include: { stamp: { select: { id: true, name: true, imageUrl: true, conditionType: true } } },
          orderBy: { collectedAt: 'desc' },
        },
        userCoupons: {
          include: { coupon: { select: { id: true, name: true, imageUrl: true } } },
          orderBy: { createdAt: 'desc' },
        },
        missionCompletions: {
          include: { mission: { select: { id: true, name: true, type: true } } },
          orderBy: { completedAt: 'desc' },
        },
        eventParticipants: {
          include: { event: { select: { id: true, name: true, type: true } } },
          orderBy: { joinedAt: 'desc' },
        },
      },
    });

    if (!user) {
      return errorResponse('NOT_FOUND', '사용자를 찾을 수 없습니다', 404);
    }

    return successResponse(user);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
