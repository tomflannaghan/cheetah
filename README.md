# Cheetah Dictionary

## Search queries

### Regexes

Standard regexes will work. The app automatically requires that the regex matches
the entire word. So `he` only matches the word `HE`, whereas `he.*` matches `HE` and `HELLO` among other things.

The regex cannot contain any of ``/ ` < >``. The regex cannot also start with `s:`. If these conditions are met,
alternative modes will automatically be selected for the query, so it will not be treated as a regex.

### Full Text Search

This mode is selected when the query starts with `s:`. The remainder of the query is a search term that will be
searched for in the text.

`s:hell` will find all entries that contain `HELL` in the definition as a standalone word.

`s:hell*` will find all entries that contain a word that starts with `HELL`, e.g. `HELLO`, in the definition.

### Custom Pattern Matching

This mode is selected when the query contains any of ``/ ` < >``. It is vaguely Qat-like.

- If the string starts with a number of backticks, we require exactly this many misprints.
- A letter from `a` to `z` matches exactly that letter.
- The dot character `.` matches any letter.
- Characters contained within a pair of `/` will match an anagram of the contained characters (dots allowed).
- `>` and `<` match any word in the dictionary, either written forwards or backwards.
- Characters within `>(...)` and `<(...)` are treated as a subquery (can be any of the query types), with the pattern
  matching any of the results of that subquery, either forwards or backwards.

Here are some examples:

`/abc/.` matches `BACK`, `BACH`, `CABA`, `CABS`.

`` `snake`` matches `SHAKE`, `STAKE`, etc, but does not match `SNAKE` itself.

`a>g` matches any word with a and g around the outside, e.g. `ABASING`.

`<(s:food)` matches `BURG`, as grub contains food in its definition.

### Multiple Queries

Several of these queries can be specified at the same time, either as new lines within the search box, or seperated
by `;`. The results are words that match all of the queries.

## Data Sources

The `/data` folder contains the data sources that the app will make use of. Each data source is defined by a JSON file
and a data file. Both must have the same name, but different extensions. All `.json` files within the `/data` folder
will be processed as config files.

Here's an example showing all of the fields:

```
{
  "name": "UKACD 17",
  "color": "#0000FF",
  "type": "TextFileWordList",
  "metadata": {},
  "useWordListByDefault": true,
  "useDefinitionsByDefault": true
}
```

Here are the definitions of the fields:

- `name` - shown in the word list menu
- `color` - the colour of the data source's icon
- `type` - either `TextFileWordList` or `SqliteWordDatabase`
- `metadata` - optional and currently unused
- `useWordListByDefault` - this is optional, defaulting to `true`. Selects whether we should use this data source
  as a source of words that we consider valid.
- `useDefinitionsByDefault` - this is optional, defaulting to `true`. Selects whether we should use this data source
  as a source of definitions (for both full text search and display).

### Text File Word Lists

This mode requies a data file with extension `.txt`. Each line must contain a word as it should be rendered, followed by
a tab, followed by the canonical representation of the word (that pattern matching should be applied to).
See `/data/ukacd.json` for examples.

This type of data can only act as a word list. It cannot act as a source of definitions.

### Sqllite Word Databases

This mode requies an sqlite data file with extension `.sqlite`. The database structure is complex.

This type of data can act as a word list and as a source of definitions.

# Installing on Android

1. Connect the device with adb (USB, or pair it for wireless debugging), and check `adb devices` lists it.
2. Install the debug build: `./gradlew :android:installDebug`, or run the app from Idea.
3. Copy the data files: `scripts/push-android-data.sh`.

The data files live in the app's private directory, `/data/data/com.flannaghan.cheetah.android/`. The script copies
every `data/*.json` there, along with the `.txt` or `.sqlite` file each one points at. Files in `data/` that no config
uses are not copied. Pass source names to copy only those, e.g. `scripts/push-android-data.sh wiktionary ukacd`. It
force-stops the app first, so the new data is loaded when the app is next opened.

The script needs the debug build, because it copies the files in with `run-as`, which only works for debuggable apps.
If more than one device or emulator is connected, choose one with `ANDROID_SERIAL=<serial from adb devices>`. It finds
`adb` on the `PATH`, or else from `sdk.dir` in `local.properties`.

Uninstalling the app or clearing its storage deletes the data files. Reinstalling over the top keeps them.
