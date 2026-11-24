# Estrutura Universal para Vibe Coding

> Versão do documento: **20/11/2026**. O `CLAUDE.md` de cada projeto registra a versão que segue; com uma versão nova, a IA compara as duas e alinha o projeto (seção 2, regra 5).
>
> Acompanham este documento dois arquivos de modelo: `modelos/ci.yml` (workflow, seção 7) e `modelos/android-build.gradle.kts` (versão, assinatura e R8 no Android). O documento tem as regras; os modelos, o código.
>
> Entregue este arquivo a um chat/agente de IA: *"Leia este documento e monte o projeto [descrição] em [stack]."*

**Princípio:** sistemas simples sobrevivem melhor a erros. Todo projeto começa na estrutura mínima, e uma peça nova só entra quando um gatilho da seção 3 aparece. Quanto menos peças, menos atrito.

---

## 0. Antes de criar: dados do mantenedor

O documento não traz dados pessoais de ninguém. Antes de criar o projeto, a IA **pergunta ao mantenedor** e registra as respostas no `CLAUDE.md` (bloco *Mantenedor*):

| Dado | Usado em | Se não houver |
|------|----------|---------------|
| **Tipo do projeto:** público ou pessoal/privado (primeira pergunta) | Licença, páginas legais, Pages, seções de IA e apoio do README, conteúdo do Sobre e aviso de atualização | Perguntar; não presumir |
| Nome ou apelido público | Créditos, páginas legais, assinatura (`O=` do keystore) | Usar o usuário do GitHub |
| E-mail de contato (só projeto público) | README, páginas legais | Usar só "abra uma issue" |
| Link de apoio financeiro (só projeto público) | Seção de apoio do README | Omitir só a parte de doação; o contato fica |
| Licença (só projeto público) | `LICENSE` | Sugerir GPL-3.0 (copyleft) ou MIT (permissiva) e deixar o mantenedor escolher |
| País (código de 2 letras) | Assinatura (`C=` do keystore) | Perguntar; não presumir |
| Onde guardar segredos | Seção 7.4 | Sugerir uma pasta privada num serviço de nuvem (ex: Google Drive) ou um gerenciador de senhas |

A IA **nunca inventa nem reaproveita** esses dados de outro projeto ou de outra pessoa.

**Público** aqui quer dizer repositório público e uso por terceiros. App distribuído com repositório privado é um caso misto: o convite de apoio no app leva ao repositório, o Pages gratuito exige repo público e o README não é visto. Nesse caso, a IA avisa e pergunta como seguir.

---

## 1. Estrutura mínima (sempre o ponto de partida)

```
projeto/
├── CLAUDE.md        # regras e contexto para a IA
├── CHANGELOG.md     # histórico de mudanças
├── README.md        # para humanos; seções de IA e de apoio só em projeto público
├── LICENSE          # só em projeto público; escolhida pelo mantenedor (seção 0)
├── .gitignore
├── .github/workflows/ci.yml   # build + testes em todo push; publica versão nova (seção 7)
└── <pasta de código da stack>/
    ├── core/        # base compartilhada
    ├── data/        # tudo que toca o mundo externo
    └── features/    # uma pasta por funcionalidade
```

**Também na criação:**
- **Ícone:** todo app nasce com um ícone original que representa o projeto, em formato vetorial e no local nativo da stack (Android: ícone adaptativo com camada monocromática; web: favicon SVG; Linux: SVG). Nada de marcas ou personagens de terceiros. **Depois de criado, só muda se o mantenedor pedir.**
- **Pacote Android:** sempre `com.<nome>.app`, com o nome do app em minúsculas, sem espaços, hífens nem acentos (ex: Engram → `com.engram.app`, Boom Listener → `com.boomlistener.app`). Vale para `applicationId` e `namespace`. Ele não pode mudar depois de publicado na Play Store, então é definido uma vez e registrado no `CLAUDE.md`.
- **Tela "Sobre":** todo app nasce com ela (seção 9.2). **Aviso de atualização:** todo app distribuído por Releases públicas do GitHub (seção 9).
- **Versão (Android):** uma fonte só, `APP_VERSION=X.Y.Z` em `gradle.properties`, lida pelo `build.gradle.kts` para o `versionName`. O `versionCode` é derivado dela (`MAJOR*10000 + MINOR*100 + PATCH`) e ninguém edita `versionCode` nem `versionName` à mão. **O `versionCode` nunca desce:** o esquema é escolhido na criação e não muda depois da primeira Release pública, porque um número menor impede o app de atualizar por cima. Projeto que já publicou com outro esquema (ex: o número da execução do CI) fica com ele.
- **Assinatura (Android):** configurada já na criação (seção 7.4). Sem ela, o APK de release sai sem assinatura e não instala, e não há o que vistoriar.
- **R8 (Android):** sempre ativo no build de release, para encolher e otimizar o app. **Sem ofuscação:** os nomes do código ficam legíveis, para que os erros vindos do aparelho possam ser lidos e corrigidos. Ofuscar não melhora o desempenho, e num repositório público não esconde nada.
  Configuração em `modelos/android-build.gradle.kts`. Em `proguard-rules.pro`, a primeira linha é `-dontobfuscate`. Ofuscar só com pedido explícito do mantenedor.

  O R8 só atua no release. Se algo funcionar no debug e quebrar no release (em geral, código que usa reflexão, como serialização), a correção é uma regra `-keep` em `proguard-rules.pro`, nunca desligar o R8. Como o build para vistoria do Actions já é o release com R8, esses problemas aparecem na vistoria (seção 7.5).

---

## 2. Papéis, não caminhos (como adaptar a qualquer stack)

`core`, `data` e `features` são **papéis**. A IA cria essas três pastas dentro da pasta de código **nativa da stack**. O que importa é o papel, não o nome exato: em projeto novo, usar `core`, `data` e `features`; em projeto existente, manter os nomes que já existem (ex: `feature` no singular). Todo o resto (nomes de arquivo, testes, build) segue a convenção da própria stack.

| Papel      | O que vai                                                      | Pode importar |
|------------|----------------------------------------------------------------|---------------|
| `core`     | Config, erros, utilitários, UI base. **Sem regra de negócio.** | nada interno  |
| `data`     | Banco, APIs, arquivos, cache, modelos                          | core          |
| `features` | Uma pasta por funcionalidade: lógica, telas, testes            | core, data    |
| entrada    | O ponto de entrada da stack (main, Activity, index) monta tudo | tudo          |

Regras:
1. **Feature nunca importa outra feature.** O que é compartilhado sobe para `core` ou `data`.
2. **Onde fica:** na pasta de código da stack. Ex.: Android → pacotes `core`, `data`, `features` em `app/src/main/java/<pacote>/`; JS/web → `src/core`, `src/data`, `src/features`.
3. **Navegação entre features** passa pela entrada (rotas/telas registradas lá), nunca por import direto.
4. **Registro:** o mapeamento real fica em uma linha no `CLAUDE.md` (campo *Mapa*). Nada de documento por stack.
5. **Projeto existente (ou versão nova deste documento):** a IA alinha as regras (CLAUDE.md, CI, README, release) sem reestruturar o código. Renomear pastas ou mover arquivos só com pedido do mantenedor. Antes, compara o `CLAUDE.md` antigo com este documento e mostra ao mantenedor as regras que se contradizem; ele decide qual fica, e a outra é apagada do `CLAUDE.md`. Duas regras opostas no mesmo arquivo fazem a IA escolher sozinha.

---

## 3. Crescer só com gatilho

| Gatilho                                                      | O que adicionar                                     |
|--------------------------------------------------------------|-----------------------------------------------------|
| `CLAUDE.md` passou de ~150 linhas ou o projeto tem 5+ features | `ARCHITECTURE.md` (arquitetura e decisões saem do CLAUDE.md) |
| A IA erra o que você quer numa feature                       | Spec curta em `docs/features/<nome>.md` (objetivo, fluxo, fora do escopo) |
| Um arquivo de feature passou de ~300 linhas                  | Dividir a feature em lógica / dados / tela          |
| O projeto vai ter usuários reais                             | Testes da lógica que importa                        |
| O projeto é público (loja, site, release para terceiros) | Páginas legais e GitHub Pages (seção 8); projeto pessoal não tem |
| A mesma tarefa se repete toda semana (ex: corrigir bugs relatados) | Issues como fila de tarefas (seção 7.6)             |
| Uma revisão (seção 7.5) ou investigação deixou hipóteses ainda não provadas | `ACHADOS.md` (seção 4.1)                         |

**Remover também é crescer:** código e features sem uso saem de verdade, em vez de acumular.

---

## 4. Template: CLAUDE.md

````markdown
# CLAUDE.md

Lido no início de cada sessão. Em conflito entre uma instrução antiga do chat e este
arquivo, vale este arquivo. Decisão nova que mude algo aqui entra neste arquivo no
mesmo commit. Segue o documento de estrutura de [data da versão].

## Projeto
[Nome] — [o que faz e para quem].
- Tipo: [público | pessoal/privado] · Público-alvo: [global | regional: Brasil]
- Nome nos arquivos: `[slug]` (ex: boomlistener_v1.2.0.apk; título da Release: "[Nome] v1.2.0")

## Mantenedor
- Nome: [nome público] · Contato: [e-mail ou "issues"]
- Apoio: [link de apoio ou "nenhum"] · Licença: [ex: GPL-3.0 | nenhuma (pessoal)]
- País (assinatura, `C=`): [código de 2 letras]
- Segredos ficam em: [ex: Google Drive/<Projeto>/, gerenciador de senhas]

## Stack
- Stack: [ex: Kotlin + Jetpack Compose]
- Build: `[comando]` · Testes: `[comando]`
- Mapa: core → `[caminho]` · data → `[caminho]` · features → `[caminho]`
- Versão: `[arquivo onde a versão mora]` · lida no CI por `VERSION_CMD`
  (Android: `APP_VERSION` em `gradle.properties`; versionCode = [esquema], nunca desce)
- Versionamento: SemVer (regra; o número é calculado pelo [Unreleased], nunca escolhido)
- Release: `[comando de release]` → `[artefatos gerados]`
- Pacote (Android): `com.[nome].app`, fixo · R8 ativo no release (sem ofuscação), nunca desligar
- Ícone: `[caminho]`, só muda se o mantenedor pedir
- Ajustes: tela inicial só com as categorias + bloco Sobre ("Sobre o [App]"); nada
  solto. Sobre: ícone, versão e tipo de build (do build), descrição, Desenvolvedor,
  Projeto (código-fonte, novidades, issues), Legal (privacidade, termos, licenças;
  abrem no navegador) e apoio (só leva ao repo). Pessoal: cabeçalho, descrição e
  novidades. Toda opção mora numa categoria; se nenhuma serve, perguntar.
- Atualizações: [sim, via Releases públicas | não: repo privado / Play Store / web].
  Consulta ao abrir e no máximo a cada 10 min ao voltar, com ETag; "Agora não" só na
  sessão; sino adia 1 semana / 1 mês ou pula a versão. Em Sistema → Atualizações:
  "Verificar atualizações" e "Avisos de atualização" (painel de baixo: avisar
  normalmente / 1 semana / 1 mês)

## Arquitetura
[2–5 linhas. Quando crescer, mover para ARCHITECTURE.md e deixar só o link.]

## Regras de estrutura
- Feature não importa outra feature; o compartilhado sobe para core/data.
- Navegação entre features passa pela entrada (rotas registradas lá).
- Banco, APIs e arquivos só via data. Core sem regra de negócio.
- Seguir as convenções da stack. Nenhuma dependência nova sem justificar; antes de
  usar uma versão nova, conferir na documentação oficial que ela existe e o que exige.
- Dados da pessoa já guardados: mudança de formato sempre com migração escrita e
  testada; nunca apagar dados para "começar limpo".

## Limites do produto (não mudam)
- [O que o app nunca faz: permissões que nunca pede, serviços que nunca usa,
  dados que nunca saem do aparelho. Mudar algo aqui só com o mantenedor.]

## Fluxo de trabalho
- Todo pedido vira tarefa na lista de tarefas da sessão (a ferramenta de tarefas,
  não a memória), antes de começar: um item por pedido, mesmo pequeno. Mensagem com
  6 pedidos = 6 itens. Um item só é marcado como feito quando está feito.
- Antes de dizer que terminou: reler a mensagem original do mantenedor e conferir
  item por item contra a lista. Se a sessão acabar antes, o que ficou pendente vai
  para *Contexto atual* (ou vira issue), para a próxima sessão continuar.
- Uma tarefa por vez, mudanças pequenas. Não refatorar fora do escopo.
- Tudo direto na `main`: sem branches (`wip/`, `feature/`…) e sem PR. Cada push
  gera o build para vistoria. Se o ambiente exigir commit ao parar, é na `main`.
- Rodar build e testes antes do push quando o ambiente permitir. Se a sessão não
  consegue compilar (ex: nuvem sem SDK ou sem acesso às dependências), o CI é a
  única prova, e a IA diz isso em vez de afirmar que testou.
- Só dizer que terminou quando o CI daquele commit passar no GitHub.
  CI vermelho é corrigido antes de qualquer tarefa nova. Quando falha, o CI posta o
  erro como comentário no commit: `gh api repos/OWNER/REPO/commits/<sha>/comments`.
- Achados de revisão e hipóteses ainda não provadas vão para `ACHADOS.md`, nunca
  para este arquivo; ao provar, anotar ao lado (provado / refutado / corrigido,
  data e commit), sem apagar. Não agir sobre eles sem o mantenedor pedir.
- Registrar toda mudança em [Unreleased] no CHANGELOG.md. Ler só o topo dele (até a
  primeira versão publicada); arquivos de `changelog/` só se o mantenedor perguntar
  sobre uma versão antiga, e só o arquivo dessa versão.
- Preferir remover a acumular: o que não é usado sai.
- Na dúvida, perguntar. Nunca inventar API.
- Bug que a IA não consegue reproduzir: log primeiro, correção depois. Pedir o
  registro (ou criar um modo de registrar) antes de mexer no código; só chamar de
  causa o que o log sustentar, o resto é hipótese e é dito como hipótese. Vale
  perguntar se outro app tem o mesmo sintoma (pode ser do sistema, não do app).
- Bloqueio de segurança do ambiente (proxy, permissão, leitura de credencial) nunca
  é contornado: o passo vai para o CI ou para o mantenedor.

## Release (regra absoluta)
- Só com confirmação explícita do mantenedor citando a versão
  ("confirmo release v1.2.0"), na conversa.
  "Ok", "beleza" ou silêncio não contam.
- A versão é calculada pelo [Unreleased]: só Fixed/Security → PATCH; Added,
  Changed ou Deprecated → MINOR; quebra de compatibilidade → MAJOR. Subir um número
  zera os seguintes (1.8.0 → 1.8.1 / 1.9.0 / 2.0.0). Número pedido fora da regra:
  explicar o número certo e esperar a confirmação com ele.
- Passos após a confirmação: (1) revisão com contexto limpo: um subagente lê só este
  arquivo e o diff desde o último release e aponta falhas; o mantenedor decide se
  corrige antes; o que não for corrigido vai para `ACHADOS.md`. (2) mover
  [Unreleased] para `## [X.Y.Z] - data`. (3) se o CHANGELOG.md passar de 300 linhas,
  mover as versões anteriores (menos a nova) para `changelog/vA-vB.md`, que nunca
  mais muda, e terminar o CHANGELOG.md com `## [Earlier versions]` e o link para
  `changelog/` (o título impede o link de entrar nas notas da Release). (4) subir a
  versão. (5) commit `chore(release): vX.Y.Z` na main.
- Sem confirmação: nada de bump de versão ou mover [Unreleased].
- A IA nunca cria tag nem Release: o commit `chore(release): vX.Y.Z` na main basta,
  e o CI cria a tag e publica quando vê versão nova com seção no CHANGELOG.
- Toda atualização vai para os artefatos do Actions (build para vistoria) e a IA
  avisa com o link da execução do workflow `CI` **daquele commit** (conferir o nome
  do workflow e o commit; nunca a do "pages build and deployment", que também
  aparece na lista quando `docs/` muda). O mantenedor vistoria e só então pede o
  release público.
- Ferramenta temporária de teste ou diagnóstico (registro de log, botão de teste)
  nunca sai em release pública: antes do commit de release ela é removida por
  inteiro, o que gera nova build e nova vistoria.
- Entre a vistoria e o commit de release, só mudam versão e CHANGELOG; mudou código, nova vistoria.

## Segredos
- Nunca commitar chaves, keystores, senhas ou .env.
- Secrets de assinatura (se a plataforma exigir): a IA gera e envia os arquivos direto
  ao mantenedor, sem nunca ler ou exibir o conteúdo. O mantenedor guarda no local
  definido em *Mantenedor*. Nunca no repo, nunca escritos no texto do chat.
- Cadastrar secrets no GitHub é sempre tarefa manual do mantenedor.

## Idioma
- README e CHANGELOG: [inglês (global) | português (regional)].
- Textos visíveis do app sempre nos arquivos de idioma, nunca escritos no código.

## Projeto público (só se Tipo = público)
- README com a seção de transparência sobre IA (nunca remover) e, no fim, a de
  apoio e contato com os dados de *Mantenedor*.
- No app: nunca citar doação ou pagamento. Só "Quer apoiar o projeto? Entre no
  repositório do projeto" com link para o repo.
- Páginas legais em `docs/` (privacidade, termos, licenças; cookies se houver),
  publicadas no GitHub Pages e linkadas no README e em Sobre → Legal. Sempre abrem
  no navegador: o app não tem telas com esses textos. Biblioteca ou serviço novo →
  `docs/licenses.md` no mesmo commit.
- Coleta nova (SDK, permissão, chamada de rede, cookie) → atualizar a política
  no mesmo commit.
- Nenhuma afirmação sem respaldo: o que README, loja e páginas legais dizem
  precisa ser verdade no código. Na dúvida, não afirmar.
- Projeto pessoal/privado: nada desta seção. Tudo entra quando virar público.

## Decisões mantidas (não propor mudar sem o mantenedor pedir)
- [Comportamento que parece errado mas é de propósito] ([data]): [motivo].

## Decisões recusadas (não propor de novo sem o mantenedor pedir)
Toda ideia recusada (da IA ou de usuário) entra aqui no mesmo commit, com data e
motivo. Antes de propor algo, conferir esta lista e a de mantidas.
- [Ideia] ([data]): [motivo em uma linha].

## Contexto atual
- Foco: [...] · Problemas conhecidos: [...]
````

### 4.1 `ACHADOS.md` (só com o gatilho da seção 3)

Guarda o que uma revisão ou investigação encontrou e **ainda não foi provado**: bugs prováveis, dúvidas de privacidade, testes que faltam. Fica fora do `CLAUDE.md` de propósito, para hipóteses não virarem regra. Cada item diz onde está (arquivo e trecho), por que é suspeito e **como provar**. Ao provar, o resultado vai ao lado do item (*provado*, *refutado* ou *corrigido*, com data e commit), sem apagar, para a mesma hipótese não ser investigada duas vezes.

**Decisões recusadas:** toda ideia que o mantenedor recusa (proposta da IA ou pedido de usuário) entra nesta lista no mesmo commit, com data e motivo. Antes de propor algo, a IA confere a lista. A ideia só volta se o mantenedor pedir.

---

## 5. Template: CHANGELOG.md

Formato [Keep a Changelog](https://keepachangelog.com/) + [SemVer](https://semver.org/). **Mesmo idioma do README**, porque as notas viram o texto do GitHub Release.

````markdown
# Changelog

## [Unreleased]
### Added
### Changed
### Fixed

## [0.1.0] - YYYY-MM-DD
### Added
- Initial structure.
````

- Categorias: Added, Changed, Deprecated, Removed, Fixed, Security, e **Known issues** para o que ainda está quebrado.
- **Versionamento é regra, não sugestão.** O número vem do que está em `[Unreleased]`, e a IA calcula; nem ela nem o mantenedor escolhem um número fora da regra:

  | O que mudou desde a última versão | Sobe | Zera | Ex: a partir de 1.8.0 |
  |---|---|---|---|
  | Só correções (*Fixed*, *Security*) | PATCH | — | 1.8.1 |
  | Algo novo ou alterado que continua compatível (*Added*, *Changed*) | MINOR | PATCH | 1.9.0 |
  | Algo que quebra compatibilidade (*Removed*, mudança incompatível) | MAJOR | MINOR e PATCH | 2.0.0 |

- *Deprecated* conta como MINOR. *Known issues* sozinho não gera versão nova.
- Se o mantenedor pedir um número que não bate com a regra (ex: "2.8.0" depois de uma mudança pequena em 1.8.0), a IA não usa esse número: mostra o número certo pela regra e o motivo, e o release só sai quando a confirmação citar o número certo.
- Trocar o **sistema** de versionamento (ex: para versão por data) só com pedido explícito do mantenedor. O sistema em uso fica registrado no `CLAUDE.md`, e enquanto ele valer, é regra.

### 5.1 Abertura e fechamento (para o CHANGELOG não crescer sem fim)

- **Aberto:** `CHANGELOG.md` na raiz, sempre o único em uso. Tem o `[Unreleased]` e as versões desde o último fechamento.
- **Quando fecha:** só no commit de release (seção 7.5), e só se o `CHANGELOG.md` passar de **300 linhas** (alguns milhares de tokens). Fora do release, nunca.
- **Como fecha:** todas as versões publicadas, menos a que está sendo lançada, vão para `changelog/vA-vB.md` (A = a mais antiga, B = a mais recente do bloco, ex: `changelog/v0.1.0-v0.55.0.md`). O `CHANGELOG.md` fica com o título, o `[Unreleased]`, a versão nova e, no fim, a seção `## [Earlier versions]` com a linha *"Previous versions (A to B): [changelog/](changelog/)"*, no idioma do CHANGELOG.
- **Por que o título com colchetes:** o CI copia as notas da Release até o próximo `## [`. Sem esse título, a linha do link entraria nas notas da versão lançada.
- **Arquivo fechado não muda mais.** Correção de algo antigo entra como item novo no `[Unreleased]`.
- **Por que a versão nova fica:** o CI tira dela as notas da Release, e a próxima pessoa lendo o `CHANGELOG.md` ainda vê a última versão publicada.
- **Leitura:** a IA lê só o topo do `CHANGELOG.md`. Um arquivo de `changelog/` só é aberto quando o mantenedor pergunta sobre uma versão antiga, e o nome do arquivo diz qual abrir.

---

## 6. README

**Idioma:** projeto global → README inteiro em inglês (também é o padrão na dúvida). Projeto regional → README inteiro no idioma da região. Nunca misturar idiomas.

**Ordem:** nome e uma frase → screenshots → funcionalidades → privacidade/permissões → **seção de IA** → instalação → licença → links legais → **apoio e contato (sempre por último)**. Em projeto pessoal, o README fica só com nome, funcionalidades e instalação; as seções em negrito, a licença e os links legais entram quando o projeto virar público.

**Seção de IA:** obrigatória em todo projeto público. A IA ajusta os colchetes, mas mantém os cinco pontos e só linka arquivos que existem (inclua `ARCHITECTURE.md` quando ele existir).

**Projeto regional:** os modelos abaixo estão em inglês; a IA traduz para o idioma da região, mantendo o mesmo conteúdo e os mesmos pontos.

```markdown
## 🤖 Built with AI ("vibe coding")

In the spirit of transparency: **[Project] is developed with heavy use of AI.**
Most of the code is written by [Claude](https://claude.ai) (Anthropic) under the
direction of the human maintainer, a workflow often called *vibe coding*.
We take care that it doesn't turn into AI slop:

- **The maintainer decides, the AI executes.** Every feature starts from a real
  need, and every build is tested on [real devices] before the next round.
- **Written rules the AI has to follow.** [`CLAUDE.md`](CLAUDE.md) sets the
  architecture, privacy limits and process, and is updated with every decision.
- **Tests and automated builds.** Every change goes through GitHub Actions,
  and a failing test means no build.
- **No release without approval.** Nothing is published without the
  maintainer's explicit approval.
- **Honest changelogs.** [`CHANGELOG.md`](CHANGELOG.md) says what changed,
  including what is still broken.

If AI-assisted code is a concern for you, now you know, and you are welcome to
read every line. Found something careless? Please [open an issue](../../issues).
```

### Apoio e contato (projeto público, sempre no fim do README)

Preenchido com os dados de *Mantenedor* do `CLAUDE.md`. Sem link de apoio, a seção fica só com o contato. O botão usa o serviço escolhido pelo mantenedor (o exemplo abaixo é do Buy Me a Coffee).

```markdown
## ☕ Support the project

[Project] is free and maintained independently. If it's useful to you, you can
support its development with a coffee:

[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-support-FFDD00?logo=buymeacoffee&logoColor=black)]([LINK DE APOIO])

**Contact:** [[E-MAIL]](mailto:[E-MAIL])
```

**Dentro do app, nunca citar doação ou pagamento diretamente.** Sem link para o serviço de apoio, sem "doe", sem valores. Só um convite que leva ao repositório, no fim da tela Sobre (seção 9.2), no idioma do app:
*"Want to support the project? Visit the project repository."* → link para `https://github.com/OWNER/REPO` (traduzido no idioma do app; em português: *"Quer apoiar o projeto? Entre no repositório do projeto."*)

---

## 7. Release e CI (qualquer stack)

Faz parte da estrutura mínima: o projeto já nasce com o workflow, para que todo push gere o build para vistoria desde o primeiro commit. Um único workflow para qualquer stack. Só muda o bloco `env` do topo, com os mesmos dados registrados no `CLAUDE.md` (*Build*, *Testes*, *Versão* e *Release*).

Ao instalar, a IA confere e usa a versão mais recente de cada action (`checkout`, `setup-*`, `upload-artifact`, `action-gh-release`); as do exemplo são as que rodavam nos projetos em novembro de 2026.

**O CI fala com a IA pelo commit.** A sessão da IA não consegue ler os logs do Actions, então, quando o build falha, o próprio workflow posta o erro como comentário no commit. A IA lê com `gh api repos/OWNER/REPO/commits/<sha>/comments`. Sem esse passo, a regra "CI vermelho é corrigido antes de tudo" não tem como ser cumprida.

### 7.1 Setup (uma vez por projeto)

| # | Quem        | O quê |
|---|---------|-------|
| 1 | IA          | Envia o link do Claude GitHub App: `https://github.com/apps/claude/installations/new` |
| 2 | Mantenedor  | Instala o app no repositório desejado (sem isso o Claude não acessa o repo) |
| 3 | IA          | Faz commit do workflow (`modelos/ci.yml`, seção 7.2) com `APP_NAME`, `APP_SLUG` e as variáveis da stack (7.3) |
| 4 | IA          | **Só se a plataforma exigir assinatura:** gera os dois arquivos e envia direto ao mantenedor (7.4) |
| 5 | Mantenedor  | **Só se houver secrets:** guarda os arquivos no local seguro dele e cadastra em `https://github.com/OWNER/REPO/settings/secrets/actions/new` |
| 6 | IA          | **Só depois que o mantenedor confirmar o passo 5:** apaga as cópias locais |

### 7.2 Workflow (`.github/workflows/ci.yml`)

Todo push compila o app, roda os testes e deixa o **build para vistoria** nos artefatos do Actions: é o mesmo tipo de build que vai ao público (no Android, release assinado e com R8). Se o build quebrar, o CI fica vermelho. Em todo push na `main`, o job `release` lê a versão do projeto: se ela ainda não tem tag e já tem seção no CHANGELOG, **o próprio CI cria a tag `vX.Y.Z`** naquele commit e publica a Release assinada. Nos pushes comuns a versão já tem tag, e ele não faz nada. A versão só muda com a confirmação do mantenedor (seção 7.5), então nada é publicado sem ela. A assinatura do build de vistoria roda quando os secrets existem; no release, com `REQUIRE_SIGNING`, a falta deles barra a publicação. No Android, sem os secrets o APK de vistoria sai sem assinatura e não instala: por isso a assinatura entra já na criação.

Modelo completo em `modelos/ci.yml`. O que ele faz, e que nenhum projeto pode perder ao adaptar:

- **Job `ci`, em todo push:** build + testes com a saída em `build.log`; **se falhar, posta o erro como comentário no commit**; nomeia e sobe o build para vistoria (`if-no-files-found: error`).
- **Job `release`:** lê a versão (`VERSION_CMD`); sai sem fazer nada se a tag já existe (`git ls-remote`) ou se o CHANGELOG não tem a seção da versão; tag manual que não bate com a versão ou sem CHANGELOG falha; copia as notas com comparação literal até o próximo `## [`; exige assinatura (`REQUIRE_SIGNING`); cria a tag `vX.Y.Z` e publica com o título "[App] vX.Y.Z".
- **`concurrency: release`:** duas execuções nunca publicam a mesma versão.
- **Android:** Java 21 para o Gradle, `setup-gradle` para o cache, sem `android-actions/setup-android` (o SDK já vem no runner).

### 7.3 Ajustes por stack (exemplos testados na prática)

| Stack            | `VERSION_CMD` (imprime a versão) | `TEST_CMD` (build + testes)    | `RELEASE_CMD`                  | `ARTIFACTS`                 | Setup |
|------------------|------------------------|--------------------------------|--------------------------------|-----------------------------|-------|
| Android          | `grep -m1 -oP '^APP_VERSION=\K\S+' gradle.properties` | `./gradlew testDebugUnitTest assembleRelease` | `./gradlew assembleRelease` | `.apk` de release | `setup-java` 21 + `setup-gradle` |
| Web/Node         | `node -p "require('./package.json').version"` | `npm ci && npm run build && npm test && (cd dist && zip -qr ../site.zip .)` | igual ao `TEST_CMD` | `site.zip` | nenhum (já vem no runner) |

- Outras stacks seguem a mesma lógica: comando que imprime a versão, comando de teste, comando de release, artefatos. Uma linha nova só entra nesta tabela depois de rodar num CI de verdade.
- Empacotamento extra (AppImage, Flatpak, `.deb`) entra no `RELEASE_CMD` só quando for necessário.
- `TEST_ARTIFACTS` e `ARTIFACTS` apontam para **um arquivo por extensão** (APK, binário, `.zip`). Saída com muitos arquivos, como o `dist/` de um site, é compactada num `.zip` antes.
- **AAB não vai para a Release pública.** O `.aab` é o formato de envio à Play Store, não de instalação: quem baixa da Release precisa do APK. Quando o projeto for publicado na loja, o `.aab` é gerado à parte (`./gradlew bundleRelease`) e enviado por quem publica, sem passar pela Release do GitHub.
- **Nomes dos arquivos** (qualquer stack, a extensão é mantida):
  - Build para vistoria: `<slug>_v<versão>-build<nº da execução do CI>` → `boomlistener_v0.56.0-build628.apk`.
  - Release pública: `<slug>_v<versão>` → `boomlistener_v0.56.0.apk`, título **"BoomListener v0.56.0"**.
  - A tag continua `v0.56.0`: o Git não aceita espaço em nome de tag, então o nome do app vai no título.
  - Dois artefatos com a mesma extensão travam o CI, para nenhum sobrescrever o outro.
- **Stack sem assinatura, ao instalar apagar:** as variáveis `REQUIRE_SIGNING`, `KEYSTORE_B64` e `ANDROID_*`, os dois steps *Assinatura* e o `setup-java`/`setup-gradle` (se a stack não for JVM). O passo que posta o erro no commit fica em toda stack. Fica só o que a stack usa.

### 7.4 Assinatura (só se a plataforma exigir — ex: Android)

**Gerar o keystore e o arquivo de secrets:**

```bash
SLUG="[slug]"; ALIAS="release"   # mesmo APP_SLUG do workflow
PASS="$(openssl rand -base64 24 | tr -d '/+=')"

keytool -genkeypair -keystore "$SLUG-release.p12" -storetype PKCS12 \
  -alias "$ALIAS" -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass "$PASS" -keypass "$PASS" -dname "CN=[App], O=[Nome do mantenedor], C=[País]"

{ echo ANDROID_KEYSTORE_BASE64; base64 -w0 "$SLUG-release.p12"; echo; echo
  echo ANDROID_KEYSTORE_PASSWORD; echo "$PASS"; echo
  echo ANDROID_KEY_ALIAS; echo "$ALIAS"; echo
  echo ANDROID_KEY_PASSWORD; echo "$PASS"; } > "$SLUG-github-secrets.txt"
```

**Entrega (a IA nunca lê o segredo):**
- **Por quê:** a sessão bloqueia a IA de ler material de credencial, e subir os arquivos por um conector (Drive, por exemplo) exigiria ler. Por isso a IA só gera e envia os arquivos; quem guarda é o mantenedor.
- **Antes de gerar:** se já existir um keystore do projeto (no repo ou com o mantenedor), **parar e perguntar.** Trocar o keystore de um app publicado impede atualizações.
- **Ao gerar:** nunca dar `cat`, imprimir ou abrir os arquivos, e nunca mostrar a senha. O script acima não exibe nada sensível.
- **Enviar** `<slug>-release.p12` e `<slug>-github-secrets.txt` como arquivos anexos na conversa, sem copiar nenhum valor para o texto.
- **O mantenedor** salva os dois no local definido em *Mantenedor* (privado, nunca compartilhado), cadastra os 4 secrets e avisa.
- **Só então** a IA apaga as cópias locais. O container da sessão é descartado depois de um tempo parado: se isso acontecer antes de o mantenedor salvar, gerar um par novo.
- **Atenção:** os anexos ficam no histórico da conversa. Não compartilhe essa conversa.

**Gradle:** `modelos/android-build.gradle.kts` (assinatura lida das variáveis que o CI exporta).

**Cuidados:**
- Na Play Store, o **Play App Signing** (obrigatório para apps novos) faz desta chave a chave de *upload*. Perdê-la deixa de ser fatal, porque o Google permite redefini-la.
- `.gitignore`: `*.p12`, `*.jks`, `*.keystore`, `*-github-secrets.txt`.
- PKCS12 usa a mesma senha para chave e keystore. Por isso os dois secrets de senha têm o mesmo valor.
- O base64 é colado numa linha só, sem espaços.

### 7.5 Vistoria e release

**A cada atualização:** a IA faz push → o CI gera o *build para vistoria* nos artefatos do Actions → a IA avisa com o link da execução do workflow `CI` daquele commit (em projeto com Pages, mudar `docs/` dispara também o "pages build and deployment", que pode aparecer primeiro na lista: conferir o nome antes de mandar o link) → o mantenedor instala e vistoria num aparelho real. É aqui que aparecem problemas que só existem no release, como os do R8.

**Só após "confirmo release vX.Y.Z":**

0. **Revisão com contexto limpo:** a IA abre um subagente (ou sessão nova) que lê só o `CLAUDE.md` e o diff desde o último release, e procura falhas de segurança, quebra das regras de estrutura e itens do CHANGELOG que não estão no código. Os achados vão para o mantenedor, que decide entre lançar assim ou corrigir antes (o que exige novo build e nova vistoria). O que ficar sem correção e sem prova vai para `ACHADOS.md` (seção 4.1).

   Antes da revisão, a IA confere que nenhuma ferramenta temporária de teste ou diagnóstico ficou no código: ela nunca sai em release pública e, se existir, é removida primeiro (nova build, nova vistoria).
1. Mover `[Unreleased]` para `## [X.Y.Z] - YYYY-MM-DD` e deixar um `[Unreleased]` vazio. Se o `CHANGELOG.md` passar de 300 linhas, fechar as versões anteriores (seção 5.1) neste mesmo commit, terminando com `## [Earlier versions]`.
2. Atualizar a versão no arquivo de versão do projeto (Android: `APP_VERSION`). O `versionCode` segue o esquema registrado no `CLAUDE.md` (seção 1) e nunca desce.
3. Commit `chore(release): vX.Y.Z` e push na `main`. **A IA não cria tag.**
4. O CI vê a versão nova sem tag e com seção no CHANGELOG, cria a tag `vX.Y.Z`, assina e publica a Release com as notas do CHANGELOG.

Entre a vistoria e o commit de release, **só mudam versão e CHANGELOG**. Qualquer mudança de código exige um novo build e uma nova vistoria.

**Por que a tag é do CI, e não da IA:** a sessão da IA acessa o GitHub por um intermediário (proxy) do ambiente, que pode permitir push na `main` e barrar a criação de tags, como já aconteceu. O CI cria a tag com a permissão do próprio repositório, sem passar pela sessão, e o único passo da IA fica sendo um push comum. Se um dia for preciso, uma tag criada à mão pelo mantenedor ainda dispara a publicação.

### 7.6 Issues como fila de tarefas (só com o gatilho da seção 3)

Sem PR: o fluxo é o mesmo de sempre, direto na `main`.

1. O mantenedor anota bugs e pedidos como issues, no celular ou onde estiver.
2. Na sessão, pede: *"resolva as issues abertas"* (ou uma específica).
3. A IA lê as issues, corrige uma por vez direto na `main` e fecha cada uma pelo commit (`Fixes #12`).
4. Cada push gera o build para vistoria (seção 7.5), como qualquer atualização.

Não usa o Claude Code GitHub Action: ele trabalha por branch e PR, e exigiria mais um workflow e mais um secret.

---

## 8. Projeto público: páginas legais e afirmações

Vale para todo projeto público (loja, site, release para terceiros). Projeto pessoal ou privado não tem licença, páginas legais, GitHub Pages nem as seções de IA e apoio do README.

**Quando um projeto pessoal vira público,** a IA faz de uma vez, a pedido do mantenedor: pergunta os dados da seção 0 que faltam (contato, apoio, licença), cria o `LICENSE`, as páginas legais e o Pages, acrescenta ao README as seções de IA, de apoio e os links legais, e confere o inventário da seção 8.3 antes de publicar. Antes disso, revisa o histórico por segredos e dados pessoais, porque o repositório ao se tornar público expõe todos os commits.

### 8.1 Páginas obrigatórias

```
docs/
├── privacy.md   # política de privacidade (sempre)
├── terms.md     # termos e condições (sempre)
├── licenses.md  # licença do app, bibliotecas e serviços usados (sempre)
└── cookies.md   # política de cookies (só se usar cookies ou rastreadores)
```

- **Externas ao app:** publicadas no GitHub Pages (Settings → Pages → branch `main`, pasta `/docs`), em `https://OWNER.github.io/REPO/privacy`. A Play Store exige essa URL pública para a política de privacidade. O Pages publica a pasta `docs/` inteira, inclusive as specs de `docs/features/`; como o repositório já é público, isso não expõe nada novo.
- **Linkadas** no README, no bloco *Legal* da tela Sobre (seção 9.2) e na página da loja. O app não tem telas com esses textos: tudo abre no navegador.
- **Biblioteca ou serviço novo** entra em `licenses.md` no mesmo commit.
- **Idioma:** o mesmo do README. Projeto regional no Brasil cita a LGPD; projeto global cita a LGPD e a GDPR quando atender usuários desses lugares.
- **Conteúdo mínimo:** quem é o responsável, contato (de *Mantenedor*), data da última atualização, quais dados são coletados e por quê, com quem são compartilhados, por quanto tempo ficam e como o usuário pede para apagar.

### 8.2 Cookies e rastreadores (só se usar)

Cookie ou rastreador **não essencial** (analytics, anúncios, pixels, SDKs de terceiros, também em app nativo) só é ativado depois de um consentimento em que recusar é tão fácil quanto aceitar e dá para mudar de ideia depois. Os estritamente necessários (sessão, login) dispensam o consentimento, mas entram na política.

### 8.3 Nenhuma afirmação sem respaldo

Tudo o que o projeto diz publicamente (README, loja, site, páginas legais) precisa ser verificável no código.

- **Antes de escrever a política,** a IA faz um inventário a partir do código: permissões, SDKs e bibliotecas de terceiros, chamadas de rede (inclusive a consulta de atualização ao GitHub, que expõe o IP do aparelho ao GitHub), armazenamento local e cookies. A política descreve esse inventário, nem mais nem menos.
- **Proibido sem prova:** "não coletamos nenhum dado", "100% seguro", "criptografia de ponta a ponta", "o mais rápido", números de usuários ou de desempenho não medidos.
- **Toda mudança de coleta** (SDK novo, permissão nova, nova chamada de rede) atualiza a política e a data no mesmo commit, e entra no CHANGELOG.
- **Na dúvida, não afirmar:** é melhor descrever o que acontece ("a localização é usada só no aparelho para desenhar o mapa") do que prometer o que não dá para provar.

**Aviso:** páginas legais geradas por IA são um ponto de partida, não aconselhamento jurídico. Para apps com coleta de dados sensíveis, pagamentos ou muitos usuários, vale uma revisão profissional.

---

## 9. Tela "Sobre" e aviso de atualização

**App de referência: [BoomListener](https://github.com/rilclesaraiva/BoomListener)** (`feature/settings/AboutScreens.kt`, `UpdatesSettings.kt`, `UpdatePrompt.kt` e `data/updates/`). Os textos exatos das telas, a ordem e o visual estão lá; aqui ficam as regras. Em outra stack, o mesmo resultado com os componentes dela.

### 9.1 Ajustes: onde cada coisa mora

- A tela inicial de Ajustes tem só as **categorias** do app (ex: Aparência, Reprodução, Biblioteca, Compartilhamento, Sistema) num bloco *Geral*, e embaixo um bloco *Sobre* com um único item: **"Sobre o [App]"**, com a versão (lida do build) no subtítulo. Nada de item solto.
- Toda opção mora numa categoria. Se nenhuma serve, a IA pergunta.
- As atualizações ficam em **Sistema → Atualizações**, nunca no Sobre (seção 9.3).

### 9.2 Tela "Sobre"

Página com título grande no topo (sem barra presa) e, nesta ordem:

| Bloco | Conteúdo | Ao tocar | Projeto pessoal |
|-------|----------|----------|-----------------|
| Cabeçalho | Ícone do app em destaque (pode ser animado, tocando ao entrar e a cada toque), nome, duas etiquetas: **versão** e **tipo de build** (RELEASE ou DEBUG), ambas lidas do build | — | Sim |
| Descrição | Uma frase centralizada do que o app é, seguindo a seção 8.3 | — | Sim |
| **Desenvolvedor** | Foto do perfil do GitHub do mantenedor, nome, país e perfil | Abre o perfil | Opcional |
| **Projeto** | Código-fonte · Novidades · Reportar um problema | Abrem no navegador: o repo, o `CHANGELOG.md` no GitHub e as issues | Só Novidades |
| **Legal** | Política de privacidade · Termos de uso · Licenças e atribuições (licença do app, bibliotecas e serviços usados) | Abrem no navegador as páginas do GitHub Pages (seção 8.1) | Não |
| Apoio (sem título) | O convite da seção 6 | Abre o repositório | Não |

- **Textos legais nunca viram tela dentro do app:** tudo abre no navegador, e assim só existe uma versão de cada texto.
- **Foto do desenvolvedor:** a guardada aparece na hora; o GitHub (`github.com/[usuário].png`) é consultado no máximo uma vez por semana, com `ETag` (resposta 304 não baixa a imagem de novo). Sem foto guardada e com falha, tenta de novo em 10 minutos. Fica fora do backup, porque não é dado da pessoa.
- **Todos os textos** vêm dos arquivos de idioma do app, nunca escritos no código.

### 9.3 Sistema → Atualizações

Vale para app instalável distribuído pelas **Releases públicas** do GitHub. Não vale para repositório privado (ler Releases privadas exige um token, que dentro do app pode ser extraído), para a versão da Play Store (a loja proíbe o app de se atualizar por fora dela) nem para web (o site já é sempre a última versão).

Bloco **Atualizações** dentro da categoria Sistema, com dois itens:

- **Verificar atualizações:** consulta manual, que ignora adiamentos. O subtítulo mostra o estado (verificando, em dia, versão nova, nenhuma publicada, sem internet). Com versão nova, tocar abre o download.
- **Avisos de atualização:** o subtítulo mostra o estado (ligados, pausados até uma data ou pulando uma versão). Tocar abre um **painel que sobe de baixo** com: avisar normalmente (desfaz pausa e pulo), pausar por uma semana, pausar por um mês.

### 9.4 O aviso de versão nova

**Peças (papéis da seção 2):**
- `data`: consulta a última Release pública (`GET /repos/OWNER/REPO/releases/latest`, que ignora rascunhos e pré-releases) e compara a versão com a instalada pela regra do SemVer. Guarda a última resposta e o estado dos avisos (no Android, SharedPreferences). Só existe aviso se houver Release publicada mais nova.
- `features`: o "anfitrião" do aviso (decide quando consultar e mostra o painel) e os itens de Sistema → Atualizações.
- entrada: coloca o anfitrião na raiz do app, para valer em qualquer tela.

**Quando consulta:**
- Abrindo o app do zero: sempre.
- Voltando ao app (ao ficar visível de novo): no máximo a cada 10 minutos.
- Antes da consulta, mostra na hora a última versão nova já guardada, sem esperar a rede.
- Funciona mesmo com um eventual modo offline do app ligado.

**Economia do limite da API do GitHub:**
- A consulta manda o `ETag` guardado (`If-None-Match`); "não mudou" (304) não gasta o limite.
- Resposta nova sempre substitui a guardada: o aviso nunca mostra versão ou notas antigas.
- A hora da consulta só é marcada quando o GitHub respondeu; falhar sem internet não conta.

**O painel (tela cheia):**
- Versão nova e a instalada, e as notas da Release, que são a seção da versão no CHANGELOG (seção 5). Uma linha `> destaque` vira parágrafo; sem notas, um texto genérico de correções.
- Botões: **Baixar** (abre o arquivo da Release, `<slug>_v<versão>.apk`, ou a página dela; quem instala é o sistema), **Agora não** e o **sino**.
- **"Agora não":** vale só até o app ser aberto de novo, e não guarda nada.
- **Sino:** pausar por uma semana, por um mês ou pular esta versão. Fica guardado e aparece em Sistema → Avisos de atualização, onde dá para desfazer.
- Depois que a pessoa responde, o aviso não volta na mesma sessão.
