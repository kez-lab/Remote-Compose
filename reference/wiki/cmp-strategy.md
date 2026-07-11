---
title: Compose Multiplatform Integration Strategy
type: synthesis
created: 2026-07-10
updated: 2026-07-11
as_of: 2026-07-11
confidence: high
sources:
  - ../raw/official-sources-2026-07-10.md
---

# CMP 통합 전략

## 핵심 경계

Compose Multiplatform은 **동일한 Kotlin UI 소스를 여러 플랫폼 앱에서 실행**한다. Remote Compose는 **직렬화된 UI operation을 Android remote player에서 실행**한다. 공유 가능한 것은 Remote Compose player가 아니라 제품의 도메인/화면 계약이다.

[CMP FAQ](https://kotlinlang.org/docs/multiplatform/faq.html)에 따르면 Android target은 Jetpack Compose를 사용하고 Android/iOS/desktop은 stable, Wasm web은 beta다. AndroidX Remote Compose source에는 이들 target용 공통 player가 없다.

## 세 가지 통합 옵션

### A. 제품 전용 UI contract + CMP renderer

서버가 고수준 JSON/CBOR/ProtoBuf 문서를 보내고 `commonMain` renderer가 Compose UI로 변환한다.

장점:

- Android/iOS/desktop/web가 같은 계약 사용
- schema와 rollout을 제품 팀이 통제
- player 내부 API에 의존하지 않음

비용:

- layout/action/accessibility/inspector를 직접 설계
- 모든 renderer의 parity test 필요

### B. Remote Compose binary 전용

서버 또는 Android producer가 Remote Compose 문서를 만들고 Android player가 재생한다.

장점:

- Android embedded player의 document-local state/animation
- AndroidX의 wire/runtime 구현 재사용

비용:

- Android 중심
- alpha와 restricted player API
- CMP 공통 renderer가 되지 않음

### C. 고수준 contract + Android embedded Remote Compose adapter

고수준 계약을 source of truth로 두고:

- CMP 앱 화면: 로컬 Compose renderer
- Android app experiment: Remote Compose document compiler/embedded player adapter
- 필요하면 서버가 미리 생성한 Android 문서를 CDN에 캐시

이 저장소의 권장안이다. AndroidX alpha 변화를 adapter 경계 안에 격리할 수 있다.

## 권장 모듈 구조

```text
core/
  ui-contract/              # commonMain, Compose 비의존
  domain/                   # 공통 모델과 validation
app/
  sharedLogic/              # repository, use case, Ktor client
  sharedUI/                 # CMP renderer
  androidApp/
  iosApp/
  desktopApp/
  webApp/
server/
  ktor-api/                 # contract, auth, cache, actions
android-remote-compose/
  adapter/                  # UiDocument -> Remote Compose
  embedded-player-poc/      # Android 앱 내부 alpha player 격리
```

[Ktor KMP full-stack 가이드](https://ktor.io/docs/full-stack-development-with-kotlin-multiplatform.html)는 server와 client가 공유 타입을 재사용하는 구조를 보여 준다. 다만 UI 문서의 server DTO와 domain model을 완전히 같은 타입으로 묶으면 server migration이 client binary compatibility를 깨뜨릴 수 있으므로 API boundary mapper를 둔다.

## contract 설계

UI node는 시각적 구현 세부보다 제품 의미를 우선한다.

좋은 예:

- `Hero(title, body, media, primaryAction)`
- `Metric(label, value, trend, accessibilityLabel)`
- `Action(command = OpenOrder, orderId = ...)`

신중해야 할 예:

- 임의 클래스명
- 임의 Kotlin expression
- raw modifier chain
- 서버가 지정하는 Activity/URL/file path
- pixel-perfect 절대 좌표만 있는 canvas

고수준 node는 renderer가 플랫폼 접근성·입력·safe area·font scale에 맞게 해석할 여지를 준다. 필요한 곳에만 저수준 `Row`, `Column`, `Text`를 허용한다.

## 공통 renderer 계약

```kotlin
interface UiRenderer<Output> {
    fun supports(document: UiDocument, capabilities: ClientCapabilities): SupportResult
    fun render(document: UiDocument): Output
}
```

실제 CMP renderer는 `@Composable`이지만 contract와 validation은 Compose에 의존하지 않게 유지한다. Android Remote Compose adapter도 같은 validation 결과를 소비한다.

## platform 차이

[CMP platform specifics](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)는 입력, 스크롤, text rendering, native interop가 target마다 다르며 pixel-perfect 동일성을 보장하지 않는다고 설명한다.

따라서 계약은 다음을 강제하지 않는다.

- 모든 플랫폼의 동일 glyph rasterization
- desktop multitouch
- 모든 native popup의 같은 모양
- Android back gesture를 iOS/desktop에 그대로 복제

대신 semantic parity, action parity, 정보 구조, spacing token, 접근성 결과를 검증한다.

## 버전 기준

| 구성요소 | 기준 버전 | 정책 |
|---|---|---|
| CMP | stable `1.11.1` | preview 1.12는 별도 실험 branch |
| Jetpack Compose UI | stable `1.11.4` | Android BOM/호환성 확인 |
| Kotlin | stable release와 같은 Compose compiler plugin | compiler plugin 버전을 Kotlin과 일치 |
| Ktor | `3.5.1` | server/client minor를 version catalog에서 통제 |
| Remote Compose | `1.0.0-alpha14` | adapter module에 격리, 버전 고정 |

Remote Compose source의 `remote-creation-compose`는 Compose 1.11.0 계열에 빌드돼 있다. 앱 전체 Compose를 alpha14 내부 버전과 무작정 강제 정렬하지 말고 Gradle dependency graph, binary compatibility, sample compile을 검증한다.

## 결론

CMP를 목표로 한다면 Remote Compose를 공통 UI 계층으로 놓지 않는다. 공통 계층에는 제품 contract와 validation을 두고, Remote Compose는 Android 앱 내부 실험 renderer로 격리한다.

## 공식 근거

- [CMP compatibility and versioning](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html)
- [KMP FAQ](https://kotlinlang.org/docs/multiplatform/faq.html)
- [CMP platform specifics](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)
- [Compose compiler](https://kotlinlang.org/docs/multiplatform/compose-compiler.html)
