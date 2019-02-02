package com.developmentontheedge.be5.query.util;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.util.MoreStrings;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstTableRef;

public class QueryUtils
{
    public static boolean shouldBeSkipped(DynamicProperty property)
    {
        String name = property.getName();
        return property.isHidden() || MoreStrings.startsWithAny(name, DatabaseConstants.EXTRA_HEADER_COLUMN_PREFIX,
                DatabaseConstants.HIDDEN_COLUMN_PREFIX, DatabaseConstants.GLUE_COLUMN_PREFIX);
    }

    public static boolean shouldBeSkipped(String alias)
    {
        return MoreStrings.startsWithAny(alias, DatabaseConstants.EXTRA_HEADER_COLUMN_PREFIX,
                DatabaseConstants.HIDDEN_COLUMN_PREFIX, DatabaseConstants.GLUE_COLUMN_PREFIX);
    }

    public static void countFromQuery(AstQuery query)
    {
        AstSelect select = Ast.selectCount().from(AstTableRef.as(
                new AstParenthesis(query.clone()),
                new AstIdentifierConstant("data", true)
        ));
        query.replaceWith(new AstQuery(select));
    }
}
