---
title: Remote Compose alpha14 API syntax audit 2026-07-12
type: source
created: 2026-07-12
updated: 2026-07-12
status: immutable
as_of: 2026-07-12
---

# Remote Compose alpha14 API 문법 감사 — 2026-07-12

## 감사 목적

HTML 실습과 Wiki의 `.rs`, `.rb`, `valueChange`, `RemoteStateLayout`, capture 예제가 실제 `1.0.0-alpha14` API와 일치하는지 다시 확인했다. Android Developers API reference만 믿지 않고 release commit에 고정한 `api/current.txt`, 구현 source, 로컬 Gradle cache의 배포 AAR bytecode를 교차 확인했다.

## 확인한 원자료

- [alpha14 고정 `api/current.txt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/api/current.txt)
- [alpha14 고정 `RemoteBoolean.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/state/RemoteBoolean.kt)
- [alpha14 고정 `RemoteString.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/state/RemoteString.kt)
- [alpha14 고정 `ValueChange.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/action/ValueChange.kt)
- [alpha14 고정 `RemoteStateLayout.kt`](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/layout/RemoteStateLayout.kt)
- [Android Developers `RemoteText`](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/layout/RemoteText.composable)
- [Android Developers state package](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/state/package-summary)
- [Android Developers action package](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/action/package-summary)

로컬 Google Maven AAR도 확인했다.

- artifact: `androidx.compose.remote:remote-creation-compose:1.0.0-alpha14`
- SHA-256: `538fa86277dd65795170d236ae99337de88d8406c38753a1cca1da2296bc2a0c`
- bytecode 확인: `RemoteBooleanKt.getRb(boolean)`, `RemoteStringKt.getRs(String)`, `ValueChangeKt.valueChange(...)`, Boolean/Enum/Int `RemoteStateLayout(...)`

## `.rs`와 `.rb` 판정

| 표현 위치 | 받는 타입 | 전달한 값 | 변환 필요 여부 |
|---|---|---|---|
| `rememberMutableRemoteBoolean(false)` | `Boolean` | `Boolean` | `.rb` 불필요 |
| `RemoteStateLayout { isDone -> ... }` | lambda에 `Boolean` 전달 | `Boolean` | 조건문에는 `.rb` 불필요 |
| `RemoteText(text = "완료".rs)` | `RemoteString` | `String` | `.rs` 필요 |
| `valueChange(done, (!isDone).rb)` | `RemoteState<Boolean>` | `Boolean` | `.rb` 필요 |
| `hostAction("task.create".rs)` | `RemoteString` | `String` | `.rs` 필요 |
| `valueChange(screen, 1.ri)` | `RemoteState<Int>` | `Int` | `.ri` 필요 |

`Boolean.rb`와 `String.rs`는 alpha13부터 공개된 extension property다. Kotlin implicit conversion이 아니므로 target API가 remote type만 받는 위치에서는 명시적으로 사용한다.

## 공식 문서 간 모순

Android Developers action package는 확인 시점에 함수 이름을 `ValueChange`로 표시했다. 그러나 alpha14 release commit의 `api/current.txt`, `ValueChange.kt`, 배포 AAR bytecode는 모두 소문자 `valueChange`다. 이 저장소의 alpha14 코드는 소문자 표기가 맞다. moving API reference와 released artifact가 충돌할 때는 version-pinned signature와 실제 artifact를 우선한다.

## 저장소 문서에서 찾은 문제

1. HTML과 Wiki의 `captureSingleRemoteDocument` 예제는 suspend API를 일반 top-level 코드처럼 보여 줬다. `suspend fun` 또는 coroutine 문맥을 보여 줘야 copy/paste 가능한 예제가 된다.
2. HTML 화면 전환 단계는 public Compose `RemoteStateLayout` 예제와 실제 Ktor POC의 restricted procedural `StateLayout`을 같은 구현처럼 표현했다. 두 API는 selector 개념은 비슷하지만 작성 surface가 다르다.
3. `18.rsp` 설명은 official remote type이라는 점은 맞지만 생성 환경의 `RemoteDensity`와 font scale로 pixel 값이 정해진다는 제한을 충분히 설명하지 않았다. 재생 기기의 Android `sp`처럼 자동 적응한다고 읽히면 안 된다.
4. HTML의 목록/상세 예제는 `TaskList`와 `TaskDetail` helper signature를 생략한 축약 예제인데 copy/paste 가능한 완성 코드처럼 보였다.
5. HTML의 “공식 Text API” 링크가 `RemoteText`가 선언된 layout package가 아니라 text style package를 가리켰다.
6. `document-anatomy-and-state.md`의 첫 문장은 anatomy를 초심자의 선행 지식으로 요구해 현재 beginner-first 학습 경로와 충돌했다.
7. 기존 공식 근거 스냅샷의 primitive conversion 목록은 `.rb`를 누락해 불완전했다. 원본 스냅샷은 immutable이므로 이 파일로 보완한다.

## 오류가 아닌 것으로 확인한 항목

- `"완료".rs`, `(!isDone).rb`, `1.ri`, `18.rsp`
- alpha14의 소문자 `valueChange`
- Boolean, Enum, Int overload를 가진 public `RemoteStateLayout`
- `hostAction`이 `RemoteString` name을 받는다는 설명

## 남은 검증 경계

API signature와 배포 bytecode 일치는 확인했다. public Compose 예제를 별도 compile fixture와 player golden test로 실행하는 자동화는 아직 없다. 향후 alpha 업데이트 때 documentation page, pinned `api/current.txt`, Maven artifact를 다시 교차 검증해야 한다.
