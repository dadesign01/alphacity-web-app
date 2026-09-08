import { NextRequest } from 'next/server';
import { createRemoteJWKSet, jwtVerify } from 'jose';
import { prisma } from '@/lib/prisma';
import { signToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';
import { grantFirstComeCoupon } from '@/lib/first-come-coupon';

interface SocialProfile {
  socialId: string;
  email: string;
  nickname: string;
  profileImage?: string;
}

const APPLE_JWKS = createRemoteJWKSet(
  new URL('https://appleid.apple.com/auth/keys')
);

const APPLE_BUNDLE_ID =
  'com.brint.AlphaCityStampTour';


// =========================================================
// Apple
// =========================================================

async function getAppleProfile(
  identityToken: string,
  nickname?: string
): Promise<SocialProfile> {
  let payload;

  try {
    ({ payload } = await jwtVerify(
      identityToken,
      APPLE_JWKS,
      {
        issuer: 'https://appleid.apple.com',
        audience: APPLE_BUNDLE_ID,
      }
    ));
  } catch (e) {
    console.error(
      '[auth/social] apple token verify failed',
      e
    );

    throw new Error(
      'Apple 로그인 검증에 실패했습니다'
    );
  }

  const socialId = payload.sub;

  if (!socialId) {
    throw new Error(
      'Apple 로그인 검증에 실패했습니다'
    );
  }

  return {
    socialId,
    email:
      typeof payload.email === 'string'
        ? payload.email
        : `apple_${socialId}@apple.com`,
    nickname:
      nickname ||
      `애플유저${socialId.slice(-6)}`,
  };
}


// =========================================================
// Kakao
// =========================================================

async function getKakaoAccessToken(
  code: string
): Promise<string> {
  const clientId =
    process.env.KAKAO_REST_API_KEY;

  const redirectUri =
    process.env.KAKAO_REDIRECT_URI;

  if (!clientId) {
    throw new Error(
      'Kakao REST API Key가 서버에 설정되지 않았습니다'
    );
  }

  if (!redirectUri) {
    throw new Error(
      'Kakao Redirect URI가 서버에 설정되지 않았습니다'
    );
  }

  const res = await fetch(
    'https://kauth.kakao.com/oauth/token',
    {
      method: 'POST',

      headers: {
        'Content-Type':
          'application/x-www-form-urlencoded;charset=utf-8',
      },

      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: clientId,
        redirect_uri: redirectUri,
        code,
      }),
    }
  );

  if (!res.ok) {
    const errorBody =
      await res.text().catch(
        () => '<no body>'
      );

    console.error(
      `[auth/social] kakao token failed status=${res.status} body=${errorBody}`
    );

    throw new Error(
      '카카오 로그인 토큰 발급에 실패했습니다'
    );
  }

  const data = await res.json();

  if (!data.access_token) {
    throw new Error(
      '카카오 access token을 받지 못했습니다'
    );
  }

  return data.access_token;
}


async function getKakaoProfile(
  accessToken: string
): Promise<SocialProfile> {
  const res = await fetch(
    'https://kapi.kakao.com/v2/user/me',
    {
      headers: {
        Authorization:
          `Bearer ${accessToken}`,
      },
    }
  );

  if (!res.ok) {
    const errorBody =
      await res.text().catch(
        () => '<no body>'
      );

    console.error(
      `[auth/social] kakao profile failed status=${res.status} body=${errorBody}`
    );

    throw new Error(
      `카카오 프로필 조회 실패 (${res.status})`
    );
  }

  const data = await res.json();

  const account =
    data.kakao_account;

  return {
    socialId: String(data.id),

    email:
      account?.email ||
      `kakao_${data.id}@kakao.com`,

    nickname:
      account?.profile?.nickname ||
      `카카오유저${data.id}`,

    profileImage:
      account?.profile?.profile_image_url,
  };
}


// =========================================================
// Naver
// =========================================================

async function getNaverProfile(
  accessToken: string
): Promise<SocialProfile> {
  const res = await fetch(
    'https://openapi.naver.com/v1/nid/me',
    {
      headers: {
        Authorization:
          `Bearer ${accessToken}`,
      },
    }
  );

  if (!res.ok) {
    throw new Error(
      '네이버 프로필 조회 실패'
    );
  }

  const data = await res.json();

  const profile =
    data.response;

  return {
    socialId: profile.id,

    email:
      profile.email ||
      `naver_${profile.id}@naver.com`,

    nickname:
      profile.nickname ||
      profile.name ||
      `네이버유저${String(
        profile.id
      ).slice(-6)}`,

    profileImage:
      profile.profile_image,
  };
}


// =========================================================
// POST /auth/social
// =========================================================

export async function POST(
  request: NextRequest
) {
  try {
    const {
      provider,
      accessToken,
      code,
      nickname,
    } = await request.json();


    // =======================================================
    // 기본 검증
    // =======================================================

    if (!provider) {
      return errorResponse(
        'INVALID_INPUT',
        'provider를 입력하세요'
      );
    }

    if (
      provider !== 'kakao' &&
      provider !== 'naver' &&
      provider !== 'apple'
    ) {
      return errorResponse(
        'INVALID_INPUT',
        '지원하지 않는 소셜 로그인입니다'
      );
    }


    // =======================================================
    // 소셜 프로필 조회
    // =======================================================

    let profile: SocialProfile;


    // -------------------------------------------------------
    // Kakao
    // -------------------------------------------------------

    if (provider === 'kakao') {
      let kakaoAccessToken =
        accessToken;

      if (!kakaoAccessToken && code) {
        kakaoAccessToken =
          await getKakaoAccessToken(
            code
          );
      }

      if (!kakaoAccessToken) {
        return errorResponse(
          'INVALID_INPUT',
          '카카오 accessToken 또는 code가 필요합니다'
        );
      }

      profile =
        await getKakaoProfile(
          kakaoAccessToken
        );
    }


    // -------------------------------------------------------
    // Naver
    // -------------------------------------------------------

    else if (provider === 'naver') {
      if (!accessToken) {
        return errorResponse(
          'INVALID_INPUT',
          'accessToken을 입력하세요'
        );
      }

      profile =
        await getNaverProfile(
          accessToken
        );
    }


    // -------------------------------------------------------
    // Apple
    // -------------------------------------------------------

    else {
      if (!accessToken) {
        return errorResponse(
          'INVALID_INPUT',
          'accessToken을 입력하세요'
        );
      }

      profile =
        await getAppleProfile(
          accessToken,
          typeof nickname === 'string'
            ? nickname
            : undefined
        );
    }


    // =======================================================
    // 기존 유저 검색
    // =======================================================

    let user =
      await prisma.user.findUnique({
        where: {
          provider_socialId: {
            provider,
            socialId:
              profile.socialId,
          },
        },
      });

    // 선착순 쿠폰 지급 여부
    let firstComeCoupon = false;


    // =======================================================
    // 신규 / 기존 회원 처리
    // =======================================================

    if (!user) {
      const existingEmailUser =
        await prisma.user.findUnique({
          where: {
            email: profile.email,
          },
        });

      // -----------------------------------------------------
      // 기존 이메일 회원 + 소셜 계정 연결
      // -----------------------------------------------------

      if (existingEmailUser) {
        /*
         * 이미 가입된 회원입니다.
         *
         * 신규 회원가입이 아니므로
         * 선착순 쿠폰 카운팅을 하지 않습니다.
         */

        user =
          await prisma.user.update({
            where: {
              id: existingEmailUser.id,
            },

            data: {
              provider,
              socialId:
                profile.socialId,
            },
          });
      }

      // -----------------------------------------------------
      // 완전 신규 소셜 회원
      // -----------------------------------------------------

      else {
        /*
         * 신규 회원가입입니다.
         *
         * User 생성
         * ↓
         * 선착순 카운터 잠금
         * ↓
         * 100명 이내라면 쿠폰 발급
         * ↓
         * 카운터 +1
         *
         * 전부 하나의 transaction으로 처리합니다.
         */

        const result =
          await prisma.$transaction(
            async (tx) => {
              const newUser =
                await tx.user.create({
                  data: {
                    email:
                      profile.email,

                    nickname:
                      profile.nickname,

                    profileImage:
                      profile.profileImage,

                    provider,

                    socialId:
                      profile.socialId,
                  },
                });

              const couponGranted =
                await grantFirstComeCoupon(
                  tx,
                  newUser.id
                );

              return {
                user: newUser,
                firstComeCoupon:
                  couponGranted,
              };
            }
          );

        user =
          result.user;

        firstComeCoupon =
          result.firstComeCoupon;
      }
    }


    // =======================================================
    // 비활성 회원 체크
    // =======================================================

    if (!user.isActive) {
      return errorResponse(
        'USER_INACTIVE',
        '사용할 수 없는 계정입니다',
        403
      );
    }


    // =======================================================
    // JWT 발급
    // =======================================================

    const token =
      await signToken({
        userId: user.id,
        email: user.email,
      });

    const refreshToken =
      await signToken(
        {
          userId: user.id,
          type: 'refresh',
        },
        '30d'
      );


    // =======================================================
    // 응답
    // =======================================================

    return successResponse({
      token,

      refreshToken,

      user: {
        id: user.id,
        email: user.email,
        nickname: user.nickname,
        profileImage:
          user.profileImage,
      },

      firstComeCoupon,
    });

  } catch (error) {
    console.error(
      '[auth/social] failed',
      error
    );

    const message =
      error instanceof Error
        ? error.message
        : '서버 오류가 발생했습니다';

    return errorResponse(
      'SOCIAL_AUTH_ERROR',
      message,
      500
    );
  }
}