import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export const dynamic = 'force-dynamic';

async function getUserId(request: NextRequest): Promise<number | null> {
  const token = request.headers.get('authorization')?.replace('Bearer ', '');
  if (!token) return null;
  try {
    const payload = await verifyToken(token);
    return payload.userId as number;
  } catch {
    return null;
  }
}

interface ActivityEntry {
  type: 'stamp' | 'mission' | 'event' | 'coupon';
  subType?: 'redeemed' | 'used' | 'expired';
  title: string;
  date: string;
  validUntil?: string | null;
}

export async function GET(request: NextRequest) {
  try {
    const userId = await getUserId(request);
    if (!userId) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

    // 모든 활동 데이터를 병렬로 조회
    const [userStamps, missionCompletions, eventParticipants, userCoupons] = await Promise.all([
      prisma.userStamp.findMany({
        where: { userId },
        include: { stamp: true },
        orderBy: { collectedAt: 'desc' },
      }),
      prisma.missionCompletion.findMany({
        where: { userId },
        include: { mission: true },
        orderBy: { completedAt: 'desc' },
      }),
      prisma.eventParticipant.findMany({
        where: { userId },
        include: { event: true },
        orderBy: { joinedAt: 'desc' },
      }),
      prisma.userCoupon.findMany({
        where: { userId },
        include: { coupon: true },
        orderBy: { createdAt: 'desc' },
      }),
    ]);

    const activities: ActivityEntry[] = [];

    // 스탬프 수집 기록
    for (const us of userStamps) {
      activities.push({
        type: 'stamp',
        title: `${us.stamp.name} 스탬프 획득`,
        date: us.collectedAt.toISOString(),
        validUntil: null,
      });
    }

    // 미션 완료 기록
    for (const mc of missionCompletions) {
      const missionTypeLabel =
        mc.mission.type === 'quiz' ? '퀴즈' :
        mc.mission.type === 'location_auth' ? '위치인증' : '체류시간';
      activities.push({
        type: 'mission',
        title: `${mc.mission.name} ${missionTypeLabel} 미션 완료`,
        date: mc.completedAt.toISOString(),
      });
    }

    // 이벤트 참여 기록
    for (const ep of eventParticipants) {
      activities.push({
        type: 'event',
        title: `${ep.event.name} 참여`,
        date: ep.joinedAt.toISOString(),
      });
    }

    // 쿠폰 기록 (교환/사용/미사용 구분)
    const now = new Date();
    for (const uc of userCoupons) {
      const validDate = uc.coupon.validUntil;
      const formatted = validDate
        ? `${validDate.getFullYear()}년 ${String(validDate.getMonth() + 1).padStart(2, '0')}월 ${String(validDate.getDate()).padStart(2, '0')}일까지 사용 가능`
        : null;
      const isExpired = validDate && validDate < now;

      if (uc.status === 'used') {
        // 사용된 쿠폰
        activities.push({
          type: 'coupon',
          subType: 'used',
          title: `${uc.coupon.name} 쿠폰 사용`,
          date: (uc.usedAt ?? uc.createdAt).toISOString(),
          validUntil: formatted,
        });
      } else if (uc.status === 'issued' && isExpired) {
        // 미사용 만료 쿠폰
        activities.push({
          type: 'coupon',
          subType: 'expired',
          title: `${uc.coupon.name} 쿠폰 미사용 만료`,
          date: validDate!.toISOString(),
          validUntil: formatted,
        });
      } else {
        // 교환(발급) 기록
        activities.push({
          type: 'coupon',
          subType: 'redeemed',
          title: `${uc.coupon.name} 쿠폰 교환`,
          date: uc.createdAt.toISOString(),
          validUntil: formatted,
        });
      }
    }

    // 최신순 정렬
    activities.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

    return successResponse(activities);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
