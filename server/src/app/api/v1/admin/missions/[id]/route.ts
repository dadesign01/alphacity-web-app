import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const mission = await prisma.mission.findUnique({
      where: { id: Number(id) },
      include: { place: true },
    });
    if (!mission) return errorResponse('NOT_FOUND', '미션을 찾을 수 없습니다', 404);
    return successResponse(mission);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await request.json();
    const mission = await prisma.mission.update({
      where: { id: Number(id) },
      data: {
        ...(body.name && { name: body.name }),
        ...(body.type && { type: body.type }),
        ...(body.placeId !== undefined && { placeId: body.placeId }),
        ...(body.question !== undefined && { question: body.question }),
        ...(body.answer !== undefined && { answer: body.answer }),
        ...(body.stayMinutes !== undefined && { stayMinutes: body.stayMinutes }),
      },
    });
    return successResponse(mission, '미션이 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    await prisma.mission.delete({ where: { id: Number(id) } });
    return successResponse(null, '미션이 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
