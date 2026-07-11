---
title: Remote UI Testing and Operations
type: engineering
created: 2026-07-10
updated: 2026-07-11
as_of: 2026-07-11
confidence: high
sources:
  - ../raw/official-sources-2026-07-10.md
---

# 테스트와 운영

원격 UI의 완료 기준은 “샘플이 보인다”가 아니라 producer, protocol, player, action, network, fallback을 하나의 호환성 시스템으로 검증하는 것이다.

## 테스트 피라미드

### 1. Contract unit test

- schema version과 migration
- 필수/선택 field
- unknown field/type 정책
- action allowlist
- design token/asset resolver
- node count/depth/text/resource limit
- locale와 accessibility metadata

### 2. Renderer component test

- 각 component의 정상·빈 값·최댓값
- unsupported capability fallback
- action wiring
- semantics tree
- Remote Compose와 CMP renderer의 semantic parity

### 3. Golden/screenshot

matrix:

- light/dark/high contrast
- LTR/RTL
- 최소·최대 font scale
- 작은/큰 viewport와 density
- locale별 긴 문자열
- loading/error/empty/content state
- Android API/player/profile 버전

pixel-perfect 일치만 보지 않는다. text rasterization 차이가 있는 CMP target은 구조, clipping, overflow, semantics, 주요 geometry threshold를 함께 본다.

### 4. Protocol compatibility

| Producer | Client/player | 기대 |
|---|---|---|
| N | N | 정상 |
| N | N-1 | server downgrade 또는 명시적 fallback |
| N-1 | N | 정상 또는 migration |
| unknown profile | N | 문서 거부 + fallback |
| unknown operation/type | N | crash 금지, 정책대로 거부 |

Remote Compose byte golden은 버전별 fixture로 고정한다. serializer 내부 변화로 byte가 달라도 의미가 같을 수 있으므로 parsed operation/semantic assertion도 병행한다.

### 5. Malformed/fuzz

- 잘린 header와 buffer
- 음수/과대 길이
- unknown operation
- 너무 깊은 container
- 순환/잘못된 reference ID
- 과대 string, bitmap, font, list, shader
- NaN/Infinity와 expression edge
- action payload type mismatch
- 압축 폭탄
- 잘못된 image header/MIME

성공 조건은 timeout/OOM/crash 없이 bounded failure와 fallback이다.

### 6. Network/repository

- offline cold start
- timeout, DNS, TLS, 401/403/404/429/5xx
- partial download
- ETag/304
- 만료·clock skew
- hash mismatch
- cache corruption
- candidate promote 중 process death
- rollback과 kill switch

### 7. End-to-end

- CMS/rule 입력 → server compiler → CDN/Ktor → client validator → renderer → action → server
- experiment cohort와 authorization
- app N/N-1 혼합 fleet
- accessibility service와 실제 입력 장치
- Android Activity/process lifecycle과 document reload

alpha14 POC에서 확인했듯 state value assertion과 rendered text/semantics assertion을 분리한다. click이 `handled=true`여도 visual feedback이 stale할 수 있다.

## Remote Compose 도구

alpha14 생태계에는:

- `remote-tooling-preview`
- `RemoteContentPreview`, `RemoteDocumentPreview`
- `remote-testing`
- source의 screenshot/integration test
- Remote-specific lint

가 존재한다. 다만 alpha API이므로 테스트 인프라도 adapter module에 묶고 업그레이드 시 fixture를 재검토한다.

## 성능 예산

surface별 SLO를 먼저 정한다.

| 지표 | 측정 구간 |
|---|---|
| manifest latency | request start → metadata 수신 |
| payload latency/size | bytes와 압축 전후 크기 |
| validation time | 수신 완료 → candidate 승인 |
| parse time | bytes → runtime document |
| first render | active 선택 → 첫 안정 frame |
| frame time/jank | animation/scroll/action |
| decoded image memory | peak 및 cache |
| fallback rate | 전체 render 중 fallback 비율 |

숫자는 제품·기기군에 맞춰 파일럿에서 정한다. AndroidX 내부 max를 제품 SLO로 사용하지 않는다.

## telemetry

필수 dimension:

- document ID/revision의 비식별 hash
- schema/document API/profile
- renderer type과 version
- app build, OS/API, device class
- validation/parse/render/action/fallback outcome
- fallback reason
- payload size, latency bucket

금지:

- 화면에 포함된 사용자 text 원문
- auth token
- 전체 문서 bytes
- 민감 action payload

## 자동 rollback 신호

- crash/ANR가 baseline 대비 상승
- parse/validation 실패율 급증
- fallback rate threshold 초과
- action success rate 급락
- accessibility critical check 실패
- 특정 player/profile cohort만 실패

rollback은 server 문서 revision을 되돌리는 것과 client가 candidate를 격리하는 두 층 모두 제공한다.

## release checklist

- [ ] 공식 dependency와 player API 공개 범위 재확인
- [ ] version/profile capability matrix 통과
- [ ] malformed input에서 bounded failure
- [ ] last-known-good와 bundled fallback 검증
- [ ] host action allowlist와 server authorization 검증
- [ ] RTL/font scale/locale/semantics 확인
- [ ] offline/timeout/cache corruption 확인
- [ ] canary, kill switch, rollback drill 완료
- [ ] privacy-safe dashboard와 alert 준비
- [ ] 최신 Google Play 정책 검토
