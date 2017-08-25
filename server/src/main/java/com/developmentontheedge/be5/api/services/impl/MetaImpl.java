package com.developmentontheedge.be5.api.services.impl;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.QueryLink;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
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
import com.developmentontheedge.be5.metadata.model.base.BeCaseInsensitiveCollection;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.be5.metadata.model.base.BeModelElementSupport;

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

    /**
     * We must not keep the project directly as services are created once, but
     * the project can be reloaded.
     */
    private final ProjectProvider projectProvider;

    public MetaImpl(ProjectProvider projectProvider)
    {
        this.projectProvider = projectProvider;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.beanexplorer.enterprise.components.Meta#getEntity(java.lang.String)
     */
    @Override
    public Entity getEntity(String name)
    {
        return getProject().getEntity(name);
    }

    private boolean hasAccess(RoleSet roles, List<String> availableRoles)
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.beanexplorer.enterprise.components.Meta#getOrderedEntities(java.lang.
     * String)
     */
    @Override
    public List<Entity> getOrderedEntities(String language)
    {
        return  getOrderedEntities(null, language);
    }

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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.beanexplorer.enterprise.components.Meta#getTitle(com.beanexplorer.
     * enterprise.metadata.model.Entity, java.lang.String)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.beanexplorer.enterprise.components.Meta#getTitle(com.beanexplorer.
     * enterprise.metadata.model.Query, java.lang.String)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.beanexplorer.enterprise.components.Meta#getOperation(boolean,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Operation getOperation(boolean useQueryName, String entity, String queryName, String name,
            List<String> availableRoles)
    {
        if (useQueryName)
            return getOperation(entity, queryName, name, availableRoles);
        return getOperation(entity, name, availableRoles);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.beanexplorer.enterprise.components.Meta#getOperation(java.lang.
     * String, java.lang.String)
     */
    @Override
    public Operation getOperation(String entity, String name, List<String> availableRoles)
    {
        Operation operation = getProject().getEntity(entity).getOperations().get(name);
        if (operation == null)
            throw Be5ErrorCode.UNKNOWN_OPERATION.exception(entity, name);
        if (!hasAccess(operation.getRoles(), availableRoles))
            throw Be5ErrorCode.ACCESS_DENIED_TO_OPERATION.exception(entity, name);
        return operation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.beanexplorer.enterprise.components.Meta#getOperation(java.lang.
     * String, java.lang.String, java.lang.String)
     */
    @Override
    public Operation getOperation(String entity, String queryName, String name, List<String> availableRoles)
    {
        return getOperation(entity, name, availableRoles);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.beanexplorer.enterprise.components.Meta#getQueries(com.beanexplorer.
     * enterprise.metadata.model.Entity, java.util.List)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.beanexplorer.enterprise.components.Meta#isAvailableFor(com.
     * beanexplorer.enterprise.metadata.model.EntityItem, java.util.List)
     */
    @Override
    public boolean isAvailableFor(EntityItem entityItem, List<String> roles)
    {
        return roles.stream().anyMatch(entityItem.getRoles().getFinalRoles()::contains);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.beanexplorer.enterprise.components.Meta#getQuery(java.lang.String,
     * java.lang.String)
     */
    @Override
    public Query getQuery(String entityName, String queryName, List<String> availableRoles)
    {
        Query query = getQueryIgnoringRoles(entityName, queryName);
        if (!hasAccess(query.getRoles(), availableRoles))
            throw Be5ErrorCode.ACCESS_DENIED_TO_QUERY.exception(entityName, queryName);
        return query;
    }

    @Override
    public Query getQueryIgnoringRoles(String entityName, String queryName)
    {
        Query query = getEntity(entityName).getQueries().get(queryName);
        if (query == null)
            throw Be5ErrorCode.UNKNOWN_QUERY.exception(entityName, queryName);
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
    public Map<String, ColumnDef> getColumns(Entity entity)
    {
        BeModelElement scheme = entity.getAvailableElement("Scheme");
        if(scheme == null) return new HashMap<>();
        BeCaseInsensitiveCollection<ColumnDef> columns = (BeCaseInsensitiveCollection<ColumnDef>) ((TableDef) scheme).get("Columns");

        return StreamSupport.stream(columns.spliterator(), false).collect(
                Collectors.toMap(ColumnDef::getName, Function.identity() )
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

        return getColumns(entity).get(columnName);
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
            case SqlColumnType.TYPE_SMALLINT:
                return Integer.class;
            case SqlColumnType.TYPE_DECIMAL:
                return Double.class;
            case SqlColumnType.TYPE_DATE:
                return Date.class;
            case SqlColumnType.TYPE_TIMESTAMP:
                return Timestamp.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isNumericColumn(String entityName, String columnName)
    {
        Objects.requireNonNull(entityName);
        Objects.requireNonNull(columnName);

        ColumnDef column = getColumn(entityName, columnName);
        Class<?> type = getColumnType(column);
        return type == Long.class ||
                type == Integer.class ||
                type == Double.class;
    }

    @Override
    public boolean isNumericColumn(Entity entity, String columnName)
    {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(columnName);

        ColumnDef column = getColumn(entity, columnName);
        Class<?> type = getColumnType(column);
        return type == Long.class ||
                type == Integer.class ||
                type == Double.class;
    }

    /**
     * Tries to find a entity with the specified name.
     */
    private Optional<Entity> findEntity(String entityName)
    {
        return Optional.ofNullable(getProject().getEntity(entityName));
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

}
