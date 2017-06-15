package com.developmentontheedge.be5.operation.databasemodel;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.databasemodel.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.operation.databasemodel.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.operation.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseModelTest extends AbstractProjectTest
{

    @BeforeClass
    public static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @AfterClass
    public static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void databaseModel() throws Exception
    {
        DatabaseModel databaseModel = injector.get(DatabaseModel.class);

        assertEquals(DynamicPropertyMetaClass.class,
                InvokerHelper.getMetaRegistry().getMetaClass(DynamicProperty.class).getClass());

        assertEquals(DynamicPropertySetMetaClass.class,
                InvokerHelper.getMetaRegistry().getMetaClass(DynamicPropertySetSupport.class).getClass());

    }


//    private final DatabaseConnector connector = TestDB.getDefaultConnector( "postgresql", "be_test" );
//
//    @Override
//    protected void tearDown() throws Exception
//    {
//        super.tearDown();
//        connector.releaseConnection( null );
//    }
//
//    @Override
//    protected void setUp() throws Exception
//    {
//        TestDB.delete( connector, "persons" );
//        super.setUp();
//    }
//
//    public void testGetEntity()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//
//        EntityModel entity = database.getEntity( "persons" );
//
//        assertEquals( "persons", entity.getEntityName() );
//        assertTrue( entity.isEmpty() );
//    }
}
