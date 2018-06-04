/* Generated By:JJTree: Do not edit this line. AstSqlVar.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;

import one.util.streamex.StreamEx;

import java.util.Set;

public class AstBeSqlVar extends AstBeNode
{
    private static final Set<String> ALLOWED_PARAMETERS = StreamEx.of( "refColumn", "safestr", "default", "type", "prefix" ).toSet();
    
    public AstBeSqlVar(int id)
    {
        super( id );
        this.tagName = "var";
        this.allowedParameters = ALLOWED_PARAMETERS;
    }

    public AstBeSqlVar(String name){
        this(SqlParserTreeConstants.JJTBESQLVAR);
        setName(name);
    }

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getParametersString()
    {
        return ":"+getName()+super.getParametersString();
    }
    
    public String getDefault()
    {
        return getParameter( "default" );
    }
}
/* JavaCC - OriginalChecksum=0c52b5c1679c73b9b6d22f6c19dbcb6a (do not edit this line) */
