---
title: Ktor와 RcScope로 배우는 서버 중심 SDUI 학습 경로
type: synthesis
created: 2026-07-13
updated: 2026-07-13
as_of: 2026-07-13
confidence: high
sources:
  - ../raw/androidx-remote-compose-official-2026-07-12.md
  - ../raw/remote-compose-api-syntax-audit-2026-07-12.md
  - ../raw/remote-compose-document-anatomy-2026-07-12.md
  - ../../samples/remote-state-lab/server/src/main/kotlin/com/example/remotestatelab/server/ChecklistDocument.kt
  - ../../samples/remote-state-lab/server/src/main/kotlin/com/example/remotestatelab/server/Server.kt
---

# Ktor와 RcScope로 배우는 서버 중심 SDUI 학습 경로

이 페이지는 HTML 코드랩의 기준 경로를 고정한다.

```text
Ktor/JVM server
  → RcScope procedural DSL
  → Remote Compose binary document
  → HTTP ByteArray
  → Android embedded player
  → 화면
```

이 경로는 현재 저장소 POC가 실제로 실행하는 구조다. Ktor와 Remote Compose를 결합한 공식 Codelab이라는 뜻은 아니다. alpha14의 `RcScope`, `createRcBuffer`, embedded player는 고정 source 기준 restricted API이므로 제품용 안정 계약으로 간주하지 않는다.

## 왜 본편에서 RemoteText를 제외했는가

`RemoteText`, `RemoteColumn`, `.rs`, `.rb`, `valueChange`, `captureSingleRemoteDocument`는 `remote-creation-compose` frontend의 API다. Android `Context`와 Compose capture 환경에서 document를 만드는 별도 경로다.

현재 서버 sample은 `createRcBuffer { ... }`의 `RcScope` receiver에서 `Text(String)`, `Column`, `StateLayout`, `setValue`, `hostAction(String)`을 사용한다. 이 경로에는 `RemoteText`도 `.rs/.rb`도 필요하지 않다.

두 frontend를 초보 본문에 동시에 제시하면 다음 세 질문이 섞인다.

1. 누가 UI source를 작성하는가?
2. 어디에서 binary document를 생성하는가?
3. 어느 시스템이 요청 시점의 화면을 결정하는가?

이 코드랩의 답은 모두 서버 경로에 맞춰 고정한다. Android Compose capture frontend는 사전 생성 artifact를 서버/CDN에 배포하는 방식을 검토할 때만 [별도 심화 문서](remote-state-and-values.md)에서 다룬다.

## 코드랩의 10단계

1. 완성 화면을 먼저 보고 연결·목록·입력·상세 사용자 흐름을 확인한다.
2. 일반 data-driven UI와 server-driven UI의 화면 결정권 차이를 이해한다.
3. 독립 Ktor server와 Android sample을 실행한다.
4. 서버의 `RcScope.Column`과 `RcScope.Text`로 첫 UI operation을 기록한다.
5. Profile과 Header, `createRcBuffer`가 자동 생성하는 root, 그 아래 content가 `ByteArray`가 되는 과정을 배운다.
6. `remoteNamedInteger`, `StateLayout`, `setValue`로 document-local state를 바꾼다.
7. `screen` state와 child index로 목록/상세를 교체하고 Android Navigation과 구분한다.
8. `hostAction(String)` → Android callback → ViewModel → Repository/API 경계를 연결한다.
9. Ktor `respondBytes`가 현재 server data로 만든 document를 전송하는 역할임을 확인한다.
10. restricted alpha API, 보안, fallback, 접근성, 호환성, rollback과 다른 생성 frontend를 분리해 평가한다.

## 기본 UI API 대응

| UI 의도 | Jetpack Compose | 현재 server RcScope 경로 | 중요한 차이 |
|---|---|---|---|
| 세로 layout | `Column` | `RcScope.Column` | 후자는 document operation 기록 |
| text | `Text` | `RcScope.Text(String)` | 후자는 Android `@Composable`이 아님 |
| modifier | `androidx.compose.ui.Modifier` | remote DSL `Modifier` | type과 지원 operation이 다름 |
| click | Kotlin callback | `onClick { RcActionScope }` | 임의 callback이 아니라 serializable action 기록 |
| local selector state | `mutableStateOf` | `remoteNamedInteger` | recomposition이 아니라 player ID 평가 |
| state 변경 | Kotlin assignment | `setValue` | document action으로 기록 |
| host 요청 | ViewModel 함수 호출 | `hostAction(String)` | 이름만 host로 전달; 앱이 해석 |

## 상태와 화면 전환

```kotlin
val screen = remoteNamedInteger("screen", 0)

StateLayout(stateIndex = screen) {
    TaskListScreen(snapshot, screen) // child 0
    snapshot.tasks.forEach { task ->
        TaskDetailScreen(snapshot, task, screen) // child 1..N
    }
}
```

서버는 snapshot의 task 수만큼 child를 문서 생성 시점에 기록한다. Player는 `screen` ID의 값과 같은 child 하나를 표시한다. `setValue(screen, index + 1)`은 상세 UI를 목록 위에 덮는 명령이 아니라 selector 값을 바꾸는 action이다.

## hostAction이 API가 되는 경계

```text
RcScope.hostAction("task.create") 기록
  → Player onNamedAction callback
  → Android allowlist router
  → ViewModel이 native 입력 UI 요청
  → Repository가 POST /tasks
  → GET /document 재요청
  → 새 document로 player 갱신
```

`hostAction`은 HTTP client도 suspend callback도 아니다. 문서에 event 이름을 기록한다. API 요청이 발생하는 이유는 Android host가 그 이름을 허용된 command로 번역했기 때문이다.

## 사실, 추론, 권고

- **확인된 사실:** 현재 sample server는 procedural `RcScope` DSL로 요청 시점의 task snapshot을 binary document로 만든다.
- **확인된 사실:** alpha14 고정 source에서 procedural builder와 player는 restricted다.
- **확인된 사실:** Ktor route는 `respondBytes`로 document를 전달하며 공식 Remote Compose 전용 Ktor 통합은 확인하지 못했다.
- **공학적 권고:** 하나의 beginner 코드랩에서는 제작 경로를 하나만 선택하고 다른 frontend 문법을 본문에 섞지 않는다.
- **공학적 권고:** public Compose capture는 “공식이므로 먼저”가 아니라 배포 모델이 Android/CI 사전 생성일 때 선택하는 심화 경로로 다룬다.

## 다음 읽기

- [Android Compose 생성 frontend와 remote value 심화](remote-state-and-values.md)
- [Document Anatomy와 State Lifecycle](document-anatomy-and-state.md)
- [alpha14 API 문법 감사](api-syntax-audit.md)
- [Ktor transport](ktor-transport.md)
- [POC 회고와 도입 판단](remote-compose-poc-retrospective.md)
