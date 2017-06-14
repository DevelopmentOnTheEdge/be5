package com.developmentontheedge.be5.operation.databasemodel;

import junit.framework.TestCase;
import org.junit.Ignore;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@Ignore
public class RecordModelTest extends TestCase
{
//    private final DatabaseConnector connector = TestDB.getDefaultConnector( "postgresql", "be_test" );
//
//    @Override
//    protected void tearDown() throws Exception
//    {
//        connector.releaseConnection( null );
//        super.tearDown();
//    }
//
//    @Override
//    protected void setUp() throws Exception
//    {
//        super.setUp();
//        TestDB.delete( connector, "persons" );
//    }
//
//    public void testDelete()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entity = database.<EntityModel>getEntity( "persons" );
//
//        String id = entity.add( ImmutableMap.of(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        assertFalse( entity.isEmpty() );
//
//        RecordModel record = entity.get( id );
//        record.remove();
//
//        assertTrue( entity.isEmpty() );
//    }
//
//    public void testGetRecord()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entity = database.<EntityModel>getEntity( "persons" );
//
//        String id = entity.add( ImmutableMap.of(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        RecordModel record = entity.get( id );
//        assertEquals( "Niklaus", record.getValueAsString( "lastname" ) );
//    }
//
//    public void testFindRecord() throws SQLException
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entity = database.<EntityModel>getEntity( "persons" );
//
//        entity.add( ImmutableMap.of(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        RecordModel rec = database.getEntity( "persons" ).get( Maps.hashMap(
//            "sex", "male"
//        ));
//
//        assertEquals( "Niklaus", rec.getValueAsString( "lastname" ) );
//    }
//
//    public void testUpdateRecord()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entity = database.<EntityModel>getEntity( "persons" );
//
//        String id = entity.add( ImmutableMap.of(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        RecordModel record = entity.get( id );
//        assertEquals( "Emil", record.getValue( "middlename" ) );
//
//        record.update( "middlename", "Champion" );
//        assertEquals( "Champion", record.getValue( "middlename" ) );
//
//        record = entity.get( id );
//        assertEquals( "Champion", record.getValue( "middlename" ) );
//    }
//
    
}
