---
title: Remote Compose Document Anatomy와 State Lifecycle
type: concept
created: 2026-07-12
updated: 2026-07-12
as_of: 2026-07-12
confidence: high
sources:
  - ../raw/remote-compose-document-anatomy-2026-07-12.md
  - ../raw/androidx-remote-compose-official-2026-07-12.md
---

# Remote Compose Document Anatomy와 State Lifecycle

이 페이지는 public Compose API로 첫 화면과 state/action 흐름을 익힌 뒤 읽는 심화 자료다. 여기서는 각 API가 어떤 document operation을 만드는지, 생성 단계와 player runtime의 state가 어떻게 분리되는지 분석한다.

## 전체 그림

```text
@RemoteComposable content
        │ capture
        ▼
┌─────────────────────────────────────────────┐
│ Header                                      │
│ version · size · profile · density behavior │
├─────────────────────────────────────────────┤
│ Data / state / expression operations        │
│ TextData #42 · MutableInt #43 · Expr #44    │
├─────────────────────────────────────────────┤
│ RootLayoutComponent                         │
│ └─ Column                                   │
│    ├─ modifiers                             │
│    └─ LayoutContent                         │
│       ├─ Text(ref #42)                      │
│       └─ StateLayout(indexId #43)           │
├─────────────────────────────────────────────┤
│ Action operations                           │
│ ValueChange(target #43) · HostAction        │
└─────────────────────────────────────────────┘
        │ parse + inflate
        ▼
CoreDocument + RemoteComposeState(ID value cache)
        │
        ▼
measure → layout → paint → input → state update
```

저장 형식은 flat operation list다. 위 tree는 parser가 container와 `ContainerEnd`를 이용해 복원한 결과다.

## Header를 읽는 법

Header는 반드시 첫 operation이다.

| 값 | 질문 |
|---|---|
| protocol version / internal API level | 이 player가 이 operation set을 읽을 수 있는가? |
| width / height | 어떤 가상 display를 기준으로 만들었는가? |
| profile mask | 어떤 operation profile로 decode해야 하는가? |
| density behavior | 숫자를 legacy pixel, pixel, dp 중 무엇으로 해석하는가? |
| content description/source | 접근성과 provenance를 어떻게 추적하는가? |
| feature versions | measure/touch/click 동작의 어느 구현을 기대하는가? |

AndroidX library version `1.0.0-alpha14`, document semantic version `1.1.0`, internal document API level `8`은 서로 다른 축이다.

public Compose capture에서는 Header를 직접 만들지 않는다. `RemoteCreationDisplayInfo`와 `Profile`이 writer에 전달되고 writer가 header를 기록한다.

## Root는 무엇인가

`RootLayoutComponent`는 component tree의 entry point다.

- player viewport에서 measure와 layout을 시작한다.
- top-level child를 가진다.
- `LayoutContent` wrapper 없이 child를 직접 가진다.
- matching `ContainerEnd`로 닫힌다.

public Compose capture에서는 내부 `RemoteRootNode`가 writer의 `root { ... }`를 호출한다. 앱 개발자가 root operation을 직접 작성하는 API가 아니다.

## Root와 state를 혼동하지 않기

별도 `RootState` operation은 없다.

```text
RootLayoutComponent = UI tree root
RemoteComposeCreationState = document를 만드는 동안의 ID/writer cache
RemoteComposeState = player runtime의 ID → value 저장소
```

이 세 객체는 이름이 비슷해도 수명과 책임이 다르다.

## State가 ID가 되는 과정

```kotlin
val screen = rememberMutableRemoteInt(0)
val next = valueChange(screen, 1.ri)
```

1. capture 중 `screen`에 mutable integer ID가 할당된다.
2. 같은 state cache key를 다시 참조하면 같은 ID를 재사용한다.
3. `valueChange`는 target state ID와 새 literal/expression을 action operation으로 기록한다.
4. player click 시 runtime state map의 해당 ID가 갱신된다.
5. ID listener가 dependent operation을 dirty로 표시한다.
6. repaint가 요청되고 layout selector라면 remeasure도 발생한다.

## Mutable state와 named state

```kotlin
val localCount = rememberMutableRemoteInt(0)
val hostVisibleScreen = rememberNamedRemoteInt("screen", 0)
```

| state | 적합한 용도 |
|---|---|
| mutable remote state | document 안의 클릭/선택/animation 입력 |
| named remote state | host가 name으로 값을 넣어야 하는 remote variable |
| Android ViewModel state | loading, API 결과, destination, process restore |
| server state | 권한, 영속 데이터, domain rule |

named state의 기본 domain은 application-specific `User`다. `System`은 platform/framework state용이다.

## StateLayout이 하는 일

`RemoteStateLayout`은 가능한 child를 모두 capture한다. core operation은 `indexId`로 runtime integer를 읽고 current child를 선택한다.

```text
RemoteComposeState[indexId] = 0  → child 0 measure/layout/paint
RemoteComposeState[indexId] = 1  → previous=0, current=1, remeasure
```

따라서 다음이 아니다.

- state 저장소
- Android Navigation back stack
- ViewModel
- server synchronization

## 문서 교체 시 주의

alpha14 source는 document를 inflate하고 macro를 확장하며 내부 state table을 다시 구성한다. public app-embedded API에서 local state 자동 restore 계약은 확인하지 못했으므로 document replacement 뒤 값 보존을 가정할 수 없다.

- document refresh 뒤 보존해야 하는 값은 host/ViewModel에 둔다.
- named state override 재적용 여부를 player 버전별로 테스트한다.
- local state가 reset되어도 안전한 UI만 document-local state로 둔다.

## 초심자 확인 문제

1. `Header`와 AndroidX Maven version은 왜 다른가?
2. `RootLayoutComponent`가 `LayoutContent`를 직접 필요로 하지 않는 이유는 무엇인가?
3. `rememberMutableRemoteInt`의 값은 어디에 저장되는가?
4. `valueChange` 뒤 어떤 ID listener가 다시 평가되는가?
5. `StateLayout`을 Navigation으로 사용할 때 무엇이 빠지는가?

답을 source와 함께 확인하려면 [공식 근거 스냅샷](../raw/remote-compose-document-anatomy-2026-07-12.md)을 읽는다.
