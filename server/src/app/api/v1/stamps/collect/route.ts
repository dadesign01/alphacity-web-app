import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
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
    Number(startHour) * 60 + Number(startMinute);

  const endMinutes =
    Number(endHour) * 60 + Number(endMinute);

  // 일반적인 운영시간
  if (startMinutes <= endMinutes) {
    return (
      currentMinutes >= startMinutes &&
      currentMinutes <= endMinutes
    );
  }

  // 자정을 넘어가는 운영시간
  // 예: 22:00-02:00
  return (
    currentMinutes >= startMinutes ||
    currentMinutes <= endMinutes
  );
}

export async function OPTIONS() {
  return new Response(null, {
    status: 204,
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    },
  });
}

export async function POST(request: NextRequest) {
  try {
    // 인증
    const token = request.headers
      .get('authorization')
      ?.replace('Bearer ', '');

    if (!token) {
      return errorResponse(
        'UNAUTHORIZED',
        '인증이 필요합니다',
        401,
      );
    }

    const payload = await verifyToken(token);
    const userId = payload.userId as number;

    const body = await request.json();

    const {
      qrCode,
      latitude,
      longitude,
    } = body;

    // QR 코드 확인
    if (!qrCode || typeof qrCode !== 'string') {
      return errorResponse(
        'INVALID_INPUT',
        'QR 코드가 필요합니다',
      );
    }

    // QR 코드로 스탬프 조회
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

    // 활성 상태 확인
    if (!stamp.isActive) {
      return errorResponse(
        'STAMP_INACTIVE',
        '현재 사용할 수 없는 스탬프입니다',
      );
    }

    // 운영시간 확인
    if (!isWithinOperatingHours(stamp.operatingHours)) {
      return errorResponse(
        'OUTSIDE_OPERATING_HOURS',
        '현재는 스탬프 운영시간이 아닙니다',
      );
    }

    // 이미 수집했는지 확인
    const existingUserStamp =
      await prisma.userStamp.findUnique({
        where: {
          userId_stampId: {
            userId,
            stampId: stamp.id,
          },
        },
      });

    if (existingUserStamp) {
      return errorResponse(
        'ALREADY_COLLECTED',
        '이미 수집한 스탬프입니다',
      );
    }

    // 스탬프 수집
    const userStamp = await prisma.userStamp.create({
      data: {
        userId,
        stampId: stamp.id,
        latitude,
        longitude,
      },
    });

    return successResponse(
      userStamp,
      '스탬프를 수집했습니다',
    );
  } catch (error) {
    console.error('Stamp collect error:', error);

    return errorResponse(
      'SERVER_ERROR',
      '스탬프 수집 중 오류가 발생했습니다',
      500,
    );
  }
}