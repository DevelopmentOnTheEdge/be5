package com.developmentontheedge.be5.query.util;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.database.util.DynamicPropertyMeta;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;

public class AggregateUtils
{
    public static void addAggregateRow(List<QRec> rows, List<QRec> aggregateRows,
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

                if (!totalTitleAdded && !QueryUtils.shouldBeSkipped(prop))
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
}
