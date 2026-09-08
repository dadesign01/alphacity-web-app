import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

  function generateQrCode() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    const random = Array.from(
      crypto.getRandomValues(new Uint8Array(8)),
      n => chars[n % chars.length]
    ).join('');

    return `QR-${random}`;
  }

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const festivalIdRaw = searchParams.get('festivalId');

    const where: Record<string, unknown> = {};

    if (festivalIdRaw && festivalIdRaw !== 'all') {
      const festivalId = Number(festivalIdRaw);

      if (Number.isInteger(festivalId)) {
        where.festivalId = festivalId;
      }
    }

    const stamps = await prisma.stamp.findMany({
      where,
      include: {
        festival: { select: { id: true, name: true } },
        program: { select: { id: true, name: true } },
        place: { select: { id: true, name: true } },
        _count: { select: { userStamps: true } },
      },
      orderBy: { createdAt: 'desc' },
    });

    return successResponse(stamps);
  } catch {
    return errorResponse(
      'SERVER_ERROR',
      '서버 오류가 발생했습니다',
      500,
    );
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();

    const {
      festivalId,
      name,
      conditionType,
      conditionDetail,
      imageUrl,
      programId,
      placeId,
      description,
      operatingHours,
      isActive,
      useQrCode,
    } = body;

    if (!festivalId || !name || !conditionType) {
      return errorResponse(
        'INVALID_INPUT',
        '축제, 스탬프명, 발급 조건은 필수입니다',
      );
    }

    const useQr = useQrCode === true;

    // QR을 사용하는 스탬프는 행사(프로그램) 필수
    if (useQr && !programId) {
      return errorResponse(
        'INVALID_INPUT',
        'QR을 사용하는 경우 행사(프로그램)를 선택해야 합니다',
      );
    }

    const parsedProgramId = programId
      ? Number(programId)
      : null;

    // 같은 행사(프로그램)에 스탬프가 이미 존재하는지 확인
    if (parsedProgramId !== null) {
      const existingStamp = await prisma.stamp.findUnique({
        where: {
          programId: parsedProgramId,
        },
      });

      if (existingStamp) {
        return errorResponse(
          'DUPLICATE_STAMP',
          '해당 행사(프로그램)에 이미 스탬프가 등록되어 있습니다',
        );
      }
    }

    const stamp = await prisma.stamp.create({
      data: {
        festivalId: Number(festivalId),
        name,
        conditionType,
        conditionDetail: conditionDetail || null,
        imageUrl: imageUrl || null,
        programId: parsedProgramId,
        placeId: placeId ? Number(placeId) : null,
        description: description || null,
        operatingHours: operatingHours || null,

        // QR 사용 여부에 따라 생성
        qrCode: useQr ? generateQrCode() : null,

        isActive: isActive !== false,
      },
    });

    return successResponse(
      stamp,
      '스탬프가 등록되었습니다',
    );
  } catch (error) {
    console.error('Stamp create error:', error);

    return errorResponse(
      'SERVER_ERROR',
      '서버 오류가 발생했습니다',
      500,
    );
  }
}