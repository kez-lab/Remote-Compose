---
title: Android Compose 생성 frontend 심화 학습 경로
type: synthesis
created: 2026-07-12
updated: 2026-07-13
as_of: 2026-07-13
confidence: high
sources:
  - ../raw/remote-compose-api-syntax-audit-2026-07-12.md
  - ../raw/remote-compose-document-anatomy-2026-07-12.md
  - ../raw/androidx-remote-compose-official-2026-07-12.md
---

# Android Compose 생성 frontend 심화 학습 경로

> 이 페이지는 더 이상 HTML beginner 코드랩의 기준 경로가 아니다. 현재 코드랩은 [Ktor와 RcScope로 배우는 서버 중심 SDUI](ktor-rcscope-codelab-path.md)를 따른다. 이 페이지는 Android 또는 Android 기반 CI에서 public Compose frontend로 문서를 capture하는 별도 심화 과정이다.

이 페이지는 `remote-creation-compose`를 선택한 개발자가 public authoring/capture API와 이 저장소의 procedural POC를 구분하도록 정리한다. 서버가 요청마다 `RcScope`로 동적 document를 생성하는 방법을 배우려는 독자는 이 페이지가 아니라 서버 중심 코드랩을 먼저 읽는다.

## 첫 문장

> Remote Compose는 서버가 보낸 UI 문서를 Android 앱 안에서 화면으로 그리는 기술이다.

서버가 Kotlin 소스나 Android 앱을 내려보내 실행하는 것이 아니다. 앱에 포함된 player는 자신이 지원하는 Remote Compose operation만 해석한다. ViewModel, Repository, API client와 서버 데이터 저장소도 사라지지 않는다.

## 먼저 구분할 세 가지

| 분류 | 예 | 이 심화 문서에서의 취급 |
|---|---|---|
| AndroidX public API | `RemoteColumn`, `RemoteText`, `RemoteModifier`, `18.rsp`, `captureSingleRemoteDocument`, `RemoteStateLayout`, `valueChange`, `hostAction` | Android/CI capture 경로의 심화 실습 코드 |
| AndroidX restricted API | `RemoteDocumentPlayer`, `RemoteComposePlayer`, `RcScope`, procedural `createRcBuffer` | 비교와 공개 범위 경고만 제공 |
| 저장소 custom code | Ktor endpoint, `HostActionRouter`, `scaledSp`, `scaledSize`, task DTO | 서버 중심 코드랩과 POC 회고로 연결 |

## 권장 학습 순서

1. 서버 중심 코드랩을 끝내고 document, player, host action의 기본 실행 모델을 이해한다.
2. document를 요청마다 서버에서 만들지, Android/CI에서 미리 capture할지 배포 모델을 선택한다.
3. Android/CI capture를 선택했다면 `RemoteColumn`, `RemoteText`, `RemoteModifier`, remote value를 배운다.
4. `RemoteContentPreview`가 authoring 확인 도구이고 운영 전송 자체는 아니라는 점을 구분한다.
5. `captureSingleRemoteDocument`가 Android `Context` 기반으로 `ByteArray`를 만드는 흐름을 익힌다.
6. `rememberMutableRemote*`, `valueChange`, `RemoteStateLayout`의 capture 결과와 player-local state를 분석한다.
7. capture artifact를 versioning·검증·서버/CDN 배포하는 파이프라인은 별도 engineering 설계임을 기록한다.
8. restricted embedded player, 호환성, 보안, fallback, 접근성, rollback을 제품 도입 gate로 평가한다.

## 전체 흐름

```text
서버 데이터
  → Remote Compose 문서 생성
  → HTTP 등으로 document bytes 전달
  → Android 앱의 player가 해석
  → 사용자에게 화면 표시

사용자 클릭
  → document-local valueChange 또는 hostAction
  → hostAction이면 Android ViewModel/Repository/API가 처리
  → 필요하면 새 document 요청
```

## 심화: document anatomy는 나중에 이해하기

```text
Header
Data / state / expression operations
RootLayoutComponent
  LayoutManager
    Modifier operations
    LayoutContent
      Child components
Actions
```

저장 포맷은 flat operation list이고 player가 container/end operation으로 tree를 복원한다. 별도 `RootState` operation은 없다. root는 UI tree의 시작점이고 runtime state는 ID 기반 `RemoteComposeState`에 보관된다.

public Compose API로 첫 화면과 상태 흐름을 익히는 동안 이 구조를 직접 작성할 필요는 없다. restricted procedural builder를 분석하거나 protocol 호환성과 document replacement를 설계할 때 [Document Anatomy와 State Lifecycle](document-anatomy-and-state.md)을 읽는다.

## 공식 dependency

```kotlin
dependencies {
    implementation("androidx.compose.remote:remote-core:1.0.0-alpha14")
    implementation("androidx.compose.remote:remote-creation-compose:1.0.0-alpha14")
    implementation("androidx.compose.remote:remote-tooling-preview:1.0.0-alpha14")
}
```

이 조합은 공식 Compose 생성·preview 학습에 집중한다. 릴리스 페이지가 나열하는 전체 artifact를 무조건 모두 추가하지 않는다.

## 공식 remote composable 작성

```kotlin
@Composable
@RemoteComposable
fun ChecklistContent() {
    RemoteColumn(
        modifier = RemoteModifier.fillMaxSize().padding(24.rdp),
    ) {
        RemoteText("배포 체크리스트".rs, fontSize = 24.rsp)
        RemoteSpacer(RemoteModifier.size(12.rdp))
        RemoteText("API 회귀 테스트".rs, fontSize = 18.rsp)
    }
}
```

- `RemoteColumn`은 `Column`과 닮았지만 player가 평가할 operation을 기록한다.
- `RemoteModifier`는 remote-first modifier다.
- `rdp`, `rsp`, `rs`는 AndroidX가 제공하는 변환이다.

## 공식 preview

```kotlin
@Preview
@Composable
fun ChecklistContentPreview() {
    RemoteContentPreview { ChecklistContent() }
}
```

`RemoteContentPreview`는 `remote-tooling-preview`의 public API다.

## 공식 document capture

```kotlin
val displayInfo = RemoteCreationDisplayInfo(
    width = 1080,
    height = 1920,
    densityDpi = 420,
    densityBehavior = RemoteDensityBehavior.Dp,
)

suspend fun captureChecklist(context: Context): ByteArray {
    val captured = captureSingleRemoteDocument(
        context = context,
        creationDisplayInfo = displayInfo,
    ) {
        ChecklistContent()
    }

    return captured.bytes
}
```

이 API는 `suspend`이며 Android `Context`를 받는다. Ktor/JVM 서버 코드와 동일한 경로가 아니다.

## 공식 local state와 action

public Compose frontend의 `RemoteText`와 Ktor/JVM sample의 procedural `RcScope.Text`는 서로 다른 API다. remote value와 상태 수명주기를 먼저 이해하려면 [Frontend, Remote Value, State 실행 모델](remote-state-and-values.md)을 읽는다.

```kotlin
@Composable
@RemoteComposable
fun ToggleTask() {
    val done = rememberMutableRemoteBoolean(false)

    RemoteStateLayout(currentState = done) { isDone ->
        val next = (!isDone).rb
        RemoteText(
            text = if (isDone) "완료".rs else "미완료".rs,
            fontSize = 18.rsp,
            modifier = RemoteModifier.clickable(valueChange(done, next)),
        )
    }
}
```

`valueChange`는 player-local remote state를 바꾼다. ViewModel이나 Repository를 호출하지 않는다.

- `false`는 `rememberMutableRemoteBoolean`이 일반 `Boolean`을 받으므로 변환하지 않는다.
- `RemoteStateLayout`의 `isDone`도 가능한 child를 구성하기 위한 일반 `Boolean`이다.
- `RemoteText`는 `RemoteString`을 요구하므로 `.rs`가 필요하다.
- `valueChange`의 updated value는 `RemoteState<Boolean>`이어야 하므로 일반 Boolean에 `.rb`가 필요하다.
- alpha14 배포 AAR와 고정 source의 함수 이름은 소문자 `valueChange`다. moving Android API reference의 대문자 `ValueChange` 표기와 충돌한다.

## 공식 `hostAction`

```kotlin
RemoteText(
    text = "상세 열기".rs,
    modifier = RemoteModifier.clickable(
        hostAction("task.open".rs, taskId.ri),
    ),
)
```

공식 API가 보장하는 것은 이름과 선택적 payload를 host action으로 기록하는 것까지다. 앱이 이 event를 어떤 callback으로 받고 어느 ViewModel이나 API에 연결하는지는 player/host 통합의 책임이다.

alpha14의 `RemoteDocumentPlayer.onNamedAction`은 이 메커니즘을 보여 주지만 player 자체가 `LIBRARY_GROUP` restricted다. 따라서 일반 앱용 안정 public integration 예제로 제시하지 않는다.

## density와 `scaledSp`

공식 Compose 경로:

```kotlin
RemoteText("제목".rs, fontSize = 18.rsp)
RemoteSpacer(RemoteModifier.size(12.rdp))
```

`RemoteTextUnit`은 `RemoteDensity`와 비선형 font scale 변환을 사용한다. alpha14에서는 `RemoteDensityBehavior.Dp`도 public이다.

다만 `RemoteText`의 density-dependent 값은 player 기기가 아니라 document 생성 환경의 `RemoteDensity`와 font scale을 기준으로 pixel 값으로 변환된다. `18.rsp`를 썼다는 사실만으로 모든 재생 기기에서 Android `18.sp`와 동일하게 보인다고 가정할 수 없다.

POC 전용 helper:

```kotlin
// Remote Compose 공식 API가 아니다.
// restricted RcScope/RcSp 기반 JVM POC에서 관찰된 크기를 보정한 로컬 workaround다.
context(density: RcFloat)
private val Int.scaledSp: RcSp
    get() = RcSp((density * toFloat()).toFloat())
```

public Compose frontend 심화 예제에서 이 helper를 사용하지 않는다. POC 회고에서 발생 이유와 제거 조건만 기록한다.

## 현재 도입 경계

| 질문 | alpha14 근거 기반 답 |
|---|---|
| 공식 Compose 문서 생성 API가 있는가? | 예. `remote-creation-compose` public API가 있다. |
| 공식 preview API가 있는가? | 예. `remote-tooling-preview` public API가 있다. |
| remote local state와 host action이 public인가? | 예. state, `valueChange`, `RemoteStateLayout`, `hostAction`이 공개되어 있다. |
| 일반 앱에서 사용할 public embedded player가 있는가? | 고정 alpha14 source 기준 확인하지 못했다. player class는 restricted다. |
| 공식 Ktor/JVM server codelab이 있는가? | 확인하지 못했다. 이 저장소의 서버는 engineering POC다. |
| Remote Compose가 ViewModel/Repository를 대체하는가? | 아니다. 문서 layout/state/action과 앱 business/data orchestration은 별도 책임이다. |

## 심화 문서 작성 원칙

- 서버 중심 코드랩을 끝낸 독자가 Android/CI capture라는 다른 제작 방식을 선택했을 때 읽도록 범위를 표시한다.
- Header, RootLayout, runtime ID는 서버 중심 본편의 설명과 중복하지 않고 필요할 때 심화 문서로 연결한다.
- 각 코드 블록에 `공식 public API`, `공식 source demo`, `restricted`, `POC custom` 출처 배지를 붙인다.
- public이라는 이유만으로 서버 중심 본편보다 먼저 배치하지 않는다. producer와 배포 모델이 이 API에 맞을 때 학습한다.
- `scaledSp`, Ktor route, `HostActionRouter`를 AndroidX 예제로 표현하지 않는다.
- public player가 없는 현재 상태를 생략하지 않는다.
- [API 문법과 예제 감사](api-syntax-audit.md)의 conversion·suspend·public/procedural 경계를 회귀 기준으로 사용한다.

## 근거

- [공식 근거 스냅샷](../raw/androidx-remote-compose-official-2026-07-12.md)
- [Remote Compose 릴리스 노트](https://developer.android.com/jetpack/androidx/releases/compose-remote)
- [Compose 생성 API reference](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/layout/package-summary)
- [alpha14 고정 API signature](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/api/current.txt)
