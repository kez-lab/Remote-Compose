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
