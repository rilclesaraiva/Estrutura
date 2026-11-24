# Changelog

History of the Universal Structure for Vibe Coding. Newest first. The date in `VERSAO` is the date of the latest entry.

## [20/11/2026]

### Changed
- CI workflow and Android Gradle snippets moved out of the document into `modelos/ci.yml` and `modelos/android-build.gradle.kts`.
- README templates in the document are English only; a regional project gets a translation.
- Stack table limited to Android and Web/Node; a new stack enters only after running in a real CI.
- Cookies section reduced to a short paragraph; section 9 (About screen and updates) points to BoomListener as the reference app.

### Added
- Rules learned from real projects: commit-error comment in CI, task list per request with a final re-read, log first for bugs that cannot be reproduced, "Decisões recusadas" list, CHANGELOG rotation, versionCode never decreases.
