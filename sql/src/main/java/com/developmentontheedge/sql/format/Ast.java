package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstColumnList;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstInsert;
import com.developmentontheedge.sql.model.AstInsertValueList;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstReplacementParameter;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstStringConstant;
import com.developmentontheedge.sql.model.AstTableName;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.SimpleNode;

import java.util.Arrays;

public class Ast
{

    public static class ColumnList
    {
        AstDerivedColumn[] columns;

        ColumnList(AstDerivedColumn[] columns)
        {
            this.columns = columns;
        }

        public AstSelect from(String table)
        {
            AstFrom from = new AstFrom(new AstTableRef(table));
            return new AstSelect(new AstSelectList(columns), from);
        }

        public AstSelect from(AstTableRef tableRef)
        {
            AstFrom from = new AstFrom(tableRef);
            return new AstSelect(new AstSelectList(columns), from);
        }

    }

    public static class InsertTable
    {
        String tableName;

        InsertTable(String tableName) {
            this.tableName = tableName;
        }

        public InsertValues fields(Object... columns){
            return new InsertValues(tableName, columns);
        }
    }

    public static class InsertValues
    {
        String tableName;
        Object[] columns;

        InsertValues(String tableName, Object[] columns) {
            this.tableName = tableName;
            this.columns = columns;
        }

        public AstInsert values(Object... values)
        {
            AstFieldReference[] columnsNodes = Arrays.stream(columns).map(x ->
                    (AstFieldReference) ((x instanceof AstFieldReference) ? x : new AstFieldReference((String) x))
            ).toArray(AstFieldReference[]::new);

            SimpleNode[] valuesNodes = Arrays.stream(values).map(x -> {
                if(x instanceof SimpleNode)return (SimpleNode)x;
                if(x instanceof String) {
                    if("?".equals(x))return new AstReplacementParameter();
                    return new AstStringConstant((String) x);
                }
                return new AstNumericConstant((Number) x);
            }).toArray(SimpleNode[]::new);

            return new AstInsert(new AstTableName(tableName),
                    new AstColumnList(columnsNodes), new AstInsertValueList(valuesNodes));
        }
    }

    public static ColumnList select(AstDerivedColumn... columns)
    {
        return new ColumnList(columns);
    }

    public static InsertTable insert(String tableName)
    {
        return new InsertTable(tableName);
    }

//    Ast.delete("users").where();
//    Ast.update("users").set(Map<>()).where();
//
//    public static AstQuery union(AstSelect... selects){
//
//    }
}
