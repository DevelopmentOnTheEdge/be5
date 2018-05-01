package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.sql.format.Ast;
import com.google.common.collect.ObjectArrays;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public class SqlHelper
{
    private final SqlService db;

    public SqlHelper(SqlService db)
    {
        this.db = db;
    }

    public <T> T insert(String tableName, Map<String, ? super Object> values)
    {
        return db.insert(generateInsertSql(tableName, values), values.values().toArray());
    }

    public int update(String tableName, String primaryKeyName, Object primaryKeyValue, Map<String, ? super Object> values)
    {
        Map<String, String> valuePlaceholders = values.entrySet().stream()
                .collect(toLinkedMap(Map.Entry::getKey, e -> "?"));

        return db.update(generateUpdateSql(tableName, primaryKeyName, valuePlaceholders),
                ObjectArrays.concat(values.values().toArray(), primaryKeyValue));
    }

    public int delete(String tableName, Map<String, ? super Object> values)
    {
        return db.update(generateDeleteSql(tableName, values), values.keySet().toArray());
    }

    public int deleteIn(String tableName, String columnName, Object[] values)
    {
        return db.update(generateDeleteInSql(tableName, columnName, values.length), values);
    }

    public String generateInsertSql(String tableName, Map<String, ? super Object> values)
    {
        Object[] columns = values.keySet().toArray();

        Object[] valuePlaceholders = values.keySet().stream()
                .map(x -> "?")
                .toArray(Object[]::new);

        return Ast.insert(tableName).fields(columns).values(valuePlaceholders).format();
    }

    public String generateUpdateSql(String tableName, String primaryKeyName, Map<String, String> values)
    {
        return Ast.update(tableName).set(values)
                .where(Collections.singletonMap(primaryKeyName, "?")).format();
    }

    public String generateDeleteSql(String tableName, Map<String, ? super Object> values)
    {
        return Ast.delete(tableName).where(values).format();
    }

    public String generateDeleteInSql(String tableName, String columnName, int count)
    {
        return Ast.delete(tableName).whereInPredicate(columnName, count).format();
    }

    public static <T, K, U> Collector<T, ?, Map<K,U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(keyMapper, valueMapper,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new);
    }
}
