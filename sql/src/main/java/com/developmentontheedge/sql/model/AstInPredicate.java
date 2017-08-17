/* Generated By:JJTree: Do not edit this line. AstInPredicate.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;

public class AstInPredicate extends SimpleNode 
{
    public static AstInPredicate of(String columnName, int count)
    {
        return new AstInPredicate(
                new AstFieldReference(columnName),
                AstInValueList.of(count)
        );
    }

    private AstInPredicate(AstFieldReference fieldReference, AstInValueList inValueList)
    {
        this(SqlParserTreeConstants.JJTINPREDICATE);
        addChild(fieldReference);
        addChild(inValueList);
    }

    public AstInPredicate(int id)
    {
        super(id);
        this.childrenDelimiter = "IN";
    }
    
    private boolean inversed = false;

    public boolean isInversed()
    {
        return inversed;
    }

    public void setInversed(boolean inversed)
    {
        this.inversed = inversed;
        this.childrenDelimiter = inversed ? "NOT IN" : "IN";
    }
    
}
/* JavaCC - OriginalChecksum=1c711b45901e0bd36d05f152936b4665 (do not edit this line) */
