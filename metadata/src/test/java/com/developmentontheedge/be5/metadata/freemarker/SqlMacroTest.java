package com.developmentontheedge.be5.metadata.freemarker;

import static org.junit.Assert.*;

import org.junit.Test;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.Rdbms;


public class SqlMacroTest
{
    @Test
    public void testMacro() throws ProjectElementException
    {
        Project project = new Project("test");
        project.setDatabaseSystem( Rdbms.SQLSERVER );
        FreemarkerScript script = new FreemarkerScript("script", project.getApplication().getFreemarkerScripts());
        DataElementUtils.save( script );
        script.setSource( "SELECT ${concat('a'?asDate, 'b', 'c'?str)} FROM test" );
        assertEquals("SELECT ( CONVERT( DATE, a, 120 ) + b + 'c' ) FROM test", project.mergeTemplate( script ).validate());
        script.setSource( "<#macro _sql>${project.enterSQL()}<#assign nested><#nested></#assign>${project.translateSQL(nested)}</#macro>" +
                "<@_sql>SELECT TO_DATE(a) || b || 'c' FROM test</@>" );
        assertEquals("SELECT CONVERT(DATE, a, 120)+ b + 'c' FROM test", project.mergeTemplate( script ).validate());
        script.setSource( "<#macro _sql>${project.enterSQL()}<#assign nested><#nested></#assign>${project.translateSQL(nested)}</#macro>" +
                "<@_sql>SELECT ${'a'?asDate} || b || 'c' FROM test</@>" );
        assertEquals("SELECT CONVERT(DATE, a, 120)+ b + 'c' FROM test", project.mergeTemplate( script ).validate());
    }

    @Test
    public void testMacroPostgres() throws ProjectElementException
    {
        Project project = new Project("test");
        project.setDatabaseSystem( Rdbms.POSTGRESQL );
        FreemarkerScript script = new FreemarkerScript("script", project.getApplication().getFreemarkerScripts());
        DataElementUtils.save( script );
        script.setSource( "SELECT ${concat('a'?asDate, 'b', 'c'?str)} FROM test" );
        assertEquals("SELECT ( TO_DATE(a,'YYYY-MM-DD') || b || 'c' ) FROM test", project.mergeTemplate( script ).validate());

        script.setSource( "SELECT ${'a'?asDate} || b || 'c' FROM test" );
        assertEquals("SELECT TO_DATE(a,'YYYY-MM-DD') || b || 'c' FROM test", project.mergeTemplate( script ).validate());
//todo
//        script.setSource( "SELECT TO_DATE(a) || b || 'c' FROM test" );
//        assertEquals("SELECT TO_DATE(a,'YYYY-MM-DD') || b || 'c' FROM test", project.mergeTemplate( script ).validate());
    }


}
