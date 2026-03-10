import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const status = searchParams.get('status');
    const category = searchParams.get('category');

    const where: Record<string, unknown> = {};
    if (status && status !== 'all') {
      where.status = status;
    }
    if (category && category !== 'all') {
      where.category = category;
    }

    const programs = await prisma.program.findMany({
      where,
      include: { _count: { select: { events: true } } },
      orderBy: { createdAt: 'desc' },
    });

    return successResponse(programs);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { name, description, category, subcategory, hasCoupon, imageUrl, operatingHours, location, speaker, startDate, endDate, status } = body;

    if (!name || !startDate || !endDate) {
      return errorResponse('INVALID_INPUT', '행사명, 시작일, 종료일은 필수입니다');
    }

    const program = await prisma.program.create({
      data: {
        name,
        description,
        category: category || 'exhibition',
        subcategory,
        hasCoupon: hasCoupon ?? false,
        imageUrl,
        operatingHours,
        location,
        speaker,
        startDate: new Date(startDate),
        endDate: new Date(endDate),
        status: status || 'scheduled',
      },
    });

    return successResponse(program, '행사가 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
