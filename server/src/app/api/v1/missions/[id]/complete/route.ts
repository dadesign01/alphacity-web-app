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
    const missionId = Number(id);

    const mission = await prisma.mission.findUnique({
      where: { id: missionId },
      include: { stamp: true },
    });
    if (!mission) return errorResponse('NOT_FOUND', '미션을 찾을 수 없습니다', 404);

    const existing = await prisma.missionCompletion.findUnique({
      where: { missionId_userId: { missionId, userId } },
    });
    if (existing) return errorResponse('ALREADY_COMPLETED', '이미 완료한 미션입니다');

    // 퀴즈 미션인 경우 정답 확인
    if (mission.type === 'quiz') {
      const body = await request.json();
      if (body.answer !== mission.answer) {
        return errorResponse('WRONG_ANSWER', '정답이 아닙니다');
      }
    }

    const completion = await prisma.missionCompletion.create({
      data: { missionId, userId },
    });

    // 미션에 스탬프가 연결된 경우 자동 적립
    if (mission.stampId) {
      await prisma.userStamp.upsert({
        where: { userId_stampId: { userId, stampId: mission.stampId } },
        create: { userId, stampId: mission.stampId },
        update: {},
      });
    }

    return successResponse(
      { ...completion, stamp: mission.stamp },
      '미션을 완료했습니다'
    );
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
