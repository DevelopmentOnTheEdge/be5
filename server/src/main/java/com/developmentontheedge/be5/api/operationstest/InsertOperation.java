package com.developmentontheedge.be5.api.operationstest;


import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class InsertOperation extends OperationSupport implements DatabaseConstants, SystemOperation
{
    private static final Logger log = Logger.getLogger(InsertOperation.class.getName());

    public static final String DBMS_DATE_PLACEHOLDER = "@@getCurrentDateExpr@@";
    public static final String DBMS_DATETIME_PLACEHOLDER = "@@getCurrentDateTimeExpr@@";
    public static final String PUT_DBMS_DATE_PLACEHOLDER_FLAG = "put-dbms-date-placeholder-flag";
    public static final String PUT_DBMS_DATETIME_PLACEHOLDER_FLAG = "put-dbms-datetime-placeholder-flag";
    public static final String KEEP_VALUE_PLACEHOLDER = "@@keepValuePlaceholder@@";
    public static final String FORCE_NULL_PLACEHOLDER = "@@forceNullPlaceholder@@";

    public static final String COLL_PREFIX = "_coll_";
    public static final String OWNER_PREFIX = "_own_";

    private static String MOVE_AFTER = "move-after";
    private static String FOR_PROPERTY = "for-property";

    private Map completeParams;

    boolean bFast = false;

    public void setFast( boolean bFast )
    {
        this.bFast = bFast;
    }

    boolean bRetainPKColumn = false;

    public void setRetainPKColumn( boolean bRetainPKColumn )
    {
        this.bRetainPKColumn = bRetainPKColumn;
    }

    boolean bNoCollectionsOrOwners = false;

    public void setNoCollectionsOrOwners( boolean bNoCollectionsOrOwners )
    {
        this.bNoCollectionsOrOwners = bNoCollectionsOrOwners;
    }

    @Override
    public boolean storeParamTableWithEmptyValues()
    {
        return false;
    }

    @Override
    public Map getCompleteParamTable()
    {
        return completeParams;
    }

    @Override
    public void setCompleteParamTable( Map params )
    {
        completeParams = params;
    }

    @Override
    public Object getAnyParam( String name )
    {
        if( completeParams == null )
            return null;

        return completeParams.get( name );
    }

    private String requestUrl;

    public String getRequestUrl()
    {
        return requestUrl;
    }

    public void setRequestUrl( String url )
    {
        this.requestUrl = url;
    }

    protected DynamicPropertySet parameters;

    String propNameForMultipleInsert;
    Object []ownersForMultipleInsert;

    protected ArrayList collections = null;
    protected Be5Operation[]collectionOps = null;
    protected Map ownedFields = null;

//    @Override
//    public Operation []getCollectionOperations()
//    {
//        return collectionOps;
//    }
//
    protected Be5Operation[]ownerOps = null;
//    @Override
//    public Operation []getOwnerOperations()
//    {
//        return ownerOps;
//    }

    // supposed to return the same value as getParameters
    // but without re-creating the bean
    @Override
    public Object getStoredParameters()
    {
        return parameters;
    }

    protected Object[] getOwnersForMultipleInsert()
    {
        return ownersForMultipleInsert;
    }
    
    protected Map getPresetValuesForOwnerOperation(Map presetValues)
    {
        return Collections.EMPTY_MAP;
    }

    @Override
    public Object getParameters(Writer out, DatabaseService connector, Map presetValues ) throws Exception
    {
        //System.out.println( "*presetValues = " + presetValues );

        //parameters = (DynamicPropertySet) Utils.readTableBean( connector, entity, tcloneId ).clone();

        //System.out.println( "0 parameters = " + parameters );

        String pk = primaryKey;

//        boolean isFirstSearch = Utils.isEmpty( getAnyParam( HttpConstants.SEARCH_PARAM ) );
//        Object searchPresetsObj = getAnyParam( HttpConstants.SEARCH_PRESETS_PARAM );
//        //System.err.println( "searchPresetsObj = " + searchPresetsObj );
//        String searchPresets = "";
//        if( searchPresetsObj != null )
//            searchPresets += searchPresetsObj;
//        HashMap searchPresetsMap = new HashMap();
//        if( !Utils.isEmpty( searchPresets ) )
//        {
//            StringTokenizer st = new StringTokenizer( searchPresets, "," );
//            while( st.hasMoreTokens() )
//                searchPresetsMap.put( st.nextToken(), "dummy" );
//        }
//
//        Map newPresets = mapPresetsForTheDatabase( connector, parameters, presetValues );
//        if( !isFirstSearch )
//        {
//            for( Iterator entries = newPresets.entrySet().iterator(); entries.hasNext(); )
//            {
//                Map.Entry entry = ( Map.Entry )entries.next();
//                String name = ( String )entry.getKey();
//                if( searchPresetsMap.get( name ) == null )
//                    entries.remove();
//            }
//        }

        //System.out.println( "*newPresets = " + newPresets );
        //System.out.println( "*completeParams = " + completeParams );

        //System.out.println( "1 parameters = " + parameters );
        //applyMetaData( connector, entity, pk, parameters, newPresets, true, tcloneId );
        //System.out.println( "2 parameters = " + parameters );

        //Utils.ignoreComputedColumns( parameters, connector, entity );
//        if( !bFast && tcloneId != null )
//        {
//            for( DynamicProperty prop : parameters )
//            {
//                if( SQLHelper.IMPORT_DB_FIELDS.contains( prop.getName() ) )
//                {
//                    prop.setExpert( true );
//                }
//            }
//        }

        // set all values to NULLs or preset or default values
//        for( Iterator props = parameters.propertyIterator(); props.hasNext(); )
//        {
//            DynamicProperty prop = ( DynamicProperty ) props.next();
//            Object knownValue = newPresets.get( prop.getName() );
//
//            if( knownValue == null )
//            {
//                knownValue = prop.getAttribute( BeanInfoConstants.DEFAULT_VALUE );
//
//                String typeName = ( String )prop.getAttribute( JDBCRecordAdapter.DATABASE_TYPE_NAME );
//
//                if( knownValue instanceof String && typeName != null && connector.isPostgreSQL() &&
//                    ( "BIGSERIAL".equalsIgnoreCase( typeName ) || "SERIAL".equalsIgnoreCase( typeName ) ) &&
//                    ( ( String )knownValue ).startsWith( "nextval(" )
//                  )
//                {
//                    knownValue = null;
//                }
//            }
//            else
//            {
//                if( knownValue.getClass().isArray() )
//                {
//                    // this is necessary for multiple insert feature
//                    // and this should only happens for a single property
//                    // so we just store value make property expert and continue
//                    propNameForMultipleInsert = prop.getName();
//                    prop.setExpert( true );
//                    ownersForMultipleInsert = ( Object [] )knownValue;
//                    continue;
//                }
//                //System.out.println( "1 knownValue = " + knownValue );
//                knownValue = Utils.changeType( knownValue, prop.getType(), userInfo );
//                //System.out.println( "2 knownValue = " + knownValue );
//            }
//            prop.setValue( knownValue );
//        }

//        if( !bFast )
//        {
//            //System.out.println( "3 parameters = " + parameters );
//            addTagEditors( connector, platform, userInfo, entity, pk, parameters, newPresets, tcloneId );
//            //System.out.println( "4 parameters = " + parameters );
//            applyColumnDescriptions( connector, entity, pk, parameters, userInfo );
//            //System.out.println( "5 parameters = " + parameters );
//            addOwnerNamesForROProperties( connector, parameters );
//
//            addCategoryAttributes( connector, parameters, true );
//            //System.out.println( "6 parameters = " + parameters );
//        }

        // if preset value is an array and not tag list provided
        // set it to the preset value itself
//        for( Iterator entries = newPresets.entrySet().iterator(); entries.hasNext(); )
//        {
//            Map.Entry entry = ( Map.Entry )entries.next();
//            String name = ( String )entry.getKey();
//            DynamicProperty prop = parameters.getProperty( name );
//            if( prop == null )
//                continue;
//            Object val = entry.getValue();
//            if( val != null && val.getClass().isArray() &&
//                prop.getAttribute( TAG_LIST_ATTR ) == null )
//            {
//                prop.setAttribute( TAG_LIST_ATTR, val );
//            }
//        }

        // add fields from collections and owners
        if( bFast )
            return parameters;

        if( bNoCollectionsOrOwners )
            return parameters;

        ArrayList ownerProps = new ArrayList();
        ArrayList ownerOpsList = new ArrayList();

//*
        int propCount = 0;
        for( Iterator props = parameters.propertyIterator(); props.hasNext(); )
        {
            propCount++;
            DynamicProperty prop = ( DynamicProperty ) props.next();
            if( prop.isReadOnly() )
                continue;
            Object ref = prop.getAttribute( TABLE_REF );
            Object ext = prop.getAttribute( EXTERNAL_TAG_LIST );

            List ents;
//            if( ref instanceof String )
//            {
//                try
//                {
//                    String etype = Utils.getEntityType( connector, ( String )ref );
//                    if( ENTITY_TYPE_TABLE.equals( etype ) )
//                    {
//                        ents = Collections.singletonList( ref );
//                    }
//                    else
//                        continue;
//                }
//                catch( Exception exc )
//                {
//                    continue;
//                }
//            }
//            else if ( ext instanceof ArrayList )
//            {
//                ents = ( List )ext;
//            }
//            else
//                continue;

//            for( int i = 0; i < ents.size(); i++ )
//            {
//                String ent = ( String )ents.get( i );
//                if( ent.equals( entity ) )
//                    continue; // to avoid confusion
//                //System.out.println( "ent = " + ent );
//                Operation ownerOp = getInsertOwnerOperation( out,  connector, ent );
//                if( ownerOp == null )
//                    continue;
//                //System.out.println( "2ent = " + ent );
//                ownerOpsList.add( ownerOp );
//                ( ( InsertOperation )ownerOp ).setNoCollectionsOrOwners( true );
//                DynamicPropertySet opars = ( DynamicPropertySet )
//                    ownerOp.getParameters( out, connector, getPresetValuesForOwnerOperation(presetValues) );
//
//                for( Iterator oprops = opars.propertyIterator(); oprops.hasNext(); )
//                {
//                    DynamicProperty oprop = ( DynamicProperty ) oprops.next();
//                    String origName = oprop.getName();
//
//                    oprop.setAttribute( ORIG_PROPERTY_NAME_ATTR, origName );
//                    oprop.setAttribute( ORIG_PROPERTY_ENTITY_ATTR, ent );
//
//                    oprop.setName( OWNER_PREFIX + ent + "_" + prop.getName() + i + "_" + origName );
//
//                    oprop.setAttribute( MOVE_AFTER, new Integer( propCount ) );
//                    oprop.setAttribute( FOR_PROPERTY, prop );
//
//                    oprop.setAttribute( BeanInfoConstants.GROUP_ID, prop.getName() + i );
//                    oprop.setAttribute( BeanInfoConstants.GROUP_NAME, localizedMessage( "Insert into" ) + " " + localizedMessage( ent ) );
//                    oprop.setAttribute( BeanInfoConstants.GROUP_INITIALLY_CLOSED, Boolean.TRUE );
//
//                    if( !oprop.isCanBeNull() )
//                    {
//                        oprop.setAttribute( CONDITIONALLY_NOT_NULL, Boolean.TRUE );
//                    }
//                    oprop.setCanBeNull( true );
//
//                    if( !oprop.isHidden() && !oprop.isExpert() )
//                    {
//                        oprop.setValue( null );
//                    }
//
//                    ownerProps.add( oprop );
//                }
//            }
        }

        //System.out.println( "Step 2" );
        if( ownerOpsList.size() > 0 )
        {
            ownerOps = ( Be5Operation[] )ownerOpsList.toArray( new Be5Operation[0] );
            int prevBase = -1;
            int offset = 0;
            int nAdded = 0;
            int shift = 0;
            for( int i = 0; i < ownerProps.size(); i++ )
            {
                DynamicProperty prop = ( DynamicProperty ) ownerProps.get( i );
                parameters.add( prop );
                int base = ( ( Integer )prop.getAttribute( MOVE_AFTER ) ).intValue();
                if( prevBase != base )
                {
                    shift += nAdded;
                    nAdded = 0;
                    prevBase = base;
                    offset = 0;
                }
                parameters.moveTo( prop.getName(), base + shift + offset++ );
                nAdded++;

                //Do you want to have a brain damage? Look at operationPage.js :)
                DynamicProperty forProperty = ( DynamicProperty )prop.getAttribute( FOR_PROPERTY );
                if( !forProperty.isCanBeNull() )
                {
                    forProperty.setAttribute( CONDITIONALLY_NOT_NULL, Boolean.TRUE );
                }
                forProperty.setCanBeNull( true );
            }
        }
//*/

//        String myFromQuery = fromQuery;
//        if( fromQuery != null )
//        {
//            String queryEntity = getFromQueryEntity( connector );
//            // we are in the insert operation of collection invoked from parent query
//            if( queryEntity != null && !entity.equals( queryEntity ) )
//                myFromQuery = null;
//        }

//        if( myFromQuery == null )
//        {
//            try
//            {
//                myFromQuery = RedirectServlet.findRedirectQueryIdFor( connector, userInfo, entity );
//            }
//            catch( QRec.NoRecord nr )
//            {
//                return parameters;
//            }
//        }

        //Operation[] ops = getInsertCollectionOperations( out, connector, myFromQuery );

//        if( ops.length > 0 )
//        {
//            collectionOps = ops;
//            collections = new ArrayList();
//            for( int i = 0; i < ops.length; i++ )
//            {
//                collections.add( ops[ i ].getEntity() );
//            }
//
//            String tsql = "SELECT ";
//            tsql += " tr.tableFrom, tr.columnsFrom, tr.tableTo, tr.columnsTo ";
//            tsql += " FROM table_refs tr WHERE tr.tableFrom IN " + Utils.toInClause( collections, false );
//            tsql += " AND tr.tableTo IS NULL OR tr.tableTo = '" + Utils.safestr( connector, entity ) + "'";
//
//            ownedFields = Utils.readAsMap( connector, tsql, ReferencesMapsCache.getInstance() );
//
//            tsql = "SELECT trc.tableFrom AS tfc, trc.columnsFrom AS cfc, trp.columnsFrom AS cfp ";
//            tsql += " FROM table_refs trp, table_refs trc ";
//            tsql += " WHERE trp.tableTo = trc.tableTo AND trp.columnsTo = trc.columnsTo ";
//            tsql += "   AND trp.tableFrom = '" + Utils.safestr( connector, entity ) + "' ";
//            tsql += "   AND trc.tableFrom IN " + Utils.toInClause( collections, false );
//
//            Map transitives = Utils.readAsMap( connector, tsql, ReferencesMapsCache.getInstance() );
//
//            for( int i = 0; i < ops.length; i++ )
//            {
//                try
//                {
//                    Object[] refData = ( Object[] )ownedFields.get( ops[ i ].getEntity() );
//                    Object[] transitiveData = ( Object[] )transitives.get( ops[ i ].getEntity() );
//
//                    if( ops[ i ] instanceof InsertOperation )
//                    {
//                        ( ( InsertOperation )ops[ i ] ).setNoCollectionsOrOwners( true );
//                    }
//
//                    DynamicPropertySet cpars = ( DynamicPropertySet )
//                            ops[ i ].getParameters( out, connector, Collections.EMPTY_MAP );
//
//                    if( transitiveData != null )
//                    {
//                        // trying to find presets for the collection
//                        // since we used EMPTY_MAP in the previous statement
//                        HashMap collPresets = new HashMap();
//                        for( Iterator props = cpars.propertyIterator(); props.hasNext(); )
//                        {
//                            DynamicProperty prop = ( DynamicProperty )props.next();
//                            String origName = prop.getName();
//                            DynamicProperty tp = null;
//                            if( origName.equals( transitiveData[ 0 ] ) &&
//                                ( tp = parameters.getProperty( ( String )transitiveData[ 1 ] ) ) != null &&
//                                tp.isReadOnly() )
//                            {
//                                collPresets.put( origName, tp.getValue() );
//                            }
//                        }
//                        // if there are presets for the collection - get parameters again
//                        if( !collPresets.isEmpty() )
//                        {
//                            cpars = ( DynamicPropertySet )ops[ i ].getParameters( out, connector, collPresets );
//                        }
//                    }
//
//                    DynamicProperty owned = cpars.getProperty( ( String )refData[ 0 ] );
//                    if (owned == null) {
//                        owned = cpars.getProperty(connector.getAnalyzer()
//                                .getCaseCorrectedIdentifier(
//                                        ((String) refData[0])));
//                    }
//                    if( owned != null )
//                    {
//                        owned.setHidden( true );
//                    }
//
//
//                    int collPropCount = 0;
//                    DynamicProperty lastProp = null;
//                    for( DynamicProperty prop : cpars )
//                    {
//                        String origName = prop.getName();
//                        if( origName.startsWith( OWNER_PREFIX ) || origName.startsWith( COLL_PREFIX ) )
//                        {
//                            // prevent groups of included operations
//                            continue;
//                        }
//
//                        prop.setAttribute( ORIG_PROPERTY_NAME_ATTR, origName );
//                        prop.setAttribute( ORIG_PROPERTY_ENTITY_ATTR, ops[ i ].getEntity() );
//                        prop.setName( COLL_PREFIX + ops[ i ].getEntity() + "_" + origName );
//
//                        prop.setAttribute( BeanInfoConstants.GROUP_ID, "" + i );
//                        prop.setAttribute( BeanInfoConstants.GROUP_NAME, ops[ i ].getEntity() );
//                        prop.setAttribute( BeanInfoConstants.GROUP_INITIALLY_CLOSED, Boolean.TRUE );
//
//                        if( prop.getAttribute( TAG_LIST_ATTR ) != null && "classifications".equals( ops[ i ].getEntity() ) )
//                        {
//                            prop.setAttribute( TAG_LIST_ATTR, AddRemoveCategoryOperation.readCategoriesForEntity( this, connector, userInfo, entity ) );
//                        }
//
//                        DynamicProperty tp = null;
//                        if( transitiveData != null &&
//                            origName.equals( transitiveData[ 0 ] ) &&
//                            ( tp = parameters.getProperty( ( String )transitiveData[ 1 ] ) ) != null &&
//                            tp.isReadOnly() )
//                        {
//                            // Should we hide it?
//                            // prop.setExpert( true );
//
//                            prop.setReadOnly( true );
//                            prop.setValue( tp.getValue() );
//                        }
//                        else
//                        {
//                            if( !prop.isCanBeNull() )
//                            {
//                                prop.setAttribute( CONDITIONALLY_NOT_NULL, Boolean.TRUE );
//                            }
//                            prop.setCanBeNull( true );
//                            if( !prop.isHidden() && !prop.isExpert() )
//                            {
//                                prop.setValue( null );
//                            }
//                        }
//
//                        if( !prop.isReadOnly() && !prop.isHidden() && !prop.isExpert() )
//                        {
//                            lastProp = prop;
//                            collPropCount++;
//                        }
//
//                        parameters.add( prop );
//                    }
//                    if( collPropCount == 1 && lastProp.getAttribute( TAG_LIST_ATTR ) != null )
//                    {
//                        lastProp.setAttribute( MULTIPLE_SELECTION_LIST, Boolean.TRUE );
//                    }
//                }
//                catch( Exception e )
//                {
//                    //in case when one of N ops fails, let others execute
//                    Logger.error( cat, "Operation " + ops[ i ].getClass().getName() + " for entity " + ops[ i ].getEntity(), e );
//                }
//            }
//
//        }

        // Make possibility to add several records at once for simple collections like companies2territories
        ArrayList<DynamicProperty> multHackArray = new ArrayList<DynamicProperty>();
        for( DynamicProperty dp : parameters )
        {
            if( dp.isHidden() )
                continue; 
            if( dp.isExpert() )
                continue; 
            multHackArray.add( dp );
        }

        if( multHackArray.size() == 2 &&
            ( 
               multHackArray.get( 0 ).isReadOnly() && multHackArray.get( 1 ).getAttribute( TAG_LIST_ATTR ) != null ||
               multHackArray.get( 1 ).isReadOnly() && multHackArray.get( 0 ).getAttribute( TAG_LIST_ATTR ) != null 
            )
          )
        {
            if( multHackArray.get( 0 ).isReadOnly() && multHackArray.get( 1 ).getAttribute( EXTERNAL_TAG_LIST ) == null )
            {
                multForcedProperty = multHackArray.get( 1 );
            }
            else if( multHackArray.get( 0 ).getAttribute( EXTERNAL_TAG_LIST ) == null )
            {
                multForcedProperty = multHackArray.get( 0 );
            }
            if( multForcedProperty != null )
            {
                multForcedProperty.setAttribute( MULTIPLE_SELECTION_LIST, true );
            } 
        }

        //System.out.println( "last parameters = " + parameters );
        return parameters;
    }

    DynamicProperty multForcedProperty = null;

//    public static String safeValue( DatabaseService connector, DynamicProperty prop )
//        throws java.io.UnsupportedEncodingException,
//               java.security.GeneralSecurityException,
//               java.io.IOException
//    {
//        Object val = prop.getValue();
//
//        if( val instanceof Wrapper )
//        {
//             val = ( (Wrapper)val ).unwrap();
//        }
//
//        String value;
//        if( val == null )
//        {
//            value = "";
//        }
//        else if( val instanceof java.sql.Timestamp )
//        {
//            value = new SimpleDateFormat( connector.isOracle() ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm:ss.SSS" ).format( val );
//        }
//        else if( val instanceof java.sql.Time )
//        {
//            value = new SimpleDateFormat( "HH:mm:ss" ).format( val );
//        }
//        else if( val instanceof Date )
//        {
//            java.sql.Date sqlDate = new java.sql.Date( ( ( Date )val ).getTime() );
//            value = sqlDate.toString();
//        }
//        else if( val instanceof Calendar )
//        {
//            value = new SimpleDateFormat( connector.isOracle() ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm:ss.SSS" ).format( ( ( Calendar )val ).getTime() );
//        }
//        else if( val instanceof Boolean ) // BIT
//        {
//            value = Boolean.TRUE.equals( val ) ? "1" : "0";
//        }
//        // must be encrypted?
//        else if( prop.getName().toLowerCase().startsWith( ENCRYPT_COLUMN_PREFIX ) )
//        {
//            value = CryptoUtils.encrypt( val.toString() );
//            prop.setAttribute( PASSWORD_FIELD, Boolean.TRUE );
//        }
//        else
//        {
//            value = val.toString();
//
//            if( Number.class.isAssignableFrom( prop.getType() ) )
//            {
//                value = Utils.subst( value, ",", "." );
//            }
//
///*          String orig = value;
//            String sizeStr = ( String )prop.getAttribute( COLUMN_SIZE_ATTR );
//            String encoding = connector.getEncoding();
//            if( value != null && value.length() > 0 && sizeStr != null && encoding != null )
//            {
//                if( connector.isOracle() )
//                {
//                    // truncate value since it causes exception
//                    int size = Integer.parseInt( sizeStr );
//                    byte bytes[] = value.getBytes( encoding );
//                    int length = size;
//                    if( length > bytes.length )
//                        length = bytes.length;
//                    value = new String( bytes, 0, length, encoding );
//
//                    // in case we got into middle of multi-byte char
//                    // From Google
//                    // Unicode 65533 is a substitute character for use when
//                    // a character is found that can't be output in the selected encoding
//                    char last = value.charAt( value.length() - 1 );
//                    if( (int)last == 65533 )
//                    {
//                        value = value.substring( 0, value.length() - 1 );
//                    }
//                }
//            }
//*/
//        }
//
//        if( "".equals( value ) && prop.isCanBeNull() )
//            value = null;
//
//        if( value == null && String.class.equals( prop.getType() ) && !prop.isCanBeNull() )
//            value = "";
//
//        return value;
//    }

    public String generateSQL( DatabaseService connector, boolean bSkipNulls ) throws Exception
    {
        //DatabaseAnalyzer analyzer = connector.getAnalyzer();

        boolean bFirstValue = true;
        String pk = primaryKey;
        StringBuffer sql = new StringBuffer();
        ArrayList<String> columnsUsed = new ArrayList<String>();
        sql.append( "INSERT INTO " );
//        sql.append( analyzer.quoteIdentifier( entity + Utils.ifNull( tcloneId, "" ) ) ).append( " ( " );
//        for( DynamicProperty prop : parameters )
//        {
//            // collection's or owner property
//            if( prop.getName().startsWith( OWNER_PREFIX ) )
//                continue;
//            if( prop.getName().startsWith( COLL_PREFIX ) )
//                continue;
//
//            if( Boolean.TRUE.equals( prop.getAttribute( JDBCRecordAdapter.COMPUTED_COLUMN ) ) )
//                continue;
//
//            boolean isUtilsInsert = bFast && completeParams != null && completeParams.get( prop.getName() ) != null;
//            if( !isUtilsInsert && Boolean.TRUE.equals( prop.getAttribute( WebFormPropertyInspector.PSEUDO_PROPERTY ) ) )
//                continue;
//
//            if( prop.getAttribute( CATEGORY_ATTRIBUTE_ID ) != null )
//                continue;
//
//            // Ignore auto-increment columns but check first if PK is indeed SET
//            if( Boolean.TRUE.equals( prop.getAttribute( JDBCRecordAdapter.AUTO_IDENTITY ) ) && bRetainPKColumn && prop.getValue() == null )
//            {
//                bRetainPKColumn = false;
//            }
//            if( Boolean.TRUE.equals( prop.getAttribute( JDBCRecordAdapter.AUTO_IDENTITY ) ) && !bRetainPKColumn )
//                continue;
//
//            String value = safeValue( connector, prop );
//
//            String colName = prop.getName();
//
//            if( value == null && bSkipNulls && !SQLHelper.SYSTEM_FIELDS.contains( colName ) )
//                 continue;
//
//            columnsUsed.add( colName );
//
//            if( !bFirstValue )
//                sql.append( ',' );
//            sql.append( connector.NL() );
//            sql.append( "          " ).append( analyzer.quoteIdentifier( colName ) );
//            bFirstValue = false;
//        }
//        sql.append( connector.NL() );
//        sql.append( ") VALUES ( " );
//
//        bFirstValue = true;
//        for( DynamicProperty prop : parameters )
//        {
//            String colName = prop.getName();
//            if( !columnsUsed.contains( colName ) )
//            {
//                continue;
//            }
//
//            String value = safeValue( connector, prop );
//
//            if( !bFirstValue )
//                sql.append( ',' );
//            sql.append( connector.NL() );
//            sql.append( "          " );
//
//            // Oracle trick for auto-generated IDs
//            if( connector.isOracle() && colName.equalsIgnoreCase( pk ) )
//            {
//                if( entity.equalsIgnoreCase( value ) || JDBCRecordAdapter.AUTO_IDENTITY.equals( value ) )
//                {
//                    sql.append( "beIDGenerator.NEXTVAL" );
//                }
//                else if( ( entity + "_" + pk + "_seq" ).equalsIgnoreCase( value ) )
//                {
//                    sql.append( value ).append( ".NEXTVAL" );
//                }
//                else
//                {
//                    //in case of not autoincremented PK
//                    justAddValueToQuery( connector, entity, prop, value, sql );
//                }
//            }
//            else if( connector.isOracle() && !connector.isOracle8() &&
//                     "CLOB".equals( prop.getAttribute( JDBCRecordAdapter.DATABASE_TYPE_NAME ) ) )
//            {
//                sql.append( OracleDatabaseAnalyzer.makeClobValue( connector, value ) );
//            }
//            else if( colName.equalsIgnoreCase( WHO_INSERTED_COLUMN_NAME ) && !userInfo.isGuest() )
//            {
//                sql.append( "'" ).append( Utils.safestr( connector, userInfo.getUserName() ) ).append( "'" );
//            }
//            else if( colName.equalsIgnoreCase( WHO_MODIFIED_COLUMN_NAME ) && !userInfo.isGuest() )
//            {
//                sql.append( "'" ).append( Utils.safestr( connector, userInfo.getUserName() ) ).append( "'" );
//            }
//            else if( colName.equalsIgnoreCase( CREATION_DATE_COLUMN_NAME ) )
//            {
//                sql.append( analyzer.getCurrentDateTimeExpr() );
//            }
//            else if( colName.equalsIgnoreCase( MODIFICATION_DATE_COLUMN_NAME ) )
//            {
//                sql.append( analyzer.getCurrentDateTimeExpr() );
//            }
//            else if( colName.equalsIgnoreCase( IS_DELETED_COLUMN_NAME ) )
//            {
//                sql.append( "'no'" );
//            }
//            else if( DBMS_DATE_PLACEHOLDER.equals( value )  )
//            {
//                sql.append( analyzer.getCurrentDateExpr() );
//            }
//            else if( DBMS_DATETIME_PLACEHOLDER.equals( value ) )
//            {
//                sql.append( analyzer.getCurrentDateTimeExpr() );
//            }
//            else if( InsertOperation.FORCE_NULL_PLACEHOLDER.equals( value ) )
//            {
//                sql.append( "NULL" );
//            }
//
//            else if( Boolean.TRUE.equals( prop.getAttribute( PUT_DBMS_DATE_PLACEHOLDER_FLAG ) ) &&
//                     ( value == null || value != null && value.equals( prop.getAttribute( BeanInfoConstants.DEFAULT_VALUE ) ) )
//                   )
//            {
//                sql.append( analyzer.getCurrentDateExpr() );
//            }
//            else if( Boolean.TRUE.equals( prop.getAttribute( PUT_DBMS_DATETIME_PLACEHOLDER_FLAG ) ) &&
//                     ( value == null || value != null && value.equals( prop.getAttribute( BeanInfoConstants.DEFAULT_VALUE ) ) )
//                   )
//            {
//                sql.append( analyzer.getCurrentDateTimeExpr() );
//            }
//
//            else if( colName.equalsIgnoreCase( IP_INSERTED_COLUMN_NAME ) && userInfo.getRemoteAddr() != null )
//            {
//                sql.append( "'" ).append( Utils.safestr( connector, userInfo.getRemoteAddr() ) ).append( "'" );
//            }
//            else if( colName.equalsIgnoreCase( IP_MODIFIED_COLUMN_NAME ) && userInfo.getRemoteAddr() != null )
//            {
//                sql.append( "'" ).append( Utils.safestr( connector, userInfo.getRemoteAddr() ) ).append( "'" );
//            }
//            else
//            {
//                justAddValueToQuery( connector, entity, prop, value, sql );
//            }
//            bFirstValue = false;
//        }
//        sql.append( connector.NL() );
        sql.append( ")" );
        return sql.toString();
    }

    public static interface StringHacker
    {
        // 'Hello' -> _koi8r'Hello'
        String hackStringBeforeSendingToDB(String entity, String value);
    };

//    static void justAddValueToQuery( DatabaseService connector, String entity, DynamicProperty prop, String value, StringBuffer sql )
//        throws Exception
//    {
//        if( value != null )
//        {
//            if( Boolean.TRUE.equals( prop.getAttribute( JDBCRecordAdapter.NUMERIC_COLUMN ) ) )
//            {
//                sql.append( Utils.safestr( connector, value ) );
//            }
//            else if( connector.isOracle() && FragmentContextSupport.isODBCDateStr( value ) &&
//                Arrays.asList( "DATE", "TIMESTAMP" ).contains( prop.getAttribute( JDBCRecordAdapter.DATABASE_TYPE_NAME ) ) )
//            {
//                // ugly hack because Oracle somtimes looses date format stored in connection
//                sql.append( "TO_DATE('" ).append( value ).append( "','YYYY-MM-DD')" );
//            }
//            else if( connector.isSQLServer() &&
//                 ( FragmentContextSupport.isODBCDateStr( value ) || FragmentContextSupport.isODBCDateTimeStr( value ) ) &&
//                "DATETIME".equalsIgnoreCase( ( String )prop.getAttribute( JDBCRecordAdapter.DATABASE_TYPE_NAME ) ) )
//            {
//                // ugly hack for SQL Server 2012 since it doesn't convert 'YYYY-MM-DD' to DATETIME sometimes
//                sql.append( "CONVERT( DATETIME, '" ).append( value ).append( "', 120 )" );
//            }
//            else
//            {
//                String hackVal = Utils.safestr( connector, value, true );
//                String hackClassName = Utils.getSystemSetting( connector, "STRING_HACKER_CLASS" );
//                if( hackClassName != null )
//                {
//                    StringHacker hacker = Class.forName( hackClassName ).asSubclass( StringHacker.class ).newInstance();
//                    hackVal = hacker.hackStringBeforeSendingToDB( entity, hackVal );
//                }
//                sql.append( hackVal );
//            }
//        }
//        else
//        {
//            sql.append( "NULL" );
//        }
//    }

    protected String lastInsertID;

    public String getLastInsertID() { return lastInsertID; }

//    private void singleInsert( MessageHandler output, DatabaseService connector, Operation.InterruptMonitor interruptChecker ) throws Exception
//    {
//        if( ownerOps != null )
//        {
//            Operation[] ops = ownerOps;
//            for( int i = 0; i < ops.length; i++ )
//            {
//                 DynamicPropertySet cpars = ( DynamicPropertySet )ops[ i ].getStoredParameters();
//                 if( cpars == null )
//                 {
//                     continue;
//                 }
//                 boolean allNulls = true;
//                 DynamicProperty forProperty = null;
//                 for( DynamicProperty prop : cpars )
//                 {
//                     parameters.remove( prop.getName() );
//                     prop.setName( ( String )prop.getAttribute( ORIG_PROPERTY_NAME_ATTR ) );
//                     if( forProperty == null )
//                         forProperty = ( DynamicProperty )prop.getAttribute( FOR_PROPERTY );
//
//                     if( prop.isHidden() || prop.isExpert() )
//                     {
//                         continue;
//                     }
//
//                     Object propVal = prop.getValue();
//                     if( allNulls && propVal != null && !prop.isReadOnly() )
//                         allNulls = false;
//                 }
//
//                 if( allNulls )
//                     continue;
//
//                 if( ops[ i ] instanceof OfflineOperation )
//                 {
//                     ( ( OfflineOperation )ops[ i ] ).invoke( output, connector );
//                 }
//                 else
//                 {
//                     ops[ i ].invoke( new StringWriter(), connector );
//                 }
//
//                 String grsql = "SELECT tr.tableFrom ";
//                 grsql += " FROM table_refs tr ";
//                 grsql += " WHERE tr.tableTo IS NULL AND tr.tableFrom = '" + Utils.safestr( connector, entity ) + "' ";
//                 grsql += " AND tr.columnsFrom IN ('" + forProperty.getName() + "')";
//
//                 Object[] genericRefs = Utils.readAsArray( connector, grsql, ReferencesArraysCache.getInstance() );
//                 boolean isGeneric = genericRefs != null && genericRefs.length > 0;
//
//                 String lid = ( ( InsertOperation )ops[ i ] ).getLastInsertID();
//
//                 if( isGeneric )
//                     forProperty.setValue( ops[ i ].getEntity() + "." + lid );
//                 else
//                     forProperty.setValue( Utils.changeType( lid, forProperty.getType(), userInfo ) );
//
//                 break;
//            }
//        }
//
//        String sql = generateSQL( connector, false );
//        if( userInfo.isAdmin() )
//        {
//            output.message( localizedMessage( "SQL statement for inserting into" ) +
//                " <b>" + entity + "</b>" );
//            String printSQL = Utils.subst( sql, "<", "&lt;", "" );
//            printSQL = Utils.subst( printSQL, ">", "&gt;", "" );
//            if( PLATFORM_WML.equals( platform ) )
//                printSQL = StringUtils.XMLize( printSQL );
//            output.message( "<pre>" + printSQL + "</pre>" );
//        }
//
//
//        String lid = null;
//
//        // when primary keys are not auto generated - the value must be entered in parameters
//        // in this case we will use this value as lastInsertID
//        DynamicProperty pkProp = parameters.getProperty( primaryKey );
//        if( pkProp != null )
//        {
//            String defValue = null;
//            if( pkProp.getAttribute( BeanInfoConstants.DEFAULT_VALUE ) != null )
//            {
//                defValue = pkProp.getAttribute( BeanInfoConstants.DEFAULT_VALUE ).toString();
//            }
//            //System.out.println( "defValue = " + defValue );
//            if( !connector.isOracle() && !Boolean.TRUE.equals( pkProp.getAttribute( JDBCRecordAdapter.AUTO_IDENTITY ) ) ||
//                connector.isOracle() &&
//                   !( defValue != null &&
//                      (
//                      defValue.equalsIgnoreCase( entity ) ||
//                      JDBCRecordAdapter.AUTO_IDENTITY.equals( defValue ) ||
//                      ( entity + "_" + primaryKey + "_seq" ).equalsIgnoreCase( defValue )
//                      )
//                   )
//              )
//            {
//                connector.executeUpdate( sql );
//                lid = ( pkProp.getValue() != null ) ? pkProp.getValue().toString() : "";
//                //System.out.println( "lid = " + lid );
//            }
//        }
//
//        try
//        {
//            if( lid == null )
//                lid = connector.executeInsert( sql );
//        }
//        catch( SQLException se )
//        {
//            String msg = "" + se.getMessage() + ":\n";
//            msg += sql;
//            throw new SQLException( msg, se );
//        }
//
//        output.message( "1 " + localizedMessage( "records were inserted into" ) + " <i>" + entity + "</i>" );
//
//        if( !Utils.isEmpty( lid ) && !"0".equals( lid ) )
//        {
//            lastInsertID = lid;
//            output.message( localizedMessage( "ID of new record is" ) + " <i>" + lid + "</i>" );
//            saveCategoryAttributes( connector, parameters, new String[] { lid }, true );
//        }
//
//        if( collectionOps == null )
//            return;
//
//        boolean bDoOwnerSubstitute = doOwnerSubstitute( connector, entity );
//
//        Operation[] ops = collectionOps;
//        for( int i = 0; i < ops.length; i++ )
//        {
//             boolean allNulls = true;
//             ArrayList arrVals = new ArrayList();
//             DynamicPropertySet cpars = ( DynamicPropertySet )ops[ i ].getStoredParameters();
//             if( cpars == null )
//             {
//                 continue;
//             }
//             for( DynamicProperty prop : cpars )
//             {
//                 prop.setName( ( String )prop.getAttribute( ORIG_PROPERTY_NAME_ATTR ) );
//
//                 if( prop.isHidden() || prop.isExpert() )
//                 {
//                     continue;
//                 }
//
//                 Object propVal = prop.getValue();
//                 if( allNulls && propVal != null && !prop.isReadOnly() )
//                     allNulls = false;
//                 if( propVal instanceof String[] )
//                     arrVals.add( prop );
//             }
//
//             if( allNulls )
//                 continue;
//
//            Object []refData = ( Object[] )ownedFields.get( ops[ i ].getEntity() );
//
//            DynamicProperty owned = cpars.getProperty( ( String )refData[ 0 ] );
//            if( owned == null )
//                owned = cpars.getProperty( connector.getAnalyzer().getCaseCorrectedIdentifier( ( String )refData[ 0 ] ) );
//
//             boolean isGeneric = ( ( Object[] )ownedFields.get( ops[ i ].getEntity() ) )[ 1 ] == null;
//             if( owned != null )
//             {
//                 owned.setHidden( false );
//                 if( isGeneric && !bDoOwnerSubstitute )
//                     owned.setValue( entity + "." + lid );
//                 else
//                     owned.setValue( Utils.changeType( lid, owned.getType(), userInfo ) );
//             }
//
//             try
//             {
//                 if( arrVals.isEmpty() )
//                 {
//                     if( ops[ i ] instanceof OfflineOperation )
//                     {
//                         ( ( OfflineOperation )ops[ i ] ).invoke( output, connector );
//                     }
//                     else
//                     {
//                         ops[ i ].invoke( new StringWriter(), connector );
//                     }
//                 }
//                 else
//                 {
//                     for( int np = 0; np < arrVals.size(); np++ )
//                     {
//                          DynamicProperty prop = ( DynamicProperty )arrVals.get( np );
//                          String []items = ( String [] )prop.getValue();
//                          for( int vi = 0; vi < items.length; vi++ )
//                          {
//                               prop.setValue( items[ vi ] );
//                               if( ops[ i ] instanceof OfflineOperation )
//                               {
//                                   ( ( OfflineOperation )ops[ i ] ).invoke( output, connector );
//                               }
//                               else
//                               {
//                                   ops[ i ].invoke( new StringWriter(), connector );
//                               }
//                          }
//                     }
//                 }
//             }
//             catch( Exception exc )
//             {
//                 output.message( localizedMessage( "Error when storing record into" ) + " <b>" +
//                      ops[ i ].getEntity() + "</b>: <i>" + exc.getMessage() + "</i>" );
//             }
//        }
//
//    }

    @Override
    public void invoke( Writer out, DatabaseService connector )
            throws Exception
    {
//        if( multForcedProperty != null && multForcedProperty.getValue() != null )
//        {
//            Object []ret = ( Object [] )multForcedProperty.getValue();
//            int i = 0;
//            for( Object value : ret )
//            {
//                if( i++ > 0 )
//                    output.message( "" );
//                multForcedProperty.setValue( Utils.changeType( value, multForcedProperty.getType(), userInfo ) );
//                singleInsert( output, connector, interruptChecker );
//            }
//        }
//        else if( ownersForMultipleInsert != null )
//        {
//            for( int i = 0; i < ownersForMultipleInsert.length; i++ )
//            {
//                if( i > 0 )
//                    output.message( "" );
//                DynamicProperty prop = parameters.getProperty( propNameForMultipleInsert );
//                Object val = ownersForMultipleInsert[ i ];
//                val = Utils.changeType( val, prop.getType(), userInfo );
//                prop.setValue( val );
//                singleInsert( output, connector, interruptChecker );
//            }
//        }
//        else
//        {
//            singleInsert( output, connector, interruptChecker );
//
//            if( !Utils.isEmpty( category ) )
//            {
//                if( !Utils.isEmpty( lastInsertID ) )
//                {
//                    boolean bNewApproach = Utils.columnExists( connector, "classifications", "entity" );
//
//                    StringBuffer csql = new StringBuffer( "INSERT INTO classifications( " );
//                    if( connector.isOracle() )
//                        csql.append( "ID, " );
//                    if( bNewApproach )
//                        csql.append( "entity, " );
//                    csql.append( "categoryID, recordID ) VALUES( " );
//                    if( connector.isOracle() )
//                        csql.append( "beIDGenerator.NEXTVAL, " );
//                    if( bNewApproach )
//                    {
//                        csql.append( "'" ).append( Utils.safestr( connector, entity ) ).append( "', " )
//                            .append( category ).append( ", '" )
//                            .append( lastInsertID ).append( "' )" );
//                    }
//                    else
//                    {
//                        csql.append( category ).append( ", '" ).append( Utils.safestr( connector, entity ) )
//                            .append( "." ).append( lastInsertID ).append( "' )" );
//                    }
//                    int updateCount = connector.executeUpdate( csql.toString() );
//                    if( updateCount == 1 )
//                    {
//                        output.message( localizedMessage( "New record" ) + " (ID = " + lastInsertID + ") " +
//                                   localizedMessage( "was assigned to category" ) + " <i>" + category + "</i>" );
//                    }
//                }
//            }
//        }
//
//        clearAffectedCaches( connector, parameters );
    }
}
