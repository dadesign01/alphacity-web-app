import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
  // Admin 계정
  const hashedPassword = await bcrypt.hash('admin1234', 10);
  const admin = await prisma.admin.upsert({
    where: { email: 'admin@festival.kr' },
    update: {},
    create: {
      email: 'admin@festival.kr',
      password: hashedPassword,
      name: '관리자',
      phone: '010-0000-0000',
    },
  });
  console.log('Admin created:', admin.email);

  // 프로그램
  const program = await prisma.program.create({
    data: {
      name: '2025 봄꽃 프로그램',
      description: '봄꽃과 함께하는 즐거운 프로그램입니다.',
      startDate: new Date('2025-04-01'),
      endDate: new Date('2025-04-07'),
      status: 'in_progress',
    },
  });
  console.log('Program created:', program.name);

  // 이벤트
  await prisma.event.createMany({
    data: [
      { programId: program.id, name: '스탬프 10개 달성 추첨 이벤트', type: 'raffle', startDate: new Date('2025-04-01'), endDate: new Date('2025-04-07'), reward: '애플워치', participantLimit: 100 },
      { programId: program.id, name: '선착순 100명 경품 이벤트', type: 'first_come', startDate: new Date('2025-04-01'), endDate: new Date('2025-04-07'), reward: '텀블러', participantLimit: 100 },
      { programId: program.id, name: '포토존 인증 추첨 이벤트', type: 'raffle', startDate: new Date('2025-04-05'), endDate: new Date('2025-04-10'), reward: '기프티콘 1만원', participantLimit: 200 },
    ],
  });
  console.log('Events created');

  // 장소
  const places = await Promise.all([
    prisma.place.create({ data: { name: '메인 게이트', category: 'entrance', latitude: 37.5665, longitude: 126.978, address: '서울특별시 중구 세종대로 110' } }),
    prisma.place.create({ data: { name: '푸드 코트', category: 'food', latitude: 37.5665, longitude: 126.979 } }),
    prisma.place.create({ data: { name: '공연장', category: 'facility', latitude: 37.5666, longitude: 126.978 } }),
    prisma.place.create({ data: { name: '포토존 1', category: 'photo_zone', latitude: 37.5664, longitude: 126.978 } }),
  ]);
  console.log('Places created');

  // 미션
  await prisma.mission.createMany({
    data: [
      { name: '포토존 인증', type: 'location_auth', placeId: places[3].id },
      { name: '프로그램 퀴즈', type: 'quiz', question: '프로그램 시작 연도는?', answer: '2025' },
      { name: '푸드코트 체류', type: 'stay_time', placeId: places[1].id, stayMinutes: 5 },
    ],
  });
  console.log('Missions created');

  // 스탬프
  await prisma.stamp.createMany({
    data: [
      { name: '첫 방문 스탬프', conditionType: 'place_visit', conditionDetail: '프로그램 첫 방문' },
      { name: '미션 완료 스탬프', conditionType: 'mission_complete', conditionDetail: '미션 3개 완료' },
      { name: '이벤트 참여 스탬프', conditionType: 'event_participate', conditionDetail: '이벤트 1회 참여' },
    ],
  });
  console.log('Stamps created');

  // 쿠폰
  await prisma.coupon.createMany({
    data: [
      { name: '커피 무료 쿠폰', description: '제휴 카페에서 사용 가능한 커피 무료 쿠폰', requiredStamps: 3, validUntil: new Date('2025-04-30') },
      { name: '기념품 교환권', description: '프로그램 기념품을 교환할 수 있는 쿠폰', requiredStamps: 5, validUntil: new Date('2025-05-31') },
      { name: '10% 할인 쿠폰', description: '프로그램 내 상점 10% 할인', requiredStamps: 2, validUntil: new Date('2025-04-15') },
    ],
  });
  console.log('Coupons created');

  // 상점
  await prisma.store.createMany({
    data: [
      { name: '카페 ABC', category: 'food', ownerName: '김사장', phone: '010-1234-5678', requestDate: new Date('2025-01-25'), status: 'pending' },
      { name: '기념품샵', category: 'souvenir', ownerName: '이사장', phone: '010-2345-6789', requestDate: new Date('2025-01-26'), status: 'pending' },
      { name: '푸드코트', category: 'food', ownerName: '박사장', phone: '010-3456-7890', requestDate: new Date('2025-01-20'), status: 'approved' },
      { name: '액세서리 가게', category: 'other', ownerName: '최사장', phone: '010-4567-8901', requestDate: new Date('2025-01-18'), status: 'rejected' },
    ],
  });
  console.log('Stores created');

  // 알림
  await prisma.notification.createMany({
    data: [
      { title: '새로운 이벤트 시작!', message: '스탬프 랠리 이벤트가 시작되었습니다.', target: 'all', recipientCount: 1247 },
      { title: '프로그램 일정 안내', message: '오늘 공연 일정을 확인하세요.', target: 'all', recipientCount: 1150 },
      { title: '쿠폰 사용 마감 임박', message: '3일 후 쿠폰 유효기간이 종료됩니다.', target: 'active', recipientCount: 892 },
    ],
  });
  console.log('Notifications created');

  console.log('\nSeed completed successfully!');
  console.log('Login: admin@festival.kr / admin1234');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
