package com.developmentontheedge.be5.databasemodel.groovy

import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.inject.Inject
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport

import org.junit.Ignore
import org.junit.Test


import static org.junit.Assert.*
import static com.developmentontheedge.beans.BeanInfoConstants.*

class DynamicPropertiesGroovyTest extends Be5ProjectTest
{
    @Inject private DatabaseModel database

    @Test
    void testSetValue()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps[ "property" ] = "value"
        assertEquals "value", dps.getValue( "property" )
    }

    @Test
    void testGetValue()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build( "property", String.class ).value( "value" )

        assertEquals "value", dps.$property
    }

    @Test
    void testGetProperty()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build( "property", String.class ).value( "value" )

        assertTrue dps.property instanceof DynamicProperty
        assertEquals "value", dps.property.value
    }

    @Test
    void testGetMissingValue()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        assertNull dps.$property
    }

    @Test
    void testPropertyAccess()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build( "property", String.class ).value( "value" )
        assertEquals "value", dps._property.value
    }

    @Test
    void testGetMissingValueWithoutAccessor()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        boolean assertException = false
        try
        {
            dps.property
        }
        catch( MissingPropertyException e )
        {
            assertException = true
        }
        assertTrue assertException
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
                TAG_LIST_ATTR   : ['A': 'a', 'B': 'b', 'C': 'c', 'D': 'd'],
                RELOAD_ON_CHANGE: true,
                RAW_VALUE       : true
        ]
        DynamicProperty property = dps._testProperty
        assertEquals 1, property.value
        assertEquals "testProperty", property.name
        assertEquals "Тестовое свойство", property.displayName
        assertEquals java.sql.Date.class, property.type
        assertTrue property.hidden

        final def tags = ['A': 'a', 'B': 'b', 'C': 'c', 'D': 'd']
        assertEquals tags, property.getAttribute( TAG_LIST_ATTR )
        assertTrue property.getAttribute( RELOAD_ON_CHANGE )
        assertTrue property.getAttribute( RAW_VALUE )
    }

    @Test
    void testAddProperty()
    {
        DynamicPropertySet dps = [ a : "a", b : "b" ] as DynamicPropertySetSupport
        DynamicProperty property = new DynamicProperty( "d", String, "d" )
        assert ( ( dps << property ).hasProperty( "d" ) )
        assert dps.hasProperty( "d" )
    }

    @Test
    @Ignore
    void testQRec()
    {
        def rec = database.operations( table_name : "operations", name : "Clone" )
        def id = rec.getPrimaryKey
        try {
            assert rec.table_name == "operations"
            rec.table_name = "cool"
            assert database.operations[ id ].table_name == "cool"
        } finally {
            rec.table_name = "operations"
        }
    }

    @Test
    @Ignore
    void testQRecWithLeftShiftAndMap()
    {
        def rec = database.operations( table_name : "operations", name : "Clone" )
        def id = rec.getPrimaryKey
        try {
            assert rec.table_name == "operations"
            assert rec.name == "Clone"
            rec << [ table_name : "newTableName", name: "newName" ]
            assert database.operations[ id ].table_name == "newTableName"
            assert database.operations[ id ].name == "newName"
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
        dps.build( "test", String.class ).attr( INPUT_SIZE_ATTR, "10" )

        assertEquals dps._test.attr[ INPUT_SIZE_ATTR ], "10"
    }

    @Test
    void testGetAttributeStringConstant()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build( "test", String.class ).attr( INPUT_SIZE_ATTR, "10" )

        assertEquals dps._test.attr[ INPUT_SIZE_ATTR ], "10"
        assertEquals dps._test.attr.INPUT_SIZE_ATTR, "10"
    }

    @Test
    void testSetAttribute()
    {
        def DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build( "test", String.class )

        dps._test.attr[ INPUT_SIZE_ATTR ] = "10"
        assertEquals dps._test.getAttribute( INPUT_SIZE_ATTR ), "10"
    }

    @Test
    void testSetAttributeStringConstant()
    {
        def DynamicPropertySetSupport dps = new DynamicPropertySetSupport()
        dps.build( "test", String.class )

        dps._test.attr[ "INPUT_SIZE_ATTR" ] = "10"
        assertEquals dps._test.getAttribute( INPUT_SIZE_ATTR ), "10"
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
        assertEquals 1, dps[ "testProperty" ].value
    }


    @Test
    void testDynamicPropertySetPlus()
    {
        def dps1 = [ a : "a", b : "b", c : "c" ] as DynamicPropertySetSupport
        def dps2 = [ d : "d", e : "e", f : "f" ] as DynamicPropertySetSupport

        DynamicPropertySetSupport dps3 = dps1 + dps2
        assert dps1.size() == 3
        assert dps2.size() == 3
        [ a : "a", b : "b", c : "c", d : "d", e : "e", f : "f" ].each( { a, b ->
            assert dps3.getValue( a ) == b
        } )
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
        assertEquals 2, dps.getValue( "testProperty" )
        assertEquals 2, dps.getValue( "testProperty2" )
    }

}
