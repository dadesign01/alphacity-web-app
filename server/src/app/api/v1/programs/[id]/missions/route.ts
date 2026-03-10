import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
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

    return successResponse(missions);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
