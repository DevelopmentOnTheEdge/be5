---
name: mcp-be5-sql
description: >
  Use this skill whenever the user wants to write, generate, fix, or analyze SQL queries against a
  database schema exposed via an MCP server. Trigger when the user mentions: writing SQL, querying
  a database, generating queries from a schema, working with tables or columns from an MCP, or asks
  to "use the MCP to write a query" or "look up the schema and generate SQL". Also trigger when the
  user asks about joins, filters, aggregations, or any other SQL operation that requires knowing the
  database structure. ALWAYS use this skill before writing any SQL — do not guess schema, ever.
---

# MCP SQL Query Generation

Write accurate SQL by consulting the MCP schema before touching any query. This skill defines the
exact tool-call order, entity filtering rules, and query conventions to follow.

---

## Decision Rules (read these first)

These rules are absolute. Follow them in order before writing any SQL.

### Rule 1 — Schema before SQL
Never write a table name, column name, or JOIN without first confirming it exists in the MCP
response. If you have not called a schema tool yet, call one now.

### Rule 2 — Entity-level before database-level
Always call `get_entity_schema` first. Fall back to `get_table_info` only if the entity is not
found in the application-level schema.

### Rule 3 — Confirm every JOIN key
Before writing any JOIN, call `get_entity_references` on the relevant table. Never assume a
foreign key name or relationship direction.

### Rule 4 — Skip dummy entities
Ignore any entity where:
- The primary key is named `_dummy_` or `dummy`
- The entity name is surrounded by underscores (e.g., `_some_entity_`)

These entities have no corresponding database tables.

### Rule 5 — Learn from existing queries
Before writing new SQL, scan `*.yaml` files in any `src/meta` folder in the project. These files
contain developer-authored queries. Use them as reference for naming conventions, patterns, and
business logic.

---

## Tool Call Order

Execute in this exact sequence for every SQL generation task:

```
1. /mcp                         → verify MCP is connected
2. get_entity_schema(table)     → get application-level schema
   └─ if not found:
      get_table_info(table)     → fall back to database-level schema
3. get_entity_references(table) → confirm foreign key names and directions for every JOIN
4. (optional) scan src/meta/**/*.yaml → read developer-authored queries for context
5. write SQL using only confirmed names
6. validate each identifier against schema before finalising
```

---

## MCP Server Setup

Configure once per project. Choose the auth method that matches your server.

### No auth
```bash
claude mcp add-json <server-name> '{
  "type": "http",
  "url": "https://your-mcp-server/api/mcp"
}'
```

### Basic auth
```bash
MY_BASIC=$(echo -n "username:password" | base64 | tr -d '\n')
claude mcp add-json <server-name> "{
  \"type\": \"http\",
  \"url\": \"https://your-mcp-server/api/mcp\",
  \"headers\": {\"Authorization\": \"Basic $MY_BASIC\"}
}"
```

### Bearer token
```bash
claude mcp add-json <server-name> '{
  "type": "http",
  "url": "https://your-mcp-server/api/mcp",
  "headers": {"Authorization": "Bearer YOUR_TOKEN"}
}'
```

Add `-s user` to any command to make the server available across all projects.

### opencode — `opencode.json`
```json
{
  "$schema": "https://opencode.ai/config.json",
  "mcp": {
    "<server-name>": {
      "type": "remote",
      "url": "https://your-mcp-server/api/mcp",
      "enabled": true,
      "headers": {"Authorization": "Basic <base64(username:password)>"}
    }
  }
}
```

### Auth header reference
| Auth type    | Header value                  |
|--------------|-------------------------------|
| Basic auth   | `Basic <base64(user:pass)>`   |
| Bearer token | `Bearer <token>`              |
| API key      | Server-specific (e.g. `X-API-Key: <key>`) |
| No auth      | Omit `headers` entirely       |

> **Security:** Do not hardcode credentials in version-controlled files. Inject via environment
> variables at registration time (see Basic auth example above).

---

## Workflow

### Step 1 — Connect and explore
```
/mcp
```
Confirm the server is listed and connected. Then call schema tools to map the relevant tables.
Summarise findings before writing SQL:
> "I found tables: `users`, `orders`, `products`. Relevant columns for your query: ..."

### Step 2 — Clarify intent
Confirm these before writing any SQL. Ask if ambiguous:

| Question | Why it matters |
|---|---|
| What columns to return? | Determines SELECT list |
| Filters / date ranges / status values? | Determines WHERE clause |
| Aggregations needed? | Determines GROUP BY / aggregate functions |
| Database engine? | Affects syntax (e.g. `INTERVAL`, `ILIKE`, `DATE_TRUNC`) |
| Performance constraints? | Affects LIMIT, index hints, pagination strategy |

### Step 3 — Write the query
Use only names confirmed from MCP. Follow this structure:

```sql
-- Purpose: [what this query does]
SELECT
    u.id,
    u.email,
    COUNT(o.id) AS order_count
FROM users u
LEFT JOIN orders o ON o.user_id = u.id   -- key confirmed via get_entity_references
WHERE u.created_at >= NOW() - INTERVAL '30 days'
GROUP BY u.id, u.email
ORDER BY order_count DESC
LIMIT 100;
```

Required in every query:
- A `-- Purpose:` comment at the top
- A `LIMIT` on any exploratory or potentially large query
- Aliases for readability

### Step 4 — Validate
Before delivering the query, check every identifier:

- [ ] Each table name exists in the MCP schema response
- [ ] Each column belongs to the correct table
- [ ] Every JOIN key was confirmed via `get_entity_references`
- [ ] Syntax matches the target database engine

If anything is uncertain, re-call the MCP tool.

### Step 5 — Save (if requested)
```bash
cat > queries/monthly_active_users.sql << 'EOF'
-- Purpose: Monthly active users with order count
SELECT ...
EOF
```

Use descriptive filenames: `active_users_last_30d.sql`, not `query1.sql`.

---

## Common Query Patterns

### Simple SELECT with filter
```sql
-- Purpose: Active users created in the last 30 days
SELECT id, email, created_at
FROM users
WHERE status = 'active'
  AND created_at >= NOW() - INTERVAL '30 days'
ORDER BY created_at DESC
LIMIT 100;
```

### Aggregation with GROUP BY
```sql
-- Purpose: Revenue by product category this month
SELECT
    p.category,
    COUNT(o.id)        AS order_count,
    SUM(o.total_cents) AS revenue_cents
FROM orders o
JOIN products p ON p.id = o.product_id   -- key confirmed via get_entity_references
WHERE o.created_at >= DATE_TRUNC('month', NOW())
GROUP BY p.category
ORDER BY revenue_cents DESC;
```

### CTE for readability
```sql
-- Purpose: Top customers by lifetime value
WITH customer_totals AS (
    SELECT
        customer_id,
        SUM(total_cents) AS lifetime_value
    FROM orders
    WHERE status = 'completed'
    GROUP BY customer_id
)
SELECT
    c.email,
    ct.lifetime_value
FROM customer_totals ct
JOIN customers c ON c.id = ct.customer_id   -- key confirmed via get_entity_references
ORDER BY ct.lifetime_value DESC
LIMIT 20;
```

---

## Troubleshooting

| Symptom | Action |
|---|---|
| MCP not responding | Run `/mcp`, check connection status |
| Unsure if column exists | Call `get_entity_schema` or `get_table_info` for the specific table |
| Wrong query results | Re-fetch schema; verify JOIN keys and column names |
| Syntax error | Confirm target DB engine; adjust dialect |
| Slow query | Ask if indexes exist; add `EXPLAIN` or query hints |
| Entity has no table | Check if primary key is `_dummy_` / entity name has surrounding `_` — if so, skip it |

---

## Persistent Context (CLAUDE.md)

For multi-session projects, create `CLAUDE.md` in the project root:

```markdown
# Project Database Context

## MCP Server
- Server name: `<server-name>` (configured in Claude Code)
- Rule: Always call get_entity_schema → get_entity_references before writing any SQL

## Database
- Engine: [PostgreSQL / MySQL / SQLite]
- Primary schema: [schema name if applicable]

## SQL Conventions
- snake_case for aliases
- LIMIT on all exploratory queries
- Prefer CTEs over deeply nested subqueries
- Always include a `-- Purpose:` comment
```
