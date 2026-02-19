import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

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

    return successResponse(notification, '알림이 전송되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
