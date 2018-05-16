package com.developmentontheedge.be5.api.services.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.QueryLink;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityItem;
import com.developmentontheedge.be5.metadata.model.EntityLocalizations;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.LanguageLocalizations;
import com.developmentontheedge.be5.metadata.model.LocalizationElement;
import com.developmentontheedge.be5.metadata.model.Localizations;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.RoleSet;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.metadata.model.base.BeCaseInsensitiveCollection;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.be5.metadata.model.base.BeModelElementSupport;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.sql.format.SqlTypeUtils;

import javax.inject.Inject;


public class MetaImpl implements Meta
{

    /**
     * Predicates.
     */
    private static class Pr
    {

        private static final Predicate<LocalizationElement> TOPICS_CONTAIN_DISPLAY_NAME = topicsContain("displayName");
        private static final Predicate<LocalizationElement> TOPICS_CONTAIN_VIEW_NAME    = Pr.topicsContain("viewName");

        private static Predicate<LocalizationElement> topicsContain(final String topic)
        {
            return l10n -> l10n.getTopics().contains(topic);
        }

        private static Predicate<LocalizationElement> keyIs(final String key)
        {
            return l10n -> l10n.getKey().equals(key);
        }

        private static Predicate<LocalizationElement> topicsContainDisplayName()
        {
            return TOPICS_CONTAIN_DISPLAY_NAME;
        }

        private static Predicate<LocalizationElement> topicsContainViewName()
        {
            return TOPICS_CONTAIN_VIEW_NAME;
        }

    } /* class Pr */

    private static final Pattern MENU_ITEM_PATTERN = Pattern.compile("<!--\\S+?-->");

    private final ProjectProvider projectProvider;

    @Inject
    public MetaImpl(ProjectProvider projectProvider)
    {
        this.projectProvider = projectProvider;
    }

    @Override
    public Entity getEntity(String name)
    {
        return getProject().getEntity(name);
    }

    @Override
    public boolean hasAccess(RoleSet roles, List<String> availableRoles)
    {
        Set<String> finalRoles = roles.getFinalRoles();
        for (String role : availableRoles)
        {
            if (role.equals(RoleType.ROLE_ADMINISTRATOR) || role.equals(RoleType.ROLE_SYSTEM_DEVELOPER)
                    || finalRoles.contains(role))
                return true;
        }
        return false;
    }

    @Override
    public List<Entity> getOrderedEntities(String language)
    {
        return getOrderedEntities(null, language);
    }

    @Override
    public List<Entity> getOrderedEntities(EntityType entityType, String language)
    {
        List<OrderedEntity> entities = new ArrayList<>();

        for (Module module : getProject().getModulesAndApplication())
        {
            for (Entity entity : module.getEntities())
            {
                if (entityType == null || entity.getType() == entityType)
                {
                    entities.add(new OrderedEntity(entity, getTitle(entity, language)));
                }
            }
        }

        Collections.sort(entities);

        return entities.stream().map(e -> e.entity).collect(Collectors.toList());
    }

    @Override
    public List<Entity> getEntities(EntityType entityType)
    {
        List<Entity> entities = new ArrayList<>();

        for (Module module : getProject().getModulesAndApplication())
        {
            for (Entity entity : module.getEntities())
            {
                if (entityType == null || entity.getType() == entityType)
                {
                    entities.add(entity);
                }
            }
        }

        return entities;
    }

    @Override
    public List<TableReference> getTableReferences(EntityType entityType)
    {
        return getEntities(entityType).stream()
                .flatMap(entity -> entity.getAllReferences().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<Entity>> getOrderedEntitiesByModules(String language)
    {
        return getOrderedEntitiesByModules(null, language);
    }

    @Override
    public Map<String, List<Entity>> getOrderedEntitiesByModules(EntityType entityType, String language)
    {
        HashMap<String, List<Entity>> result = new HashMap<>();

        for (Module module : getProject().getModulesAndApplication())
        {
            List<OrderedEntity> entities = new ArrayList<>();
            for (Entity entity : module.getEntities())
            {
                if (entityType == null || entity.getType() == entityType)
                {
                    entities.add(new OrderedEntity(entity, getTitle(entity, language)));
                }
            }
            Collections.sort(entities);
            result.put(module.getName(), entities.stream().map(e -> e.entity).collect(Collectors.toList()));
        }

        return result;
    }

    @Override
    public String getTitle(Entity entity, String language)
    {
        String l10n = getLocalization(entity.getProject(), language, entity.getName(), Pr.topicsContainDisplayName());
        return !l10n.isEmpty() ? l10n : getDisplayName(entity);
    }

    private String getDisplayName(Entity entity)
    {
        String displayName = entity.getDisplayName();

        if (displayName != null && displayName.trim().length() > 0)
            return displayName;

        return entity.getName();
    }

    @Override
    public String getTitle(Query query, String language)
    {
        return removeHints(getBe3Title(query, language));
    }

    /**
     * Removes prefixes used to sort items.
     */
    private String removeHints(String title)
    {
        return MENU_ITEM_PATTERN.matcher(title).replaceAll("");
    }

    private String getBe3Title(Query query, String language)
    {
        Predicate<LocalizationElement> accept = Pr.topicsContainViewName().and(Pr.keyIs(query.getName()));
        String l10n = getLocalization(query.getProject(), language, query.getEntity().getName(), accept);
        return !l10n.isEmpty() ? l10n : getMenuName(query);
    }

    private String getLocalization(Project project, String language, String entity,
            Predicate<LocalizationElement> accept)
    {
        for (Module module : project.getModulesAndApplication())
        {
            Localizations localizations = module.getLocalizations();
            LanguageLocalizations languageLocalizations = localizations.get(language);

            if (languageLocalizations == null)
                continue;

            EntityLocalizations entityLocalizations = languageLocalizations.get(entity);

            if (entityLocalizations == null)
                continue;

            for (LocalizationElement element : entityLocalizations.elements())
                if (accept.test(element))
                    return element.getValue();
        }

        return "";
    }

    @Override
    public Locale getLocale(Locale locale)
    {
        List<String> languages = Arrays.asList(getProject().getLanguages());

        if(locale == null || !languages.contains(locale.getLanguage()))
        {
            return new Locale( languages.get(0) );
        }
        else
        {
            return locale;
        }
    }

    private String getMenuName(Query query)
    {
        String menuName = query.getMenuName();

        if (menuName != null && menuName.trim().length() > 0)
            return menuName;

        return query.getName();
    }

    @Override
    public List<String> getOperationNames(Entity entity)
    {
        return entity.getOperations().stream()
                .map(BeModelElementSupport::getName).toList();
    }

    @Override
    public Operation getOperation(String entityName, String queryName, String name)
    {
        Operation operation = getProject().findOperation(entityName, queryName, name);
        if (operation == null)
        {
            if(getProject().findOperation(entityName, name) != null)
            {
                throw Be5ErrorCode.ACCESS_DENIED_TO_OPERATION.exception(entityName, name);//todo add - for current query
            }
            else
            {
                throw Be5ErrorCode.UNKNOWN_OPERATION.exception(entityName, name);
            }
        }

        return operation;
    }

    @Override
    public Operation getOperation(String entityName, String name)
    {
        Operation operation = getProject().findOperation(entityName, name);
        if (operation == null)
        {
            throw Be5ErrorCode.UNKNOWN_OPERATION.exception(entityName, name);
        }
        return operation;
    }

    @Override
    public List<Query> getQueries(Entity entity, List<String> roles)
    {
        List<Query> permittedQueries = new ArrayList<>();

        for (Query query : entity.getQueries())
        {
            if (!query.isInvisible() && isAvailableFor(query, roles))
            {
                permittedQueries.add(query);
            }
        }

        return permittedQueries;
    }

    @Override
    public boolean isAvailableFor(EntityItem entityItem, List<String> roles)
    {
        return roles.stream().anyMatch(entityItem.getRoles().getFinalRoles()::contains);
    }

    @Override
    public String getQueryCode(String entityName, String queryName)
    {
        Query query = getQuery(entityName, queryName);

        return getQueryCode(query);
    }

    @Override
    public String getQueryCode(Query query)
    {
        try
        {
            synchronized(query.getProject())
            {
                return query.getQueryCompiled().validate().trim();
            }
        }
        catch( ProjectElementException e )
        {
            throw Be5Exception.internalInQuery( e, query );
        }
    }

    @Override
    public Query getQuery(String entityName, String queryName)
    {
        Entity entity = getEntity(entityName);
        if(entity == null)
        {
            throw Be5Exception.unknownEntity(entityName);
        }

        Query query = entity.getQueries().get(queryName);
        if (query == null)
        {
            throw Be5ErrorCode.UNKNOWN_QUERY.exception(entityName, queryName);
        }

        return query;
    }

    @Override
    public List<String> getQueryNames(Entity entity)
    {
        return entity.getQueries().stream()
                .map(BeModelElementSupport::getName).toList();
    }

    @Override
    public Optional<Query> findQuery(String entityName, String queryName)
    {
        Objects.requireNonNull(entityName);
        Objects.requireNonNull(queryName);

        return findEntity(entityName).flatMap(entity -> findQuery(entity, queryName));
    }

    private Optional<Query> findQuery(Entity entity, String queryName)
    {
        return Optional.ofNullable(entity.getQueries().get(queryName));
    }

    @Override
    public Optional<Query> findQuery(QueryLink link)
    {
        Objects.requireNonNull(link);
        return findQuery(link.getEntityName(), link.getQueryName());
    }

    @Override
    public Map<String, ColumnDef> getColumns(String entityName)
    {
        Objects.requireNonNull(entityName);
        return findEntity(entityName).map(this::getColumns).orElse(null);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Map<String, ColumnDef> getColumns(Entity entity)
    {
        BeModelElement scheme = entity.getAvailableElement("Scheme");
        if(scheme == null) return new HashMap<>();
        BeCaseInsensitiveCollection<ColumnDef> columns = (BeCaseInsensitiveCollection<ColumnDef>) ((TableDef) scheme).get("Columns");

        return StreamSupport.stream(columns.spliterator(), false).collect(
                Utils.toLinkedMap(ColumnDef::getName, Function.identity())
        );
    }

    @Override
    public ColumnDef getColumn(String entityName, String columnName)
    {
        Objects.requireNonNull(entityName);
        Objects.requireNonNull(columnName);

        return getColumns(entityName).get(columnName);
    }

    @Override
    public ColumnDef getColumn(Entity entity, String columnName)
    {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(columnName);

        ColumnDef columnDef = getColumns(entity).get(columnName);

        if(columnDef == null)
        {
            throw Be5Exception.internal("Column '"+columnName+"' not found in '" + entity.getName() + "'");
        }

        return getColumns(entity).get(columnName);
    }

    @Override
    public Class<?> getColumnType(Entity entity, String columnName)
    {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(columnName);

        return getColumnType(getColumn(entity, columnName));
    }

    @Override
    public Class<?> getColumnType(ColumnDef columnDef)
    {
        switch( columnDef.getType().getTypeName() )
        {
            case SqlColumnType.TYPE_BIGINT:
            case SqlColumnType.TYPE_UBIGINT:
            case SqlColumnType.TYPE_KEY:
                return Long.class;
            case SqlColumnType.TYPE_INT:
            case SqlColumnType.TYPE_UINT:
                return Integer.class;
            case SqlColumnType.TYPE_SMALLINT:
                return Short.class;
            case SqlColumnType.TYPE_DECIMAL:
            case SqlColumnType.TYPE_CURRENCY:
                return Double.class;
            case SqlColumnType.TYPE_BOOL:
                return String.class;//TODO change to Boolean
            case SqlColumnType.TYPE_DATE:
                return Date.class;
            case SqlColumnType.TYPE_TIMESTAMP:
                return Timestamp.class;
            case SqlColumnType.TYPE_BLOB:
            case SqlColumnType.TYPE_MEDIUMBLOB:
                return byte[].class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isNumericColumn(String entityName, String columnName)
    {
        Objects.requireNonNull(entityName);
        return isNumericColumn(getEntity(entityName), columnName);
    }

    @Override
    public boolean isNumericColumn(Entity entity, String columnName)
    {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(columnName);

        return SqlTypeUtils.isNumber(getColumnType(getColumn(entity, columnName)));
    }

    /**
     * Tries to find a entity with the specified name.
     */
    @Override
    public Optional<Entity> findEntity(String entityName)
    {
        return Optional.ofNullable(getProject().getEntity(entityName));
    }

    @Override
    public String getStaticPageContent(String language, String name)
    {
        return getProject().getStaticPageContent(language, name);
    }

    private Project getProject()
    {
        return projectProvider.getProject();
    }

    @Override
    public boolean isParametrizedTable(Query query)
    {
        return !query.getParametrizingOperationName().isEmpty();
    }

    @Override
    public Set<String> getProjectRoles()
    {
        return getProject().getRoles();
    }

    @Override
    public Query createQueryFromSql(String sql)
    {
        Entity e = new Entity( "be5DynamicQueries", getProject().getApplication(), EntityType.TABLE );
        e.setBesql( true );
        DataElementUtils.save( e );
        Query query = new Query( "query", e );
        DataElementUtils.save( query );
        query.setQuery( sql );
        return query;
    }
}
