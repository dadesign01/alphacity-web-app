import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const body = await request.json();
    const { type, title, content, version, isActive } = body;

    const term = await prisma.term.update({
      where: { id: Number(id) },
      data: {
        ...(type !== undefined && { type }),
        ...(title !== undefined && { title }),
        ...(content !== undefined && { content }),
        ...(version !== undefined && { version }),
        ...(isActive !== undefined && { isActive }),
      },
    });

    return successResponse(term, '약관이 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    await prisma.term.delete({ where: { id: Number(id) } });
    return successResponse(null, '약관이 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
