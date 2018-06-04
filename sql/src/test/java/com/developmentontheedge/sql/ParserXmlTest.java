package com.developmentontheedge.sql;

import com.developmentontheedge.xmltest.TargetFactory;
import com.developmentontheedge.xmltest.XmlTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ParserXmlTest extends TestCase
{
    protected static final String relativePath = "./src/test/resources/";
    
    /** Standart JUnit constructor */
    public ParserXmlTest(String name)
    {
        super(name);
    }

    /** Make suite of tests. */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(ParserXmlTest.class.getName());

        TargetFactory targetFactory = new TargetFactory();
        targetFactory.addTarget(ParserTarget.TAG_PARSER, ParserTarget.class.getName());
        
        suite.addTest(new XmlTestSuite(relativePath+"core.xml", targetFactory));
        suite.addTest(new XmlTestSuite(relativePath+"count.xml", targetFactory));
        suite.addTest(new XmlTestSuite(relativePath+"limit.xml", targetFactory));
        suite.addTest(new XmlTestSuite(relativePath+"be.xml", targetFactory));
        suite.addTest(new XmlTestSuite(relativePath+"functions.xml", targetFactory));
        suite.addTest(new XmlTestSuite(relativePath+"payments.xml", targetFactory));
        suite.addTest(new XmlTestSuite(relativePath+"realty.xml", targetFactory));
        suite.addTest(new XmlTestSuite(relativePath+"biostore.xml", targetFactory));

        //suite.addTest(new XmlTestSuite(relativePath+"single.xml", targetFactory));
        
        return suite;
    }
} 
