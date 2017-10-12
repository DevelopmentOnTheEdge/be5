package com.developmentontheedge.be5.api.helpers.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.util.MoreStrings;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class UserAwareMetaImpl implements UserAwareMeta//, Configurable<String>
{
    /**
     * The prefix constant for localized message.
     * <br/>This "{{{".
     */
    public static final String LOC_MSG_PREFIX = "{{{";

    /**
     * The postfix constant for localized message.
     * <br/>This "}}}".
     */
    public static final String LOC_MSG_POSTFIX = "}}}";

    private static final Pattern MESSAGE_PATTERN = MoreStrings.variablePattern(LOC_MSG_PREFIX, LOC_MSG_POSTFIX);

    private CompiledLocalizations localizations;

    private final Meta meta;
    /**
     * We must not keep the project directly as services are created once, but
     * the project can be reloaded.
     */
    private final ProjectProvider projectProvider;

    public UserAwareMetaImpl(Meta meta, ProjectProvider projectProvider)
    {
        this.meta = meta;
        this.projectProvider = projectProvider;
        compileLocalizations();//todo move to configure
    }

//    @Override
//    public void configure(String config)
//    {
//        compileLocalizations();
//    }

    @Override
    public void compileLocalizations()
    {
        localizations = CompiledLocalizations.from(projectProvider.getProject());
    }

    /* (non-Javadoc)
     * @see com.developmentontheedge.enterprise.components.Meta#getLocalizedEntityTitle(com.developmentontheedge.enterprise.metadata.model.Entity)
     */
    @Override
    public String getLocalizedEntityTitle(Entity entity) {
        Optional<String> localization = getLocalizedEntityTitle(entity.getName());

        if (!localization.isPresent()) {
            if (!Strings.isNullOrEmpty(entity.getDisplayName())) {
                return entity.getDisplayName();
            }
            return entity.getName();
        }

        return localization.get();
    }

    @Override
    public Optional<String> getLocalizedEntityTitle(String entity) {
        return localizations.getEntityTitle(UserInfoHolder.getLanguage(), entity);
    }

    @Override
    public String getLocalizedQueryTitle(String entity, String query) {
        return localizations.getQueryTitle(UserInfoHolder.getLanguage(), entity, query);
    }

    @Override
    public String getLocalizedOperationTitle(String entity, String operation) {
        return localizations.getOperationTitle(UserInfoHolder.getLanguage(), entity, operation);
    }

    public String getLocalizedOperationField(String entityName, String operationName, String name)
    {
        return localizations.getFieldTitle(UserInfoHolder.getLanguage(), entityName, operationName, name).orElse(name);
    }

    /*
     * use getColumnTitle(entityName, columnName)
     */
    @Deprecated
    public String getLocalizedOperationField(String entityName, String name)
    {
        ImmutableList<String> defaultOp = ImmutableList.of("Insert");
        for (String operationName : defaultOp)
        {
            Optional<String> fieldTitle = localizations.getFieldTitle(UserInfoHolder.getLanguage(), entityName, operationName, name);
            if(fieldTitle.isPresent())return fieldTitle.get();
        }
        return name;
    }

    @Override
    public String getLocalizedCell(String content, String entity, String query)
    {
        String localized = MoreStrings.substituteVariables(content, MESSAGE_PATTERN, (message) ->
                localizations.get(UserInfoHolder.getLanguage(), entity, query, message).orElse(content)
        );

        if(localized.startsWith("{{{") && localized.endsWith("}}}"))
        {
            String clearContent = localized.substring(3,localized.length()-3);
            return localizations.get(UserInfoHolder.getLanguage(), entity, query, clearContent)
                    .orElse(clearContent);
        }

        return localized;
    }

    @Override
    public String getLocalizedValidationMessage(String message)
    {
        return localizations.get(UserInfoHolder.getLanguage(), "messages.l10n", "validation", message).orElse(message);
    }

    @Override
    public String getLocalizedExceptionMessage(String message)
    {
        return localizations.get(UserInfoHolder.getLanguage(), "messages.l10n", "exception", message).orElse(message);
    }

    @Override
    public QuerySettings getQuerySettings(Query query) {
        List<String> availableRoles = UserInfoHolder.getCurrentRoles();
        for(QuerySettings settings: query.getQuerySettings()) {
            Set<String> roles = settings.getRoles().getFinalRoles();
            for(String role : availableRoles) {
                if(roles.contains(role)) {
                    return settings;
                }
            }
        }
        return new QuerySettings(query);
    }

    @Override
    public OperationInfo getOperation(String entity, String queryName, String name)
    {
        return new OperationInfo(queryName, meta.getOperation(entity, queryName, name, UserInfoHolder.getCurrentRoles()));
    }

    @Override
    public OperationInfo getOperation(boolean useQueryName, String entity, String queryName, String name)
    {
        return new OperationInfo(queryName, meta.getOperation(useQueryName, entity, queryName, name, UserInfoHolder.getCurrentRoles()));
    }

    @Override
    public OperationInfo getOperation(String entity, String name)
    {
        return new OperationInfo("", meta.getOperation(entity, name, UserInfoHolder.getCurrentRoles()));
    }

    @Override
    public Query getQuery(String entity, String name)
    {
        return meta.getQuery(entity, name, UserInfoHolder.getCurrentRoles());
    }

    @Override
    public String getColumnTitle(String entityName, String queryName, String columnName)
    {
        return localizations.get(UserInfoHolder.getLanguage(), entityName, queryName, columnName).orElse(columnName);
    }

    public String getColumnTitle(String entityName, String columnName)
    {
        ImmutableList<String> defaultQueries = ImmutableList.of("All records");
        for (String queryName : defaultQueries)
        {
            Optional<String> columnTitle = localizations.get(UserInfoHolder.getLanguage(), entityName, queryName, columnName);
            if(columnTitle.isPresent())return columnTitle.get();
        }
        return columnName;
    }

    @Override
    public String getFieldTitle(String entityName, String operationName, String queryName, String name)
    {
        return localizations.getFieldTitle(UserInfoHolder.getLanguage(), entityName, operationName, queryName, name).orElse(name);
    }

    public CompiledLocalizations getLocalizations()
    {
        return localizations;
    }

}
