---
title: AndroidX Remote Compose official evidence snapshot 2026-07-12
type: source
created: 2026-07-12
updated: 2026-07-12
status: immutable
as_of: 2026-07-12
---

# AndroidX Remote Compose 공식 근거 스냅샷 — 2026-07-12

이 문서는 AndroidX Remote Compose를 처음 학습할 때 사용할 공식 근거를 한곳에 고정한다. 외부 문서를 복제하지 않고 URL, 확인 날짜, 확인한 사실, API 공개 범위만 기록한다.

## 조사 방법

- AndroidX 릴리스 노트와 Google Maven metadata로 현재 버전을 교차 확인했다.
- `1.0.0-alpha14` 릴리스 범위의 마지막 커밋 `19660b9e1b2fec4a9528fe80ce0a432c0fa2f825`에 고정해 `api/current.txt`, 구현 소스, integration demo를 확인했다.
- Android Developers API reference에서 capture, layout, state, action, modifier, preview package를 확인했다.
- Android CLI 문서 검색도 수행했지만 2026-07-12 인덱스에는 Remote Compose 일반 API 문서가 검색되지 않았다. 따라서 릴리스 원문, API reference, Google Maven, 고정 AndroidX 소스를 현재 판단 기준으로 삼았다.

## 버전과 아티팩트

| 공식 출처 | 확인한 사실 |
|---|---|
| [Remote Compose 릴리스 노트](https://developer.android.com/jetpack/androidx/releases/compose-remote) | 최신 버전은 2026-07-01의 `1.0.0-alpha14`다. dependency 예시는 `remote-core`, creation 계열, `remote-player-core`, `remote-player-view`, `remote-tooling-preview`를 나열한다. |
| [Google Maven metadata](https://dl.google.com/dl/android/maven2/androidx/compose/remote/remote-core/maven-metadata.xml) | `latest`와 `release`가 `1.0.0-alpha14`, `lastUpdated`가 `20260701170551`이다. |
| [AndroidX 전체 버전 표](https://developer.android.com/jetpack/androidx/versions) | `compose.remote`의 alpha가 `1.0.0-alpha14`로 표시된다. |

Google Maven에서 alpha14 POM 존재를 확인한 artifact:

- `remote-core`
- `remote-creation`
- `remote-creation-core`
- `remote-creation-android`
- `remote-creation-jvm`
- `remote-creation-compose`
- `remote-player-core`
- `remote-player-view`
- `remote-player-compose`
- `remote-tooling-preview`
- `remote-testing`

artifact가 배포되었다는 사실은 그 안의 API가 일반 앱을 위한 public contract라는 뜻이 아니다.

## 공식 공개 Compose 생성 API

판정 기준은 alpha14 고정 커밋의 [`remote-creation-compose/api/current.txt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/api/current.txt)다.

### 문서 캡처

| API | 공식 계약 |
|---|---|
| [`captureSingleRemoteDocument`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/capture/package-summary#captureSingleRemoteDocument(android.content.Context,androidx.compose.remote.creation.compose.capture.RemoteCreationDisplayInfo,androidx.compose.remote.creation.compose.capture.RemoteDensity,androidx.compose.ui.unit.LayoutDirection,androidx.compose.remote.core.RemoteClock,androidx.compose.remote.creation.profile.Profile,androidx.compose.remote.creation.compose.capture.WriterEvents,kotlin.Function0)) | Android `Context`와 remote composable content로 문서를 한 번 캡처하고 `CapturedDocument`를 반환하는 suspend API다. |
| [`captureRemoteDocument`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/capture/package-summary#captureRemoteDocument(android.content.Context,androidx.compose.remote.creation.compose.capture.RemoteCreationDisplayInfo,androidx.compose.remote.creation.compose.capture.RemoteDensity,androidx.compose.ui.unit.LayoutDirection,androidx.compose.remote.creation.compose.capture.WriterEvents,androidx.compose.remote.core.RemoteClock,androidx.compose.remote.creation.profile.Profile,kotlin.coroutines.CoroutineContext,kotlin.Function0)) | 재구성과 시각 변화가 발생할 때 중복을 제거한 `Flow<ByteArray>`를 방출한다. |
| [`RemoteCreationDisplayInfo`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/capture/RemoteCreationDisplayInfo) | 원격 표시를 예측하기 위한 가상 화면 크기, density, font scale, inspection mode, density behavior를 담는다. 실제 player 환경과 다를 수 있다. |
| [`RemoteDensity`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/capture/RemoteDensity) | remote composition의 dp와 font scale 변환에 사용하는 density를 나타낸다. `Host`는 player host의 값을 참조한다. |
| [`Profile`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/profile/Profile) | 문서 API level, 허용 operation, platform service, writer를 묶어 생성 시 허용 범위를 결정한다. |

### 레이아웃과 표시

| API reference | 공개 API |
|---|---|
| [layout package](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/layout/package-summary) | `RemoteBox`, `RemoteRow`, `RemoteColumn`, `RemoteSpacer`, `RemoteText`, `RemoteImage`, `RemoteCanvas`, `RemoteStateLayout`, `@RemoteComposable` |
| [modifier package](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/modifier/package-summary) | `RemoteModifier`, size/padding/background/border/clip/click/touch/scroll/semantics 계열 |
| [state package](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/state/package-summary) | `RemoteBoolean`, `RemoteInt`, `RemoteFloat`, `RemoteString`, mutable/named remote state, remote expression과 변환 |
| [action package](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/action/package-summary) | `valueChange`, `combinedAction`, `hostAction`, Android `pendingIntentAction` |
| [tooling preview](https://developer.android.com/reference/kotlin/androidx/compose/remote/tooling/preview/package-summary) | `RemoteContentPreview`, `RemoteDocumentPreview`, `RemotePreviewWrapper` |

### 전체 public Compose API package 카탈로그

| package | 책임 |
|---|---|
| [`androidx.compose.remote.creation.compose`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/package-summary) | creation feature flag와 Compose 생성 진입점 |
| [`...compose.action`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/action/package-summary) | 직렬화 가능한 state/host/PendingIntent action |
| [`...compose.capture`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/capture/package-summary) | 단일/stream capture, display info, density, vector, writer event |
| [`...compose.layout`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/layout/package-summary) | layout, image, text, canvas, state layout, remote scopes |
| [`...compose.modifier`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/modifier/package-summary) | layout/draw/input/scroll/semantics modifier |
| [`...compose.painter`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/painter/package-summary) | `RemotePainter`, remote image bitmap painter |
| [`...compose.shaders`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/shaders/package-summary) | `RemoteBrush`와 image/gradient brush |
| [`...compose.shapes`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/shapes/package-summary) | remote shape, outline, rounded corner |
| [`...compose.state`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/state/package-summary) | remote primitive/state/expression, unit conversion |
| [`...compose.text`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/text/package-summary) | remote font family, typeface, text style |
| [`...compose.vector`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/vector/package-summary) | remote path builder와 vector painter |
| [`androidx.compose.remote.creation.profile`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/profile/package-summary) | `Profile`, AndroidX/Widgets profile과 operation 허용 범위 |
| [`androidx.compose.remote.tooling.preview`](https://developer.android.com/reference/kotlin/androidx/compose/remote/tooling/preview/package-summary) | content/document preview |

이 카탈로그는 public API의 존재 범위를 기록한다. 모든 API를 한 화면에서 사용해야 한다는 의미가 아니며, 코드랩은 layout/state/action/capture/density의 최소 학습 경로만 실습한다.

### 공식 단위 API

alpha14 공개 Compose API에는 다음 변환이 있다.

- `Int.rdp`, `Float.rdp` → `RemoteDp`
- `Int.rsp` → `RemoteTextUnit`
- `Int.rf`, `Float.rf` → `RemoteFloat`
- `Int.ri` → `RemoteInt`
- `String.rs` → `RemoteString`
- `Color.rc` → `RemoteColor`

[`RemoteTextUnit.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/state/RemoteTextUnit.kt)는 `rsp`를 `RemoteDensity.fontScale`과 density를 사용해 pixel 값으로 변환하며 비선형 font scaling converter를 거친다. 따라서 공개 Compose API 학습 코드에는 `18.rsp`를 사용한다.

`scaledSp`, `scaledSize`는 AndroidX API가 아니다. 이 저장소의 restricted procedural DSL POC에서 관찰한 크기 차이를 보정하기 위해 만든 로컬 helper다.

## 상태와 액션의 공식 의미

### `valueChange`

[`valueChange`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/action/package-summary)는 `MutableRemoteState<T>`와 변경할 `RemoteState<T>`를 받아 player 안의 remote state를 변경하는 직렬화 가능한 action을 만든다.

### `RemoteStateLayout`

[`RemoteStateLayout` 고정 소스](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/layout/RemoteStateLayout.kt)는 Boolean, Enum, Int overload를 공개한다. 가능한 child를 모두 문서에 기록하고 player가 현재 remote state에 맞는 child를 표시한다. Android Navigation의 back stack을 제공하는 API는 아니다.

### `hostAction`

[`hostAction` 고정 소스](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/action/HostAction.kt)는 이름만 또는 이름과 `RemoteFloat`/`RemoteInt`/`RemoteString` payload를 전달하는 공개 action factory다. 이 operation 자체는 URL, HTTP method, coroutine, repository를 실행하지 않는다.

## 공식 샘플과 테스트 자료

| 공식 소스 | 확인한 내용 |
|---|---|
| [`ClickableDemo.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/integration-tests/demos/src/main/java/androidx/compose/remote/integration/demos/modifier/ClickableDemo.kt) | `rememberMutableRemoteInt`, `valueChange`, `RemoteModifier.clickable`, `RemoteColumn/Row/Box/Text`, `rdp`를 함께 사용한다. |
| [`RemoteStateLayoutDemos.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/integration-tests/demos/src/main/java/androidx/compose/remote/integration/demos/layout/RemoteStateLayoutDemos.kt) | named integer state를 player에서 갱신하고 `RemoteStateLayout`이 child를 선택하는 예를 제공한다. Demo harness는 restricted player를 쓰므로 파일 자체가 `RestrictedApiAndroidX`를 suppress한다. |
| [`remote-creation-compose/samples`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/samples/) | alpha, rotate, scale modifier와 preview wrapper의 작은 공식 샘플이 있다. |
| [`TESTING_GUIDE.md`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/Documentation/TESTING_GUIDE.md) | Remote Compose source tree의 테스트 실행과 golden/update 절차를 설명한다. |
| [`remote-testing`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-testing/) | alpha14에 artifact는 존재하지만 `api/current.txt`에는 public signature가 없다. |

## 문서 포맷 공식 근거

- [고정 wire format 문서](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/Documentation/RemoteComposeWireFormat.md.html)
- [document structure](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/Documentation/parts/structure.md)

문서는 flat binary operation list로 저장되며 header, 선행 data operation, macro, layout tree 순서를 가진다. container와 matching end operation으로 tree가 복원된다. wire format 문서는 WIP이므로 장기 고정 공개 표준으로 간주하지 않는다.

## public와 restricted 경계

### public API로 확인

- `remote-creation-compose/api/current.txt`의 capture, remote composable, layout, modifier, state, action API
- `remote-tooling-preview/api/current.txt`의 preview API
- alpha14의 `RemoteDensityBehavior`, `RemoteCreationDisplayInfo.densityBehavior`

### artifact는 있으나 app-embedded public player contract가 아님

- [`RemoteDocumentPlayer.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-player-compose/src/main/java/androidx/compose/remote/player/compose/RemoteDocumentPlayer.kt)는 file과 function 모두 `RestrictTo(LIBRARY_GROUP)`다.
- [`RemoteComposePlayer.java`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-player-view/src/main/java/androidx/compose/remote/player/view/RemoteComposePlayer.java)는 class가 `RestrictTo(LIBRARY_GROUP)`다.
- `remote-player-compose`, `remote-player-view`, `remote-player-core`의 `api/current.txt`에는 public player signature가 없다.

### 이 저장소 POC가 사용하지만 restricted인 표면

- [`RcScope`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-core/src/main/java/androidx/compose/remote/creation/dsl/RcScope.kt)는 file과 interface가 `RestrictTo(LIBRARY_GROUP)`다.
- `createRcBuffer`, procedural `Modifier`, `RcSp`, `RcFloat` 기반 Ktor/JVM document builder 경로는 public Compose capture API와 구분해야 한다.
- AndroidX에는 Ktor 서버에서 document를 생성해 일반 Android 앱의 public embedded player로 재생하는 공식 codelab이나 supported end-to-end architecture가 현재 없다.

## alpha14 변경사항

공식 릴리스 노트에서 확인한 alpha14 변경:

- `RemoteDensityBehavior`와 density-aware `RemoteCreationDisplayInfo` 생성자를 public으로 공개
- `RemoteComposeCreationState.densityBehavior` 공개
- `RemoteComposeCreationComposeFlags` 공개
- standard Compose `CompositionLocal`을 `@RemoteComposable` 안에서 사용하는 경우 lint 경고 추가
- `compileSdk 37` 요구 제거
- font weight adjustment, border bounds, writer profile serialization, dynamic color background 수정
- restricted Compose player에 typeface resolver와 custom component support 추가

## 조사 결론

1. 처음 배우는 공식 경로는 `remote-creation-compose`의 remote composable 작성 → preview → capture → state/action 이해 순서가 맞다.
2. `18.rsp`, `24.rdp`, `RemoteDensityBehavior.Dp`가 공식 단위 경로다.
3. Ktor/JVM producer와 app-embedded playback은 가능한 POC지만 alpha14의 supported public end-to-end 경로라고 설명하면 안 된다.
4. `scaledSp`는 공식 API가 아니라 POC workaround다.
5. API 학습과 제품 도입 판단을 분리해야 한다.
