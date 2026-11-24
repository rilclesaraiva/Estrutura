# Changelog

Newest first. Each item says **what to check in a project's CLAUDE.md**, so an AI can compare content without any stored version. The first entry is cumulative: it is the baseline for projects that predate this repository.

## [20/11/2026] — baseline

Check that the project's CLAUDE.md has:

- Task list per request (one item per request, a final re-read of the maintainer's message against the list).
- Log first for bugs the AI cannot reproduce.
- A "Decisões recusadas" section with date and reason.
- Release only after an explicit "confirmo release vX.Y.Z"; version computed from [Unreleased]; the AI never creates a tag or Release, only the `chore(release)` commit.
- Every push produces an inspection build named `<slug>_vX.Y.Z-build<run#>`; only version and CHANGELOG change between inspection and release.
- CI posts build errors as a commit comment; the session reports it instead of claiming it tested.
- Everything on `main`, no branches or PRs.
- Android: `versionCode = MAJOR*10000 + MINOR*100 + PATCH`, never decreasing; signing configured at creation; R8 on in release with `-dontobfuscate`; one keystore, never a second.
- Secrets: the AI generates and sends the files, never reads or prints them; the maintainer registers them on GitHub.
- CHANGELOG rotation above 300 lines, with an `## [Earlier versions]` link section.
- Public projects: README in English with AI-transparency and support sections, legal pages (privacy, terms, licenses), no unsupported claims, no mention of donation inside the app.
- About screen, and update notice for public Releases, as in section 9 of the document.
