package com.developmentontheedge.be5.metadata.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class OperationExtenderTest
{
    @Test
    public void testCreate()
    {
        Project prj = new Project("test");
        Entity e = new Entity( "e", prj.getApplication(), EntityType.TABLE );
        DataElementUtils.save( e );
        Operation operation = Operation.createOperation( "op", Operation.OPERATION_TYPE_JAVA, e );
        DataElementUtils.save( operation );
        OperationExtender extender = new OperationExtender( operation, prj.getProjectOrigin() );
        DataElementUtils.save( extender );
        assertEquals("application - 0001", extender.getName());
    }

    @Test
    public void testClone() throws Exception
    {
        Project prj = new Project("test");
        Entity e = new Entity( "e", prj.getApplication(), EntityType.TABLE );
        DataElementUtils.save( e );
        Operation operation = Operation.createOperation( "op", Operation.OPERATION_TYPE_JAVA, e );
        DataElementUtils.save( operation );
        OperationExtender extender = new OperationExtender( operation, prj.getProjectOrigin() );
        extender.setClassName( "class" );
        DataElementUtils.save( extender );
        OperationExtender extender2 = ( OperationExtender ) extender.clone( extender.getOrigin(), extender.getName() );
        assertEquals(extender2, extender);
        extender2.setClassName( "class2" );
        assertNotEquals(extender2, extender);
        extender2 = ( OperationExtender ) extender.clone( extender.getOrigin(), extender.getName() );
        extender2.setInvokeOrder( 10 );
        assertNotEquals(extender2, extender);
    }
}
