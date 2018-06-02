package com.developmentontheedge.be5.databasemodel.groovy

import com.developmentontheedge.be5.databasemodel.DatabaseModelProjectDbTest
import com.developmentontheedge.be5.databasemodel.EntityModel
import com.developmentontheedge.be5.databasemodel.RecordModel
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.test.BaseTestUtils
import com.developmentontheedge.beans.DynamicPropertySet
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue


class DatabaseModelGroovyTest extends DatabaseModelProjectDbTest
{
    EntityModel testtableAdmin

    @Before
    void before()
    {
        testtableAdmin = database.getEntity("testtableAdmin")
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER)
        db.update("DELETE FROM testtableAdmin")
    }

    @After
    void after()
    {
        initUserWithRoles(RoleType.ROLE_GUEST)
    }

    @Test
    void test()
    {
        assertEquals(null, db.oneLong("SELECT id FROM testtableAdmin WHERE name = ?", "TestName"))

        def id = testtableAdmin << [
                "name": "TestName",
                "value": 1]

        assertEquals(id, db.oneLong("SELECT id FROM testtableAdmin WHERE name = ?", "TestName"))

        testtableAdmin[id] << [
                "value": 2
        ]
        Assert.assertEquals(2, db.oneInteger("SELECT value FROM testtableAdmin WHERE name = ?", "TestName"))

        def testtableAdmin = testtableAdmin

        assert db.oneLong("SELECT id FROM testtableAdmin WHERE name = ?", "TestName") != null
        assert testtableAdmin( ["name": "TestName"] ) != null
    }

    @Test
    void testGroovyCount()
    {
        assertEquals 0, testtableAdmin.count()

        testtableAdmin << [
                "name": "TestName",
                "value": "1"]

        assertEquals 1, testtableAdmin.count()
    }

    @Test
    void testGetColumns()
    {
        testtableAdmin << [ "name": "TestName", "value": "1"]

        RecordModel rec = testtableAdmin.getColumnsBy(["value"],
                ["name": "TestName"]
        )

        assertEquals null, rec.getValue("name")
        assertEquals 1, rec.getValue("value")
    }

    @Test
    void testInsert()
    {
        testtableAdmin << [
                "name": "InsertName",
                "value": "2"]

        assert db.oneInteger("SELECT value FROM testtableAdmin WHERE name = ?", "InsertName") == 2
    }

    @Test
    void testInsertWithCanBeNullOrDefaultValue()
    {
        database.testTags << [
                CODE         : "12",
                payable      : "yes",
                admlevel     : "Regional",
                referenceTest: null
        ]

        assertEquals "12,yes,Regional,null",
                db.select("SELECT * FROM testTags WHERE CODE = ?", {rs -> BaseTestUtils.resultSetToString(rs)}, "12")

        database.getEntity("testTags").remove("12")
    }

    @Test(expected = RuntimeException.class)
    void testInsertError()
    {
        testtableAdmin << [
                "name": "InsertName",
                "value": "asd"]
    }

    @Test
    void testIsEmpty()
    {
        assertTrue(testtableAdmin.empty)

        testtableAdmin << [
                "name": "TestName",
                "value": "1"]

        assertFalse(testtableAdmin.empty)

        testtableAdmin << [
                "name": "TestName2",
                "value": "2"]

        assertFalse(testtableAdmin.empty)
    }

    @Test
    void testIsEmptyWithConditions()
    {
        assertTrue testtableAdmin.empty

        testtableAdmin << [
                "name": "TestName2",
                "value": "1"]

        assertFalse testtableAdmin.empty
        assertTrue testtableAdmin.contains( ["value": "1"] )
        assertFalse testtableAdmin.contains( ["value": "2"] )
    }

    @Test
    void testGetEntity()
    {
        assertEquals "testtableAdmin", testtableAdmin.tableName
        assertTrue testtableAdmin.empty
    }

//    @Test
//    @Ignore
//    void testDeleteAll()
//    {
//        def entityName = (EntityModel)testtableAdmin
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
//        assertEquals 0, testtableAdmin.count(["name": "TestName"])
//        assertEquals 1, testtableAdmin.count(["name": "TestName2"])
//    }

    @Test
    void testDeleteIn()
    {
        def id = testtableAdmin << [
                "name": "TestName1",
                "value": 1]

        assert testtableAdmin[ id ] != null

        assertFalse testtableAdmin.empty
        assertEquals 1, testtableAdmin.remove( id )

        assert testtableAdmin[ id ] == null
        assertTrue testtableAdmin.empty

        assertEquals 0, testtableAdmin.remove( id )

        def id2 = testtableAdmin << [
                "name": "TestName2",
                "value": 2]
        assertEquals 1, testtableAdmin.remove( [id2] as Long[] )
    }

    @Test
    void testDelete()
    {
        def id = testtableAdmin << [ "name": "TestName", "value": 1]
        def id2 = testtableAdmin << [ "name": "TestName2", "value": 1]

        assertFalse testtableAdmin.empty
        assertEquals 1, testtableAdmin.removeBy( ["name": "TestName2"] )
        assertNotNull testtableAdmin[ id ]
        assertNull testtableAdmin[ id2 ]
    }

    @Test
    void testDeleteSeveralId()
    {
        def id = testtableAdmin << [ "name": "TestName1", "value": 1]
        def id2 = testtableAdmin << [ "name": "TestName2", "value": 1]
        def id3 = testtableAdmin << [ "name": "TestName3", "value": 1]

        assertEquals 2, testtableAdmin.remove( id, id2 )

        assert testtableAdmin[ id ] == null
        assert testtableAdmin[ id2 ] == null
        assert testtableAdmin[ id3 ] != null
    }

    @Test
    void testUpdate()
    {
        def id = testtableAdmin << [
                "name": "TestName",
                "value": 1]

        testtableAdmin[id] = [//putAt(id, map)
                "name": "TestName2",
        ]

        def record = testtableAdmin[id]

        assertEquals "TestName2", record.$name
        assertEquals "TestName2", testtableAdmin[id].$name

        record << [
                "name": "TestName3",
        ]

        assertEquals "TestName3", record.$name
        assertEquals "TestName3", testtableAdmin[id].$name

        testtableAdmin[id] = BaseTestUtils.getDpsS("name": "TestName4")//putAt(id, map)

        assertEquals "TestName4", testtableAdmin[id].$name
    }

    @Test
    void testFindRecord()
    {
        testtableAdmin << [
            "name": "TestName2",
            "value": "123"]

        def rec = testtableAdmin( ["name": "TestName2"] )
        assertEquals( 123, rec.$value )
    }


    @Test
    void testGetRecord()
    {
        def id = testtableAdmin << [
                "name": "TestName2",
                "value": "123"]

        def record = testtableAdmin[ id ]
        assertEquals( "TestName2", record.$name )
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

    @Test
    void testGetList()
    {
        testtableAdmin << [
                "name": "TestName",
                "value": "1"]

        testtableAdmin << [
                "name": "TestName2",
                "value": "2"]

        List<RecordModel> list = testtableAdmin.toList()

        assertTrue( listContains( list, "name", "TestName" ) )
        assertTrue( listContains( list, "value", "1" ) )

        assertTrue( listContains( list, "name", "TestName2" ) )
        assertTrue( listContains( list, "value", "2" ) )


        list = testtableAdmin.toList( value: "1" )
        assertTrue( listContains( list, "name", "TestName" ) )

        list = testtableAdmin.toList( name: "TestName3" )
        assertTrue list.empty

        def array = testtableAdmin.toArray()
        assertTrue( arrayContains( array, "name", "TestName" ) )
    }

//    @Test
//    void testOperationGenerate()
//    {
//        def params = testtableAdmin.getOperation("ErrorProcessing").generate { presetValues = [ 'name': 'ok' ] }
//
//        assertEquals("ok", params.$name)
//    }
//
//    @Test
//    void testOperationExecute()
//    {
//        def operation = testtableAdmin.getOperation("ErrorProcessing").execute { presetValues = [ 'name': 'ok' ] }
//
//        assertEquals(OperationStatus.FINISHED, operation.getStatus())
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
//            def op = database.operationExtension.createOperation( 'Edit' )
//            op.records = [ opID ];
//            op.invoke();
//            assert database.operationExtension[ opID ] == null;
//        }
//        finally
//        {
//            database.operationExtension.remove( opID );
//        }
//    }

    private static boolean listContains(List<DynamicPropertySet> recs, String propertyName, String value )
    {
        for( DynamicPropertySet rec : recs )
        {
            if( rec.getValueAsString( propertyName ).equals( value ) )
            {
                return true
            }
        }
        return false
    }

    private static boolean arrayContains(DynamicPropertySet[] recs, String propertyName, String value )
    {
        for( DynamicPropertySet rec : recs )
        {
            if( rec.getValueAsString( propertyName ).equals( value ) )
            {
                return true
            }
        }
        return false
    }
}
