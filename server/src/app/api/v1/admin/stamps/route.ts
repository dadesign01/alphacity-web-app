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

    const stamps = await prisma.stamp.findMany({
      where,
      include: {
        festival: { select: { id: true, name: true } },
        program: { select: { id: true, name: true } },
        place: { select: { id: true, name: true } },
        _count: { select: { userStamps: true } },
      },
      orderBy: { createdAt: 'desc' },
    });
    return successResponse(stamps);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { festivalId, name, conditionType, conditionDetail, imageUrl, programId, placeId } = body;

    if (!festivalId || !name || !conditionType) {
      return errorResponse('INVALID_INPUT', '축제, 스탬프명, 발급 조건은 필수입니다');
    }

    const stamp = await prisma.stamp.create({
      data: { festivalId: Number(festivalId), name, conditionType, conditionDetail, imageUrl, programId: programId || null, placeId: placeId || null },
    });

    return successResponse(stamp, '스탬프가 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
