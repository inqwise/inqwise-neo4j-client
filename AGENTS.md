# Repository Guidelines

## Project Structure & Module Organization
Core library code sits in `src/main/java/com/inqwise/neo4j`, mirroring Neo4j concepts (`Neo4jSession`, `Neo4jTransaction`). Resources live in `src/main/resources`; generated sources in `src/main/generated` stay uncommitted. Keep tests and fixtures in `src/test/java` and `src/test/resources`. `pom.xml` orchestrates the build, and outputs land in `target/`.

## Build, Test, and Development Commands
Run `mvn clean compile` for a clean Java 21 build. Use `mvn test` for the full JUnit 5 and Vert.x suite with JaCoCo coverage. Before a release, execute `mvn verify -Psonatype-oss-release` to sign artifacts and enforce publishing rules. For quick iterations, `mvn -DskipTests package` builds the library JAR—always follow with the full tests ahead of a push or PR.

## Coding Style & Naming Conventions
Follow standard Java conventions: four-space indentation, same-line braces, and class names that reflect their Neo4j role. Keep packages under `com.inqwise.neo4j` and align filenames with public types. Prefer immutable data, asynchronous methods ending in `Async`, and Vert.x `Promise`/`Future` results. Use your IDE formatter to maintain import order, and refresh Javadoc via `mvn javadoc:javadoc` when adjusting public APIs.

## Testing Guidelines
Author new tests beside production code in `src/test/java`, suffixing classes with `Test` (for example, `Neo4jClientTest`). If a scenario needs a running graph, isolate it behind an integration profile. Close sessions with Vert.x `VertxTestContext` helpers inside `@AfterEach` to avoid blocking. After `mvn test`, review `target/site/jacoco/index.html` to confirm coverage trends.

## Commit & Pull Request Guidelines
Write short, imperative commit subjects (`Ensure coverage report before Codecov upload`), adding optional detail in the body. Group related changes and avoid mixing refactors with functional updates. Pull requests should summarize intent, link GitHub issues, and list validation commands. Share logs or screenshots when workflows change, keep documentation in sync, and confirm CI (CI, Release, CodeQL, Snyk) is green.

## Security & Publishing Tips
Never commit secrets; rely on GitHub Actions secrets for automation. The `sonatype-oss-release` profile signs artifacts, so keep your GPG setup current. Triage CodeQL and Snyk alerts within active branches. Document new configuration toggles in `README.adoc` and update `SECURITY.md` when security posture shifts.
