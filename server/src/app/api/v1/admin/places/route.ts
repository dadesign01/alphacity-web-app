import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const places = await prisma.place.findMany({
      include: { _count: { select: { missions: true } } },
      orderBy: { createdAt: 'desc' },
    });
    return successResponse(places);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { name, category, latitude, longitude, address, description, ttsText } = body;

    if (!name || !category || latitude === undefined || longitude === undefined) {
      return errorResponse('INVALID_INPUT', '장소명, 카테고리, 위도, 경도는 필수입니다');
    }

    const place = await prisma.place.create({
      data: { name, category, latitude, longitude, address, description, ttsText },
    });

    return successResponse(place, '장소가 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
