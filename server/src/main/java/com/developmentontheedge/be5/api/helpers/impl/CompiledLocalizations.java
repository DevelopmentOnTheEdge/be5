package com.developmentontheedge.be5.api.helpers.impl;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.EntityLocalizations;
import com.developmentontheedge.be5.metadata.model.EntityLocalizations.LocalizationRow;
import com.developmentontheedge.be5.metadata.model.LanguageLocalizations;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class CompiledLocalizations {
    
    private static String L10N_TOPIC_COLUMN_NAME = "columnName";
    
    public static CompiledLocalizations from(Project project)
    {
        // language -> entity name -> entity localizations
        Table<String, String, CompiledEntityLocalizations> all = HashBasedTable.create();
        
        for (Module module : project.getModulesAndApplication())
        {
            collectLocalizations(module, all);
        }
        
        return new CompiledLocalizations(all);
    }

    private static void collectLocalizations(Module module, Table<String, String, CompiledEntityLocalizations> all) {
        for (LanguageLocalizations languageLocalizations : module.getLocalizations())
        {
            collectLocalizations(languageLocalizations, all);
        }
    }

    private static void collectLocalizations(LanguageLocalizations localizations, Table<String, String, CompiledEntityLocalizations> all) {
        String language = localizations.getName();
        
        for (EntityLocalizations entityLocalizations : localizations)
        {
            collectLocalizations(language, entityLocalizations, all);
        }
    }

    private static void collectLocalizations(String language, EntityLocalizations localizations, Table<String, String, CompiledEntityLocalizations> all) {
        String entityName = localizations.getName();
        Table<String, String, String> targetEntityLocalizations = toTable(localizations);
        CompiledEntityLocalizations createdInThisLoopLocalizations = all.get(language, entityName);
        
        if (createdInThisLoopLocalizations == null)
        {
            all.put(language, entityName, new CompiledEntityLocalizations(targetEntityLocalizations));
        }
        else
        {
            createdInThisLoopLocalizations.entityLocalizations.putAll(targetEntityLocalizations);
        }
    }

    private static Table<String, String, String> toTable(EntityLocalizations entityLocalizations) {
        Table<String, String, String> targetEntityLocalizations = HashBasedTable.create();
        
        for (LocalizationRow row : entityLocalizations.getRawRows()) // XXX this can be incorrect: getRows()?
        {
            targetEntityLocalizations.put(row.getTopic(), row.getKey(), row.getValue());
        }
        
        return targetEntityLocalizations;
    }
    
    private static class CompiledEntityLocalizations {
        // topic -> key -> value
        final Table<String, String, String> entityLocalizations;
        
        public CompiledEntityLocalizations(Table<String, String, String> entityLocalizations) {
            this.entityLocalizations = entityLocalizations;
        }
        
        public static Function<CompiledEntityLocalizations, String> fnGetFirstByTopic(final String topic) {
            return entityLocalizations -> entityLocalizations.getFirstByTopic(topic);
        }
        
        public static Function<CompiledEntityLocalizations, String> fnGetByTopicAndKey(final String topic, final String key) {
            return entityLocalizations -> entityLocalizations.getByTopicAndKey(topic, key);
        }
        
        String getByTopicAndKey(String topic, String key) {
            return entityLocalizations.get(topic, key);
        }
        
        String getFirstByTopic(String topic) {
            Map<String, String> pairs = entityLocalizations.row(topic);
            return Iterables.getFirst(pairs.values(), null);
        }
    }

    // language -> entity -> CompiledEntityLocalizations
    private final Table<String, String, CompiledEntityLocalizations> all;
    
    public CompiledLocalizations(Table<String, String, CompiledEntityLocalizations> all)
    {
        this.all = all;
    }
    
    public Optional<String> getEntityTitle(String language, final String entityName) {
        checkNotNull(language);
        checkNotNull(entityName);
        return findLocalization(language, entityName,
                 CompiledEntityLocalizations.fnGetFirstByTopic(DatabaseConstants.L10N_TOPIC_DISPLAY_NAME));
    }
    
    public String getOperationTitle(String language, String entityName, final String operationName) {
        checkNotNull(language);
        checkNotNull(entityName);
        checkNotNull(operationName);
        return findLocalization(language, entityName, operationName,
                CompiledEntityLocalizations.fnGetByTopicAndKey(DatabaseConstants.L10N_TOPIC_OPERATION_NAME, operationName));
    }
    
    public String getQueryTitle(String language, String entityName, final String queryName) {
        checkNotNull(language);
        checkNotNull(entityName);
        checkNotNull(queryName);
        return findLocalization(language, entityName, queryName,
                CompiledEntityLocalizations.fnGetByTopicAndKey(DatabaseConstants.L10N_TOPIC_VIEW_NAME, queryName));
    }

    @Deprecated
    public Optional<String> getColumnTitle(String language, String entityName, String columnName) {
        checkNotNull(language);
        checkNotNull(entityName);
        checkNotNull(columnName);
        return findLocalization(language, entityName,
                CompiledEntityLocalizations.fnGetByTopicAndKey(L10N_TOPIC_COLUMN_NAME, columnName));
    }

    public Optional<String> getColumnTitle(String language, String entityName, String queryName, String columnName) {
        checkNotNull(language);
        checkNotNull(entityName);
        checkNotNull(columnName);

        Optional<String> localization = findLocalization(language, entityName,
                CompiledEntityLocalizations.fnGetByTopicAndKey(queryName, columnName));
        if(!localization.isPresent())
        {
            localization = findLocalization(language, "query.jsp",
                    CompiledEntityLocalizations.fnGetByTopicAndKey("page", columnName));
        }
        return localization;
    }
    
    /**
     * Tries to find a localization and apply the given function to it.
     * Returns the default value if can't find a localization or if the given function returns null.
     * 
     * @deprecated Use {@link CompiledLocalizations#findLocalization(String, String, Function)} instead.
     */
    @Deprecated
    private String findLocalization(String language, String entityName, String defaultValue, Function<CompiledEntityLocalizations, String> continuation) {
        CompiledEntityLocalizations entityLocalizations = all.get(language.toLowerCase(Locale.US), entityName);
        
        if (entityLocalizations == null)
        {
            return defaultValue;
        }
        
        return MoreObjects.firstNonNull(continuation.apply(entityLocalizations), defaultValue);
    }
    
    private Optional<String> findLocalization(String language, String entityName, Function<CompiledEntityLocalizations, String> continuation) {
        CompiledEntityLocalizations entityLocalizations = all.get(language.toLowerCase(Locale.US), entityName);
        
        if (entityLocalizations == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable(continuation.apply(entityLocalizations));
    }
    
}
