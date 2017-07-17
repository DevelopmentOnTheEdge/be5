package com.developmentontheedge.be5.databasemodel.groovy

import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class DatabaseModelGroovyTest extends AbstractProjectIntegrationH2Test
{
    DatabaseModel database = injector.get(DatabaseModel.class);
    def db = injector.getSqlService();

    @BeforeClass
    static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER)
    }

    @AfterClass
    static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST)
    }

    @Before
    void before(){
        db.update("DELETE FROM testtableAdmin")
    }

    @Test
    void test()
    {
        assertEquals(null, db.getLong("SELECT id FROM testtableAdmin WHERE name = ?", "TestName"));

        Long id = database.testtableAdmin << [
                "name": "TestName",
                "value": "1"];

        assertEquals(id, db.getLong("SELECT id FROM testtableAdmin WHERE name = ?", "TestName"));

        def testtableAdmin = database.testtableAdmin;

        assert db.getLong("SELECT id FROM testtableAdmin WHERE name = ?", "TestName") != null
        assert testtableAdmin( ["name": "TestName"] ) != null
    }

    @Test
    void testGroovyCount()
    {
        def testtableAdmin = database.testtableAdmin;

        assertEquals 0, testtableAdmin.size()

        testtableAdmin << [
                "name": "TestName",
                "value": "1"];

        assertEquals 1, testtableAdmin.size()
    }

    @Test
    void testInsert() throws Exception
    {
        database.testtableAdmin << [
                "name": "InsertName",
                "value": "2"];

        assert db.getInteger("SELECT value FROM testtableAdmin WHERE name = ?", "InsertName") == 2
    }

    @Test(expected = NumberFormatException.class)
    void testInsertError() throws Exception
    {
        database.testtableAdmin << [
                "name": "InsertName",
                "value": "asd"];

        assert db.getInteger("SELECT value FROM testtableAdmin WHERE name = ?", "InsertName") == 2
    }

    @Test
    void testIsEmpty()
    {
        def testtableAdmin = database.testtableAdmin;

        assertTrue(testtableAdmin.empty)

        testtableAdmin << [
                "name": "TestName",
                "value": "1"];

        assertFalse(testtableAdmin.empty)

        testtableAdmin << [
                "name": "TestName2",
                "value": "2"];

        assertFalse(testtableAdmin.empty)
    }


    @Test
    void testIsEmptyWithConditions()
    {
        def entityName = database.testtableAdmin;

        assertTrue entityName.empty;

        entityName << [
                "name": "TestName2",
                "value": "1"];

        assertFalse entityName.empty;
        assertTrue entityName.contains( ["value": "1"] );
        assertFalse entityName.contains( ["value": "2"] );
    }

    @Test
    void testGetEntity()
    {
        def testtableAdmin = database.testtableAdmin;

        assertEquals "testtableAdmin", testtableAdmin.entityName
        assertTrue testtableAdmin.empty
    }

    @Test
    void testDelete()
    {
        def entityName = database.testtableAdmin;

        def id = entityName << [
                "name": "TestName2",
                "value": 1]

        assert database.testtableAdmin[ id ] != null

        assertFalse entityName.empty;
        assertEquals 1, entityName.remove( id )

        assert database.testtableAdmin[ id ] == null
        assertTrue entityName.empty;

        assertEquals 0, entityName.remove( id )
    }

    @Test
    void testUpdate()
    {
        def entityName = database.testtableAdmin;

        def id = entityName << [
                "name": "TestName",
                "value": 1]

        def record = database.testtableAdmin[id]

        record << [
                "name": "TestName2",
        ]

        assertEquals "TestName2", record.name

        //TODO update
        assertEquals "TestName2", database.testtableAdmin[id].name
    }

    @Test
    void testFindRecord()
    {
        def entityName = database.testtableAdmin;

        entityName << [
            "name": "TestName2",
            "value": "123"]

        def rec = database.testtableAdmin( ["name": "TestName2"] );
        assertEquals( 123, rec.$value );
    }


    @Test
    void testGetRecord()
    {
        def entityName = database.testtableAdmin;

        def id = entityName << [
                "name": "TestName2",
                "value": "123"]

        def record = entityName[ id ];
        assertEquals( "TestName2", record.$name );
    }
//
//    private boolean listContains( List<DynamicPropertySet> recs, String propertyName, String value )
//    {
//        for( DynamicPropertySet rec : recs )
//        {
//            if( rec.getValueAsString( propertyName ).equals( value ) )
//            {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void testGetList()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entityName = database.getEntity( "persons" );
//
//        entityName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"]
//
//        entityName << [
//                "firstname": "Bjarne",
//                "lastname" : "Stroustrup",
//                "birthday" : "30.12.1950",
//                "sex"      : "male"]
//
//        List<RecordModel> list = entityName.list;
//
//        assertTrue( listContains( list, "firstname", "Wirth" ) )
//        assertTrue( listContains( list, "firstname", "Bjarne" ) )
//        assertFalse( listContains( list, "firstname", "Rocky" ) )
//
//        list = entityName.list( sex: "male" )
//        assertTrue( listContains( list, "firstname", "Bjarne" ) )
//
//        list = entityName.list( sex: "female" )
//        assertTrue list.empty
//    }
//
//    public void testGetArray()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entityName = database.getEntity( "persons" );
//
//        entityName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"]
//
//        entityName << [
//                "firstname": "Bjarne",
//                "lastname" : "Stroustrup",
//                "birthday" : "30.12.1950",
//                "sex"      : "male"]
//
//        RecordModel[] records = entityName.array
//        List<RecordModel> list = records as List;
//
//        assertTrue listContains( list, "firstname", "Wirth" )
//        assertTrue listContains( list, "firstname", "Bjarne" )
//    }
//
//    public void testCollect()
//    {
//        setUp();
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entityName = database.persons;
//
//        entityName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"]
//
//        entityName << [
//                "firstname": "Bjarne",
//                "lastname" : "Stroustrup",
//                "birthday" : "30.12.1950",
//                "sex"      : "male"]
//
//
//        List<DynamicPropertySet> list = entityName.<DynamicPropertySet> collect( [:], { bean, row -> bean } );
//
//        assertTrue( listContains( list, "firstname", "Wirth" ) );
//        assertTrue( listContains( list, "firstname", "Bjarne" ) );
//    }
//
//    public void testQueryIterator()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        database.persons << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male" ]
//        database.persons.query( 'All records', [:] ).iterator.withCloseable {
//            assertTrue it.hasNext()
//            assertEquals it.next().Name, "Niklaus Wirth Emil"
//        };
//    }
//
//    public void testQueryIteratorWithHandler()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        database.persons << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male" ]
//        database.persons.query( 'All records', [:] ).iterator.withCloseable { iterator ->
//            def it = Iterators.map( iterator, { it.$name } );
//            assertTrue it.hasNext()
//            assertEquals it.next(), "Niklaus Wirth Emil"
//        };
//    }
//
//    public void testSetRecordValue()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entityName = database.persons;
//
//        entityName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"]
//
//        def id = entityName << [
//                "firstname": "Bjarne",
//                "lastname" : "Stroustrup",
//                "birthday" : "30.12.1950",
//                "sex"      : "male"]
//
//        RecordModel rec = entityName[ id ];
//
//        rec.sex = "female";
//        assertEquals "female", entityName[ id ].$sex
//
//        rec.update( [ sex : "male", birthday : "01.02.1995" ] );
//        assertEquals "male", entityName[ id ].$sex;
//        assertEquals "1995-02-01", String.valueOf( entityName[ id ].$birthday );
//
//    }
//
//    public void testGetConnector()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        assertTrue( database.connector instanceof DatabaseConnector );
//    }
//
//    public void testQueryCollect()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def entityName = database.operations;
//        boolean oneTimeTrigger = false;
//        def list = entityName.query( DatabaseConstants.ALL_RECORDS_VIEW, [ table_name : 'operations', Name : 'Clone' ] ).collect();
//        assert list.size() == 1;
//        assert 'Java', list.$Type;
//    }
//
//    public void testQueryCollectWithClosure()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def entityName = database.operations;
//        boolean oneTimeTrigger = false;
//        def list = entityName.query( DatabaseConstants.ALL_RECORDS_VIEW, [ table_name : 'operations' ] ).collect { dps, rn ->
//            dps.Name == 'Clone'
//        }
//        assert list.size() == 1;
//        assert 'Java', list.$Type;
//    }
//
//    public void testQueryEachWithClosure()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def entityName = database.operations;
//        boolean oneTimeTrigger = false;
//        def list = entityName.query( DatabaseConstants.ALL_RECORDS_VIEW, [ table_name : 'operations' ] ).collect { dps, rn ->
//            dps.Name == 'Clone'
//        }
//        assert list.size() == 1;
//        assert 'Java', list.$Type;
//    }
//
//    public void testQueryEach()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def entityName = database.operations;
//        boolean oneTimeTrigger = false;
//        entityName.query( DatabaseConstants.ALL_RECORDS_VIEW, [ table_name : 'operations', Name : 'Clone' ] ).each { dps, rn ->
//            assertFalse oneTimeTrigger;
//            assertEquals 'Java', dps.$Type;
//            oneTimeTrigger = true;
//        };
//        assertTrue oneTimeTrigger
//    }
//
//    public void testQuery()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def entityName = database.operations;
//        boolean oneTimeTrigger = false;
//        entityName.query( DatabaseConstants.ALL_RECORDS_VIEW, [ table_name : 'operations', Name : 'Clone' ] ) { dps, rn ->
//            assertFalse oneTimeTrigger;
//            assertEquals 'Java', dps.$Type;
//            oneTimeTrigger = true;
//        };
//        assertTrue oneTimeTrigger
//    }
//
//    public void testOperationGetParameters()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def params = database.entities.getOperation( "Insert" ).parameters
//        assertNotNull( "${params.nameIterator().collect()}", params._name );
//        assertNotNull( "${params.nameIterator().collect()}", params._type );
//    }
//
//    def parameters;
//    public void testOperation()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entityName = database.entities;
//        def rec = entityName( name : 'operations' );
//        def origin = rec.$origin;
//        try
//        {
//            println entityName.runOperation( "Edit", { records = [ rec.$name ]; parameters.origin = "test" } )
//            assertEquals 'test', entityName[ rec.$name ].$origin;
//        }
//        finally
//        {
//            rec << [ origin : rec.$origin ];
//        }
//    }
//
//    public void testGroovyOperationExtender()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        assert database.operations( table_name : 'operationExtension', name : 'Edit' ) != null
//        def opID = database.operationExtension << [
//            table_name : 'operationExtension',
//            operation_name : 'Edit',
//            class_name : 'com.developmentontheedge.enterprise.operations.databasemodel.groovy.GroovyOperationExtenderSupport',
//            jscode : '''
//                import com.developmentontheedge.enterprise.DatabaseConnector;
//                import com.developmentontheedge.enterprise.Operation;
//                import com.developmentontheedge.enterprise.OperationExtenderSupport;
//                import com.developmentontheedge.enterprise.output.MessageHandler;
//
//                public class Test extends OperationExtenderSupport
//                {
//                    public void postInvoke( MessageHandler output, Operation op, DatabaseConnector connector )
//                    {
//                        database.operationExtension[ op.recordIDs[0] ].remove();
//                    }
//                }
//
//            ''',
//            module_name : 'beanexplorer'
//        ];
//        try
//        {
//            assert database.operationExtension[ opID ] != null;
//            def op = database.operationExtension.getOperation( 'Edit' )
//            op.records = [ opID ];
//            op.invoke();
//            assert database.operationExtension[ opID ] == null;
//        }
//        finally
//        {
//            database.operationExtension.remove( opID );
//        }
//    }
}
