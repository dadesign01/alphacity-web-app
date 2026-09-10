import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const stampId = Number(id);

    if (!Number.isInteger(stampId)) {
      return errorResponse(
        'INVALID_INPUT',
        '유효하지 않은 스탬프 ID입니다',
        400
      );
    }

    // 스탬프 존재 여부 확인
    const stamp = await prisma.stamp.findUnique({
      where: { id: stampId },
    });

    if (!stamp) {
      return errorResponse(
        'NOT_FOUND',
        '스탬프를 찾을 수 없습니다',
        404
      );
    }

    // 해당 스탬프에 연결된 미션 조회
    const missions = await prisma.mission.findMany({
      where: {
        stampId,
      },
      include: {
        place: {
          select: {
            name: true,
            latitude: true,
            longitude: true,
          },
        },
      },
      orderBy: {
        createdAt: 'desc',
      },
    });

    // 로그인한 사용자의 미션 완료 여부 확인
    let completedMissionIds: Set<number> = new Set();

    const token = request.headers
      .get('authorization')
      ?.replace('Bearer ', '');

    if (token) {
      try {
        const payload = await verifyToken(token);
        const userId = payload.userId as number;

        const completions =
          await prisma.missionCompletion.findMany({
            where: {
              userId,
              missionId: {
                in: missions.map((mission) => mission.id),
              },
            },
            select: {
              missionId: true,
            },
          });

        completedMissionIds = new Set(
          completions.map((completion) => completion.missionId)
        );
      } catch {
        // 토큰 검증 실패 시 완료 여부 없이 진행
      }
    }

    const result = missions.map((mission) => {
      // 정답은 사용자에게 노출하지 않음
      const {
        answer,
        ...rest
      } = mission as typeof mission & {
        options?: string | null;
      };

      let parsedOptions: string[] | null = null;

      if (rest.options) {
        try {
          parsedOptions = JSON.parse(rest.options);
        } catch {
          // JSON 파싱 실패 시 null
        }
      }

      return {
        ...rest,
        place: rest.place
          ? {
              name: rest.place.name,
              latitude:
                rest.place.latitude != null
                  ? Number(rest.place.latitude)
                  : null,
              longitude:
                rest.place.longitude != null
                  ? Number(rest.place.longitude)
                  : null,
            }
          : null,
        options: parsedOptions,
        isCompleted: completedMissionIds.has(mission.id),
      };
    });

    return successResponse(result);
  } catch (error) {
    console.error(
      '[GET /stamps/[id]/missions]',
      error
    );

    return errorResponse(
      'SERVER_ERROR',
      '서버 오류가 발생했습니다',
      500
    );
  }
}