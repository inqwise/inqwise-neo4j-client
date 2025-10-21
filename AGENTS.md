# Repository Guidelines

## Project Structure & Module Organization
Production sources live under `src/main/java/com/inqwise/neo4j`, grouped by driver concept (`Neo4jDriver`, `Neo4jSession`, etc.). Tests mirror the same package tree in `src/test/java/com/inqwise/neo4j`, with helper fakes in `ReactiveStubs.java`. Build output and generated artifacts land in `target/`; keep this directory out of version control. The Maven build is defined in `pom.xml`, and project documentation sits in `README.adoc` plus `SECURITY.md`.

## Build, Test, and Development Commands
- `mvn clean compile` – compile against Java 21, clearing previously built classes.
- `mvn test` – run the JUnit 5/Vert.x suite and emit JaCoCo coverage (report under `target/site/jacoco/index.html`).
- `mvn -DskipTests package` – assemble jars quickly; follow up with `mvn test` before pushing.
- `mvn clean deploy -Psonatype-oss-release` – sign and publish artifacts to Sonatype (requires configured credentials and GPG key).
- `mvn javadoc:javadoc` – regenerate public API docs when you touch exported types.

## Coding Style & Naming Conventions
Use four-space indentation, same-line braces, and descriptive class names matching their file. Keep all packages rooted at `com.inqwise.neo4j`. Methods returning Vert.x futures should prefer `Future<T>` over blocking APIs; helper methods that convert futures to publishers belong in `FutureHelper`. Maintain import order via your IDE’s formatter and avoid introducing `var` outside of obvious short-lived locals.

## Testing Guidelines
Add unit tests next to the code they exercise; name classes `*Test` (for example, `Neo4jSessionTest`). The suite relies on JUnit 5 and Vert.x reactive streams, so prefer non-blocking assertions and use the provided stub classes in `src/test/java/com/inqwise/neo4j`. Run `mvn test` locally before committing; inspect JaCoCo output to keep coverage from regressing. Integration scenarios that require a live database should be guarded behind a profile and skipped in default CI runs.

## Commit & Pull Request Guidelines
Write concise, imperative commit messages such as “Add reactive driver wrapper tests.” Group related changes and keep refactors separate from behavior updates. Pull requests should describe the change, link tracking issues, list validation commands, and mention any documentation updates. Ensure CI workflows (CI, Release, CodeQL, Snyk) pass before requesting review; attach logs or screenshots when altering build or release automation.

## Security & Release Considerations
Never commit credentials; use GitHub secrets for Sonatype and GPG keys. The release profile signs artifacts automatically, so keep your local GPG agent unlocked before manual releases. Review CodeQL/Snyk alerts promptly and record any remediation in `SECURITY.md`. When creating new configuration flags, document defaults and impact in `README.adoc` to ease adoption by downstream users.
