package com.developmentontheedge.be5.operation.databasemodel;


import com.developmentontheedge.be5.model.UserInfo;
import junit.framework.TestCase;
import org.junit.Ignore;

@Ignore
public class DatabaseModelCommonTest extends TestCase
{
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
//        EntityModel entity = database.getEntity( "persons" );
//
//        assertEquals( "persons", entity.getEntityName( ) );
//        assertTrue( entity.isEmpty( ) );
//    }
//
//    public void testGetUserInfo()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        assertTrue( UserInfo.ADMIN == database.getUserInfo() );
//    }
//
//    public void testGetCache()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        assertTrue( database.getCache()!= null );
//    }
//
//    public void testGetAnalyzer()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        assertEquals( connector.getAnalyzer(), database.getAnalyzer() );
//    }
//
//    public void testGetCloned()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        assertEquals( "1", database.getCloned( "1" ).getTcloneId() );
//    }
//
//    public void testGetConnector()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        assertEquals( connector, database.getConnector() );
//    }
//
//    public void testGetEntityWithTcloneId()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entity = database.getEntity( "persons", "1" );
//        assertEquals( "persons", entity.getEntityName() );
//        assertEquals( "1", entity.getTcloneId() );
//        assertEquals( "persons1", entity.getTableName() );
//    }
//
//    public void testEquals()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        DatabaseModel database2 = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        assertNotSame( database, database2 );
//        assertEquals( database, database2 );
//
//        DatabaseConnector fakeConnector = TestDB.getConnector( "jdbc:sqlite:test.sqlite3" );
//        DatabaseModel databaseFake = DatabaseModel.makeInstance( fakeConnector, UserInfo.GUEST );
//        assertFalse( database.equals( databaseFake ) );
//
//        assertFalse( database.equals( database2.getCloned( "1" ) ) );
//    }

}
