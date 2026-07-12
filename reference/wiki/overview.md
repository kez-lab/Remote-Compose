---
title: Remote Compose Overview
type: synthesis
created: 2026-07-10
updated: 2026-07-13
as_of: 2026-07-12
confidence: high
sources:
  - ../raw/androidx-remote-compose-official-2026-07-12.md
  - ../raw/remote-sdui-poc-2026-07-11.md
  - ../raw/compose-remote-alpha14-debugging-2026-07-11.md
---

# Remote Compose 개요와 결론

## 이 저장소의 범위

이 저장소는 **Ktor/JVM이 Remote Compose binary document를 제공하고 Android 앱 내부 embedded player가 화면을 그리는 SDUI**를 연구한다.

다른 Android surface와의 통합은 현재 범위가 아니다. 과거 raw source와 append-only log에 남아 있는 조사는 현재 아키텍처 결론으로 사용하지 않는다.

## 기술 경계

AndroidX Remote Compose는 UI를 binary operation document로 기록하고 Android player가 layout, drawing, state, animation, action을 평가하는 프레임워크다.

- Jetpack Compose: Android 앱 프로세스에서 컴파일된 Kotlin UI를 실행한다.
- Compose Multiplatform: 동일한 Kotlin UI 소스를 target별 앱으로 컴파일한다.
- Custom SDUI: 제품 소유 JSON/CBOR/ProtoBuf 계약을 앱 renderer가 해석한다.
- Remote Compose SDUI: AndroidX operation document를 Android player가 해석한다.
- Ktor: document와 action을 전달하는 transport다. UI engine이 아니다.

## 현재 상태

| 항목 | 2026-07-12 판단 |
|---|---|
| Remote Compose | `1.0.0-alpha14`, stable/beta 없음 |
| JVM producer | 가능하지만 procedural DSL과 `createRcBuffer`가 restricted |
| Android app player | artifact는 있으나 핵심 Compose/View player가 restricted |
| POC | fetch, parse, render, local state, host action, emulator touch까지 확인 |
| 제품 채택 | public supported player와 compatibility 계약 전에는 No-go |

## 실제 흐름

1. Ktor server module이 allowlist된 Remote Compose operation으로 문서를 생성한다.
2. Android 앱이 binary document를 다운로드한다.
3. 앱이 size와 parser limit을 검증하고 last-known-good를 유지한다.
4. embedded player가 document-local state와 layout을 평가한다.
5. local action은 player 안에서 처리한다.
6. named host action은 player의 `onNamedAction` callback으로 올라오고, Android router가 typed command로 검증한 뒤 ViewModel이 Ktor API를 호출한다.

## 이번 POC가 보여 준 난점

- public Compose API의 `RemoteTextUnit`/`rsp` 경로와 restricted procedural `RcSp` 경로를 섞으면 density 결론을 잘못 일반화할 수 있다. `scaledSp`는 후자에서 만든 POC workaround다.
- density metadata가 빠지면 font와 spacing이 pixel-small하게 보인다.
- click action 성공과 동적 text 재표시는 별개다.
- alpha14 POC에서는 `TextLookupInt`, `TextFromFloat`, derived `StateLayout` index가 stale display를 만들었다.
- 중첩 `StateLayout`은 명시적 bounds 없이 layout을 깨뜨릴 수 있었다.
- 복잡한 expression은 operation 길이 제한을 넘을 수 있다.
- player와 JVM builder가 restricted라 API churn을 감수해야 한다.

자세한 내용은 [alpha14 디버깅과 컴포넌트 이슈](alpha14-debugging-and-component-issues.md)에 있다.

## 권장 결론

### Android-only 실험

Remote Compose와 custom SDUI + Jetpack Compose renderer를 동일 화면으로 비교한다. 기능 가능성보다 다음을 측정한다.

- public API 의존성
- state/action/render correctness
- 접근성·font scale·RTL
- document size와 first render
- fallback·rollback·debug 비용
- N/N-1 player compatibility

### Android+iOS/desktop 공통 제품

Remote Compose binary를 공통 UI 계약으로 사용하지 않는다. Ktor가 제품 소유 `UiDocument`를 전달하고 CMP/native renderer가 해석하도록 한다. Android Remote Compose adapter는 별도 실험 module에 격리한다.

## 주요 위험

- alpha API와 wire format 변화
- producer/player/profile 불일치
- restricted API에 기반한 제품 배포
- 외부 document의 CPU·메모리·image 공격
- host action 권한 오용
- stale/partial rollout에서 빈 화면
- 값 변경과 visual feedback의 불일치
- semantics, font scale, RTL, localization 누락

## 다음 읽을 문서

- [Ktor와 RcScope 서버 중심 코드랩 경로](ktor-rcscope-codelab-path.md)
- [Android Compose 생성 frontend 심화](official-api-learning-path.md)
- [Document Anatomy와 State Lifecycle](document-anatomy-and-state.md)
- [Remote Compose POC 회고와 학습 포인트](remote-compose-poc-retrospective.md)
- [AndroidX Remote Compose](androidx-remote-compose.md)
- [생태계 비교](ecosystem-map.md)
- [alpha14 디버깅과 컴포넌트 이슈](alpha14-debugging-and-component-issues.md)
- [Ktor → Android SDUI POC](sample-sdui-poc.md)
- [보안과 신뢰성](security-reliability.md)
