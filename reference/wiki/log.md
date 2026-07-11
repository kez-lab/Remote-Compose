---
title: Remote Compose Wiki Log
type: log
created: 2026-07-10
updated: 2026-07-11
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
