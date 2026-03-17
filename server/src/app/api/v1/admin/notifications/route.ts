import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import { sendPushNotification } from '@/lib/firebase';

export async function GET() {
  try {
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
    const { title, message, target } = body;

    if (!title || !message) {
      return errorResponse('INVALID_INPUT', '제목과 내용은 필수입니다');
    }

    // FCM 토큰이 있는 대상 사용자 조회
    const users = await prisma.user.findMany({
      where: target === 'active'
        ? { isActive: true, fcmToken: { not: null } }
        : { fcmToken: { not: null } },
      select: { fcmToken: true },
    });

    const tokens = users
      .map(u => u.fcmToken)
      .filter((t): t is string => t !== null);

    // FCM 푸시 발송 (500개씩 배치)
    let totalSuccess = 0;
    let totalFailure = 0;

    for (let i = 0; i < tokens.length; i += 500) {
      const batch = tokens.slice(i, i + 500);
      const result = await sendPushNotification(batch, title, message);
      totalSuccess += result.successCount;
      totalFailure += result.failureCount;
    }

    // DB에 알림 기록 저장
    const recipientCount = await prisma.user.count({
      where: target === 'active' ? { isActive: true } : {},
    });

    const notification = await prisma.notification.create({
      data: {
        title,
        message,
        target: target || 'all',
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
