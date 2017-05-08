package com.developmentontheedge.sql;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.developmentontheedge.sql.format.BasicQueryContext;
import com.developmentontheedge.sql.format.BasicQueryContext.QueryResolver;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.model.AstBeSqlSubQuery;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;

public class SubQueryTest
{
    @Test
    public void testSubQueryResolver()
    {
        AstStart start = SqlQuery.parse( "SELECT ID, '<sql limit=\"2\" queryName=\"test\"></sql>' FROM table" );
        QueryResolver resolver = (entity, query) -> {
            assertNull(entity);
            assertEquals("test", query);
            return "SELECT * FROM subTable WHERE tableID=<var:ID/>";
        };
        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder().queryResolver( resolver ).build() );
        contextApplier.applyContext( start );
        String key = contextApplier.subQueryKeys().findFirst().get();
        Map<String, String> vars = Collections.singletonMap( "ID", "5" );
        AstBeSqlSubQuery subQuery = contextApplier.applyVars( key, vars::get );
        assertEquals("SELECT * FROM subTable WHERE tableID ='5' LIMIT 2", subQuery.getQuery().format());
    }

    @Test
    public void testSubQueryResultParse() {
        String sql= "SELECT\n" +
                "      t.name AS \"___Name\",\n" +
                "      '<sql>SubQuery#1</sql>' AS \"testtUserValues\"\n" +
                "    FROM\n" +
                "      testtable t ORDER BY 2 LIMIT 2147483647";
        AstStart start = SqlQuery.parse(sql);
        //assertEquals(sql, start.format());
    }

    @Test
    public void testSubQueryResolverCheckExpression()
    {
        AstStart start = SqlQuery.parse( "SELECT ID, '<sql limit=\"2\" queryName=\"test\"></sql>' FROM table" );
        QueryResolver resolver = (entity, query) -> {
            assertNull(entity);
            assertEquals("test", query);
            return "SELECT * FROM subTable WHERE tableID=<var:ID/>" +
                    "<if parameter=\"idList\">\n" +
                    "   AND us.ID IN <parameter:idList multiple=\"true\" refColumn=\"utilitySuppliers.ID\" />\n" +
                    "</if>";
        };
        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder().queryResolver( resolver ).build() );
        contextApplier.applyContext( start );
        String key = contextApplier.subQueryKeys().findFirst().get();
        Map<String, String> vars = Collections.singletonMap( "ID", "5" );
        AstBeSqlSubQuery subQuery = contextApplier.applyVars( key, vars::get );
        assertEquals("SELECT * FROM subTable WHERE tableID ='5' LIMIT 2", subQuery.getQuery().format());
    }

    @Test
    public void testSubQueryResolverExecInclude()
    {
        AstStart start = SqlQuery.parse( "SELECT * FROM table t\n" +
                "            LEFT JOIN (<sql exec=\"include\" entity=\"meters\" queryName=\"*** Selection view ***\"></sql>) m\n" +
                "                ON m.ID = t.meterID" );
        QueryResolver resolver = (entity, query) -> {
            assertEquals("meters", entity);
            assertEquals("*** Selection view ***", query);
            return "SELECT m.ID FROM public.meters m";
        };
        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder().queryResolver( resolver ).build() );
        contextApplier.applyContext( start );
        assertEquals("SELECT * FROM table t\n" +
                "            LEFT JOIN (SELECT m.ID FROM public.meters m) m\n" +
                "                ON m.ID = t.meterID", start.format());
    }

    @Test
    public void testApplyWithVarsExecDelayedAddFilter()
    {
        //TODO us.ID vs ID in be3 (us from entity)
        AstStart start = SqlQuery.parse(
        "SELECT '<sql exec=\"delayed\" filterKey=\"us.ID\" filterValProperty=\"___usID\" limit=\"1\" " +
                "entity=\"utilitySuppliers\" queryName=\"*** Selection view ***\" outColumns=\"Name\"></sql>' AS \"Услуга\",\n" +
                "ID AS \"___usID\" FROM table" );
        QueryResolver resolver = (entity, query) -> {
            assertEquals("utilitySuppliers", entity);
            assertEquals("*** Selection view ***", query);
            return "SELECT us.ID AS \"CODE\",us.utilityType AS \"Name\" FROM utilitySuppliers us";
        };
        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder().queryResolver( resolver ).build() );
        contextApplier.applyContext( start );

        String key = contextApplier.subQueryKeys().findFirst().get();
        Map<String, String> vars = Collections.singletonMap( "___usID", "5" );
        AstBeSqlSubQuery subQuery = contextApplier.applyVars( key, vars::get );

        assertEquals("SELECT '"+key+"' AS \"Услуга\",\n" +
                "ID AS \"___usID\" FROM table", start.format());

        assertEquals("SELECT us.utilityType AS \"Name\" " +
                "FROM utilitySuppliers us WHERE us.ID ='5' LIMIT 1", subQuery.getQuery().format());

    }

    
    @Test
    public void testApplyWithVars()
    {
        AstStart start = SqlQuery.parse( "SELECT ID, '<sql limit=\"2\">SELECT COUNT(*) FROM subTable WHERE tableID=<var:ID/></sql> entries' FROM table" );
        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder().build() );
        contextApplier.applyContext( start );
        String key = contextApplier.subQueryKeys().findFirst().get();
        Map<String, String> vars = Collections.singletonMap( "ID", "5" );
        AstBeSqlSubQuery subQuery = contextApplier.applyVars( key, vars::get );
        assertEquals("SELECT COUNT(*) FROM subTable WHERE tableID ='5' LIMIT 2", subQuery.getQuery().format());
        assertEquals("SELECT ID, '"+key+" entries' FROM table", start.format());
    }
    
    @Test
    public void testSeveralSubQueries()
    {
        AstStart start = SqlQuery.parse( "SELECT ID, '<sql limit=\"2\">SELECT * FROM subTable WHERE tableID=<var:ID/></sql>' FROM table" );
        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder().build() );
        contextApplier.applyContext( start );
        String key = contextApplier.subQueryKeys().findFirst().get();
        Map<String, String> vars = Collections.singletonMap( "ID", "5" );
        AstBeSqlSubQuery subQuery = contextApplier.applyVars( key, vars::get );
        assertEquals("SELECT * FROM subTable WHERE tableID ='5' LIMIT 2", subQuery.getQuery().format());
    }
    
    @Test
    public void testVarsInFieldReference()
    {
        AstStart start = SqlQuery.parse( "SELECT ID, '<sql limit=\"2\">SELECT field.<var:reference/> FROM table.<var:name/></sql>' FROM table" );
        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder().build() );
        contextApplier.applyContext( start );
        String key = contextApplier.subQueryKeys().findFirst().get();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put( "reference", "ref" );
        vars.put( "name", "name" );
        AstBeSqlSubQuery subQuery = contextApplier.applyVars( key, vars::get );
        assertEquals("SELECT field.'ref' FROM table.'name' LIMIT 2", subQuery.getQuery().format());
    }
}
