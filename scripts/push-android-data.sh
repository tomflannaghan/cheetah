#!/usr/bin/env bash
# Copy the data sources in data/ to the debug Android app's data directory.
#
# Usage: scripts/push-android-data.sh [name ...]
#   With no names, copies every data/*.json and the file it points at.
#   With names (e.g. "wiktionary ukacd"), copies only those sources.
#
# Needs the debug build installed (run-as only works on debuggable apps) and
# one device connected (or ANDROID_SERIAL set to choose one). adb is found on
# PATH, or via sdk.dir in local.properties.
set -euo pipefail

PACKAGE=com.flannaghan.cheetah.android
DEST=/data/data/$PACKAGE
TMP=/data/local/tmp/cheetah-data

cd "$(dirname "$0")/.."

ADB=${ADB:-$(command -v adb || true)}
if [[ -z $ADB ]]; then
    sdk_dir=$(sed -n 's/^sdk\.dir=//p' local.properties 2>/dev/null || true)
    ADB=${sdk_dir:+$sdk_dir/platform-tools/adb}
fi
if [[ ! -x ${ADB:-} ]]; then
    echo "adb not found: put it on PATH, set ADB, or set sdk.dir in local.properties" >&2
    exit 1
fi

if [[ $# -eq 0 ]]; then
    set -- $(for f in data/*.json; do basename "$f" .json; done)
fi

files=()
for name in "$@"; do
    config=data/$name.json
    [[ -f $config ]] || { echo "No such config: $config" >&2; exit 1; }
    # The config's type decides the sibling file's extension (see DataSource.kt).
    case $(sed -n 's/.*"type": *"\([A-Za-z]*\)".*/\1/p' "$config") in
        SqliteWordDatabase) data=data/$name.sqlite ;;
        TextFileWordList) data=data/$name.txt ;;
        *) echo "Unknown type in $config" >&2; exit 1 ;;
    esac
    [[ -f $data ]] || { echo "Missing $data for $config" >&2; exit 1; }
    files+=("$config" "$data")
done

"$ADB" shell run-as "$PACKAGE" true ||
    { echo "run-as failed: is the debug build of $PACKAGE installed?" >&2; exit 1; }

# The app's directory is only writable by the app, so push to a world-readable
# staging directory and copy from there as the app's user.
"$ADB" shell am force-stop "$PACKAGE"
"$ADB" shell rm -rf "$TMP" && "$ADB" shell mkdir -p "$TMP"
trap '"$ADB" shell rm -rf "$TMP"' EXIT
for f in "${files[@]}"; do
    "$ADB" push "$f" "$TMP/"
    "$ADB" shell run-as "$PACKAGE" cp "$TMP/$(basename "$f")" "$DEST/"
    "$ADB" shell rm "$TMP/$(basename "$f")"
done

"$ADB" shell run-as "$PACKAGE" ls -l "$DEST"
