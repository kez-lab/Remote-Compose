# Remote Compose SDUI Lab

AndroidX Remote Compose `1.0.0-alpha14`로 만든 Android 앱 내부 server-driven UI POC다. 앱은 네이티브 서버 연결 화면에서 시작하고, 연결되면 JVM Ktor 서버가 현재 task 목록으로 생성한 Remote Compose 문서를 풀스크린으로 렌더링한다.

> 제품용 SDK 예제가 아니다. alpha14의 JVM builder와 embedded player 진입점에는 `@RestrictTo(LIBRARY_GROUP)`가 남아 있어 POC 파일에 제한 경고를 억제했다.

## 최종 사용자 흐름

```text
native connection screen
  └─ GET /document
       └─ full-screen Remote Compose task list
            ├─ row 선택 ── StateLayout detail screen
            ├─ 새 작업 작성 ── hostAction("task.create")
            │                    └─ Android TextField dialog
            │                         └─ POST /tasks → document auto reload
            └─ 삭제 ── hostAction("task.delete.<id>")
                         └─ DELETE /tasks/<id> → document auto reload
```

- 서버 URL·연결·입력창·API 호출은 Android host가 소유한다.
- 목록·스크롤·행·상세 화면은 Remote Compose 문서가 소유한다.
- 추가와 삭제가 성공하면 최신 문서를 자동으로 받으므로 수동 동기화 버튼은 없다.
- 시스템 뒤로가기는 remote screen을 닫고 연결 화면으로 복귀한다.

## 왜 입력창은 네이티브인가

alpha14 procedural DSL의 component surface에는 `Box`, `Column`, `Row`, `Flow`, `Text`, `Image`, `Canvas`, `StateLayout`과 scroll modifier는 있지만 일반 앱의 `TextField`처럼 IME text input을 받는 component는 확인되지 않았다.

따라서 Remote Compose의 `새 작업 작성` action이 Android host dialog를 열고, native `OutlinedTextField`가 입력을 받는다. 이 경계는 우회 사실을 UI 하단과 dialog 설명에 명시한다.

## 고정 4개 제한 제거

이전 4비트/16-state fixture를 제거했다. 서버의 `TaskStore`는 고정된 business count cap 없이 task를 저장하고, 문서를 요청할 때 현재 task 수만큼 row와 detail state를 선형으로 생성한다.

목록에는 `verticalScroll`을 적용했다. 12개 task 문서를 emulator에서 스크롤해 마지막 항목까지 확인했다.

다만 “물리적으로 무한”한 목록은 아니다. 문서에는 모든 row와 detail screen이 들어가므로 task 수에 따라 payload와 operation 수가 증가한다. 앱은 보안·안정성을 위해 기존 512 KiB document limit을 유지한다. alpha14에는 검증된 lazy/virtualized Remote Compose list가 없으므로 대규모 목록은 별도 pagination 또는 custom SDUI 비교가 필요하다.

## 상세 화면 Navigation

문서에 `screen` remote integer를 두고 root `StateLayout`의 0번을 목록, 1..N을 각 task 상세 화면으로 사용한다.

- 행의 왼쪽 영역 선택: `setValue(screen, detailIndex)`
- 상세의 `← 목록`: `setValue(screen, 0)`
- Android Navigation을 호출하지 않는 document-local 화면 전환
- 삭제하면 서버 mutation 후 새 문서를 받으므로 목록 화면으로 돌아온다.

## Host action과 API

| Remote action | Android 검증 | Ktor API | 결과 |
|---|---|---|---|
| `task.create` | 정확한 이름만 허용 | dialog 확인 후 `POST /tasks` | task 저장, 문서 reload |
| `task.delete.<id>` | 양의 정수 ID만 허용 | `DELETE /tasks/<id>` | task 삭제, 문서 reload |
| 그 외 이름 | 거부 | 요청 없음 | error 표시 |

기능 관점에서는 host action이 API 작업을 시작시키는 것이 맞다. 다만 `hostAction("task.create")` 자체가 HTTP 요청은 아니다. 문서에 저장된 **named-action event**를 player가 Android callback으로 올리고, 앱 코드가 그 문자열을 command와 Ktor 요청으로 번역한다.

```text
Remote document click
  → hostAction("task.create") operation 실행
  → RemoteDocumentPlayer.onNamedAction("task.create", value, stateUpdater)
  → RemoteSduiScreen.onHostAction
  → RemoteSduiViewModel.handleHostAction
  → HostActionRouter.commandFor("task.create")
  → CreateTask command
  → native TextField dialog 표시
  → 사용자 submit
  → RemoteSduiViewModel.createTask(title)
  → Ktor client POST /tasks
  → 성공 시 GET /document
```

player와 Android를 연결하는 실제 callback은 다음 한 줄이다.

```kotlin
RemoteDocumentPlayer(
  document = document,
  onNamedAction = { name, value, _ -> onHostAction(name, value) },
)
```

`MainActivity`는 이 callback을 `viewModel::handleHostAction`에 연결한다. `HostActionRouter`는 exact `task.create`를 `CreateTask`, 양의 정수 suffix가 있는 `task.delete.<id>`를 `DeleteTask(id)`로 바꾸며 나머지는 거부한다.

create는 입력값이 필요하므로 host action 시점에는 dialog만 연다. 사용자가 제출한 뒤에야 `viewModelScope.launch` 안에서 `POST /tasks`가 실행된다. delete는 ID가 action 이름에 있으므로 router 통과 직후 `DELETE /tasks/{id}`를 실행한다. alpha14 procedural `hostAction`은 이 POC에서 이름만 전달하므로 delete ID를 이름에 넣었고, callback의 `value`와 `StateUpdater`는 사용하지 않았다.

따라서 Remote Compose document가 임의 URL이나 Android suspend 함수를 직접 실행하는 것은 아니다. **서버 문서는 command 이름을 요청하고, Android host가 그 이름을 어떤 UI·UseCase·API에 연결할지 결정한다.**

## Density 보정

alpha14 procedural DSL의 `fontSize = n.rsp`와 exact `height/width`는 DP header만으로 일관되게 scaling되지 않았다. 현재 문서는 player의 `density()` system value로 font와 fixed dimension을 계산한다.

```kotlin
context(density: RcFloat)
private val Int.scaledSp: RcSp
  get() = RcSp((density * toFloat()).toFloat())

context(density: RcFloat)
private val Int.scaledSize: RcFloat
  get() = (density * toFloat()).flush()

context(density()) {
  Text("작업", fontSize = 18.scaledSp)
  Modifier.height(76.scaledSize)
}
```

Kotlin `2.3.20`의 experimental context parameter를 POC 범위에서 활성화해 동일한 density를 반복 전달하지 않는다. `RcScope` 자체를 context로 사용하면 Remote Compose DSL receiver를 shadow하므로, 더 좁은 `RcFloat` density만 context로 제공한다. 별도 metrics DTO 없이 각 UI 선언 위치에서 크기를 바로 읽을 수 있으며, `server/build.gradle.kts`는 `-Xcontext-parameters`를 명시한다.

## 실행

서버는 독립 Gradle project이므로 `server/`에서 Android app을 구성하지 않고 직접 빌드·실행할 수 있다.

```bash
cd server
./gradlew build
./gradlew run
```

상위 `remote-state-lab`에서는 composite build task 경로도 그대로 사용할 수 있다.

```bash
./gradlew :server:run
```

다른 터미널에서:

```bash
./gradlew :app:assembleDebug
android describe --project_dir=.
android run --device=emulator-5554 \
  --apks=app/build/outputs/apk/debug/app-debug.apk \
  --activity=.MainActivity
```

에뮬레이터 기본 URL은 `http://10.0.2.2:8080`이다. `Address already in use`가 나오면 다음 명령으로 기존 8080 서버를 확인한다.

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
```

## 검증

```bash
./gradlew :server:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

2026-07-11 `emulator-5554`에서 확인한 여정:

1. 연결 후 3개 task의 Remote Compose 목록 표시
2. 첫 row 선택 → Remote Compose 상세 → `← 목록`
3. `새 작업 작성` → Android TextField에 직접 입력
4. 저장 후 `R1·3개 → R2·4개` 자동 갱신
5. 서버에 12개 task를 저장한 문서의 scroll과 마지막 row 확인
6. 입력한 task의 상세 화면과 server ID 확인
7. 상세에서 삭제 후 `R10·12개 → R11·11개` 자동 갱신

증거: [`raw/verification/2026-07-11-dynamic-task-flow`](raw/verification/2026-07-11-dynamic-task-flow/README.md)

## 주요 코드

- `server/.../TaskStore.kt`: server task identity, revision, add/delete
- `server/.../ChecklistDocument.kt`: 동적 row, scroll, `StateLayout` 상세 화면
- `server/.../Server.kt`: `GET /document`, `POST /tasks`, `DELETE /tasks/{id}`
- `server/settings.gradle.kts`: Android app과 분리된 standalone server build
- `app/.../HostActionRouter.kt`: create/delete action allowlist와 ID validation
- `app/.../RemoteSduiViewModel.kt`: native input state, Ktor mutation, 자동 document reload
- `app/.../RemoteSduiScreen.kt`: 연결 화면, embedded player, native task editor dialog

alpha14 제한은 [`reference/wiki/alpha14-debugging-and-component-issues.md`](../../reference/wiki/alpha14-debugging-and-component-issues.md)에 정리돼 있다.
