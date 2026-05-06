import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const festivalIdRaw = searchParams.get('festivalId');
    const where: Record<string, unknown> = {};
    if (festivalIdRaw && festivalIdRaw !== 'all') {
      const festivalId = Number(festivalIdRaw);
      if (Number.isInteger(festivalId)) where.festivalId = festivalId;
    }
    const stamps = await prisma.stamp.findMany({ where, orderBy: { createdAt: 'desc' } });
    return successResponse(stamps);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
