import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const stamps = await prisma.stamp.findMany({ orderBy: { createdAt: 'desc' } });
    return successResponse(stamps);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
