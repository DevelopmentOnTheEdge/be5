package com.developmentontheedge.be5.util;

import static com.google.common.base.Preconditions.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import one.util.streamex.StreamEx;

import com.developmentontheedge.be5.metadata.sql.DatabaseConnector;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Builds SQL statements accourding to the selected RDBMS syntax.
 * 
 * @author asko
 */
public class SqlBuilder
{
    public static class Change
    {
        
        public final String name;
        public final Object value;
        
        public Change(String name, Object value)
        {
            this.name = name;
            this.value = value;
        }
        
    }
    
    public static class Condition
    {
        
        /**
         * Always a field name.
         */
        public final String lhs;
        /**
         * Value or a field name.
         */
        public final Object rhs;
        
        public Condition(String lhs, Value rhs)
        {
            this.lhs = lhs;
            this.rhs = rhs;
        }
        
        public Condition(String lhs, FieldName rhs)
        {
            this.lhs = lhs;
            this.rhs = rhs;
        }
        
        @Deprecated
        public Condition(String lhs, Object rhs)
        {
            checkArgument(!(rhs instanceof Value));
            checkArgument(!(rhs instanceof FieldName));
            this.lhs = lhs;
            this.rhs = new Value(rhs);
        }
        
    }
    
    public static class Value
    {
        
        public final Object value;
        
        public Value(Object value)
        {
            this.value = value;
        }
        
    }
    
    public static class FieldName
    {
        
        public final String fieldName;
        
        public FieldName(String fieldName)
        {
            this.fieldName = fieldName;
        }
        
    }
    
    /**
     * 
     * @author asko
     * @see <a href="http://savage.net.au/SQL/sql-2003-2.bnf.html#select%20list">http://savage.net.au/SQL/sql-2003-2.bnf.html#select%20list</a>
     * @see Asterisk
     * @see SelectSublist
     */
    private static interface SelectList
    {
        String print();
    }
    
    private static class Asterisk implements SelectList
    {
        
        public Asterisk()
        {
            // stateless
        }
        
        @Override
        public String print()
        {
            return "*";
        }
        
    }
    
    private class SelectSublist implements SelectList
    {
        
        private final List<DerivedColumn> list;
        
        public SelectSublist(List<DerivedColumn> derivedColumns)
        {
            this.list = ImmutableList.copyOf(derivedColumns);
        }
        
        @Override
        public String print()
        {
            return StreamEx.of(list).map(DerivedColumn::print).joining(", ");
        }
        
    }
    
    /**
     * 
     * @see http://savage.net.au/SQL/sql-2003-2.bnf.html#derived%20column
     * @see http://savage.net.au/SQL/sql-2003-2.bnf.html#as%20clause
     * @see http://savage.net.au/SQL/sql-2003-2.bnf.html#column%20name
     * @author asko
     */
    private class DerivedColumn
    {
        
        private final ValueExpression valueExpression;
        private final Optional<Identifier> asClause;
        
        public DerivedColumn(ValueExpression valueExpression, Optional<Identifier> asClause)
        {
            this.valueExpression = valueExpression;
            this.asClause = asClause;
        }
        
        public String print()
        {
            return valueExpression.print() + asClause.map(identifier -> " AS " + identifier.print()).orElse("");
        }
        
    }
    
    /**
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#value%20expression
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#common%20value%20expression
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#reference%20value%20expression
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#value%20expression%20primary
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#nonparenthesized%20value%20expression%20primary
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#column%20reference
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#basic%20identifier%20chain
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#basic%20identifier%20chain
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#identifier%20chain
     * 
     * @author asko
     */
    private interface ValueExpression
    {
        String print();
    }
    
    private class IdentifierChain implements ValueExpression
    {
        
        private final List<Identifier> identifiers;

        public IdentifierChain(List<Identifier> identifiers)
        {
            this.identifiers = identifiers;
        }
        
        @Override
        public String print()
        {
            return StreamEx.of(identifiers).map(Identifier::print).joining(".");
        }
        
    }
    
    private class Identifier
    {
        
        private final String identifier;
        
        public Identifier(String identifier)
        {
            this.identifier = identifier;
        }
        
        public String print()
        {
            return rdbms.getTypeManager().normalizeIdentifier(identifier);
        }
        
    }
    
    private class Select
    {
        
        private final SelectList selectList;
        private final TableExpression tableExpression;

        public Select(SelectList selectList, TableExpression tableExpression)
        {
            this.selectList = selectList;
            this.tableExpression = tableExpression;
        }
        
        public String print()
        {
            return "SELECT " + selectList.print() + " " + tableExpression.print();
        }
        
    }
    
    /**
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#table%20expression
     * 
     * @author asko
     */
    private class TableExpression
    {
        
        private final FromClause fromClause;
        private final Optional<WhereClause> whereClause;
        private final Optional<Limit> limitClause;
        
        public TableExpression(FromClause fromClause)
        {
            this(fromClause, Optional.empty(), Optional.empty());
        }
        
        public TableExpression(FromClause fromClause, Optional<WhereClause> whereClause)
        {
            this(fromClause, whereClause, Optional.empty());
        }
        
        public TableExpression(FromClause fromClause, Optional<WhereClause> whereClause, Optional<Limit> limit)
        {
            this.fromClause = fromClause;
            this.whereClause = whereClause;
            this.limitClause = limit;
        }
        
        public String print()
        {
            return fromClause.print()
                    + whereClause.map(wc -> " " + wc.print()).orElse("")
                    + limitClause.map(l -> " " + l.print()).orElse("");
        }
        
    }
    
    /**
     * http://savage.net.au/SQL/sql-2003-2.bnf.html#from%20clause
     * 
     * @author asko
     */
    private class FromClause
    {
        
        /**
         * http://savage.net.au/SQL/sql-2003-2.bnf.html#table%20reference%20list
         */
        private final List<String> tableReferenceList;
        
        public FromClause(List<String> tableReferenceList)
        {
            this.tableReferenceList = ImmutableList.copyOf(tableReferenceList);
        }
        
        public String print()
        {
            return "FROM " + Joiner.on(", ").join(tableReferenceList);
        }
        
    }
    
    /**
     * Note that this class is not finished.
     * 
     * @see <a href="http://savage.net.au/SQL/sql-2003-2.bnf.html#where%20clause">http://savage.net.au/SQL/sql-2003-2.bnf.html#where%20clause</a>
     * @author asko
     */
    private class WhereClause
    {
        
        private final List<Condition> conditions;

        public WhereClause(List<Condition> conditions)
        {
            this.conditions = conditions;
        }
        
        public String print()
        {
            return conditions.isEmpty() ? "" : "WHERE " + searchCondition();
        }
        
        private String searchCondition()
        {
            return StreamEx.of(conditions).map(this::and).joining(" AND ");
        }
        
        private String and(Condition cond)
        {
            checkNotNull(cond);
            checkArgument(SqlStatements.isFieldName(cond.lhs));
            return fieldName(cond.lhs) + " = " + expr(cond.rhs);
        }
        
    }
    
    private class Limit
    {
        
        private final int limit;

        public Limit(int limit)
        {
            this.limit = limit;
        }
        
        public String print()
        {
            return "LIMIT " + limit;
        }
        
    }
    
    private final Rdbms rdbms;
    
    public static SqlBuilder create(DatabaseConnector connector)
    {
        return new SqlBuilder( DatabaseUtils.getRdbms( connector ) ); 
    }
    
    private SqlBuilder(Rdbms rdbms)
    {
        this.rdbms = rdbms;
    }
    
    public String update(String tableName, List<Condition> conditions, List<Change> changes)
    {
        checkNotNull(tableName);
        checkNotNull(conditions);
        checkNotNull(changes);
        checkArgument(SqlStatements.isTableName(tableName));
        return "update " + identifier(tableName) + " " + set(changes) + " " + new WhereClause(conditions).print();
    }
    
    private String set(List<Change> changes)
    {
        return "set " + changes(changes);
    }
    
    private String changes(List<Change> changes)
    {
        return StreamEx.of(changes).map(this::change).joining(", ");
    }
    
    private String change(Change field)
    {
        checkNotNull(field);
        checkArgument(SqlStatements.isFieldName(field.name));
        return identifier(field.name) + " = " + value(field.value);
    }

    private String identifier(String identifier)
    {
        return new Identifier(identifier).print();
    }
    
    private String expr(Object value)
    {
        if (value instanceof Value)
        {
            return value(((Value) value).value);
        }
        else if (value instanceof FieldName)
        {
            return fieldName(((FieldName) value).fieldName);
        }
        
        throw new IllegalStateException();
    }
    
    /**
     * FIXME this should happen earlier, inside FieldName for example
     */
    private String fieldName(String fullFieldName)
    {
        if (fullFieldName.indexOf('.') != -1)
        {
            List<String> splitted = Splitter.on('.').splitToList(fullFieldName);
            if (splitted.size() == 2)
            {
                return identifier(splitted.get(0)) + "." + identifier(splitted.get(1));
            }
            throw new IllegalStateException();
        }
        return identifier(fullFieldName);
    }
    
    private String value(Object value)
    {
        // note that values can be null
        
        if (value == null)
        {
            return SqlStatements.NULL;
        }
        
        if (value instanceof String)
        {
            return rdbms.getMacroProcessorStrategy().str((String) value);
        }
        
        if (value instanceof Long || value instanceof Integer)
        {
            return value.toString();
        }
        
        if (value instanceof Duration)
        {
            return Long.toString(((Duration) value).toNanos());
        }
        
        if (value instanceof Double)
        {
            // TODO
            // A user can define a field with better or even unknown precision.
            // Probably we should determine formatting type by some additional criterion.
            return new DecimalFormat("#.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format((double) value);
        }
        
        if (value instanceof LocalDate)
        {
            return "'"+((LocalDate)value).format( DateTimeFormatter.ISO_LOCAL_DATE )+"'";
        }
        
        if (value instanceof LocalDateTime)
        {
            return "'"+((LocalDateTime)value).format( DateTimeFormatter.ISO_LOCAL_DATE_TIME )+"'";
        }
        
        if (value instanceof Enum)
        {
            return "'"+value.toString().toLowerCase( Locale.ENGLISH )+"'";
        }
        
        if (value instanceof Boolean)
        {
            return (boolean)value ? "'yes'" : "'no'";
        }
        
        throw new IllegalArgumentException("Unknown type");
    }
    
    public String insert(String tableName, String[] names, Object[] values)
    {
        checkNotNull(names);
        checkNotNull(values);
        return insert(tableName, Arrays.asList(names), Arrays.asList(values));
    }

    public String insert(String tableName, List<String> names, List<Object> values)
    {
        checkArgument(SqlStatements.isTableName(tableName));
        checkNotNull(names);
        checkNotNull(values);
        for (String name : names)
            checkArgument(SqlStatements.isFieldName(name));
        return "insert into " + identifier(tableName) + "(" + fields(names) + ") " + values(values);
    }
    
    public String insert(String tableName, List<Change> changes)
    {
        checkArgument(SqlStatements.isTableName(tableName));
        checkNotNull(changes);
        return "insert into " + identifier(tableName) +
            StreamEx.of(changes).map( change -> identifier( change.name ) ).joining( ", ", "(", ")" ) +
            StreamEx.of(changes).map( change -> value( change.value ) ).joining( ", ", "values (", ")" );
    }
    
    private String fields(List<String> fieldNames)
    {
        return selectSublistByColumnIdentifiers(fieldNames).print();
    }
    
    private String values(List<Object> values)
    {
        return StreamEx.of(values).map(this::value).joining(", ", "values (", ")");
    }
    
    public String selectCountAsValue(String tableName, List<Condition> conditions)
    {
        checkArgument(SqlStatements.isTableName(tableName));
        checkNotNull(conditions);
        return "select (count(*) > 0) as " + identifier("value") + " from " + identifier(tableName) + " " + new WhereClause(conditions).print();
    }
    
    public String selectColumns(List<String> columnIdentifiers, String tableReference)
    {
        return selectColumns(columnIdentifiers, ImmutableList.of(tableReference));
    }
    
    public String selectColumns(List<String> columnIdentifiers, List<String> tableReferenceList)
    {
        return new Select(
                selectSublistByColumnIdentifiers(columnIdentifiers),
                new TableExpression(new FromClause(tableReferenceList))).print();
    }
    
    public String select(String tableName, List<Condition> conditions)
    {
        checkArgument(SqlStatements.isTableName(tableName));
        checkNotNull(conditions);
        return select(ImmutableList.of(tableName), conditions);
    }
    
    public String select(List<String> tableReferenceList, List<Condition> conditions)
    {
        for (String tableName : tableReferenceList)
            checkArgument(SqlStatements.isTableName(tableName));
        checkNotNull(conditions);
        return new Select(new Asterisk(),
                new TableExpression(
                    new FromClause(tableReferenceList),
                    Optional.of(new WhereClause(conditions)))).print();
    }
    
    public String select(List<String> tableReferenceList, List<Condition> conditions, int limit)
    {
        for (String tableName : tableReferenceList)
            checkArgument(SqlStatements.isTableName(tableName));
        checkNotNull(conditions);
        checkArgument(limit > 0);
        return new Select(new Asterisk(),
                new TableExpression(
                    new FromClause(tableReferenceList),
                    Optional.of(new WhereClause(conditions)),
                    Optional.of(new Limit(limit)))).print();
    }
    
    public String selectOne(List<String> tableNames, List<Condition> conditions)
    {
        return select(tableNames, conditions, 1);
    }
    
    private SelectSublist selectSublistByColumnIdentifiers(List<String> identifiers)
    {
        return new SelectSublist(
                StreamEx.of(identifiers)
                    .map(Identifier::new)
                    .map(this::valueExpressionByIdentifier)
                    .map(this::derivedColumnByValueExpression)
                    .toList());
    }
    
    private ValueExpression valueExpressionByIdentifier(Identifier identifier)
    {
        return valueExpressionByIdentifierChain(ImmutableList.of(identifier));
    }
    
    private ValueExpression valueExpressionByIdentifierChain(List<Identifier> identifiers)
    {
        return new IdentifierChain(identifiers);
    }
    
    private DerivedColumn derivedColumnByValueExpression(ValueExpression valueExpression)
    {
        return new DerivedColumn(valueExpression, Optional.empty());
    }
    
}
