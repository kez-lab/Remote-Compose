---
title: Remote Compose Korean checklist UX verification
type: evidence
created: 2026-07-11
as_of: 2026-07-11
---

# Remote Compose Korean checklist UX verification

## 범위

- sample: `samples/remote-state-lab`
- AndroidX Remote Compose: `1.0.0-alpha14`
- device: `emulator-5554`, `1080 × 2400`
- server: JVM Ktor `http://0.0.0.0:8080`
- client URL: `http://10.0.2.2:8080`

## 구현 결과

- 앱 시작 시 네이티브 서버 연결 화면을 표시한다.
- 연결 성공 후 app chrome 없이 Remote Compose 문서가 전체 가용 화면을 채운다.
- 4개 release task를 `items_mask` 4비트 state로 표현하고 16개 direct `StateLayout` state를 생성한다.
- 추가·삭제·초기화는 문서 안에서 실행되며 HTTP 요청을 만들지 않는다.
- `checklist.sync`만 Android allowlist가 `POST /actions/sync`에 매핑하고 새 문서를 요청한다.

## alpha14 density 관찰

첫 렌더에서 DP density header와 padding은 적용됐지만 `RcSp` font와 `height(Float)` fixed dimension은 raw pixel 크기에 가까웠다. 그 결과 글꼴이 작고, density가 적용된 padding이 작은 fixed-height component 내부를 모두 소비해 summary와 row subtitle이 잘렸다.

alpha14 source에서 `TextLayout` font size는 pixel 값으로 기록되고 `DimensionModifierOperation.Type.EXACT`는 density를 곱하지 않는 반면 `PaddingModifierOperation`은 DP behavior에서 density를 곱한다. 샘플은 `density()` remote system value를 사용해 font expression과 fixed dimension을 명시적으로 스케일했다.

이 결론은 alpha14 procedural DSL/player 조합에 대한 source-backed engineering observation이며 향후 API 계약으로 일반화하지 않는다.

## Android journey 결과

| 검증 | 결과 |
|---|---|
| 연결 화면 → 문서 진입 | PASS |
| 초기 `3개 작업` | PASS |
| 추가 후 `4개 작업`과 Crashlytics 행 | PASS |
| Crashlytics 삭제 후 `3개 작업` | PASS |
| server sync 후 `R1 → R2` | PASS |
| back 후 연결 화면 복귀 | PASS |
| server/app tests, APK, lint | PASS |

상세 action과 PNG는 `samples/remote-state-lab/raw/verification/2026-07-11-korean-checklist/README.md`에 있다.
