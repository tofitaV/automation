# AGENTS.md

## Purpose
Repository guidelines for AI agents and contributors building and maintaining the Java + Playwright + TestNG automation framework.

## Tech Stack (fixed)
- Language: Java (JDK 17)
- UI automation: Playwright
- Test framework: TestNG
- Assertions: AssertJ
- Build: Gradle
- Design pattern: Page Object Model (POM)

## Project Rules and Conventions
- Follow `one test case = one test class` strictly.
- Keep tests deterministic, isolated, and parallel-safe.
- Keep assertions in web page classes to maximize reuse across tests.
- Keep Page Objects focused on UI interaction, state exposure, and reusable UI assertions.
- Avoid static mutable shared state across tests.
- Prefer composition over inheritance except for shared test lifecycle (`BaseTest`) and shared page primitives (`BasePage`).
- Never hardcode environment URLs or credentials in Java classes.
- Strictly follow Playwright best practices only; avoid non-Playwright/Selenium-style patterns.

## Folder Structure Standards
- `src/test/java/com/automation/framework/base` for core test lifecycle classes.
- `src/test/java/com/automation/framework/config` for configuration loading and environment resolution.
- `src/test/java/com/automation/framework/factory` for browser/runtime object creation.
- `src/test/java/com/automation/framework/pages` for POM classes only.
- `src/test/java/com/automation/framework/dtos` for API/JSON DTOs used by the framework.
- `src/test/java/com/automation/framework/utils` for reusable, framework-level helpers.
- `src/test/java/com/automation/tests/<feature>` for TestNG tests.
- `src/test/resources/config/env.properties` for runtime environment configuration.
- `src/test/resources/testdata` for external test data files.
- `src/test/resources/testng.xml` for default execution suite and parallel strategy.
- `src/test/resources/testng-smoke.xml` for smoke suite execution.

## Coding Standards
- Follow standard Java naming and formatting conventions.
- Keep methods small and intention-revealing.
- Use `private final` fields where possible.
- Use explicit waits/conditions where needed; avoid arbitrary sleeps.
- Keep selector definitions private inside page classes.
- Do not mix API/business logic into Page Objects.

## Test Design Principles
- One class validates one behavior/test case end-to-end.
- Tests follow `Arrange -> Act -> Assert` structure.
- Design test flows using the chain (fluent) pattern common in automation frameworks, where semantic methods return the current or next page object to enable readable step chaining.
- Use Booker test method naming for primary `@Test` methods: `booker<testCaseNumber><TestCaseName>` (camelCase, no spaces/underscores).
- Every `@Test` must declare `testName` in this format: `Booker<testCaseNumber> <Test case name>`.
- Do not use underscores (`_`) in test method names.
- Use TestNG groups (`smoke`, `regression`) for suite-level filtering.
- Each test must be independently runnable.
- Prefer semantic page methods over low-level locator actions in tests.

## Execution
- Default suite: `./gradlew test`
- Smoke suite: `./gradlew smokeTest`
- Runtime config overrides should be passed as system properties, for example: `./gradlew smokeTest -Dheadless=true -Dbrowser=chromium`

## Restrictions and Best Practices
- No assertions in test classes; implement reusable assertions in page classes.
- No test data literals duplicated across classes.
- No direct browser lifecycle control in test classes (use `BaseTest`).
- No cross-test dependencies.
- No use of `Thread.sleep` unless explicitly justified and documented.

## Naming Conventions
- Test class: `Booker<testCaseNumber><TestCaseName>Test`.
- Page class: `<PageName>Page`.
- Base classes: `BaseTest`, `BasePage`.
- Config classes: `*Config` or `*Manager`.
- Methods that perform verification/assertion must start with `verify`.
- Constants: `UPPER_SNAKE_CASE`.
- Packages: lowercase.

## Dependency Management Rules
- All dependencies are declared in `build.gradle` with explicit versions.
- Keep dependency scope minimal (`testImplementation` for test libs).
- Update dependencies in controlled increments; do not batch unrelated upgrades.
- Do not add additional assertion or unit-test frameworks unless approved.

## Test Structure Rule
- Exactly one primary `@Test` scenario per test class.
- Supporting helper methods are allowed within the same class.
- If a new scenario is needed, create a new test class.

## Parallel Execution Rule
- Framework must be thread-safe via `ThreadLocal` for Playwright, Browser, Context, and Page.
- TestNG suite parallelization should run at `classes` level by default.
- Test data and resources must be read-only or isolated per thread/test.
