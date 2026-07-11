---
title: Compose Remote alpha14 debugging evidence
type: evidence
created: 2026-07-11
as_of: 2026-07-11
scope: Android app embedded SDUI
---

# Compose Remote alpha14 디버깅 증거

## 조사 범위

- AndroidX Remote Compose `1.0.0-alpha14`
- standalone JVM Ktor producer
- Android 앱 내부 `RemoteDocumentPlayer`
- sample: `samples/remote-state-lab`
- emulator: `emulator-5554`

이 문서는 Android 앱 내부 SDUI POC만 다룬다. 다른 Android surface와의 통합은 현재 연구 범위가 아니다.

## 현재 버전 확인

- Android Developers release page는 2026-07-01의 최신 alpha를 `1.0.0-alpha14`로 표시한다.
- 공식 artifact 목록에는 creation core/Android/JVM/Compose, player core/View, tooling preview가 포함된다.
- alpha14 source pin: `19660b9e1b2fec4a9528fe80ce0a432c0fa2f825`.

Sources:

- https://developer.android.com/jetpack/androidx/releases/compose-remote
- https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/

로컬 Google Maven source artifact SHA-256:

| artifact | SHA-256 |
|---|---|
| `remote-core-1.0.0-alpha14-sources.jar` | `ef3e355cd77a2a763cfa2a294df5ae35f10cd6448fc34555f30e21bc9e179e72` |
| `remote-player-compose-1.0.0-alpha14-sources.jar` | `a8f86d86f0e867c833ff5c62bd5332cb3d07bafbe7f3111a6f7c9c6319452353` |
| `remote-creation-core-1.0.0-alpha14-sources.jar` | `c652cd1561a8869d4db80fe6abc381ed646aaa067faa2bae53624dd8e16a0ce3` |

## 전체 디버깅 타임라인

### 1. 초기 증상

- remote document의 제목·본문·control text가 Android host UI보다 지나치게 작았다.
- `+`는 semantics tree에서 clickable로 보였지만 화면의 `3 members`, `$48 / month`가 변하지 않았다.
- native `Load` 버튼은 정상 동작했다.

### 2. density와 typography

초기 문서에는 `Header.DOC_DENSITY_BEHAVIOR`가 없었다. alpha14 core의 기본은 legacy behavior이므로 procedural document의 수치와 player density를 의도대로 맞추지 못했다.

적용:

```kotlin
RemoteComposeWriter.HTag(
  Header.DOC_DENSITY_BEHAVIOR,
  CoreDocument.DENSITY_BEHAVIOR_DP,
)
```

추가로 주요 제목, seat control, choice label, footnote font size를 올렸다. 결과적으로 Android host와 remote content의 상대 크기가 정상 범위로 들어왔다.

판정: component bug가 아니라 producer metadata와 unit 선택 문제다. alpha14 release note가 density behavior API를 public으로 노출한 것도 이 설정이 명시적 계약임을 뒷받침한다.

### 3. 첫 `StateLayout` 우회 실패

seat label을 곧바로 중첩 `StateLayout`으로 바꾼 첫 시도에서는 layout이 Column의 예상 높이에 기여하지 않아 앞선 heading과 겹쳤다.

우회:

- 중첩 state content를 정확한 높이의 outer `Box`에 넣음
- state별 child도 `fillMaxSize()` layout으로 감쌈

판정: alpha14 procedural DSL에서 중첩 `StateLayout`을 wrap-content처럼 신뢰하지 않는다. 명시적 bounds를 부여한다.

### 4. 클릭 경로 확인

Android CLI layout은 `+`를 clickable/focusable로 보고했다. annotated screenshot에서 좌표를 resolve해 실제 input event를 보냈다.

player basic debug log:

```text
[RC] Click at 919.0, 256.0
```

runtime component tree에는 `+` Box 아래 다음 action이 존재했다.

```text
ClickModifier
  ValueIntegerExpressionChangeActionOperation(...)
```

진단 코드로 같은 `RemoteContext`의 값을 action 전후에 읽은 결과:

```text
before seats=3 plan=0
plus handled=true
pro handled=true
after seats=4 plan=1
```

따라서 click hit testing과 integer action은 정상이고, “버튼이 안 됨”의 실제 원인은 표시 계층이었다.

### 5. 동적 text 표시가 갱신되지 않음

action 뒤에도 다음 output은 초기 값을 유지했다.

- `TextLookupInt`로 생성한 `3 members`
- `TextFromFloat`로 생성한 가격
- plan/add-on/billing의 lookup label

core source의 `TextLookupInt`는 index와 dataset을 listen하도록 구현돼 있지만, 이 procedural document + embedded player 구조에서는 click 후 표시가 재평가되지 않았다. alpha08 release note의 array-element tracking fix와도 다른 조건이다.

판정: alpha14 전체의 확정 bug라고 일반화하지 않는다. 이 POC 구조에서 재현된 component-level compatibility issue로 기록한다.

### 6. 파생 expression을 `StateLayout` index로 사용

가격 조합을 하나의 derived integer index로 계산해 `StateLayout`에 전달한 시도에서는 seat direct state는 바뀌어도 가격 state가 이동하지 않았다.

우회:

- `seats`, `plan`, `analytics`, `support`, `annual` 각각을 직접 index로 사용하는 중첩 `StateLayout` 구성
- leaf에서 80개 조합의 가격을 생성

직접 state index는 emulator에서 정상 전환됐다.

### 7. 긴 `FloatExpression`

가격을 하나의 float expression으로 합친 시도는 document 생성 중 다음 오류를 냈다.

```text
RuntimeException: ... to long
```

`Limits.MAX_EXPRESSION_OPS` 계열 제한과 expression serialization 길이를 고려해야 한다. 중간 표현식을 `flush()`하면 생성은 가능했지만, 동적 text 표시 문제 때문에 최종 구현에는 사용하지 않았다.

판정: 복잡한 식은 작은 중간 expression으로 분해하고 document-generation test를 둔다.

### 8. 서버 포트 충돌

검증용 Ktor 서버가 실행 중인 상태에서 두 번째 `:server:run`을 실행해 `BindException: Address already in use`가 발생했다. 검증 서버를 종료하고 8080이 free인 것을 확인했다.

이 문제는 Remote Compose가 아니라 개발 실행 lifecycle 문제다. sample README에 점유 프로세스 확인 절차를 추가했다.

### 9. 최종 검증

```text
initial: 3 members, Starter, $48 / month
plus:    4 members, $64 / month
pro:     Pro ✓, $112 / month
review:  4, Pro, $112 / month
```

통과한 명령:

```bash
./gradlew :server:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
android run --device=emulator-5554 --apks=app/build/outputs/apk/debug/app-debug.apk --activity=.MainActivity
android layout --device=emulator-5554 --pretty
```

Screenshot:

- `samples/remote-state-lab/raw/verification/2026-07-11-font-plus-fix/final-configured-clean.png`
- 전체 screenshot 단계 설명: `samples/remote-state-lab/raw/verification/2026-07-11-font-plus-fix/README.md`

## source에서 확인한 API 제한

| API/component | alpha14 source 상태 | 의미 |
|---|---|---|
| `RemoteDocumentPlayer` | file/function `LIBRARY_GROUP` restricted | 일반 앱용 supported public player로 간주 불가 |
| `RemoteComposePlayer` | class와 다수 method restricted | 직접 View embedding도 같은 위험 |
| `createRcBuffer` / `createRawRcBuffer` | `LIBRARY_GROUP` restricted | JVM server producer가 내부 DSL에 의존 |
| `RcScope`, `RcActionScope`, procedural `Modifier` | `LIBRARY_GROUP` restricted | sample 전체가 alpha 내부 API 변화에 민감 |
| core `StateLayout`, `TextLookupInt` | `LIBRARY_GROUP` restricted | runtime operation을 제품 component contract로 고정하면 안 됨 |

## 사실, 관찰, 권고 분리

- Sourced fact: 최신은 alpha14이고 release notes에 density/font/border/background fix가 기록돼 있다.
- Sourced fact: 위 표의 API는 alpha14 source에서 restricted다.
- Engineering observation: 이 sample에서 dynamic text와 derived state index가 click 후 화면을 갱신하지 않았다.
- Engineering observation: direct integer state 기반 `StateLayout`은 정상 동작했다.
- Recommendation: 이 workaround를 production architecture로 일반화하지 말고 다음 alpha마다 최소 재현 test를 다시 실행한다.
