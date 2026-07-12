# Remote Compose Codelab site

SDUI라는 용어도 처음 듣는 Android 개발자를 위한 정적 HTML 입문 실습입니다. 이 저장소의 실제 POC와 동일한 `Ktor/JVM → RcScope → ByteArray → Android Player` 한 경로를 처음부터 끝까지 따라갑니다.

`@RemoteComposable`, `RemoteText`, `.rs/.rb`, `captureSingleRemoteDocument`를 사용하는 Android Compose 생성 frontend는 본편에 섞지 않습니다. Android 또는 CI에서 문서를 미리 capture해 서버/CDN에 배포하는 경로를 선택할 때만 심화 문서에서 학습합니다.

## 실행

공개 사이트: <https://kez-lab.org/Remote-Compose/>

로컬 서버 없이 다음 파일을 브라우저로 열 수 있습니다.

`/Users/kwak-euijin/StudioProjects/Remote-Compose/codelab/index.html`

브라우저의 `file:` 제약 때문에 정적 서버가 필요한 경우에만 저장소 root에서 다음을 실행합니다.

```bash
python3 -m http.server 4173 --bind 127.0.0.1
```

브라우저에서 `http://127.0.0.1:4173/codelab/`을 엽니다. `127.0.0.1`에만 bind하므로 외부 네트워크에는 공개되지 않습니다.

## 구성

- `index.html`: 완성 화면 → SDUI → 실행 → 서버 RcScope UI → Header와 bytes → procedural state → 화면 전환 → hostAction → Ktor transport 순서의 10단계 학습 내용
- `styles.css`: 실제 앱 화면, 일반 앱/SDUI 비교, 문서·액션 흐름을 한 개념씩 보여 주는 desktop/mobile layout
- `app.js`: 단계 이동, mobile drawer, 진행률, 완료 상태, 체크리스트, 코드 복사

진행 상태와 체크리스트는 브라우저 `localStorage`에만 저장됩니다. 별도 backend나 build step은 없습니다.

## 출처 경계

이 사이트는 Google 공식 Codelab이 아니다. procedural builder와 player는 `restricted`, Ktor route·`HostActionRouter`·`scaledSp`는 `저장소 POC`로 표시한다.

본편에는 procedural API만 사용한다. public Compose frontend의 `.rs`·`.rb`·`valueChange`, suspend capture는 [`reference/wiki/remote-state-and-values.md`](../reference/wiki/remote-state-and-values.md)에 심화 과정으로 격리한다.

상태 학습 단계는 현재 server sample의 `remoteNamedInteger`, `StateLayout`, `setValue`, `hostAction(String)`을 사용한다.

## 근거

- `reference/raw/androidx-remote-compose-official-2026-07-12.md`
- `reference/wiki/ktor-rcscope-codelab-path.md`
- `reference/wiki/remote-state-and-values.md` (선택 심화)
