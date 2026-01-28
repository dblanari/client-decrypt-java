# Repository Guidelines

## Project Structure & Module Organization
This repository currently contains only IDE metadata (`.idea/`). No source, test, or build files are present yet. When adding code, keep the layout explicit and predictable (for example, `src/` for main code and `tests/` or `src/test/` for tests). If multiple modules are introduced, group them under a top-level folder per module and document the boundary in this file.

## Build, Test, and Development Commands
No build tool or scripts are defined yet. Once tooling is added, document the exact commands here. Example entries to include:
- `./gradlew build` or `mvn package` — build artifacts.
- `./gradlew test` or `mvn test` — run unit tests.
- `./gradlew run` — run the application locally.
If commands differ by environment, include the required env vars and sample values.

## Coding Style & Naming Conventions
No formatting or linting configuration exists yet. When code lands, pick one formatter/linter and state:
- Indentation (spaces vs tabs) and width.
- Java naming conventions (e.g., `CamelCase` types, `lowerCamelCase` methods/fields).
- File naming patterns and any package naming rules.
Record the formatter command in the Build/Test section.

## Testing Guidelines
No testing framework is present yet. When tests are introduced, document:
- The framework (e.g., JUnit).
- Test file naming (`*Test.java`) and package layout.
- How to run unit vs integration tests (if applicable).

## Commit & Pull Request Guidelines
No Git history or conventions are visible in this repository. Until standards are set:
- Use short, imperative commit subjects (e.g., “Add decrypt helper”).
- Open PRs with a concise summary, test evidence, and linked issue/ticket if one exists.
- Include screenshots/log excerpts only when behavior is user-visible.

## Security & Configuration
Avoid committing secrets. If configuration files are added, include a sanitized example (e.g., `.env.example`) and document required values here.
