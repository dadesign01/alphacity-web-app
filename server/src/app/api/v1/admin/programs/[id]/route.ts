import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import { ProgramStatus } from '@prisma/client';

export async function GET(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const program = await prisma.program.findUnique({
      where: { id: Number(id) },
      include: { events: true },
    });
    if (!program) return errorResponse('NOT_FOUND', '행사를 찾을 수 없습니다', 404);
    return successResponse(program);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await request.json();

    // 날짜 기반 상태 계산을 위해 기존 데이터 조회
    const existing = await prisma.program.findUnique({ where: { id: Number(id) } });
    if (!existing) return errorResponse('NOT_FOUND', '행사를 찾을 수 없습니다', 404);

    const newStartDate = body.startDate ? new Date(body.startDate) : existing.startDate;
    const newEndDate = body.endDate ? new Date(body.endDate) : existing.endDate;

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const start = new Date(newStartDate);
    start.setHours(0, 0, 0, 0);
    const end = new Date(newEndDate);
    end.setHours(0, 0, 0, 0);

    let status: ProgramStatus;
    if (today < start) status = ProgramStatus.scheduled;
    else if (today > end) status = ProgramStatus.ended;
    else status = ProgramStatus.in_progress;

    const program = await prisma.program.update({
      where: { id: Number(id) },
      data: {
        ...(body.name && { name: body.name }),
        ...(body.description !== undefined && { description: body.description }),
        ...(body.category && { category: body.category }),
        ...(body.subcategory !== undefined && { subcategory: body.subcategory }),
        ...(body.hasCoupon !== undefined && { hasCoupon: body.hasCoupon }),
        ...(body.imageUrl !== undefined && { imageUrl: body.imageUrl }),
        ...(body.operatingHours !== undefined && { operatingHours: body.operatingHours }),
        ...(body.location !== undefined && { location: body.location }),
        ...(body.speaker !== undefined && { speaker: body.speaker }),
        ...(body.phone !== undefined && { phone: body.phone }),
        ...(body.latitude !== undefined && { latitude: body.latitude }),
        ...(body.longitude !== undefined && { longitude: body.longitude }),
        ...(body.startDate && { startDate: newStartDate }),
        ...(body.endDate && { endDate: newEndDate }),
        status,
        ...(body.autoNotification !== undefined && { autoNotification: body.autoNotification }),
        ...(body.collectStats !== undefined && { collectStats: body.collectStats }),
      },
    });
    return successResponse(program, '행사가 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    await prisma.program.delete({ where: { id: Number(id) } });
    return successResponse(null, '행사가 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
