# AGENTS.md - Be5 (BeanExplorer5)

## Build & Test
- **Java version**: Java 8 only (see `.github/workflows/run_tests_on_push_java_8.yaml`)
- **Build**: `mvn -B test` (runs checkstyle during validate, tests, and jacoco)
- **Single module**: `mvn -B test -pl <module>` (e.g., `-pl database`)
- **Single test**: `mvn -B test -Dtest=TestClassName`
- **Optional static analysis**: `mvn -B spotbugs:spotbugs`
- **Deploy whole project to Maven Central**: `mvn -DskipTests source:jar javadoc:jar deploy`

## Code Quality
- **Checkstyle**: Runs during `mvn validate`, config at `checkstyle.xml`
- **Line length limit**: 130 chars
- **Excluded from checkstyle**: `freemarker/core/*`, `com/developmentontheedge/be5/metadata/model/selectors/parser/*`

## Project Structure
- **Type**: Maven multi-module project (18 modules)
- **Modules**: database, dbms, metadata, maven, base, util/test-base, operation, query, database-model, web, server, modules/bpmn, modules/core, modules/monitoring, test, util/app, util/jetty, freemarker-patched
- **DI Framework**: Guice (version 5.1.0)

## Key Files
- `pom.xml` - parent POM, defines all modules
- `checkstyle.xml` - coding standards
- `.github/workflows/run_tests_on_push_java_8.yaml` - CI workflow