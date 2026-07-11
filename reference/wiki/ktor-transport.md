---
title: Ktor Transport for Remote UI
type: engineering
created: 2026-07-10
updated: 2026-07-10
as_of: 2026-07-10
confidence: high
sources:
  - ../raw/official-sources-2026-07-10.md
---

# Ktor 전송 계층

## 역할

Ktor는 다음을 담당할 수 있다.

- HTTP content negotiation과 JSON/CBOR/ProtoBuf/binary 응답
- Bearer/JWT/API key 인증
- ETag, Cache-Control을 이용한 캐시
- WebSocket 또는 SSE 실시간 업데이트
- client timeout, retry, auth refresh, cache
- Android/iOS/desktop/web용 KMP client

Ktor가 담당하지 않는 것은 Remote Compose 문서 의미, 허용 컴포넌트, player profile, action 권한, fallback UI다.

## 권장 endpoint

| Endpoint | 목적 |
|---|---|
| `GET /v1/ui/{surface}` | 문서 또는 고수준 UI 계약 조회 |
| `GET /v1/ui/{surface}/events` | 선택적 SSE invalidation/version event |
| `POST /v1/ui/{surface}/actions` | 호스트 action 결과를 서버 command로 전달 |
| `GET /v1/ui/capabilities` | server가 제공할 수 있는 schema/profile 정보 |

초기 버전에는 UI 문서 전체를 WebSocket으로 계속 밀지 않는다. HTTP GET + ETag로 시작하고 정말 필요한 단방향 invalidation만 SSE로 추가한다. 양방향·고빈도 상호작용이 입증될 때 WebSocket을 사용한다. [Ktor WebSocket 가이드](https://ktor.io/docs/server-websockets.html)도 단방향 업데이트에는 SSE를 고려하라고 안내한다.

## 요청 capability

client는 최소한 다음을 보낸다.

- app version/build
- platform과 OS/API level
- UI contract version 범위
- Remote Compose document API/profile 범위
- viewport size, density behavior, font scale, layout direction
- locale, theme, accessibility-relevant preference
- 지원 component/action 목록 또는 capability hash

서버는 알지 못하는 client에 최신 문서를 낙관적으로 보내지 말고 가장 낮은 공통 계약이나 bundled fallback을 선택한다.

## 응답 envelope

Remote Compose raw bytes만 반환하더라도 HTTP header 또는 별도 manifest로 다음 메타데이터를 제공한다.

```json
{
  "surface": "home.hero",
  "format": "androidx-remote-compose",
  "contractVersion": 3,
  "documentApi": 7,
  "profile": "androidx",
  "producerVersion": "1.0.0-alpha14",
  "minAppBuild": 120,
  "issuedAt": "2026-07-10T12:00:00Z",
  "expiresAt": "2026-07-11T12:00:00Z",
  "etag": "rc-home-42",
  "sha256": "...",
  "fallbackKey": "home.hero.v2"
}
```

권장 media type:

- Remote Compose bytes: 제품이 소유한 vendor type, 예: `application/vnd.example.remote-compose`
- 고수준 UI contract: `application/vnd.example.ui+json` 또는 CBOR/ProtoBuf vendor type
- manifest와 payload를 한 응답으로 묶을 때 multipart보다 header + body 또는 작은 typed envelope를 우선

`androidx`가 공식 등록한 IANA media type이 있다는 근거는 확인하지 못했으므로 제품 vendor type을 사용한다.

## 캐시와 오프라인

1. 서버는 `ETag`, `Cache-Control`, `Vary`를 명확히 설정한다.
2. client는 검증을 통과한 문서만 last-known-good로 승격한다.
3. 새 문서는 임시 저장 → hash/size/version 검증 → parse/render smoke → atomic promote 순으로 처리한다.
4. `304 Not Modified`면 기존 검증 문서를 유지한다.
5. 만료됐어도 bundled fallback보다 last-known-good가 안전한지는 surface별 정책으로 정한다.
6. 인증 실패, network timeout, parse failure를 서로 다른 fallback reason으로 기록한다.

[Android offline-first 가이드](https://developer.android.com/topic/architecture/data-layer/offline-first)는 네트워크가 불안정해도 핵심 읽기가 가능하도록 로컬 source of truth를 권장한다. UI 문서 cache도 같은 원칙을 따른다.

## 인증과 액션

- UI 조회는 user/tenant/experiment 권한에 따라 서버에서 필터링한다.
- token이나 개인정보를 Remote Compose 문서 안에 포함하지 않는다.
- `hostAction` payload를 직접 서버 endpoint나 URL로 사용하지 않는다.
- app의 action registry가 remote action을 typed command로 변환한 뒤 authorization을 다시 확인한다.
- 결제, 삭제, 권한 변경은 원격 문서의 한 번 클릭만으로 완료하지 말고 native confirmation과 server-side authorization을 둔다.
- 재시도 가능한 action에는 idempotency key를 사용한다.

[Ktor Authentication](https://ktor.io/docs/server-auth.html)은 Bearer/JWT/API key를 지원하지만 어떤 scheme을 고를지는 제품의 identity architecture 결정이다.

## serialization 전략

고수준 SDUI 계약은 sealed component type과 명시적 discriminator를 사용한다. unknown **field**는 호환성 때문에 선택적으로 무시할 수 있지만 unknown **component/action type**은 조용히 drop하지 않는다.

[kotlinx.serialization `JsonBuilder`](https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-json/kotlinx.serialization.json/-json-builder/)에서 `ignoreUnknownKeys`, `classDiscriminator`, `encodeDefaults`, `explicitNulls`를 설정할 수 있다. 권장 원칙:

- envelope의 unknown metadata field: forward compatibility를 위해 허용 가능
- security-sensitive action field: 엄격 파싱
- unknown component type: explicit unsupported 결과 + safe fallback
- enum의 unknown value: `Unknown(raw)` 모델 또는 문서 거부
- default 값 변경: schema version 없이 의미를 바꾸지 않음

## server-side Remote Compose 생성

`remote-creation-jvm` procedural API로 JVM 서버 생성 가능성을 실험할 수 있다. 그러나 다음을 전제로 한다.

- alpha API에 대한 dependency isolation
- producer/player version lockstep 또는 capability negotiation
- deterministic document generation test
- Android 전용 capture DSL과 혼동하지 않음
- profile validation과 payload limit

`remote-creation-compose`의 capture API는 Android `Context`를 받고 Android module이므로 일반 Ktor JVM 서버에서 CMP Compose처럼 실행하는 경로가 아니다.

## KMP client

[Ktor client engine 문서](https://ktor.io/docs/client-engines.html)에 따라 engine은 target별로 둔다.

- Android: OkHttp 또는 CIO
- iOS/macOS: Darwin 또는 CIO의 지원 범위 검증
- JVM desktop/server: CIO, Java, Apache5 등
- JS/Wasm: 지원 engine과 브라우저 cache/CORS 제약 확인

공통 module에는 request/response contract와 plugin 설정의 공통 부분만 두고, TLS·proxy·cache storage·background policy는 platform source set에서 구성한다.

## 공식 근거

- [Ktor server serialization](https://ktor.io/docs/server-serialization.html)
- [Ktor authentication](https://ktor.io/docs/server-auth.html)
- [Ktor WebSocket](https://ktor.io/docs/server-websockets.html)
- [Ktor client platforms](https://ktor.io/docs/client-supported-platforms.html)
- [Ktor client cache](https://ktor.io/docs/client-caching.html)

