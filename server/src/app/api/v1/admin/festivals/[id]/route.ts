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

export async function GET(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const festival = await prisma.festival.findUnique({
      where: { id: Number(id) },
      include: { _count: { select: { programs: true, events: true, missions: true, stamps: true, coupons: true, stores: true } } },
    });
    if (!festival) return errorResponse('NOT_FOUND', '축제를 찾을 수 없습니다', 404);
    return successResponse({ ...festival, status: computeStatus(festival.startDate, festival.endDate) });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await request.json();

    const existing = await prisma.festival.findUnique({ where: { id: Number(id) } });
    if (!existing) return errorResponse('NOT_FOUND', '축제를 찾을 수 없습니다', 404);

    const newStartDate = body.startDate ? new Date(body.startDate) : existing.startDate;
    const newEndDate = body.endDate ? new Date(body.endDate) : existing.endDate;

    const festival = await prisma.festival.update({
      where: { id: Number(id) },
      data: {
        ...(body.name && { name: body.name }),
        ...(body.description !== undefined && { description: body.description }),
        ...(body.imageUrl !== undefined && { imageUrl: body.imageUrl }),
        ...(body.bannerUrl !== undefined && { bannerUrl: body.bannerUrl }),
        ...(body.startDate && { startDate: newStartDate }),
        ...(body.endDate && { endDate: newEndDate }),
        ...(body.latitude !== undefined && { latitude: body.latitude !== null && body.latitude !== '' ? Number(body.latitude) : null }),
        ...(body.longitude !== undefined && { longitude: body.longitude !== null && body.longitude !== '' ? Number(body.longitude) : null }),
        ...(body.address !== undefined && { address: body.address }),
        ...(body.sortOrder !== undefined && { sortOrder: body.sortOrder }),
        ...(body.isActive !== undefined && { isActive: body.isActive }),
        status: computeStatus(newStartDate, newEndDate),
      },
    });

    return successResponse(festival, '축제가 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(_: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    await prisma.festival.delete({ where: { id: Number(id) } });
    return successResponse(null, '축제가 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
