package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.security.UserInfoProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Who may use the Query Builder, and so run any SQL (also by execute_sql of the MCP server): the users with a role
 * of the query {@value #QUERY_NAME} of the entity {@value #ENTITY_NAME} - the menu item of the Query Builder, for
 * SystemDeveloper by default. Adding a role to that query gives the role the Query Builder. Without the query
 * (a project without the core module) only system developers may use it.
 */
@Singleton
public class QueryBuilderAccess
{
    public static final String ENTITY_NAME = "_system_";
    public static final String QUERY_NAME = "Query Builder";

    private final Meta meta;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public QueryBuilderAccess(Meta meta, UserInfoProvider userInfoProvider)
    {
        this.meta = meta;
        this.userInfoProvider = userInfoProvider;
    }

    /**
     * Whether the logged in user may use the Query Builder, by the current roles.
     */
    public boolean isAllowed()
    {
        if (userInfoProvider.getLoggedUser() == null) return false;
        List<String> roles = userInfoProvider.getCurrentRoles();
        Query query = getQuery();
        if (query == null) return roles.contains(RoleType.ROLE_SYSTEM_DEVELOPER);
        return query.isAvailable() && meta.isAvailableFor(query, roles);
    }

    /**
     * Whether an AI agent of the logged in user may run any SQL (execute_sql of the MCP server): the user may use
     * the Query Builder, and its query is accessible to AI agents (aiAccessible).
     */
    public boolean isAllowedForAi()
    {
        Query query = getQuery();
        return isAllowed() && (query == null || query.isAiAccessible());
    }

    private Query getQuery()
    {
        return meta.hasEntity(ENTITY_NAME) ? meta.getEntity(ENTITY_NAME).getQueries().get(QUERY_NAME) : null;
    }
}
