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

    def database = injector.get(DatabaseModel.class);

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
    void testAddProperty()
    {
        DynamicPropertySet dps = [ a : "a", b : "b" ] as DynamicPropertySetSupport
        DynamicProperty property = new DynamicProperty( "d", String, "d" );
        assert ( ( dps << property ).hasProperty( "d" ) )
        assert dps.hasProperty( "d" )
    }

    @Test
    @Ignore
    void testQRec()
    {
        def rec = database.operations( table_name : "operations", name : "Clone" );
        def id = rec.id;
        try {
            assert rec.table_name == "operations";
            rec.table_name = "cool";
            assert database.operations[ id ].table_name == "cool";
        } finally {
            rec.table_name = "operations"
        }
    }

    @Test
    @Ignore
    void testQRecWithLeftShiftAndMap()
    {
        def rec = database.operations( table_name : "operations", name : "Clone" );
        def id = rec.id;
        try {
            assert rec.table_name == "operations";
            assert rec.name == "Clone";
            rec << [ table_name : "newTableName", name: "newName" ];
            assert database.operations[ id ].table_name == "newTableName";
            assert database.operations[ id ].name == "newName";
        } finally {
            rec.table_name = "operations"
            rec.name = "Clone"
        }
    }
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

    @Test
    void testGetAt()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()
        dps << [
                name : "testProperty",
                value: 1
        ]
        assertEquals 1, dps[ '$testProperty' ]
    }

    @Test
    void testGetAtWithoutAccessor()
    {
        def dps = new DynamicPropertySetSupport()
        dps << [
                name : "testProperty",
                value: 1
        ]
        assertEquals 1, dps[ "testProperty" ]
    }


    @Test
    void testDynamicPropertySetPlus()
    {
        def dps1 = [ a : "a", b : "b", c : "c" ] as DynamicPropertySetSupport;
        def dps2 = [ d : "d", e : "e", f : "f" ] as DynamicPropertySetSupport;

//Work
//        DynamicPropertySetSupport.metaClass.plus = { DynamicPropertySet dps ->
//            DynamicPropertySet clonedDps = new DynamicPropertySetSupport( delegate );
//            for (DynamicProperty dp : dps2)
//            {
//                try
//                {
//                    clonedDps.add(DynamicPropertySetSupport.cloneProperty(dp));
//                } catch (Exception wierd)
//                {
//                    log.severe("Unable to clone property " + dp.getName() + ", message = " + wierd.getMessage());
//                }
//            }
//            return clonedDps;
//        }

        DynamicPropertySetSupport dps3 = dps1 + dps2;
        assert dps1.size() == 3;
        assert dps2.size() == 3;
        [ a : "a", b : "b", c : "c", d : "d", e : "e", f : "f" ].each( { a, b ->
            assert dps3.getValue( a ) == b
        } );
    }

    @Test
    void testGetMissingProperty() throws Exception
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.build( "___property", String ).value( "testValue" );
        assert "testValue" == dps."___property"
    }

    @Test
    void testSetWith()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()
        dps << [ name : "testProperty", value: 1 ]
        dps << [ name : "testProperty2", value: 1 ]
        dps.with {
            testProperty = 2
            testProperty2 = 2
        }
        assertEquals 2, dps.getValue( "testProperty" );
        assertEquals 2, dps.getValue( "testProperty2" );
    }

}
