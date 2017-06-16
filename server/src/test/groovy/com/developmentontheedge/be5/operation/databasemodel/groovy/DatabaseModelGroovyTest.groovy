package com.developmentontheedge.be5.operation.databasemodel.groovy

import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.operation.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.test.AbstractProjectTest
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport


import junit.framework.TestCase
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import java.sql.SQLException

import static com.developmentontheedge.beans.BeanInfoConstants.*

class DatabaseModelGroovyTest extends AbstractProjectTest
{
    DatabaseModel database = injector.get(DatabaseModel.class);

    @BeforeClass
    static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @AfterClass
    static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    @Ignore
    void test()
    {
        //def testtableAdmin = database.testtableAdmin;

        //assert database.persons( ["sex": "male"] ) == null

        database.testtableAdmin << [
                "name": "Test",
                "value": "1"];

        //assert database.persons( ["sex": "male"] ) != null
        //assert database.cache.persons[ id ] != null

        //database.persons[ id ].remove()
        //assert database.persons[ id ] == null
        //assert database.cache.persons[ id ] != null
    }
//
//    public void testGroovyCount()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def persons = database.persons;
//
//        assertEquals 0, persons.size()
//
//        persons << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"];
//
//        assertEquals 1, persons.size()
//    }
//
//    public void testInsert() throws Exception
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        DynamicPropertySet dps = new DynamicPropertySetSupport();
//
//        database.persons = [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"];
//
//        TestDB.checkTableData( connector, "persons", [
//                ["firstName", "middleName", "lastName", "birthday", "sex"],
//                ["Wirth", "Emil", "Niklaus", "1934-02-15", "male"]] as String[][] );
//    }
//
//    public void testCount()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def persons = database.persons;
//
//        assertEquals 0, persons.size()
//
//        persons << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"];
//
//        assertEquals 1, persons.size()
//    }
//
//    public void testIsEmpty()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def persons = database.persons;
//
//        assertTrue persons.empty
//
//        persons << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"];
//
//        assertFalse persons.empty
//
//        persons << [
//                "firstname": "Bjarne",
//                "lastname" : "Stroustrup",
//                "birthday" : "30.12.1950",
//                "sex"      : "male"]
//
//        assertFalse persons.empty
//    }
//
//
//    public void testIsEmptyWithConditions()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def entityName = database.persons;
//
//        assertTrue entityName.empty;
//
//        entityName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"];
//
//        assertFalse entityName.empty;
//        assertTrue entityName.contains( ["lastname": "Niklaus"] );
//        assertFalse entityName.contains( ["lastname": "Bjarne"] );
//    }
//
//
//    public void testGetEntity()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def persons = database.persons;
//
//        assertEquals "persons", persons.entityName
//        assertTrue persons.empty
//    }
//
//    public void testDelete()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def entityName = database.persons;
//
//        String id = database.persons << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"
//        ];
//
//        assertFalse entityName.empty;
//        assertEquals 1, entityName.remove( id );
//        assertTrue entityName.empty;
//        assertEquals 0, entityName.remove( id );
//    }
//
//    public void testFindRecord() throws SQLException
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def entityName = database.persons;
//
//        entityName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"
//        ];
//
//        def rec = database.persons( ["sex": "male"] );
//        assertEquals( "Niklaus", rec.$lastname );
//    }
//
//
//    public void testGetRecord()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def entityName = database.persons;
//
//        def id = entityName << [
//                "firstname" : "Wirth",
//                "middlename": "Emil",
//                "lastname"  : "Niklaus",
//                "birthday"  : "15.02.1934",
//                "sex"       : "male"
//        ];
//
//        def record = entityName[ id ];
//        assertEquals( "Niklaus", record.$lastname );
//    }
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
