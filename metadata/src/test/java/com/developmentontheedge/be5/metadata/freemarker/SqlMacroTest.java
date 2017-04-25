package com.developmentontheedge.be5.metadata.freemarker;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.Rdbms;

public class SqlMacroTest
{
    @Test
    @Ignore
    public void testMacro() throws ProjectElementException
    {
        Project project = new Project("test");
        project.setDatabaseSystem( Rdbms.SQLSERVER );
        FreemarkerScript script = new FreemarkerScript("script", project.getApplication().getFreemarkerScripts());
        DataElementUtils.save( script );
        script.setSource( "<@_sql>SELECT ${concat('a'?asDate, 'b', 'c'?str)} FROM test</@>" );
        assertEquals("SELECT ( CONVERT( DATE, a, 120 ) + b + 'c' ) FROM test", project.mergeTemplate( script ).validate());
        script.setSource( "<@_sql>SELECT TO_DATE(a) || b || 'c' FROM test</@>" );
        assertEquals("SELECT CONVERT(DATE, a, 120)+ b + 'c' FROM test", project.mergeTemplate( script ).validate());
        script.setSource( "<@_sql>SELECT ${'a'?asDate} || b || 'c' FROM test</@>" );
        assertEquals("SELECT CONVERT(DATE, a, 120)+ b + 'c' FROM test", project.mergeTemplate( script ).validate());
    }
    
}
