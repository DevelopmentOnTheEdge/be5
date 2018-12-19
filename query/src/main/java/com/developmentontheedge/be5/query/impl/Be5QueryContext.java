package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.sql.format.QueryContext;
import com.developmentontheedge.sql.model.AstBeSqlSubQuery;
import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Be5QueryContext implements QueryContext
{
    private final Map<String, AstBeSqlSubQuery> subQueries = new HashMap<>();

    private final Query query;
    private final Map<String, List<Object>> parameters;
    private final QuerySession querySession;
    private final UserInfo userInfo;
    private final Meta meta;

    public Be5QueryContext(Query query, Map<String, ?> parameters, QuerySession querySession, UserInfo userInfo,
                           Meta meta)
    {
        this.query = query;
        this.parameters = getMapOfList(parameters);
        this.querySession = querySession;
        this.userInfo = userInfo;
        this.meta = meta;
    }

    @Override
    public Map<String, AstBeSqlSubQuery> getSubQueries()
    {
        return subQueries;
    }

    @Override
    public StreamEx<String> roles()
    {
        return StreamEx.of(userInfo.getCurrentRoles());
    }

    @Override
    public String resolveQuery(String entityName, String queryName)
    {
        return meta.getQuery(entityName == null ? query.getEntity().getName() : entityName, queryName)
                .getFinalQuery();
    }

    @Override
    public String getUserName()
    {
        return userInfo.getUserName();
    }

    @Override
    public Object getSessionVariable(String name)
    {
        return querySession.get(name);
    }

    @Override
    public String getParameter(String name)
    {
        if (parameters.get(name) == null)
            return null;
        if (parameters.get(name).size() != 1)
            throw new IllegalStateException(name + " contains more than one value");
        else
            return parameters.get(name).get(0) + "";
    }

    @Override
    public List<String> getListParameter(String name)
    {
        if (parameters.get(name) == null) return null;
        return parameters.get(name).stream().map(x -> x + "").collect(Collectors.toList());
    }

    @Override
    public Map<String, List<Object>> getParameters()
    {
        return parameters;
    }

    @Override
    public Map<String, String> asMap()
    {
        return StreamEx.ofKeys(parameters).toMap(this::getParameter);
    }

    @Override
    public String getDictionaryValue(String tagName, String name, Map<String, String> conditions)
    {
        throw new UnsupportedOperationException();
//            EntityModel entityModel = database.get().getEntity(tagName);
//            RecordModel row = entityModel.getBy(conditions);
//
//            String value = row.getValue(name).toString();
//
//            if(!meta.isNumericColumn(entityModel.getEntity(), name))
//            {
//                value = "'" + value + "'";
//            }
//
//            return value;
    }

    private Map<String, List<Object>> getMapOfList(Map<String, ?> parameters)
    {
        Map<String, List<Object>> mapOfList = new HashMap<>();
        parameters.forEach((k, v) -> mapOfList.put(k, getParameterList(v)));
        return mapOfList;
    }

    private static List<Object> getParameterList(Object value)
    {
        if (value == null) return null;

        if (value instanceof String[])
        {
            return Arrays.asList((String[]) value);
        }
        else if (value instanceof List)
        {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            return list;
        }
        else
        {
            return Collections.singletonList(value.toString());
        }
    }
}
