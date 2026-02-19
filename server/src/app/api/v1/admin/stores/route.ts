import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const stores = await prisma.store.findMany({ orderBy: { createdAt: 'desc' } });

    const [totalCount, pendingCount, approvedCount, rejectedCount] = await Promise.all([
      prisma.store.count(),
      prisma.store.count({ where: { status: 'pending' } }),
      prisma.store.count({ where: { status: 'approved' } }),
      prisma.store.count({ where: { status: 'rejected' } }),
    ]);

    return successResponse({
      stores,
      stats: { totalCount, pendingCount, approvedCount, rejectedCount },
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
