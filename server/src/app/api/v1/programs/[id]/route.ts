import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';


export async function OPTIONS() {
  return new Response(null, {
    status: 204,
    headers: {
      'Access-Control-Allow-Origin': 'http://192.168.0.12:8080',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    },
  });
}

export async function GET(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const programId = Number(id);

    if (isNaN(programId)) {
      return errorResponse('INVALID_INPUT', '잘못된 프로그램 ID입니다', 400);
    }

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

    const program = await prisma.program.findUnique({
      where: { id: programId },
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
    });

    if (!program) {
      return errorResponse('NOT_FOUND', '프로그램을 찾을 수 없습니다', 404);
    }

    // isParticipated 필드 추가
    const result = {
      ...program,
      events: program.events.map((event) => {
        const { participants, _count, ...rest } = event as typeof event & { participants?: { id: number }[] };
        return {
          ...rest,
          _count,
          isParticipated: userId ? (participants?.length ?? 0) > 0 : undefined,
        };
      }),
    };

    return successResponse(result);
  } catch (error) {
    console.error('Program detail API error:', error);
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
