# 카카오 로그인 설정 정보

## 하드코딩된 정보

### 1. 네이티브 앱 키 (Native App Key)
```
68c990d0dffd2c76950df08fc29f42a0
```

**위치:**
- `app/src/main/AndroidManifest.xml` (meta-data)
- `app/src/main/java/team/swyp/sdu/WalkingBuddyApplication.kt` (SDK 초기화)
- `app/src/main/AndroidManifest.xml` (리다이렉트 URI 스킴)

### 2. 리다이렉트 URI
```
kakaodd4a75c8aa122d46a52c64492a973b63://oauth
```

**위치:**
- `app/src/main/AndroidManifest.xml` (AuthCodeHandlerActivity의 intent-filter)

### 3. 카카오 SDK 버전
```
2.23.1
```

**위치:**
- `gradle/libs.versions.toml` (kakaoSdk 버전)

### 4. 카카오 SDK 저장소
```
https://devrepo.kakao.com/nexus/content/groups/public/
```

**위치:**
- `settings.gradle.kts` (Maven 저장소)

## 카카오 개발자 콘솔에서 설정해야 할 사항

### 1. 플랫폼 설정
- **Android 플랫폼 추가**
- **패키지명**: `team.swyp.sdu`
- **키 해시**: 개발용 키 해시 등록 필요
  - **디버그 키 해시 (SHA1 - Base64)**: `v2opGwwChMrG7VRm/Bc83bScWLE=`
  - **디버그 키 해시 (SHA256 - Base64)**: `81rdq0MOi++jdmybGj8w4IUSjPNlRCeDguLodgytFWQ=`
  - **디버그 키 해시 (SHA1 - 콜론 구분)**: `BF:6A:29:1B:0C:02:84:CA:C6:ED:54:66:FC:17:3C:DD:B4:9C:58:B1`
  - **디버그 키 해시 (SHA256 - 콜론 구분)**: `F3:5A:DD:AB:43:0E:8B:EF:A3:76:6C:9B:1A:3F:30:E0:85:12:8C:F3:65:44:27:83:82:E2:E8:76:0C:AD:15:64`
  - 릴리즈 키 해시: 릴리즈 키스토어가 생성되면 동일한 방법으로 생성 필요

### 2. 리다이렉트 URI 등록
카카오 개발자 콘솔 > 앱 설정 > 플랫폼 > Android에서 다음 URI 등록:
```
kakao68c990d0dffd2c76950df08fc29f42a0://oauth
```

### 3. 카카오 로그인 활성화
- 카카오 개발자 콘솔 > 제품 설정 > 카카오 로그인 > 활성화 설정 > ON

### 4. 동의항목 설정 (필요시)
- 카카오 개발자 콘솔 > 제품 설정 > 카카오 로그인 > 동의항목
- 필수/선택 동의항목 설정

## 구현된 기능

### 1. 로그인 화면 (`LoginScreen.kt`)
- 카카오 로그인 버튼 제공
- 로딩 상태 표시
- 에러 메시지 표시

### 2. 로그인 ViewModel (`LoginViewModel.kt`)
- 카카오톡으로 로그인 (우선 시도)
- 카카오계정으로 로그인 (카카오톡 미설치 시)
- 로그인 상태 확인
- 로그아웃 기능

### 3. 네비게이션
- 앱 시작 시 로그인 화면 표시
- 로그인 성공 시 메인 화면으로 이동
- 로그인 화면은 백 스택에서 제거

## 테스트 방법

1. 앱 실행 시 로그인 화면이 표시되는지 확인
2. 카카오 로그인 버튼 클릭
3. 카카오톡 또는 카카오계정으로 로그인
4. 로그인 성공 시 메인 화면으로 이동 확인

## 주의사항

1. **키 해시 등록**: 카카오 개발자 콘솔에 키 해시를 등록하지 않으면 로그인이 실패할 수 있습니다.
2. **리다이렉트 URI**: AndroidManifest.xml의 리다이렉트 URI와 카카오 개발자 콘솔에 등록한 URI가 정확히 일치해야 합니다.
3. **네이티브 앱 키**: 프로덕션 환경에서는 보안을 위해 BuildConfig나 환경 변수로 관리하는 것을 권장합니다.

