package com.developmentontheedge.be5.operation.databasemodel;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


import junit.framework.TestCase;
import org.junit.Ignore;

@Ignore
public class EntityModelAddTest extends TestCase 
{

//    private final DatabaseConnector connector = TestDB.getDefaultConnector( "postgresql", "be_test" );
//
//    @Override
//    protected void tearDown() throws Exception
//    {
//        super.tearDown();
//        connector.releaseConnection( null );
//    }
//
//    @Override
//    protected void setUp() throws Exception
//    {
//        super.setUp();
//        TestDB.delete( connector, "persons" );
//        ReferencesQueriesCache.getInstance().clear();
//    }
//
//    public void testAdd() throws Exception
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//
//        EntityModel entity = database.getEntity( "persons" );
//
//        entity.add( Utils.valueMap(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        TestDB.checkTableData( connector, "persons", new String[][]{
//            { "firstName", "middleName", "lastName", "birthday" ,"sex" },
//            { "Wirth" ,"Emil", "Niklaus", "1934-02-15", "male" }
//        } );
//    }
//
//    public void testAddWithTcloneId() throws Exception
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entity = database.getEntity( "persons", "11" );
//        entity.dropClonedTable();
//        assertFalse( entity.isTableExists() );
//
//        entity.makeClonedTable( false );
//
//        try
//        {
//            assertTrue( entity.isTableExists() );
//
//            entity.add( Utils.valueMap(
//                "firstname", "Wirth",
//                "middlename", "Emil",
//                "lastname", "Niklaus",
//                "birthday", "15.02.1934",
//                "sex", "male" )
//            );
//
//            TestDB.checkTableData( connector, "persons11", new String[][]{
//                { "firstName", "middleName", "lastName", "birthday" ,"sex" },
//                { "Wirth" ,"Emil", "Niklaus", "1934-02-15", "male" }
//            } );
//        }
//        finally
//        {
//            entity.dropClonedTable();
//        }
//
//        assertFalse( entity.isTableExists() );
//    }
//
//    public void testAddAll() throws Exception
//    {
//        DatabaseModel databaseModel = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//
//        EntityModel entityModel = databaseModel.getEntity( "persons");
//
//        java.util.List<Map<String, String>> listPersons = Arrays.<Map<String, String>>asList(
//            Maps.hashMap(
//                "firstname", "Wirth",
//                "middlename", "Emil",
//                "lastname", "Niklaus",
//                "birthday", "15.02.1934",
//                "sex", "male"
//            ),
//            Maps.hashMap(
//                "firstname", "Abakan",
//                "middlename", "Djigurda",
//                "lastname", "Adarbekovich",
//                "birthday", "15.06.2007",
//                "sex", "male"
//            ),
//            Maps.hashMap(
//                "firstname", "Abakan",
//                "middlename", "Djigurda",
//                "lastname", "Adarbekovich",
//                "birthday", "15.06.2007",
//                "sex", "male"
//            )
//        );
//
//        entityModel.addAll( listPersons );
//
//        TestDB.checkTableData( connector, "persons", new String[][]{
//                { "firstName", "middleName", "lastName", "birthday" ,"sex" },
//                { "Wirth" ,"Emil", "Niklaus", "1934-02-15", "male" },
//                { "Abakan", "Djigurda", "Adarbekovich", "2007-06-15", "male" },
//                { "Abakan", "Djigurda", "Adarbekovich", "2007-06-15", "male" }
//        } );
//    }

}
