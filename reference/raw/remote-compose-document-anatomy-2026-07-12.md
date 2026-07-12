---
title: Remote Compose document anatomy and runtime state evidence 2026-07-12
type: source
created: 2026-07-12
updated: 2026-07-12
status: immutable
as_of: 2026-07-12
---

# Remote Compose document 구조와 runtime state 공식 근거 — 2026-07-12

이 스냅샷은 처음 Remote Compose를 배우는 개발자가 binary document의 `Header`, `RootLayoutComponent`, data/state operation, runtime state를 구분할 수 있도록 alpha14 고정 source에서 확인한 근거를 기록한다.

## 기준 source

- [Document Structure](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/Documentation/parts/structure.md)
- [Wire format](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/Documentation/RemoteComposeWireFormat.md.html)
- [`Header.java`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-core/src/main/java/androidx/compose/remote/core/operations/Header.java)
- [`RootLayoutComponent.java`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-core/src/main/java/androidx/compose/remote/core/operations/layout/RootLayoutComponent.java)
- [`CoreDocument.java`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-core/src/main/java/androidx/compose/remote/core/CoreDocument.java)
- [`RemoteComposeBuffer.java`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-core/src/main/java/androidx/compose/remote/core/RemoteComposeBuffer.java)
- [`RemoteComposeState.java`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-core/src/main/java/androidx/compose/remote/core/RemoteComposeState.java)
- [`RemoteState.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/state/RemoteState.kt)
- [`RemoteStateLayout.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/layout/RemoteStateLayout.kt)
- [core `StateLayout.java`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-core/src/main/java/androidx/compose/remote/core/operations/layout/managers/StateLayout.java)
- [`ValueChange.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/action/ValueChange.kt)

## 1. document는 flat operation list다

공식 structure 문서는 저장 포맷을 binary-encoded operation의 flat list로 정의한다. parser/player는 container 시작과 `ContainerEnd`를 이용해 이를 tree로 inflate한다.

일반적인 순서는 다음과 같다.

1. `Header`: 반드시 첫 operation
2. data operation: text, bitmap, font, float/integer constant와 expression
3. macro definition: 사용 전 정의
4. layout tree: 보통 `RootLayoutComponent`부터 시작

data operation은 ID로 참조되므로 사용 전에 정의되어야 한다.

## 2. Header

`Header`는 document protocol metadata다. alpha14 core source의 protocol semantic version은 `1.1.0`, 내부 `DOCUMENT_API_LEVEL`은 `8`이다. AndroidX Maven library version `1.0.0-alpha14`와 document protocol version은 다른 값이다.

### map-based header framing

API level 7 이상 header는 다음 framing을 사용한다.

```text
HEADER opcode
majorVersion | magic number 0x048C0000
minorVersion
patchVersion
property count
typed property entries...
```

parser는 magic number, property count limit, data type을 검사한다. legacy header는 width, height, capabilities가 고정 필드로 이어지는 다른 형태다.

### alpha14에서 확인한 주요 property

| property | 의미 | 초심자에게 중요한 이유 |
|---|---|---|
| `DOC_WIDTH`, `DOC_HEIGHT` | 생성 시 가상 document 크기 | player viewport와 같은 값이라고 가정하면 안 됨 |
| `DOC_DENSITY_AT_GENERATION` | 생성 density | legacy 경로와 density 분석에 사용 |
| `DOC_CONTENT_DESCRIPTION` | document 설명 | 접근성 root metadata |
| `DOC_SOURCE` | document source | provenance/관측성에 활용 가능 |
| `DOC_DATA_UPDATE` | 기존 document의 data update 여부 | full document와 delta를 구분하는 내부 계약 |
| `HOST_EXCEPTION_HANDLER` | 예외 시 host action ID | fail-safe 통합 근거 |
| `DOC_PROFILES` | operation profile mask | parser가 올바른 operation map을 선택하는 핵심 |
| `DOC_DENSITY_BEHAVIOR` | legacy/pixels/dp 해석 | spacing과 font 문제를 분석할 때 핵심 |
| feature version/flag | measure, touch, click, resize 등의 동작 선택 | producer/player behavior negotiation |

`capabilities` bitmask는 source 주석상 현재 unused다. 존재한다고 production capability negotiation이 이미 완성됐다고 해석하지 않는다.

### 누가 Header를 만드는가

public Compose capture 사용자는 `Header`를 직접 쓰지 않는다. `RemoteCreationDisplayInfo`와 `Profile`을 전달하면 내부 `RemoteComposeWriter`가 width, height, content description, profile, density behavior를 header property로 기록한다.

## 3. RootLayoutComponent

`RootLayoutComponent`는 component tree의 layout/paint entry point다.

- `Container`다.
- top-level UI component 또는 data operation을 child로 가진다.
- 일반 `LayoutManager`와 달리 child를 `LayoutContent`로 한 번 더 감싸지 않는다.
- 마지막에 `ContainerEnd`가 필요하다.
- viewport width/height를 기준으로 measure/layout pass를 시작한다.
- touch listener가 있는지 추적하고 component ID를 보정한다.

public Compose capture에서는 `RemoteRootNode.render()`가 내부 writer의 `root { ... }`를 호출해 root start와 matching end를 자동으로 기록한다. 개발자가 `RootLayoutComponent`를 직접 생성하지 않는다.

## 4. layout manager의 중첩 규칙

`Box`, `Column`, `Row`, `Text` 같은 layout manager는 다음 순서를 가진다.

```text
LayoutManager
  Modifier operations
  LayoutContent
    Child components
    ContainerEnd   // LayoutContent 종료
  ContainerEnd     // LayoutManager 종료
```

modifier는 `LayoutContent`보다 먼저 와야 한다. 모든 container는 matching `ContainerEnd`가 필요하다.

## 5. parse와 inflate 흐름

alpha14 `RemoteComposeBuffer`와 `CoreDocument` source에서 확인한 흐름:

1. buffer의 첫 opcode가 `HEADER`인지 확인한다.
2. Header에서 document API level과 profile mask를 읽는다.
3. API/profile에 맞는 operation map을 고른다.
4. 각 opcode를 operation object로 읽는다. unknown operation이면 예외다.
5. flat operation을 container stack으로 중첩한다.
6. macro를 확장하고 component parent/child를 연결한다.
7. 첫 `RootLayoutComponent`를 document root로 저장한다.
8. expression과 listener를 수집한다.
9. root에서 measure → layout → paint를 수행한다.

이 과정 때문에 header/profile 불일치, 잘못된 nesting, unknown opcode, 과도한 nesting은 render 이전에 실패할 수 있다.

## 6. “root state”라는 단일 operation은 없다

공식 structure와 alpha14 source에는 `RootLayoutComponent`는 있지만 `RootState`라는 단일 document operation은 없다.

서로 다른 세 대상을 구분해야 한다.

| 대상 | 역할 |
|---|---|
| `RootLayoutComponent` | UI component tree의 시작점 |
| `RemoteComposeCreationState` | document 생성 중 state → ID 캐시, named state, writer/profile/density를 가진 내부 creation context |
| `RemoteComposeState` | player runtime에서 ID별 data/float/int/color 값과 listener를 저장하는 runtime cache |

## 7. public RemoteState가 document에 기록되는 방식

`RemoteState<T>`는 상수 또는 player에서 평가되는 dynamic expression을 표현한다. `BaseRemoteState`는 creation state별로 ID를 얻고, 같은 cache key는 같은 ID를 재사용한다.

### 세 가지를 구분한다

| public API | document/runtime 의미 |
|---|---|
| `0.ri`, `"text".rs` | remote constant/reference |
| `rememberMutableRemoteInt(0)` | document 안에 mutable value ID를 할당; `valueChange` 대상이 될 수 있음 |
| `rememberNamedRemoteInt("screen", 0)` | `User` domain이 기본인 name + default value; host가 name으로 override 가능한 state |
| `count + 1`, 비교식 | 기존 ID를 참조하는 expression operation |

`RemoteState.Domain.User`는 application-specific named state에 권장된다. `System`은 platform/framework state용이다.

## 8. runtime update와 invalidation

`RemoteComposeState`는 object, float, integer, color 등을 ID map에 보관한다. dependent operation은 state ID listener로 등록된다.

```text
click/touch action
  → ValueIntegerChange 또는 expression change
  → RemoteComposeState의 ID 값 변경
  → 해당 ID listener markDirty
  → repaint 요청
  → 필요하면 remeasure/layout
```

`valueChange(mutableState, updatedValue)`는 mutable state ID와 새 literal/expression ID를 직렬화 가능한 action으로 기록한다. ViewModel이나 server state를 직접 바꾸지 않는다.

## 9. StateLayout은 state 저장소가 아니다

core `StateLayout`은 `indexId`를 가지고 있다. paint 시점에 runtime `RemoteComposeState.getInteger(indexId)`를 읽어 다음을 수행한다.

- current/previous layout index 갱신
- transition flag 설정
- measure invalidation
- 현재/이전 child visibility와 animation 처리
- current child에 click 전달

public `RemoteStateLayout`은 Boolean, Enum, Int 가능한 child를 capture 시 모두 기록한다. `StateLayout` 자체가 state를 소유하는 것이 아니라 전달받은 remote state ID를 selector로 사용한다.

## 10. document 교체와 state 보존

`CoreDocument`의 macro expansion source는 내부 `RemoteComposeState`를 새로 구성한다. player/host가 named override를 다시 적용하거나 별도 restore를 제공할 수 있지만, alpha14 public app-embedded state restoration contract는 확인하지 못했다. 따라서 source 내부 구현만으로 document replacement 뒤 local state 보존을 보장할 수 없다.

따라서 제품 설계에서는 다음을 기본으로 둔다.

- document 교체를 local state 보존으로 간주하지 않는다.
- 보존해야 하는 화면/업무 상태는 ViewModel 또는 host state에 둔다.
- named state를 사용하더라도 refresh/restart restore를 별도로 검증한다.

## 결론

초심자 학습 순서는 `UI DSL`부터가 아니라 다음이어야 한다.

```text
Remote composable
  → capture/writer
  → Header + data + RootLayout + actions
  → binary operation list
  → parse/inflate
  → runtime state ID cache
  → measure/layout/paint
```

이 구조를 이해한 뒤 `RemoteColumn`, `valueChange`, `RemoteStateLayout`, `hostAction`을 학습해야 각각이 document에 무엇을 기록하는지 설명할 수 있다.
