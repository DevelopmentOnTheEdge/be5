package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.google.common.annotations.Beta;

import java.util.Optional;

@Beta
public interface UserAwareMeta
{

    void reCompileLocalizations();

    /**
     * Returns a localized title in user's preferred language.
     */
    String getLocalizedEntityTitle(Entity entity);

    /**
     * Returns a localized title in user's preferred language. Ignores entity's display name.
     */
    Optional<String> getLocalizedEntityTitle(String entity);

    /**
     * Returns a localized title of a query in user's preferred language.
     */
    String getLocalizedQueryTitle(String entity, String query);

    /**
     * Returns a localized title of an operation in user's preferred language.
     */
    String getLocalizedOperationTitle(String entity, String operation);

    String getLocalizedOperationField(String entityName, String operationName, String name);

    String getLocalizedOperationField(String entityName, String name);

    /**
     * Returns a localized title of an operation in user's preferred language.
     */
    String getLocalizedCell(String content, String entity, String query);

    /**
     * Returns a query.
     * Throws an exception if there's no such query or it is not awailable due to lack of rights.
     */
    Query getQuery(String entity, String name);

    /**
     * Finds some settings of the query that corresponds to the roles of the user or returns empty settings.
     */
    QuerySettings getQuerySettings(Query query);

    /**
     * Returns an operation by its name.
     * Throws an exception if there's no operation with this name.
     */
    OperationInfo getOperation(String entity, String name);

    /**
     * Returns an operation by its name.
     * Throws an exception if there's no operation with this name.
     */
    OperationInfo getOperation(String entity, String queryName, String name);

    /**
     * Returns an operation by its name.
     * Throws an exception if there's no operation with this name.
     */
    OperationInfo getOperation(boolean useQueryName, String entity, String queryName, String name);

    /**
     * Returns a localized title of a column - be4 format.
     */
    String getColumnTitle(String entityName, String queryName, String columnName);

    String getColumnTitle(String entityName, String columnName);

    String getFieldTitle(String entityName, String operationName, String queryName, String name);

}
