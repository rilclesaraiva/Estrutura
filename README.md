# Estrutura

A single, minimal document that any AI agent can follow to create and maintain software projects ("vibe coding"). It starts with the smallest structure, grows only when a trigger appears, and adapts to any stack through roles (`core`, `data`, `features`) instead of fixed paths.

> The document itself is written in Brazilian Portuguese.

## Contents

| File | What it is |
|------|------------|
| `estrutura-vibe-coding.md` | The rules: maintainer questions, structure, CLAUDE.md template, CHANGELOG, README, release/CI, legal pages, About screen |
| `modelos/ci.yml` | GitHub Actions workflow (inspection build on every push, public release by version) |
| `modelos/android-build.gradle.kts` | Android snippets: single-source version, signing from env vars, R8 |
| `VERSAO` | Version of the document (a date); projects compare it with the one they follow |
| `CHANGELOG.md` | What changed in each version |

The document and the two templates are meant to be used together.

## Using it in a project

Add this same line to the `CLAUDE.md` of any project, new or old. Nothing to edit: the AI writes the applied version itself once you accept or refuse each rule:

```
Estrutura universal: repositório rilclesaraiva/Estrutura. Na primeira mensagem de cada sessão, ler o VERSAO e o CHANGELOG.md de lá. Sem "Versão aplicada" neste arquivo, ou com uma anterior à do VERSAO, conferir o conteúdo: listar as regras do CHANGELOG que este CLAUDE.md ainda não cumpre e que não estejam em "Decisões recusadas", e perguntar ao mantenedor se aplica; nunca aplicar sozinha. Depois de aplicar ou recusar tudo, registrar aqui "Versão aplicada: <VERSAO>". Se o repositório não abrir, avisar e seguir com este arquivo.
```

## License

GPL-3.0. See `LICENSE`.
