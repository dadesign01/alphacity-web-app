import { NextRequest } from 'next/server';
import { createRemoteJWKSet, jwtVerify } from 'jose';
import { prisma } from '@/lib/prisma';
import { signToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';

interface SocialProfile {
  socialId: string;
  email: string;
  nickname: string;
  profileImage?: string;
}

const APPLE_JWKS = createRemoteJWKSet(new URL('https://appleid.apple.com/auth/keys'));
const APPLE_BUNDLE_ID = 'com.brint.AlphaCityStampTour';

// Apple은 identityToken(JWT)을 전달받아 Apple 공개키로 서명 검증
async function getAppleProfile(identityToken: string, nickname?: string): Promise<SocialProfile> {
  let payload;
  try {
    ({ payload } = await jwtVerify(identityToken, APPLE_JWKS, {
      issuer: 'https://appleid.apple.com',
      audience: APPLE_BUNDLE_ID,
    }));
  } catch (e) {
    console.error('[auth/social] apple token verify failed', e);
    throw new Error('Apple 로그인 검증에 실패했습니다');
  }

  const socialId = payload.sub;
  if (!socialId) throw new Error('Apple 로그인 검증에 실패했습니다');

  return {
    socialId,
    email: typeof payload.email === 'string' ? payload.email : `apple_${socialId}@apple.com`,
    nickname: nickname || `애플유저${socialId.slice(-6)}`,
  };
}

async function getKakaoProfile(accessToken: string): Promise<SocialProfile> {
  const res = await fetch('https://kapi.kakao.com/v2/user/me', {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!res.ok) {
    const errorBody = await res.text().catch(() => '<no body>');
    console.error(`[auth/social] kakao profile failed status=${res.status} body=${errorBody}`);
    throw new Error(`카카오 프로필 조회 실패 (${res.status})`);
  }

  const data = await res.json();
  const account = data.kakao_account;

  return {
    socialId: String(data.id),
    email: account?.email || `kakao_${data.id}@kakao.com`,
    nickname: account?.profile?.nickname || `카카오유저${data.id}`,
    profileImage: account?.profile?.profile_image_url,
  };
}

async function getNaverProfile(accessToken: string): Promise<SocialProfile> {
  const res = await fetch('https://openapi.naver.com/v1/nid/me', {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!res.ok) throw new Error('네이버 프로필 조회 실패');

  const data = await res.json();
  const profile = data.response;

  return {
    socialId: profile.id,
    email: profile.email || `naver_${profile.id}@naver.com`,
    nickname: profile.nickname || profile.name || `네이버유저${profile.id}`,
    profileImage: profile.profile_image,
  };
}

export async function POST(request: NextRequest) {
  try {
    const { provider, accessToken, nickname } = await request.json();

    if (!provider || !accessToken) {
      return errorResponse('INVALID_INPUT', 'provider와 accessToken을 입력하세요');
    }

    if (provider !== 'kakao' && provider !== 'naver' && provider !== 'apple') {
      return errorResponse('INVALID_INPUT', '지원하지 않는 소셜 로그인입니다');
    }

    // 소셜 프로필 조회
    const profile = provider === 'kakao'
      ? await getKakaoProfile(accessToken)
      : provider === 'naver'
        ? await getNaverProfile(accessToken)
        : await getAppleProfile(accessToken, typeof nickname === 'string' ? nickname : undefined);

    // 기존 유저 검색 (provider + socialId)
    let user = await prisma.user.findUnique({
      where: { provider_socialId: { provider, socialId: profile.socialId } },
    });

    if (!user) {
      // 같은 이메일로 가입된 이메일 유저가 있는지 확인
      const existingEmailUser = await prisma.user.findUnique({
        where: { email: profile.email },
      });

      if (existingEmailUser) {
        // 기존 이메일 유저에 소셜 정보 연결
        user = await prisma.user.update({
          where: { id: existingEmailUser.id },
          data: { provider, socialId: profile.socialId },
        });
      } else {
        // 신규 회원가입
        user = await prisma.user.create({
          data: {
            email: profile.email,
            nickname: profile.nickname,
            profileImage: profile.profileImage,
            provider,
            socialId: profile.socialId,
          },
        });
      }
    }

    const token = await signToken({ userId: user.id, email: user.email });
    const refreshToken = await signToken({ userId: user.id, type: 'refresh' }, '30d');

    return successResponse({
      token,
      refreshToken,
      user: {
        id: user.id,
        email: user.email,
        nickname: user.nickname,
        profileImage: user.profileImage,
      },
    });
  } catch (error) {
    console.error('[auth/social] failed', error);
    const message = error instanceof Error ? error.message : '서버 오류가 발생했습니다';
    return errorResponse('SOCIAL_AUTH_ERROR', message, 500);
  }
}
