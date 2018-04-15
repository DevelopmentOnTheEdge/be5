package com.developmentontheedge.be5.metadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.freemarker.FreemarkerUtils;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.FreemarkerCatalog;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.sql.Rdbms;

import junit.framework.TestCase;
import freemarker.template.Configuration;


public class FreemarkerTest extends TestCase
{
    public void testBasics() throws ProjectElementException
    {
        Project project = new Project( "test" );
        project.setRoles( Arrays.asList( "Admin", "DbAdmin","Guest" ) );
        project.setDatabaseSystem( Rdbms.MYSQL );
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put( "project", project );
        
        Configuration configuration = FreemarkerUtils.getConfiguration( project );

        assertEquals("qqq\nwww\nqqq\nwww\nqqq\nwww\n", FreemarkerUtils.mergeTemplate("line breaks test", "<#macro a>\r\nqqq\r\nwww\n</#macro><@a/>\r\n<@a/>\n<@a/>", null, configuration));

        assertEquals( "CONCAT( COALESCE( CAST(a AS CHAR),'b' ),'c','Admin,DbAdmin,Guest' )", FreemarkerUtils.mergeTemplate( "testTemplate",
                "<#macro test>${concat(coalesce('a'?asVarchar,'b'?str),'c'?str,'@AllRoles,DbAdmin'?roles?str)}</#macro><@test/>", dataModel, configuration ) );
        
        //$db.concat('u.emailAddress', $db.str(' ('), 'per.courtesy', $db.str(' '), 'per.firstName', $db.str(' '), 'per.lastName', $db.str(', #'), $db.castIntToVarchar('per.ID'), $db.str(')'))#end
        assertEquals("CONCAT( u.emailAddress,' (',per.courtesy,' ',per.firstName,' ',per.lastName,', #',CAST(per.ID AS CHAR),')' )", 
                FreemarkerUtils.mergeTemplate( "USER_INFO", 
                "${concat('u.emailAddress', ' ('?str, 'per.courtesy', ' '?str, 'per.firstName', ' '?str, 'per.lastName', ', #'?str, 'per.ID'?asVarchar, ')'?str)}", dataModel, configuration ));
        
        assertEquals("12345", FreemarkerUtils.mergeTemplate( "list test", "<#list 1..5 as a>${a}</#list>", null, configuration ));
        
        assertEquals(" ", FreemarkerUtils.mergeTemplate( "space test", " ", null, configuration ));
        
        assertEquals("CONCAT( COALESCE( nameSpace,'' ),COALESCE( name,'' ) )", FreemarkerUtils.mergeTemplate( "orEmpty test", "${concat('nameSpace'?orEmpty, 'name'?orEmpty)}", null, configuration ));
        
        assertEquals("bba", FreemarkerUtils.mergeTemplate("boolean param test", "<#macro test param=false><#if param>a<#else>b</#if></#macro><@test/><@test false/><@test true/>", null, configuration));
        
        //TODO assertEquals("CAST( mycolumn AS BIGINT )", FreemarkerUtils.mergeTemplate("asPK test", "${'mycolumn'?asPK}", null, configuration));
        
        assertEquals("SELECT `___whoModified` FROM myTable", FreemarkerUtils.mergeTemplate("quote test", "SELECT ${'___whoModified'?quote} FROM myTable", null, configuration));
        
        assertEquals("SELECT REPLACE('abc','b','d') FROM myTable", FreemarkerUtils.mergeTemplate("replace test", "SELECT ${replace('abc'?str,'b'?str,'d'?str)} FROM myTable", null, configuration));
        
//TODO        assertEquals("DELETE FROM javaScriptHandlers WHERE CODE = 'ccc';\n"+
//                "INSERT INTO javaScriptHandlers ( CODE, name, algorithmCode )\n"+
//                "VALUES ('ccc', 'www',\n"+
//                "'A\nB\nC\n'\n"+
//                ");\n", FreemarkerUtils.mergeTemplate( "jshandlerTest", "<@_jsHandler 'ccc' 'www'>A\nB\nC\n</@>", null, configuration));
    }
    
    public void testVariables()
    {
        Project project = new Project( "myProject" );
        project.setDatabaseSystem( Rdbms.POSTGRESQL );
        FreemarkerScript script = new FreemarkerScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY, project.getMacroCollection() );
        DataElementUtils.saveQuiet( script );
        script.setSource( "<#assign test = dbPlatform+'.ver'/>" );
        assertEquals("postgres", project.getVariableValue( "dbPlatform" ));
        assertEquals("postgres.ver", project.getVariableValue( "test" ));
        assertEquals("[currentDate, currentDateTime, dbPlatform, fromFakeTable, test]", project.getVariables().keySet().toString());
    }
    
    public void testDefinitions() throws ProjectElementException
    {
        Project project = new Project( "myProject" );
        project.setDatabaseSystem( Rdbms.POSTGRESQL );
        FreemarkerScript script = new FreemarkerScript( "test", project.getApplication().getFreemarkerScripts());
        DataElementUtils.saveQuiet( script );
        script.setSource( "ALTER TABLE test ADD COLUMN ${columnDef('myCol', {'type': 'BOOL', 'canBeNull': true})}" );
        assertEquals("ALTER TABLE test ADD COLUMN mycol VARCHAR(3) CHECK(mycol IN ('no', 'yes') )", project.mergeTemplate( script ).validate());
        script.setSource( "ALTER TABLE test ADD COLUMN ${columnDef('myCol', {'type': 'BOOL', 'canBeNull': false})}" );
        //TODO assertEquals("ALTER TABLE test ADD COLUMN mycol VARCHAR(3) NOT NULL CHECK(mycol IN ('no', 'yes') )", project.mergeTemplate( script ).validate());
        script.setSource( "${tableDef('uiSocialBlocks', {"
            + "'CODE': {'type': 'VARCHAR(2)', 'primaryKey': true},"
            + "'name': {'type': 'VARCHAR(250)'},"
            + "'parentCode': {'type': 'VARCHAR(2)', 'canBeNull': true}"
            + "})}" );
        assertEquals("DROP TABLE IF EXISTS uisocialblocks;\n"+
            "CREATE TABLE uisocialblocks (\n"+
            "code VARCHAR(2) NOT NULL PRIMARY KEY,\n"+
            "name VARCHAR(250) NOT NULL,\n"+
            "parentcode VARCHAR(2));", project.mergeTemplate( script ).validate());
        project.setDatabaseSystem( Rdbms.ORACLE );
        assertEquals("call drop_if_exists( 'uiSocialBlocks' );\n"+
            "CREATE TABLE UISOCIALBLOCKS (\n"+
            "CODE VARCHAR2(2 CHAR) NOT NULL PRIMARY KEY,\n"+
            "NAME VARCHAR2(250 CHAR) NOT NULL,\n"+
            "PARENTCODE VARCHAR2(2 CHAR));", project.mergeTemplate( script ).validate());
        script.setSource( "${tableDef('uiSocialBlocks', {"
                + "'CODE': {'type': 'VARCHAR(2)', 'primaryKey': true},"
                + "'name': {'type': 'VARCHAR(250)'},"
                + "'parentCode': {'type': 'VARCHAR(2)', 'canBeNull': true}"
                + "}, {"
                + "'idx_uisb_name': {'unique': 'true', 'columns': ['name', 'upper(parentCode)']}"
                + "})}" );
        assertEquals("call drop_if_exists( 'uiSocialBlocks' );\n"+
                "CREATE TABLE UISOCIALBLOCKS (\n"+
                "CODE VARCHAR2(2 CHAR) NOT NULL PRIMARY KEY,\n"+
                "NAME VARCHAR2(250 CHAR) NOT NULL,\n"+
                "PARENTCODE VARCHAR2(2 CHAR));\n"+
                "CREATE UNIQUE INDEX IDX_UISB_NAME ON UISOCIALBLOCKS(NAME, UPPER(PARENTCODE));", project.mergeTemplate( script ).validate());
    }
    
    public void testProject() throws ProjectElementException
    {
        Project project = new Project( "myProject" );
        project.setRoles( Arrays.asList( "Admin", "Guest" ) );
        project.setDatabaseSystem( Rdbms.MYSQL );
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put( "project", project );
        Entity entity = new Entity( "myTable", project.getApplication(), EntityType.TABLE );
        DataElementUtils.saveQuiet( entity );
        Query query = new Query( "query", entity );
        DataElementUtils.saveQuiet( query );
        query.setQuery( "SELECT ${concat('col1','col2'?asVarchar)} FROM ${entity.getName()}" );
        assertEquals("SELECT CONCAT( col1,CAST(col2 AS CHAR) ) FROM myTable", query.getQueryCompiled().validate());
        query.setQuery( "SELECT ${error} FROM myTable" );
        ProjectElementException error = query.getQueryCompiled().getError();
        assertNotNull(error);
        assertEquals(1, error.getRow());
        assertEquals(8, error.getColumn());
        assertEquals("myProject/application/Entities/myTable/Queries/query", error.getPath());
        
        Query query2 = new Query( "myQuery", entity );
        DataElementUtils.saveQuiet( query2 );
        query2.setMenuName( "My menu item" );
        query.setQuery( "${project.getEntity('myTable').getQueries().get('myQuery').getMenuName()}" );
        assertEquals("My menu item", query.getQueryCompiled().validate());
    }
    
    public void testPredefinedMacros() throws ProjectElementException
    {
        Project project = new Project( "project" );
        project.setRoles( Arrays.asList( "Admin", "Guest" ) );
        project.setDatabaseSystem( Rdbms.MYSQL );
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put( "project", project );
        Entity entity = new Entity( "myTable", project.getApplication(), EntityType.TABLE );
        DataElementUtils.saveQuiet( entity );
//TODO        Query query = new Query( "All records", entity );
//        DataElementUtils.saveQuiet( query );
//        query.setQuery( "SELECT * FROM ${entity.getName()}" );
//        Query query2 = new Query("Copy", entity);
//        DataElementUtils.saveQuiet( query2 );
//        query2.setQuery( "<@_copyAllRecordsQuery/>" );
//        assertEquals("SELECT * FROM myTable", query2.getQueryCompiled().validate());
//
//        query2.setQuery( "SELECT <@_bold>name</@_bold> FROM myTable" );
//        assertEquals("SELECT CONCAT( '<b>',name,'</b>' ) FROM myTable", query2.getQueryCompiled().validate());
//        query2.setQuery( "SELECT <@_bold><@_italic>name</@></@> FROM myTable" );
//        assertEquals("SELECT CONCAT( '<b>',CONCAT( '<i>',name,'</i>' ),'</b>' ) FROM myTable", query2.getQueryCompiled().validate());
    }
    
    public void testProjectMacros() throws ProjectElementException
    {
        Project project = new Project( "biostore" );
        project.setRoles( Arrays.asList( "Admin", "Guest" ) );
        project.setDatabaseSystem( Rdbms.MYSQL );
        FreemarkerScript script = new FreemarkerScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY, project.getMacroCollection() );
        DataElementUtils.saveQuiet( script );
        script.setSource( "<#macro USER_INFO>User: ${concat('u.emailAddress', ' ('?str, 'per.courtesy', ' '?str, 'per.firstName', ' '?str, 'per.lastName', ', #'?str, 'per.ID'?asVarchar, ')'?str)}</#macro>\n\n");
        Entity entity = new Entity( "myTable", project.getApplication(), EntityType.TABLE );
        DataElementUtils.saveQuiet( entity );
        Query query = new Query( "All records", entity );
        DataElementUtils.saveQuiet( query );
        query.setQuery( "SELECT <@USER_INFO/> FROM ${entity.getName()}" );
        assertEquals("SELECT User: CONCAT( u.emailAddress,' (',per.courtesy,' ',per.firstName,' ',per.lastName,', #',CAST(per.ID AS CHAR),')' ) FROM myTable", query.getQueryCompiled().validate());
        script.setSource( "<#macro USER_INFO>${concat('u.emailAddress', ' ('?str, 'per.courtesy', ' '?str, 'per.firstName', ' '?str, 'per.lastName', ', #'?str, 'per.ID'?asVarchar, ')'?str)}</#macro>" );
        assertEquals("SELECT CONCAT( u.emailAddress,' (',per.courtesy,' ',per.firstName,' ',per.lastName,', #',CAST(per.ID AS CHAR),')' ) FROM myTable", query.getQueryCompiled().validate());

        script.setSource( "<#macro test a b c>${a} - ${b} - ${c}</#macro>" );
        query.setQuery( "<@test 'q' 'w' 'e'/>" );
        assertEquals("q - w - e", query.getQueryCompiled().validate());
    }
    
    public void testProjectMacroDefValues() throws ProjectElementException
    {
        Project project = new Project( "biostore" );
        project.setRoles( Arrays.asList( "Admin", "Guest" ) );
        project.setDatabaseSystem( Rdbms.MYSQL );
        String source = "<#macro test a b='default'>a = ${a}, b = ${b}</#macro>";
        FreemarkerScript script = new FreemarkerScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY, project.getMacroCollection() );
        script.setSource( source );
        DataElementUtils.saveQuiet( script );
        assertEquals(source, project.getMacroCollection().optScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY ).getSource());
        Entity entity = new Entity( "myTable", project.getApplication(), EntityType.TABLE );
        DataElementUtils.saveQuiet( entity );
        Query query = new Query( "All records", entity );
        DataElementUtils.saveQuiet( query );
        query.setQuery( "<@test 'a'/>|<@test 'a' 'b'/>|<@test a='q'/>" );
        assertEquals("a = a, b = default|a = a, b = b|a = q, b = default", query.getQueryCompiled().validate());
    }
}
