package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.util.Strings2;
import com.developmentontheedge.sql.format.ColumnAdder;
import com.developmentontheedge.sql.format.ColumnRef;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstLimit;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstOrderBy;
import com.developmentontheedge.sql.model.AstOrderingElement;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstStringConstant;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.SimpleNode;
import com.developmentontheedge.sql.model.Token;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.query.util.QueryUtils.shouldBeSkipped;

public class QueryMetaHelper
{
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;

    @Inject
    public QueryMetaHelper(Meta meta, UserAwareMeta userAwareMeta)
    {
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
    }

    void applyFilters(AstStart ast, Map<String, List<Object>> parameters)
    {
        String mainTableDefName = getMainTableDefName(ast);
        if( mainTableDefName == null )
            return;
 
        Set<String> usedParams = ast.tree().select(AstBeParameterTag.class).map(AstBeParameterTag::getName).toSet();

        Map<String, String> aliasToTable = getAliasToTable(ast);

        Map<ColumnRef, List<Object>> rawFilters = EntryStream.of(parameters)
                .removeKeys(usedParams::contains)
                .removeKeys(QueryMetaHelper::isNotQueryParameters)
                .removeKeys(k -> isNotContainsInQuery(mainTableDefName, aliasToTable, k))
                .mapKeys(k -> ColumnRef.resolve(ast, k.contains(".") ? k : mainTableDefName + "." + k))
                .nonNullKeys()
                .toMap();

        Map<ColumnRef, List<Object>> filters = resolveTypes(rawFilters, aliasToTable);

        if (!filters.isEmpty())
        {
            new Be5FilterApplier().addFilter(ast, filters);
        }
    }

    private static Map<String, String> getAliasToTable(AstStart ast)
    {
        return ast.tree()
                    .select(AstTableRef.class)
                    .filter(t -> t.getTable() != null && t.getAlias() != null)
                    .collect(Collectors.toMap(AstTableRef::getAlias, AstTableRef::getTable,
                            (address1, address2) -> address1));
    }

    boolean isNotContainsInQuery(String mainTableDefName, Map<String, String> aliasToTable, String key)
    {
        List<String> split = StreamEx.split(key, "\\.").toList();
        if (split.size() == 1)
        {
            return mainTableDefName == null || meta.getColumn(mainTableDefName, split.get(0)) == null;
        }
        else
        {
            String entityName = Strings2.joinWithoutTail(".", split);
            final String columnName = split.get(split.size() - 1);
            if (aliasToTable.get(entityName) != null)
            {
                entityName = aliasToTable.get(entityName);
            }
            return !meta.hasEntity(entityName) || meta.getColumn(entityName, columnName) == null;
        }
    }

    private static boolean isNotQueryParameters(String key)
    {
        return key.startsWith("_") && key.endsWith("_");
    }

    void resolveTypeOfRefColumn(AstStart ast)
    {
        String mainTableDefName = getMainTableDefName(ast); 

        ast.tree().select(AstBeParameterTag.class).forEach((AstBeParameterTag tag) -> {
            if (tag.getType() != null || mainTableDefName == null) 
                return;
            
            ColumnDef columnDef = getColumnDef(ast, tag, mainTableDefName);
            if (columnDef != null)
                tag.setType(meta.getColumnType(columnDef).getName());
        });
    }

    public static String getMainTableDefName(AstStart ast)
    {
        try
        {
            return ast.getQuery().tree()
                    .select(AstTableRef.class)
                    .findFirst().get().getTable();
        }
        catch(Exception e) {}
        
        return null;
    }

    @Nullable
    public ColumnDef getColumnDef(AstStart ast, AstBeParameterTag beParameterTag, String mainEntityName)
    {
        if (beParameterTag.getRefColumn() != null)
        {
            ColumnDef columnDef = getColumnDef(ast, beParameterTag.getRefColumn(), mainEntityName);
            if (columnDef == null)
            {
                throw new IllegalArgumentException("Can not resolve " +
                        "refColumn=\"" + beParameterTag.getRefColumn() + "\"");
            }
            return columnDef;
        }
        else
        {
            SimpleNode node = beParameterTag.jjtGetParent();
            if (node.getClass() == AstStringConstant.class)
            {
                node = node.jjtGetParent();
            }
            if (node.jjtGetParent().getClass() == AstOrderBy.class)
            {
                return null;
            }
            Optional<AstFieldReference> first = node.children().select(AstFieldReference.class).findFirst();
            if (first.isPresent())
            {
                return getColumnDef(ast, first.get().getValue(), mainEntityName);
            }
            else
            {
                return null;
            }
        }
    }

    @Nullable
    private ColumnDef getColumnDef(AstStart ast, String rawColumnDef, String mainEntityName)
    {
        final List<String> splittedTo = StreamEx.split(rawColumnDef, "\\.").toList();
        if (splittedTo.size() == 1)
        {
            return meta.getColumn(mainEntityName, splittedTo.get(0));
        }
        else
        {
            String entityName = Strings2.joinWithoutTail(".", splittedTo);
            final String column = splittedTo.get(splittedTo.size() - 1);
            Set<String> entityNames = meta.getProject().getEntityNames();
            if (!entityNames.contains(entityName))
            {
                if (getAliasToTable(ast).get(entityName) == null)
                {
                    throw new RuntimeException("Entity with alias '" + entityName + "' not found, " +
                            "for " + rawColumnDef);
                }
                entityName = getAliasToTable(ast).get(entityName);
            }
            return meta.getColumn(entityName, column);
        }
    }

    private Map<ColumnRef, List<Object>> resolveTypes(Map<ColumnRef, List<Object>> parameters,
                                                             Map<String, String> aliasToTable)
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

    private static int getQuerySortingColumn(AstStart ast, int orderColumn)
    {
        AstSelectList selectList = ast.getQuery().getSelect().getSelectList();
        if (selectList.isAllColumns()) return orderColumn;

        int sortCol = -1;
        int restCols = orderColumn - 1;
        for (int i = 0; i < selectList.jjtGetNumChildren(); i++)
        {
            String alias = ((AstDerivedColumn) selectList.child(i)).getAlias();
            if (alias != null && shouldBeSkipped(alias)) continue;

            if (restCols-- == 0)
            {
                sortCol = i + 1;
                break;
            }
        }
        return sortCol;
    }

    static void applySort(AstStart ast, int orderColumn, String orderDir)
    {
        if (orderColumn >= 0)
        {
            int sortCol = getQuerySortingColumn(ast, orderColumn);
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
        }
    }

    static void addIDColumnLabel(AstStart ast, Query query)
    {
        new ColumnAdder().addColumn(ast, query.getEntity().getName(), query.getEntity().getPrimaryKey(),
                DatabaseConstants.ID_COLUMN_LABEL);
    }

    String getTotalTitle(Query query)
    {
        return userAwareMeta.getColumnTitle(query.getEntity().getName(), query.getName(), "total");
    }
}
