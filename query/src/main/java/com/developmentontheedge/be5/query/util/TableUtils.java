package com.developmentontheedge.be5.query.util;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.util.MoreStrings;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstTableRef;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;

public class TableUtils
{
    public static void filterBeanWithRoles(DynamicPropertySet dps, List<String> currentRoles)
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

    public static void addAggregateRowIfNeeded(List<QRec> rows, List<QRec> aggregateRows,
                                               String totalTitle)
    {
        DynamicPropertySet firstRow = aggregateRows.get(0);
        Map<String, Map<String, String>> aggregateColumnNames = getAggregateColumnNames(rows.get(0));
        Map<String, Double> aggregateValues = new HashMap<>();

        for (DynamicPropertySet row: aggregateRows)
        {
            for (Map.Entry<String, Map<String, String>> aggregateColumn: aggregateColumnNames.entrySet())
            {
                String name = aggregateColumn.getKey();
                Map<String, String> aggregate = aggregateColumn.getValue();
                Object value = row.getValue(aggregateColumn.getKey());
                if ("Number".equals(aggregate.get("type")))
                {
                    switch (aggregate.get("function"))
                    {
                        case "COUNT":
                            aggregateValues.put(name, aggregateValues.getOrDefault(name, 0.0) + 1);
                            break;
                        case "SUM":
                        case "AVG":
                            aggregateValues.put(name, aggregateValues.getOrDefault(name, 0.0) + getDoubleValue(value));
                            break;
                        default:
                            throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
                    }
                }
                else
                {
                    throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
                }
            }
        }
        for (Map.Entry<String, Map<String, String>> aggregateColumn: aggregateColumnNames.entrySet())
        {
            String name = aggregateColumn.getKey();
            Map<String, String> aggregate = aggregateColumn.getValue();
            if ("Number".equals(aggregate.get("type")))
            {
                switch (aggregate.get("function"))
                {
                    case "SUM":
                    case "COUNT":
                        break;
                    case "AVG":
                        aggregateValues.put(name, aggregateValues.get(name) / aggregateRows.size());
                        break;
                    default:
                        throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
                }
            }
            else
            {
                throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
            }
        }
        rows.add(getTotalRow(firstRow, aggregateValues, totalTitle));
    }

    private static QRec getTotalRow(DynamicPropertySet firstRow, Map<String, Double> aggregateValues,
                                                  String totalTitle)
    {
        QRec res = new QRec();
        boolean totalTitleAdded = false;
        for (Iterator<DynamicProperty> props = firstRow.propertyIterator(); props.hasNext();)
        {
            DynamicProperty prop = props.next();
            String name = prop.getName();
            DynamicProperty aggregateProp;

            if (aggregateValues.containsKey(name))
            {
                Map<String, String> aggregate = DynamicPropertyMeta.get(prop).get(QueryConstants.COL_ATTR_AGGREGATE);

                aggregateProp = new DynamicProperty(name, Double.class, aggregateValues.get(name));
                Map<String, Map<String, String>> options = new HashMap<>();
                options.put("css", Collections.singletonMap("class", aggregate.getOrDefault("cssClass", "")));
                options.put("format", Collections.singletonMap("mask", aggregate.getOrDefault("format", "")));
                DynamicPropertyMeta.set(aggregateProp, options);
            }
            else
            {
                aggregateProp = new DynamicProperty(name, prop.getType(), null);

                if (!totalTitleAdded && !shouldBeSkipped(prop))
                {
                    totalTitleAdded = true;
                    aggregateProp.setValue(totalTitle);
                    aggregateProp.setType(String.class);
                }
            }
            res.add(aggregateProp);
        }
        if (res.getProperty(ID_COLUMN_LABEL) != null) res.setValue(ID_COLUMN_LABEL, "aggregate");
        return res;
    }

    private static Double getDoubleValue(Object value)
    {
        Double add;
        if (value instanceof List)
        {
            add = Double.parseDouble((String) ((List) ((List) value).get(0)).get(0));
        }
        else
        {
            if (value != null) add = Double.parseDouble("" + value);
            else add = 0.0;
        }
        return add;
    }

    private static Map<String, Map<String, String>> getAggregateColumnNames(DynamicPropertySet firstRow)
    {
        Map<String, Map<String, String>> aggregateColumnNames = new HashMap<>();
        for (DynamicProperty dp : firstRow)
        {
            Map<String, Map<String, String>> meta = DynamicPropertyMeta.get(dp);
            Map<String, String> aggregateMeta = meta.get(QueryConstants.COL_ATTR_AGGREGATE);
            if (aggregateMeta != null) aggregateColumnNames.put(dp.getName(), aggregateMeta);
        }
        return aggregateColumnNames;
    }

    public static boolean shouldBeSkipped(DynamicProperty property)
    {
        String name = property.getName();
        return property.isHidden() || MoreStrings.startsWithAny(name, DatabaseConstants.EXTRA_HEADER_COLUMN_PREFIX,
                DatabaseConstants.HIDDEN_COLUMN_PREFIX, DatabaseConstants.GLUE_COLUMN_PREFIX);
    }

    public static boolean shouldBeSkipped(String alias)
    {
        return MoreStrings.startsWithAny(alias, DatabaseConstants.EXTRA_HEADER_COLUMN_PREFIX,
                DatabaseConstants.HIDDEN_COLUMN_PREFIX, DatabaseConstants.GLUE_COLUMN_PREFIX);
    }

    public static void replaceBlob(DynamicPropertySet properties)
    {
        for (DynamicProperty dp : properties)
        {
            if (dp.getValue() == null) continue;
            if (dp.getValue().getClass() == byte[].class || dp.getValue() instanceof Blob)
            {
                dp.setValue("Blob");
                dp.setType(String.class);
            }
        }
    }

    public static void countFromQuery(AstQuery query)
    {
        AstSelect select = Ast.selectCount().from(AstTableRef.as(
                new AstParenthesis(query.clone()),
                new AstIdentifierConstant("data", true)
        ));
        query.replaceWith(new AstQuery(select));
    }

    public static void applyCategory(Query query, AstStart ast, String categoryString)
    {
        if (categoryString != null)
        {
            new CategoryFilter(query.getEntity().getName(), query.getEntity().getPrimaryKey(),
                    Long.parseLong(categoryString)).apply(ast);
        }
    }
}
