# Remote Compose Reference

이 디렉터리는 Remote Compose 관련 기술을 다시 조사하지 않고도 설계·구현·검증 결정을 내릴 수 있게 만든 장기 연구 자료다.

## 빠른 시작

1. [전체 개요](wiki/overview.md): 결론과 현재 상태
2. [AndroidX Remote Compose](wiki/androidx-remote-compose.md): 실제 프레임워크와 와이어 포맷
3. [생태계 비교](wiki/ecosystem-map.md): Jetpack Compose, CMP, Remote Compose, custom SDUI의 차이
4. [권장 아키텍처](wiki/reference-architecture.md): Ktor와 CMP까지 포함한 구현 방향
5. [보안·신뢰성](wiki/security-reliability.md): 원격 문서를 신뢰 경계로 다루는 방법
6. [파일럿 계획](wiki/pilot-plan.md): PoC에서 운영까지의 검증 순서
7. [실행 가능한 SDUI POC](wiki/sample-sdui-poc.md): Ktor 문서 생성, Android player, state/action 경계
8. [alpha14 디버깅과 컴포넌트 이슈](wiki/alpha14-debugging-and-component-issues.md): 실제 실패, 원인 분리, workaround, 재검증 목록

공식 원자료 목록은 [2026-07-10 공식 소스 스냅샷](raw/official-sources-2026-07-10.md)에 있고, 구현 증거는 [2026-07-11 POC 스냅샷](raw/remote-sdui-poc-2026-07-11.md)에 있다.

## 중요한 한 문장

AndroidX Remote Compose는 CMP용 원격 렌더러가 아니다. Android 중심의 직렬화 가능한 UI 문서 포맷과 생성·재생 도구이며, Ktor는 그 문서를 전달할 수 있는 네트워크 계층이고, CMP는 각 플랫폼 앱 안에서 실행되는 공유 UI 계층이다.

## 최신성

- 조사 기준일: 2026-07-11
- 변동성이 큰 항목: Remote Compose alpha API, 공개 플레이어/JVM builder 범위, Compose/Ktor/CMP 버전
- 갱신 방법: 최상위 [AGENTS.md](../AGENTS.md)의 ingest/review 절차를 따른다.
