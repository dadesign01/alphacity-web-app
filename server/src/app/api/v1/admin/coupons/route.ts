import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const coupons = await prisma.coupon.findMany({
      include: {
        program: { select: { id: true, name: true } },
        _count: { select: { userCoupons: true } },
      },
      orderBy: { createdAt: 'desc' },
    });
    return successResponse(coupons);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { name, description, requiredStamps, validUntil, imageUrl, programId } = body;

    if (!name || !validUntil) {
      return errorResponse('INVALID_INPUT', '쿠폰명과 유효기간은 필수입니다');
    }

    const coupon = await prisma.coupon.create({
      data: {
        name,
        description,
        requiredStamps: requiredStamps || 0,
        validUntil: new Date(validUntil),
        imageUrl,
        programId: programId || null,
      },
    });

    return successResponse(coupon, '쿠폰이 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
