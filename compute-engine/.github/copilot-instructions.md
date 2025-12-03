## Quick context

This repository is a small Gradle Java project (root project `engine`, single subproject `app`). It was generated with `gradle init` and contains minimal application scaffolding under `app/src/main/java/org/worker`.

Key source locations
- `app/src/main/java/org/worker/App.java` — application entry and sample code.
- `app/src/main/java/org/worker/core/ExecutionEngine.java` — core engine module (currently a placeholder).
- `app/src/main/java/org/worker/rest/WorkerController.java` — REST/controller package (placeholder).
- `app/build.gradle` — Gradle build for the `app` module. Note: `application.mainClass` currently references `org.example.App` which does not match the package `org.worker.App`.
- `gradle/libs.versions.toml` — version catalog used by the build (Guava, JUnit).

What an AI coding agent should know (concise)
- This is a Gradle (wrapper) project. Use the wrapper: `./gradlew` (macOS zsh). Typical commands:
  - Build: `./gradlew :app:build`
  - Run tests: `./gradlew :app:test`
  - Run app: `./gradlew :app:run` (ensure `application.mainClass` matches `org.worker.App`)
  - Run a single test: `./gradlew :app:test --tests org.worker.AppTest`

- Java toolchain is configured for Java 21 in `app/build.gradle` (see `java.toolchain.languageVersion`). Keep changes compatible with that or update the toolchain in the build files.

- Project uses the Gradle version catalog (`gradle/libs.versions.toml`) — add dependencies via the catalog when possible (e.g. `implementation libs.guava`).

Codebase patterns and examples (from repo)
- Packages live under `org.worker` with two obvious subpackages: `core` and `rest`. When adding new classes, follow that package layout. Example:
  - new core class -> `app/src/main/java/org/worker/core/MyWorker.java`
  - new REST controller -> `app/src/main/java/org/worker/rest/MyController.java`

- Tests use JUnit 4 (see `app/src/test/java/org/worker/AppTest.java`). Use the same testing style and runner when adding tests.

Common pitfalls to avoid
- Don't assume `application.mainClass` is correct — the generated `app/build.gradle` currently has `org.example.App` while the code is `org.worker.App`. Update `app/build.gradle` before using `:app:run`.
- Several sources are placeholders (empty classes). Be conservative with automated refactors: run `./gradlew :app:build` and `:app:test` after edits.

Integration and external pieces
- No external services or API keys are present in the repo. Dependencies are resolved from Maven Central via the Gradle config.

What to include in pull requests from an AI agent
- Short summary of changes (one line), why they were needed, and the verification steps performed (build + test commands and results).
- If you change the Java toolchain or Gradle config, include exact gradle command used to validate (`./gradlew :app:build`).

When you need clarification
- If a class is intentionally left empty (e.g., `ExecutionEngine`, `WorkerController`), prefer asking a short clarifying question about intended behavior before implementing non-trivial logic.

If anything in this file seems off or you want more details (package responsibilities, intended REST API surface, expected input/output of the engine), tell me and I will expand the instructions with endpoints, DTO shapes, and example requests/responses.
