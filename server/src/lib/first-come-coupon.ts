import { Prisma } from '@prisma/client';
import { randomUUID } from 'crypto';

const FIRST_COME_COUPON_ID = 9;

type TransactionClient = Prisma.TransactionClient;

export async function grantFirstComeCoupon(
  tx: TransactionClient,
  userId: number,
): Promise<boolean> {
  // 선착순 카운터 행을 잠금
  const events = await tx.$queryRaw<
    Array<{
      id: number;
      coupon_id: number;
      max_count: number;
      current_count: number;
    }>
  >`
    SELECT
      id,
      coupon_id,
      max_count,
      current_count
    FROM first_come_coupon_events
    WHERE coupon_id = ${FIRST_COME_COUPON_ID}
    FOR UPDATE
  `;

  if (events.length === 0) {
    return false;
  }

  const event = events[0];

  // 이미 100명 모두 지급했으면 종료
  if (event.current_count >= event.max_count) {
    return false;
  }

  // 혹시라도 해당 유저에게 이미 지급된 경우 방어
  const user = await tx.user.findUnique({
    where: {
      id: userId,
    },
    select: {
      firstComeCoupon: true,
    },
  });

  if (!user || user.firstComeCoupon) {
    return false;
  }

  const code =
    `FIRST-${randomUUID().replace(/-/g, '').slice(0, 20).toUpperCase()}`;

  // 쿠폰 발급
  await tx.userCoupon.create({
    data: {
      userId,
      couponId: FIRST_COME_COUPON_ID,
      code,
      status: 'issued',
    },
  });

  // 해당 유저에게 지급 완료 표시
  await tx.user.update({
    where: {
      id: userId,
    },
    data: {
      firstComeCoupon: true,
      firstComePopupShown: false,
    },
  });

  // 선착순 카운터 증가
  await tx.firstComeCouponEvent.update({
    where: {
      id: event.id,
    },
    data: {
      currentCount: {
        increment: 1,
      },
    },
  });

  return true;
}