import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { signToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';
import { verifyCode } from '@/lib/sms';
import { grantFirstComeCoupon } from '@/lib/first-come-coupon';

export async function OPTIONS() {
  return new Response(null, {
    status: 204,
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    },
  });
}

export async function POST(request: NextRequest) {
  try {
    const { phone, name, code } = await request.json();

    if (!name) {
      return errorResponse(
        'INVALID_INPUT',
        '이름을 입력하세요',
      );
    }

    if (!phone) {
      return errorResponse(
        'INVALID_INPUT',
        '전화번호를 입력하세요',
      );
    }

    if (!code) {
      return errorResponse(
        'INVALID_INPUT',
        '인증번호를 입력하세요',
      );
    }

    const verified = verifyCode(phone, code);

    if (!verified) {
      return errorResponse(
        'INVALID_CODE',
        '인증번호가 올바르지 않거나 만료되었습니다',
        400,
      );
    }

    let user = await prisma.user.findUnique({
      where: {
        phone,
      },
    });

    let firstComeCoupon = false;

    // =========================
    // 신규 회원
    // =========================
    if (!user) {
      const email = `${phone}@phone.festival.kr`;

      const result = await prisma.$transaction(async (tx) => {
        const newUser = await tx.user.create({
          data: {
            email,
            nickname: name,
            phone,
            passwordHash: null,
          },
        });

        const couponGranted = await grantFirstComeCoupon(
          tx,
          newUser.id,
        );

        return {
          user: newUser,
          firstComeCoupon: couponGranted,
        };
      });

      user = result.user;
      firstComeCoupon = result.firstComeCoupon;
    }

    // =========================
    // 기존 회원
    // =========================
    if (!user.isActive) {
      return errorResponse(
        'USER_INACTIVE',
        '사용할 수 없는 계정입니다',
        403,
      );
    }

    const token = await signToken({
      userId: user.id,
      email: user.email,
    });

    const refreshToken = await signToken(
      {
        userId: user.id,
        type: 'refresh',
      },
      '30d',
    );

    return successResponse(
      {
        token,
        refreshToken,
        user: {
          id: user.id,
          email: user.email,
          nickname: user.nickname,
          phone: user.phone,
        },
        firstComeCoupon,
      },
      '휴대폰 인증이 완료되었습니다',
    );
  } catch (error) {
    console.error('================================');
    console.error('Phone login error');
    console.error(error);
    console.error('================================');

    return errorResponse(
      'SERVER_ERROR',
      error instanceof Error
        ? error.message
        : '휴대폰 인증 로그인 중 오류가 발생했습니다',
      500,
    );
  }
}