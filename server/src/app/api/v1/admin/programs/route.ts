import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import { ProgramStatus } from '@prisma/client';

function computeStatus(startDate: Date, endDate: Date): ProgramStatus {
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

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const status = searchParams.get('status');
    const category = searchParams.get('category');
    const festivalIdRaw = searchParams.get('festivalId');

    const where: Record<string, unknown> = {};
    if (category && category !== 'all') {
      where.category = category;
    }
    if (festivalIdRaw && festivalIdRaw !== 'all') {
      const festivalId = Number(festivalIdRaw);
      if (Number.isInteger(festivalId)) where.festivalId = festivalId;
    }

    const programs = await prisma.program.findMany({
      where,
      include: { festival: { select: { id: true, name: true } }, _count: { select: { events: true } } },
      orderBy: { createdAt: 'desc' },
    });

    // 날짜 기반으로 상태 계산
    const programsWithStatus = programs.map(p => ({
      ...p,
      status: computeStatus(p.startDate, p.endDate),
    }));

    // 상태 필터 적용 (계산된 상태 기준)
    const filtered = status && status !== 'all'
      ? programsWithStatus.filter(p => p.status === status)
      : programsWithStatus;

    return successResponse(filtered);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { festivalId, name, description, category, subcategory, hasCoupon, imageUrl, operatingHours, location, speaker, phone, latitude, longitude, startDate, endDate } = body;

    if (!festivalId || !name || !startDate || !endDate) {
      return errorResponse('INVALID_INPUT', '축제, 행사명, 시작일, 종료일은 필수입니다');
    }

    const program = await prisma.program.create({
      data: {
        festivalId: Number(festivalId),
        name,
        description,
        category: category || 'exhibition',
        subcategory,
        hasCoupon: hasCoupon ?? false,
        imageUrl,
        operatingHours,
        location,
        speaker,
        phone,
        ...(latitude !== undefined && { latitude }),
        ...(longitude !== undefined && { longitude }),
        startDate: new Date(startDate),
        endDate: new Date(endDate),
        status: computeStatus(new Date(startDate), new Date(endDate)),
      },
    });

    return successResponse(program, '행사가 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
