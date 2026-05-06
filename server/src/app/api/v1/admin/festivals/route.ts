import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import { FestivalStatus } from '@prisma/client';

function computeStatus(startDate: Date, endDate: Date): FestivalStatus {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const start = new Date(startDate);
  start.setHours(0, 0, 0, 0);
  const end = new Date(endDate);
  end.setHours(0, 0, 0, 0);
  if (today < start) return 'scheduled';
  if (today > end) return 'ended';
  return 'in_progress';
}

export async function GET() {
  try {
    const festivals = await prisma.festival.findMany({
      orderBy: [{ sortOrder: 'asc' }, { createdAt: 'desc' }],
      include: { _count: { select: { programs: true, events: true, missions: true, stamps: true, coupons: true, stores: true } } },
    });
    const result = festivals.map(f => ({ ...f, status: computeStatus(f.startDate, f.endDate) }));
    return successResponse(result);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { name, description, imageUrl, bannerUrl, startDate, endDate, latitude, longitude, address, sortOrder, isActive } = body;

    if (!name || !startDate || !endDate) {
      return errorResponse('INVALID_INPUT', '축제명, 시작일, 종료일은 필수입니다');
    }

    const festival = await prisma.festival.create({
      data: {
        name,
        description: description || null,
        imageUrl: imageUrl || null,
        bannerUrl: bannerUrl || null,
        startDate: new Date(startDate),
        endDate: new Date(endDate),
        latitude: latitude !== undefined && latitude !== null && latitude !== '' ? Number(latitude) : null,
        longitude: longitude !== undefined && longitude !== null && longitude !== '' ? Number(longitude) : null,
        address: address || null,
        sortOrder: sortOrder ?? 0,
        isActive: isActive ?? true,
        status: computeStatus(new Date(startDate), new Date(endDate)),
      },
    });

    return successResponse(festival, '축제가 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
