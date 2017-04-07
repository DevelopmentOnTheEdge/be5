package com.developmentontheedge.be5.api.helpers.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.developmentontheedge.be5.util.MoreStrings;
import com.google.common.base.Strings;

public class UserAwareMetaImpl implements UserAwareMeta
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
    /**
     * Cache.
     */
    private static CompiledLocalizations compiledLocalizations = null;

    public static UserAwareMeta get(ServiceProvider serviceProvider)
    {
        if (compiledLocalizations == null)
        {
            compiledLocalizations = CompiledLocalizations.from(serviceProvider.getProject());
        }

        return new UserAwareMetaImpl(serviceProvider, compiledLocalizations);
    }

    private final CompiledLocalizations localizations;
    private final Meta meta;

    private UserAwareMetaImpl(ServiceProvider serviceProvider, CompiledLocalizations localizations)
    {
        this.meta = serviceProvider.getMeta();
        this.localizations = localizations;
    }

    /* (non-Javadoc)
     * @see com.beanexplorer.enterprise.components.Meta#getLocalizedEntityTitle(com.beanexplorer.enterprise.metadata.model.Entity)
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

    /* (non-Javadoc)
     * @see com.beanexplorer.enterprise.components.Meta#getLocalizedEntityTitle(java.lang.String)
     */
    @Override
    public Optional<String> getLocalizedEntityTitle(String entity) {
        return localizations.getEntityTitle(UserInfoHolder.getLanguage(), entity);
    }

    /* (non-Javadoc)
     * @see com.beanexplorer.enterprise.components.Meta#getLocalizedQueryTitle(java.lang.String, java.lang.String)
     */
    @Override
    public String getLocalizedQueryTitle(String entity, String query) {
        return localizations.getQueryTitle(UserInfoHolder.getLanguage(), entity, query);
    }

    /* (non-Javadoc)
     * @see com.beanexplorer.enterprise.components.Meta#getLocalizedOperationTitle(java.lang.String, java.lang.String)
     */
    @Override
    public String getLocalizedOperationTitle(String entity, String operation) {
        return localizations.getOperationTitle(UserInfoHolder.getLanguage(), entity, operation);
    }

    @Override
    public String getLocalizedCell(String content, String entity, String query)
    {
        String localized = MoreStrings.substituteVariables(content, MESSAGE_PATTERN, (message) ->
                localizations.get(UserInfoHolder.getLanguage(), entity, query, message).orElse(content)
        );

        if(localized.startsWith("{{{") && localized.endsWith("}}}"))
        {
            String clearContent = content.substring(3,localized.length()-3);
            return localizations.get(UserInfoHolder.getLanguage(), entity, query, clearContent)
                    .orElse(clearContent);
        }

        return localized;
    }

    /* (non-Javadoc)
     * @see com.beanexplorer.enterprise.components.Meta#getQuerySettings(com.beanexplorer.enterprise.metadata.model.Query)
     */
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
    public Operation getOperation(String entity, String queryName, String name)
    {
        return meta.getOperation(entity, queryName, name, UserInfoHolder.getCurrentRoles());
    }

    @Override
    public Operation getOperation(boolean useQueryName, String entity, String queryName, String name)
    {
        return meta.getOperation(useQueryName, entity, queryName, name, UserInfoHolder.getCurrentRoles());
    }

    @Override
    public Query getQuery(String entity, String name)
    {
        return meta.getQuery(entity, name, UserInfoHolder.getCurrentRoles());
    }

    @Override
    public Operation getOperation(String entity, String name)
    {
        return meta.getOperation(entity, name, UserInfoHolder.getCurrentRoles());
    }

    @Override
    public Optional<String> getColumnTitle(String entityName, String queryName, String columnName)
    {
        return localizations.get(UserInfoHolder.getLanguage(), entityName, queryName, columnName);
    }

    @Override
    public Optional<String> getFieldTitle(String entityName, String operationName, String queryName, String name)
    {
        return localizations.getFieldTitle(UserInfoHolder.getLanguage(), entityName, operationName, queryName, name);
    }

}
