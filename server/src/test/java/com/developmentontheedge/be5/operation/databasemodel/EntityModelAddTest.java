package com.developmentontheedge.be5.operation.databasemodel;

import java.util.Arrays;
import java.util.Map;


import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.google.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class EntityModelAddTest extends AbstractProjectTest
{
    private Injector sqlMockInjector = Be5.createInjector(new AbstractProjectTest.SqlMockBinder());
    private DatabaseModel database = sqlMockInjector.get(DatabaseModel.class);

    @BeforeClass
    public static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
//        TestDB.delete( connector, "persons" );
//        ReferencesQueriesCache.getInstance().clear();
    }

    @AfterClass
    public static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    @Ignore
    public void testAdd() throws Exception
    {
        EntityModel entity = database.getEntity( "persons" );

        entity.add( ImmutableMap.of(
            "firstname", "Wirth",
            "middlename", "Emil",
            "lastname", "Niklaus",
            "birthday", "15.02.1934",
            "sex", "male" )
        );

//        TestDB.checkTableData( connector, "persons", new String[][]{
//            { "firstName", "middleName", "lastName", "birthday" ,"sex" },
//            { "Wirth" ,"Emil", "Niklaus", "1934-02-15", "male" }
//        } );
    }

//    public void testAddWithTcloneId() throws Exception
//    {
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
//            entity.add( ImmutableMap.of(
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


    public void testAddAll() throws Exception
    {
        EntityModel entityModel = database.getEntity( "persons");

        java.util.List<Map<String, String>> listPersons = Arrays.<Map<String, String>>asList(
            ImmutableMap.of(
                "firstname", "Wirth",
                "middlename", "Emil",
                "lastname", "Niklaus",
                "birthday", "15.02.1934",
                "sex", "male"
            ),
            ImmutableMap.of(
                "firstname", "Abakan",
                "middlename", "Djigurda",
                "lastname", "Adarbekovich",
                "birthday", "15.06.2007",
                "sex", "male"
            ),
            ImmutableMap.of(
                "firstname", "Abakan",
                "middlename", "Djigurda",
                "lastname", "Adarbekovich",
                "birthday", "15.06.2007",
                "sex", "male"
            )
        );

        entityModel.addAll( listPersons );

//        TestDB.checkTableData( connector, "persons", new String[][]{
//                { "firstName", "middleName", "lastName", "birthday" ,"sex" },
//                { "Wirth" ,"Emil", "Niklaus", "1934-02-15", "male" },
//                { "Abakan", "Djigurda", "Adarbekovich", "2007-06-15", "male" },
//                { "Abakan", "Djigurda", "Adarbekovich", "2007-06-15", "male" }
//        } );
    }

}
