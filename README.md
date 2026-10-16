# BeanExplorer5
Java Web Framework

[![Tests](https://github.com/DevelopmentOnTheEdge/be5/actions/workflows/run_tests_on_push_java_8.yaml/badge.svg)](https://github.com/DevelopmentOnTheEdge/be5/actions/workflows/run_tests_on_push_java_8.yaml)  [![Coverage Status](https://coveralls.io/repos/github/DevelopmentOnTheEdge/be5/badge.svg?branch=master)](https://coveralls.io/github/DevelopmentOnTheEdge/be5?branch=master) 

### Wiki
Be5    
https://github.com/DevelopmentOnTheEdge/be5/wiki

BE-SQL, metadata, Freemarker    
http://wiki.dote.ru    

### Frontend on React
https://github.com/DevelopmentOnTheEdge/be5-react


```sh
# Deploy whole project to local Nexus
mvn deploy
```

```sh
# Deploy whole project to Maven Central
mvn -DskipTests source:jar javadoc:jar deploy
```

## MCP server

Every be5 application that installs `CoreServletModule` exposes its queries and operations, its
database schema and read-only SQL to AI clients at `/api/mcp` (Model Context Protocol, streamable HTTP).

An agent works with the application as its user does in the UI, with the user's roles:

| Tool | What it does |
|---|---|
| `list_queries` | The queries the user may open, grouped by entity, with their `shortDescription`. |
| `describe_query` | Parameters of a query, the entity whose columns filter it, its operations with their `shortDescription`, how many selected rows each needs and whether it asks for confirmation. |
| `execute_query` | One page of rows; the id of a row (`___ID`) is what an operation gets in `selectedRows`. |
| `get_operation_parameters` | The form of an operation (`getParameters`): fields, types, values, options, messages. Nothing is executed, not even an operation without parameters. Called again with the values so far after a `reloadOnChange` field changes. |
| `execute_operation` | Submits the form (`getParameters` with the values, then `invoke`). Values that are not accepted bring the form back with messages on the fields. The only tool that changes data. |
| `upload_file` | A one-time link to upload a file for a file field (see below). |

What a query or an operation is for is told to the agent by `shortDescription`, so fill it in:

```yaml
  queries:
  - Open orders:
      shortDescription: Orders not shipped yet, the oldest first
  operations:
  - Cancel:
      records: 2
      shortDescription: Cancels the selected orders and e-mails the customers
```

Operations run by an agent are written to the operation and event logs like any other.

### Files

A field of the type `Base64File` or `File` (as the UI decides from the simple class name) is shown as
`type: "file"` and takes:

- `{"fileId": "file_..."}` - a file uploaded by the link from `upload_file`. The user opens the link in a
  browser and chooses the file, or an agent with a shell sends it: `curl -F file=@report.pdf '<link>'`.
  The link works once within 15 minutes, the file can be used for an hour by its owner only.
- `{"name": "data.csv", "mimeType": "text/csv", "base64": "..."}` - a small file the agent has itself;
  `base64` may also be a `data:` URL. A model writes base64 slowly, so this is for some kilobytes.
- a list of these for several files.

A `Base64File` field gets a `Base64File`; any other one gets the file as in a multipart request of the UI:
the value is the file name and `getFileItem(name)` (or `FileUploadWrapper` from `request.getRawRequest()`)
gives the file. Files are logged by their names. The largest file accepted is set by the system property
`be5.mcp.files.maxSize` (bytes, 20 MB by default); a proxy in front of the application must accept such a
request body (nginx: `client_max_body_size`) for `/api/mcp`.

A request is authenticated by

- **OAuth 2.1** - for Claude Web / Desktop (*Connectors → Add custom connector* → `https://<host>/api/mcp`),
  Claude Code (`claude mcp add --transport http <name> https://<host>/api/mcp`, then `/mcp`) and other
  remote clients. The client registers itself and the user signs in with the be5 account; nothing is
  configured on the server. The authorization server is served at `/api/oauth`.
- **Basic** - `Authorization: Basic base64(user:password)` for clients with a static header.

Optional system properties:

| Property | Meaning |
|---|---|
| `be5.mcp.oauth.issuer` | Public URL of the authorization server, e.g. `https://example.org/api/oauth`. By default it is derived from `X-Forwarded-Host`/`X-Forwarded-Proto` or the `Host` header; set it when the proxy does not forward the public host. |
| `be5.mcp.oauth.clients` | Clients with a fixed id: `clientId=redirectUri1\|redirectUri2;clientId2=...`. |
| `be5.mcp.roles` | Comma-separated roles allowed to use MCP. By default any user that can log in. |
| `be5.mcp.files.maxSize` | The largest file an agent can give an operation, in bytes. 20 MB by default. |

Tokens, registered clients and uploaded files are kept in memory (files in temporary files): after a
restart clients authorize again, and the application must run as a single node.

## Usage with Claude

In order to have up-to-date skill from this project available for all user projects execute
```sh
ln -s `pwd`/.claude/skills/be5-dev ~/.claude/skills
```