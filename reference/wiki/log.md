---
title: Remote Compose Wiki Log
type: log
created: 2026-07-10
updated: 2026-07-12
---

# 변경 로그

## [2026-07-10] schema | 연구 위키 초기화

- Source: 빈 저장소와 공식 Android/Kotlin/Ktor 자료
- Action: `reference/raw`와 `reference/wiki` 구조, 문서 규칙, 인덱스를 생성했다.
- Updated: `AGENTS.md`, `reference/README.md`, `reference/raw/README.md`, `reference/wiki/index.md`

## [2026-07-10] ingest | Remote Compose 생태계 전문 조사

- Source: `reference/raw/official-sources-2026-07-10.md`
- Action: AndroidX Remote Compose, Jetpack Compose, Glance, Wear, Ktor, CMP, 보안·정책 자료를 교차 검증했다.
- Key finding: Remote Compose는 Android 중심의 원격 UI 문서 기술이며 CMP나 일반 JSON SDUI와 동일하지 않다. alpha14의 플레이어 공개 범위도 별도 검증이 필요하다.
- Updated: `reference/wiki/*.md`

## [2026-07-11] ingest | 공식 alpha14와 SDUI 글 재검증

- Source: `reference/raw/remote-sdui-poc-2026-07-11.md`
- Action: AndroidX release notes, ProAndroidDev 글, alpha14 고정 source를 대조해 snapshot-era 설명과 현재 API 제한을 분리했다.
- Updated: `reference/raw/remote-sdui-poc-2026-07-11.md`, `reference/wiki/androidx-remote-compose.md`, `reference/wiki/questions.md`.

## [2026-07-11] implementation | Ktor to Android Remote Compose SDUI POC

- Source: `samples/remote-state-lab`
- Action: 기존 Glance/Wear 샘플을 제거하고, JVM document builder, Ktor document/action API, Android Ktor client, restricted embedded player, 복합 local state와 host action allowlist를 구현했다.
- Updated: `samples/remote-state-lab/server`, `samples/remote-state-lab/app`, `samples/remote-state-lab/README.md`, `reference/wiki/sample-sdui-poc.md`.

## [2026-07-11] verification | SDUI POC build and HTTP round trip

- Source: `reference/raw/remote-sdui-poc-2026-07-11.md`
- Action: server/app unit tests, debug APK, lint, server distribution, health/document/action endpoints와 revision별 document hash 변화를 검증했다.
- Updated: `reference/wiki/pilot-plan.md`, `reference/wiki/sample-sdui-poc.md`, `reference/wiki/index.md`.

## [2026-07-11] fix | Remote font scale and interactive state feedback

- Source: `reference/raw/remote-sdui-emulator-verification-2026-07-11.md`
- Action: document density behavior와 font size를 보정하고, alpha14에서 갱신되지 않던 `TextLookupInt`/파생 text 표시를 직접 state 기반 `StateLayout` label 및 가격 매트릭스로 교체했다.
- Verification: emulator에서 `3 → 4 members`, `$48 → $64`, Pro 선택 `$112`, review 상태 보존을 확인했고 server/app tests, APK assemble, lint를 통과했다.
- Updated: `samples/remote-state-lab`, `reference/wiki/sample-sdui-poc.md`, `reference/wiki/index.md`.

## [2026-07-11] maintenance | Android app embedded SDUI로 위키 범위 교정

- Source: `reference/raw/compose-remote-alpha14-debugging-2026-07-11.md`
- Action: 현재 synthesis에서 다른 Android surface 관련 가정과 선택 지침을 제거하고 Ktor/JVM producer → Android app embedded player 범위로 재작성했다. 과거 raw와 append-only log의 언급은 역사적 조사 기록이며 현재 결론이 아님을 명시했다.
- Updated: `AGENTS.md`, `reference/README.md`, `reference/wiki/overview.md`, `reference/wiki/androidx-remote-compose.md`, `reference/wiki/ecosystem-map.md`, `reference/wiki/cmp-strategy.md`, `reference/wiki/reference-architecture.md`, `reference/wiki/pilot-plan.md`, `reference/wiki/testing-and-operations.md`, `reference/wiki/questions.md`, `reference/wiki/index.md`.

## [2026-07-11] verification | alpha14 디버깅과 문제 컴포넌트 기록

- Source: `reference/raw/compose-remote-alpha14-debugging-2026-07-11.md`
- Action: density, click routing, remote state, dynamic text, nested/derived `StateLayout`, expression limit, server port 충돌의 디버깅 과정을 보존하고 source-confirmed restriction과 emulator-observed issue를 분리했다.
- Updated: `reference/wiki/alpha14-debugging-and-component-issues.md`, `reference/wiki/sample-sdui-poc.md`, `reference/wiki/index.md`.

## [2026-07-11] implementation | 한국어 풀스크린 릴리즈 체크리스트

- Source: `samples/remote-state-lab`, `reference/raw/remote-checklist-ux-verification-2026-07-11.md`
- Action: native 서버 연결 화면 뒤에 풀스크린 Remote Compose 체크리스트를 배치하고, 4비트 direct state로 목록 추가·삭제·초기화, Android allowlist를 통한 Ktor sync를 구현했다. alpha14 `RcSp`/exact size와 density-scaled padding 불일치를 `density()` expression으로 보정했다.
- Verification: `emulator-5554`에서 연결, `3 → 4 → 3` 목록 상태, `R1 → R2` sync, back 복귀를 확인했고 server/app tests, APK assemble, lint를 통과했다.
- Updated: `samples/remote-state-lab`, `reference/raw/remote-checklist-ux-verification-2026-07-11.md`, `reference/wiki/sample-sdui-poc.md`, `reference/wiki/alpha14-debugging-and-component-issues.md`, `reference/wiki/index.md`.

## [2026-07-11] synthesis | Remote Compose POC 발표용 최종 회고

- Source: `reference/raw/official-sources-2026-07-10.md`, `reference/raw/compose-remote-alpha14-debugging-2026-07-11.md`, `reference/raw/remote-checklist-ux-verification-2026-07-11.md`
- Action: POC의 출발 질문, 기술 경계, 최종 구조, 상태/API 경계, alpha14 디버깅 교훈, 검증 범위, 학습 주제, 10분 데모 순서와 예상 Q&A를 하나의 발표용 글로 종합했다.
- Updated: `reference/wiki/remote-compose-poc-retrospective.md`, `reference/wiki/index.md`, `reference/wiki/overview.md`, `reference/wiki/log.md`.

## [2026-07-11] implementation | 직접 입력과 server-backed 가변 task flow

- Source: `samples/remote-state-lab`, `reference/raw/remote-dynamic-task-flow-verification-2026-07-11.md`
- Action: 고정 4비트/16-state fixture와 수동 sync 버튼을 제거했다. Remote create action이 native TextField를 열고 Ktor `TaskStore`에 저장하며, task 수만큼 Remote Compose row/detail을 생성한다. `verticalScroll`, direct `StateLayout` 상세 전환, ID 검증 delete action, mutation 후 자동 document reload를 구현했다.
- Verification: emulator에서 직접 입력 `R1·3 → R2·4`, 12개 목록 scroll, 입력 task 상세, 상세 삭제 `R10·12 → R11·11`을 확인했고 server/app tests, APK assemble, lint를 통과했다.
- Updated: `samples/remote-state-lab`, `reference/raw/remote-dynamic-task-flow-verification-2026-07-11.md`, `reference/wiki/sample-sdui-poc.md`, `reference/wiki/alpha14-debugging-and-component-issues.md`, `reference/wiki/questions.md`, `reference/wiki/remote-compose-poc-retrospective.md`, `reference/wiki/index.md`.

## [2026-07-11] synthesis | Android 개발자 관점 POC 회고 보강

- Source: `samples/remote-state-lab/server/src/main/kotlin/com/example/remotestatelab/server/ChecklistDocument.kt`, `reference/raw/compose-remote-alpha14-debugging-2026-07-11.md`, `reference/raw/remote-dynamic-task-flow-verification-2026-07-11.md`
- Action: 일반 기술 비교 섹션을 제거하고 Jetpack Compose와 Remote Compose 기본 UI API 대응표, 실제 `screen` 상태 흐름, `scaledSp`가 필요해진 증상부터 source 확인·실패한 시도·최종 workaround까지의 트러블슈팅을 추가했다. 초기 고정 목록 구현은 Android 개발자가 이해하기 쉬운 요구와 한계 중심으로 다시 설명했다.
- Updated: `reference/wiki/remote-compose-poc-retrospective.md`, `reference/wiki/index.md`, `reference/wiki/log.md`.

## [2026-07-11] synthesis | 기존 Android 앱 도입 학습 가이드 추가

- Source: `reference/wiki/reference-architecture.md`, `reference/wiki/security-reliability.md`, `reference/wiki/testing-and-operations.md`, `reference/wiki/questions.md`, `samples/remote-state-lab`
- Action: 최종 판단 전에 기존 NavGraph, ViewModel, Repository, domain use case를 유지하면서 Remote Compose player를 adapter로 격리하는 권장 구조를 추가했다. 도입 화면, API·문서 호환성, 상태 소유권, Navigation, host action, offline, 보안, 접근성, 성능, 테스트, rollout에서 공부하고 검증할 항목과 단계별 도입 순서를 정리했다.
- Updated: `reference/wiki/remote-compose-poc-retrospective.md`, `reference/wiki/index.md`, `reference/wiki/log.md`.

## [2026-07-11] implementation | Ktor server standalone Gradle build

- Source: `samples/remote-state-lab/server`, `samples/remote-state-lab/settings.gradle.kts`
- Action: server를 독립 settings, wrapper, version catalog를 가진 standalone Gradle build로 만들고 상위 Android project에서는 composite build로 포함했다. `server/`의 `./gradlew build`와 상위의 `./gradlew :server:build`를 모두 지원한다.
- Verification: server 디렉터리의 `./gradlew clean build --no-daemon`과 상위의 `./gradlew :server:clean :server:build :app:testDebugUnitTest --no-daemon`이 성공했다.
- Updated: `samples/remote-state-lab/server`, `samples/remote-state-lab/settings.gradle.kts`, `samples/remote-state-lab/README.md`, `reference/wiki/log.md`.

## [2026-07-11] implementation | Context parameter 기반 Remote size DSL

- Source: `samples/remote-state-lab/server/src/main/kotlin/com/example/remotestatelab/server/ChecklistDocument.kt`, Kotlin 2.3.20 context parameters
- Action: 반복하던 density 인자를 제거하고 `RcScope` context에서 `18f.scaledSp`, `76f.scaledSize`로 표현하는 extension property를 추가했다. 사용되지 않던 `ActionButton`의 raw `18.rsp` 기본값도 제거해 모든 버튼이 보정된 font token을 명시하도록 했다.
- Verification: standalone server의 `./gradlew clean build --no-daemon`과 상위 composite의 server clean build, app unit test, debug APK assemble이 성공했다.
- Updated: `samples/remote-state-lab/server`, `samples/remote-state-lab/README.md`, `reference/wiki/remote-compose-poc-retrospective.md`, `reference/wiki/log.md`.

## [2026-07-11] implementation | ChecklistMetrics 제거와 local size 선언

- Source: `samples/remote-state-lab/server/src/main/kotlin/com/example/remotestatelab/server/ChecklistDocument.kt`
- Action: 작은 POC에서 값의 의미를 사용 위치와 분리하던 `ChecklistMetrics`를 제거했다. 각 화면이 `RcFloat` density context 안에서 `18.scaledSp`, `76.scaledSize`처럼 font와 component size를 직접 선언하도록 단순화했다. `RcScope` 전체를 context로 사용한 첫 시도는 implicit DSL receiver shadowing으로 컴파일되지 않아 필요한 density 값만 context로 좁혔다.
- Verification: standalone server clean build와 상위 composite의 server clean build, app unit test, debug APK assemble이 성공했다.
- Updated: `samples/remote-state-lab/server`, `samples/remote-state-lab/README.md`, `reference/wiki/remote-compose-poc-retrospective.md`, `reference/wiki/log.md`.

## [2026-07-11] maintenance | scaled size DSL과 hostAction API 경로 문서 동기화

- Source: `samples/remote-state-lab/server/src/main/kotlin/com/example/remotestatelab/server/ChecklistDocument.kt`, `samples/remote-state-lab/app/src/main/java/com/example/remotestatelab/remote/RemoteSduiScreen.kt`, `RemoteSduiViewModel.kt`, `HostActionRouter.kt`
- Action: 모든 현재 문서의 density helper를 `RcFloat` context 기반 `18.scaledSp`/`76.scaledSize` 구현으로 맞췄다. `hostAction`을 막연한 intent로 표현하던 설명을 player `onNamedAction` callback, Android router, ViewModel coroutine, Ktor mutation과 document reload의 실제 호출 경로로 교체하고 create와 delete의 실행 시점 차이를 명시했다.
- Updated: `samples/remote-state-lab/README.md`, `reference/wiki/sample-sdui-poc.md`, `reference/wiki/remote-compose-poc-retrospective.md`, `reference/wiki/overview.md`, `reference/wiki/androidx-remote-compose.md`, `reference/wiki/index.md`, `reference/wiki/log.md`.

## [2026-07-12] implementation | Android 개발자용 Remote Compose HTML Codelab

- Source: `samples/remote-state-lab`, `reference/wiki/remote-compose-poc-retrospective.md`, Android 공식 Codelab 정보 구조
- Action: 처음 Remote Compose를 접하는 Android 개발자가 실행 모델, standalone Ktor producer, binary document, Android player, StateLayout, hostAction API 경계, density 디버깅, production gate를 순서대로 학습하는 9단계 정적 HTML 사이트를 만들었다. 단계 레일, 진행률, 완료·체크 상태 저장, 코드 복사, light/dark theme, 반응형 mobile drawer를 구현했다.
- Updated: `codelab/`, `README.md`, `reference/wiki/index.md`, `reference/wiki/log.md`.

## [2026-07-12] ingest | alpha14 공식 API 전체 근거와 학습 경계 재정리

- Source: AndroidX Remote Compose release notes, Google Maven metadata, Android Developers API reference, alpha14 pinned `api/current.txt`, implementation source, integration demos, wire-format documentation
- Action: `remote-creation-compose` public API, restricted player/procedural DSL, 저장소 POC custom code를 분리해 새 immutable source snapshot과 공식 API 학습 경로를 만들었다. `Int.rsp`와 `RemoteDensityBehavior.Dp`가 공식 density 경로이고 `scaledSp`는 restricted procedural POC workaround라는 점을 명시했다. Android CLI 문서 검색에서는 Remote Compose 일반 API가 색인되지 않아 release/API/source를 우선 근거로 기록했다.
- Updated: `reference/raw/androidx-remote-compose-official-2026-07-12.md`, `reference/raw/README.md`, `reference/wiki/official-api-learning-path.md`, `reference/wiki/androidx-remote-compose.md`, `reference/wiki/overview.md`, `reference/wiki/remote-compose-poc-retrospective.md`, `reference/wiki/index.md`, `reference/wiki/log.md`.

## [2026-07-12] implementation | 공식 API 기반 Remote Compose HTML Codelab 재구축

- Source: `reference/raw/androidx-remote-compose-official-2026-07-12.md`, `reference/wiki/official-api-learning-path.md`, Android 공식 Codelab 정보 구조
- Action: 기존 POC 중심 9단계 사이트를 공식 public API 중심 10단계 Codelab으로 교체했다. 모든 code block을 public/source/restricted/custom으로 분류하고, public Compose 작성·preview·capture·state·StateLayout·hostAction·density 순서 뒤에 restricted Ktor/player POC를 배치했다. 깨지기 쉬운 theme UI를 제거하고 button reset, 명확한 Material 계열 navigation, mobile drawer, keyboard focus, code selection fallback을 다시 구현했다.
- Updated: `codelab/`, `README.md`, `reference/wiki/index.md`, `reference/wiki/log.md`.

## [2026-07-12] ingest | Document Header, RootLayout, runtime state lifecycle 보강

- Source: alpha14 pinned `Document Structure`, `Header.java`, `RootLayoutComponent.java`, `CoreDocument.java`, `RemoteComposeBuffer.java`, `RemoteComposeState.java`, public `RemoteState.kt`, `RemoteStateLayout.kt`, `ValueChange.kt`
- Action: Header의 protocol/version/property framing, RootLayout nesting, flat operation parse/inflate, creation state와 runtime state의 차이, mutable/named/expression state의 ID 할당, listener dirty/repaint, StateLayout의 index selector 동작을 source-backed 문서로 추가했다. 별도 `RootState` operation은 없다는 용어 경계와 document replacement 시 public 자동 restore 계약을 확인하지 못했다는 점을 명시했다.
- Updated: `reference/raw/remote-compose-document-anatomy-2026-07-12.md`, `reference/raw/README.md`, `reference/wiki/document-anatomy-and-state.md`, `reference/wiki/official-api-learning-path.md`, `reference/wiki/androidx-remote-compose.md`, `reference/wiki/overview.md`, `reference/wiki/index.md`, `reference/wiki/log.md`.

## [2026-07-12] implementation | Codelab document-first 12단계 확장

- Source: `reference/wiki/document-anatomy-and-state.md`, AndroidX alpha14 pinned source
- Action: 기존 API 사용 순서 앞에 Document Anatomy와 Runtime State 두 단계를 추가했다. Header field table, operation stack, RootLayout nesting tree, parse/inflate 5단계, root/creation/runtime state 비교, ID listener update flow, document refresh state restore 경고를 시각적으로 학습하도록 구성했다.
- Updated: `codelab/index.html`, `codelab/styles.css`, `codelab/app.js`, `codelab/README.md`, `reference/wiki/index.md`, `reference/wiki/log.md`.

## [2026-07-12] implementation | SDUI 왕초보 관점의 HTML 실습 전면 재구성

- Source: `reference/raw/androidx-remote-compose-official-2026-07-12.md`, `reference/wiki/official-api-learning-path.md`, `samples/remote-state-lab/raw/verification/2026-07-11-dynamic-task-flow`
- Action: Header·RootLayout·runtime ID를 선행 지식으로 요구하던 12단계 구성을 제거했다. 실제 완성 화면 → SDUI와 일반 앱 비교 → sample 실행 → public Remote UI → document bytes → local state → 문서 내부 화면 전환 → hostAction과 ViewModel/API 경계 → Ktor bytes transport 순서의 왕초보용 10단계 과정으로 다시 구성했다. Ktor는 공식 Remote Compose 통합이 아닌 저장소 POC이고 document internals는 심화 과정임을 명시했다.
- Updated: `codelab/index.html`, `codelab/styles.css`, `codelab/app.js`, `codelab/README.md`, `reference/wiki/official-api-learning-path.md`, `reference/wiki/index.md`, `reference/wiki/log.md`

## [2026-07-12] lint | alpha14 API 문법과 Codelab 예제 전수 감사

- Source: alpha14 pinned `api/current.txt`, `RemoteBoolean.kt`, `RemoteString.kt`, `ValueChange.kt`, `RemoteStateLayout.kt`, Android Developers API reference, Google Maven `remote-creation-compose:1.0.0-alpha14` AAR bytecode
- Action: `.rs`와 `.rb`는 질문의 parameter boundary에서 실제로 필요하고 소문자 `valueChange`가 alpha14 artifact와 일치함을 확인했다. 동시에 suspend capture 문맥 누락, public `RemoteStateLayout`과 procedural POC `StateLayout` 혼합, `rsp`의 creation-time density 경계 누락, helper 생략 예제의 분류 부족, 잘못된 `RemoteText` 링크, anatomy-first 학습 문구를 찾아 정정했다. Android API reference의 대문자 `ValueChange`와 alpha14 artifact의 소문자 `valueChange` 모순을 보존했다.
- Updated: `reference/raw/remote-compose-api-syntax-audit-2026-07-12.md`, `reference/raw/README.md`, `reference/wiki/api-syntax-audit.md`, `reference/wiki/official-api-learning-path.md`, `reference/wiki/androidx-remote-compose.md`, `reference/wiki/document-anatomy-and-state.md`, `reference/wiki/remote-compose-poc-retrospective.md`, `reference/wiki/questions.md`, `reference/wiki/index.md`, `codelab/index.html`, `codelab/README.md`, `reference/wiki/log.md`

## [2026-07-12] maintenance | 상태 실행 모델과 두 frontend 초보자 설명 보강

- Source: alpha14 pinned `RcScope.kt`, `RemoteStateLayout.kt`, `RemoteBoolean.kt`, 배포 AAR signature, 실제 `ChecklistDocument.kt`
- Action: 문법 존재 여부만 확인하고 remote value의 목적과 실행 시점을 설명하지 않은 문서 실패를 기록했다. public Compose `RemoteText`와 restricted server `RcScope.Text`가 같은 core document를 만드는 서로 다른 frontend임을 artifact·receiver·type·공개 범위로 비교했다. Kotlin value → remote value → document operation → player state map 흐름, Compose recomposition과 player ID/listener 평가 차이, 토글의 capture 결과, interactive playback lab, 상태 소유권 표를 추가했다. 재발 방지 기준을 `AGENTS.md`에 고정했다.
- Updated: `AGENTS.md`, `codelab/index.html`, `codelab/styles.css`, `codelab/app.js`, `reference/wiki/remote-state-and-values.md`, `reference/wiki/api-syntax-audit.md`, `reference/wiki/index.md`, `reference/wiki/log.md`

## [2026-07-13] maintenance | HTML 코드랩을 Ktor RcScope 서버 SDUI 단일 경로로 정리

- Source: `samples/remote-state-lab/server/ChecklistDocument.kt`, `Server.kt`, alpha14 pinned procedural/player source, 기존 API 문법 감사
- Action: public Compose frontend를 공식이라는 이유만으로 beginner 본문에 먼저 배치해 실제 POC 경로를 흐리던 구조를 정정했다. 본편의 UI, Header, state, 화면 전환, action 예제를 `RcScope.Text`, `createRcBuffer`, `remoteNamedInteger`, `StateLayout`, `setValue`, `hostAction(String)`으로 통일했다. `RemoteText`, `.rs/.rb`, `valueChange`, Android capture는 배포 모델이 다른 선택 심화 과정으로 격리했다.
- Updated: `AGENTS.md`, `codelab/`, `reference/wiki/ktor-rcscope-codelab-path.md`, `reference/wiki/remote-state-and-values.md`, `reference/wiki/official-api-learning-path.md`, `reference/wiki/api-syntax-audit.md`, `reference/wiki/index.md`, `reference/wiki/log.md`.

## [2026-07-13] lint | Remote Compose 문서와 코드 예제 전문 감사

- Source: AndroidX alpha14 release/API, 배포 source jars, pinned commit `19660b9e1b2fec4a9528fe80ce0a432c0fa2f825`, 실제 sample source
- Action: 코드랩 명칭, automatic root, procedural StateLayout의 layout-child 요구, RemoteDocumentPlayer 필수 dimensions, 기본/host RemoteDensity 경계를 재검증했다. 5개 설명 오류를 수정하고 public Compose compile fixture와 link/API-boundary 감사 스크립트를 추가했다.
- Verification: `scripts/audit_docs.py`, `:docs-api-fixture:compileDebugKotlin`, app/server build를 배포 gate에 포함했다.
- Updated: `README.md`, `codelab/`, `samples/remote-state-lab`, `reference/raw/remote-compose-documentation-audit-2026-07-13.md`, `reference/wiki/documentation-audit.md`, affected wiki pages, `reference/wiki/index.md`, `reference/wiki/log.md`.

## [2026-07-13] deployment | GitHub Pages Codelab workflow

- Source: GitHub Pages custom workflow documentation
- Action: `configure-pages@v5`, `upload-pages-artifact@v4`, `deploy-pages@v4`로 Codelab, reference, verification screenshots를 정적 artifact로 배포하는 workflow를 추가했다. root URL은 `/codelab/`로 이동한다.
- Updated: `.github/workflows/pages.yml`, `site/index.html`, `README.md`, `reference/wiki/log.md`.
