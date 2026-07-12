---
title: Remote Compose API 문법과 예제 감사
type: maintenance
created: 2026-07-12
updated: 2026-07-13
as_of: 2026-07-13
confidence: high
sources:
  - ../raw/remote-compose-api-syntax-audit-2026-07-12.md
  - ../raw/androidx-remote-compose-official-2026-07-12.md
---

# Remote Compose API 문법과 예제 감사

이 페이지는 Codelab과 Wiki의 코드가 `androidx.compose.remote:remote-creation-compose:1.0.0-alpha14`에 실제로 존재하는지 확인한 결과다. 판정 순서는 배포 AAR bytecode → alpha14 고정 `api/current.txt`와 source → Android Developers moving reference다.

## 질문에 대한 결론

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

이 코드에서 `.rs`와 `.rb`는 둘 다 필요하다.

- `rememberMutableRemoteBoolean`의 초기값은 일반 `Boolean`이므로 `false` 그대로 전달한다.
- Boolean `RemoteStateLayout`의 content parameter인 `isDone`도 일반 `Boolean`이다.
- `RemoteText.text`는 `RemoteString`만 받으므로 문자열 literal을 `.rs`로 바꾼다.
- `valueChange`의 updated value는 `RemoteState<Boolean>`이어야 하므로 `(!isDone).rb`가 필요하다.

`.rb`는 Boolean을 무조건 꾸미는 문법이 아니다. **remote type을 요구하는 parameter boundary에서만** 사용한다.

## alpha14에서 사용할 표기

| 목적 | alpha14 표기 | 근거 |
|---|---|---|
| 문자열 literal | `"text".rs` | `String.rs: RemoteString` |
| Boolean literal | `true.rb` | `Boolean.rb: RemoteBoolean` |
| integer literal | `1.ri` | `Int.ri: RemoteInt` |
| text size | `18.rsp` | `Int.rsp: RemoteTextUnit` |
| mutable state 변경 | `valueChange(state, remoteValue)` | 배포 AAR와 고정 signature는 소문자 |

Android Developers action reference가 대문자 `ValueChange`를 표시하는 모순이 있다. alpha14 artifact 기준으로는 소문자 `valueChange`가 맞다.

## 감사에서 수정한 문서 문제

| 문제 | 영향 | 정정 |
|---|---|---|
| capture 예제에 suspend 문맥 없음 | 그대로 복사하면 compile 불가 | `suspend fun captureTaskList(...)`로 감쌈 |
| public `RemoteStateLayout`과 procedural `StateLayout` 혼합 | POC 작성 API를 오해 | selector 개념만 같고 API surface는 다름을 표시 |
| `rsp`의 density 경계 누락 | 기본 capture와 host-adaptive density를 혼동 | 기본은 creation display info, 선택적으로 `RemoteDensity.Host`를 전달할 수 있음을 명시 |
| helper가 생략된 화면 전환 예제 | 완성 코드처럼 오해 | 축약 예제 badge와 helper 생략 설명 추가 |
| `RemoteText` source link 오류 | 잘못된 package로 이동 | layout의 `RemoteText` reference로 교체 |
| anatomy-first 문장 | beginner 학습 경로와 충돌 | anatomy를 심화 과정으로 이동 |

## 설명 방식에서 저지른 실수

1. API signature가 맞는지만 확인하고 왜 remote type이 존재하는지 설명하지 않았다.
2. `.rs`와 `.rb`를 suffix 암기 문제로 축소하고 Kotlin 값이 document data/expression이 되는 과정을 생략했다.
3. `RemoteStateLayout` content lambda가 capture 중 모든 child를 만든다는 사실을 설명하지 않아 runtime callback처럼 읽히게 했다.
4. Jetpack Compose recomposition과 player의 ID/listener 기반 갱신을 나란히 비교하지 않았다.
5. public `RemoteText`와 서버의 restricted `RcScope.Text`가 서로 다른 frontend라는 핵심을 뒤늦게 설명했다.
6. 실제 server sample과 public Compose 문법의 타입, artifact, 공개 범위를 한 표로 연결하지 않았다.
7. 공식 출처 badge를 붙인 것을 학습 설명이 충분하다는 증거로 잘못 취급했다.
8. 실제 목표가 Ktor/JVM procedural SDUI인데 public Compose frontend를 본문 기본 경로로 배치해 제작 위치와 API 선택을 뒤섞었다.
9. `RemoteText`, `.rs`, `.rb`를 정확히 설명하는 것과 해당 코드랩에 필요한 내용을 구분하지 못했다. 정확하지만 범위 밖인 정보도 초보자에게는 혼란이 된다.

재발 방지 규칙은 저장소 `AGENTS.md`의 `Beginner documentation rules`와 [Frontend, Remote Value, State 실행 모델](remote-state-and-values.md)에 고정했다.

## 앞으로의 검증 규칙

1. 코드 블록마다 `공식 compile 가능`, `공식 API pattern`, `restricted POC`, `pseudo-code`를 구분한다.
2. suspend 여부와 필요한 receiver/import를 생략하면 축약 예제라고 명시한다.
3. alpha API는 moving reference 하나만 믿지 않고 pinned signature와 Maven artifact를 함께 확인한다.
4. public Compose capture와 restricted Ktor/JVM procedural builder의 이름이 비슷해도 같은 API로 설명하지 않는다.
5. 다음 단계는 public 예제를 실제로 compile하는 전용 fixture와 player golden test를 추가하는 것이다.
6. beginner 문서는 먼저 하나의 producer 경로를 고정한다. 다른 frontend 비교와 값 변환은 별도 심화 과정으로 연결한다.
7. Ktor/RcScope 본편의 state 예제는 실제 sample과 같은 `remoteNamedInteger`, `StateLayout`, `setValue`, `hostAction(String)`을 사용한다.

상세 증거는 [2026-07-12 alpha14 API 문법 감사](../raw/remote-compose-api-syntax-audit-2026-07-12.md)에 있다.
