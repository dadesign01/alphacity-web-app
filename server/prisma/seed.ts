import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
  // 기존 데이터 정리 (외래키 순서에 따라 삭제)
  await prisma.notification.deleteMany();
  await prisma.missionCompletion.deleteMany();
  await prisma.mission.deleteMany();
  await prisma.userCoupon.deleteMany();
  await prisma.coupon.deleteMany();
  await prisma.userStamp.deleteMany();
  await prisma.stamp.deleteMany();
  await prisma.eventParticipant.deleteMany();
  await prisma.event.deleteMany();
  await prisma.program.deleteMany();
  await prisma.place.deleteMany();
  await prisma.store.deleteMany();
  await prisma.banner.deleteMany();
  console.log('Existing data cleared');

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

  // 프로그램 (전시)
  const program1 = await prisma.program.create({
    data: {
      name: '현대 AI 모빌리티 혁신 전시 2026',
      description: '2026, 새로워진 현대의 AI 기술로\n자율주행부터 스마트 이동 기술까지, 미래 모빌리티를 경험하세요.',
      category: 'exhibition',
      imageUrl: '/uploads/program_img_1.png',
      operatingHours: '10:00 - 18:00',
      location: '알파시티 2로 33 태왕알파시티 3층 AI 체험존',
      latitude: 35.8420,
      longitude: 128.6900,
      startDate: new Date('2026-01-15'),
      endDate: new Date('2026-01-31'),
      status: 'in_progress',
    },
  });
  const program2 = await prisma.program.create({
    data: {
      name: '삼성전자 차세대 인공지능 테크 쇼케이스',
      description: '생성형 AI부터 스마트 디바이스 AI까지, 미래 기술을 직접 만나다.\n삼성의 최신 AI 기술로 구현된 미래 라이프를 경험하세요.',
      category: 'exhibition',
      imageUrl: '/uploads/program_img_2.png',
      operatingHours: '10:00 - 18:00',
      location: '알파시티 2로 33 태왕알파시티 3층 AI 체험존',
      latitude: 35.8435,
      longitude: 128.6915,
      startDate: new Date('2026-01-15'),
      endDate: new Date('2026-02-28'),
      status: 'in_progress',
    },
  });
  const program3 = await prisma.program.create({
    data: {
      name: 'NAVER 넥스트 제너레이션 : 스마트시티 솔루션',
      description: 'NAVER에서 제시하는 생활·교통·환경을 연결하는\nIoT 기반 스마트시티 솔루션을 한자리에서 체험하세요.',
      category: 'exhibition',
      imageUrl: '/uploads/program_img_1.png',
      operatingHours: '24시간 운영',
      location: '알파시티 2로 33 태왕알파시티 3층 AI 체험존',
      latitude: 35.8410,
      longitude: 128.6885,
      startDate: new Date('2026-01-02'),
      endDate: new Date('2026-06-10'),
      status: 'in_progress',
    },
  });
  const program4 = await prisma.program.create({
    data: {
      name: '현대 AI 모빌리티 테크 리뷰 2025',
      description: '현대자동차가 2025년 한 해 동안 선보인 AI 기반 모빌리티 기술을\n한자리에서 돌아보며 체험을 통해 현대의 기술을 느껴보세요.',
      category: 'exhibition',
      imageUrl: '/uploads/program_img_2.png',
      operatingHours: '24시간 운영',
      location: '알파시티 2로 33 태왕알파시티 3층 AI 체험존',
      latitude: 35.8445,
      longitude: 128.6920,
      startDate: new Date('2026-01-02'),
      endDate: new Date('2026-01-30'),
      status: 'scheduled',
    },
  });
  console.log('Exhibition programs created');

  // 프로그램 (세미나)
  const seminar1 = await prisma.program.create({
    data: {
      name: '다가오는 AI 전환 시대와 대응 전략',
      description: 'AI 시대 가속화에 따른 산업 구조 변화와 대응 전략 인사이트',
      category: 'seminar',
      imageUrl: '/uploads/program_img_1.png',
      operatingHours: '10:00 - 12:00',
      speaker: '김태현 교수(서울대학교 경제학과)',
      location: '알파시티 2로 33 태왕알파시티 3층 AI 체험존',
      latitude: 35.8430,
      longitude: 128.6895,
      startDate: new Date('2026-03-01'),
      endDate: new Date('2026-03-01'),
      status: 'in_progress',
    },
  });
  const seminar2 = await prisma.program.create({
    data: {
      name: '메타버스 비즈니스 전략',
      description: '메타버스 플랫폼을 활용한 비즈니스 모델을 전략하여 기술력을 증진하라',
      category: 'seminar',
      imageUrl: '/uploads/program_img_2.png',
      operatingHours: '10:00 - 12:00',
      speaker: '이수진 대표(네이버Z)',
      location: '알파시티 2로 33 태왕알파시티 3층 AI 체험존',
      latitude: 35.8425,
      longitude: 128.6910,
      startDate: new Date('2026-03-05'),
      endDate: new Date('2026-03-05'),
      status: 'in_progress',
    },
  });
  const seminar3 = await prisma.program.create({
    data: {
      name: '박민수의 디지털 마케팅 토크 콘서트',
      description: '디지털 플랫폼이 지배하는 지금을 살아가는 당신에게 필요한 마케팅 전략',
      category: 'seminar',
      imageUrl: '/uploads/program_img_1.png',
      operatingHours: '10:00 - 12:00',
      speaker: '박민수 대표이사(카카오)',
      location: '알파시티 2로 33 태왕알파시티 3층 AI 체험존',
      latitude: 35.8418,
      longitude: 128.6930,
      startDate: new Date('2026-03-10'),
      endDate: new Date('2026-03-10'),
      status: 'in_progress',
    },
  });
  console.log('Seminar programs created');

  // 프로그램 (맛집)
  await prisma.program.create({
    data: {
      name: '알파시티 카페거리 맛집 투어',
      description: '알파시티 내 숨겨진 카페와 디저트 맛집을 스탬프와 함께 즐겨보세요.',
      category: 'food',
      imageUrl: '/uploads/program_img_2.png',
      operatingHours: '09:00 - 21:00',
      location: '알파시티 카페거리 일대',
      latitude: 35.8405,
      longitude: 128.6875,
      startDate: new Date('2026-02-01'),
      endDate: new Date('2026-04-30'),
      status: 'in_progress',
    },
  });
  await prisma.program.create({
    data: {
      name: '스마트 비스트로 미식 체험',
      description: 'AI가 추천하는 맞춤형 메뉴와 스마트 키친의 미래를 경험하세요.',
      category: 'food',
      imageUrl: '/uploads/program_img_1.png',
      operatingHours: '11:00 - 20:00',
      location: '알파시티 2로 33 태왕알파시티 1층 푸드홀',
      latitude: 35.8440,
      longitude: 128.6905,
      startDate: new Date('2026-01-15'),
      endDate: new Date('2026-06-30'),
      status: 'in_progress',
    },
  });
  await prisma.program.create({
    data: {
      name: '로컬 푸드 페스티벌',
      description: '지역 농산물과 수제 먹거리를 한자리에서 만나는 로컬 푸드 축제',
      category: 'food',
      imageUrl: '/uploads/program_img_2.png',
      operatingHours: '10:00 - 19:00',
      location: '알파시티 중앙광장',
      latitude: 35.8415,
      longitude: 128.6940,
      startDate: new Date('2026-04-01'),
      endDate: new Date('2026-04-15'),
      status: 'scheduled',
    },
  });
  console.log('Food programs created');

  // 프로그램 (이벤트)
  await prisma.program.create({
    data: {
      name: '알파시티 봄맞이 스탬프 랠리',
      description: '봄을 맞아 알파시티 곳곳을 돌며 스탬프를 모으고 특별 경품을 받아가세요!',
      category: 'event',
      imageUrl: '/uploads/program_img_1.png',
      operatingHours: '10:00 - 18:00',
      location: '알파시티 전역',
      latitude: 35.8422,
      longitude: 128.6888,
      startDate: new Date('2026-03-01'),
      endDate: new Date('2026-03-31'),
      status: 'in_progress',
    },
  });
  await prisma.program.create({
    data: {
      name: '수성알파시티 야간 라이트업 페스티벌',
      description: '알파시티의 밤을 밝히는 미디어아트와 LED 조명 퍼포먼스',
      category: 'event',
      imageUrl: '/uploads/program_img_2.png',
      operatingHours: '18:00 - 22:00',
      location: '알파시티 중앙광장',
      latitude: 35.8438,
      longitude: 128.6925,
      startDate: new Date('2026-03-15'),
      endDate: new Date('2026-04-15'),
      status: 'scheduled',
    },
  });
  console.log('Event programs created');

  // 이벤트
  await prisma.event.createMany({
    data: [
      {
        programId: program1.id,
        name: '갤럭시탭 증정 추첨 이벤트',
        description: '수성알파시티 스탬프 투어에 참여하신 분들을 대상으로\n최신 갤럭시 탭 S9를 추첨으로 드립니다.',
        imageUrl: '/uploads/event_img_1.png',
        type: 'raffle',
        startDate: new Date('2026-02-01'),
        endDate: new Date('2026-02-28'),
        reward: '갤럭시 탭 S9(5명)',
        participantLimit: 2000,
        status: 'in_progress',
      },
      {
        programId: program1.id,
        name: 'VR 헤드셋을 잡아라! - VR 증정 이벤트',
        description: '수성알파시티 스탬프 투어에 참여하신 분들을 대상으로\n최신 VR헤드셋 기기를 추첨으로 드립니다.',
        imageUrl: '/uploads/event_img_2.png',
        type: 'raffle',
        startDate: new Date('2026-02-01'),
        endDate: new Date('2026-02-28'),
        reward: 'VR 헤드셋(3명)',
        participantLimit: 1000,
        status: 'in_progress',
      },
      {
        programId: program2.id,
        name: '알파시티 상품권 추첨 이벤트',
        description: '알파시티 내 상점 및 카페, 행사 등 활발히 사용 가능한 알파시티 상품권을\n수성알파시티 스탬프 투어에 참여하신 분들 대상 추첨으로 드립니다.',
        imageUrl: '/uploads/event_img_3.png',
        type: 'raffle',
        startDate: new Date('2026-01-01'),
        endDate: new Date('2026-01-31'),
        reward: '알파시티 상품권 10만원권(10명)',
        participantLimit: 3000,
        status: 'ended',
      },
      {
        programId: program1.id,
        name: '수성알파시티 10주년 기념 스페셜 굿즈 이벤트',
        description: '수성알파시티 10주년을 기념해 방문객 여러분께 한정판 에코백을\n선착순으로 증정하는 특별 이벤트입니다.',
        imageUrl: '/uploads/event_img_1.png',
        type: 'first_come',
        startDate: new Date('2026-02-01'),
        endDate: new Date('2026-03-31'),
        reward: '수성알파시티 한정판 에코백(200명)',
        participantLimit: 200,
        status: 'in_progress',
      },
      {
        programId: program2.id,
        name: '뚜비 스티커 세트 선착순 증정 이벤트',
        description: '대구 수성구 마스코트인 귀여운 뚜비 스티커 세트를\n수성알파시티 방문객 여러분께 선착순으로 증정하는 이벤트입니다.',
        imageUrl: '/uploads/event_img_2.png',
        type: 'first_come',
        startDate: new Date('2026-02-01'),
        endDate: new Date('2026-03-31'),
        reward: '뚜비 스티커 세트(300명)',
        participantLimit: 300,
        status: 'in_progress',
      },
      {
        programId: program3.id,
        name: '수성알파시티 에코 텀블러 증정 이벤트',
        description: '수성알파시티에서 곧 다가올 환경의 날을 기념하여 제작된\n에코 텀블러를 방문객 여러분께 선착순으로 증정하는 이벤트입니다.',
        imageUrl: '/uploads/event_img_3.png',
        type: 'first_come',
        startDate: new Date('2026-01-01'),
        endDate: new Date('2026-01-31'),
        reward: 'Alpha ECO 텀블러 500ml(150명)',
        participantLimit: 150,
        status: 'ended',
      },
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
      {
        name: '알파시티 카페',
        category: 'cafe',
        ownerName: '김사장',
        phone: '053-123-4567',
        address: '대구광역시 수성구 알파시티 2로 33',
        addressDetail: '태왕알파시티 1층 101호',
        description: '알파시티 내 분위기 좋은 카페입니다.',
        storeCode: '#CAFE0001',
        operatingDays: '월,화,수,목,금,토',
        openTime: '09:00',
        closeTime: '21:00',
        requestDate: new Date('2026-02-25'),
        status: 'pending',
      },
      {
        name: '알파 레스토랑',
        category: 'restaurant',
        ownerName: '이사장',
        phone: '053-234-5678',
        address: '대구광역시 수성구 알파시티 2로 35',
        addressDetail: '태왕알파시티 2층 201호',
        description: '한식과 양식을 모두 즐길 수 있는 레스토랑입니다.',
        storeCode: '#REST0001',
        operatingDays: '월,화,수,목,금,토,일',
        openTime: '11:00',
        closeTime: '22:00',
        requestDate: new Date('2026-02-26'),
        status: 'pending',
      },
      {
        name: '알파 마트',
        category: 'convenience',
        ownerName: '박사장',
        phone: '053-345-6789',
        address: '대구광역시 수성구 알파시티 2로 33',
        addressDetail: '태왕알파시티 1층 102호',
        description: '생활용품과 간식을 편리하게 구매할 수 있습니다.',
        storeCode: '#CONV0001',
        operatingDays: '월,화,수,목,금,토,일',
        openTime: '07:00',
        closeTime: '23:00',
        requestDate: new Date('2026-02-20'),
        status: 'approved',
      },
      {
        name: '수성 기념품샵',
        category: 'shopping',
        ownerName: '최사장',
        phone: '053-456-7890',
        address: '대구광역시 수성구 알파시티 2로 37',
        description: '수성구 기념품과 관광 상품을 판매합니다.',
        storeCode: '#SHOP0001',
        operatingDays: '화,수,목,금,토,일',
        openTime: '10:00',
        closeTime: '19:00',
        requestDate: new Date('2026-02-18'),
        status: 'rejected',
      },
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
