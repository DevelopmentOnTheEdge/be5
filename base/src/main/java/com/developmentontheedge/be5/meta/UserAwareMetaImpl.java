package com.developmentontheedge.be5.meta;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.exceptions.ErrorTitles;
import com.developmentontheedge.be5.lifecycle.Start;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class UserAwareMetaImpl implements UserAwareMeta
{
    private CompiledLocalizations localizations;

    private final Meta meta;
    private final ProjectProvider projectProvider;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public UserAwareMetaImpl(Meta meta, ProjectProvider projectProvider, UserInfoProvider userInfoProvider)
    {
        this.meta = meta;
        this.projectProvider = projectProvider;
        this.userInfoProvider = userInfoProvider;
    }

    @Start(order = 20)
    public void start()
    {
        projectProvider.addToReload(this::compileLocalizations);
        compileLocalizations();
    }

    @Override
    public void compileLocalizations()
    {
        localizations = CompiledLocalizations.from(projectProvider.get());
    }

    //todo localize entity, query, operation names
    @Override
    public String getLocalizedBe5ErrorMessage(Be5Exception e)
    {
        return ErrorTitles.formatTitle(
                getLocalizedExceptionMessage(ErrorTitles.getTitle(e.getCode())),
                e.getParameters()
        );
    }

    @Override
    public String getLocalizedEntityTitle(Entity entity)
    {
        Optional<String> localization = localizations.getEntityTitle(getLanguage(), entity.getName());

        if (!localization.isPresent())
        {
            if (!Strings.isNullOrEmpty(entity.getDisplayName()))
            {
                return entity.getDisplayName();
            }
            return entity.getName();
        }

        return localization.get();
    }

    @Override
    public String getLocalizedEntityTitle(String entity)
    {
        return getLocalizedEntityTitle(meta.getEntity(entity));
    }

    @Override
    public String getLocalizedQueryTitle(String entityName, String queryName)
    {
        return localizations.getQueryTitle(getLanguage(), entityName, queryName);
    }

    @Override
    public String getLocalizedOperationTitle(Operation operation)
    {
        return localizations.getOperationTitle(getLanguage(),
                operation.getEntity().getName(), operation.getName());
    }

    @Override
    public String getLocalizedOperationTitle(String entity, String name)
    {
        return localizations.getOperationTitle(getLanguage(), entity, name);
    }

    public String getLocalizedOperationField(String entityName, String operationName, String name)
    {
        return localizations.getFieldTitle(getLanguage(), entityName, operationName, name)
                .orElseGet(() -> getColumnTitle(entityName, name));
    }

    @Override
    public Optional<String> getLocalization(String entityName, String queryName, String message)
    {
        return localizations.get(getLanguage(), entityName, queryName, message);
    }

    @Override
    public String getLocalizedValidationMessage(String message)
    {
        return localizations.get(getLanguage(), "messages.l10n", "validation", message).orElse(message);
    }

    @Override
    public String getLocalizedExceptionMessage(String message)
    {
        return localizations.get(getLanguage(), "messages.l10n", "exception", message).orElse(message);
    }

    @Override
    public String getLocalizedInfoMessage(String message)
    {
        return localizations.get(getLanguage(), "messages.l10n", "info", message).orElse(message);
    }

    @Override
    public QuerySettings getQuerySettings(Query query)
    {
        List<String> currentRoles = userInfoProvider.getCurrentRoles();
        for (QuerySettings settings : query.getQuerySettings())
        {
            Set<String> roles = settings.getRoles().getFinalRoles();
            for (String role : currentRoles)
            {
                if (roles.contains(role))
                {
                    return settings;
                }
            }
        }
        return new QuerySettings(query);
    }

    @Override
    public Operation getOperation(String entityName, String operationName)
    {
        Operation operation = meta.getOperation(entityName, operationName);
        if (!meta.hasAccess(operation.getRoles(), userInfoProvider.getCurrentRoles()))
            throw Be5Exception.accessDeniedToOperation(entityName, operationName);

        return operation;
    }

    @Override
    public boolean hasAccessToOperation(String entityName, String queryName, String operationName)
    {
        Operation operation = meta.getOperation(entityName, queryName, operationName);
        return meta.hasAccess(operation.getRoles(), userInfoProvider.getCurrentRoles());
    }

    @Override
    public Operation getOperation(String entityName, String queryName, String operationName)
    {
        Operation operation = meta.getOperation(entityName, queryName, operationName);
        if (!meta.hasAccess(operation.getRoles(), userInfoProvider.getCurrentRoles()))
            throw Be5Exception.accessDeniedToOperation(entityName, operationName);

        return operation;
    }

    @Override
    public Query getQuery(String entityName, String queryName)
    {
        Query query = meta.getQuery(entityName, queryName);
        if (!meta.hasAccess(query.getRoles(), userInfoProvider.getCurrentRoles()))
            throw Be5Exception.accessDeniedToQuery(entityName, queryName);
        return query;
    }

    @Override
    public String getColumnTitle(String entityName, String queryName, String columnName)
    {
        return localizations.get(getLanguage(), entityName, queryName, columnName).orElse(columnName);
    }

    public String getColumnTitle(String entityName, String columnName)
    {
        ImmutableList<String> defaultQueries = ImmutableList.of("All records");
        for (String queryName : defaultQueries)
        {
            Optional<String> columnTitle = localizations.get(getLanguage(), entityName, queryName, columnName);
            if (columnTitle.isPresent()) return columnTitle.get();
        }
        return columnName;
    }

    @Override
    public String getFieldTitle(String entityName, String operationName, String queryName, String name)
    {
        return localizations.getFieldTitle(getLanguage(), entityName, operationName, queryName, name).orElse(name);
    }

    private String getLanguage()
    {
        return meta.getLocale(userInfoProvider.getLocale()).getLanguage();
    }

    @Override
    public String getStaticPageContent(String name)
    {
        String pageContent = projectProvider.get().getStaticPageContent(getLanguage(), name);
        if (pageContent == null)
            throw Be5Exception.notFound("static/" + name);

        return pageContent;
    }
}
