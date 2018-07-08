/* Generated By:JJTree: Do not edit this line. AstInValueList.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public class AstInValueList extends SimpleNode
{
    public static AstInValueList withReplacementParameter(int count)
    {
        checkArgument(count > 0);

        AstInValueList astInValueList = new AstInValueList(SqlParserTreeConstants.JJTINVALUELIST);
        for (int i = 0; i < count; i++)
        {
            astInValueList.addChild(AstReplacementParameter.get());
        }
        return astInValueList;
    }

    public static AstInValueList of(List<SimpleNode> nodes)
    {
        checkNotNull(nodes);

        AstInValueList astInValueList = new AstInValueList(SqlParserTreeConstants.JJTINVALUELIST);
        astInValueList.addChilds(nodes);

        return astInValueList;
    }

    public AstInValueList(int id)
    {
        super(id);
        this.childrenDelimiter = ",";
        this.nodePrefix = "(";
        this.nodeSuffix = ")";
    }
}
/* JavaCC - OriginalChecksum=9e5d8b9eadad3808bdda702871fbaa75 (do not edit this line) */
