---
title: Remote Compose documentation audit evidence 2026-07-13
type: source
created: 2026-07-13
updated: 2026-07-13
status: immutable
as_of: 2026-07-13
---

# Remote Compose 문서 전문 감사 근거 — 2026-07-13

## 감사 범위

- root README, HTML Codelab, maintained Wiki, sample README
- public Compose code block compile 가능성
- GitHub Pages static asset와 link 구조

## 현재 버전 재확인

| 출처 | 2026-07-13 확인 결과 |
|---|---|
| [Remote Compose release notes](https://developer.android.com/jetpack/androidx/releases/compose-remote) | dependency 예제가 `1.0.0-alpha14`를 사용한다. |
| [AndroidX versions](https://developer.android.com/jetpack/androidx/versions) | `compose.remote` 최신 alpha가 2026-07-01의 `1.0.0-alpha14`다. |
| [Google Maven metadata](https://dl.google.com/dl/android/maven2/androidx/compose/remote/remote-core/maven-metadata.xml) | alpha14 artifact 배포를 확인했다. |

검색 결과 요약 cache가 일시적으로 alpha13을 노출한 경우가 있었지만, 실제 release page dependency block, AndroidX versions, Maven artifact를 교차 확인해 alpha14를 기준으로 유지했다.

## 배포 source artifact

- `remote-creation-compose-1.0.0-alpha14-sources.jar`
  - SHA-256: `55cac040c614333361458271bc086a9acfc649f60ab30c00abc3749676a746e9`
- `remote-creation-core-1.0.0-alpha14-sources.jar`
  - SHA-256: `c652cd1561a8869d4db80fe6abc381ed646aaa067faa2bae53624dd8e16a0ce3`
- pinned AndroidX commit: `19660b9e1b2fec4a9528fe80ce0a432c0fa2f825`

## 발견하고 수정한 설명 오류

### 1. 코드랩을 “공식 API Codelab”이라고 부름

- 문제: root README가 restricted `RcScope`와 player를 사용하는 저장소 POC를 공식 API 코드랩이라고 표시했다.
- 근거: `RcScope.kt`, `RcDocCreator.kt`, `RemoteDocumentPlayer.kt`는 `RestrictTo(LIBRARY_GROUP)`다.
- 수정: “Ktor와 RcScope 서버 중심 SDUI 코드랩”으로 바꾸고 Google 공식 Codelab이 아님을 표시했다.

### 2. `createRcBuffer`의 root 생성 설명

- 문제: content block의 첫 layout이 root라고 설명했다.
- 근거: alpha14 `RcDocCreator.kt`의 `createRcBuffer`는 `writer.root { scope.content() }`를 호출한다. `createRawRcBuffer`만 root를 만들지 않는다.
- 수정: `createRcBuffer`가 `RootLayoutComponent`를 자동 생성하고 block operation은 그 아래 content가 된다고 정정했다.

### 3. procedural `StateLayout`에 `Text`를 직접 배치

- 문제: 토글 예제가 `StateLayout` 바로 아래에 `Text` 두 개를 기록했다.
- 근거: player core `StateLayout.getLayout(index)`는 child 중 `LayoutComponent`이며 `LayoutManager`인 항목을 선택한다. 첫 layout이 없으면 runtime exception을 발생시킨다.
- 수정: 각 state를 `Box` layout으로 감싸고 layout child index임을 명시했다.

### 4. `RemoteDocumentPlayer` 필수 인자 누락

- 문제: Codelab, sample README, POC 회고의 player snippet이 `documentWidth`와 `documentHeight`를 생략했다.
- 근거: alpha14 `RemoteDocumentPlayer` signature에서 두 인자는 default가 없는 필수 `Int`다.
- 수정: 실제 sample과 같은 `390`, `720`을 추가했다.

### 5. `rsp`를 capture-time 고정이라고 절대화

- 문제: public Compose의 `RemoteTextUnit`이 항상 생성 환경 density로 pixel에 고정된다고 설명했다.
- 근거: 기본 `captureSingleRemoteDocument`는 creation display info의 density/font scale을 사용한다. 그러나 public `RemoteDensity.Host`는 player의 `FLOAT_DENSITY`와 system font-size expression으로 density/font scale을 구성한다.
- 수정: 기본 동작과 명시적 host-adaptive 선택을 나누고 전달한 `remoteDensity`를 함께 기록하도록 했다.

## 재확인되어 유지한 주장

- `String.rs`, `Boolean.rb`, `Int.ri`, `Int.rsp`, 소문자 `valueChange`는 alpha14 배포 source와 일치한다.
- procedural `RcScope.Text(String)`에는 `.rs`가 필요하지 않다.
- procedural `RcActionScope.hostAction`은 이름만 기록하며 HTTP나 suspend 함수를 실행하지 않는다.
- public `RemoteStateLayout`과 procedural `StateLayout`은 서로 다른 API다.
- embedded player, `RcScope`, `createRcBuffer`는 alpha14 고정 source 기준 restricted다.
- procedural `RcScope`에는 일반 IME `TextField` surface가 없다.
- Ktor와 Remote Compose의 supported end-to-end 공식 Codelab은 확인하지 못했다.

## 자동 검증

- `scripts/audit_docs.py`: link, Wiki index, Codelab step/ID, frontend 경계, root, StateLayout layout child, player dimensions
- `samples/remote-state-lab/docs-api-fixture`: public Compose creation/state/action/capture snippet을 alpha14로 compile

## 남은 한계

- public Compose fixture는 compile 검증이며 실제 player golden 비교는 아직 없다.
- restricted API는 다음 alpha에서 source/binary incompatible하게 바뀔 수 있다.
- “공식 Ktor 통합이 없다”는 부재 주장은 AndroidX/Ktor 공식 문서 검색 범위에 한정된다.
- GitHub Pages는 학습 문서 배포 수단이며 Remote Compose document server가 아니다.
