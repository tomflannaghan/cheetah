# cheetah

A crossword-solving dictionary app: type a pattern, get matching words, see their definitions. Kotlin Compose Multiplatform with a desktop (JVM) target and an Android target. The databases it reads are built by `~/src/cheetah-preprocessing`. `~/src/cheetah2` is a separate, newer Python rewrite (library, webapp, MCP server) and shares no code with this repo.

## Building from the terminal

```sh
./gradlew :android:assembleDebug :common:desktopTest
```

Gradle itself runs on the system Java, which is a JRE. Compilation uses a JDK 21 toolchain (`jvmToolchain`), which the foojay resolver plugin downloads into `~/.gradle/jdks` if none is installed. So no `JAVA_HOME` is needed.

`:desktop:run` launches the desktop app. Versions are in `gradle/libs.versions.toml`: Gradle 9.8, Kotlin 2.4, Compose Multiplatform 1.12, AGP 9.4, SQLDelight 2.4 (`app.cash`), lifecycle 2.11, kotlinx.serialization.

## Layout

- `common/` holds almost everything. `commonMain` has the UI (`gui/`), search engine (`search/`), data sources (`datasource/`), definition parsing (`definitions/`) and the SQLDelight schema (`sqldelight/`). `SearchViewModel` (`commonMain`) is a multiplatform `androidx.lifecycle.ViewModel` holding the UI state as `StateFlow`s. `desktopMain` and `androidMain` hold only `DesktopPlatform`/`AndroidPlatform`, which implement `Platform` (data directory and SQL driver).
- `desktop/` and `android/` are thin entry points that create the `SearchViewModel` with `viewModel { }` and call `App()`. On Android the ViewModel survives configuration changes, so the word lists are loaded once.
- Tests are all in `common/src/desktopTest`. There are none for Android.

## How a search runs

1. `stringToSearchQuery` (`search/SearchQuery.kt`) uppercases the input, splits it on newlines and `;`, trims each term, and ANDs the terms together. A blank query gives no results without searching. Each term is classified in this order:
   - `S:...` is full-text search. All `S:` terms are merged into one FTS4 `MATCH` of `"a" AND "b"`.
   - `R:word` is a relationship search: the words whose parent is `word` in the `derivedWord` table, e.g. plurals or thesaurus synonyms.
   - Anything containing `` / ` < > `` is a custom pattern (`search/custompattern/`): anagrams, misprints, sub-words.
   - Letters only (`[A-Z]+`) are a prefix search. A bare word is not an exact match.
   - `N`, `N-M`, `-M` or `N-` filter by entry length.
   - `W:N` matches phrases of N words, counted on the display string.
   - Anything else is a Java regex that must match the whole entry.
2. `searchQueryToMatcher` turns the query into a `Matcher` tree. `optimize()` wraps CPU-bound matchers in `ParallelChunkMatcher`, keeping DB-backed ones out of it, and reorders AND/OR children by heuristic weights. DB searches go first and custom patterns last.
3. Matching works on `Word.entry`, the canonical uppercase letters-only form, never on `Word.string`.

`README.md` documents only regex, `s:` and custom patterns. The prefix, length, `w:` and `r:` forms are documented only in the code.

`SearchContext` caches prefix trees, which are slow to build. It is rebuilt only when the selected word lists change. `SearchViewModel` runs searches and definition lookups with `collectLatest`, so a new query cancels the previous search, and `executeAsListSuspend` lets long DB reads notice cancellation.

## Data sources

`dataSources()` reads every `*.json` in the data directory. Each config names a sibling `.txt` (`TextFileWordList`) or `.sqlite` (`SqliteWordDatabase`) file with the same basename. Configs are decoded with kotlinx.serialization and read in file-name order. A config that fails to decode, has a bad colour or points at a missing file throws `InvalidDataSourceException`, which `dataSources()` prints and skips. Files without a `.json`, such as a backed-up `wiktionary-2021.sqlite`, are ignored. `data/` is gitignored. Only `ukacd.*` and `wiktionary.json`, added before that, are tracked. Everything else in it is local, and new files there need `git add -f`.

- Word list `.txt`: one `display string<TAB>CANONICAL` pair per line. Lines with an empty canonical form are dropped.
- `DataSourcesManager` merges every source's words into one sorted list and tags each word with a bitmask of its sources (`1 shl index`, an `Int`, so at most 32 sources).
- `useWordListByDefault` sets which sources start selected under "Word Lists" in the drawer. `useDefinitionsByDefault` sets which SQLite sources start selected under "Definitions". The selected ones are shown in the definition pane, in file-name order. SQLite sources with an empty `definition` table, such as Bradford's thesaurus, aren't listed there.
- `s:` searches every source selected under "Definitions" and merges the results. `r:` searches **every** SQLite source, whatever its flags.

Paths: desktop reads the `cheetah.dataDir` system property, which `desktop/build.gradle.kts` (for `run`) and `common/build.gradle.kts` (for tests) set to this repo's `data/`. Android uses `context.dataDir`, so copy the files to `/data/data/com.flannaghan.cheetah.android/` with `scripts/push-android-data.sh` (see `README.md`).

## Database contract with cheetah-preprocessing

The schema is defined twice: here in `common/src/commonMain/sqldelight/`, with the read queries, and in cheetah-preprocessing's `src/main/sqldelight/db/`, with the inserts and indexes. Keep the table definitions identical. Tables:

- `word(id, word, canonical_form)`
- `definition`: an FTS4 virtual table `(word, text)` whose `rowid` is the definition id
- `wordDefinition(id_word, rank, id_definition)`
- `relationship(id, name, rank)`
- `derivedWord(id_word, id_word_parent, id_relationship)`

On Android, `AndroidSqliteDriver` is given `WordDatabase.Schema`. A prebuilt DB has `user_version` 0, so the driver runs `Schema.create` on it at first open. Every `CREATE` here must therefore stay `IF NOT EXISTS`. The indexes live only in the preprocessing schema.

Canonical forms: `stringToEntry` (`words/Word.kt`) must agree with preprocessing's `wordToCanonicalForm`. That function also strips `ˈ`. Here `stringToEntry` is used only to canonicalise `r:` input. Every other canonical form comes from the data files.

`lookupDefinition` returns the definitions of every word that shares the entry. It then appends the definitions of each parent word from `derivedWord`, ordered by `relationship.rank` descending.

Definition text is a small wiki-like markup parsed by `DefinitionParser`:

- `=word=` and `==Heading==` are headings. `DefinitionLines` styles level 3 or deeper like level 2.
- `#`, `##`, ... are numbered list items.
- `[[target|text]]` is a link.
- `{{...}}` is an italic label.
- `<sup>`, `<etym>...</etym>` and `<~pos~>` are also supported.

Changes to this format have to be made in both repos.

## Test data

The desktop tests open the real dictionary databases. They read `data/` through the `cheetah.dataDir` system property, and the `data/*.sqlite` files there are gitignored. They are built by `~/src/cheetah-preprocessing` (see its `CLAUDE.md`). `QueryTest` and `SqliteWordDatabaseDataSourceKtTest` need `wiktionary.sqlite`, `WordListTextFileDataSourceKtTest` needs `ukacd.txt`, and `DataSourceKtTest`/`ProfilingTest` need at least one valid config. The other tests use in-memory words or temporary directories.

If a DB test fails with an SQLite error such as `no such column`, the database is probably older than the schema in `common/src/commonMain/sqldelight/`. Regenerate it with cheetah-preprocessing rather than changing the code.
