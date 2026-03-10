import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

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
        ...(body.startDate && { startDate: new Date(body.startDate) }),
        ...(body.endDate && { endDate: new Date(body.endDate) }),
        ...(body.status && { status: body.status }),
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
