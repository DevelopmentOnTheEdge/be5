/* Generated By:JJTree: Do not edit this line. AstUpdate.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;

import java.util.Map;
import java.util.Objects;

public class AstUpdate extends SimpleNode
{

    public AstUpdate(AstTableName tableName, AstUpdateSetList astUpdateSetList)
    {
        this(SqlParserTreeConstants.JJTUPDATE);
        addChild(tableName);
        addChild(astUpdateSetList);
    }

    public AstUpdate(int id)
    {
        super(id);
        this.nodePrefix = "UPDATE";
    }

    public AstUpdate where(Map<String, ?> conditions)
    {
        Objects.requireNonNull(conditions);
        if (!conditions.isEmpty()) where(new AstWhere(conditions));
        return this;
    }

    public void where(AstWhere where)
    {
        Objects.requireNonNull(where);
        AstWhere oldWhere = getWhere();
        if (oldWhere == null)
        {
            addChild(where);
        }
        else
        {
            oldWhere.replaceWith(where);
        }
    }

    public AstWhere getWhere()
    {
        return children().select(AstWhere.class).findFirst().orElse(null);
    }

    public AstUpdate whereInWithReplacementParameter(String columnName, int count)
    {
        Objects.requireNonNull(columnName);
        where(AstWhere.withReplacementParameter(columnName, count));
        return this;
    }
}
/* JavaCC - OriginalChecksum=a166eea221758a97dda82854eb2103cd (do not edit this line) */
