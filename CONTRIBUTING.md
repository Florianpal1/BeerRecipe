# Contributing to Beer Recipe

Thanks for your interest in the project! This guide explains how to propose a change.

## Reporting a bug or suggesting an idea

Open an [issue](https://github.com/Florianpal1/BeerRecipe/issues) using the matching template.
For a bug, include the Android version, the device, the steps to reproduce it and, for a
calculation error, the expected values and their source (brewing software, published formula…).

## Proposing a change

1. Fork the repository and create a branch from `master`:
   `git checkout -b feat/my-feature`
2. Make your changes.
3. Check that the project builds and the tests pass:
   ```bash
   ./gradlew assembleDebug test
   ```
4. Open a pull request describing the change and how you tested it.

## Code conventions

- Kotlin, following the style configured in `.idea/codeStyles` (Android Studio formatter).
- Code comments and UI strings are written in **French**, like the rest of the codebase.
- The calculation engine (`domain/calc`) and the advisor (`domain/advisor`) stay independent of
  Room and Android: every new formula must come with unit tests in `app/src/test`, with the
  formula's source cited in a comment.
- Any change to a Room entity requires bumping the `AppDatabase` version, adding a migration, and
  committing the generated schema in `app/schemas/`.

## Commit messages

Keep the first line short (≈ 50 characters) and describing the change, followed if needed by a
paragraph explaining why.

```
Add the Daniels IBU formula

Some brewers compare their results with software
that uses it by default.
```

## License

The project is distributed under the [GPL-3.0](LICENSE). By submitting a contribution, you agree
that it will be published under the same license.

## Code of conduct

By participating, you agree to abide by the [code of conduct](CODE_OF_CONDUCT.md).
