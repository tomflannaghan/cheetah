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



