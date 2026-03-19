import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';
import bcrypt from 'bcryptjs';

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
      name: user.name,
      phone: user.phone,
      address: user.address,
      addressDetail: user.addressDetail,
      profileImage: user.profileImage,
      provider: user.provider,
      stampCount: user._count.userStamps,
      couponCount: user._count.userCoupons,
      missionCount: user._count.missionCompletions,
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(request: NextRequest) {
  try {
    const userId = await getUserId(request);
    if (!userId) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

    // 관련 데이터 삭제 후 사용자 삭제 (Prisma cascading 이 설정되어 있지만 명시적으로 처리)
    await prisma.$transaction([
      prisma.eventParticipant.deleteMany({ where: { userId } }),
      prisma.missionCompletion.deleteMany({ where: { userId } }),
      prisma.userStamp.deleteMany({ where: { userId } }),
      prisma.userCoupon.deleteMany({ where: { userId } }),
      prisma.user.delete({ where: { id: userId } }),
    ]);

    return successResponse(null, '회원 탈퇴가 완료되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest) {
  try {
    const userId = await getUserId(request);
    if (!userId) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

    const body = await request.json();

    // 이름은 한번 저장하면 수정 불가
    const existingUser = await prisma.user.findUnique({ where: { id: userId } });
    const nameUpdate = body.name && !existingUser?.name ? { name: body.name } : {};

    // 비밀번호 변경 처리
    const passwordUpdate = body.password
      ? { passwordHash: await bcrypt.hash(body.password, 10) }
      : {};

    const user = await prisma.user.update({
      where: { id: userId },
      data: {
        ...(body.nickname && { nickname: body.nickname }),
        ...nameUpdate,
        ...passwordUpdate,
        ...(body.phone !== undefined && { phone: body.phone }),
        ...(body.address !== undefined && { address: body.address }),
        ...(body.addressDetail !== undefined && { addressDetail: body.addressDetail }),
        ...(body.profileImage !== undefined && { profileImage: body.profileImage }),
      },
    });

    return successResponse({
      id: user.id,
      email: user.email,
      nickname: user.nickname,
      name: user.name,
      phone: user.phone,
      address: user.address,
      addressDetail: user.addressDetail,
      profileImage: user.profileImage,
      provider: user.provider,
    }, '프로필이 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
