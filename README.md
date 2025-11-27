# JeSafe - 안전 관광 도우미 앱

> 사고 데이터 기반 AI 안전 관광 보조 애플리케이션

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [아키텍처](#-아키텍처)
- [프로젝트 설정](#-프로젝트-설정)
- [빌드 및 실행](#-빌드-및-실행)

## 🎯 프로젝트 소개

JeSafe는 사고 데이터 및 관광 자료 기반으로 사용자에게 실시간 안전 관광 정보를 제공하는 안드로이드 애플리케이션입니다.
AI 채팅 기능을 통해 관광객이 안전 운전에 대한 조언을 받을 수 있으며, 지도 기반으로 위험 지역을 시각화합니다.

## ✨ 주요 기능

### 1. AI 채팅 안전 도우미

**실시간 스트리밍 채팅 시스템**

- **구현 위치**: `ui/main/AiChatDetailScreen.kt`, `ui/main/AiChatViewModel.kt`
- **핵심 기술**:
  ```kotlin
  // 스트리밍 응답 처리 (ChatRepository.kt)
  fun sendChatMessage(query: String, sessionId: String): Flow<String>
  ```
    - Server-Sent Events 방식의 스트리밍 JSON 파싱
    - Flow를 활용한 실시간 응답 전달
    - 불완전한 JSON 버퍼링으로 안정적인 데이터 처리

- **주요 UI 기능**:
    - 메시지 버블 (사용자/AI 구분)
    - 길게 눌러 복사/공유/삭제 컨텍스트 메뉴
    - 햅틱 피드백 및 스케일 애니메이션
    - 추천 질문 카드로 쉬운 시작

### 2. 사용자 인증 시스템

**카카오 소셜 로그인 및 자체 회원가입**

- **구현 위치**: `ui/auth/LoginScreen.kt`, `ui/auth/RegisterScreen.kt`
- **핵심 기능**:
    - 카카오 OAuth 2.0 연동
    - 비밀번호 유효성 검사 (영문, 숫자, 특수문자 포함 8자 이상)
    - 이메일 형식 검증
    - 전화번호 형식 검증
    - 추가 정보 입력 (생년월일, 기저질환 등)

### 3. 지도 기반 사고 정보 시각화

**Google Maps 연동**

- **구현 위치**: `ui/map/MapScreen.kt`
- **핵심 기능**:
    - Room Database를 통한 사고 데이터 로컬 저장
    - 지도 마커로 사고 다발 지역 표시
    - 실시간 위치 기반 안전 정보 제공

### 4. 사용자 프로필 관리

**개인정보 및 건강 정보 관리**

- **구현 위치**: `ui/main/MyInfoScreen.kt`, `ui/main/UnderlyingDiseaseScreen.kt`
- **핵심 기능**:
    - 이름, 이메일, 전화번호 변경
    - 비밀번호 변경 (기존 비밀번호 확인)
    - 기저질환 정보 관리 (고혈압, 당뇨, 심장질환 등)
    - 알림 설정

## 🛠 기술 스택

### UI Layer

- **Jetpack Compose**: 선언형 UI 프레임워크
- **Material Design 3**: 최신 디자인 시스템
- **Navigation Compose**: 화면 전환 관리

### Architecture

- **MVVM Pattern**: ViewModel을 통한 UI와 비즈니스 로직 분리
- **Clean Architecture**: Domain, Data, Presentation 레이어 분리
- **Hilt**: 의존성 주입 (Dependency Injection)

### Network & API

- **Retrofit**: RESTful API 통신
- **OkHttp**: HTTP 클라이언트 (타임아웃 설정)
- **Gson**: JSON 직렬화/역직렬화
- **Coroutines & Flow**: 비동기 처리 및 스트리밍

### Local Storage

- **Room Database**: 사고 데이터 로컬 캐싱
- **SharedPreferences**: 사용자 설정 저장

### Authentication

- **Kakao SDK**: 카카오 소셜 로그인

### Map

- **Google Maps SDK**: 지도 표시 및 마커 관리

## 🏗 아키텍처

```
app/
├── data/
│   ├── local/          # Room Database (DAO, Entity)
│   ├── network/        # Retrofit API, Repository
│   └── repository/     # 데이터 저장소 계층
├── di/                 # Hilt 의존성 주입 모듈
├── ui/
│   ├── auth/          # 인증 관련 화면
│   ├── main/          # 메인 기능 화면
│   ├── map/           # 지도 화면
│   └── theme/         # 디자인 테마
└── MainActivity.kt     # 앱 진입점
```

### 데이터 플로우

```
UI (Compose) 
    ↕
ViewModel (StateFlow)
    ↕
Repository
    ↕
┌─────────────┬──────────────┐
│             │              │
Local DB    API Service   Kakao SDK
(Room)      (Retrofit)
```

## ⚙️ 프로젝트 설정

### 1. 저장소 클론

```bash
git clone https://github.com/2025-ITEC001-003/JeSafe.git
cd JeSafe
```

### 2. API 키 및 서버 URL 설정

프로젝트 실행을 위해 필요한 API 키를 설정해야 합니다.

1. `app/secrets.properties` 생성:

2. `app/secrets.properties` 파일에 실제 API 키 입력:
   ```properties
   # Google Maps API 키 (Google Cloud Console에서 발급)
   MAPS_API_KEY=your_actual_google_maps_api_key
   
   # AI 채팅 서버 URL
   SERVER_URL=http://your-server-url:port/
   ```

> **⚠️ 중요**:
> - `app/secrets.properties` 파일은 `.gitignore`에 포함되어 Git에 커밋되지 않습니다.
> - 각 개발자는 자신의 환경에 맞는 API 키와 서버 URL을 설정해야 합니다.
> - Google Maps API 키는 [Google Cloud Console](https://console.cloud.google.com/)에서 발급받을 수 있습니다.

### 3. Kakao 개발자 등록 (선택사항)

카카오 로그인 기능을 사용하려면:

1. [Kakao Developers](https://developers.kakao.com/)에서 앱 등록
2. 네이티브 앱 키 발급
3. 프로젝트에 설정

## 🚀 빌드 및 실행

### Android Studio에서 실행

1. Android Studio에서 프로젝트 열기
2. `app/secrets.properties` 설정 확인
3. Run 버튼 클릭 (Shift + F10)

### Gradle 명령어로 빌드

```bash
# Debug 빌드
./gradlew assembleDebug

# Release 빌드
./gradlew assembleRelease

# 테스트 실행
./gradlew test
```

### 최소 요구사항

- **minSdk**: 24 (Android 7.0 Nougat)
- **targetSdk**: 36
- **compileSdk**: 36
- **Kotlin**: 1.9.0+
- **Gradle**: 8.13

## 📱 스크린샷

> TODO: 앱 스크린샷 추가 예정

## 🤝 기여

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feat/#issue-number`)
3. Commit your Changes (`git commit -m 'feat: 새로운 기능 추가'`)
4. Push to the Branch (`git push origin feat/#issue-number`)
5. Open a Pull Request

## 📄 라이센스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 팀원

- **팀 이름**: 2025-ITEC001-003

## 📞 문의

프로젝트에 대한 문의사항이 있으시면 Issue를 생성해주세요.

