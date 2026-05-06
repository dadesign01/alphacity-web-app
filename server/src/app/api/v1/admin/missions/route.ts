import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const festivalIdRaw = searchParams.get('festivalId');
    const where: Record<string, unknown> = {};
    if (festivalIdRaw && festivalIdRaw !== 'all') {
      const festivalId = Number(festivalIdRaw);
      if (Number.isInteger(festivalId)) where.festivalId = festivalId;
    }

    const missions = await prisma.mission.findMany({
      where,
      include: {
        festival: { select: { id: true, name: true } },
        place: { select: { name: true } },
        program: { select: { id: true, name: true } },
        stamp: { select: { id: true, name: true, imageUrl: true } },
        _count: { select: { completions: true } },
      },
      orderBy: { createdAt: 'desc' },
    });
    return successResponse(missions);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { festivalId, name, type, placeId, programId, stampId, question, answer, options, stayMinutes } = body;

    if (!festivalId || !name || !type) {
      return errorResponse('INVALID_INPUT', '축제, 미션명, 유형은 필수입니다');
    }

    const mission = await prisma.mission.create({
      data: { festivalId: Number(festivalId), name, type, placeId, programId, stampId, question, answer, options, stayMinutes },
    });

    return successResponse(mission, '미션이 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
