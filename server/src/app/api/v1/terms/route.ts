import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);
    const type = searchParams.get('type');

    const where: Record<string, unknown> = { isActive: true };
    if (type) {
      where.type = type;
    }

    const terms = await prisma.term.findMany({
      where,
      orderBy: { updatedAt: 'desc' },
    });

    return successResponse(terms);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
