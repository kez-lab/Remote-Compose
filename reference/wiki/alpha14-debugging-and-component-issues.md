---
title: Alpha14 Debugging and Component Issues
type: engineering
created: 2026-07-11
updated: 2026-07-11
as_of: 2026-07-11
confidence: high
sources:
  - ../raw/compose-remote-alpha14-debugging-2026-07-11.md
  - ../raw/remote-sdui-emulator-verification-2026-07-11.md
  - ../raw/remote-checklist-ux-verification-2026-07-11.md
---

# alpha14 디버깅과 컴포넌트 이슈

이 페이지는 Android 앱 내부 embedded player POC에서 얻은 문제와 우회책을 다음 구현자가 바로 재현할 수 있게 정리한다. 상세 로그와 source hash는 [raw evidence](../raw/compose-remote-alpha14-debugging-2026-07-11.md)에 있다.

## 결론

`+`가 동작하지 않은 것이 아니었다. click과 `ValueIntegerExpressionChangeActionOperation`은 `seats=3 → 4`를 정상 처리했지만, alpha14 procedural document의 동적 text output이 이전 값을 계속 그려 사용자에게 무반응으로 보였다.

최종 구현은 표시를 `TextLookupInt`/`TextFromFloat`에서 직접 remote integer state 기반 `StateLayout`으로 바꿨다.

## 현재 이슈 레지스터

| 대상 | 상태 | 증거 | 영향 | 현재 우회 |
|---|---|---|---|---|
| 문서 density metadata | integration pitfall | DP header 전후 emulator 비교 | density 계약을 빠뜨리면 padding/spacing이 혼합 좌표계로 렌더링 | `DOC_DENSITY_BEHAVIOR_DP` 명시 후 exact size/font 별도 검증 |
| `RcSp`와 exact size density 불일치 | source + emulator 재현 | `TextLayout` raw font size, `DimensionModifierOperation.EXACT`, 1080×2400 screenshot | 작은 글꼴, summary/subtitle clip, 화면 하단 과도한 공백 | `density()` expression으로 font와 fixed width/height를 명시 스케일 |
| `TextLookupInt` | alpha14 POC에서 재현 | action 후 context는 4, label은 3 유지 | 선택/수량 상태가 무반응처럼 보임 | direct state `StateLayout` label |
| `TextFromFloat` | alpha14 POC에서 재현 | float mirror state 변경 후 가격 text 유지 | 파생 가격 표시 stale | state 조합별 가격 leaf |
| derived expression → `StateLayout.stateIndex` | alpha14 POC에서 재현 | direct seat state는 이동, 합성 price index는 유지 | 파생 상태 화면이 갱신되지 않음 | 각 원본 state를 중첩 `StateLayout` index로 사용 |
| nested `StateLayout` measurement | alpha14 POC에서 재현 | exact parent 없는 첫 구현에서 heading과 overlap | layout 붕괴 | exact-size `Box` + child `fillMaxSize()` |
| 긴 `RcFloat` expression | alpha14 POC에서 재현 | document generation `RuntimeException: ... to long` | 서버가 문서를 만들지 못함 | 작은 식으로 분해하고 필요 시 `flush()` |
| `RemoteDocumentPlayer` | source-confirmed restriction | alpha14 source annotation | supported public app embedding 계약 부재 | POC에 격리, 제품 Go 금지 |
| `RemoteComposePlayer` | source-confirmed restriction | alpha14 source annotation | View player 직접 사용도 API churn 위험 | POC에 격리 |
| JVM procedural DSL | source-confirmed restriction | `createRcBuffer`, `RcScope` annotations | Ktor producer가 내부 API에 결합 | `server` module 격리와 deterministic test |
| TextField/IME 입력 surface | alpha14 DSL/source에서 미확인 | `RcScope` component API와 core operation 검색 | remote document 내부 직접 타이핑 불가 | named host action → native Android TextField |
| lazy/virtualized list | alpha14 POC에서 미확인 | `verticalScroll`은 존재, 12개 static row scroll은 정상 | task 수에 따라 document가 선형 증가 | 안전 limit 유지, 큰 목록은 pagination/custom renderer 비교 |

## 정상 확인된 경로

- coordinate click과 accessibility clickable semantics
- `MultiClickModifier` → integer value/expression action
- direct integer state를 index로 사용하는 `StateLayout`
- 과거 fixture의 `items_mask` direct state 전이와 현재 `screen` 목록 ↔ 상세 전이
- named host action callback과 Android allowlist
- Ktor binary fetch와 parser validation
- `verticalScroll`로 12개 server-backed row 탐색
- direct `StateLayout(screen)` 목록 ↔ 상세 화면 전환
- native TextField 입력 후 create/delete API와 자동 document reload

즉, 모든 click/component가 고장난 것으로 분류하면 안 된다.

## 다음 alpha 업그레이드 회귀 테스트

1. `TextLookupInt(index=mutableInt)`가 click 한 번 뒤 즉시 새 text를 그리는지 확인한다.
2. `TextFromFloat(mutableFloat)`가 같은 frame에 갱신되는지 확인한다.
3. derived integer expression을 `StateLayout.stateIndex`에 넣어 전환되는지 확인한다.
4. size 없는 nested `StateLayout`과 exact-size wrapper를 각각 screenshot 비교한다.
5. 32 operation 경계 전후 float/integer expression document generation을 테스트한다.
6. `RemoteDocumentPlayer`, `RemoteComposePlayer`, `createRcBuffer`의 restriction이 해제됐는지 source/API reference를 확인한다.
7. 현재 workaround를 제거한 variant와 유지한 variant를 같은 emulator journey로 비교한다.
8. `RcSp`, exact size, padding을 mdpi/xhdpi/xxhdpi에서 비교해 density behavior가 일관적인지 확인한다.
9. public Remote TextField/IME 또는 lazy list component가 추가됐는지 확인한다.

## 제품 판단

이 이슈들은 alpha14 embedded app POC의 기술 난이도를 보여 준다. 특히 “state 값이 바뀜”과 “화면이 바뀜”을 별도 assertion으로 검증해야 한다. player와 producer API가 restricted인 동안에는 이 샘플을 production dependency가 아니라 조사 fixture로 유지한다.
