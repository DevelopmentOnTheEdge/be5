---
name: mcp-be5-sql
description: >
  Use this skill whenever the user wants to write, generate, or analyze SQL queries using a database
  schema exposed via an MCP server. Triggers when the user mentions writing SQL, querying a database,
  generating queries from schema, working with tables/columns from an MCP, or asks Claude Code to
  produce SQL based on their database structure. Also trigger when the user asks Claude Code to
  "use the MCP to write a query", "look up the schema and generate SQL", or similar. This skill
  ensures Claude Code always consults the MCP schema before writing any SQL, uses correct table/column
  names, and iterates based on schema feedback.
---

# MCP SQL Query Generation

A skill for writing accurate SQL queries by consulting a database schema exposed via an MCP server.

## Core Principle

**Always consult the MCP schema before writing any SQL.** Never guess table or column names.
Fetch the schema first, then write queries using only names that exist in the schema response.

**Always use the `get_entity_references` tool to find correct references between tables before writing any JOIN.** Never assume foreign key names or relationship directions — let the tool confirm them.

---

## MCP Server Setup & Authentication

This skill works with any remote MCP server that exposes a database schema. Configure it once per project using your server's URL and auth method.

### Claude Code — no auth

```bash
claude mcp add-json <server-name> '{
  "type": "http",
  "url": "https://your-mcp-server/api/mcp"
}'
```

### Claude Code — Basic auth

```bash
# Generate the base64 credential first
MY_BASIC=$(echo -n "username:password" | base64 | tr -d '\n')

claude mcp add-json <server-name> "{
  \"type\": \"http\",
  \"url\": \"https://your-mcp-server/api/mcp\",
  \"headers\": {\"Authorization\": \"Basic $MY_BASIC\"}
}"
```

### Claude Code — Bearer token

```bash
claude mcp add-json <server-name> '{
  "type": "http",
  "url": "https://your-mcp-server/api/mcp",
  "headers": {
    "Authorization": "Bearer YOUR_TOKEN"
  }
}'
```

Add `-s user` to any command above to make the server available across all projects, not just the current one.

### opencode — `opencode.json`

```json
{
  "$schema": "https://opencode.ai/config.json",
  "mcp": {
    "<server-name>": {
      "type": "remote",
      "url": "https://your-mcp-server/api/mcp",
      "enabled": true,
      "headers": {
        "Authorization": "Basic <base64(username:password)>"
      }
    }
  }
}
```

### Auth type reference

| Auth type | Header value format |
|---|---|
| Basic auth | `Basic <base64(user:pass)>` |
| Bearer token | `Bearer <token>` |
| API key header | Depends on server (e.g. `X-API-Key: <key>`) |
| No auth | Omit `headers` entirely |

> **Security tip:** Avoid hardcoding credentials in version-controlled files. Use environment variables and inject at registration time, as shown in the Basic auth example above.

---

## Workflow

### Step 1: Connect and Explore the Schema

At the start of any SQL session, verify the MCP is connected and explore the schema:

```
/mcp
```

Then ask the MCP for schema information. Try these prompts to the MCP tools:
- List all available tables
- Describe columns and types for relevant tables
- Call `get_entity_references` on any table you plan to JOIN to confirm foreign key names and directions

Summarize what you found before writing any SQL:
> "I found the following tables: `users`, `orders`, `products`. Here are the relevant columns for your query: ..."

### Step 2: Clarify the Query Goal

Before writing SQL, confirm:
1. **What data is needed** — what columns to return
2. **Filters** — WHERE conditions, date ranges, status values
3. **Aggregations** — COUNT, SUM, GROUP BY, etc.
4. **Database engine** — PostgreSQL, MySQL, SQLite, etc. (affects syntax)
5. **Performance constraints** — does this need to be optimized, indexed, paginated?

If any of these are ambiguous, ask before proceeding.

### Step 3: Write the Query

Use **only** table and column names confirmed from the MCP schema. Structure the SQL clearly:

```sql
-- Purpose: [brief description of what this query does]
SELECT
    u.id,
    u.email,
    COUNT(o.id) AS order_count
FROM users u
LEFT JOIN orders o ON o.user_id = u.id
WHERE u.created_at >= NOW() - INTERVAL '30 days'
GROUP BY u.id, u.email
ORDER BY order_count DESC
LIMIT 100;
```

Always include:
- A comment describing the query's purpose
- A `LIMIT` on exploratory or potentially large queries
- Proper aliasing for readability

### Step 4: Validate Against Schema

After writing the query, cross-reference every identifier:
- ✅ Each table name exists in the MCP schema response
- ✅ Each column belongs to the correct table
- ✅ JOIN keys confirmed via `get_entity_references` (not assumed)
- ✅ Syntax matches the target database engine

If anything is uncertain, call the MCP again to verify.

### Step 5: Save the Query (if requested)

Save finished queries to a logical path:

```bash
# Example
cat > queries/monthly_active_users.sql << 'EOF'
-- Purpose: Monthly active users with order count
SELECT ...
EOF
```

---

## CLAUDE.md Template

If the user wants persistent context across sessions, create a `CLAUDE.md` in the project root:

```markdown
# Project Database Context

## MCP Server
- Schema MCP server: `<server-name>` (configured in Claude Code)
- Always consult this MCP before writing any SQL

## Database
- Engine: [PostgreSQL / MySQL / SQLite]
- Primary schema: [schema name if applicable]

## SQL Conventions
- Use snake_case for aliases
- Always LIMIT exploratory queries
- Prefer CTEs over deeply nested subqueries
- Use -- comments to describe query intent
```

---

## Common Patterns

### Simple SELECT with filter
```sql
-- Get active users created in last 30 days
SELECT id, email, created_at
FROM users
WHERE status = 'active'
  AND created_at >= NOW() - INTERVAL '30 days'
ORDER BY created_at DESC
LIMIT 100;
```

### Aggregation with GROUP BY
```sql
-- Revenue by product category this month
SELECT
    p.category,
    COUNT(o.id)        AS order_count,
    SUM(o.total_cents) AS revenue_cents
FROM orders o
JOIN products p ON p.id = o.product_id
WHERE o.created_at >= DATE_TRUNC('month', NOW())
GROUP BY p.category
ORDER BY revenue_cents DESC;
```

### CTE for readability
```sql
-- Top customers by lifetime value
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
JOIN customers c ON c.id = ct.customer_id
ORDER BY ct.lifetime_value DESC
LIMIT 20;
```

---

## Troubleshooting

| Problem | Action |
|---|---|
| MCP not responding | Run `/mcp` to check connection status |
| Unsure if column exists | Call MCP to describe the specific table |
| Query returns wrong results | Re-fetch schema, verify JOIN keys and column names |
| Syntax error | Confirm target DB engine, adjust dialect |
| Query is slow | Ask user if indexes exist; add hints or EXPLAIN |

---

## Tips

- **Re-fetch schema when in doubt** — it's cheap and prevents wrong queries
- **Name queries descriptively** — files like `active_users_last_30d.sql` are self-documenting
- **Ask about DB engine early** — `INTERVAL`, `DATE_TRUNC`, `ILIKE`, etc. differ between engines
- **Explain your reasoning** — tell the user which tables/columns you're using and why
