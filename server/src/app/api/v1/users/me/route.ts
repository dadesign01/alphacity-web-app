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
      birthDate: user.birthDate ? user.birthDate.toISOString().split('T')[0] : null,
      gender: user.gender,
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

    // 비밀번호 변경 처리 (현재 비밀번호 확인 필수)
    let passwordUpdate = {};
    if (body.newPassword) {
      if (!body.currentPassword) {
        return errorResponse('INVALID_INPUT', '현재 비밀번호를 입력해주세요');
      }
      if (!existingUser?.passwordHash) {
        return errorResponse('INVALID_INPUT', '소셜 로그인 계정은 비밀번호를 변경할 수 없습니다');
      }
      const isMatch = await bcrypt.compare(body.currentPassword, existingUser.passwordHash);
      if (!isMatch) {
        return errorResponse('WRONG_PASSWORD', '현재 비밀번호가 일치하지 않습니다');
      }
      passwordUpdate = { passwordHash: await bcrypt.hash(body.newPassword, 10) };
    }

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
        ...(body.birthDate !== undefined && { birthDate: body.birthDate ? new Date(body.birthDate) : null }),
        ...(body.gender !== undefined && { gender: body.gender }),
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
      birthDate: user.birthDate ? user.birthDate.toISOString().split('T')[0] : null,
      gender: user.gender,
      profileImage: user.profileImage,
      provider: user.provider,
    }, '프로필이 수정되었습니다');
  } catch (e: unknown) {
    if (e && typeof e === 'object' && 'code' in e && (e as { code: string }).code === 'P2002') {
      return errorResponse('DUPLICATE_PHONE', '이미 사용 중인 전화번호입니다', 409);
    }
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
