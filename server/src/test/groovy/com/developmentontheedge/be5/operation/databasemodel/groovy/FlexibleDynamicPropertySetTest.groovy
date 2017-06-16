package com.developmentontheedge.be5.operation.databasemodel.groovy

import com.developmentontheedge.be5.operation.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport
import org.junit.Ignore;
import org.junit.Test;


import static org.junit.Assert.*;

class FlexibleDynamicPropertySetTest extends AbstractProjectTest
{

    def database = injector.get(DatabaseModel.class);

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

    private static void assertException( Class<? extends Throwable> exp, Closure c )
    {
        try {
            c();
        } catch( Exception e ) {
            assert e.class, exp
            return;
        }
        fail();
    }

    @Test
    void testFlexibleDynamicPropertySetReadWrite()
    {
        DynamicPropertySet instanceOfNativeDps = new DynamicPropertySetSupport();

        def dps = FlexibleDynamicPropertySet.getInstance( instanceOfNativeDps );
        dps.idps1.idps2.idps3.idps4 = "1";
        dps.idps1.idps2.idps3_1.idps4 = "1";
        dps.idps1.idps2.idps3_2.idps4 = "1";
        dps.iidps1.iidps2.iidps3.iidps4 = "2";
        dps.iiidps1.iiidps2.iiidps3.iiidps4 = "3";
        assertEquals dps.idps1.idps2.idps3.$idps4, "1"
        // STRONG PATH
        assertEquals dps.$idps1.$idps2.$idps3.$idps4, "1"

        assertException( NullPointerException.class ) {
        dps.$idps1.$idpsNONE.$idps3
    };

        def dps2 = FlexibleDynamicPropertySet.getInstance( new DynamicPropertySetSupport() );
        dps2.test1.test2 = dps.idps1.idps2.idps3.$idps4;

        assertEquals dps2.test1.$test2, "1"
        assertEquals dps2.test1.$test3, null
        assertTrue dps2.test1.test3.testSome instanceof FlexibleDynamicPropertySet
        assertEquals dps2.test1.test10.test12.$Test, null

        assertEquals dps2.$test1.$test2, "1"
        assertEquals dps2.$test1.$test10.$test12.$Test, null
    }

    @Test
    void testFlexibleDynamicPropertyWrap()
    {

        def test = new DynamicPropertySetSupport();
        def dps = new DynamicPropertySetSupport();
        dps.build( "test", DynamicPropertySet.class ).value( test );
        def flex = FlexibleDynamicPropertySet.getInstance( dps );
        assert flex.test instanceof FlexibleDynamicPropertySet
    }

    @Test
    void testFlexibleDynamicPropertyUnwrap()
    {
        def flex = FlexibleDynamicPropertySet.getInstance();
        flex.test.hello.world = "1";
        assert flex.test.hello instanceof FlexibleDynamicPropertySet
        def dps = FlexibleDynamicPropertySet.unwrap( flex );
        assert !( dps.test.hello instanceof FlexibleDynamicPropertySet )
    }

    @Test
    @Ignore
    void testFlexibleMap()
    {
        def map = new com.developmentontheedge.be5.operation.databasemodel.groovy.FlexibleMap();
        map.test.node = "1";
        assert map[ "test" ] instanceof HashMap;
        assert map.test.node == "1";
    }
}