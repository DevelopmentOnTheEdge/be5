package com.developmentontheedge.be5.api.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityItem;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;

public interface Meta
{

    /**
     * ...sorted by localized title.
     */
    List<Entity> getOrderedEntities(String language);
    
    /**
     * Returns an entity with by its name. Throws an exception if there's no entity with this name.
     */
    Entity getEntity(String name);

    /**
     * Returns an operation by its name.
     * Throws an exception if there's no operation with this name.
     */
    Operation getOperation(boolean useQueryName, String entity, String queryName, String name, List<String> availableRoles);

    /**
     * Returns an operation by its name.
     * Throws an exception if there's no operation with this name.
     */
    Operation getOperation(String entity, String name, List<String> roles);

    /**
     * Returns an operation by its name.
     * Throws an exception if there's no operation with this name.
     * Throws an exception if there's no query with this name or this query hasn't this operation.
     */
    Operation getOperation(String entity, String queryName, String name, List<String> roles);

    /**
     * Returns a list of all queries of the entity that a user with the given roles can run.
     * Returns an empty list if there's no such queries.
     */
    List<Query> getQueries(Entity entity, List<String> roles);

    /**
     * Checks if we can run an operation or a query having given roles.
     */
    boolean isAvailableFor(EntityItem entityItem, List<String> roles);

    /**
     * Returns a query.
     * Throws an exception if there's no such query or it is not awailable due to lack of rights.
     */
    Query getQuery(String entity, String name, List<String> roles);
    
    /**
     * Returns a query.
     * Throws an exception if there's no such query.
     */
    Query getQueryIgnoringRoles(String entity, String name);
    
    /**
     * Tries to find a query ignoring roles.
     */
    Optional<Query> findQuery(String entityName, String queryName);
    
    Optional<Query> findQuery(QueryLink link);

    Map<String, ColumnDef> getColumns(String entityName);

    Map<String, ColumnDef> getColumns(Entity entity);

    ColumnDef getColumn(String entityName, String columnName);

    ColumnDef getColumn(Entity entity, String columnName);

    Class<?> getColumnType(ColumnDef columnDef);

    boolean isNumericColumn(String entityName, String columnName);

    boolean isNumericColumn(Entity entity, String columnName);
    /**
     * Returns a localized title. Takes into consideration its display name.
     */
    String getTitle(Entity entity, String language);

    /**
     * Returns a localized title of a query. Takes into consideration its menu name.
     */
    String getTitle(Query query, String language);
    
    /**
     * Checks only the specified query, doesn't respolve redirects.
     */
    boolean isParametrizedTable(Query query);

}