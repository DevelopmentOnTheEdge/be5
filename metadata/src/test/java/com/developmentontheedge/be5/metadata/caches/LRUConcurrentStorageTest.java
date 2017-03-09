// $Id: LRUConcurrentStorageTest.java,v 1.2 2011/09/02 05:52:50 sav Exp $
package com.developmentontheedge.be5.metadata.caches;

import com.developmentontheedge.be5.metadata.caches.impl.LRUConcurrentStorage;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LRUConcurrentStorageTest extends TestCase
{
    public void testSimple()
    {
        LRUConcurrentStorage s = new LRUConcurrentStorage( 100 );
        s.put( "a", "val1" );
        s.put( "ab", "val2" );
        s.put( "xx", "val3" );
        assertEquals( "val1", s.get( "a" ) );
        assertEquals( "val2", s.get( "ab" ) );
        assertEquals( "val3", s.get( "xx" ) );

        s.remove( "xx" );
        assertNull( s.get( "xx" ) );
    }

    public void testLRUBehavior()
    {
        LRUConcurrentStorage s = new LRUConcurrentStorage( 100 );
        for( int i = 0; i < 101; i++ )
        {
            s.put( "x" + i, "val" + i );
        }

        assertNull( s.get( "x0" ) );

        for( int i = 1; i < 101; i++ )
        {
            s.put( "x" + i, "val" + i );
            assertEquals( "val" + i, s.get( "x" + i ) );
        }
    }

    public void testLRUBehaviorMT() throws Exception
    {
        ExecutorService executor = Executors.newFixedThreadPool( 20 );

        int cacheSize = 10000;
        final LRUConcurrentStorage s = new LRUConcurrentStorage( cacheSize );

        List<Future<?>> futures = new ArrayList<Future<?>>( cacheSize + 10 );
        for( int i = 0; i < cacheSize + 1; i++ )
        {
            final int k = i;
            Future<?> f = executor.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    s.put( "x" + k, "val" + k );
                }
            } );
            futures.add( f );
        }

        for( Future<?> f : futures )
        {
            f.get();
        }

        //assertNull( s.get( "x0" ) );

        assertTrue( s.size() <= cacheSize );

        for( int i = 1; i < cacheSize + 1; i++ )
        {
            s.put( "x" + i, "val" + i );
            assertEquals( "val" + i, s.get( "x" + i ) );
        }

        for( Future<?> f : futures )
        {
            f.get();
        }
    }
}
