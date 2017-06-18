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
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.SimpleNode;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public static class InsertBuilder
    {
        Object[] columns;

        InsertBuilder(Object[] columns) {
            this.columns = columns;
        }

        public AstInsert values(Object... values)
        {
            List<AstFieldReference> columnsNodes = Arrays.stream(columns).map(x ->
                    (AstFieldReference) ((x instanceof AstFieldReference) ? x : new AstFieldReference((String) x))
            ).collect(Collectors.toList());

            List<SimpleNode> valuesNodes = Arrays.stream(values).map(x -> {
                if(x instanceof SimpleNode)return (SimpleNode)x;
                if(x instanceof String) {
                    if("?".equals(x))return new AstReplacementParameter();
                    return new AstStringConstant((String) x);
                }
                return new AstNumericConstant((Number) x);
            }).collect(Collectors.toList());

            return new AstInsert(
                    new AstColumnList(columnsNodes.toArray(new AstFieldReference[columnsNodes.size()])),
                    new AstInsertValueList(valuesNodes.toArray(new SimpleNode[valuesNodes.size()])));
        }
    }

    public static ColumnList select(AstDerivedColumn... columns)
    {
        return new ColumnList(columns);
    }

    public static InsertBuilder insert(Object... columns)
    {
        return new InsertBuilder(columns);
    }
//
//    public static AstQuery union(AstSelect... selects){
//
//    }
}
