package com.developmentontheedge.be5.api.helpers.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoManager;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.google.common.base.Strings;

public class UserAwareMetaImpl implements UserAwareMeta
{
    
    /**
     * Cache.
     */
    private static CompiledLocalizations compiledLocalizations = null;
    
    public static UserAwareMeta get(Request req, ServiceProvider serviceProvider)
    {
        if (compiledLocalizations == null)
        {
            compiledLocalizations = CompiledLocalizations.from(serviceProvider.getProject());
        }
        
        return new UserAwareMetaImpl(req, serviceProvider, compiledLocalizations);
    }
    
    private final UserInfoManager userInfo;
    private final CompiledLocalizations localizations;
    private final Meta meta;
    
    private UserAwareMetaImpl(Request req, ServiceProvider serviceProvider, CompiledLocalizations localizations)
    {
        this.meta = serviceProvider.getMeta();
        this.userInfo = UserInfoManager.get(req, serviceProvider);
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
        return localizations.getEntityTitle(userInfo.getLanguage(), entity);
    }
    
    /* (non-Javadoc)
     * @see com.beanexplorer.enterprise.components.Meta#getLocalizedQueryTitle(java.lang.String, java.lang.String)
     */
    @Override
    public String getLocalizedQueryTitle(String entity, String query) {
        return localizations.getQueryTitle(userInfo.getLanguage(), entity, query);
    }
    
    /* (non-Javadoc)
     * @see com.beanexplorer.enterprise.components.Meta#getLocalizedOperationTitle(java.lang.String, java.lang.String)
     */
    @Override
    public String getLocalizedOperationTitle(String entity, String operation) {
        return localizations.getOperationTitle(userInfo.getLanguage(), entity, operation);
    }
    
    /* (non-Javadoc)
     * @see com.beanexplorer.enterprise.components.Meta#getQuerySettings(com.beanexplorer.enterprise.metadata.model.Query)
     */
    @Override
    public QuerySettings getQuerySettings(Query query) {
        List<String> availableRoles = userInfo.getCurrentRoles();
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
        return meta.getOperation(entity, queryName, name, userInfo.getCurrentRoles());
    }
    
    @Override
    public Operation getOperation(boolean useQueryName, String entity, String queryName, String name)
    {
        return meta.getOperation(useQueryName, entity, queryName, name, userInfo.getCurrentRoles());
    }

    @Override
    public Query getQuery(String entity, String name)
    {
        return meta.getQuery(entity, name, userInfo.getCurrentRoles());
    }

    @Override
    public Operation getOperation(String entity, String name)
    {
        return meta.getOperation(entity, name, userInfo.getCurrentRoles());
    }

    @Override
    public Optional<String> getColumnTitle(String entityName, String columnName)
    {
        return localizations.getColumnTitle(userInfo.getLanguage(), entityName, columnName);
    }

    @Override
    public Optional<String> getColumnTitle(String entityName, String queryName, String columnName)
    {
        return localizations.getColumnTitle(userInfo.getLanguage(), entityName, queryName, columnName);
    }
    
}
