import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

async function getUserId(request: NextRequest): Promise<number | null> {
  const token = request.headers
    .get('authorization')
    ?.replace('Bearer ', '');

  if (!token) {
    return null;
  }

  try {
    const payload = await verifyToken(token);
    return payload.userId as number;
  } catch {
    return null;
  }
}

export async function GET(request: NextRequest) {
  try {
    const userId = await getUserId(request);

    if (!userId) {
      return errorResponse(
        'UNAUTHORIZED',
        '인증이 필요합니다',
        401,
      );
    }

  const result = await prisma.$executeRaw`
    UPDATE users
    SET first_come_popup_shown = 1
    WHERE id = ${userId}
      AND first_come_coupon = 1
      AND first_come_popup_shown = 0
  `;

    return successResponse(result === 1);
  } catch (error) {
    console.error(
      '[first-come-popup] failed',
      error,
    );

    return errorResponse(
      'SERVER_ERROR',
      '선착순 쿠폰 팝업 확인 중 오류가 발생했습니다',
      500,
    );
  }
}