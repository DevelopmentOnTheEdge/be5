/* Generated By:JJTree: Do not edit this line. AstThen.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;


public class AstBeThen extends AstBeNode
{
    public AstBeThen(int id)
    {
        super(id);
        this.tagName = "then";
    }

    private boolean implicit;

    public boolean isImplicit()
    {
        return implicit;
    }

    public void setImplicit(boolean implicit)
    {
        this.implicit = implicit;
        this.tagName = implicit ? null : "then";
    }
}
/* JavaCC - OriginalChecksum=906614d87169aba9ce67530418d62f26 (do not edit this line) */
