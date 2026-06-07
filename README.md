## 커밋 메시지 컨벤션 (Commit Message Convention)

깃허브 저장소의 일관된 히스토리 관리를 위해 아래 규칙에 맞춰 커밋 메시지를 작성합니다. 제목과 본문 사이에는 한 칸의 빈 줄을 둡니다.

### 제목 양식

{이모지} {작업파일}: {작업내용}_{작업자}

### 이모지 종류

* ✨ 기능 추가 (Feature) : 새로운 소스 파일 생성, 백엔드 로직 구현, 웹 화면 UI 개발 등 기능적인 변경이 있을 때 사용합니다.
* 🎨 스타일 작업 (Style) : style.css 수정, JSP 파일의 단순 마크업 구조 변경, 인덴트나 포맷팅 등 코드의 의미적 변경 없이 외관만 수정할 때 사용합니다.
* ♻️ 코드 정리 (Refactor) : 기능의 변화는 없으나 가독성 향상, 패키지 구조 개선, 클래스 및 메소드 재구성 등 코드 구조를 리팩토링할 때 사용합니다.
* 🐛 수정 (Fix / Typo) : 버그 해결, 오탈자 수정, 인증 실패 오류나 암호화 로직 내 예외 처리 수정 등 잘못된 부분을 바로잡을 때 사용합니다.
* 📝 기타 (Docs / Chore) : README.md 등 문서 수정, .classpath 나 .project 같은 이클립스 설정 파일 업데이트, 외부 라이브러리(.jar) 추가 및 빌드 설정 변경 시 사용합니다.

### 커밋 예시

* ✨ processLogin.jsp: 로그인 폼 검증 및  기능 구현
* 🐛 KeyManager.java: 암호화 키 생성 오류 수정
* 🎨 style.css: 메인 작업 공간 레이아웃 스타일 수정

## 패키지 구조
```
src/
│
├── ui/
│   ├── MainConsole.java   # CLI 전체 실행 루프 및 제어 흐름 (컨트롤러)
│   └── ConsoleView.java   # 메뉴 템플릿, 텍스트 아트(ASCII ART) 상수 및 출력 전담
│
├── security/
│   ├── AuthService.java   # 회원가입/로그인 세션 제어 및 패스워드 기반 사설키 복구
│   └── CryptoService.java # AES 암·복호화, RSA 암·복호화, SHA256withRSA 전자서명/검증 (싱글톤)
│
├── data/
│   ├── User.java          # 사용자 보안 엔티티 (Hashed PW, Salt, IV, Encrypted Private Key 등)
│   ├── Envelope.java      # 전자봉투 메타데이터 엔티티 (수신자, 암호화된 AES 키, 서명, IV, 파일경로 등)
│   └── LocalDB.java       # 데이터 파일 영속성 및 인메모리 관리 저장소 (싱글톤)
│
└── data/letters/          # 암호화된 편지 본문 바이너리(.txt) 파일 저장소
```