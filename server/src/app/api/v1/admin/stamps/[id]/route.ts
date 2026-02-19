import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const stamp = await prisma.stamp.findUnique({ where: { id: Number(id) } });
    if (!stamp) return errorResponse('NOT_FOUND', '스탬프를 찾을 수 없습니다', 404);
    return successResponse(stamp);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await request.json();
    const stamp = await prisma.stamp.update({
      where: { id: Number(id) },
      data: {
        ...(body.name && { name: body.name }),
        ...(body.conditionType && { conditionType: body.conditionType }),
        ...(body.conditionDetail !== undefined && { conditionDetail: body.conditionDetail }),
        ...(body.imageUrl !== undefined && { imageUrl: body.imageUrl }),
      },
    });
    return successResponse(stamp, '스탬프가 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    await prisma.stamp.delete({ where: { id: Number(id) } });
    return successResponse(null, '스탬프가 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
