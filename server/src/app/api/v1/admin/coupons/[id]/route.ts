import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const coupon = await prisma.coupon.findUnique({ where: { id: Number(id) } });
    if (!coupon) return errorResponse('NOT_FOUND', '쿠폰을 찾을 수 없습니다', 404);
    return successResponse(coupon);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await request.json();
    const coupon = await prisma.coupon.update({
      where: { id: Number(id) },
      data: {
        ...(body.name && { name: body.name }),
        ...(body.description !== undefined && { description: body.description }),
        ...(body.requiredStamps !== undefined && { requiredStamps: body.requiredStamps }),
        ...(body.validUntil && { validUntil: new Date(body.validUntil) }),
        ...(body.imageUrl !== undefined && { imageUrl: body.imageUrl }),
        ...(body.programId !== undefined && { programId: body.programId || null }),
      },
    });
    return successResponse(coupon, '쿠폰이 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    await prisma.coupon.delete({ where: { id: Number(id) } });
    return successResponse(null, '쿠폰이 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
