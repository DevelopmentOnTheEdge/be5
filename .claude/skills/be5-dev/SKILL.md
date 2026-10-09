# BE5 Application Development Skill

## Project Structure

Multi-module Maven project:
- **`base`** — Core engine: Groovy operations, entity YAMLs, business logic
- **`billing`** — Subscription and payment logic
- **`web`** — React frontend, Jetty server

## Entity Definition (YAML)

Entities live in `src/meta/entities/` and are registered in `project.yaml`. This mandatory for entities and their queries+operations to be accessible by be5 engine.

### Regular Entity (collection)

```yaml
my_entity:
  type: collection
  displayName: My Entity
  order: '50'
  primaryKey: ID
  scheme:
    columns:
    - ID:
        type: KEYTYPE
        autoIncrement: true
        primaryKey: true
    - name:
        type: VARCHAR(200)
    - status:
        type: ENUM('active','archived','deleted')
        defaultValue: '''active'''
    - extras:
        type: JSON
        canBeNull: true
    # Auto-filled by << operator — do NOT set manually
    - whoInserted___:
        type: VARCHAR(30)
        canBeNull: true
    - whoModified___:
        type: VARCHAR(30)
        canBeNull: true
    - creationDate___:
        type: DATETIME
        canBeNull: true
    - modificationDate___:
        type: DATETIME
        canBeNull: true
  queries:
  - All records:
      type: 1D_unknown
      roles: '@AdminRoles'
      invisible: true
      code: |
          SELECT ID, name, status FROM my_entity ORDER BY ID
  operations:
  - Delete:
      records: 2
      roles: '@AdminRoles'
      code: ru.biosoft.biostore.operations.BiostoreDeleteOperation
```

### Pseudo-Entity (custom API)

```yaml
_chatbot_:
  type: table
  displayName: AI Chat Bot
  order: '999'
  primaryKey: _dummy_
  queries:
  - All records:
      type: static
      roles: []
      value: ''
      operations:
      - 'session/create'
      - 'session/list'
  operations:
  - 'session/create':
      type: Groovy
      roles: []
      file: operations.chatbot.chat_session_create.groovy   # MUST include .groovy extension
  - 'session/list':
      type: Groovy
      roles: []
      file: operations.chatbot.chat_session_list.groovy
```

### Vector column (pgvector, PostgreSQL)

`VECTOR(n)` is a regular be5 column type, so `be5:sync` understands it and never
drops the column or the HNSW/IVFFlat index. `CREATE EXTENSION IF NOT EXISTS vector`
is generated automatically before the table / column is created.

```yaml
chunks:
  type: table
  primaryKey: ID
  scheme:
    columns:
    - ID:
        type: KEYTYPE
        autoIncrement: true
        primaryKey: true
    - doc_id:
        type: VARCHAR(100)
    - content:
        type: TEXT
    - embedding:
        type: VECTOR(768)   # number of dimensions; VECTOR without size is also allowed
        canBeNull: true     # NOT NULL vector can be added to an empty table only
    indices:
    - chunks_doc_id_idx:
        columns: doc_id
    - chunks_embedding_idx:
        method: hnsw                      # also ivfflat, gin, gist, brin, hash; default btree
        operatorClass: vector_cosine_ops  # vector_l2_ops, vector_ip_ops, vector_l1_ops, ...
        options: m=16, ef_construction=64 # index storage parameters (WITH (...))
        columns: embedding
```

- `method`, `operatorClass`, `options` are PostgreSQL only; other DBMS (H2 in tests)
  get a plain index and store the vector as `TEXT` (e.g. `'[0.1,0.2,0.3]'`).
- Entity DSL writes (`database.chunks << [embedding: '[0.1,0.2,0.3]']`, `set`, `setBy`) pass
  vector values as strings and cast them automatically (`CAST(? AS vector)`) on PostgreSQL.
- Changing dimensions generates `ALTER COLUMN ... SET DATA TYPE VECTOR(n)`
  (fails if stored vectors have other dimensions, data is never dropped).
- Distance operators `<->` (L2), `<#>` (negative inner product), `<=>` (cosine),
  `<+>` (L1), `<~>`, `<%>` and casts `?::vector` are supported by be-sql in queries:

```groovy
def rows = db.listWithParams(
    "SELECT ID, content, embedding <=> ?::vector AS distance FROM chunks ORDER BY embedding <=> ?::vector LIMIT 5",
    queryVector, queryVector   // queryVector is a String like '[0.1,0.2,...]'
).collect { [id: it.$ID, content: it.$content, distance: it.$distance] }
```

### Register in project.yaml

```yaml
application:
  - _chatbot_          # add pseudo-entity here
  - my_entity          # add regular entity here
```

## Database Operations — BE5 DSL

### Insert (auto-fills whoInserted___, creationDate___)

```groovy
def id = database.my_entity << [
    name: 'Hello',
    status: 'active'
]
```

### Lookup by primary key

```groovy
def entity = database.my_entity[entityID]
```

### Lookup by arbitrary fields

```groovy
def entity = database.my_entity.getBy([
    name: 'Hello',
    status: 'active'
])
```

### Update (auto-fills whoModified___, modificationDate___)

```groovy
database.my_entity[entityID] << [status: 'archived']
```

### Raw SQL — single value

```groovy
def count = db.oneLong("SELECT COUNT(1) FROM my_entity WHERE status = ?", 'active')
```

### Raw SQL — list with parameters (PREFERRED)

Use `db.listWithParams(sql, params...)` and map the rows with `.collect`.
Each row is a `DynamicPropertySet`, so read columns via the `$` prefix
(`it.$ID`, `it.$name`) — NOT `it.ID` or `rs.getString(...)`.

```groovy
def rows = db.listWithParams(
    "SELECT ID, name, status FROM my_entity WHERE status = ? ORDER BY ID",
    'active'
).collect { [id: it.$ID, name: it.$name, status: it.$status] }
```

Notes:
- Positional args after the SQL are the bind parameters, in `?` order.
- `.collect` builds the result list for you — do NOT declare `def rows = []`
  and append in `.each { rows << ... }`.
- Keep the column names in the output map (e.g. `id: it.$ID`); this also lets
  you rename output keys (e.g. `id: it.$ID, date: it.$modificationDate___`).

#### Legacy: `db.list` with an explicit parser

Older code uses `db.list(sql, { rs -> ... }, params...)`. Works, but prefer
`listWithParams` + `.collect` for new code.

```groovy
def rows = db.list(
    "SELECT ID, name, status FROM my_entity WHERE status = ? ORDER BY ID",
    { rs ->
        [
            id: rs.getLong("ID"),
            name: rs.getString("name"),
            status: rs.getString("status")
        ]
    },
    'active'
)
```

### Raw SQL — DML

```groovy
db.update("UPDATE my_entity SET status = ? WHERE ID = ?", 'archived', entityID)
```

## Operations (Groovy)

Operations are Groovy classes extending `OperationSupport`, placed in `src/main/groovy/operations/`.

### Basic operation

```groovy
package operations.chatbot

import com.developmentontheedge.be5.server.operations.support.OperationSupport
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport

import javax.inject.Inject

import static groovy.json.JsonOutput.toJson

@ru.biosoft.biostore.controllers.ChatApiMethod(desc = "Description here")
class chat_session_create extends OperationSupport
{
    @Inject private com.developmentontheedge.be5.meta.Meta meta

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DPS params = new DynamicPropertySetSupport()
        params.title = [ value: presetValues.title, DISPLAY_NAME: "Title", CAN_BE_NULL: true ]
        return params
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        // OpenAPI spec generation stub
        if (session.openApiSpecGeneration == 'true') {
            setResultFinished('{"status":200,"id":1}')
            return
        }

        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()
        def personID = userInfo.userName as Long

        // Use BE5 DSL — never raw INSERT/UPDATE
        def id = database.my_entity << [
            name: params.$title,
            personID: personID
        ]

        setResultFinished(toJson([status: 200, id: id]))
    }
}
```

### SSE streaming (e.g. AI chat)

```groovy
def writer = response.getWriter()
response.setContentType("text/event-stream")
response.setCharacterEncoding("UTF-8")
response.setHeader("Cache-Control", "no-cache")
response.setHeader("Connection", "keep-alive")
response.flushBuffer()

writer.write("data: ${toJson([type: "chunk", content: "hello"])}\n\n")
writer.flush()
writer.close()
```

**Important:** SSE operations write directly to the response writer, NOT via `setResultFinished()` or `sendJson()`. Tests must capture the writer, not `sendJson`.

## Testing

### Test class hierarchy

```
BiostoreBaseDBTest (abstract)
  └── GenexplainDbBaseTest (abstract)
        └── YourTest
```

### Basic test

```groovy
package mypackage

import org.junit.Test
import static org.junit.Assert.*

class MyTest extends BiostoreBaseDBTest
{
    @Test
    public void testSomething() throws Exception
    {
        loginAsTestUser()

        // Execute operation
        def result = executeOperation("my_entity", "All records", "SomeOperation", "123", "[key: value]")
        assertNotNull(result)
    }
}
```

### Mocking API endpoints

```groovy
import com.developmentontheedge.be5.test.mocks.TestRequest
import com.developmentontheedge.be5.test.mocks.TestResponse
import org.mockito.ArgumentCaptor

// Mock request body
Mockito.when(TestRequest.mock.getBody()).thenReturn(reqBody)

// Capture response
ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class)
Mockito.doNothing().when(TestResponse.mock).sendJson(valueCapture.capture())

// Call controller
controller.generate(TestRequest.mock, TestResponse.mock, 'endpoint')

// Assert
def resp = new groovy.json.JsonSlurper().parseText(valueCapture.getValue())
assertNull(resp.errorMessage)
assertEquals(200, resp.status)
```

### Important test gotchas

1. **Remove `compiledGroovy` from `project.yaml` features** — causes `ClassNotFoundException` in tests (test classloader doesn't include compiled classes)
2. **`db.record()`** — works fine, but takes a ready-made SQL string (no bind parameters). Build the full SQL inline; for parameterized queries use `db.listWithParams(sql, params)?.first()` or `database.entity.getBy([...])`
3. **`created_at`/`updated_at` columns** — do NOT define these. Use `creationDate___`/`modificationDate___` which are auto-filled by `<<`
4. **`file:` in YAML must include `.groovy` extension** — e.g. `file: operations.foo.bar.groovy`, NOT `file: operations.foo.bar`
5. **`database.entity << [...]` requires all NOT NULL columns** — mark optional columns with `canBeNull: true`
6. **Access entity properties via `$` prefix** — `entity.$status`, `entity.$title` (DynamicProperty), NOT `entity.status`

## Common Patterns

### Soft-delete

```groovy
database.my_entity[entityID] << [status: 'deleted']
```

### Insert with auto-increment ID

```groovy
def id = database.my_entity << [name: 'value']
// id is the generated primary key
```

### Conditional update

```groovy
def entity = database.my_entity.getBy([ID: id, personID: personID])
if (!entity) {
    setResultFinished(toJson([error: "Not found", code: 404]))
    return
}
database.my_entity[id] << [status: 'archived']
```

### List with ordering

```groovy
def rows = db.listWithParams(
    "SELECT ID, name, modificationDate___ FROM my_entity WHERE personID = ? ORDER BY modificationDate___ DESC",
    personID
).collect { [id: it.$ID, name: it.$name] }
```

## Anti-patterns to Avoid

- **Raw SQL INSERT/UPDATE** — use `database.entity << [...]` instead
- **`created_at`/`updated_at` columns** — use `creationDate___`/`modificationDate___` (auto-filled)
- **`db.list(sql, { rs -> ... }, params)` parser-closure form** — use `db.listWithParams(sql, params).collect { it.$col }` for new code
- **`def rows = []; db.list(...).each { rows << ... }`** — use `.collect { ... }` to build the list
- **`db.record()` with bind parameters** — it only takes a ready-made SQL string. For parameterized queries use `db.listWithParams(sql, params)?.first()` or `database.entity.getBy([...])`
- **`file: operations.foo.bar`** without `.groovy` extension
- **`compiledGroovy` in test classpath** — remove from `project.yaml` features
- **`entity.status`** — use `entity.$status` for DynamicProperty access
- **`setResultFinished()` in SSE operations** — write directly to `response.getWriter()`
