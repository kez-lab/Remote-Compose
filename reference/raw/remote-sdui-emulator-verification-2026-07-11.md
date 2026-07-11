---
title: Remote Compose SDUI emulator verification
type: evidence
created: 2026-07-11
as_of: 2026-07-11
---

# Remote Compose SDUI emulator verification

## 범위

- sample: `samples/remote-state-lab`
- AndroidX Remote Compose: `1.0.0-alpha14`
- device: `emulator-5554`
- server: local Ktor `http://0.0.0.0:8080`
- client URL: `http://10.0.2.2:8080`

## 재현과 관찰

1. 초기 문서는 `3 members`, Starter, Analytics ON, `$48 / month`로 렌더링됐다.
2. 기존 구현에서 `+` action은 remote context의 seats 값을 `3 → 4`로 변경했지만 `TextLookupInt` 기반 label과 파생 text는 화면에서 이전 값을 유지했다.
3. phase를 직접 index로 사용하는 root `StateLayout` 전이는 정상 작동했다.
4. 표시 label과 가격을 직접 state 기반 중첩 `StateLayout`으로 바꾼 뒤 `+`가 `4 members`, `$64 / month`를 표시했다.
5. Pro 선택 뒤 `Pro ✓`, `$112 / month`가 표시됐고 review 화면도 `4`, `Pro`, `$112 / month`를 유지했다.

2번은 이 alpha14 sample/player 조합에서 얻은 engineering observation이다. AndroidX의 공식 보장이나 모든 문서 구조에 대한 결론으로 일반화하지 않는다.

## 검증 명령

```bash
./gradlew :server:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
android run --device=emulator-5554 --apks=app/build/outputs/apk/debug/app-debug.apk --activity=.MainActivity
android layout --device=emulator-5554 --pretty
android screen capture --output=raw/verification/2026-07-11-font-plus-fix/final-configured-clean.png
```

## 결과

- Gradle verification: PASS
- APK install/launch: PASS
- `+` state/price feedback: PASS
- plan selection feedback: PASS
- configure → review state consistency: PASS
- screenshot: `samples/remote-state-lab/raw/verification/2026-07-11-font-plus-fix/final-configured-clean.png`
