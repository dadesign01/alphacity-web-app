import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import { NextRequest } from 'next/server';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();

    const {
      festivalId,
      name,
      category,
      ownerName,
      phone,
      address,
      addressDetail,
      description,
      imageUrl,
      storeCode,
      operatingDays,
      openTime,
      closeTime,
    } = body;

    if (!name || !category || !ownerName || !phone) {
      return errorResponse('INVALID_INPUT', '필수 항목을 입력해주세요', 400);
    }

    const validCategories = ['cafe', 'restaurant', 'shopping', 'hotel', 'convenience'];
    if (!validCategories.includes(category)) {
      return errorResponse('INVALID_INPUT', '유효하지 않은 카테고리입니다', 400);
    }

    let resolvedFestivalId = festivalId ? Number(festivalId) : null;
    if (!resolvedFestivalId || Number.isNaN(resolvedFestivalId)) {
      const fallback = await prisma.festival.findFirst({
        where: { isActive: true },
        orderBy: [{ status: 'asc' }, { sortOrder: 'asc' }, { id: 'asc' }],
        select: { id: true },
      });
      if (!fallback) {
        return errorResponse('NO_FESTIVAL', '등록 가능한 축제가 없습니다', 400);
      }
      resolvedFestivalId = fallback.id;
    }

    if (storeCode) {
      const existing = await prisma.store.findUnique({
        where: { storeCode },
      });
      if (existing) {
        return errorResponse('DUPLICATE_CODE', '이미 사용 중인 상점 코드입니다', 409);
      }
    }

    const store = await prisma.store.create({
      data: {
        festivalId: resolvedFestivalId,
        name,
        category,
        ownerName,
        phone,
        address: address || null,
        addressDetail: addressDetail || null,
        description: description || null,
        imageUrl: imageUrl || null,
        storeCode: storeCode || null,
        operatingDays: operatingDays || null,
        openTime: openTime || null,
        closeTime: closeTime || null,
        status: 'pending',
        requestDate: new Date(),
      },
    });

    return successResponse(store, '상점 등록 신청이 완료되었습니다');
  } catch (e) {
    console.error('[stores/register] failed', e);
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
