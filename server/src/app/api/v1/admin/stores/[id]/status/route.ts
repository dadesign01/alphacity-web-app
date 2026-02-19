import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await request.json();
    const { status } = body;

    if (!['approved', 'rejected'].includes(status)) {
      return errorResponse('INVALID_INPUT', '상태는 approved 또는 rejected만 가능합니다');
    }

    const store = await prisma.store.update({
      where: { id: Number(id) },
      data: { status },
    });

    return successResponse(store, status === 'approved' ? '승인되었습니다' : '반려되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
