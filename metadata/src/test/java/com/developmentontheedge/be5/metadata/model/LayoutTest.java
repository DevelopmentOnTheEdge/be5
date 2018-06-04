package com.developmentontheedge.be5.metadata.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class LayoutTest
{
    @Test
    public void test()
    {
        Project prj = new Project("test");
        Entity e = new Entity( "e", prj.getApplication(), EntityType.TABLE );
        DataElementUtils.save( e );
        Operation operation = Operation.createOperation( "op", Operation.OPERATION_TYPE_JAVA, e );
        DataElementUtils.save( operation );
        Query q = new Query( "q", e );
        DataElementUtils.save( q );
        StaticPage sp = new StaticPage( "sp", prj.getApplication().getStaticPageCollection() );
        DataElementUtils.save( sp );
        List<String> domains = Arrays.asList(PageCustomization.getDomains( prj.getApplication() ));

        assertEquals("", q.getLayout());

        q.setLayout("{\"test\":\"test\"}");
        assertEquals("{\"test\":\"test\"}", q.getLayout());

        assertEquals("", operation.getLayout());

        operation.setLayout("{\"test\":\"test2\"}");
        assertEquals("{\"test\":\"test2\"}", operation.getLayout());
    }

}
