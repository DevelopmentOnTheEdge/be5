package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.components.impl.model.ActionHelper;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Menu implements Component {

    private static final String INSERT_OPERATION = "Insert";
    
    public static class MenuResponse {
        
        final boolean loggedIn;
        final List<RootNode> root;
        
        MenuResponse(boolean loggedIn, List<RootNode> root)
        {
            this.loggedIn = loggedIn;
            this.root = root;
        }

        public boolean isLoggedIn()
        {
            return loggedIn;
        }

        public List<RootNode> getRoot()
        {
            return root;
        }
    }

    public static class RootNode {
        
        private final Id id;
        private final String title;
        private final boolean isDefault;
        private final Action action;
        private final List<QueryNode> children;
        private final List<OperationNode> operations;
        
        static RootNode action(Id id, String title, boolean isDefault, Action action, List<OperationNode> operations)
        {
            return new RootNode(id, title, isDefault, action, null, operations);
        }
        
        static RootNode container(Id id, String title, List<QueryNode> children, List<OperationNode> operations)
        {
            return new RootNode(id, title, false, null, children, operations);
        }
        
        private RootNode(Id id, String title, boolean isDefault, Action action, List<QueryNode> children, List<OperationNode> operations)
        {
            this.id = id;
            this.title = title;
            this.isDefault = isDefault;
            this.action = action;
            this.children = children;
            this.operations = operations;
        }

        public Id getId()
        {
            return id;
        }

        public String getTitle()
        {
            return title;
        }

        //@JsonbProperty("default")
        public boolean isDefault()
        {
            return isDefault;
        }

        public Action getAction()
        {
            return action;
        }

        public List<QueryNode> getChildren()
        {
            return children;
        }

        public List<OperationNode> getOperations()
        {
            return operations;
        }
    }

    public static class Id {
        
        final String entity;
        final String query;
        
        public Id(String entity, String query)
        {
            this.entity = entity;
            this.query = query;
        }

        public String getEntity()
        {
            return entity;
        }

        public String getQuery()
        {
            return query;
        }
    }

    public static class QueryNode {

        private final Id id;
        private final String title;
        private final Action action;
        private final boolean isDefault;

        public QueryNode(Id id, String title, Action action, boolean isDefault)
        {
            this.id = id;
            this.title = title;
            this.action = action;
            this.isDefault = isDefault;
        }

        public Id getId()
        {
            return id;
        }

        public String getTitle()
        {
            return title;
        }

        public Action getAction()
        {
            return action;
        }

        //@JsonbProperty("default")
        public boolean isDefault()
        {
            return isDefault;
        }
    }

    public static class OperationNode {
        
        final OperationId id;
        final String title;
        final Action action;
        
        OperationNode(OperationId id, String title, Action action)
        {
            this.id = id;
            this.title = title;
            this.action = action;
        }

        public OperationId getId()
        {
            return id;
        }

        public String getTitle()
        {
            return title;
        }

        public Action getAction()
        {
            return action;
        }
    }

    public static class OperationId {
        
        final String entity;
        final String operation;
        
        OperationId(String entity, String operation) {
            this.entity = entity;
            this.operation = operation;
        }

        public String getEntity()
        {
            return entity;
        }

        public String getOperation()
        {
            return operation;
        }
    }
    
    /**
     * Used to sort queries.
     * @author asko
     */
    public static class OrderedQuery implements Comparable<OrderedQuery> {
        
        final Query query;
        final String title;
        
        public OrderedQuery(Query query, String title) {
            Objects.requireNonNull(query);
            Objects.requireNonNull(title);
            this.query = query;
            this.title = title;
        }
        
        @Override
        public int compareTo(OrderedQuery other) {
            Objects.requireNonNull(other);
            return title.compareTo( other.title );
        }

        public Query getQuery()
        {
            return query;
        }

        public String getTitle()
        {
            return title;
        }
    }

    public Menu() {
        /* stateless */
    }

    /**
     * Generated JSON sample:
     * <pre>
     * <code>
     *   { "root": [
     *     { "title": "Entity With All Records", "action": {"name":"ajax", "arg":"entity.query"} },
     *     { "title": "Some Entity", children: [
     *       { "title": "Query1", "action": {"name":"url", "arg":"https://www.google.com"} }
     *     ] }
     *   ] }
     * </code>
     * </pre>
     */
    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        switch (req.getRequestUri())
        {
        case "":
            res.sendAsRawJson(generateSimpleMenu(injector, EntityType.TABLE ));
            return;
        case "dictionary":
            res.sendAsRawJson(generateSimpleMenu(injector, EntityType.DICTIONARY));
            return;
        case "withIds":
            res.sendAsRawJson(generateMenuWithIds(injector, EntityType.TABLE));
            return;
        case "defaultAction":
            res.sendAsRawJson(getDefaultAction(injector, EntityType.TABLE));
            return;
        default:
            res.sendUnknownActionError();
            return;
        }
    }

    MenuResponse generateMenuWithIds(Injector injector, EntityType entityType) {
        return generateMenu(injector, true, entityType);
    }

    public MenuResponse generateSimpleMenu(Injector injector, EntityType entityType) {
        return generateMenu(injector, false, entityType);
    }

    private MenuResponse generateMenu(Injector injector, boolean withIds, EntityType entityType) {
        UserAwareMeta userAwareMeta = UserAwareMeta.get(injector);
        
        List<String> roles = UserInfoHolder.getCurrentRoles();
        String language = UserInfoHolder.getLanguage();
        boolean loggedIn = UserInfoHolder.isLoggedIn();
        List<RootNode> entities = collectEntities(injector.getMeta(), userAwareMeta, language, roles, withIds, entityType);
        
        return new MenuResponse(loggedIn, entities);
    }

    private Action getDefaultAction(Injector injector, EntityType entityType) {
        UserAwareMeta userAwareMeta = UserAwareMeta.get(injector);

        List<String> roles = UserInfoHolder.getCurrentRoles();
        String language = UserInfoHolder.getLanguage();
        List<RootNode> entities = collectEntities(injector.getMeta(), userAwareMeta, language, roles, false, entityType);

        for (RootNode rootNode: entities)
        {
            if(rootNode.action != null)
            {
                if(rootNode.isDefault)return rootNode.action;
            }
            else if(rootNode.children != null)
            {
                for (QueryNode node: rootNode.children)
                {
                    if(node.isDefault)return node.action;
                }
            }
        }

        for (RootNode rootNode: entities)
        {
            if(rootNode.action != null)
            {
                return rootNode.action;
            }
            else if(rootNode.children != null)
            {
                for (QueryNode node: rootNode.children)
                {
                    return node.action;
                }
            }
        }

        return null;
    }

    /**
     * Adds all permitted queries to the root array.
     */
    private List<RootNode> collectEntities(Meta meta, UserAwareMeta userAwareMeta, String language, List<String> roles,
                                           boolean withIds, EntityType entityType) {
        List<RootNode> out = new ArrayList<>();
        
        for (Entity entity : meta.getOrderedEntities(entityType, language))
        {
            collectEntityContent(entity, language, meta, userAwareMeta, roles, withIds, out);
        }

        return out;
    }

    private void collectEntityContent(Entity entity, String language, Meta meta, UserAwareMeta userAwareMeta, List<String> roles, boolean withIds, List<RootNode> out) {
        List<Query> permittedQueries = meta.getQueries(entity, roles);
        
        if (permittedQueries.isEmpty())
        {
            return;
        }

        String title = meta.getTitle(entity, language);
        List<OperationNode> operations = generateEntityOperations(entity, meta, userAwareMeta, roles, withIds);
        
        if (operations.isEmpty())
        {
            operations = null;
        }
        
        if (canBeMovedToRoot(permittedQueries, title, language, meta))
        {
            // Query in the root, contains an action.
            Id id = null;
            Action action = ActionHelper.toAction(permittedQueries.get(0));
            boolean isDefault = permittedQueries.get(0).isDefaultView();
            
            if (withIds)
            {
                String queryTitle = getTitleOfRootQuery(permittedQueries, title, language, meta);
                id = new Id(entity.getName(), queryTitle);
            }
            
            out.add(RootNode.action(id, title, isDefault, action, operations));
        }
        else
        {
            // No query in the root, just inner queries.
            List<QueryNode> children = generateEntityQueries(permittedQueries, language, meta, withIds);
            Id id = new Id(entity.getName(), null);
            out.add(RootNode.container(id, title, children, operations));
        }
    }
    
    private List<QueryNode> generateEntityQueries(List<Query> permittedQueries, String language, Meta meta, boolean withIds) {
        List<OrderedQuery> queries = new ArrayList<>();
        
        for (Query permittedQuery : permittedQueries)
        {
            queries.add(new OrderedQuery(permittedQuery, meta.getTitle(permittedQuery, language)));
        }
        
        Collections.sort(queries);
        
        List<QueryNode> children = new ArrayList<>();
        
        for (OrderedQuery query : queries)
        {
            Query permittedQuery = query.query;
            Id id = null;
            
            if (withIds)
            {
                id = new Id(permittedQuery.getEntity().getName(), permittedQuery.getName());
            }
            
            children.add(new QueryNode(id, query.title, ActionHelper.toAction(permittedQuery), permittedQuery.isDefaultView()));
        }
        
        return children;
    }
    
    private List<OperationNode> generateEntityOperations(Entity entity, Meta meta, UserAwareMeta userAwareMeta, List<String> roles, boolean withIds) {
        List<OperationNode> operations = new ArrayList<>();
        Query allRecords = entity.getQueries().get(DatabaseConstants.ALL_RECORDS_VIEW);
        String insertOperationName = INSERT_OPERATION;
        
        if (allRecords != null && allRecords.getOperationNames().getFinalValues().contains(insertOperationName))
        {
            Operation insertOperation = entity.getOperations().get(insertOperationName);
            if (insertOperation != null && meta.isAvailableFor(insertOperation, roles))
            {
                String title = userAwareMeta.getLocalizedOperationTitle(entity.getName(), insertOperationName);
                Action action = ActionHelper.toAction(DatabaseConstants.ALL_RECORDS_VIEW, insertOperation);
                OperationId id = withIds ? new OperationId(entity.getName(), insertOperationName) : null;
                OperationNode operation = new OperationNode(id, title, action);
                operations.add(operation);
            }
        }
        
        return operations;
    }

    /**
     * If the entity contains only one query, that's named "All records" or as the entity itself.
     */
    private boolean canBeMovedToRoot(List<Query> queries, String entityTitle, String language, Meta meta) {
        return getTitleOfRootQuery(queries, entityTitle, language, meta) != null;
    }

    private String getTitleOfRootQuery(List<Query> queries, String entityTitle, String language, Meta meta)
    {
        if (queries.size() != 1)
            return null;
        
        Query query = queries.get(0);
        
        if (query.getName().equals(DatabaseConstants.ALL_RECORDS_VIEW))
            return DatabaseConstants.ALL_RECORDS_VIEW;
        
        if (meta.getTitle(query, language).equals(entityTitle))
            return entityTitle;
        
        return null;
    }
    
}
