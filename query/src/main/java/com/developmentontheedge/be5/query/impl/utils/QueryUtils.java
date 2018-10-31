package com.developmentontheedge.be5.query.impl.utils;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.util.Utils;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.format.ColumnAdder;
import com.developmentontheedge.sql.format.ColumnRef;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstLimit;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstOrderBy;
import com.developmentontheedge.sql.model.AstOrderingElement;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstStringConstant;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.SimpleNode;
import com.developmentontheedge.sql.model.Token;
import one.util.streamex.EntryStream;
import one.util.streamex.MoreCollectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


//TODO get mainEntityName from query -> FROM name
public class QueryUtils
{
    public static void applyFilters(AstStart ast, String entityName, Map<String, List<Object>> parameters, Meta meta)
    {
        Set<String> usedParams = ast.tree().select(AstBeParameterTag.class).map(AstBeParameterTag::getName).toSet();

        Map<String, String> aliasToTable = getAliasToTable(ast);

        Map<ColumnRef, List<Object>> rawFilters = EntryStream.of(parameters)
                .removeKeys(usedParams::contains)
                .removeKeys(QueryUtils::isNotQueryParameters)
                .removeKeys(k -> isNotContainsInQuery(entityName, aliasToTable, meta, k))
                .mapKeys(k -> ColumnRef.resolve(ast, k.contains(".") ? k : entityName + "." + k))
                .nonNullKeys()
                .toMap();

        Map<ColumnRef, List<Object>> filters = resolveTypes(rawFilters, aliasToTable, meta);

        if (!filters.isEmpty())
        {
            new Be5FilterApplier().addFilter(ast, filters);
        }
    }

    public static Map<String, String> getAliasToTable(AstStart ast)
    {
        return ast.tree()
                    .select(AstTableRef.class)
                    .filter(t -> t.getTable() != null && t.getAlias() != null)
                    .collect(Collectors.toMap(AstTableRef::getAlias, AstTableRef::getTable,
                            (address1, address2) -> address1));
    }

    private static boolean isNotContainsInQuery(String mainEntityName, Map<String, String> aliasToTable, Meta meta, String key)
    {
        String[] split = key.split("\\.");
        if (split.length == 1)
        {
            return meta.getColumn(mainEntityName, split[0]) == null;
        }
        else
        {
            String entityName = null;
            if (aliasToTable.get(split[0]) != null && meta.getEntity(aliasToTable.get(split[0])) != null)
            {
                entityName = aliasToTable.get(split[0]);
            }
            else if (meta.getEntity(split[0]) != null)
            {
                entityName = split[0];
            }
            return entityName == null || meta.getColumn(entityName, split[1]) == null;
        }
    }

    private static boolean isNotQueryParameters(String key)
    {
        return key.startsWith("_") && key.endsWith("_");
    }

    public static void countFromQuery(AstQuery query)
    {
        AstSelect select = Ast.selectCount().from(AstTableRef.as(
                new AstParenthesis(query.clone()),
                new AstIdentifierConstant("data", true)
        ));
        query.replaceWith(new AstQuery(select));
    }

    public static void resolveTypeOfRefColumn(AstStart ast, String entityName, Meta meta)
    {
        ast.tree().select(AstBeParameterTag.class).forEach((AstBeParameterTag tag) -> {
            ColumnDef columnDef = getColumnDef(ast, tag, entityName, meta);
            if (columnDef != null)
            {
                tag.setType(meta.getColumnType(columnDef).getName());
            }
        });
    }

    public static ColumnDef getColumnDef(AstStart ast, AstBeParameterTag beParameterTag, String mainEntityName, Meta meta)
    {
        if (beParameterTag.getRefColumn() != null)
        {
            return getColumnDef(ast, beParameterTag.getRefColumn(), mainEntityName, meta);
        }
        else
        {
            SimpleNode node = beParameterTag.jjtGetParent();
            if (node.getClass() == AstStringConstant.class)
            {
                node = node.jjtGetParent();
            }
            Optional<AstFieldReference> first = node.children().select(AstFieldReference.class).findFirst();
            if (first.isPresent())
            {
                return getColumnDef(ast, first.get().getValue(), mainEntityName, meta);
            }
            else
            {
                return null;
            }
        }
    }

    public static ColumnDef getColumnDef(AstStart ast, String rawColumnDef, String mainEntityName, Meta meta)
    {
        String[] split = rawColumnDef.split("\\.");
        String entityName, column;
        if (split.length == 2)
        {
            entityName = split[0];
            column = split[1];
        }
        else if (split.length == 3)
        {
            entityName = split[0] + "." + split[1];
            column = split[2];
        }
        else
        {
            return meta.getColumn(mainEntityName, split[0]);
        }
        Entity entity = meta.getEntity(entityName);
        if (entity == null)
        {
            entity = meta.getEntity(getAliasToTable(ast).get(entityName));
        }
        if (entity == null)
        {
            throw new RuntimeException("Entity '" + entityName + "' not found.");
        }
        return meta.getColumn(entity, column);
    }

    private static Map<ColumnRef, List<Object>> resolveTypes(Map<ColumnRef, List<Object>> parameters,
                                            Map<String, String> aliasToTable, Meta meta)
    {
        Map<ColumnRef, List<Object>> map = new HashMap<>();
        parameters.forEach((k, v) -> {
            if (v != null)
            {
                List<Object> list = new ArrayList<>();
                ColumnDef columnDef = meta.getColumn(aliasToTable.getOrDefault(k.getTable(), k.getTable()), k.getName());
                Class<?> columnType = meta.getColumnType(columnDef);
                if (columnType == String.class)
                {
                    if (v.size() == 1 && columnDef.getType().getEnumValues().length == 0 && !columnDef.hasReference())
                    {
                        String value = (String) v.get(0);
                        if (!value.startsWith("%") && !value.endsWith("%"))
                        {
                            value = "%" + value + "%";
                        }
                        list.add(value);
                    }
                    else
                    {
                        list.addAll(v);
                    }
                }
                else
                {
                    v.forEach(a -> list.add(Utils.changeType(a, columnType)));
                }
                map.put(k, list);
            }
        });
        return map;
    }

    public static int getQuerySortingColumn(DynamicProperty[] schema, int orderColumn)
    {
        int sortCol = -1;
        int restCols = orderColumn;
        for (int i = 0; i < schema.length; i++)
        {
            if (schema[i].isHidden()) continue;

            if (restCols-- == 0)
            {
                sortCol = i + 1;
                break;
            }
        }
        return sortCol;
    }

    public static void applySort(AstStart ast, DynamicProperty[] schema, DebugQueryLogger dql,
                                 int orderColumn, String orderDir)
    {
        if (orderColumn >= 0)
        {
            int sortCol = getQuerySortingColumn(schema, orderColumn);
            if (sortCol > 0)
            {
                AstSelect sel = (AstSelect) ast.getQuery().jjtGetChild(
                        ast.getQuery().jjtGetNumChildren() - 1);

                AstOrderBy orderBy = sel.getOrderBy();
                if (orderBy == null)
                {
                    orderBy = new AstOrderBy();
                    sel.addChild(orderBy);
                    AstLimit astLimit = sel.children().select(AstLimit.class).findFirst().orElse(null);
                    if (astLimit != null)
                    {
                        sel.removeChild(astLimit);
                        sel.addChild(astLimit);
                    }
                }
                AstOrderingElement oe = new AstOrderingElement(AstNumericConstant.of(sortCol));
                if ("desc".equals(orderDir))
                {
                    oe.setDirectionToken(new Token(0, "DESC"));
                }
                orderBy.addChild(oe);
                orderBy.moveToFront(oe);
            }
            dql.log("With sort", ast);
        }
    }

    public static boolean hasColumnWithLabel(AstStart ast, String idColumnLabel)
    {
        AstQuery query = ast.getQuery();
        Optional<AstSelect> selectOpt = query.children().select(AstSelect.class).collect(MoreCollectors.onlyOne());
        if (!selectOpt.isPresent())
            return false;
        AstSelect select = selectOpt.get();
        return select.getSelectList().children()
                .select(AstDerivedColumn.class)
                .map(AstDerivedColumn::getAlias)
                .nonNull()
                .map(alias -> alias.replaceFirst("^\"(.+)\"$", "$1"))
                .map(String::toUpperCase)
                .has(idColumnLabel);
    }

    public static void addIDColumnIfNeeded(AstStart ast, Query query, DebugQueryLogger dql)
    {
        if (query.getType() == QueryType.D1 && query.getEntity().findTableDefinition() != null && !hasColumnWithLabel(ast, DatabaseConstants.ID_COLUMN_LABEL))
        {
            new ColumnAdder().addColumn(ast, query.getEntity().getName(), query.getEntity().getPrimaryKey(),
                    DatabaseConstants.ID_COLUMN_LABEL);
            dql.log("With ID column", ast);
        }
        else
        {
            dql.log("Without ID column", ast);
        }
    }
}
