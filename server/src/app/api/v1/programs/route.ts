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


export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const category = searchParams.get('category');
    const festivalIdRaw = searchParams.get('festivalId');

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

    const where: Record<string, unknown> = {};

    if (category && category !== 'all') {
      where.category = category;
    }
    if (festivalIdRaw && festivalIdRaw !== 'all') {
      const festivalId = Number(festivalIdRaw);
      if (Number.isInteger(festivalId)) where.festivalId = festivalId;
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
        stores: {
          where: { status: 'approved' },
          include: {
            storeCoupons: {
              include: { coupon: { select: { id: true, name: true, description: true } } },
            },
          },
        },
      },
      orderBy: { startDate: 'asc' },
    });

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Decimal → number 변환 + 날짜 기반 상태 계산 + 종료된 프로그램 제외
    const result = programs
      .map((program) => {
        const start = new Date(program.startDate);
        start.setHours(0, 0, 0, 0);
        const end = new Date(program.endDate);
        end.setHours(0, 0, 0, 0);

        let status: string;
        if (today < start) status = 'scheduled';
        else if (today > end) status = 'ended';
        else status = 'in_progress';

        // 상점별 쿠폰을 플랫하게 변환
        const storeCoupons = program.stores.flatMap(store =>
          store.storeCoupons.map(sc => ({
            storeId: store.id,
            storeName: store.name,
            couponId: sc.coupon.id,
            couponName: sc.coupon.name,
            couponDescription: sc.coupon.description,
          }))
        );

        return {
          ...program,
          status,
          latitude: program.latitude ? Number(program.latitude) : null,
          longitude: program.longitude ? Number(program.longitude) : null,
          storeCoupons,
          events: program.events.map((event) => {
            const { participants, _count, ...rest } = event as typeof event & { participants?: { id: number }[] };
            return {
              ...rest,
              _count,
              isParticipated: userId ? (participants?.length ?? 0) > 0 : undefined,
            };
          }),
        };
      })
      .filter(p => p.status !== 'ended');

    return successResponse(result);
  } catch (error) {
    console.error('Programs API error:', error);
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
