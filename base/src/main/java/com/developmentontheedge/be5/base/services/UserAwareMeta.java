package com.developmentontheedge.be5.base.services;

import com.developmentontheedge.be5.base.Service;
import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;

import java.util.Optional;

public interface UserAwareMeta extends Service
{
    void compileLocalizations();

    String getLocalizedBe5ErrorMessage(Be5Exception e);

    /**
     * Returns a localized title in user's preferred language.
     */
    String getLocalizedEntityTitle(Entity entityName);

    /**
     * Returns a localized title in user's preferred language. Ignores entity's display name.
     */
    String getLocalizedEntityTitle(String entityName);

    /**
     * Returns a localized title of a query in user's preferred language.
     */
    String getLocalizedQueryTitle(String entityName, String queryName);

    /**
     * Returns a localized title of an operation in user's preferred language.
     */
    String getLocalizedOperationTitle(Operation operation);

    String getLocalizedOperationTitle(String entityName, String operationName);

    String getLocalizedOperationField(String entityName, String operationName, String name);

    Optional<String> getLocalization(String entityName, String queryName, String message);

    String getLocalizedValidationMessage(String message);

    String getLocalizedExceptionMessage(String message);

    String getLocalizedInfoMessage(String message);

    /**
     * Returns a localized title of a column - be4 format.
     */
    String getColumnTitle(String entityName, String queryName, String columnName);

    String getColumnTitle(String entityName, String columnName);

    String getFieldTitle(String entityName, String operationName, String queryName, String name);

    /**
     * Returns a query.
     * Throws an exception if there's no such query or it is not awailable due to lack of rights.
     */
    Query getQuery(String entityName, String queryName);

    /**
     * Finds some settings of the query that corresponds to the roles of the user or returns empty settings.
     */
    QuerySettings getQuerySettings(Query query);

    /**
     * Returns an operation by its name.
     * Throws an exception if there's no operation with this name.
     */
    Operation getOperation(String entityName, String operationName);

    boolean hasAccessToOperation(String entityName, String queryName, String operationName);

    /**
     * Returns an operation by its name.
     * Throws an exception if there's no operation with this name.
     */
    Operation getOperation(String entityName, String queryName, String operationName);

    String getStaticPageContent(String name);
}
