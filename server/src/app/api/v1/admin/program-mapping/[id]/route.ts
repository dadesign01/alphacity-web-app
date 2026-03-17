import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export const dynamic = 'force-dynamic';

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const programId = parseInt(id);

    const [program, programPlaces, missions, stamps] = await Promise.all([
      prisma.program.findUnique({
        where: { id: programId },
        select: {
          id: true, name: true, description: true, category: true,
          startDate: true, endDate: true, status: true,
        },
      }),
      prisma.programPlace.findMany({
        where: { programId },
        include: { place: true },
        orderBy: { sortOrder: 'asc' },
      }),
      prisma.mission.findMany({
        where: { programId },
        include: { place: { select: { id: true, name: true } } },
        orderBy: { createdAt: 'asc' },
      }),
      prisma.stamp.findMany({
        where: { programId },
        include: { place: { select: { id: true, name: true } } },
        orderBy: { createdAt: 'asc' },
      }),
    ]);

    if (!program) {
      return errorResponse('NOT_FOUND', '프로그램을 찾을 수 없습니다', 404);
    }

    return successResponse({
      program,
      places: programPlaces.map(pp => ({ ...pp.place, sortOrder: pp.sortOrder })),
      missions,
      stamps,
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const programId = parseInt(id);
    const body = await request.json();

    const program = await prisma.program.findUnique({ where: { id: programId } });
    if (!program) {
      return errorResponse('NOT_FOUND', '프로그램을 찾을 수 없습니다', 404);
    }

    const { places, missions, stamps } = body;

    await prisma.$transaction(async (tx) => {
      // === Places: delete all, re-create ===
      if (places) {
        await tx.programPlace.deleteMany({ where: { programId } });
        if (places.placeIds && places.placeIds.length > 0) {
          await tx.programPlace.createMany({
            data: places.placeIds.map((placeId: number, index: number) => ({
              programId,
              placeId,
              sortOrder: index,
            })),
          });
        }
      }

      // === Missions: unlink, link, create ===
      if (missions) {
        if (missions.unlink && missions.unlink.length > 0) {
          await tx.mission.updateMany({
            where: { id: { in: missions.unlink }, programId },
            data: { programId: null },
          });
        }
        if (missions.link && missions.link.length > 0) {
          await tx.mission.updateMany({
            where: { id: { in: missions.link } },
            data: { programId },
          });
        }
        if (missions.create && missions.create.length > 0) {
          for (const m of missions.create) {
            await tx.mission.create({
              data: {
                name: m.name,
                type: m.type,
                placeId: m.placeId || null,
                programId,
                question: m.question || null,
                answer: m.answer || null,
                options: m.options || null,
                stayMinutes: m.stayMinutes || null,
              },
            });
          }
        }
      }

      // === Stamps: unlink, link, create ===
      if (stamps) {
        if (stamps.unlink && stamps.unlink.length > 0) {
          await tx.stamp.updateMany({
            where: { id: { in: stamps.unlink }, programId },
            data: { programId: null },
          });
        }
        if (stamps.link && stamps.link.length > 0) {
          await tx.stamp.updateMany({
            where: { id: { in: stamps.link } },
            data: { programId },
          });
        }
        if (stamps.create && stamps.create.length > 0) {
          for (const s of stamps.create) {
            await tx.stamp.create({
              data: {
                name: s.name,
                conditionType: s.conditionType || 'mission_complete',
                conditionDetail: s.conditionDetail || null,
                imageUrl: s.imageUrl || null,
                programId,
                placeId: s.placeId || null,
              },
            });
          }
        }
      }
    });

    return successResponse(null, '프로그램 연결 설정이 저장되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
