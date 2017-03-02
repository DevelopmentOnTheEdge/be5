/* Generated By:JJTree: Do not edit this line. AstJoinType.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;

public class AstJoinType extends SimpleNode
{
    public AstJoinType(int id)
    {
        super( id );
        this.nodeContent = "JOIN";
        this.nodePrefix = type.name();
    }
    
    public AstJoinType(JoinType type)
    {
        this(SqlParserTreeConstants.JJTJOINTYPE);
        setType( type );
    }

    private JoinType type = JoinType.INNER;

    public JoinType getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        setType(JoinType.valueOf( type ));
    }

    public void setType(JoinType type)
    {
        this.type = type;
        this.nodePrefix = type.name();
    }
}
/* JavaCC - OriginalChecksum=e6c432b03a2428b0c49ff092be6ed2cf (do not edit this line) */
