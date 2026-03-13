import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const banners = await prisma.banner.findMany({
      orderBy: { sortOrder: 'asc' },
    });
    return successResponse(banners);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { title, imageUrl, sortOrder, isActive } = body;

    if (!title || !imageUrl) {
      return errorResponse('INVALID_INPUT', '제목과 이미지는 필수입니다');
    }

    const banner = await prisma.banner.create({
      data: {
        title,
        imageUrl,
        sortOrder: sortOrder ?? 0,
        isActive: isActive ?? true,
      },
    });

    return successResponse(banner, '배너가 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest) {
  try {
    const body = await request.json();
    const { orders } = body as { orders: { id: number; sortOrder: number }[] };

    if (!orders || !Array.isArray(orders)) {
      return errorResponse('INVALID_INPUT', '순서 데이터가 필요합니다');
    }

    await prisma.$transaction(
      orders.map((o) =>
        prisma.banner.update({
          where: { id: o.id },
          data: { sortOrder: o.sortOrder },
        })
      )
    );

    const banners = await prisma.banner.findMany({ orderBy: { sortOrder: 'asc' } });
    return successResponse(banners, '배너 순서가 변경되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
