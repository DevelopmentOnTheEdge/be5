package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.sql.QRecParser;
import com.developmentontheedge.be5.query.support.AbstractQueryExecutor;
import com.developmentontheedge.be5.query.util.AggregateUtils;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.query.util.QueryUtils;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetAsMap;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.format.QueryContext;
import com.developmentontheedge.sql.model.AstStart;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static com.developmentontheedge.be5.query.QueryConstants.LIMIT;
import static com.developmentontheedge.be5.query.QueryConstants.OFFSET;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_DIR;

/**
 * A modern query executor that uses our new parser.
 */
public class Be5SqlQueryExecutor extends AbstractQueryExecutor implements QueryExecutor
{
    private DbService db;
    private Meta meta;
    private QuerySession querySession;
    private UserInfoProvider userInfoProvider;
    private QueryMetaHelper queryMetaHelper;
    private CellFormatter cellFormatter;
    private QuerySqlGenerator querySqlGenerator;

    private Query query;
    private Map<String, ?> parameters;
    private ContextApplier contextApplier;
    private QueryContext queryContext;
    private Boolean selectable;

    @Inject
    public void inject(QuerySession querySession, UserInfoProvider userInfoProvider, Meta meta, DbService db,
                       QueryMetaHelper queryMetaHelper, CellFormatter cellFormatter,
                       QuerySqlGenerator querySqlGenerator)
    {
        this.querySession = querySession;
        this.userInfoProvider = userInfoProvider;
        this.meta = meta;
        this.db = db;
        this.queryMetaHelper = queryMetaHelper;
        this.cellFormatter = cellFormatter;
        this.querySqlGenerator = querySqlGenerator;
    }

    public void initialize(Query query, Map<String, Object> parameters)
    {
        this.query = Objects.requireNonNull(query);
        this.parameters = Objects.requireNonNull(parameters);

        queryContext = new Be5QueryContext(query, parameters, querySession, userInfoProvider.getLoggedUser(), meta);
        contextApplier = new ContextApplier(queryContext);

        selectable = query.getType() == QueryType.D1 && query.getOperationNames().getFinalValues().stream()
                .map(name -> meta.getOperation(query.getEntity().getName(), name))
                .filter(o -> meta.hasAccess(o.getRoles(), userInfoProvider.getCurrentRoles()))
                .map(Operation::getRecords)
                .filter(r -> r == 1 || r == 2).count() > 0;
    }

    @Override
    public List<QRec> execute()
    {
        List<QRec> rows = db.list(querySqlGenerator.getSql(query, queryContext), new QRecParser());
        addAggregateRowIfNeeded(rows);
        return formatCell(rows);
    }

    private List<QRec> formatCell(List<QRec> rows)
    {
        List<QRec> res = new ArrayList<>();
        rows.forEach(cells -> res.add(formatCell(cells)));
        return res;
    }

    private QRec formatCell(DynamicPropertySet properties)
    {
        filterBeanWithRoles(properties, userInfoProvider.getCurrentRoles());
        addRowClass(properties);

        QRec resultCells = new QRec();
        DynamicPropertySet previousCells = new DynamicPropertySetAsMap();

        for (DynamicProperty cell : properties)
        {
            Object processedContent = cellFormatter.formatCell(cell, previousCells, query, contextApplier);
            cell.setValue(processedContent);
            cell.setType(processedContent == null ? String.class : processedContent.getClass());
            previousCells.add(cell);
            if (!cell.isHidden())
            {
                resultCells.add(cell);
            }
        }

        return resultCells;
    }

    private void addRowClass(DynamicPropertySet properties)
    {
        DynamicProperty cssRowClassProperty = properties.getProperty(QueryConstants.CSS_ROW_CLASS);
        if (cssRowClassProperty != null)
        {
            for (DynamicProperty property : properties)
            {
                Map<String, Map<String, String>> options = DynamicPropertyMeta.get(property);
                if (options.get("grouping") != null) continue;
                Map<String, String> css = options.putIfAbsent("css", new HashMap<>());
                if (css == null) css = options.get("css");

                String className = css.getOrDefault("class", "");
                css.put("class", className + " " + cssRowClassProperty.getValue());
            }
        }
    }

    private static void filterBeanWithRoles(DynamicPropertySet dps, List<String> currentRoles)
    {
        for (Iterator<DynamicProperty> props = dps.propertyIterator(); props.hasNext();)
        {
            DynamicProperty prop = props.next();
            Map<String, String> info = DynamicPropertyMeta.get(prop).get(QueryConstants.COL_ATTR_ROLES);
            if (info == null)
            {
                continue;
            }

            String roles = info.get("name");
            List<String> roleList = Arrays.asList(roles.split(","));
            List<String> forbiddenRoles = new ArrayList<>();
            for (String userRole : roleList)
            {
                if (userRole.startsWith("!"))
                {
                    forbiddenRoles.add(userRole.substring(1));
                }
            }
            roleList.removeAll(forbiddenRoles);

            boolean hasAccess = false;
            for (String role : roleList)
            {
                if (currentRoles.contains(role))
                {
                    hasAccess = true;
                    break;
                }
            }
            if (!hasAccess && !forbiddenRoles.isEmpty())
            {
                for (String currRole : currentRoles)
                {
                    if (!forbiddenRoles.contains(currRole))
                    {
                        hasAccess = true;
                        break;
                    }
                }
            }
            if (!hasAccess)
            {
                prop.setHidden(true);
            }
        }
    }

    private void addAggregateRowIfNeeded(List<QRec> propertiesList)
    {
        if (propertiesList.size() > 0 && StreamSupport.stream(propertiesList.get(0).spliterator(), false)
                .anyMatch(x -> DynamicPropertyMeta.get(x).containsKey(QueryConstants.COL_ATTR_AGGREGATE)))
        {
            AstStart sql = querySqlGenerator.getSql(query, getParamsWithoutLimit());
            List<QRec> aggregateRows = db.list(sql, new QRecParser());
            AggregateUtils.addAggregateRowIfNeeded(propertiesList, aggregateRows, queryMetaHelper.getTotalTitle(query));
        }
    }

    @Override
    public long count()
    {
        AstStart sql = querySqlGenerator.getSql(query, getParamsWithoutLimit());
        QueryUtils.countFromQuery(sql.getQuery());
        return db.countFrom(sql.format());
    }

    private Map<String, Object> getParamsWithoutLimit()
    {
        return new HashMap<String, Object>(parameters) {{
            remove(LIMIT); remove(OFFSET);
            remove(ORDER_COLUMN); remove(ORDER_DIR);
        }};
    }

    @Override
    public Boolean isSelectable()
    {
        return selectable;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getParameters()
    {
        return (Map<String, Object>) parameters;
    }
}
