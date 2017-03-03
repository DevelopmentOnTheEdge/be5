package com.developmentontheedge.be5.metadata;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Only automated (not visual) tests for nightly build should be included here.
 */
public class AutoTest extends TestCase
{
    /** Standard JUnit constructor */
    public AutoTest(String name)
    {
        super(name);
    }

    /** Make suite of tests. */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTestSuite( ReadModelFromDatabaseTest.class );
        suite.addTestSuite( WriteModelToXmlTest.class );
        suite.addTestSuite( ReadModelFromXmlTest.class );
        suite.addTestSuite( AntTest.class );
        suite.addTestSuite( IconTest.class );

        return suite;
    }

} 
