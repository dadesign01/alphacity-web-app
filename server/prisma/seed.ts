import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
  // 기존 데이터 정리 (외래키 순서에 따라 삭제)
  await prisma.notification.deleteMany();
  await prisma.missionCompletion.deleteMany();
  await prisma.mission.deleteMany();
  await prisma.userCoupon.deleteMany();
  await prisma.storeCoupon.deleteMany();
  await prisma.coupon.deleteMany();
  await prisma.userStamp.deleteMany();
  await prisma.stamp.deleteMany();
  await prisma.eventParticipant.deleteMany();
  await prisma.event.deleteMany();
  await prisma.programPlace.deleteMany();
  await prisma.store.deleteMany();
  await prisma.program.deleteMany();
  await prisma.place.deleteMany();
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
  const food1 = await prisma.program.create({
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
  const food2 = await prisma.program.create({
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

  // 지오펜스 테스트 프로그램 (광명시 광이로 95 기준: 37.4789, 126.8671)
  const geoTestNear = await prisma.program.create({
    data: {
      name: '[테스트] 근거리 전시 (200m 이내)',
      description: '지오펜스 테스트용 프로그램입니다.\n광명시 광이로 95 기준 약 78m 거리입니다.\n→ 참여하기 버튼이 활성화되어야 합니다.',
      category: 'exhibition',
      operatingHours: '10:00 - 18:00',
      location: '광명시 광이로 95 인근 (약 78m)',
      latitude: 37.4796,
      longitude: 126.8671,
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-12-31'),
      status: 'in_progress',
    },
  });
  const geoTestFar = await prisma.program.create({
    data: {
      name: '[테스트] 원거리 세미나 (300m 초과)',
      description: '지오펜스 테스트용 프로그램입니다.\n광명시 광이로 95 기준 약 378m 거리입니다.\n→ 참여하기 버튼이 비활성화되어야 합니다.',
      category: 'seminar',
      speaker: '테스트 강사',
      operatingHours: '10:00 - 12:00',
      location: '광명시 광이로 95 원거리 (약 378m)',
      latitude: 37.4755,
      longitude: 126.8671,
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-12-31'),
      status: 'in_progress',
    },
  });
  console.log('Geofence test programs created');

  // ──────────────────────────────────────────────────────────────
  // 광명시 광이로 95 (트리우스 아파트) 주변 스탬프 투어 프로그램 5개
  // 기준점: 37.4789, 126.8671
  // ──────────────────────────────────────────────────────────────
  const gwang1 = await prisma.program.create({
    data: {
      name: '광이 어린이공원 포토존',
      description: '트리우스 아파트 바로 옆 광이 어린이공원을 방문하고\n귀여운 포토존에서 인증샷을 남겨보세요!',
      category: 'event',
      operatingHours: '09:00 - 21:00',
      location: '광명시 광이로 95 인근 어린이공원',
      latitude: 37.4794,
      longitude: 126.8678,
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-12-31'),
      status: 'in_progress',
    },
  });
  const gwang2 = await prisma.program.create({
    data: {
      name: '트리우스 카페거리',
      description: '트리우스 아파트 상가 1층 카페거리에서 커피 한 잔 즐기며\n여유로운 시간을 보내보세요.',
      category: 'food',
      operatingHours: '08:00 - 22:00',
      location: '광명시 광이로 95 트리우스 아파트 상가 1층',
      latitude: 37.4789,
      longitude: 126.8659,
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-12-31'),
      status: 'in_progress',
    },
  });
  const gwang3 = await prisma.program.create({
    data: {
      name: '광이로 문화 광장',
      description: '광이로 95 앞 문화 광장에서 진행되는 다양한 공연과 행사를\n직접 방문해 즐겨보세요!',
      category: 'event',
      operatingHours: '10:00 - 20:00',
      location: '광명시 광이로 95 앞 문화 광장',
      latitude: 37.4782,
      longitude: 126.8671,
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-12-31'),
      status: 'in_progress',
    },
  });
  const gwang4 = await prisma.program.create({
    data: {
      name: '광이 마을 작은도서관',
      description: '광이 마을 주민들을 위한 작은 도서관을 방문하고\n지역 문화를 느껴보세요.',
      category: 'exhibition',
      operatingHours: '09:00 - 18:00',
      location: '광명시 광이로 95 인근 주민센터',
      latitude: 37.4799,
      longitude: 126.8664,
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-12-31'),
      status: 'in_progress',
    },
  });
  const gwang5 = await prisma.program.create({
    data: {
      name: '광명 스탬프 기념 포토존',
      description: '광명시 스탬프 투어의 특별한 포토존!\n방문 인증 후 아름다운 추억을 사진으로 남겨보세요.',
      category: 'event',
      operatingHours: '00:00 - 23:59',
      location: '광명시 광이로 95 인근 포토존',
      latitude: 37.4793,
      longitude: 126.8684,
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-12-31'),
      status: 'in_progress',
    },
  });
  console.log('광이로 95 area programs created');

  // 광이로 미션
  await prisma.mission.createMany({
    data: [
      {
        name: '광이 어린이공원 방문 인증',
        type: 'location_auth',
        programId: gwang1.id,
      },
      {
        name: '광명시 퀴즈',
        type: 'quiz',
        programId: gwang1.id,
        question: '광명시의 유명한 관광 명소 동굴의 이름은?',
        answer: '광명동굴',
        options: JSON.stringify(['가평동굴', '광명동굴', '안양동굴', '시흥동굴']),
      },
      {
        name: '카페에서 여유롭게',
        type: 'stay_time',
        programId: gwang2.id,
        stayMinutes: 5,
      },
      {
        name: '트리우스 퀴즈',
        type: 'quiz',
        programId: gwang2.id,
        question: '광명시가 속한 광역자치단체는?',
        answer: '경기도',
        options: JSON.stringify(['서울특별시', '인천광역시', '경기도', '충청남도']),
      },
      {
        name: '문화 광장 방문 인증',
        type: 'location_auth',
        programId: gwang3.id,
      },
      {
        name: '도서관 방문 퀴즈',
        type: 'quiz',
        programId: gwang4.id,
        question: '독서의 날은 몇 월 몇 일인가요?',
        answer: '9월 12일',
        options: JSON.stringify(['9월 12일', '10월 9일', '11월 1일', '3월 14일']),
      },
      {
        name: '도서관 체류 미션',
        type: 'stay_time',
        programId: gwang4.id,
        stayMinutes: 3,
      },
      {
        name: '포토존 방문 인증',
        type: 'location_auth',
        programId: gwang5.id,
      },
    ],
  });
  console.log('광이로 95 area missions created');

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
  // 체험 이벤트
  await prisma.event.createMany({
    data: [
      {
        programId: program1.id,
        name: '도자기 만들기',
        description: '전통 도예 기법으로 나만의 도자기를\n만들어 보세요.',
        imageUrl: '/uploads/event_img_1.png',
        type: 'experience',
        startDate: new Date('2026-02-01'),
        endDate: new Date('2026-03-31'),
        participantLimit: 0,
        status: 'in_progress',
        price: 15000,
        duration: 90,
        capacity: 10,
        location: '알파시티 2로 33 공예 체험관',
      },
      {
        programId: program2.id,
        name: '천연비누 원데이 클래스',
        description: '천연 재료로 만드는 나만의 향기가득\n비누 만들기 원데이 클래스',
        imageUrl: '/uploads/event_img_2.png',
        type: 'experience',
        startDate: new Date('2026-02-01'),
        endDate: new Date('2026-03-31'),
        participantLimit: 0,
        status: 'in_progress',
        price: 12000,
        duration: 60,
        capacity: 8,
        location: '알파시티 2로 33 DIY 공방',
      },
      {
        programId: program3.id,
        name: '3D 프린팅 액티비티',
        description: '3D 프린터로 나의 상상을 현실화하는\n나만의 작품을 뽐내보세요.',
        imageUrl: '/uploads/event_img_3.png',
        type: 'experience',
        startDate: new Date('2026-02-01'),
        endDate: new Date('2026-03-31'),
        participantLimit: 0,
        status: 'in_progress',
        price: 0,
        duration: 120,
        capacity: 6,
        location: '알파시티 2로 33 3D 프린팅 스튜디오',
      },
      {
        programId: program1.id,
        name: '수제 브레드 원데이 클래스',
        description: '유명 베이커리 카페 제빵사가 알려주는\n맛있는 빵 레시피! 제빵 체험해보세요.',
        imageUrl: '/uploads/event_img_1.png',
        type: 'experience',
        startDate: new Date('2026-02-01'),
        endDate: new Date('2026-03-31'),
        participantLimit: 0,
        status: 'in_progress',
        price: 18000,
        duration: 100,
        capacity: 12,
        location: '알파시티 2로 33 ABC 베이커리',
      },
    ],
  });
  console.log('Events created');

  // 장소
  const places = await Promise.all([
    prisma.place.create({ data: { name: '알파시티 카페', category: 'food', latitude: 37.5665, longitude: 126.978, address: '서울특별시 중구 세종대로 110' } }),
    prisma.place.create({ data: { name: 'AI 전시관', category: 'exhibition', latitude: 37.5665, longitude: 126.979 } }),
    prisma.place.create({ data: { name: '세미나홀', category: 'seminar', latitude: 37.5666, longitude: 126.978 } }),
    prisma.place.create({ data: { name: '스탬프 이벤트 광장', category: 'event', latitude: 37.5664, longitude: 126.978 } }),
  ]);
  console.log('Places created');

  // 미션
  await prisma.mission.createMany({
    data: [
      { name: '포토존 인증', type: 'location_auth', placeId: places[3].id },
      { name: '프로그램 퀴즈', type: 'quiz', question: '프로그램 시작 연도는?', answer: '2025', options: JSON.stringify(['2023', '2024', '2025', '2026']) },
      { name: '푸드코트 체류', type: 'stay_time', placeId: places[1].id, stayMinutes: 5 },
    ],
  });
  console.log('Missions created');

  // 지오펜스 테스트 미션
  await prisma.mission.createMany({
    data: [
      {
        name: '[테스트] 광명 퀴즈',
        type: 'quiz',
        programId: geoTestNear.id,
        question: '광명시의 유명 관광지는?',
        answer: '광명동굴',
        options: JSON.stringify(['광명시청', '광명동굴', '이케아', '코스트코']),
      },
      {
        name: '[테스트] 세미나 체류 미션',
        type: 'stay_time',
        programId: geoTestFar.id,
        stayMinutes: 1,
      },
    ],
  });
  console.log('Geofence test missions created');

  // 스탬프 (개별 생성 - 미션 연결을 위해 ID 필요)
  const stamp1 = await prisma.stamp.create({
    data: { name: '첫 방문 스탬프', conditionType: 'place_visit', conditionDetail: '프로그램 첫 방문', imageUrl: '/uploads/stamp_1.png' },
  });
  const stamp2 = await prisma.stamp.create({
    data: { name: '미션 완료 스탬프', conditionType: 'mission_complete', conditionDetail: '미션 완료 시 적립', imageUrl: '/uploads/stamp_2.png' },
  });
  const stamp3 = await prisma.stamp.create({
    data: { name: '이벤트 참여 스탬프', conditionType: 'event_participate', conditionDetail: '이벤트 1회 참여', imageUrl: '/uploads/stamp_3.png' },
  });

  // 미션에 스탬프 연결
  const allMissions = await prisma.mission.findMany({ orderBy: { id: 'asc' } });
  if (allMissions.length >= 3) {
    await prisma.mission.update({ where: { id: allMissions[0].id }, data: { stampId: stamp1.id } });
    await prisma.mission.update({ where: { id: allMissions[1].id }, data: { stampId: stamp2.id } });
    await prisma.mission.update({ where: { id: allMissions[2].id }, data: { stampId: stamp3.id } });
  }
  if (allMissions.length >= 4) {
    await prisma.mission.update({ where: { id: allMissions[3].id }, data: { stampId: stamp1.id } });
  }
  if (allMissions.length >= 5) {
    await prisma.mission.update({ where: { id: allMissions[4].id }, data: { stampId: stamp2.id } });
  }
  console.log('Stamps created and linked to missions');

  // 쿠폰
  const coupon1 = await prisma.coupon.create({
    data: { name: '커피 무료 쿠폰', description: '제휴 카페에서 사용 가능한 커피 무료 쿠폰', requiredStamps: 3, validUntil: new Date('2026-12-31') },
  });
  const coupon2 = await prisma.coupon.create({
    data: { name: '기념품 교환권', description: '프로그램 기념품을 교환할 수 있는 쿠폰', requiredStamps: 5, validUntil: new Date('2026-12-31') },
  });
  const coupon3 = await prisma.coupon.create({
    data: { name: '10% 할인 쿠폰', description: '프로그램 내 상점 10% 할인', requiredStamps: 2, validUntil: new Date('2026-12-31') },
  });
  const coupon4 = await prisma.coupon.create({
    data: { name: '런치 세트 할인권', description: '런치 세트 메뉴 20% 할인', requiredStamps: 4, validUntil: new Date('2026-12-31') },
  });
  console.log('Coupons created');

  // 상점 (프로그램 연결)
  const store1 = await prisma.store.create({
    data: {
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
      programId: food1.id,
      requestDate: new Date('2026-02-25'),
      status: 'approved',
    },
  });
  const store2 = await prisma.store.create({
    data: {
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
      programId: food2.id,
      requestDate: new Date('2026-02-26'),
      status: 'approved',
    },
  });
  const store3 = await prisma.store.create({
    data: {
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
      programId: food1.id,
      requestDate: new Date('2026-02-20'),
      status: 'approved',
    },
  });
  await prisma.store.create({
    data: {
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
  });
  console.log('Stores created');

  // 상점-쿠폰 연결
  await prisma.storeCoupon.createMany({
    data: [
      { storeId: store1.id, couponId: coupon1.id },
      { storeId: store1.id, couponId: coupon3.id },
      { storeId: store2.id, couponId: coupon3.id },
      { storeId: store2.id, couponId: coupon4.id },
      { storeId: store3.id, couponId: coupon2.id },
    ],
  });
  console.log('Store-Coupon links created');

  // 알림
  await prisma.notification.createMany({
    data: [
      { title: '새로운 이벤트 시작!', message: '스탬프 랠리 이벤트가 시작되었습니다.', target: 'all', recipientCount: 1247 },
      { title: '프로그램 일정 안내', message: '오늘 공연 일정을 확인하세요.', target: 'all', recipientCount: 1150 },
      { title: '쿠폰 사용 마감 임박', message: '3일 후 쿠폰 유효기간이 종료됩니다.', target: 'active', recipientCount: 892 },
    ],
  });
  console.log('Notifications created');

  // 배너
  await prisma.banner.createMany({
    data: [
      { title: '수성알파시티 스탬프 투어', imageUrl: '/uploads/program_img_1.png', sortOrder: 0, isActive: true },
      { title: '봄맞이 스탬프 랠리', imageUrl: '/uploads/program_img_2.png', sortOrder: 1, isActive: true },
    ],
  });
  console.log('Banners created');

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
