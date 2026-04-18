# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test

```bash
mvn -B test                                         # run all tests (includes checkstyle + jacoco)
mvn -B test -pl <module>                            # single module (e.g., -pl database)
mvn -B test -Dtest=TestClassName                    # single test class
mvn spotbugs:spotbugs                               # optional static analysis
mvn -DskipTests source:jar javadoc:jar deploy       # deploy to Maven Central
```

**Java 8** is required (source/target 1.8). CI runs on Java 8 via `.github/workflows/run_tests_on_push_java_8.yaml`.

## Code Style

Checkstyle runs automatically during `mvn validate`, configured in `checkstyle.xml`:
- Max line length: 130 characters
- Max method length: 200 lines
- No star imports, no trailing whitespace, braces on new lines for class/interface/method
- Excluded: `freemarker/core/*`, `com/developmentontheedge/be5/metadata/model/selectors/parser/*`

## Architecture

**BeanExplorer5** is a metadata-driven Java web framework. Applications define entities, queries, and operations via YAML/JSON metadata; the framework handles rendering, execution, and persistence.

### Module Dependency Flow

```
freemarker-patched → metadata → base → database-model → query
                                     ↘ operation
                                     ↘ web → server (integration hub)
database + dbms ──────────────────────↗
modules/{core,bpmn,monitoring,mcp-server}
```

| Module | Role |
|--------|------|
| `metadata` | Model definitions, YAML config parsing, Freemarker-based selectors |
| `base` | Guice DI setup, `ConfigurationProvider` (YAML→Java via Jsonb), caching, scheduler |
| `database` / `dbms` | DB connectivity and DBMS abstractions |
| `query` | Query execution engine |
| `operation` | Operations framework |
| `web` | Servlet layer, JSON handling |
| `server` | Core server, `Be5ServletListener` entry point |
| `modules/mcp-server` | MCP (Model Context Protocol) server for AI/LLM integration |
| `freemarker-patched` | Bundled patched Freemarker for SQL/template generation |
| `maven` | `be5-maven-plugin` for project validation |
| `util/test-base` | Shared test utilities; H2 in-memory DB for tests |

### Key Patterns

**Dependency Injection:** Google Guice 5.1.0 throughout. Modules extend `AbstractModule` and bind services in `configure()`. The web layer uses `guice-servlet`.

**Servlet lifecycle** (`Be5ServletListener`):
```
contextInitialized() → LogConfigurator.configure() → Bootstrap.boot() → LifecycleService.start()
contextDestroyed()  → Bootstrap.shutdown() → LifecycleService.stop()
```

**Configuration:** `ConfigurationProvider` reads `config.yaml` from the classpath and maps sections to Java classes via reflection/Jsonb binding.

**REST API parameters** (defined in `RestApiConstants`): `_en_` (entity), `_qn_` (query), `_on_` (operation), `_params_`, `_ts_` (timestamp).

**Testing:** JUnit 4 + Mockito. Groovy is used for test sources in several modules (via `gmavenplus-plugin`).

## Related Projects

- Frontend: https://github.com/DevelopmentOnTheEdge/be5-react (React)
- Wiki (be5): https://github.com/DevelopmentOnTheEdge/be5/wiki
- Wiki (BE-SQL, metadata, Freemarker): http://wiki.dote.ru
