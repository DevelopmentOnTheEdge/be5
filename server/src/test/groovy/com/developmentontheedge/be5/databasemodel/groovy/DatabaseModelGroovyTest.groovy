package com.developmentontheedge.be5.databasemodel.groovy

import com.developmentontheedge.be5.api.services.SqlService
import com.developmentontheedge.be5.databasemodel.EntityModel
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class DatabaseModelGroovyTest extends Be5ProjectDBTest
{
    @Inject private DatabaseModel database
    @Inject private SqlService db

    @Before
    void before()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
        db.update("DELETE FROM testtableAdmin")
    }

    @After
    void after()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    void test()
    {
        assertEquals(null, db.getLong("SELECT id FROM testtableAdmin WHERE name = ?", "TestName"));

        def id = database.testtableAdmin << [
                "name": "TestName",
                "value": "1"];

        assertEquals(Long.parseLong(id), db.getLong("SELECT id FROM testtableAdmin WHERE name = ?", "TestName"));

        database.testtableAdmin[id] << [
                "value": "2"
        ]
        assertEquals(2, db.getInteger("SELECT value FROM testtableAdmin WHERE name = ?", "TestName"));

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

        assertEquals "testtableAdmin", testtableAdmin.tableName
        assertTrue testtableAdmin.empty
    }

//    @Test
//    @Ignore
//    void testDeleteAll()
//    {
//        def entityName = (EntityModel)database.testtableAdmin
//
//        entityName << [ "name": "TestName", "value": 1]
//        entityName << [ "name": "TestName", "value": 2]
//        entityName << [ "name": "TestName2", "value": 2]
//
//        entityName.setMany(["name": "TestName"], ["name": "TestName"])
//        //entityName.remove(["name": "TestName"])
//
//        assertEquals 0, db.getLong("SELECT count(*) FROM testtableAdmin WHERE name = ?", "TestName")
//        assertEquals 1, db.getLong("SELECT count(*) FROM testtableAdmin WHERE name = ?", "TestName2")
//
//
//        assertEquals 0, database.testtableAdmin.count(["name": "TestName"])
//        assertEquals 1, database.testtableAdmin.count(["name": "TestName2"])
//    }

    @Test
    void testDeleteIn()
    {
        def entityName = database.testtableAdmin

        def id = entityName << [
                "name": "TestName2",
                "value": 1]

        assert database.testtableAdmin[ id ] != null

        assertFalse entityName.empty
        assertEquals 1, entityName.remove( id )

        assert database.testtableAdmin[ id ] == null
        assertTrue entityName.empty

        assertEquals 0, entityName.remove( id )
    }

    @Test
    void testDelete()
    {
        EntityModel entityName = database.testtableAdmin

        def id = entityName << [ "name": "TestName", "value": 1]
        def id2 = entityName << [ "name": "TestName2", "value": 1]

        assertFalse entityName.empty
        assertEquals 1, entityName.remove( ["name": "TestName2"] )
        assertNotNull database.testtableAdmin[ id ]
        assertNull database.testtableAdmin[ id2 ]
    }

    @Test
    void testDeleteSeveralId()
    {
        def entityName = database.testtableAdmin

        def id = entityName << [ "name": "TestName1", "value": 1]
        def id2 = entityName << [ "name": "TestName2", "value": 1]
        def id3 = entityName << [ "name": "TestName3", "value": 1]

        assertEquals 2, entityName.remove( id, id2 )

        assert database.testtableAdmin[ id ] == null
        assert database.testtableAdmin[ id2 ] == null
        assert database.testtableAdmin[ id3 ] != null
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

        assertEquals "TestName2", record.$name
        assertEquals "TestName2", database.testtableAdmin[id].$name
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
//        EntityModel tableName = database.getEntity( "persons" );
//
//        tableName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"]
//
//        tableName << [
//                "firstname": "Bjarne",
//                "lastname" : "Stroustrup",
//                "birthday" : "30.12.1950",
//                "sex"      : "male"]
//
//        List<RecordModel> list = tableName.list;
//
//        assertTrue( listContains( list, "firstname", "Wirth" ) )
//        assertTrue( listContains( list, "firstname", "Bjarne" ) )
//        assertFalse( listContains( list, "firstname", "Rocky" ) )
//
//        list = tableName.list( sex: "male" )
//        assertTrue( listContains( list, "firstname", "Bjarne" ) )
//
//        list = tableName.list( sex: "female" )
//        assertTrue list.empty
//    }
//
//    public void testGetArray()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel tableName = database.getEntity( "persons" );
//
//        tableName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"]
//
//        tableName << [
//                "firstname": "Bjarne",
//                "lastname" : "Stroustrup",
//                "birthday" : "30.12.1950",
//                "sex"      : "male"]
//
//        RecordModel[] records = tableName.array
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
//        EntityModel tableName = database.persons;
//
//        tableName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"]
//
//        tableName << [
//                "firstname": "Bjarne",
//                "lastname" : "Stroustrup",
//                "birthday" : "30.12.1950",
//                "sex"      : "male"]
//
//
//        List<DynamicPropertySet> list = tableName.<DynamicPropertySet> collect( [:], { bean, row -> bean } );
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
//        EntityModel tableName = database.persons;
//
//        tableName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"]
//
//        def id = tableName << [
//                "firstname": "Bjarne",
//                "lastname" : "Stroustrup",
//                "birthday" : "30.12.1950",
//                "sex"      : "male"]
//
//        RecordModel rec = tableName[ id ];
//
//        rec.sex = "female";
//        assertEquals "female", tableName[ id ].$sex
//
//        rec.update( [ sex : "male", birthday : "01.02.1995" ] );
//        assertEquals "male", tableName[ id ].$sex;
//        assertEquals "1995-02-01", String.valueOf( tableName[ id ].$birthday );
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
//        def tableName = database.operations;
//        boolean oneTimeTrigger = false;
//        def list = tableName.query( DatabaseConstants.ALL_RECORDS_VIEW, [ table_name : 'operations', Name : 'Clone' ] ).collect();
//        assert list.size() == 1;
//        assert 'Java', list.$Type;
//    }
//
//    public void testQueryCollectWithClosure()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def tableName = database.operations;
//        boolean oneTimeTrigger = false;
//        def list = tableName.query( DatabaseConstants.ALL_RECORDS_VIEW, [ table_name : 'operations' ] ).collect { dps, rn ->
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
//        def tableName = database.operations;
//        boolean oneTimeTrigger = false;
//        def list = tableName.query( DatabaseConstants.ALL_RECORDS_VIEW, [ table_name : 'operations' ] ).collect { dps, rn ->
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
//        def tableName = database.operations;
//        boolean oneTimeTrigger = false;
//        tableName.query( DatabaseConstants.ALL_RECORDS_VIEW, [ table_name : 'operations', Name : 'Clone' ] ).each { dps, rn ->
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
//        def tableName = database.operations;
//        boolean oneTimeTrigger = false;
//        tableName.query( DatabaseConstants.ALL_RECORDS_VIEW, [ table_name : 'operations', Name : 'Clone' ] ) { dps, rn ->
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
//        EntityModel tableName = database.entities;
//        def rec = tableName( name : 'operations' );
//        def origin = rec.$origin;
//        try
//        {
//            println tableName.runOperation( "Edit", { records = [ rec.$name ]; parameters.origin = "test" } )
//            assertEquals 'test', tableName[ rec.$name ].$origin;
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
