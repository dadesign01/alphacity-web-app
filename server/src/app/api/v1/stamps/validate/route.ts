import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

function isWithinOperatingHours(operatingHours: string | null) {
  if (!operatingHours) return true;

  const match = operatingHours.match(
    /^([01]\d|2[0-3]):([0-5]\d)-([01]\d|2[0-3]):([0-5]\d)$/,
  );

  if (!match) return true;

  const [, startHour, startMinute, endHour, endMinute] = match;

  const now = new Date();

  const currentMinutes =
    now.getHours() * 60 + now.getMinutes();

  const startMinutes =
    Number(startHour) * 60 +
    Number(startMinute);

  const endMinutes =
    Number(endHour) * 60 +
    Number(endMinute);

  if (startMinutes <= endMinutes) {
    return (
      currentMinutes >= startMinutes &&
      currentMinutes <= endMinutes
    );
  }

  return (
    currentMinutes >= startMinutes ||
    currentMinutes <= endMinutes
  );
}

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);

    const qrCode = searchParams.get('qrCode');
    const programIdParam = searchParams.get('programId');
    const stampIdParam = searchParams.get('stampId');

    if (
      !qrCode ||
      !programIdParam ||
      !stampIdParam
    ) {
      return errorResponse(
        'INVALID_INPUT',
        'QR 코드 정보가 필요합니다',
        400,
      );
    }

    const programId = Number(programIdParam);
    const stampId = Number(stampIdParam);

    if (
      !Number.isInteger(programId) ||
      !Number.isInteger(stampId)
    ) {
      return errorResponse(
        'INVALID_INPUT',
        '잘못된 QR 코드 정보입니다',
        400,
      );
    }

    const stamp = await prisma.stamp.findUnique({
      where: {
        qrCode,
      },
    });

    if (!stamp) {
      return errorResponse(
        'NOT_FOUND',
        '유효하지 않은 QR 코드입니다',
        404,
      );
    }

    if (stamp.id !== stampId) {
      return errorResponse(
        'INVALID_STAMP',
        '유효하지 않은 스탬프입니다',
        400,
      );
    }

  if (stamp.programId !== programId) {
    return errorResponse(
      'PROGRAM_MISMATCH',
      '다른 행사 QR 코드입니다.',
      400,
    );
  }

    if (!stamp.isActive) {
      return errorResponse(
        'STAMP_INACTIVE',
        '현재 사용할 수 없는 스탬프입니다',
      );
    }

    if (!isWithinOperatingHours(stamp.operatingHours)) {
      return errorResponse(
        'OUTSIDE_OPERATING_HOURS',
        '현재는 스탬프 운영시간이 아닙니다',
      );
    }

    return successResponse(
      {
        valid: true,
        programId: stamp.programId,
        stampId: stamp.id,
        qrCode: stamp.qrCode,
      },
      '유효한 QR 코드입니다',
    );
  } catch (error) {
    console.error('Stamp validate error:', error);

    return errorResponse(
      'SERVER_ERROR',
      'QR 코드 확인 중 오류가 발생했습니다',
      500,
    );
  }
}