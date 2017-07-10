package com.developmentontheedge.sql.format;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.developmentontheedge.sql.model.AstBeSqlVar;
import one.util.streamex.EntryStream;

import com.developmentontheedge.sql.model.AstBooleanExpression;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstFunNode;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstJoinSpecification;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstStringConstant;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.AstWhere;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SimpleNode;

public class FilterApplier
{
    public void setFilter(AstStart ast, Map<ColumnRef, String> conditions)
    {
        AstQuery query = ast.getQuery();
        dropOldConditions( query );
        if( conditions.size() == 0 )
            return;
        AstWhere where = new AstWhere();
        addWhere(where, conditions);
        if(query.jjtGetNumChildren() == 1)
            ( (AstSelect)query.child( 0 ) ).where( where );
        else
        {
            AstTableRef tableRef = new AstTableRef( new AstParenthesis( query.clone() ), new AstIdentifierConstant( "tmp" ) );
            AstSelect select = new AstSelect( new AstSelectList(), new AstFrom( tableRef ) );
            select.where( where );
            query.replaceWith( new AstQuery( select ) );
        }
    }

    public void addFilter(AstStart ast, Map<ColumnRef, String> conditions){
        addFilter(ast.getQuery(),conditions);
    }

    public void addFilter(AstQuery query, Map<ColumnRef, String> conditions)
    {
        if( conditions.size() == 0 )
            return;
        AstWhere where = new AstWhere();
        if(query.jjtGetNumChildren() == 1)
        {
            AstSelect select = (AstSelect)query.child( 0 );
            if( select.getWhere() != null )
                where = select.getWhere();
            else
                select.where( where );
        }
        else
        {
            AstTableRef tableRef = new AstTableRef( new AstParenthesis( query.clone() ), new AstIdentifierConstant( "tmp" ) );
            AstSelect select = new AstSelect( new AstSelectList(), new AstFrom( tableRef ) );
            select.where( where );
            query.replaceWith( new AstQuery( select ) );
        }
        addWhere(where, conditions);
    }
    
    private void dropOldConditions(AstQuery query)
    {
        query.children().select( AstSelect.class ).forEach( s -> {
            for( AstJoinSpecification js : s.getFrom().tree().select( AstJoinSpecification.class ) )
                js.remove();
            if( s.getWhere() != null )
                s.getWhere().remove();
            } );
    }
    
    private void addWhere(AstWhere where, Map<ColumnRef, String> conditions)
    {
        if( where.jjtGetNumChildren() != 0 )
        {
            if( !AstFunNode.isFunction( DefaultParserContext.AND_LIT ).test( where.child( 0 ) ) )
            {
                AstFunNode and = DefaultParserContext.FUNC_AND.node();
                for( SimpleNode child : where.children() )
                    and.addChild( child instanceof AstBooleanExpression ? new AstParenthesis(child) : child );
                where.removeChildren();
                where.addChild( and );
            }
            setConditions( where.child( 0 ), conditions );
        }
        else if( conditions.size() > 1 )
        {
            where.addChild( DefaultParserContext.FUNC_AND.node() );
            setConditions( where.child( 0 ), conditions );
        }
        else
            setConditions( where, conditions );
    }
    
    public void setConditions(SimpleNode where, Map<ColumnRef, String> conditions)
    {
        EntryStream.of(conditions).mapKeys( ColumnRef::asNode ).mapValues( this::toNode)
            .mapKeyValue( DefaultParserContext.FUNC_EQ::node ).forEach( where::addChild );
    }

    private static final Pattern BeSqlVar_PATTERN = Pattern.compile("<var:(.*)[ /]");

    private SimpleNode toNode(String value)
    {
        if( value.matches( "[-+]?\\d*\\.?\\d+" ) )
            return AstNumericConstant.of( value.contains( "." ) ? (Number)Double.valueOf( value ) : (Number)Integer.valueOf( value ) );

        Matcher matcher = BeSqlVar_PATTERN.matcher(value);
        if(matcher.find()){
            return new AstBeSqlVar(matcher.group(1));
        }
        return new AstStringConstant( value );
    }
}
