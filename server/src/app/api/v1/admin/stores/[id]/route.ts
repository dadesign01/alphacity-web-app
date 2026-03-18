import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import { NextRequest } from 'next/server';

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const store = await prisma.store.findUnique({
      where: { id: parseInt(id) },
    });
    if (!store) {
      return errorResponse('NOT_FOUND', '상점을 찾을 수 없습니다', 404);
    }
    return successResponse(store);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const storeId = parseInt(id);
    const body = await request.json();

    const updateData: Record<string, unknown> = {};
    if (body.status) updateData.status = body.status;
    if (body.programId !== undefined) updateData.programId = body.programId || null;
    if (body.missionId !== undefined) updateData.missionId = body.missionId || null;

    const store = await prisma.store.update({
      where: { id: storeId },
      data: updateData,
    });

    // 쿠폰 연결 업데이트
    if (body.couponIds !== undefined) {
      await prisma.storeCoupon.deleteMany({ where: { storeId } });
      if (body.couponIds.length > 0) {
        await prisma.storeCoupon.createMany({
          data: body.couponIds.map((couponId: number) => ({ storeId, couponId })),
        });
      }
    }

    const result = await prisma.store.findUnique({
      where: { id: storeId },
      include: {
        program: { select: { id: true, name: true } },
        mission: { select: { id: true, name: true, type: true } },
        storeCoupons: { include: { coupon: { select: { id: true, name: true } } } },
      },
    });

    return successResponse(result, '상점이 업데이트되었습니다');
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
    await prisma.store.delete({
      where: { id: parseInt(id) },
    });
    return successResponse(null, '상점이 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
