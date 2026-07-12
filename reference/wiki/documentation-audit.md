---
title: Remote Compose 문서 전문 감사
type: maintenance
created: 2026-07-13
updated: 2026-07-13
as_of: 2026-07-13
confidence: high
sources:
  - ../raw/remote-compose-documentation-audit-2026-07-13.md
  - ../raw/androidx-remote-compose-official-2026-07-12.md
  - ../raw/remote-compose-api-syntax-audit-2026-07-12.md
---

# Remote Compose 문서 전문 감사

## 판정

2026-07-13 기준으로 저장소의 핵심 아키텍처 결론은 유지된다.

- 서버 sample은 Ktor/JVM에서 restricted `RcScope`로 요청 시점의 document를 만든다.
- Android 앱은 restricted embedded player로 bytes를 재생한다.
- document-local state와 ViewModel/Repository/server state는 별도다.
- public Compose capture frontend는 서버 procedural frontend와 다른 제작 방식이다.
- production-ready supported architecture라고 주장할 근거는 없다.

설명과 copy/paste 예제에서는 5개 오류를 발견해 수정했다.

## 수정 결과

| 항목 | 이전 설명 | 수정된 설명 | 심각도 |
|---|---|---|---|
| Codelab 명칭 | 공식 API Codelab | restricted POC 기반 서버 SDUI Codelab | 높음 |
| Root | content의 첫 layout이 root | `createRcBuffer`가 root를 자동 생성 | 높음 |
| StateLayout | `Text`를 direct child로 사용 | 각 state를 `Box`/`Column` layout으로 감쌈 | 높음 |
| Player snippet | width/height 생략 | alpha14 필수 dimensions 추가 | 높음 |
| Font density | 항상 capture-time 고정 | 기본 creation density, 선택적으로 `RemoteDensity.Host` | 중간 |

## 설명 신뢰도 표기

| 표기 | 의미 |
|---|---|
| 확인된 사실 | alpha14 artifact, pinned source, 실제 sample build/run으로 확인 |
| POC 관찰 | 이 저장소의 emulator/device 실험에서 재현 |
| 공학적 추론 | source와 실행 결과로부터 도출했지만 AndroidX 보장 아님 |
| 미확인 | public contract 또는 재현 테스트가 부족함 |

## 회귀 방지

`python3 scripts/audit_docs.py`를 GitHub Pages build 전에 실행한다. public Compose 심화 code block은 `:docs-api-fixture:compileDebugKotlin`으로 컴파일한다. procedural code는 독립 server build와 Android app build로 검증한다.

다음 alpha 업데이트 시 다시 확인할 항목:

1. `RcScope`, `createRcBuffer`, player의 visibility
2. `RemoteDocumentPlayer` signature
3. document API/profile version
4. `StateLayout` child와 invalid-index behavior
5. `RemoteDensity` 기본값과 host expression
6. IME input 또는 lazy list component 추가 여부
7. public player와 Ktor guide 제공 여부

상세 source, checksum, 수정 근거는 [2026-07-13 감사 원자료](../raw/remote-compose-documentation-audit-2026-07-13.md)에 고정했다.
