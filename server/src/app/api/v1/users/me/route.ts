import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

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

export async function GET(request: NextRequest) {
  try {
    const userId = await getUserId(request);
    if (!userId) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

    const user = await prisma.user.findUnique({
      where: { id: userId },
      include: {
        _count: { select: { userStamps: true, userCoupons: true, missionCompletions: true } },
      },
    });

    if (!user) return errorResponse('NOT_FOUND', '사용자를 찾을 수 없습니다', 404);

    return successResponse({
      id: user.id,
      email: user.email,
      nickname: user.nickname,
      profileImage: user.profileImage,
      stampCount: user._count.userStamps,
      couponCount: user._count.userCoupons,
      missionCount: user._count.missionCompletions,
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest) {
  try {
    const userId = await getUserId(request);
    if (!userId) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

    const body = await request.json();
    const user = await prisma.user.update({
      where: { id: userId },
      data: {
        ...(body.nickname && { nickname: body.nickname }),
        ...(body.profileImage !== undefined && { profileImage: body.profileImage }),
      },
    });

    return successResponse({
      id: user.id, email: user.email, nickname: user.nickname, profileImage: user.profileImage,
    }, '프로필이 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
