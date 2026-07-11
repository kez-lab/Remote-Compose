---
title: Open Questions
type: question
created: 2026-07-10
updated: 2026-07-11
as_of: 2026-07-11
---

# 열린 질문

## Android 앱 내부 SDUI 범위

- 첫 production 후보 화면은 무엇이며 critical path가 아닌가?
- 서버가 바꿀 수 있는 범위는 content, component 순서, layout, local action 중 어디까지인가?
- offline cold start에서 bundled fallback과 last-known-good 중 무엇을 우선하는가?
- native Jetpack Compose renderer 대비 Remote Compose가 주는 측정 가능한 이점은 무엇인가?

## API 안정성

- alpha14의 `RemoteDocumentPlayer`와 `RemoteComposePlayer`를 대체할 supported public app embedding API가 제공되는가?
- JVM `createRcBuffer` procedural DSL이 public contract로 전환되는가?
- producer/player wire compatibility와 downgrade 범위는 어디까지 보장되는가?
- document refresh 전후 local state snapshot/restore API가 제공되는가?
- JVM host action이 여러 state 값을 typed payload로 전달할 수 있게 되는가?
- Remote document 안에서 IME text input을 받을 supported component가 제공되는가?
- 큰 server dataset을 위한 lazy/virtualized list 계약이 제공되는가?

## alpha14 재현 이슈

- `TextLookupInt` stale display가 Compose capture API에서도 재현되는가, procedural JVM DSL에만 한정되는가?
- `TextFromFloat` stale display의 최소 재현 문서는 무엇인가?
- derived expression 기반 `StateLayout.stateIndex`가 갱신되지 않는 정확한 조건은 무엇인가?
- nested `StateLayout`이 wrap measurement에 기여하지 않는 동작은 의도된 계약인가?
- 다음 alpha에서 위 네 문제를 제거할 수 있는가?

현재 재현 기록은 [alpha14 디버깅과 컴포넌트 이슈](alpha14-debugging-and-component-issues.md)에 있다.

## 계약과 운영

- UI contract owner와 review/approval workflow는?
- server 운영자가 발행 가능한 node/action 범위는?
- N/N-1 app/player를 얼마 동안 지원하는가?
- document rollback과 kill switch 책임자는?
- cache에 개인정보가 포함될 수 있는가?
- TLS 외에 signed document envelope가 필요한 threat model인가?

## 품질

- payload, decoded image memory, first render, jank 예산은?
- font scale, RTL, locale, screen reader의 release gate는?
- state 값과 rendered text/semantics 일치를 어떻게 자동 검증하는가?
- custom SDUI renderer와의 비교 기준과 승인자는 누구인가?

## 갱신 트리거

- Remote Compose 새 alpha/beta/RC/stable
- public player 또는 JVM builder restriction 변경
- wire compatibility 문서 공개
- 재현 component issue 수정 commit 확인
- Ktor/CMP/Kotlin major upgrade
