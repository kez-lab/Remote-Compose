# Font and remote state debugging evidence

Date: 2026-07-11  
Device: `emulator-5554`  
Remote Compose: `1.0.0-alpha14`

이 디렉터리는 font가 작고 `+`가 무반응처럼 보였던 문제를 진단한 순서대로 보존한다. annotated PNG의 magenta box/number는 Android CLI가 생성한 accessibility/layout annotation이다.

| 파일 | 단계 | 의미 |
|---|---|---|
| `before.png` | 초기 | density metadata와 typography 보정 전 |
| `after-density.png` | density 적용 | font/spacing은 개선됐지만 dynamic label은 아직 stale |
| `debug-click.png` | click hit test | `+`가 clickable semantics를 갖고 resolved coordinate가 존재 |
| `current-debug.png` | player debug | runtime component bounds와 action tree 조사 시점 |
| `diagnostic-state.png` | state/action 분리 | remote context 값은 바뀌지만 `TextLookupInt`/가격 표시가 이전 값을 유지 |
| `after-plus-fixed.png` | float text 시도 | `TextFromFloat`도 기대한 visual update를 주지 못한 중간 시도 |
| `nested-state-after-plus.png` | direct state label | `StateLayout(seats)`로 `4 members`가 보이기 시작한 단계 |
| `final-after-plus.png` | seat workaround | seat direct state는 정상, derived price index는 아직 stale했던 단계 |
| `deep-state.png` | nested direct price states | seat와 가격이 함께 `4 members`, `$64 / month`로 갱신 |
| `final-configured.png` | 최종 annotated | `4 members`, `Pro ✓`, `$112 / month` |
| `final-configured-clean.png` | 최종 clean | annotation 없는 최종 configure 화면 |
| `final-review.png` | 최종 review | configure state `4`, `Pro`, `$112 / month` 유지 |

전체 원인 분석:

- [`reference/raw/compose-remote-alpha14-debugging-2026-07-11.md`](../../../../../reference/raw/compose-remote-alpha14-debugging-2026-07-11.md)
- [`reference/wiki/alpha14-debugging-and-component-issues.md`](../../../../../reference/wiki/alpha14-debugging-and-component-issues.md)

검증 명령:

```bash
./gradlew :server:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
android run --device=emulator-5554 --apks=app/build/outputs/apk/debug/app-debug.apk --activity=.MainActivity
android layout --device=emulator-5554 --pretty
android screen capture --output=raw/verification/2026-07-11-font-plus-fix/final-configured-clean.png
```
