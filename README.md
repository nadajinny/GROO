# GROO 협업 플랫폼 백엔드

실험실·소규모 연구팀이 일정·프로젝트·메시지를 통합 관리할 수 있도록 설계된 GROO 서비스의 Spring Boot 백엔드입니다. JWT 기반 인증, 소셜 로그인, MySQL/Redis, Swagger/Postman 자동 문서화, 20개 이상의 MockMvc 테스트를 포함하고 있습니다.

## 목차
1. [아키텍처 개요](#아키텍처-개요)
2. [핵심 기능](#핵심-기능)
3. [프로젝트 구조](#프로젝트-구조)
4. [로컬 개발/실행 가이드](#로컬-개발실행-가이드)
5. [환경 변수](#환경-변수)
6. [API 문서 및 도구](#api-문서-및-도구)
7. [JCloud 배포 지침](#jcloud-배포-지침)
8. [DB 및 시드 데이터](#db-및-시드-데이터)
9. [인증·인가 흐름](#인증인가-흐름)
10. [엔드포인트 요약](#엔드포인트-요약)
11. [테스트 전략](#테스트-전략)
12. [보안·성능 고려 사항](#보안성능-고려-사항)
13. [향후 보강 계획](#향후-보강-계획)

---

## 아키텍처 개요
| 계층 | 기술 | 설명 |
| --- | --- | --- |
| API | Spring Boot 3.2, SpringDoc | `/api/**` REST + 통일된 `ApiResponse`. |
| 인증 | Spring Security, JWT, Firebase·Google | Access/Refresh 발급 및 검증, Redis 블랙리스트. |
| 데이터 | MySQL 8, Flyway | 사용자·그룹·프로젝트·태스크·활동 로그. |
| 캐시/보조 | Redis | 토큰 철회 및 향후 캐싱 확장. |
| 문서화 | Swagger UI, Postman, MockMvc | 자동 테스트/문서화 파이프라인. |

세부 시퀀스와 ERD는 `docs/` 디렉터리(추가 예정)에서 관리합니다.

## 핵심 기능
- **로컬/소셜 로그인**: 이메일 등록·로그인과 Google/Firebase 소셜 로그인 지원.
- **RBAC**: ROLE_USER, ROLE_ADMIN 기반 접근 제어, 관리자 전용 엔드포인트 제공.
- **워크스페이스 도메인**: 그룹, 멤버 초대, 프로젝트, 태스크·서브태스크·댓글·활동 로그, 페이지네이션/검색/정렬.
- **일관된 오류 포맷**: `ErrorCode` 열거형으로 400~500 상태 코드 매핑.
- **레이트리밋/검증**: Bean Validation + IP 기반 슬라이딩 윈도우 제한 (`app.rate-limit.*`).
- **자동화**: Swagger 문서, Postman 테스트 스크립트, 22건 MockMvc 시나리오, Flyway 시드 데이터.

## 프로젝트 구조
```
backend/
 ├─ src/main/java/com/groo/
 │   ├─ config/        # 시큐리티, CORS, OpenAPI, RateLimit 설정
 │   ├─ controller/    # REST 컨트롤러
 │   ├─ domain/        # 엔티티 + 리포지토리
 │   ├─ dto/           # 요청/응답 DTO
 │   ├─ security/      # JWT 필터, 사용자 상세, 레이트리밋 필터
 │   ├─ service/       # 비즈니스 로직, OAuth 검증, 토큰 블랙리스트
 │   └─ common/        # ApiResponse, 예외 처리
 ├─ src/main/resources/
 │   ├─ application.yml
 │   └─ db/migration/  # Flyway DDL/seed
 ├─ src/test/java/com/groo/
 │   ├─ controller/**  # MockMvc 통합 테스트
 │   └─ support/**     # 테스트 공통 유틸
 ├─ Dockerfile
 └─ build.gradle
```

## 로컬 개발/실행 가이드
### 준비물
- JDK 17 이상
- MySQL 8.x, Redis 7.x (로컬 또는 Docker)
- Gradle Wrapper 사용

### Gradle 로컬 실행
```bash
cd backend
cp ../.env.example ../.env   # 필요 값 입력
./gradlew flywayMigrate      # 스키마+시드 적용
./gradlew bootRun
```
기본 포트는 `http://localhost:8080` 입니다.

### Docker Compose
```bash
cp .env.example .env
docker compose up -d --build
```
노출 포트: MySQL `3307`, Redis `6380`, Backend `8080` (환경 변수로 조정 가능).

### 주요 명령어
| 명령 | 설명 |
| --- | --- |
| `./gradlew test` | 전체 단위/통합 테스트 실행(H2). |
| `./gradlew bootJar` | 실행 JAR 빌드. |
| `docker compose logs -f backend` | 컨테이너 로그 확인. |

## 환경 변수
`.env.example`를 참고하여 `.env`를 작성하세요.

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `jdbc:mysql://localhost:3306/groo` | MySQL 연결 정보. |
| `REDIS_HOST`, `REDIS_PORT` | `localhost`, `6379` | Redis 연결 정보. |
| `JWT_SECRET` | `change-me...` | 256비트 이상 비밀키 **필수**. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | 허용 도메인(쉼표 구분). |
| `GOOGLE_CLIENT_ID`, `FIREBASE_SERVICE_ACCOUNT_FILE` | - | 소셜 로그인 설정. |
| `RATE_LIMIT_ENABLED`, `RATE_LIMIT_WINDOW_SECONDS`, `RATE_LIMIT_MAX_REQUESTS` | `true / 60 / 200` | 레이트리밋 필터 제어. |

`.env`와 비밀 정보는 공개 저장소에 올리지 말고 별도 채널(Classroom 등)로 제출하세요.

## API 문서 및 도구
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`  
  - “Authorize” 버튼 클릭 → `Bearer <access-token>` 입력 후 보호 API 테스트.  
  - 모든 엔드포인트에 400/401/403/404/422/500 응답 스키마 자동 주입.
- **OpenAPI**: `http://localhost:8080/v3/api-docs`
- **Postman**: `postman/groo.postman_collection.json`  
  - 변수: `BASE_URL`, `ACCESS_TOKEN`, `REFRESH_TOKEN`.  
  - 헬스체크, 로그인, 토큰 저장, 그룹/프로젝트/태스크 흐름을 자동 테스트하는 스크립트 포함.

## JCloud 배포 지침
1. `docker compose up -d --build` 또는 CI/CD로 이미지 빌드 후 서버에 배포.  
2. 포트 포워딩/리버스 프록시(JCloud 규칙 참고)로 외부 접근 허용.  
3. 제출 시 다음 자료 포함:  
   - Base URL, Swagger URL, Health URL  
   - `.env` 원본(비공개 제출), DB 계정/명령어, SSH 키(.ppk/.pem)  
   - 헬스체크 200 스크린샷  
4. `restart: unless-stopped` 적용으로 재부팅 시 자동 복구.

## DB 및 시드 데이터
Flyway `V1__init_core_tables.sql`로 스키마 생성 후 `V2__seed_data.sql`로 다음 데이터 삽입:
- 사용자 20명 (ADMIN/USER 혼합)
- 워크스페이스 12개 + 멤버십/초대 코드
- 프로젝트 24개, 태스크 80개, 서브태스크 80개
- 댓글 40개, 활동 로그 30개

통계/페이지네이션/검색 검증에 활용할 수 있도록 설계되었습니다.

## 인증·인가 흐름
- `/api/auth/register`, `/api/auth/login`: 로컬 계정용.
- `/api/auth/google`, `/api/auth/firebase`: 소셜 로그인.
- `/api/auth/refresh`: Refresh 토큰으로 Access 재발급.
- `/api/auth/logout`: Refresh 철회 + Access 토큰 Redis 블랙리스트 등록.
- `/api/users/me`: 현재 사용자 프로필.
- ROLE_USER 기본 부여, `/api/admin/**`는 ROLE_ADMIN 필요.
- `app.rate-limit.*` 값으로 IP 기반 요청 제한 (기본 200req/min).

### 시드 계정 예시
| 역할 | 이메일 | 비밀번호 |
| --- | --- | --- |
| Admin | `seed01@groo.local` | `SeedUser123!` |
| User | `seed02@groo.local` | `SeedUser123!` |

## 엔드포인트 요약
| URL | 메서드 | 설명 | 인증 |
| --- | --- | --- | --- |
| `/api/auth/register` | POST | 회원가입 | 공개 |
| `/api/auth/login` | POST | 로그인/토큰 발급 | 공개 |
| `/api/auth/refresh` | POST | 토큰 재발급 | 공개 |
| `/api/auth/logout` | POST | 토큰 철회 | Bearer |
| `/api/users/me` | GET | 내 정보 조회 | Bearer |
| `/api/groups` | GET/POST | 그룹 조회/생성(페이지·검색 지원) | Bearer |
| `/api/groups/{id}` | GET/PATCH/DELETE | 그룹 상세·수정·삭제 | Bearer, 권한 필요 |
| `/api/groups/{id}/members` | GET/POST/DELETE | 멤버 관리/초대 | Bearer |
| `/api/projects` | GET/POST | 프로젝트 목록·생성 | Bearer |
| `/api/tasks` | GET/POST/PATCH | 태스크 CRUD, 서브태스크/댓글 | Bearer |
| `/api/admin/users/{id}/roles` | PATCH | 권한 변경 | Bearer(Admin) |
| `/api/health` | GET | 헬스 체크 | 공개 |

Swagger 문서에서 나머지 30+ 엔드포인트와 쿼리 파라미터(page/size/sort/keyword 등)를 확인하세요.

## 테스트 전략
```bash
cd backend
./gradlew test
```
- 테스트 프로필(H2) 사용, `app.rate-limit.enabled=false`.
- `IntegrationTestSupport`가 사용자/그룹/프로젝트 데이터를 자동 생성 후 정리.
- Auth, Users, Groups, Projects/Tasks, Admin 시나리오 총 22건 이상 커버.

## 보안·성능 고려 사항
- BCrypt 패스워드 해시, 256비트 JWT 비밀키.
- Redis 토큰 블랙리스트 및 장애 시 fallback 로깅.
- RateLimit 필터로 DoS 완화(환경 변수 기반 튜닝).
- CORS 허용 목록/와일드카드 지원.
- Bean Validation + `ErrorCode` 기반 예외 포맷 통일.
- Flyway 기반 스키마 버전 관리, Docker Compose로 MySQL/Redis 일괄 구동.
- 향후 분산 레이트리밋(Bucket4J+Redis)와 상세 로깅/모니터링(Prometheus, ELK) 연동 예정.

## 향후 보강 계획
- `docs/` 디렉터리에 API 설계/아키텍처/ERD 문서 업로드.
- GitHub Actions CI로 테스트·이미지 빌드 자동화.
|- Postman(Newman) 테스트를 CI 단계에 통합.
|- 모니터링·알림 시스템 추가 및 다중 인스턴스 레이트리밋 업그레이드.

문의나 배포 지원이 필요하면 Swagger Info 섹션의 연락처 또는 담당자에게 문의하세요.
