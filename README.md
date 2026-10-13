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

Every be5 application that installs `CoreServletModule` exposes its database schema and read-only SQL
to AI clients at `/api/mcp` (Model Context Protocol, streamable HTTP). A request is authenticated by

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

Tokens and registered clients are kept in memory: after a restart clients authorize again, and
the application must run as a single node.

## Usage with Claude

In order to have up-to-date skill from this project available for all user projects execute
```sh
ln -s `pwd`/.claude/skills/be5-dev ~/.claude/skills
```