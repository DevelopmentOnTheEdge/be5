package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstTableRef;

public class Ast {

    public static class ColumnList{
        AstDerivedColumn[] columns;

        ColumnList(AstDerivedColumn[] columns) {
            this.columns = columns;
        }

        public AstSelect from(String table){
            AstFrom from = new AstFrom(new AstTableRef(table));
            return new AstSelect(new AstSelectList(columns), from);
        }

        public AstSelect from(AstTableRef tableRef){
            AstFrom from = new AstFrom(tableRef);
            return new AstSelect(new AstSelectList(columns), from);
        }

    }

    public static ColumnList select(AstDerivedColumn... columns){
        return new ColumnList(columns);
    }
//
//    public static AstQuery union(AstSelect... selects){
//
//    }
}
