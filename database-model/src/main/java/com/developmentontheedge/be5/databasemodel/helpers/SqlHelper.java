package com.developmentontheedge.be5.databasemodel.helpers;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstCast;
import com.developmentontheedge.sql.model.AstReplacementParameter;
import com.google.common.collect.ObjectArrays;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.developmentontheedge.sql.model.AstWhere.NOT_NULL;


public class SqlHelper
{
    private final DbService db;

    @Inject
    public SqlHelper(DbService db)
    {
        this.db = db;
    }

    public <T> T insert(String tableName, Map<String, ?> values)
    {
        return insert(tableName, values, Collections.emptyMap());
    }

    /**
     * @param parameterCasts column name to SQL type the parameter is cast to,
     *                       e.g. "vector" for pgvector column: the value is passed as a string like '[0.1,0.2]'
     */
    public <T> T insert(String tableName, Map<String, ?> values, Map<String, String> parameterCasts)
    {
        return db.insertRaw(generateInsertSql(tableName, values, parameterCasts), values.values().toArray());
    }

    public int update(String tableName, Map<String, ?> conditions, Map<String, ?> values)
    {
        return update(tableName, conditions, values, Collections.emptyMap());
    }

    public int update(String tableName, Map<String, ?> conditions, Map<String, ?> values,
                      Map<String, String> parameterCasts)
    {
        Map<String, String> conditionsPlaceholders = conditions.entrySet().stream()
                .collect(toLinkedMap(Map.Entry::getKey, e -> "?"));

        return db.updateRaw(generateUpdateSql(tableName, conditionsPlaceholders,
                getValuePlaceholders(values, parameterCasts)),
                ObjectArrays.concat(values.values().toArray(), conditions.values().toArray(), Object.class));
    }

    public int updateIn(String tableName, String conditionColumn, Object[] conditionValues, Map<String, ?> values)
    {
        return updateIn(tableName, conditionColumn, conditionValues, values, Collections.emptyMap());
    }

    public int updateIn(String tableName, String conditionColumn, Object[] conditionValues, Map<String, ?> values,
                        Map<String, String> parameterCasts)
    {
        return db.updateRaw(generateUpdateInSql(tableName, conditionColumn, conditionValues.length,
                getValuePlaceholders(values, parameterCasts)),
                ObjectArrays.concat(values.values().toArray(), conditionValues, Object.class));
    }

    private static Map<String, Object> getValuePlaceholders(Map<String, ?> values, Map<String, String> parameterCasts)
    {
        return values.keySet().stream()
                .collect(toLinkedMap(column -> column, column -> getPlaceholder(column, parameterCasts)));
    }

    private static Object getPlaceholder(String column, Map<String, String> parameterCasts)
    {
        String type = parameterCasts.get(column);
        return type == null ? "?" : new AstCast(new AstReplacementParameter(), type);
    }

    public int delete(String tableName, Map<String, ?> conditions)
    {
        return db.updateRaw(generateDeleteSql(tableName, conditions), getWithoutConstants(conditions));
    }

    public int deleteIn(String tableName, String columnName, Object[] values)
    {
        return db.updateRaw(generateDeleteInSql(tableName, columnName, values.length), values);
    }

    private String generateInsertSql(String tableName, Map<String, ?> values, Map<String, String> parameterCasts)
    {
        Object[] columns = values.keySet().toArray();

        Object[] valuePlaceholders = values.keySet().stream()
                .map(column -> getPlaceholder(column, parameterCasts))
                .toArray(Object[]::new);

        return Ast.insert(tableName).fields(columns).values(valuePlaceholders).format();
    }

    private String generateUpdateSql(String tableName, Map<String, String> conditionsPlaceholders,
                                     Map<String, Object> valuePlaceholders)
    {
        return Ast.update(tableName).set(valuePlaceholders)
                .where(conditionsPlaceholders).format();
    }

    private String generateUpdateInSql(String tableName, String primaryKeyName, int count,
                                       Map<String, Object> valuePlaceholders)
    {
        return Ast.update(tableName).set(valuePlaceholders)
                .whereInWithReplacementParameter(primaryKeyName, count).format();
    }

    private String generateDeleteSql(String tableName, Map<String, ?> conditions)
    {
        return Ast.delete(tableName).where(conditions).format();
    }

    private String generateDeleteInSql(String tableName, String columnName, int count)
    {
        return Ast.delete(tableName).whereInPredicate(columnName, count).format();
    }

    public Object[] getWithoutConstants(Map<String, ?> conditions)
    {
        List<?> list = conditions.values().stream()
                .filter(x -> x != null && !NOT_NULL.equals(x))
                .collect(Collectors.toList());
        return list.toArray();
    }

    private static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
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
