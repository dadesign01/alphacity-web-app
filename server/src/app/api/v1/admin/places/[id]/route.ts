import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const place = await prisma.place.findUnique({
      where: { id: Number(id) },
      include: { missions: true },
    });
    if (!place) return errorResponse('NOT_FOUND', '장소를 찾을 수 없습니다', 404);
    return successResponse(place);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await request.json();
    const place = await prisma.place.update({
      where: { id: Number(id) },
      data: {
        ...(body.name && { name: body.name }),
        ...(body.category && { category: body.category }),
        ...(body.latitude !== undefined && { latitude: body.latitude }),
        ...(body.longitude !== undefined && { longitude: body.longitude }),
        ...(body.address !== undefined && { address: body.address }),
        ...(body.description !== undefined && { description: body.description }),
        ...(body.ttsText !== undefined && { ttsText: body.ttsText }),
        ...(body.isActive !== undefined && { isActive: body.isActive }),
      },
    });
    return successResponse(place, '장소가 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    await prisma.place.delete({ where: { id: Number(id) } });
    return successResponse(null, '장소가 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
