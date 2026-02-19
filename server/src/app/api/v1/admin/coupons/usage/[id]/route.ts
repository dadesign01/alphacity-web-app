import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await request.json();
    const { status } = body; // 'used' or 'rejected'

    if (!['used', 'rejected'].includes(status)) {
      return errorResponse('INVALID_INPUT', '상태는 used 또는 rejected만 가능합니다');
    }

    const userCoupon = await prisma.userCoupon.update({
      where: { id: Number(id) },
      data: {
        status,
        ...(status === 'used' && { usedAt: new Date() }),
      },
    });

    return successResponse(userCoupon, status === 'used' ? '사용 처리되었습니다' : '반려 처리되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
