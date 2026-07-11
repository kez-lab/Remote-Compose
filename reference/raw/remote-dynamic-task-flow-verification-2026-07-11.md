---
title: Remote Compose dynamic task flow verification
type: evidence
created: 2026-07-11
as_of: 2026-07-11
---

# Remote Compose dynamic task flow verification

## Source-backed capability boundary

alpha14 `RcScope` exposes layout/drawing components including `Box`, `StateLayout`, `Column`, `Row`, `Flow`, `Text`, `Image`, and `Canvas`. The procedural modifier DSL exposes `verticalScroll`. The inspected alpha14 creation/core source does not expose a TextField/EditText/IME text-entry component comparable to Jetpack Compose `TextField`.

Engineering decision:

- text entry stays in the Android host as a native `OutlinedTextField` dialog;
- the remote document emits a typed intent through `hostAction("task.create")`;
- list, scroll, row interactions, and detail navigation remain in Remote Compose;
- create/delete API success triggers automatic document reload.

This is an alpha14 source observation, not a guarantee that future releases will not add remote input components.

## Implementation change

- Removed the fixed 4-bit, 16-state list fixture.
- Added a Ktor `TaskStore` with stable positive integer IDs and no four-item business cap.
- Added `POST /tasks` and `DELETE /tasks/{id}`.
- Added dynamic server-side row/detail document generation proportional to task count.
- Added Remote Compose `verticalScroll` for the list viewport.
- Added direct `StateLayout(screen)` list/detail transitions.
- Removed the manual server sync action and button.

## Emulator verification

| assertion | result |
|---|---|
| initial 3-item list | PASS |
| row → detail → list transition | PASS |
| remote action opens native text editor | PASS |
| typed task persists, R1/3 → R2/4 | PASS |
| 12-item server document renders | PASS |
| list scroll reveals task #12 | PASS |
| typed task detail renders | PASS |
| detail delete, R10/12 → R11/11 | PASS |
| server/app tests, APK, lint | PASS |

Screenshots and action evidence: `samples/remote-state-lab/raw/verification/2026-07-11-dynamic-task-flow/README.md`.

## Limit that remains

“No fixed four-item cap” does not mean physically unlimited. Every server task currently produces a row and a detail state in the binary document. Payload and operation count therefore grow linearly, and the Android client intentionally keeps its 512 KiB document safety limit. alpha14 has scroll support, but this POC has not established a supported lazy/virtualized remote list contract.
