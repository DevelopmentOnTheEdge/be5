package com.developmentontheedge.be5.api.sql;

import com.developmentontheedge.be5.api.services.impl.ConstantDatabaseService;
import com.developmentontheedge.be5.api.services.impl.SelectExecutor;
import com.developmentontheedge.be5.util.Generators;
import com.developmentontheedge.be5.util.SqlBuilder;
import com.developmentontheedge.be5.util.SqlBuilder.Condition;
import com.developmentontheedge.be5.util.SqlBuilder.FieldName;
import com.developmentontheedge.be5.util.SqlBuilder.Value;
import com.developmentontheedge.be5.util.SqlStatements;
import com.developmentontheedge.dbms.DbmsConnector;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Selector
{
    
    @FunctionalInterface
    public interface ResultSetParser<T>
    {
        T parse(ResultSet rs) throws SQLException;
    }
    
    @FunctionalInterface
    public interface ResultSetConsumer
    {
        void accept(ResultSet rs) throws SQLException;
    }
    
    public class SelectorEndpoint
    {
        
        private final SelectorEndpoint previous;
        private final Condition condition;
        
        private SelectorEndpoint(SelectorEndpoint previous, String lhs, FieldName rhs)
        {
            this.previous = previous;
            this.condition = new Condition(lhs, rhs);
        }
        
        private SelectorEndpoint(SelectorEndpoint previous, String lhs, Value rhs)
        {
            this.previous = previous;
            this.condition = new Condition(lhs, rhs);
        }

        private SelectorEndpoint(String lhs, Value rhs)
        {
            this(null, lhs, rhs);
        }
        
        private SelectorEndpoint(String lhs, FieldName rhs)
        {
            this(null, lhs, rhs);
        }
                
        /**
         * Adds a condition.
         */
        public SelectorEndpoint and(String fieldName, Object fieldValue)
        {
            checkNotNull(fieldName);
            checkNotNull(fieldValue);
            return new SelectorEndpoint(this, fieldName, new Value(fieldValue));
        }
        
        public SelectorEndpoint bound(String field1, String field2)
        {
            checkNotNull(field1);
            checkNotNull(field2);
            return new SelectorEndpoint(this, field1, new FieldName(field2));
        }
        
        /**
         * Runs the query. The parser must not return <code>null</code>,
         * otherwise a {@link NullPointerException} will be thrown.
         */
        public <T> List<T> select(ResultSetParser<T> parser)
        {
            return Selector.this.select(genSelectAll(), parser);
        }
        
        public void forEach(ResultSetConsumer consumer)
        {
            Selector.this.forEach(genSelectAll(), consumer);
        }
        
        public <T> T getOnly(ResultSetParser<T> parser)
        {
            return Iterables.getOnlyElement(selectTwo(parser));
        }
        
        public <T> T findFirst(ResultSetParser<T> parser)
        {
            return Iterables.getFirst(selectOne(parser), null);
        }
        
        public <T> Optional<T> findOne(ResultSetParser<T> parser)
        {
            return Optional.ofNullable(findFirst(parser));
        }
        
        private <T> List<T> selectTwo(ResultSetParser<T> parser)
        {
            return Selector.this.select(genSelectTwo(), parser);
        }
        
        private <T> List<T> selectOne(ResultSetParser<T> parser)
        {
            return Selector.this.select(genSelectOne(), parser);
        }
        
        private String genSelectAll()
        {
            return SqlBuilder.create(connector).select(tableNames, collectConditions());
        }
        
        private String genSelectOne()
        {
            return SqlBuilder.create(connector).selectOne(tableNames, collectConditions());
        }
        
        private String genSelectTwo()
        {
            return SqlBuilder.create(connector).select(tableNames, collectConditions(), 2);
        }
        
        private List<Condition> collectConditions()
        {
            return Generators.reverseList(this, p -> p.previous, p -> p.condition);
        }
        
    }
    
    /**
     * Creates a selector.
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public static Selector from(DbmsConnector connector, String tableName)
    {
        checkNotNull(connector);
        checkNotNull(tableName);
        checkArgument(SqlStatements.isTableName(tableName));
        return new Selector(connector, ImmutableList.of(tableName));
    }
    
    public static Selector from(DbmsConnector connector, String tableName, String... tableNames)
    {
        checkNotNull(connector);
        checkNotNull(tableName);
        checkArgument(SqlStatements.isTableName(tableName));
        for (String tn : tableNames)
            checkArgument(SqlStatements.isTableName(tn));
        return new Selector(connector, ImmutableList.<String>builder().add(tableName).add(tableNames).build());
    }
    
    private final DbmsConnector connector;
    private final List<String> tableNames;
    
    private Selector(DbmsConnector connector, List<String> tableNames)
    {
        this.connector = connector;
        this.tableNames = tableNames;
    }
    
    /**
     * Defines a condition.
     */
    public SelectorEndpoint with(String fieldName, Object fieldValue)
    {
        checkNotNull(fieldName);
        checkNotNull(fieldValue);
        return new SelectorEndpoint(fieldName, new Value(fieldValue));
    }
    
    public SelectorEndpoint bound(String field1, String field2)
    {
        checkNotNull(field1);
        checkNotNull(field2);
        return new SelectorEndpoint(field1, new FieldName(field2));
    }
    
    /**
     * Runs with a simple condition.
     */
    public <T> List<T> selectWith(String fieldName, Object fieldValue, ResultSetParser<T> parser)
    {
        checkNotNull(fieldName);
        checkNotNull(fieldName);
        return with(fieldName, fieldValue).select(parser);
    }
    
    /**
     * Selects all the rows of the table.
     */
    public <T> List<T> selectAll(ResultSetParser<T> parser)
    {
        return select(SqlBuilder.create(connector).select(tableNames, ImmutableList.of()), parser);
    }
    
    public <T> List<T> selectAll(List<String> columnNames, ResultSetParser<T> parser)
    {
        return select(SqlBuilder.create(connector).selectColumns(columnNames, tableNames), parser);
    }
    
    /**
     * Nullable.
     */
    public <T> T findFirst(ResultSetParser<T> parser)
    {
        return Iterables.getFirst(select(SqlBuilder.create(connector).selectOne(tableNames, ImmutableList.of()), parser), null);
    }
    
    public <T> Optional<T> findOne(ResultSetParser<T> parser)
    {
        return Optional.ofNullable(findFirst(parser));
    }
    
    /**
     * Nullable.
     */
    public <T> T findFirstWith(String fieldName, Object fieldValue, ResultSetParser<T> parser)
    {
        checkNotNull(fieldName);
        checkNotNull(fieldName);
        return with(fieldName, fieldValue).findFirst(parser);
    }
    
    /**
     * The same a the {@link Selector#findFirstWith(String, Object, ResultSetParser)}, but returns an optional value.
     */
    public <T> Optional<T> findOneWith(String fieldName, Object fieldValue, ResultSetParser<T> parser)
    {
        checkNotNull(fieldName);
        checkNotNull(fieldName);
        return with(fieldName, fieldValue).findOne(parser);
    }
    
    private <T> List<T> select(String statement, ResultSetParser<T> parser)
    {
        return new SelectExecutor(new ConstantDatabaseService(connector)).select(statement, parser);
    }
    
    private void forEach(String statement, ResultSetConsumer consumer)
    {
        new SelectExecutor(new ConstantDatabaseService(connector)).forEach(statement, consumer);
    }
    
}
