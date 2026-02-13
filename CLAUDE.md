# 알파시티 스탬프 투어 (AlphaCity Stamp Tour)

관광지/명소를 방문하며 스탬프를 수집하는 모바일 애플리케이션.

## 프로젝트 구조

```
alphacity/
├── server/                    # Node.js + Next.js (API + 관리자 페이지)
│   ├── src/
│   │   ├── app/               # Next.js App Router (관리자 페이지)
│   │   │   ├── admin/         # 관리자 대시보드
│   │   │   ├── api/           # API Routes
│   │   │   └── layout.tsx
│   │   ├── lib/
│   │   │   ├── db.ts          # MySQL 연결 (mysql2)
│   │   │   ├── auth.ts        # 인증 유틸리티
│   │   │   └── swagger.ts     # Swagger 설정
│   │   ├── models/            # DB 모델 (Prisma)
│   │   ├── services/          # 비즈니스 로직
│   │   ├── middleware/        # 인증, 에러핸들링 등
│   │   └── types/             # TypeScript 타입 정의
│   ├── prisma/
│   │   └── schema.prisma      # DB 스키마
│   ├── public/                # 정적 파일
│   ├── package.json
│   ├── tsconfig.json
│   └── .env.local             # 환경변수 (git 제외)
│
├── android/                   # Android (Jetpack Compose)
│   └── app/src/main/
│       ├── java/com/alphacity/stamptour/
│       │   ├── ui/            # Composable 화면
│       │   │   ├── screen/    # 화면 단위 Composable
│       │   │   ├── component/ # 재사용 컴포넌트
│       │   │   ├── navigation/# Navigation 설정
│       │   │   └── theme/     # Material3 테마
│       │   ├── viewmodel/     # ViewModel (MVVM)
│       │   ├── model/         # 데이터 모델
│       │   ├── repository/    # 데이터 소스 추상화
│       │   ├── network/       # Retrofit API 인터페이스
│       │   ├── local/         # Room DB (로컬 캐시)
│       │   ├── di/            # Hilt DI 모듈
│       │   └── util/          # 유틸리티
│       └── res/               # 리소스
│
├── ios/                       # iOS (SwiftUI)
│   └── AlphaCityStampTour/
│       ├── App/               # App 진입점
│       ├── View/              # SwiftUI 뷰
│       │   ├── Screen/        # 화면 단위 뷰
│       │   ├── Component/     # 재사용 컴포넌트
│       │   └── Navigation/    # Navigation 설정
│       ├── ViewModel/         # ViewModel (MVVM)
│       ├── Model/             # 데이터 모델
│       ├── Repository/        # 데이터 소스 추상화
│       ├── Network/           # URLSession API 클라이언트
│       ├── Local/             # CoreData/SwiftData (로컬 캐시)
│       └── Util/              # 유틸리티
│
└── docs/                      # 프로젝트 문서
    ├── api.md                 # API 명세
    ├── database.md            # DB 설계
    └── deployment.md          # 배포 가이드
```

## 기술 스택

### 서버
- **Runtime**: Node.js 20+
- **Framework**: Next.js 15 (App Router)
- **Language**: TypeScript (strict mode)
- **ORM**: Prisma
- **DB**: MySQL 8.0
- **API 문서**: Swagger (next-swagger-doc + swagger-ui-react)
- **인증**: JWT (access + refresh token)
- **배포**: 네이버 클라우드 플랫폼 (Ubuntu Linux)

### Android
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM
- **DI**: Hilt
- **Network**: Retrofit2 + OkHttp + Kotlin Serialization
- **Image**: Coil
- **Local DB**: Room
- **Map**: Naver Map SDK (com.naver.maps:map-sdk)
- **TTS**: Android 기본 TextToSpeech API
- **Navigation**: Compose Navigation
- **비동기**: Kotlin Coroutines + Flow

### iOS
- **Language**: Swift
- **UI**: SwiftUI
- **Architecture**: MVVM
- **Network**: URLSession + async/await
- **Image**: AsyncImage (기본) / Kingfisher (필요시)
- **Local DB**: SwiftData
- **Map**: MapKit (Apple 기본)
- **TTS**: AVSpeechSynthesizer (Apple 기본)
- **Navigation**: NavigationStack
- **비동기**: Swift Concurrency (async/await)

## 아키텍처 원칙

### MVVM 패턴 (Android & iOS 공통)
```
View (UI) → ViewModel → Repository → DataSource (Remote/Local)
```
- **View**: UI 렌더링만 담당. 비즈니스 로직 없음
- **ViewModel**: UI 상태 관리, 비즈니스 로직 처리
- **Repository**: 데이터 소스 추상화 (네트워크/로컬 DB 선택)
- **Model**: 순수 데이터 클래스

### 플랫폼 간 일관성
- Android와 iOS는 **동일한 화면 구성, 레이아웃, 디자인**을 가짐
- 화면 이름, ViewModel 이름, Repository 이름을 양 플랫폼에서 동일하게 유지
- 예: `StampMapScreen` / `StampMapView`, `StampMapViewModel` (동일)
- 색상, 폰트 크기, 간격 등 디자인 토큰을 공유 상수로 관리

## 네이티브 기능 가이드

### Map
| 기능 | Android | iOS |
|------|---------|-----|
| 지도 표시 | Naver Map SDK | MapKit |
| 마커/핀 | Naver Marker | MKAnnotation |
| 현재 위치 | FusedLocationProvider | CLLocationManager |
| 지오펜싱 | Geofencing API | CLCircularRegion |

> 한국 서비스이므로 **네이버 지도 SDK**를 Android에서 사용.
> iOS는 MapKit이 한국 지원이 충분하므로 MapKit 사용.
> 만약 양 플랫폼 동일 지도가 필요하면 **Google Maps SDK**로 통일 가능.

### TTS (Text-to-Speech)
| 기능 | Android | iOS |
|------|---------|-----|
| TTS 엔진 | android.speech.tts.TextToSpeech | AVSpeechSynthesizer |
| 한국어 | Locale.KOREAN | AVSpeechSynthesisVoice(language: "ko-KR") |

> 양 플랫폼 모두 **OS 내장 TTS**를 사용. 외부 SDK 불필요.

## DB 스키마 (핵심 테이블)

```sql
-- 사용자
users (id, email, password_hash, nickname, profile_image, created_at, updated_at)

-- 스탬프 스팟 (방문 장소)
spots (id, name, description, latitude, longitude, address, image_url, tts_text, category, is_active, created_at)

-- 스탬프 투어 (코스)
tours (id, title, description, image_url, difficulty, estimated_time, is_active, created_at)

-- 투어-스팟 연결
tour_spots (id, tour_id, spot_id, order_index)

-- 스탬프 수집 기록
stamps (id, user_id, spot_id, tour_id, collected_at, latitude, longitude)

-- 뱃지/리워드
badges (id, name, description, image_url, condition_type, condition_value)

-- 사용자 뱃지
user_badges (id, user_id, badge_id, earned_at)
```

## API 설계 규칙

- RESTful 원칙 준수
- Base URL: `/api/v1`
- 인증: `Authorization: Bearer <token>`
- 응답 형식:
```json
{
  "success": true,
  "data": {},
  "message": "optional message"
}
```
- 에러 응답:
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "설명"
  }
}
```
- Swagger UI: `/api-docs`

### 주요 엔드포인트
```
POST   /api/v1/auth/register       # 회원가입
POST   /api/v1/auth/login          # 로그인
POST   /api/v1/auth/refresh        # 토큰 갱신

GET    /api/v1/tours               # 투어 목록
GET    /api/v1/tours/:id           # 투어 상세
GET    /api/v1/tours/:id/spots     # 투어의 스팟 목록

GET    /api/v1/spots               # 스팟 목록
GET    /api/v1/spots/:id           # 스팟 상세
GET    /api/v1/spots/nearby        # 근처 스팟 (위치 기반)

POST   /api/v1/stamps              # 스탬프 수집
GET    /api/v1/stamps/my           # 내 스탬프 목록

GET    /api/v1/badges              # 뱃지 목록
GET    /api/v1/badges/my           # 내 뱃지

GET    /api/v1/users/me            # 내 정보
PUT    /api/v1/users/me            # 내 정보 수정
```

## 관리자 페이지 (Next.js)

- 경로: `/admin/*`
- 기능: 투어/스팟/뱃지 CRUD, 사용자 관리, 통계 대시보드
- 인증: 관리자 전용 로그인 (role: admin)

## 코딩 컨벤션

### TypeScript (서버)
- strict mode 활성화
- 함수형 프로그래밍 선호
- 네이밍: camelCase (변수/함수), PascalCase (타입/인터페이스)
- 절대 경로 import (`@/`)

### Kotlin (Android)
- 네이밍: camelCase (변수/함수), PascalCase (클래스)
- Composable 함수: PascalCase (`@Composable fun StampCard()`)
- State: `StateFlow` + `collectAsStateWithLifecycle()`
- Coroutine scope: `viewModelScope`

### Swift (iOS)
- 네이밍: camelCase (변수/함수), PascalCase (타입)
- SwiftUI View: PascalCase (`struct StampCardView: View`)
- State: `@Published` + `@StateObject` / `@ObservedObject`
- async/await 우선 사용

## 배포

### 로컬 개발
```bash
# 서버
cd server && npm run dev     # http://localhost:3000

# MySQL
docker run -d --name alphacity-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=alphacity -p 3306:3306 mysql:8.0
```

### 네이버 클라우드 플랫폼 배포
- OS: Ubuntu 22.04 LTS
- Node.js: nvm으로 설치
- MySQL: apt 또는 Docker
- PM2로 프로세스 관리
- Nginx 리버스 프록시
- HTTPS: Let's Encrypt (certbot)

## 환경변수 (.env.local)

```env
DATABASE_URL="mysql://user:password@localhost:3306/alphacity"
JWT_SECRET="your-jwt-secret"
JWT_REFRESH_SECRET="your-refresh-secret"
NAVER_MAP_CLIENT_ID="your-naver-map-client-id"
NAVER_MAP_CLIENT_SECRET="your-naver-map-client-secret"
NEXT_PUBLIC_API_URL="http://localhost:3000/api/v1"
```

## 주요 화면 목록 (Android & iOS 동일)

1. **SplashScreen** - 앱 시작 화면
2. **LoginScreen** - 로그인
3. **RegisterScreen** - 회원가입
4. **HomeScreen** - 메인 (투어 목록)
5. **TourDetailScreen** - 투어 상세 (스팟 목록 + 지도)
6. **SpotDetailScreen** - 스팟 상세 (설명 + TTS + 지도)
7. **StampMapScreen** - 전체 지도 (스탬프 스팟 표시)
8. **StampCollectScreen** - 스탬프 수집 (위치 인증)
9. **MyStampsScreen** - 내 스탬프 컬렉션
10. **BadgesScreen** - 뱃지 목록
11. **ProfileScreen** - 프로필/설정
