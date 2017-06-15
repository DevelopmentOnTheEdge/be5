package com.developmentontheedge.be5.operation.databasemodel.groovy

import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.operation.databasemodel.groovy.FlexibleDynamicPropertySet
import com.developmentontheedge.be5.operation.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.test.AbstractProjectTest
import com.developmentontheedge.beans.BeanInfoConstants
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test


import static org.junit.Assert.*;
import static com.developmentontheedge.beans.BeanInfoConstants.*

class DynamicPropertiesGroovyTest extends AbstractProjectTest{

    DatabaseModel database = injector.get(DatabaseModel.class);

    @BeforeClass
    static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @AfterClass
    static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

//
//    private final DatabaseConnector connector = TestDB.getDefaultConnector( "postgresql", "be_test" );
//
//    public void setUp()
//    {
//        TestDB.delete( connector, "persons" );
//    }
//
    @Test
    void testSetValue() throws Exception
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps[ "property" ] = "value";
        assertEquals "value", dps.getValue( "property" );
    }

    @Test
    void testGetValue() throws Exception
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.build( "property", String.class ).value( "value" );

        assertEquals "value", dps.$property;
    }

    @Test
    void testGetMissingValue() throws Exception
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        assertNull dps.$property;
    }

    @Test
    void testGetValueWithoutAccessor() throws Exception
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.build( "property", String.class ).value( "value" );
        assertEquals "value", dps.property;
    }

    @Test
    void testGetMissingValueWithoutAccessor() throws Exception
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        boolean assertException = false;
        try
        {
            dps.property;
        }
        catch( MissingPropertyException e )
        {
            assertException = true;
        }
        assertTrue assertException;
    }

    @Test
    @Ignore
    void testCreateProperty()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()
        dps << [
                name            : "testProperty",
                DISPLAY_NAME    : "Тестовое свойство",
                value           : 1,
                HIDDEN          : true,
                TYPE            : java.sql.Date.class,
                TAG_LIST_ATTR   : [['A', 'a'], ['B', 'b'], ['C', 'c'], ['D', 'd']] as Object[][],
                RELOAD_ON_CHANGE: true,
                RAW_VALUE       : true
        ]
        DynamicProperty property = dps._testProperty;
        assertEquals 1, property.value
        assertEquals "testProperty", property.name
        assertEquals "Тестовое свойство", property.displayName
        assertEquals java.sql.Date.class, property.type
        assertTrue property.hidden

        final def tags = [['A', 'a'], ['B', 'b'], ['C', 'c'], ['D', 'd']] as Object[][]
        assertTrue Arrays.deepEquals( tags, property.getAttribute( TAG_LIST_ATTR ) )
        assertTrue property.getAttribute( RELOAD_ON_CHANGE )
        assertTrue property.getAttribute( RAW_VALUE )
    }

    @Test
    @Ignore
    void testAddProperty()
    {
        DynamicPropertySet dps = [ a : "a", b : "b" ] as DynamicPropertySetSupport
        DynamicProperty property = new DynamicProperty( "d", String, "d" );
        assert ( ( dps << property ).hasProperty( "d" ) )
        assert dps.hasProperty( "d" )
    }
//
//    public void testQRec()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def rec = database.operations( table_name : "operations", name : "Clone" );
//        def id = rec.id;
//        try {
//            assert rec.table_name == "operations";
//            rec.table_name = "cool";
//            assert database.operations[ id ].table_name == "cool";
//        } finally {
//            rec.table_name = "operations"
//        }
//    }
//
//    public void testQRecWithLeftShiftAndMap()
//    {
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def rec = database.operations( table_name : "operations", name : "Clone" );
//        def id = rec.id;
//        try {
//            assert rec.table_name == "operations";
//            assert rec.name == "Clone";
//            rec << [ table_name : "newTableName", name: "newName" ];
//            assert database.operations[ id ].table_name == "newTableName";
//            assert database.operations[ id ].name == "newName";
//        } finally {
//            rec.table_name = "operations"
//            rec.name = "Clone"
//        }
//    }
//
    @Test
    void testGetAttribute()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build( "test", String.class ).attr( BeanInfoConstants.INPUT_SIZE_ATTR, "10" );

        assertEquals dps._test.attr[ BeanInfoConstants.INPUT_SIZE_ATTR ], "10"
    }

    @Test
    void testGetAttributeStringConstant()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build( "test", String.class ).attr( BeanInfoConstants.INPUT_SIZE_ATTR, "10" );

        assertEquals dps._test.attr[ "INPUT_SIZE_ATTR" ], "10"
        assertEquals dps._test.attr.INPUT_SIZE_ATTR, "10"
    }

    @Test
    void testSetAttribute()
    {
        def DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build( "test", String.class )

        dps._test.attr[ BeanInfoConstants.INPUT_SIZE_ATTR ] = "10"
        assertEquals dps._test.getAttribute( BeanInfoConstants.INPUT_SIZE_ATTR ), "10";
    }

    @Test
    void testSetAttributeStringConstant()
    {
        def DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build( "test", String.class )

        dps._test.attr[ "INPUT_SIZE_ATTR" ] = "10"
        assertEquals dps._test.getAttribute( BeanInfoConstants.INPUT_SIZE_ATTR ), "10";
    }
//
//    public void testGetAt()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def DynamicPropertySet dps = new DynamicPropertySetSupport()
//        dps << [
//                name : "testProperty",
//                value: 1
//        ]
//        assertEquals 1, dps[ '$testProperty' ]
//    }
//
//    public void testGetAtWithoutAccessor()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def DynamicPropertySet dps = new DynamicPropertySetSupport()
//        dps << [
//                name : "testProperty",
//                value: 1
//        ]
//        assertEquals 1, dps[ "testProperty" ]
//    }
//
//    @Test
//    void testDynamicPropertySetPlus()
//    {
//        def dps1 = [ a : "a", b : "b", c : "c" ] as DynamicPropertySetSupport;
//        def dps2 = [ d : "d", e : "e", f : "f" ] as DynamicPropertySetSupport;
//        DynamicPropertySetSupport dps3 = dps1 + dps2;
//        assert dps1.size() == 3;
//        assert dps2.size() == 3;
//        [ a : "a", b : "b", c : "c", d : "d", e : "e", f : "f" ].each( { a, b ->
//            assert dps3.getValue( a ) == b
//        } );
//    }
//
    @Test
    void testGetMissingProperty() throws Exception
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.build( "___property", String ).value( "testValue" );
        println "testGetMissingProperty"
        assert "testValue" == dps."___property"
    }
//
//    public void testSetWith()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def DynamicPropertySet dps = new DynamicPropertySetSupport()
//        dps << [ name : "testProperty", value: 1 ]
//        dps << [ name : "testProperty2", value: 1 ]
//        dps.with {
//            testProperty = 2
//            testProperty2 = 2
//        }
//        assertEquals 2, dps.getValue( "testProperty" );
//        assertEquals 2, dps.getValue( "testProperty2" );
//    }
//
    @Test
    void testFlexibleDynamicPropertySet()
    {
        DynamicPropertySet dps = FlexibleDynamicPropertySet.getInstance();

        dps.hello = "Hi!";
        assertEquals( dps.getValue( "hello" ), "Hi!" );
        dps.guess.who = "King";
        def possible = dps.getValue( "guess" )
        assertTrue( possible instanceof DynamicPropertySet );
        assertEquals( possible.getValue( "who" ), "King" );
        assertEquals( dps.guess.$who, "King" );
    }
//
//    private static void assertException( Class<? extends Throwable> exp, Closure c )
//    {
//        try {
//            c();
//        } catch( Exception e ) {
//            assert e.class, exp
//            return;
//        }
//        fail();
//    }
//
//    public void testFlexibleDynamicPropertySetReadWrite()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        DynamicPropertySet instanceOfNativeDps = new DynamicPropertySetSupport();
//
//        def dps = FlexibleDynamicPropertySet.getInstance( instanceOfNativeDps );
//        dps.idps1.idps2.idps3.idps4 = "1";
//        dps.idps1.idps2.idps3_1.idps4 = "1";
//        dps.idps1.idps2.idps3_2.idps4 = "1";
//        dps.iidps1.iidps2.iidps3.iidps4 = "2";
//        dps.iiidps1.iiidps2.iiidps3.iiidps4 = "3";
//        assertEquals dps.idps1.idps2.idps3.$idps4, "1"
//        // STRONG PATH
//        assertEquals dps.$idps1.$idps2.$idps3.$idps4, "1"
//
//        assertException( NullPointerException.class ) {
//            dps.$idps1.$idpsNONE.$idps3
//        };
//
//        def dps2 = FlexibleDynamicPropertySet.getInstance( new DynamicPropertySetSupport() );
//        dps2.test1.test2 = dps.idps1.idps2.idps3.$idps4;
//
//        assertEquals dps2.test1.$test2, "1"
//        assertEquals dps2.test1.$test3, null
//        assertTrue dps2.test1.test3.testSome instanceof FlexibleDynamicPropertySet
//        assertEquals dps2.test1.test10.test12.$Test, null
//
//        assertEquals dps2.$test1.$test2, "1"
//        assertEquals dps2.$test1.$test10.$test12.$Test, null
//    }
//
//    public void testFlexibleDynamicPropertyWrap()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//
//        def test = new DynamicPropertySetSupport();
//        def dps = new DynamicPropertySetSupport();
//        dps.build( "test", DynamicPropertySet.class ).value( test );
//        def flex = FlexibleDynamicPropertySet.getInstance( dps );
//        assert flex.test instanceof FlexibleDynamicPropertySet
//    }
//
//    public void testFlexibleDynamicPropertyUnwrap()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def flex = FlexibleDynamicPropertySet.getInstance();
//        flex.test.hello.world = "1";
//        assert flex.test.hello instanceof FlexibleDynamicPropertySet
//        def dps = FlexibleDynamicPropertySet.unwrap( flex );
//        assert !( dps.test.hello instanceof FlexibleDynamicPropertySet )
//    }
//
//    public void testFlexibleMap()
//    {
//        setUp()
//        def database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        def map = new com.developmentontheedge.enterprise.operations.databasemodel.groovy.FlexibleMap();
//        map.test.node = "1";
//        assert map[ "test" ] instanceof HashMap;
//        assert map.test.node == "1";
//    }

}
