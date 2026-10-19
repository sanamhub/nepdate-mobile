# Contributing

What the apps do is defined in [docs/product/requirements.md](docs/product/requirements.md), and
the reasons behind the main choices are in [docs/adr/](docs/adr/README.md).

## Before you open a pull request

1. Work in `android/` or `ios/`, not both. An `M*` task is the exception.
2. Run the platform CI command:
   - Android: `./gradlew ci` in `android/`.
   - iOS: `ios/scripts/ci.sh`.
3. Add screenshots for a UI change: light, dark and Nepali. Add each widget size you touched.
4. Keep the pull request to one task ID. Write the task ID in the title, for example
   `A1-03 month grid`.

Do not add a dependency, Gradle plugin or permission that ADR-0004, ADR-0005, ADR-0007 or ADR-0009
does not name. Open an issue first if you think one is needed.

## Commits

Use a Conventional Commit with the task ID first:

```text
feat(month): A1-03 draw the BS month grid
```

Write the summary in the imperative, lower case, without a trailing period. The body explains why.
Do not add AI attribution.

## Issues

Use the issue forms:

- **Bug report** for a crash, a layout problem or anything that is not calendar data.
- **Wrong date or holiday** for a date, tithi or holiday that looks wrong. Maintainers move a data
  problem to [sanamhub/nepdate](https://github.com/sanamhub/nepdate).

Do not put a security problem in a public issue. Follow [SECURITY.md](SECURITY.md).
