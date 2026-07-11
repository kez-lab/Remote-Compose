---
title: Official sources snapshot 2026-07-10
type: source
created: 2026-07-10
updated: 2026-07-10
status: immutable
---

# 공식 소스 스냅샷 — 2026-07-10

## AndroidX Remote Compose

| 출처 | 확인한 근거 |
|---|---|
| [Remote Compose 릴리스 노트](https://developer.android.com/jetpack/androidx/releases/compose-remote) | 최신 배포는 `1.0.0-alpha14`(2026-07-01). 생성·플레이어·프리뷰 아티팩트와 alpha14 변경사항을 확인했다. |
| [Google Maven metadata](https://dl.google.com/dl/android/maven2/androidx/compose/remote/remote-core/maven-metadata.xml) | `remote-core`의 latest/release가 `1.0.0-alpha14`, 마지막 갱신이 2026-07-01임을 확인했다. |
| [RemoteComposable API](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/layout/RemoteComposable) | Remote composable 콘텐츠는 문서에 기록된 뒤 다른 프로세스나 기기의 플레이어에서 재생될 수 있다. |
| [capture API](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/capture/package-summary) | 단일 문서 캡처와 재구성 시 `Flow<ByteArray>` 문서 스트림을 제공하며, API는 experimental이다. |
| [layout API](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/layout/package-summary.html) | 공개 DSL의 핵심은 `RemoteBox`, `RemoteRow`, `RemoteColumn`, `RemoteText`, `RemoteImage`, `RemoteCanvas`, `RemoteStateLayout`이다. |
| [action API](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/action/package-summary) | 원격 상태 변경, 결합 액션, 이름 기반 `hostAction`, Android `PendingIntent` 액션이 존재한다. |
| [semantics API](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/compose/modifier/SemanticsProperties) | 접근성·테스트용 text, content description, state description, enabled, role 속성을 제공한다. |
| [Profile API](https://developer.android.com/reference/kotlin/androidx/compose/remote/creation/profile/Profile) | 프로필은 문서 API 레벨, 허용 operation profile, 플랫폼 서비스, writer를 묶어 생성 시 검증에 사용한다. |
| [Remote Compose 소스 트리](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/remote/) | core, creation, creation-compose, player-core/view/compose, testing, lint, preview와 통합 데모가 분리돼 있다. |
| [alpha14 고정 소스 커밋](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/) | 릴리스 노트가 alpha14 범위의 끝으로 링크한 소스 상태. 이 스냅샷의 소스 코드 판단 기준이다. |
| [Wire format 문서 — 고정 커밋](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/Documentation/RemoteComposeWireFormat.md.html) | v1.1.0 문서는 포맷을 Android용 standalone binary operation list로 설명하며 WIP·변경 가능이라고 명시한다. 헤더는 버전·크기·capabilities를 가진다. |
| [Limits.java — 고정 커밋](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-core/src/main/java/androidx/compose/remote/core/Limits.java) | 프레임당 operation, 문자열, 이미지, 비트맵 메모리, 중첩 깊이 등의 내부 제한을 확인했다. 이 클래스는 library-group restricted다. |
| [RemoteComposePlayer.java — 고정 커밋](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-player-view/src/main/java/androidx/compose/remote/player/view/RemoteComposePlayer.java) | View 플레이어와 일부 문서 로드 API가 `@RestrictTo(LIBRARY_GROUP)`인 상태이며 버전 호환 검사와 리소스 제한 설정을 수행한다. |
| [RemoteDocument.java — 고정 커밋](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-player-core/src/main/java/androidx/compose/remote/player/core/RemoteDocument.java) | byte/input stream 파싱과 표시 가능 버전 검사가 있지만 클래스 자체가 library-group restricted다. |
| [remote-creation build.gradle — 고정 커밋](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation/build.gradle) | 생성 모듈은 Android와 JVM target을 가진다. Android minSdk는 23이다. |
| [remote-creation-compose build.gradle — 고정 커밋](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-creation-compose/build.gradle) | Compose 생성 DSL은 Android 모듈이며 minSdk 29, compileSdk 35, Compose 1.11.0 계열 의존성을 사용한다. |
| [remote-player-view build.gradle — 고정 커밋](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-player-view/build.gradle) | View 플레이어는 Android 전용이며 minSdk 26이다. |
| [remote-player-compose build.gradle — 고정 커밋](https://android.googlesource.com/platform/frameworks/support/+/19660b9e1b2fec4a9528fe80ce0a432c0fa2f825/compose/remote/remote-player-compose/build.gradle) | Compose wrapper 플레이어는 Android 전용이며 minSdk 29다. |

### 주의한 모순

- 검색 색인에는 한동안 alpha13이 노출됐지만, 실제 릴리스 페이지 원문과 Google Maven metadata는 alpha14를 가리켰다. 현재 버전 판단에는 원문과 Maven metadata를 우선했다.
- 릴리스 페이지가 플레이어 아티팩트를 나열하더라도, alpha14 고정 소스에서 핵심 플레이어 클래스는 library-group restricted다. “아티팩트가 존재함”과 “일반 앱용 안정 공개 API”를 같은 의미로 취급하면 안 된다.

## Android, Jetpack Compose, Glance, Wear

| 출처 | 확인한 근거 |
|---|---|
| [Jetpack Compose UI 릴리스](https://developer.android.com/jetpack/androidx/releases/compose-ui) | 2026-07-01 기준 stable `1.11.4`, beta `1.12.0-beta02`. |
| [Jetpack Glance 개요](https://developer.android.com/develop/ui/compose/glance) | Glance는 Compose runtime 위에서 app widget을 만드는 별도 DSL이며 일반 Compose UI 요소와 직접 호환되지 않는다. |
| [App widget 개요](https://developer.android.com/develop/ui/views/appwidgets/overview) | 위젯의 제스처·요소 제약과 RemoteViews/Glance 진입점을 확인했다. |
| [Wear Widgets](https://developer.android.com/training/wearables/widgets) | Wear Widgets는 Jetpack Glance와 Remote Compose로 구동된다. |
| [Wear Widget 시작 가이드](https://developer.android.com/training/wearables/widgets/get_started) | Wear OS 7 또는 호환 renderer 요구사항, Remote Compose 의존성, `@RemoteComposable` 예제를 확인했다. |
| [Tiles](https://developer.android.com/training/wearables/tiles) | 기존 Wear Tile은 Compose가 아니라 ProtoLayout으로 선언되고 원격 환경에서 렌더링된다. |
| [Tiles에서 Widget으로 마이그레이션](https://developer.android.com/training/wearables/widgets/migration) | lambda 대신 직렬화 가능한 액션과 remote state를 사용하고, `RemoteDp` 등 플레이어 평가형 타입을 권장한다. |
| [공식 WearWidget 샘플](https://github.com/android/wear-os-samples/tree/main/WearWidget) | Remote Compose 기반 Wear Widget의 공식 샘플 위치다. |
| [2026 Google I/O Android 요약](https://android-developers.googleblog.com/2026/05/17-things-android-developers-google-io.html) | Android 17의 위젯 통합 방향에서 Remote Compose가 mobile/car animation과 Wear remote surface에 활용된다고 발표했다. 미래 방향이며 현재 일반 앱 플레이어 공개 안정성을 뜻하지 않는다. |
| [Compose 접근성](https://developer.android.com/develop/ui/compose/accessibility) | semantics, font scaling, traversal, 검사·테스트를 UI 품질 기준으로 사용했다. |
| [Offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first) | 로컬 데이터 소스를 source of truth로 두고 네트워크 실패 중에도 핵심 읽기 기능을 유지해야 한다. |

## Kotlin, CMP, Ktor

| 출처 | 확인한 근거 |
|---|---|
| [CMP 호환성과 버전](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html) | CMP `1.11.1`의 최소 플랫폼, Kotlin/Jetpack과의 릴리스 관계를 확인했다. |
| [KMP FAQ](https://kotlinlang.org/docs/multiplatform/faq.html) | CMP는 Android에서 Jetpack Compose를 사용하고, Android/iOS/desktop은 stable, Wasm web은 beta다. |
| [CMP 플랫폼 차이](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html) | 플랫폼별 진입점·입력·텍스트·interop 차이와 pixel-perfect 비보장을 확인했다. |
| [Compose compiler 설정](https://kotlinlang.org/docs/multiplatform/compose-compiler.html) | Kotlin 2.x에서는 Kotlin과 같은 버전의 Compose compiler plugin이 필요하다. |
| [Ktor client 지원 플랫폼](https://ktor.io/docs/client-supported-platforms.html) | JVM, Android, Native, JS, WasmJs 등 KMP client 지원 범위를 확인했다. |
| [Ktor client engine](https://ktor.io/docs/client-engines.html) | 플랫폼별 엔진 선택과 CIO/OkHttp/Darwin 등의 범위를 확인했다. |
| [Ktor ContentNegotiation](https://ktor.io/docs/server-serialization.html) | JSON/XML/CBOR/ProtoBuf 직렬화와 content negotiation을 지원한다. |
| [Ktor 인증](https://ktor.io/docs/server-auth.html) | Bearer, JWT, API key 등 인증 플러그인 범위를 확인했다. |
| [Ktor WebSocket](https://ktor.io/docs/server-websockets.html) | 양방향 실시간 연결은 WebSocket, 단방향 서버 push는 SSE 고려가 공식 가이드다. |
| [Ktor cache](https://ktor.io/docs/client-caching.html) | `HttpCache`와 HTTP cache header 기반 캐싱을 확인했다. |
| [Ktor KMP full-stack](https://ktor.io/docs/full-stack-development-with-kotlin-multiplatform.html) | server·client·공유 타입을 포함한 KMP 구조의 공식 예시다. |
| [Ktor Maven metadata](https://repo1.maven.org/maven2/io/ktor/ktor-server-core/maven-metadata.xml) | 2026-06-25 기준 최신 정식 배포 `3.5.1`. |
| [kotlinx.serialization JsonBuilder](https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-json/kotlinx.serialization.json/-json-builder/) | unknown key, discriminator, defaults, null 처리 등 계약 진화 옵션을 확인했다. |

## 보안과 정책

| 출처 | 확인한 근거 |
|---|---|
| [Android Network Security Configuration](https://developer.android.com/privacy-and-security/security-config) | HTTPS/cleartext 차단, trust anchors, debug override, certificate transparency 설정을 확인했다. |
| [Android 네트워크 프로토콜 보안](https://developer.android.com/privacy-and-security/security-ssl) | Android 앱에서 certificate pinning은 운영 장애 위험 때문에 일반적으로 권장되지 않는다는 주의를 확인했다. |
| [Google Play Device and Network Abuse](https://support.google.com/googleplay/android-developer/answer/16559646) | Play 외부에서 dex/JAR/so 같은 실행 코드를 내려받는 행위가 금지된다. 원격 선언형 UI 데이터는 실행 코드와 분리하고 정책 위반 기능을 노출하지 않아야 한다. |
