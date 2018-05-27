package com.developmentontheedge.be5.databasemodel;


import junit.framework.TestCase;
import org.junit.Ignore;

@Ignore
public class EntityModelTest extends TestCase 
{
//    private final DatabaseConnector connector = TestDB.getDefaultConnector( "postgresql", "be_test" );
//
//    @Override
//    protected void tearDown()
//    {
//        super.tearDown();
//        connector.releaseConnection( null );
//    }
//
//    @Override
//    protected void setUp()
//    {
//        super.setUp();
//        TestDB.delete( connector, "persons" );
//        ReferencesQueriesCache.getInstance().clear();
//    }
//
//    public void testIterator()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel persons = database.getEntity( "persons" );
//        persons.add( Maps.hashMap(
//                        "firstname", "Wirth",
//                        "middlename", "Emil",
//                        "lastname", "Niklaus",
//                        "birthday", "15.02.1934",
//                        "sex", "male" )
//        );
//        QueryModel queryModel = database.getEntity( "persons" ).getQuery( DatabaseConstants.ALL_RECORDS_VIEW );
//        try( CloseableIterator<DynamicPropertySet> i = queryModel.getIterator() )
//        {
//            assertTrue( i.hasNext() );
//            assertEquals( i.next().getValue( "name" ), "Niklaus Wirth Emil" );
//        }
//    }
//
//    public void testCaseSensetive()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        String personID = database.getEntity( "persons" ).add( Maps.hashMap(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        database.getEntity( "persons" ).set( personID, Maps.hashMap( "SeX", "female" ) );
//        assertEquals( "female", database.getEntity( "persons" ).get( personID ).getValue( "SEx" ) );
//    }
//
//    public void testRemove()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//
//        EntityModel entity = database.getEntity( "persons" );
//
//        String personID = entity.add( Maps.hashMap(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Bjarne",
//            "lastname", "Stroustrup",
//            "birthday", "30.12.1950",
//            "sex", "male" )
//        );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Marie",
//            "middlename", "Skłodowska",
//            "lastname", "Salomea",
//            "birthday", "07.10.1867",
//            "sex", "female" )
//        );
//
//        Map<String, String> values= new HashMap<>();
//        values.put( "sex", "male" );
//        values.put( "birthday", "30.12.1950" );
//
//        assertEquals( 1, entity.remove( values ) );
//        assertEquals( 1, entity.remove( Collections.singletonMap( "sex", "female" ) ) );
//
//        TestDB.checkTableData( connector, "persons", new String[][]{
//            { "firstName", "middleName", "lastName", "birthday" ,"sex" },
//            { "Wirth" ,"Emil", "Niklaus", "1934-02-15", "male" }
//        } );
//
//        assertEquals( 1, entity.remove( personID ) );
//        assertTrue( entity.isEmpty() );
//        assertEquals( 0, entity.remove( personID ) );
//    }
//
//    public void testCount()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//
//        EntityModel entity = database.getEntity( "persons" );
//
//        assertEquals( 0, entity.size() );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        assertEquals( 1, entity.size() );
//    }
//
//    public void testIsEmpty()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//
//        EntityModel entity = database.getEntity( "persons" );
//
//        assertTrue( entity.isEmpty() );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        assertFalse( entity.isEmpty() );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Bjarne",
//            "lastname", "Stroustrup",
//            "birthday", "30.12.1950",
//            "sex", "male" )
//        );
//
//        assertFalse( entity.isEmpty() );
//    }
//
//
//    public void testIsEmptyWithConditions()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//
//        EntityModel entity = database.getEntity( "persons" );
//
//        assertTrue( entity.isEmpty() );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        assertFalse( entity.isEmpty() );
//        assertTrue( entity.contains( Maps.hashMap(
//            "lastname", "Niklaus"
//        ) ) );
//
//        assertFalse( entity.contains( Maps.hashMap(
//             "lastname", "Bjarne"
//        ) ) );
//    }
//
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
//
//
//    public static class NewModel extends EntityModelBase
//    {
//
//        public NewModel( DatabaseModel database, UserInfo userInfo, String entity, String tcloneId, boolean forceCache )
//        {
//            super( database, userInfo, entity, tcloneId, forceCache );
//        }
//
//        @EntityMethod
//        public Map<?,?> getAsMap( RecordModel rec )
//        {
//            return rec.asMap();
//        }
//
//        @EntityMethod
//        public String getBirthdayWithFormat( RecordModel rec, String format )
//        {
//            return new java.text.SimpleDateFormat( format ).format( rec.getValue( "birthday" ) );
//        }
//    }
//
//    public void testEntityModelInvokeMethod()
//    {
//        if( ExtendedModels.isAvailable( connector ) )
//        {
//            DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//
//            RecordModel entityRec = database
//                    .getEntity( "entities" )
//                    .get( Collections.singletonMap( "name", "persons" ) );
//
//            Object lastModel = entityRec.getValue( "entityModel" );
//            try
//            {
//                entityRec.update( "entityModel", NewModel.class.getName() );
//                NewModel persons = database.<NewModel>getEntity( "persons" );
//
//                String id = persons.add( Maps.hashMap(
//                    "firstname", "Wirth",
//                    "middlename", "Emil",
//                    "lastname", "Niklaus",
//                    "birthday", "15.02.1934",
//                    "sex", "male" )
//                );
//
//                RecordModel rec = persons.get( id );
//
//                assertEquals( rec.asMap(), rec.invokeMethod( "getAsMap" ) );
//                assertEquals( rec.asMap(), persons.getAsMap( rec ) );
//            }
//            finally
//            {
//                entityRec.update( "entityModel", lastModel );
//            }
//        }
//    }
//
//    public void testEntityModelInvokeMethodWithArgs()
//    {
//        if( ExtendedModels.isAvailable( connector ) )
//        {
//            DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//
//            RecordModel entityRec = database
//                    .getEntity( "entities" )
//                    .get( Collections.singletonMap( "name", "persons" ) );
//
//            Object lastModel = entityRec.getValue( "entityModel" );
//            try
//            {
//                entityRec.update( "entityModel", NewModel.class.getName() );
//                NewModel persons = database.<NewModel>getEntity( "persons" );
//
//                String id = persons.add( Maps.hashMap(
//                                "firstname", "Wirth",
//                                "middlename", "Emil",
//                                "lastname", "Niklaus",
//                                "birthday", "15.02.1934",
//                                "sex", "male" )
//                );
//
//                RecordModel rec = persons.get( id );
//
//                assertEquals( "15021934", rec.invokeMethod( "getBirthdayWithFormat", "ddMMyyyy" ) );
//            }
//            finally
//            {
//                entityRec.update( "entityModel", lastModel );
//            }
//        }
//    }
//
//
//    private boolean listContains( List<? extends DynamicPropertySet> recs, String propertyName, String value )
//    {
//        for( DynamicPropertySet rec : recs )
//        {
//            if( rec.getValueAsString( propertyName ).equals( value ) )
//            {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void testGetList()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entity = database.getEntity( "persons" );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Bjarne",
//            "lastname", "Stroustrup",
//            "birthday", "30.12.1950",
//            "sex", "male" )
//        );
//
//        List<RecordModel> list = entity.toList();
//
//        assertTrue( listContains( list, "firstname" , "Wirth" ) );
//        assertTrue( listContains( list, "firstname" , "Bjarne" ) );
//    }
//
//    public void testGetArray()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entity = database.getEntity( "persons" );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Bjarne",
//            "lastname", "Stroustrup",
//            "birthday", "30.12.1950",
//            "sex", "male" )
//        );
//
//        RecordModel[] records = entity.toArray();
//        List<RecordModel> list = Arrays.asList( records );
//        assertTrue( listContains( list, "firstname" , "Wirth" ) );
//        assertTrue( listContains( list, "firstname" , "Bjarne" ) );
//    }
//
//    public void testCollect()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entity = database.getEntity( "persons" );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Wirth",
//            "middlename", "Emil",
//            "lastname", "Niklaus",
//            "birthday", "15.02.1934",
//            "sex", "male" )
//        );
//
//        entity.add( Maps.hashMap(
//            "firstname", "Bjarne",
//            "lastname", "Stroustrup",
//            "birthday", "30.12.1950",
//            "sex", "male" )
//        );
//
//        List<DynamicPropertySet> list = entity.collect( Collections.emptyMap(), ( bean, row ) -> bean );
//        assertTrue( listContains( list, "firstname" , "Wirth" ) );
//        assertTrue( listContains( list, "firstname" , "Bjarne" ) );
//    }
//
//    private void addIfPropertyValueEquals( DynamicPropertySet dps, String propName, String value, List<DynamicPropertySet> list )
//    {
//        DynamicProperty dp = dps.getProperty( propName );
//        assertNotNull( "dps = " + dps, dp );
//        if( value.equals( dp.getValue() ) )
//        {
//            list.add( dps );
//        }
//    }
//
//    public void testQuery()
//    {
//        DatabaseModel database = DatabaseModel.makeInstance( connector, UserInfo.ADMIN );
//        EntityModel entity = database.getEntity( "queries" );
//
//        RecordModel rec = entity.get( Maps.hashMap(
//            "table_name", "queries",
//            "name", DatabaseConstants.ALL_RECORDS_VIEW
//        ) );
//        String oldQuery = rec.getValueAsString( "query" );
//        String id = rec.getValueAsString( "id" );
//        assertNotNull( oldQuery );
//        assertNotNull( id );
//
//        try
//        {
//            String newSQL = "SELECT * FROM queries WHERE name = '" + DatabaseConstants.ALL_RECORDS_VIEW + "'";
//            entity.set( id, Maps.hashMap( "query", newSQL ) );
//            List<DynamicPropertySet> list = new ArrayList<>();
//            entity.getQuery( DatabaseConstants.ALL_RECORDS_VIEW ).each( ( b, rn ) -> addIfPropertyValueEquals( b, "table_name", "queries", list ) );
//            assertEquals( 1, list.size() );
//            assertEquals( newSQL, list.get( 0 ).getValueAsString( "query" ) );
//        }
//        finally
//        {
//            entity.set( id, Maps.hashMap( "query", oldQuery ) );
//        }
//    }
//
    
}
