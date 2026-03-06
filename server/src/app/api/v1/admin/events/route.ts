import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const events = await prisma.event.findMany({
      include: {
        program: { select: { name: true } },
        _count: { select: { participants: true } },
      },
      orderBy: { createdAt: 'desc' },
    });
    return successResponse(events);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { programId, name, description, imageUrl, type, startDate, endDate, reward, participantLimit } = body;

    if (!programId || !name || !type || !startDate || !endDate) {
      return errorResponse('INVALID_INPUT', '필수 항목을 모두 입력하세요');
    }

    const event = await prisma.event.create({
      data: {
        programId,
        name,
        description: description || null,
        imageUrl: imageUrl || null,
        type,
        startDate: new Date(startDate),
        endDate: new Date(endDate),
        reward,
        participantLimit: participantLimit || 0,
      },
    });

    return successResponse(event, '이벤트가 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
