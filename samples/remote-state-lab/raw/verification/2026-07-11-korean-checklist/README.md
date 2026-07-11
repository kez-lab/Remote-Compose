# Korean checklist Android journey evidence

Date: 2026-07-11  
Device: `emulator-5554` (`1080 × 2400`)  
Remote Compose: `1.0.0-alpha14`  
Server: local Ktor `http://0.0.0.0:8080`  
Client: `http://10.0.2.2:8080`

## Journey

```xml
<journey name="Remote checklist smoke">
  <action>Verify the native server connection screen</action>
  <action>Tap "서버 연결"</action>
  <action>Verify the full-screen checklist and "3개 작업"</action>
  <action>Tap "+ 작업 추가" and verify "4개 작업"</action>
  <action>Delete the Crashlytics task and verify "3개 작업"</action>
  <action>Tap "서버 동기화" and verify R1 changes to R2</action>
  <action>Press system back and verify the connection screen</action>
</journey>
```

| action | status | evidence |
|---|---|---|
| native connection screen | PASSED | `connection.png`; layout text `서버 문서에 연결합니다` |
| connect and render document | PASSED | `list-initial.png`; `릴리즈 체크리스트`, `3개 작업` |
| add list item | PASSED | `list-after-add.png`; `4개 작업`, `Crashlytics 대시보드 확인` |
| delete list item | PASSED | `list-after-delete.png`; `3개 작업`, Crashlytics row absent |
| host API sync and reload | PASSED | `list-after-server-sync.png`; `REMOTE RELEASE · R2`, 동기화 완료 문구 |
| back to connection | PASSED | final Android layout contains `서버 연결` |

## Files

- `connection.png`: native connection screen
- `list-initial.png`: initial 3-item Remote Compose document
- `list-after-add.png`: 4-item document-local state
- `list-after-delete.png`: item removed without a network request
- `list-after-server-sync.png`: allowlisted Ktor action and R2 document reload

## Verification commands

```bash
./gradlew :server:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
android run --device=emulator-5554 --apks=app/build/outputs/apk/debug/app-debug.apk --activity=.MainActivity
android layout --device=emulator-5554 --pretty
android screen capture --output=raw/verification/2026-07-11-korean-checklist/list-initial.png
```

Result: Gradle verification, install/launch, connection, add, delete, sync, and back navigation all passed.
