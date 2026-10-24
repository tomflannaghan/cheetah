# cheetah

## Building from the terminal

Use Android Studio's bundled JDK. The system Java (`/usr/lib/jvm/java-25-openjdk`) is a JRE with no `javac`, which the Android build needs:

```sh
JAVA_HOME=~/.local/share/JetBrains/Toolbox/apps/android-studio/jbr ./gradlew :android:assembleDebug :common:desktopTest
```

## Test data

The desktop tests open the real dictionary databases. `DesktopApplicationContext` hardcodes `/home/tom/src/cheetah/data/`, and the `data/*.sqlite` files there are gitignored. They are built by `~/src/cheetah-preprocessing` (see its `CLAUDE.md`).

If a DB test fails with an SQLite error such as `no such column`, the database is probably older than the schema in `common/src/commonMain/sqldelight/`. Regenerate it with cheetah-preprocessing rather than changing the code.
