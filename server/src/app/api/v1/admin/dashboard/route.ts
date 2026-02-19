import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const [programCount, userCount, stampCount, couponCount] = await Promise.all([
      prisma.program.count({ where: { status: 'in_progress' } }),
      prisma.user.count(),
      prisma.userStamp.count(),
      prisma.userCoupon.count({ where: { status: 'used' } }),
    ]);

    // 최근 활동 수집
    const [recentUsers, recentCoupons, recentMissions, recentStores, recentNotifications] = await Promise.all([
      prisma.user.findMany({ orderBy: { createdAt: 'desc' }, take: 5, select: { nickname: true, createdAt: true } }),
      prisma.userCoupon.findMany({
        where: { status: { in: ['used', 'pending'] } },
        orderBy: { createdAt: 'desc' },
        take: 5,
        select: { status: true, createdAt: true, user: { select: { nickname: true } }, coupon: { select: { name: true } } },
      }),
      prisma.missionCompletion.findMany({
        orderBy: { completedAt: 'desc' },
        take: 5,
        select: { completedAt: true, user: { select: { nickname: true } }, mission: { select: { name: true } } },
      }),
      prisma.store.findMany({ orderBy: { createdAt: 'desc' }, take: 5, select: { name: true, status: true, createdAt: true } }),
      prisma.notification.findMany({ orderBy: { sentAt: 'desc' }, take: 5, select: { title: true, sentAt: true, recipientCount: true } }),
    ]);

    type Activity = { type: string; content: string; time: Date };
    const activities: Activity[] = [];

    for (const u of recentUsers) {
      activities.push({ type: 'user', content: `새로운 사용자 "${u.nickname}" 님이 가입했습니다`, time: u.createdAt });
    }
    for (const c of recentCoupons) {
      const action = c.status === 'used' ? '사용' : '사용 요청';
      activities.push({ type: 'coupon', content: `${c.user.nickname} 님이 "${c.coupon.name}" 쿠폰을 ${action}했습니다`, time: c.createdAt });
    }
    for (const m of recentMissions) {
      activities.push({ type: 'mission', content: `${m.user.nickname} 님이 "${m.mission.name}" 미션을 완료했습니다`, time: m.completedAt });
    }
    for (const s of recentStores) {
      const action = s.status === 'pending' ? '등록 신청' : s.status === 'approved' ? '승인' : '반려';
      activities.push({ type: 'store', content: `상점 "${s.name}" ${action}`, time: s.createdAt });
    }
    for (const n of recentNotifications) {
      activities.push({ type: 'notification', content: `알림 "${n.title}" 전송 (${n.recipientCount}명)`, time: n.sentAt });
    }

    activities.sort((a, b) => b.time.getTime() - a.time.getTime());

    return successResponse({
      activePrograms: programCount,
      totalUsers: userCount,
      totalStamps: stampCount,
      usedCoupons: couponCount,
      recentActivities: activities.slice(0, 10).map((a) => ({
        type: a.type,
        content: a.content,
        time: a.time.toISOString(),
      })),
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
