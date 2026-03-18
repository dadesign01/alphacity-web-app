import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import { sendPushNotification } from '@/lib/firebase';

// 예약된 알림 중 시간이 지난 것들을 실제 발송 처리
async function processScheduledNotifications() {
  const now = new Date();
  const pendingNotifications = await prisma.notification.findMany({
    where: {
      status: 'scheduled',
      scheduledAt: { lte: now },
    },
  });

  for (const notification of pendingNotifications) {
    try {
      const users = await prisma.user.findMany({
        where: notification.target === 'active'
          ? { isActive: true, fcmToken: { not: null } }
          : { fcmToken: { not: null } },
        select: { fcmToken: true },
      });

      const tokens = users
        .map(u => u.fcmToken)
        .filter((t): t is string => t !== null);

      for (let i = 0; i < tokens.length; i += 500) {
        const batch = tokens.slice(i, i + 500);
        await sendPushNotification(batch, notification.title, notification.message);
      }

      await prisma.notification.update({
        where: { id: notification.id },
        data: { status: 'sent', sentAt: now },
      });
    } catch (error) {
      console.error(`Failed to send scheduled notification ${notification.id}:`, error);
      await prisma.notification.update({
        where: { id: notification.id },
        data: { status: 'failed' },
      });
    }
  }
}

export async function GET() {
  try {
    // 예약된 알림 중 시간이 지난 것들 자동 발송
    await processScheduledNotifications();

    const notifications = await prisma.notification.findMany({
      orderBy: { sentAt: 'desc' },
    });
    return successResponse(notifications);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { title, message, target, scheduledAt } = body;

    if (!title || !message) {
      return errorResponse('INVALID_INPUT', '제목과 내용은 필수입니다');
    }

    const recipientCount = await prisma.user.count({
      where: target === 'active' ? { isActive: true } : {},
    });

    // 예약 전송: scheduledAt이 미래 시간인 경우
    if (scheduledAt) {
      const scheduledDate = new Date(scheduledAt);
      if (scheduledDate > new Date()) {
        const notification = await prisma.notification.create({
          data: {
            title,
            message,
            target: target || 'all',
            status: 'scheduled',
            scheduledAt: scheduledDate,
            sentAt: scheduledDate,
            recipientCount,
          },
        });

        return successResponse(
          notification,
          `알림이 ${scheduledDate.toLocaleString('ko-KR')}에 예약되었습니다`,
        );
      }
    }

    // 즉시 전송
    const users = await prisma.user.findMany({
      where: target === 'active'
        ? { isActive: true, fcmToken: { not: null } }
        : { fcmToken: { not: null } },
      select: { fcmToken: true },
    });

    const tokens = users
      .map(u => u.fcmToken)
      .filter((t): t is string => t !== null);

    let totalSuccess = 0;
    let totalFailure = 0;

    for (let i = 0; i < tokens.length; i += 500) {
      const batch = tokens.slice(i, i + 500);
      const result = await sendPushNotification(batch, title, message);
      totalSuccess += result.successCount;
      totalFailure += result.failureCount;
    }

    const notification = await prisma.notification.create({
      data: {
        title,
        message,
        target: target || 'all',
        status: 'sent',
        recipientCount,
      },
    });

    return successResponse(
      { ...notification, fcmSuccess: totalSuccess, fcmFailure: totalFailure },
      `알림이 전송되었습니다 (푸시 성공: ${totalSuccess}, 실패: ${totalFailure})`,
    );
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
