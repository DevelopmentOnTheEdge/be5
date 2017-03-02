package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.ParserContext;

/**
 * Interface to process DB specific issues. 
 * 
 * For this purpose DB specific transformers modifies AST.
 */
public interface DbmsTransformer
{
    public void transformAst(AstStart start);
    public void transformQuery(AstQuery start);

    public ParserContext getParserContext();
    public void setParserContext(ParserContext parserContext);

}
