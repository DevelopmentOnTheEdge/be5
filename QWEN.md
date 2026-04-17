# BeanExplorer5 (be5) - Project Context

## Project Overview

**BeanExplorer5** (be5) is a **Java Web Framework** developed by Development On The Edge. It is a backend framework built with Java 8, using Maven as the build system. The framework follows a modular architecture with multiple sub-modules handling different concerns such as database operations, metadata management, query execution, BPMN workflows, and more.

The project is published to Maven Central (`com.developmentontheedge.be5:be5-parent`, version `0.4.1`) under the MIT License.

A separate [React-based frontend](https://github.com/DevelopmentOnTheEdge/be5-react) is available for this backend.

## Tech Stack

- **Java 8** (source/target)
- **Maven** (multi-module build)
- **Google Guice 5.1.0** (Dependency Injection)
- **Groovy 3.0+** (used in modules/core and tests)
- **Apache Freemarker** (patched version included)
- **JUnit 4.12+** & **Mockito** (testing)
- **JaCoCo** & **Coveralls** (code coverage)
- **Checkstyle**, **SpotBugs** (code quality)
- **H2 Database** (testing)
- **Quartz Scheduler**, **Caffeine Cache**, **Logback**
- **Thymeleaf**, **javax.websocket**

## Module Structure

This is a multi-module Maven project. Key modules include:

| Module | Description |
|--------|-------------|
| `base` | Core base module with utilities, DI setup, logging, caching |
| `database` | Database connectivity layer |
| `dbms` | Database management system abstractions |
| `metadata` | Metadata management (model definitions, selectors, parsers) |
| `database-model` | Database model layer |
| `operation` | Operations framework |
| `query` | Query execution engine |
| `server` | Core BE5 server implementation |
| `web` | Web layer |
| `modules/core` | Core Groovy-based extensions |
| `modules/bpmn` | BPMN workflow support |
| `modules/monitoring` | Monitoring capabilities |
| `freemarker-patched` | Patched Freemarker library |
| `maven` | Maven plugin (be5-maven-plugin) |
| `test` | Integration tests |
| `util/test-base` | Shared test utilities |
| `util/app` | Application utilities |
| `util/jetty` | Jetty server utilities |

## Building and Running

### Prerequisites
- **JDK 8** (required — the project targets Java 1.8)
- **Maven 3.x**

### Commands

```bash
# Run all tests
mvn -B test

# Build the project (compile + test)
mvn clean install

# Build without running tests
mvn clean install -DskipTests

# Run checkstyle validation
mvn checkstyle:check

# Run SpotBugs
mvn spotbugs:spotbugs

# Generate Javadoc
mvn javadoc:javadoc

# Generate coverage report
mvn clean test jacoco:report coveralls:report

# Deploy whole project to Maven Central
mvn -DskipTests source:jar javadoc:jar deploy
```

### CI/CD
Tests are run automatically on push to the `master` branch via GitHub Actions (`.github/workflows/run_tests_on_push_java_8.yaml`). Coverage is tracked with Coveralls.

## Development Conventions

### Code Style
- **Checkstyle** is enforced via the `maven-checkstyle-plugin`, configured in `checkstyle.xml` at the project root.
- Key style rules:
  - Max line length: **130 characters**
  - Max method length: **200 lines**
  - No star imports, no redundant/unused imports
  - Braces on new lines for class/interface/method definitions (`LeftCurly` with `nl` option)
  - No trailing whitespace
  - No file tab characters
- Some generated/parser code is excluded from checkstyle: `freemarker/core/*`, `com/developmentontheedge/be5/metadata/model/selectors/parser/*`

### Testing
- Tests use **JUnit 4** and **Mockito**.
- Groovy is used for test sources in several modules (via `gmavenplus-plugin`).
- Test resources are located in `src/test/resources/`.
- H2 in-memory database is used for testing.

### Code Quality
- **JaCoCo** is used for code coverage tracking.
- **SpotBugs** is available for static analysis.
- **GPG signing** is configured for artifact publication.

### Architecture Notes
- The framework uses **Google Guice** for dependency injection, including `guice-servlet` for web injection and `guice-assistedinject` / `guice-multibindings` for advanced DI patterns.
- Metadata-driven design: the framework relies heavily on metadata definitions (selectors, model configs) parsed from YAML/JSON.
- A patched version of Freemarker is bundled (`freemarker-patched/`), suggesting custom template engine behavior.
- The `be5-maven-plugin` is used in test configurations for project-related operations.

## Related Projects

- **Frontend**: [be5-react](https://github.com/DevelopmentOnTheEdge/be5-react) — React-based frontend
- **Wiki (Be5)**: https://github.com/DevelopmentOnTheEdge/be5/wiki
- **Wiki (BE-SQL, metadata, Freemarker)**: http://wiki.dote.ru
