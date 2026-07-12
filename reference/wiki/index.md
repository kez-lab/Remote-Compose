---
title: Remote Compose Wiki Index
type: index
created: 2026-07-10
updated: 2026-07-13
---

# Remote Compose Wiki

## 핵심

- [개요와 결론](overview.md) — 기술 경계, 현재 성숙도, 권장 방향
- [AndroidX Remote Compose](androidx-remote-compose.md) — 문서 생성·와이어 포맷·플레이어·아티팩트
- [Ktor와 RcScope 서버 중심 SDUI 학습 경로](ktor-rcscope-codelab-path.md) — HTML 코드랩의 기준 경로; 서버 UI 작성, Header·ByteArray, procedural state, hostAction, Android business 경계를 한 흐름으로 고정
- [Android Compose 생성 frontend 심화](official-api-learning-path.md) — Android/CI capture를 선택할 때 배우는 `RemoteText`, remote value, public authoring API
- [API 문법과 예제 감사](api-syntax-audit.md) — `.rs`·`.rb`·`.ri`, `valueChange`, suspend capture, public/procedural 예제를 alpha14 AAR와 고정 source로 재검증한 결과
- [Frontend, Remote Value, State 실행 모델](remote-state-and-values.md) — 선택 심화; Android Compose capture의 `RemoteText`·`.rs/.rb`와 서버 `RcScope.Text`의 차이
- [Document Anatomy와 State Lifecycle](document-anatomy-and-state.md) — Header, RootLayout, flat operation, runtime state ID, StateLayout 평가 과정
- [생태계 비교](ecosystem-map.md) — Jetpack Compose, CMP, Remote Compose, custom SDUI 비교

## 설계

- [Ktor 전송 계층](ktor-transport.md) — API 계약, 캐시, 인증, 실시간 업데이트
- [CMP 통합 전략](cmp-strategy.md) — cross-platform 계약과 renderer 분리
- [권장 레퍼런스 아키텍처](reference-architecture.md) — contract-first dual renderer 구조
- [보안과 신뢰성](security-reliability.md) — 위협 모델, 제한, fallback, 정책

## 실행과 운영

- [Remote Compose 처음 시작하기 HTML 실습](../../codelab/index.html) — Ktor/JVM → RcScope → ByteArray → Android Player 한 경로로 배우는 10단계 서버 중심 SDUI 입문 과정
- [Remote Compose POC 회고와 학습 포인트](remote-compose-poc-retrospective.md) — Android Compose API 대응표, 상태 소유권, density·state 트러블슈팅, hostAction→Ktor 호출 경로, 기존 앱 도입 체크리스트
- [테스트와 운영](testing-and-operations.md) — 호환성, golden, fuzz, 접근성, 관측성
- [파일럿 계획](pilot-plan.md) — 단계별 PoC와 go/no-go 기준
- [Ktor → Android SDUI POC](sample-sdui-poc.md) — native 직접 입력, server-backed 가변 목록, Remote 화면 전환, onNamedAction→router→Ktor API, 자동 reload
- [alpha14 디버깅과 컴포넌트 이슈](alpha14-debugging-and-component-issues.md) — 실패 타임라인, 문제 operation, workaround, 다음 alpha 회귀 테스트
- [열린 질문](questions.md) — 제품·플랫폼 선택에 필요한 미결정 사항
- [변경 로그](log.md) — append-only 연구 기록

## 원자료

- [alpha14 API 문법 감사 2026-07-12](../raw/remote-compose-api-syntax-audit-2026-07-12.md) — 배포 AAR bytecode, pinned signature, Android API reference 모순과 Codelab 정정 근거
- [Document 구조와 state 근거 2026-07-12](../raw/remote-compose-document-anatomy-2026-07-12.md) — Header property, RootLayout nesting, parse/inflate, runtime state source 분석
- [공식 근거 스냅샷 2026-07-12](../raw/androidx-remote-compose-official-2026-07-12.md) — alpha14 release·API signature·고정 source·sample·public/restricted 경계 재검증
- [공식 소스 스냅샷 2026-07-10](../raw/official-sources-2026-07-10.md)
- [SDUI POC 증거 2026-07-11](../raw/remote-sdui-poc-2026-07-11.md)
- [SDUI emulator 검증 2026-07-11](../raw/remote-sdui-emulator-verification-2026-07-11.md)
- [alpha14 전체 디버깅 증거 2026-07-11](../raw/compose-remote-alpha14-debugging-2026-07-11.md)
- [한국어 체크리스트 UX 검증 2026-07-11](../raw/remote-checklist-ux-verification-2026-07-11.md)
- [직접 입력·가변 목록·상세 화면 검증 2026-07-11](../raw/remote-dynamic-task-flow-verification-2026-07-11.md)
