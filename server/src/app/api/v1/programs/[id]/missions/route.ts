import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const programId = Number(id);

    const program = await prisma.program.findUnique({ where: { id: programId } });
    if (!program) {
      return errorResponse('NOT_FOUND', '프로그램을 찾을 수 없습니다', 404);
    }

    const missions = await prisma.mission.findMany({
      where: { programId },
      include: {
        place: { select: { name: true, latitude: true, longitude: true } },
      },
      orderBy: { createdAt: 'desc' },
    });

    // 로그인한 사용자의 미션 완료 여부 확인
    let completedMissionIds: Set<number> = new Set();
    const token = request.headers.get('authorization')?.replace('Bearer ', '');
    if (token) {
      try {
        const payload = await verifyToken(token);
        const userId = payload.userId as number;
        const completions = await prisma.missionCompletion.findMany({
          where: {
            userId,
            missionId: { in: missions.map(m => m.id) },
          },
          select: { missionId: true },
        });
        completedMissionIds = new Set(completions.map(c => c.missionId));
      } catch {
        // 토큰 검증 실패 시 무시
      }
    }

    const result = missions.map(mission => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { answer, ...rest } = mission as typeof mission & { options?: string | null };
      let parsedOptions: string[] | null = null;
      if (rest.options) {
        try { parsedOptions = JSON.parse(rest.options); } catch { /* ignore */ }
      }
      return {
        ...rest,
        place: rest.place
          ? {
              name: rest.place.name,
              latitude: rest.place.latitude != null ? Number(rest.place.latitude) : null,
              longitude: rest.place.longitude != null ? Number(rest.place.longitude) : null,
            }
          : null,
        options: parsedOptions,
        isCompleted: completedMissionIds.has(mission.id),
      };
    });

    return successResponse(result);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
