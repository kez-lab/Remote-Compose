---
title: Remote Compose SDUI POC Evidence
type: raw-evidence
created: 2026-07-11
as_of: 2026-07-11
---

# Remote Compose SDUI POC 증거 — 2026-07-11

## 외부 자료

### AndroidX Remote Compose release notes

- URL: https://developer.android.com/jetpack/androidx/releases/compose-remote
- 확인일: 2026-07-11
- 확인 사실:
  - 최신 릴리스는 2026-07-01의 `1.0.0-alpha14`다.
  - Google Maven에서 `androidx.compose.remote:remote-*:1.0.0-alpha14`로 배포된다.
  - alpha13에서 `RemoteStateLayout`, `RemoteInt` 비교 연산자, `captureRemoteDocument`/`captureSingleRemoteDocument`, `valueChange`가 공개 API 방향으로 정리됐다.
  - alpha12에서 외부 사용을 위해 `hostAction`과 `combinedAction` factory가 노출됐다.
  - alpha14는 `RemoteDocumentPlayer`에 typeface resolver와 custom component 지원을 추가했다.
  - alpha14에서 compileSdk 37 요구가 삭제됐다.

### ProAndroidDev article

- URL: https://proandroiddev.com/remotecompose-another-paradigm-for-server-driven-ui-in-jetpack-compose-92186619ba8f
- 게시일: 2025-11-29
- 확인일: 2026-07-11
- 유효한 개념:
  - document creation과 playback 분리
  - `ByteArray` 전송, local state/expression, host action
  - critical native UI와 remote surface를 섞는 hybrid architecture
  - document cache와 preload 필요성
- 현재와 다른 점:
  - 글은 snapshot Maven만 가능하다고 했지만 현재는 Google Maven에 alpha14가 배포됐다.
  - 일반 Compose를 그대로 capture할 수 있다는 설명은 최신 remote applier의 `Remote*` API 강제 방향과 차이가 있다.
  - 글의 product-ready 뉘앙스와 달리 embedded player의 핵심 API는 alpha14 source에서 제한 상태다.

## 고정 소스 증거

alpha14 릴리스 커밋: `19660b9e1b2fec4a9528fe80ce0a432c0fa2f825`

- `RemoteDocumentPlayer.kt`
  - https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-player-compose/src/main/java/androidx/compose/remote/player/compose/RemoteDocumentPlayer.kt
  - 파일과 composable 모두 `RestrictTo(LIBRARY_GROUP)`다.
  - named action callback은 `name`, optional value, `StateUpdater`를 host에 전달한다.
- `RemoteComposePlayer.java`
  - https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-player-view/src/main/java/androidx/compose/remote/player/view/RemoteComposePlayer.java
  - player class 자체가 `RestrictTo(LIBRARY_GROUP)`다.
  - document version 검사와 operation/image/bitmap/FPS limit API를 가진다.
- `CaptureRemoteDocument.kt`
  - https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/src/main/java/androidx/compose/remote/creation/compose/capture/CaptureRemoteDocument.kt
  - Compose capture API는 Android `Context`와 virtual display 정보를 요구한다.
- `RcDocCreator.kt`
  - https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-core/src/main/java/androidx/compose/remote/creation/dsl/RcDocCreator.kt
  - standalone JVM에서 쓸 수 있는 procedural `createRcBuffer`가 있으나 `RestrictTo(LIBRARY_GROUP)`다.
- `RcActionScope.kt`
  - https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-core/src/main/java/androidx/compose/remote/creation/dsl/RcActionScope.kt
  - JVM procedural DSL의 `hostAction`은 현재 action 이름만 받는다.

## 로컬 구현·검증 증거

대상: `samples/remote-state-lab`

### 빌드와 테스트

명령:

```text
./gradlew :server:test :app:testDebugUnitTest :app:assembleDebug
./gradlew :app:lintDebug :server:installDist
```

결과:

- 두 명령 모두 `BUILD SUCCESSFUL`.
- server document tests: 2 tests, 0 failures.
- Android host action router tests: 2 tests, 0 failures.
- debug APK: `app/build/outputs/apk/debug/app-debug.apk`, 약 35 MiB.
- lint task 성공. 버전 갱신·target SDK·template resource 관련 warning은 남아 있다.

### HTTP 왕복

로컬 Ktor server를 `:server:run`으로 실행해 확인했다.

- `GET /health` → `ok`
- 첫 `GET /document` → 11,935 bytes
- 첫 document SHA-256 → `2c88a1203a2389e728efa220b51fb0e561821f100720b041bb4006f6ce13a502`
- `POST /actions/reprice` → revision 2
- 다음 document SHA-256 → `f2e8b221e754d1b09dcfe710f382b2f868d4256e3ec4e5c9ffaf548751b92c43`
- `POST /actions/submit` → revision 3, submitted true
- 다음 document → 11,921 bytes

## 검증 범위 제한

- JVM server 생성, HTTP document/action 왕복, parser 코드 컴파일, APK build를 확인했다.
- 실제 emulator/device에서 player의 touch·layout·accessibility를 수동 검증하지 않았다.
- malformed/fuzz corpus, N/N-1 player compatibility, process-death cache, TLS/authentication은 구현하지 않았다.
- 이 증거는 product readiness가 아니라 restricted alpha POC의 기술 실행 가능성만 뒷받침한다.
