# GROO

> 실험실 협업을 위한 올인원 웹 워크스페이스 — 그룹·프로젝트·태스크·메신저를 하나의 Vue 3 + Firebase SPA에서 관리합니다.

## 목차
- [1. 프로젝트 소개](#1-프로젝트-소개)
- [2. 핵심 기능](#2-핵심-기능)
- [3. 화면 구성 및 라우트](#3-화면-구성-및-라우트)
- [4. 기술 스택](#4-기술-스택)
- [5. 폴더 구조](#5-폴더-구조)
- [6. 상태 관리 & 서비스 레이어](#6-상태-관리--서비스-레이어)
- [7. Firestore · Storage 데이터 모델](#7-firestore--storage-데이터-모델)
- [8. Firebase & 환경 변수 설정](#8-firebase--환경-변수-설정)
- [9. 개발 및 빌드 스크립트](#9-개발-및-빌드-스크립트)
- [10. UI/UX 구성 요소 가이드](#10-uiux-구성-요소-가이드)
- [11. 주의 사항 & 향후 개선 아이디어](#11-주의-사항--향후-개선-아이디어)

## 1. 프로젝트 소개
GROO는 실험실·연구실·소규모 팀이 하나의 브라우저 환경에서 그룹, 프로젝트, 태스크, 메시지를 통합적으로 관리할 수 있도록 설계된 웹 애플리케이션입니다. 대시보드 중심의 정보 집계와 프로젝트·태스크·메시지를 그룹 컨텍스트로 자연스럽게 연결하는 것이 핵심 목표입니다.

- Vue 3 (Composition API) 기반 SPA
- Pinia를 활용한 전역 상태 관리
- Firebase Firestore / Storage 실시간 연동
- 커스텀 이메일 인증(데모용)
- 그룹 단위 협업에 최적화된 UI 구조

## 2. 핵심 기능
| 영역 | 설명 |
| --- | --- |
| 온보딩 & 인증 | 이메일 기반 회원가입/로그인. `auth.ts` 스토어가 로컬 세션(`groo-account-uid`)을 복원하며, Firestore `accounts` 컬렉션에 해시된 비밀번호를 저장합니다. |
| 그룹 & 워크스페이스 | 그룹 생성, 현재 작업 그룹 선택, 멤버 초대 기능 제공. 사이드바 및 배너를 통해 모든 화면에서 동일한 그룹 컨텍스트 유지 |
| 프로젝트 & 태스크 | 그룹별 프로젝트 관리, 노트/파일/태스크를 한 화면에서 편집. 태스크 상세 뷰에서 서브태스크·댓글·활동 로그 관리 |
| 대시보드 | 마감 일정 캘린더, 나의 할 일, 프로젝트 알림을 그룹 기준으로 집계 |
| 메시징 | 그룹 채널 + 1:1 DM 지원. 친구 추가, 실시간 메시지 스트림 제공 |
| 프로필 & 테마 | 닉네임/핸들 수정, 다크·라이트 테마 전환 |

## 3. 화면 구성 및 라우트
로그인 전·후 접근 제어는 `src/router/index.ts`의 전역 가드에서 `authStore.waitForReady()`를 통해 처리됩니다. `/`, `/login`, `/features`는 공개 경로, `/app/*`는 인증 이후 레이아웃(`AppLayout`)을 공유합니다.

| 경로 | View | 설명 |
| --- | --- | --- |
| `/` | `HomeView.vue` | 랜딩 페이지 |
| `/login` | `LoginView.vue` | 이메일 로그인 / 회원가입 |
| `/features` | `FeaturesView.vue` | 기능 소개 |
| `/app` | `AppLayout.vue` | 인증 후 공통 레이아웃 |
| `/app/dashboard` | `DashboardView.vue` | 일정·알림·개인 할 일 |
| `/app/groups` | `GroupsView.vue` | 그룹 목록 / 선택 |
| `/app/groups/:groupId` | `GroupDetailView.vue` | 그룹 메타 정보 / 멤버 관리 |
| `/app/projects` | `ProjectsView.vue` | 프로젝트·노트·파일·태스크 |
| `/app/tasks/:taskId` | `TaskDetailView.vue` | 태스크 상세 |
| `/app/messages` | `MessagesView.vue` | 그룹 채널 / DM |
| `/app/profile` | `ProfileView.vue` | 프로필 / 테마 설정 |

## 4. 기술 스택
### 프런트엔드
- Vue 3.5 (Composition API)
- TypeScript 5.9
- Vite 7 (`@` alias 포함)
- Pinia 2.2
- Vue Router 4.4

### Firebase
- Firestore (데이터 저장)
- Storage (파일 업로드)
- Analytics (선택)

### 기타
- `date-fns` (날짜 처리)
- `npm-run-all2` (빌드 파이프라인)
- `prettier` (코드 포맷)
- Paperlogy 폰트(동봉)
- 권장 Node 버전: ^20.19.0 혹은 >=22.12.0

## 5. 폴더 구조
핵심 폴더는 아래와 같이 정리되어 있습니다.

```
src/
├─ assets/            # 글로벌 스타일, 폰트
├─ components/        # 재사용 컴포넌트
├─ layouts/           # 인증 후 공통 레이아웃
├─ router/            # 라우터 + 전역 가드
├─ services/          # Firestore / Storage 접근 계층
├─ stores/            # Pinia 스토어
├─ views/             # 라우트별 화면
├─ firebase.ts        # Firebase 초기화
└─ types.ts           # 공용 타입 정의
```

## 6. 상태 관리 & 서비스 레이어
Pinia 스토어와 서비스 계층을 통해 Firebase 코드를 캡슐화하고, 뷰 컴포넌트는 상태/이벤트만 구독합니다.

### Pinia 스토어 (`src/stores`)
| 스토어 | 역할 |
| --- | --- |
| `auth.ts` | 인증, 세션 복구, 사용자 프로필 동기화 |
| `group.ts` | 그룹 및 멤버십 관리 |
| `social.ts` | 친구, DM, 그룹 메시지 |
| `theme.ts` | 다크/라이트 테마 |

### 서비스 (`src/services`)
- `accountService.ts`: 이메일/비밀번호 기반 계정 관리 (데모용)
- `userProfileService.ts`: 사용자 프로필 및 핸들 관리
- `projectService.ts`, `taskService.ts`: 프로젝트·태스크 CRUD
- `projectNoteService.ts`, `projectFileService.ts`: 노트, 파일 업로드
- `userDirectory.ts`: 캐시된 프로필 조회

## 7. Firestore · Storage 데이터 모델
| 리소스 | 설명 |
| --- | --- |
| `accounts` | 이메일 / 해시 비밀번호 (데모 인증) |
| `userProfiles` | 사용자 공개 프로필 |
| `groups` | 그룹 메타 정보 |
| `groupMembers` | 그룹 멤버 및 역할 |
| `projects` | 그룹별 프로젝트 |
| `tasks` | 태스크 + 서브컬렉션 (subtasks/comments/activity) |
| `friendships` | 친구 관계 |
| `directMessages` | 1:1 메시지 |
| `groupMessages` | 그룹 채널 메시지 |
| Storage `projects/{projectId}/files/*` | 프로젝트 파일 업로드 |

보안 규칙은 `firestore.rules`에 정의되어 있으며 그룹 멤버십과 역할 기반 접근 제어를 수행합니다.

## 8. Firebase & 환경 변수 설정
1. Firebase 콘솔에서 Firestore(네이티브), Storage, (선택) Analytics를 활성화합니다.
2. 커스텀 인증을 사용하므로 Firebase Authentication provider 설정은 필요 없습니다.
3. `firestore.rules`를 프로젝트에 배포합니다.
4. `.env` 또는 `.env.local`에 Vite 환경 변수를 입력합니다.

```bash
VITE_FIREBASE_API_KEY=your-api-key
VITE_FIREBASE_AUTH_DOMAIN=your-project.firebaseapp.com
VITE_FIREBASE_DATABASE_URL=https://your-project-default-rtdb.firebaseio.com
VITE_FIREBASE_PROJECT_ID=your-project-id
VITE_FIREBASE_STORAGE_BUCKET=your-project.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=your-sender-id
VITE_FIREBASE_APP_ID=your-app-id
VITE_FIREBASE_MEASUREMENT_ID=G-XXXXXXXXXX
```

`src/firebase.ts`가 위 변수를 읽어 초기화하며, 개발 서버(`npm run dev`) 재시작 시 값이 반영됩니다.

## 9. 개발 및 빌드 스크립트
| 명령어 | 설명 |
| --- | --- |
| `npm install` | 의존성 설치 |
| `npm run dev` | Vite 개발 서버 실행 (기본 포트 5173) |
| `npm run build` | `vue-tsc` 타입 검사 후 `vite build` |
| `npm run preview` | `dist/` 빌드 결과 미리보기 |
| `npm run type-check` | Vue + TS 타입 검사만 수행 |
| `npm run format` | `src/` 이하 파일 Prettier 정렬 |

## 10. UI/UX 구성 요소 가이드
- `AppLayout.vue`: 헤더 + 그룹 사이드바 + 콘텐츠로 인증 이후 기본 프레임을 구성합니다.
- `GroupSidebar.vue`, `CurrentWorkspaceBanner.vue`: 현재 그룹 상태를 전역에서 공유해 컨텍스트 전환을 최소화합니다.
- `GroupMembersPanel.vue`: 역할 기반 정렬과 새로고침 버튼으로 Firestore 데이터를 즉시 동기화합니다.
- `MessageComposer.vue` & `MessageHistory.vue`: 입력과 히스토리를 분리해 채팅 UI를 재사용합니다.
- `AppDialog.vue`: 그룹/프로젝트/노트/친구 추가 등 공용 모달 래퍼입니다.
- `src/assets/base.css`, `src/assets/main.css`: Glassmorphism 카드, 토큰 기반 테마를 정의하며 `themeStore`가 `documentElement.dataset.theme`을 변경해 즉시 적용합니다.

## 11. 주의 사항 & 향후 개선 아이디어
- **현재 한계**
  - 인증은 Firestore `accounts` + 클라이언트 측 SHA-256 해시만 사용하므로 실서비스에서는 Firebase Authentication 등 안전한 인증이 필요합니다.
  - 자동화된 테스트가 없어 핵심 흐름(회원가입 → 그룹 → 프로젝트/태스크 → 메시지 → 프로필)을 수동 QA로 검증해야 합니다.
  - Firestore/Storage 사용 시 요금 및 보안 규칙을 반드시 점검합니다.
- **향후 개선 아이디어**
  - Firebase Authentication 도입, 실시간 알림(Push), 태스크/메시지 검색
  - 오프라인 캐싱과 접근성(A11y) 개선
  - 메시지 읽음 상태, 파일 업로드 진행률 표시

---
📎 이 문서는 학습·데모·협업 도구 설계를 위한 참고 자료입니다. 기능 확장 시 Firestore 인덱스, 데이터 스키마, UI 가이드라인을 README에 계속 기록하는 것을 권장합니다.
