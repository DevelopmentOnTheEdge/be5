/* Generated By:JJTree: Do not edit this line. AstCast.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;

public class AstFirstDayOf extends SimpleNode
{
    public AstFirstDayOf(int id)
    {
        super(id);
    }

    public AstFirstDayOf(SimpleNode node, String type)
    {
        this(0);
        this.nodeSuffix = type.toUpperCase();
        addChild(node);
    }
}

