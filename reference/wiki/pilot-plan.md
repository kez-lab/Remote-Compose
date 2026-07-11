---
title: Remote Compose Pilot Plan
type: engineering
created: 2026-07-10
updated: 2026-07-11
as_of: 2026-07-11
confidence: medium-high
sources:
  - ../raw/official-sources-2026-07-10.md
  - ../raw/remote-sdui-poc-2026-07-11.md
---

# 파일럿 계획

## 목표

한 개의 비핵심 surface에서 다음을 증명한다.

- contract가 Android/CMP renderer에서 안전하게 해석된다.
- network와 cache 실패 시 bundled UI로 복구된다.
- action이 allowlist와 authorization을 통과한다.
- Remote Compose를 쓸 경우 target player/profile에서 실제 이점이 있다.

첫 surface는 로그인, 결제, 계정 삭제, 권한 요청, 안전 경고를 피한다.

## Phase 0 — 기술 스파이크

### 2026-07-11 진행 상태

- 완료: Ktor JVM procedural DSL document 생성
- 완료: HTTP binary document/action round trip
- 완료: Android embedded player APK compile
- 완료: document-local 복합 상태와 named host action allowlist
- 완료: emulator render, `+`/plan touch, derived price, review state 검증
- 미완료: public supported player 확인
- 미완료: typed state payload, disk fallback, malformed/fuzz 검증

기술 실행 가능성과 emulator touch는 확인했지만 restricted player/JVM DSL 때문에 Phase 0 제품 Go 조건은 충족하지 않았다. [POC 문서](sample-sdui-poc.md)를 참조한다.

산출물:

- `remote-creation-compose:1.0.0-alpha14`로 preview/capture 성공
- Android 앱 내부 검증 가능한 player에서 render/touch 성공
- player public/restricted API 조사 결과
- 같은 화면의 Jetpack Compose/CMP renderer
- artifact dependency graph와 minSdk/compileSdk 영향

Go:

- 사용하려는 player API가 제품 배포에 허용되는 공식 경로
- 필요한 layout/state/action/semantics가 profile에 존재

No-go:

- restricted API를 reflection/fork로 우회해야 함
- iOS 공통 renderer로 Remote Compose가 필요하다는 전제
- fallback 없이 raw bytes를 직접 표시해야 함

## Phase 1 — Contract vertical slice

최소 node:

- container
- text
- image asset
- primary action
- loading/error/fallback

구현:

- versioned `UiDocument`
- server/client validator
- Ktor GET + ETag
- typed action POST
- bundled fallback과 last-known-good
- CMP renderer
- Android embedded Remote Compose adapter는 비교 실험에만

## Phase 2 — 안전성

- payload/decompressed size limit
- node/depth/text/image/action limit
- hash와 expiry
- asset host/MIME 검증
- action registry와 재인가
- candidate quarantine와 atomic promote
- crash-loop 격리
- kill switch

malformed/fuzz corpus와 network failure matrix를 CI에 넣는다.

## Phase 3 — 품질

- light/dark
- RTL
- 2개 이상의 locale과 긴 문자열
- font scale 최소/최대
- semantics와 screen reader
- 작은/큰 viewport
- low/mid/high device 성능
- player/profile N/N-1 compatibility

Remote Compose와 CMP renderer가 pixel이 아니라 정보·action·semantics에서 같은 결과를 내는지 확인한다.

## Phase 4 — canary

순서:

1. 개발자/QA cohort
2. internal dogfood
3. 작은 production canary
4. 특정 renderer/profile cohort 확대
5. rollback drill 후 점진 확대

관측:

- fetch/validation/parse/render 성공률
- first render와 jank
- payload/bitmap memory
- fallback rate/reason
- action 성공·거부율
- crash/ANR
- 접근성 이슈

## 비교 실험

동일 surface를 두 방식으로 구현한다.

| 항목 | Remote Compose | Custom contract + Compose |
|---|---|---|
| 구현 코드와 binary size | 측정 | 측정 |
| document size | 측정 | 측정 |
| first render/frame | 측정 | 측정 |
| state/action round trip | 측정 | 측정 |
| 접근성 parity | 검증 | 검증 |
| Android 외 재사용 | 불가/adapter 필요 | CMP로 검증 |
| API upgrade 비용 | alpha14→다음 alpha 실험 | schema migration 실험 |
| fallback/debug 비용 | 측정 | 측정 |

기술 선호가 아니라 결과로 선택한다.

## 최종 go/no-go

Go:

- 공식 supported surface/API
- malformed input과 network failure에서 crash 없음
- fallback이 항상 표시됨
- 접근성 critical failure 없음
- version/profile downgrade 가능
- 운영 rollback과 kill switch 입증
- 기존 방식 대비 표현력·성능·운영 중 하나 이상에서 명확한 이점

No-go:

- restricted player API 의존
- server/client lockstep 없이는 깨지는 protocol
- 원격 문서 하나로 critical path 전체가 사라짐
- host action이 임의 URL/class/script 실행
- iOS/CMP 요구를 충족하지 못함
- 관측 불가능하거나 rollback 불가
