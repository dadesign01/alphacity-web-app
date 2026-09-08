import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function OPTIONS() {
  return new Response(null, {
    status: 204,
    headers: {
      'Access-Control-Allow-Origin': 'http://192.168.0.12:8080',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    },
  });
}

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
