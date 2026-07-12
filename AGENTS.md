# Remote Compose Research Repository

## Purpose

This repository is a durable, source-backed research base for AndroidX Remote Compose as an Android app-embedded SDUI runtime, plus Jetpack Compose, Ktor, Kotlin Multiplatform, and Compose Multiplatform where they affect that architecture.

## Directory map

- `reference/raw/`: immutable source catalogs and dated evidence snapshots. Add a new dated file instead of rewriting historical evidence.
- `reference/wiki/`: maintained synthesis, comparisons, architecture guidance, risks, and implementation plans.
- `reference/wiki/index.md`: first retrieval layer. Update it whenever a wiki page is added, renamed, or materially repurposed.
- `reference/wiki/log.md`: append-only record of research and maintenance.

## Research rules

1. Prefer official Android, AndroidX source, Google Maven, Kotlin, JetBrains, Ktor, and Google Play sources.
2. Mark current or volatile facts with an `as_of` date. Verify versions from release notes or artifact metadata before changing them.
3. Separate sourced facts, engineering inference, and recommendation. Never present an inference as an official guarantee.
4. Link to a pinned source commit when using source code as evidence. Use moving branch links only for discovery.
5. Do not copy full upstream documents into `raw/`. Store a link, access date, concise evidence note, and any contradiction discovered.
6. Treat AndroidX Remote Compose as distinct from generic server-driven UI and from Compose Multiplatform.
7. Record unresolved questions explicitly in `reference/wiki/questions.md`.
8. Keep current synthesis scoped to Ktor/JVM document production and Android app embedded playback. Do not reintroduce unrelated Android surface guidance unless the user explicitly expands the scope.

## Maintenance workflow

### Ingest

1. Read `reference/wiki/index.md` and the relevant existing pages.
2. Add a dated source snapshot under `reference/raw/`.
3. Update affected wiki pages and their `updated` dates.
4. Update `reference/wiki/index.md`.
5. Append an entry to `reference/wiki/log.md`.

### Review

- Recheck alpha/beta APIs, player visibility, wire protocol versions, platform support, minimum SDKs, and stable dependency versions.
- Look for contradictions between release notes, Maven metadata, API reference, and pinned source.
- Preserve old conclusions only when they remain historically useful; otherwise mark them superseded.

### Quality bar

- Architecture recommendations must include failure behavior, compatibility strategy, security boundaries, observability, and rollback.
- A Remote Compose prototype is not production-ready until it has malformed-document tests, resource limits, fallback UI, accessibility checks, and cross-version verification.

## Beginner documentation rules

1. Declare one producer path before teaching syntax. A Ktor/JVM tutorial must use procedural `RcScope`/`createRcBuffer` throughout its core path; an Android/CI capture tutorial may use public `remote-creation-compose`.
2. Never call server `RcScope.Text` Jetpack Compose `Text`, and never present it as shorthand for public `RemoteText`.
3. On first use of `.rs`, `.rb`, `.ri`, `.rf`, `.rdp`, or `.rsp`, state the Kotlin source type, remote target type, serialization purpose, and phase in which the value exists.
4. Every state example must explain producer/capture execution, emitted document model, player runtime update, and host/server boundary.
5. State whether a lambda runs during capture or playback. Do not imply that `RemoteStateLayout` content lambdas execute on player clicks.
6. Compare Jetpack Compose recomposition with Remote Compose ID/listener evaluation before introducing state APIs.
7. Label code as compile-oriented public API, abbreviated public pattern, restricted POC, or pseudocode. Omitted imports, helpers, or suspend context must be visible beside the block.
8. A source badge is not an explanation. Each API must answer why it exists, what document value/operation it represents, and what it does not do.
9. When an error or confusing omission is found, update `reference/wiki/api-syntax-audit.md`, affected synthesis, index when materially repurposed, and append the log.
10. Do not introduce a second creation frontend merely to be comprehensive. Put `RemoteText`, `.rs/.rb`, and public capture in an advanced module when the core tutorial targets server-side `RcScope` generation.
11. A server-path beginner must be able to explain `RcScope.Text`, Header/root content, `remoteNamedInteger`, `StateLayout`, `setValue`, `hostAction`, Ktor transport, and the ViewModel/Repository boundary before comparing frontends.
