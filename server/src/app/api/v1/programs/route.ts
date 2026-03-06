import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const category = searchParams.get('category');

    // 선택적 인증: 토큰 있으면 userId 추출, 없으면 null
    let userId: number | null = null;
    const token = request.headers.get('authorization')?.replace('Bearer ', '');
    if (token) {
      try {
        const payload = await verifyToken(token);
        userId = payload.userId as number;
      } catch {
        // 토큰 실패해도 무시 (비로그인 취급)
      }
    }

    const where: Record<string, unknown> = {
      status: { in: ['scheduled', 'in_progress'] },
    };

    if (category && category !== 'all') {
      where.category = category;
    }

    const programs = await prisma.program.findMany({
      where,
      include: {
        events: {
          include: {
            _count: { select: { participants: true } },
            ...(userId ? {
              participants: {
                where: { userId },
                select: { id: true },
              },
            } : {}),
          },
        },
      },
      orderBy: { startDate: 'asc' },
    });

    // Decimal → number 변환 + isParticipated 필드 추가
    const result = programs.map((program) => ({
      ...program,
      latitude: program.latitude ? Number(program.latitude) : null,
      longitude: program.longitude ? Number(program.longitude) : null,
      events: program.events.map((event) => {
        const { participants, _count, ...rest } = event as typeof event & { participants?: { id: number }[] };
        return {
          ...rest,
          _count,
          isParticipated: userId ? (participants?.length ?? 0) > 0 : undefined,
        };
      }),
    }));

    return successResponse(result);
  } catch (error) {
    console.error('Programs API error:', error);
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
