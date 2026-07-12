# Remote Compose Research & POC

AndroidX Remote Compose를 Ktor JVM producer와 Android embedded player 구조로 조사하고 검증한 저장소입니다.

## 주요 문서

- [Remote Compose 공식 API Codelab](codelab/index.html)
- [Remote Compose POC 회고와 학습 포인트](reference/wiki/remote-compose-poc-retrospective.md)
- [AndroidX Remote Compose](reference/wiki/androidx-remote-compose.md)
- [alpha14 디버깅과 컴포넌트 이슈](reference/wiki/alpha14-debugging-and-component-issues.md)
- [권장 레퍼런스 아키텍처](reference/wiki/reference-architecture.md)
- [Wiki index](reference/wiki/index.md)

## 샘플

[`samples/remote-state-lab`](samples/remote-state-lab/README.md)은 다음 흐름을 검증합니다.

- native Jetpack Compose 서버 연결 화면
- Ktor 서버가 생성한 Remote Compose binary document 렌더링
- server-backed 가변 task 목록과 스크롤
- Remote Compose `StateLayout` 목록·상세 전환
- native TextField 입력과 allowlist된 host action
- create/delete 성공 후 최신 문서 자동 reload

실행 방법과 현재 API 제한은 [샘플 실행 가이드](samples/remote-state-lab/README.md)를 참고하세요.

처음부터 단계별로 학습하려면 저장소 root에서 `python3 -m http.server 4173 --bind 127.0.0.1`을 실행하고 `http://localhost:4173/codelab/`을 여세요.

> 2026-07-11 기준 샘플은 AndroidX Remote Compose `1.0.0-alpha14`를 사용합니다. 핵심 embedded player와 JVM creation API의 공개 범위, 접근성, 호환성, fallback을 검토하지 않은 채 production에 적용하지 마세요.
