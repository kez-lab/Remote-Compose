---
title: Remote Compose Frontend, Remote Value, State 실행 모델
type: concept
created: 2026-07-12
updated: 2026-07-13
as_of: 2026-07-13
confidence: high
sources:
  - ../raw/remote-compose-api-syntax-audit-2026-07-12.md
  - ../raw/androidx-remote-compose-official-2026-07-12.md
  - ../raw/remote-compose-document-anatomy-2026-07-12.md
---

# Remote Compose Frontend, Remote Value, State 실행 모델

> **선택 심화 과정:** 이 문서는 Android/Compose capture frontend를 선택했을 때 읽는다. `Ktor/JVM → RcScope → ByteArray → Android Player` 경로의 입문 코드랩에는 `RemoteText`, `.rs`, `.rb`가 필요하지 않다. 서버 중심 본편은 [Ktor와 RcScope 학습 경로](ktor-rcscope-codelab-path.md)를 따른다.

이 문서는 서로 다른 document 제작 방식을 비교해야 하는 시점의 세 질문에 답한다.

1. 왜 Android public 예제는 `RemoteText`인데 서버 sample은 `Text`인가?
2. 왜 일반 `String`과 `Boolean`에 `.rs`, `.rb`를 붙이는가?
3. Remote Compose state는 Jetpack Compose state, ViewModel, server state와 어떻게 다른가?

## 하나의 document를 만드는 두 frontend

```text
public Compose frontend                         restricted procedural frontend
@RemoteComposable                              createRcBuffer { RcScope ... }
RemoteText("완료".rs)                          Text("완료")
rememberMutableRemoteBoolean(false)            remoteNamedInteger("done", 0)
valueChange(done, true.rb)                     setValue(done, 1)
          \                                      /
           └──── Remote Compose core document ──┘
                  text/layout/state/action operations
```

두 frontend는 같은 Kotlin API의 긴 이름과 짧은 이름이 아니다. 비슷한 component를 서로 다른 작성 모델로 core document에 기록한다.

| 구분 | Public Compose frontend | Procedural JVM frontend |
|---|---|---|
| 주요 artifact | `remote-creation-compose` | `remote-creation-jvm`, `remote-creation-core` |
| 작성 문맥 | `@Composable @RemoteComposable` | `RcScope.() -> Unit` |
| 텍스트 | `RemoteText(RemoteString)` | `RcScope.Text(String)` 또는 `Text(RcText)` |
| layout | `RemoteColumn`, `RemoteRow` | `Column`, `Row` |
| state | `MutableRemoteBoolean/Int` | `RcInteger` 등 |
| action | `valueChange`, `hostAction` | `setValue`, procedural `hostAction` |
| capture 환경 | Android `Context` 기반 | 일반 JVM에서 buffer 작성 가능 |
| alpha14 공개 범위 | 작성/capture API public | `RcScope`와 `createRcBuffer` restricted |

### 서버의 `Text`는 Jetpack Compose `Text`가 아니다

```kotlin
import androidx.compose.remote.creation.dsl.RcScope

private fun RcScope.TaskListScreen(...) {
    Text("배포 작업", fontSize = 38.scaledSp)
}
```

`Text`는 `RcScope` member다. `@Composable` 함수가 아니며 Android 화면을 직접 그리지 않는다. 호출 시 writer에 Remote Compose text component를 추가한다. alpha14의 `RcScope`에는 `Text(String)`과 `Text(RcText)` overload가 있으므로 sample의 일반 문자열에는 `.rs`가 필요 없다.

public Compose의 `.rs`는 `String -> RemoteString` 변환이다. procedural `rsp`도 public Compose `rsp`와 이름만 비슷하다. 전자는 `RcSp`, 후자는 `RemoteTextUnit`이므로 타입과 density 경로를 섞으면 안 된다.

## Remote value가 필요한 이유

일반 Kotlin 값은 producer에서 문서를 만드는 동안만 존재한다. 반대편 player가 사용할 값은 writer가 document data 또는 expression operation으로 기록할 수 있어야 한다.

```text
Kotlin String "완료"
  └─ .rs
     └─ RemoteString
        └─ text data ID와 component reference로 기록
           └─ player가 ID를 해석해 표시
```

| Kotlin 값 | 변환 | Remote type | 대표 사용처 |
|---|---|---|---|
| `String` | `.rs` | `RemoteString` | `RemoteText`, `hostAction` name |
| `Boolean` | `.rb` | `RemoteBoolean` | `valueChange`의 새 값, expression |
| `Int` | `.ri` | `RemoteInt` | integer state action, payload |
| `Float`/`Int` | `.rf` | `RemoteFloat` | numeric expression |
| `Int`/`Float` | `.rdp` | `RemoteDp` | layout dimension |
| `Int` | `.rsp` | `RemoteTextUnit` | public `RemoteText.fontSize` |

suffix를 모든 literal에 붙이는 것이 아니다. parameter가 어느 세계의 타입을 요구하는지 보고 결정한다.

```kotlin
rememberMutableRemoteBoolean(false) // Boolean이므로 .rb 불필요
RemoteText("완료".rs)               // RemoteString이므로 .rs 필요
valueChange(done, true.rb)          // RemoteState<Boolean>이므로 .rb 필요
hostAction("task.create".rs)       // RemoteString이므로 .rs 필요
```

## 상태를 네 단계로 나누기

### 1. Producer/capture

Kotlin 코드가 실행된다. public frontend는 remote composable을 composition하고, procedural frontend는 `RcScope` block을 실행한다. `if`, `when`, loop와 helper는 여기서 실행되며 mutable remote state에는 document ID가 할당된다.

### 2. 전송된 document

Kotlin 함수나 lambda는 남지 않는다. 토글 예제를 단순화하면 다음과 같다.

```text
MutableInt #42 = 0
StateLayout(indexId = #42)
  child 0: Text("미완료"), ClickAction(Set #42 = 1)
  child 1: Text("완료"),   ClickAction(Set #42 = 0)
```

### 3. Player playback

player는 `RemoteComposeState[#42] = 0` 같은 runtime map을 가진다. click operation이 `#42 = 1`로 바꾸면 listener가 dependent operation을 dirty 처리하고 `StateLayout`이 child 1을 선택한다.

이때 producer Kotlin lambda, ViewModel, Repository, HTTP, server DB는 실행되지 않는다.

### 4. Host/server

영속성, 인증, retry, 다른 앱 화면 이동이 필요하면 `hostAction`으로 Android host에 event를 올린다. Android가 allowlist command로 변환한 뒤 ViewModel/Repository/API를 실행한다.

## 토글 예제를 완전히 해석하기

```kotlin
val done = rememberMutableRemoteBoolean(false)

RemoteStateLayout(currentState = done) { isDone ->
    RemoteText(
        text = if (isDone) "완료".rs else "미완료".rs,
        modifier = RemoteModifier.clickable(
            valueChange(done, (!isDone).rb),
        ),
    )
}
```

1. `rememberMutableRemoteBoolean(false)`는 일반 Boolean 초기값으로 mutable remote state를 만든다. alpha14에서 Boolean은 내부적으로 remote integer 0/1에 encode된다.
2. Boolean `RemoteStateLayout`은 content lambda를 false와 true 각각에 대해 capture 중 실행해 두 child를 기록한다.
3. `isDone`은 runtime `RemoteBoolean`이 아니라 child 생성용 일반 Kotlin `Boolean`이다.
4. false child에는 `"미완료".rs`와 `done = true.rb` action이 기록된다.
5. true child에는 `"완료".rs`와 `done = false.rb` action이 기록된다.
6. 재생 중에는 player가 `done`의 ID 값으로 둘 중 하나만 표시한다.

`valueChange(done, !done)`도 타입상 remote expression이지만, 이 문서는 child별 literal을 명확히 보여 주고 alpha14의 미검증 expression shortcut을 피하기 위해 `(!isDone).rb`를 사용한다.

## Jetpack Compose state와 비교

| 질문 | Jetpack Compose | Remote Compose document state |
|---|---|---|
| 값이 사는 곳 | Android process snapshot state | player ID 기반 state map |
| 변경 후 실행 | Kotlin recomposition | document listener/selector 평가 |
| click 표현 | Kotlin lambda | serializable action operation |
| 임의 함수 호출 | 가능 | 불가능 |
| ViewModel 접근 | Kotlin에서 가능 | 직접 불가능 |
| 문서 교체 후 유지 | 앱 state 설계에 따름 | alpha14 public restore 계약 미확인 |
| server 저장 | Repository로 가능 | `hostAction` 경계 필요 |

## 상태 소유권

| 상태 | 권장 소유자 |
|---|---|
| 임시 펼침, reset되어도 되는 토글 | Remote document state |
| loading, error, connection, process restore | Android ViewModel |
| 사용자가 입력 중인 text | Android UI/ViewModel |
| 저장된 task, 권한, 결제, domain rule | Repository/server |
| 작은 문서 내부 화면 selector | Remote state 가능 |
| deep link와 앱 back stack | Android Navigation |

## 반복하지 않을 설명 실수

- public `RemoteText`와 procedural `RcScope.Text`를 같은 API의 별칭으로 설명하지 않는다.
- `.rs`, `.rb`를 암기 항목으로 먼저 제시하지 않고 Kotlin 값이 document value가 되는 경계를 설명한다.
- `RemoteStateLayout` content lambda가 playback callback인 것처럼 표현하지 않는다.
- state 예제에는 capture, document, playback, host/server를 모두 보여 준다.
- public Compose `rsp`와 procedural `RcSp`를 동일한 타입이나 scale 계약으로 설명하지 않는다.

## 근거

- [alpha14 API 문법 감사](api-syntax-audit.md)
- [alpha14 고정 `RcScope.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-core/src/main/java/androidx/compose/remote/creation/dsl/RcScope.kt)
- [alpha14 고정 `RemoteStateLayout.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/layout/RemoteStateLayout.kt)
- [Document Anatomy와 State Lifecycle](document-anatomy-and-state.md)
