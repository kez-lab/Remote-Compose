---
title: Ktor to Android Remote Compose SDUI POC
type: engineering
created: 2026-07-11
updated: 2026-07-11
as_of: 2026-07-11
confidence: high
sources:
  - ../raw/remote-sdui-poc-2026-07-11.md
  - ../raw/compose-remote-alpha14-debugging-2026-07-11.md
  - ../raw/remote-checklist-ux-verification-2026-07-11.md
  - ../raw/remote-dynamic-task-flow-verification-2026-07-11.md
---

# Ktor → Android Remote Compose SDUI POC

실행 코드는 [`samples/remote-state-lab`](../../samples/remote-state-lab/README.md)에 있다.

## 현재 샘플

Android app의 native 연결 화면이 Ktor document를 받고 검증한 뒤, Remote Compose가 server-backed task 목록과 상세 화면을 풀스크린으로 그린다.

```text
native connection
  → GET /document
  → Remote Compose list + vertical scroll
      ├─ row → StateLayout detail → list
      ├─ create named action → native TextField → POST /tasks → reload
      └─ delete named action → DELETE /tasks/{id} → reload
```

## alpha14 입력 경계

alpha14 procedural DSL/source에서 일반 앱의 `TextField`에 해당하는 IME text-entry component surface를 확인하지 못했다. `새 작업 작성`은 remote host action이 native Android dialog를 열어 해결한다.

이 구분을 숨기지 않고 UI와 문서에 명시한다.

- Android: TextField, keyboard, validation, API request
- Remote Compose: list, scroll, task row, detail screen, document navigation

## 고정 목록 제거

과거 POC의 4비트/16-state fixture는 삭제했다. Ktor `TaskStore`에 현재 task를 저장하고 `/document` 요청마다 task 수만큼 row와 detail state를 생성한다.

task 수 N에 대한 문서 구조는 대략 O(N)이다.

- list screen: N개 row
- detail screen: N개 direct `StateLayout` state
- stable ID: server positive integer

12개 task 문서와 scroll을 emulator에서 검증했다. 고정된 네 개의 business cap은 없지만 512 KiB client document limit과 player operation limit은 유지한다. 현재 문서는 lazy virtualization이 아니라 전체 row/detail을 담는 growing finite document다.

## Navigation

root `StateLayout`의 direct integer `screen`을 사용한다.

| index | screen |
|---:|---|
| 0 | task list |
| 1..N | server snapshot 순서의 task detail |

행 선택과 `← 목록`은 player-local `setValue(screen, index)`로 실행된다. Android Navigation을 호출하지 않는다. 상세 화면에는 server ID, revision, 설명을 표시한다.

## API와 자동 갱신

| named action | Android validation | API |
|---|---|---|
| `task.create` | exact match | dialog submit → `POST /tasks` |
| `task.delete.<id>` | positive integer suffix | `DELETE /tasks/{id}` |

mutation 성공 후 Android가 `/document`를 자동으로 다시 가져온다. 이전 “서버 동기화” 버튼은 역할이 불분명하고 필요하지 않아 제거했다.

`hostAction`은 API client가 아니라 document에서 host로 보내는 named event다. 연결 과정은 다음과 같다.

```text
server document의 hostAction(name)
  → player가 click operation 평가
  → RemoteDocumentPlayer.onNamedAction(name, value, stateUpdater)
  → app의 onHostAction callback
  → RemoteSduiViewModel.handleHostAction(name, value)
  → HostActionRouter.commandFor(name)
  → 허용된 command만 native UI 또는 Ktor client로 전달
```

실제 player adapter는 `onNamedAction = { name, value, _ -> onHostAction(name, value) }`로 연결되고, `MainActivity`가 `onHostAction = viewModel::handleHostAction`을 주입한다.

create와 delete의 실행 시점은 다르다.

1. `task.create`: router가 `CreateTask`로 변환 → ViewModel이 native editor state를 연다 → 사용자 submit과 title validation 뒤 `viewModelScope.launch`에서 `POST /tasks` → 성공 시 `GET /document`.
2. `task.delete.<id>`: router가 suffix를 양의 정수로 검증해 `DeleteTask(id)`로 변환 → `viewModelScope.launch`에서 `DELETE /tasks/{id}` → 성공 시 `GET /document`.
3. unknown name 또는 잘못된 ID: router가 `null`을 반환하고 API를 호출하지 않는다.

따라서 “host action으로 API를 요청한다”는 제품 수준 설명은 맞지만, endpoint·HTTP method·coroutine을 결정하는 주체는 Remote document가 아니라 Android host다. 이 POC의 procedural `hostAction`은 이름만 보내므로 delete ID를 문자열 suffix에 포함했고 callback의 `value`와 `StateUpdater`는 사용하지 않았다.

## 2026-07-11 Android journey

1. 3개 server task 목록과 scrollable semantics 확인
2. row → Remote Compose detail → list 전환
3. remote create action → native TextField dialog
4. 직접 입력한 task 저장 후 `R1·3개 → R2·4개`
5. 12개 server task 문서 scroll과 마지막 row 확인
6. 입력한 task #4 상세 화면 확인
7. 상세에서 삭제 후 `R10·12개 → R11·11개`

증거: [직접 입력·가변 목록·상세 화면 검증](../raw/remote-dynamic-task-flow-verification-2026-07-11.md)

## 중요한 한계

1. embedded player와 JVM procedural builder는 alpha14 restricted API다.
2. remote TextField/IME component가 없어 입력은 native host가 담당한다.
3. scroll은 확인했지만 lazy/virtualized remote list 계약은 확인하지 못했다.
4. task 수에 비례해 document payload와 operation이 증가한다.
5. process restart 시 in-memory Ktor `TaskStore`는 초기화된다.
6. refresh 뒤 detail/scroll local state는 보존하지 않는다.
7. malformed/fuzz, TalkBack, font scale, RTL, locale matrix는 아직 미검증이다.

## Production으로 가기 위한 다음 단계

- disk/database task persistence
- pagination 또는 supported lazy remote list 확인
- typed host action payload와 state restore
- document/profile/version negotiation
- signed envelope, last-known-good, fallback
- malformed/resource exhaustion tests
- accessibility와 density device matrix
- custom JSON/Proto SDUI renderer와 비교

제품 gate는 [파일럿 계획](pilot-plan.md), [보안과 신뢰성](security-reliability.md), [alpha14 문제 레지스터](alpha14-debugging-and-component-issues.md)를 따른다.
