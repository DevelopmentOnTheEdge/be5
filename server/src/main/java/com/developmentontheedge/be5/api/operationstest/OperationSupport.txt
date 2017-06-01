package com.developmentontheedge.be5.api.operationstest;

import com.developmentontheedge.be5.api.operationstest.analyzers.DatabaseAnalyzer;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.components.ApplicationInfoComponent;
import com.developmentontheedge.be5.env.ServerModules;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.base.BeCaseInsensitiveCollection;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.io.Writer;
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class OperationSupport implements Be5Operation, DatabaseConstants
{
    public SqlService db = ServerModules.getServiceProvider().getSqlService();

    private static final Logger log = Logger.getLogger(OperationSupport.class.getName());
    public static final String TAG_DELIMITER = "\u0000";
    private Status result = Status.FINISHED;

    private String name;
    private String origName;

    protected ApplicationInfoComponent.ApplicationInfo appInfo;

    /**
     * type of the page (HTML, WML, XML, XHTML, cHTML, Swing, calculate)
     */
    protected String platform;
    /**
     * user info
     */
    protected UserInfo userInfo;
    /**
     * entity name
     */
    protected String entity;
    /**
     * primary key column name for the entity
     */
    protected String primaryKey;
    /**
     * selected records, as parameters for the query
     */
    protected String[] records;
    /**
     * query id (from the table "queries") for editing with the operation
     */
    protected String fromQuery;
    /**
     * operation category
     */
    protected String category;

    //protected InterruptMonitor interruptChecker;

    /**
     * ID of cloned table ( persons -> persons123 )
     */
    protected String tcloneId;

    /**
     * ID of operationLogs table ( if logging enabled )
     */
    private String operLogId;

    //private EntityAccess<EntityModel<RecordModel>> database;
    
    /**
     * Initialize protected fields of the class
     *
     * @param platform
     * @param ui
     * @param entity entity name
     * @param primaryKey
     * @param records
     * @param fromQuery
     * @param category
     */
    
    public void initialize( String platform, UserInfo ui, String entity, String primaryKey, String[] records, String fromQuery, String category  )
    {
        initialize( platform, ui, entity, primaryKey, records, fromQuery, category, null );
    }

    @Override
    public void initialize( String platform, UserInfo ui, String entity, String primaryKey, String[] records, String fromQuery, String category, String tcloneId  )
    {
        this.platform = platform;
        this.userInfo = ui;
        this.entity = entity;
        this.primaryKey = primaryKey;
        this.records = records;
        this.fromQuery = fromQuery;
        this.category = category;
        this.tcloneId  = tcloneId ;
    }


    private SessionAdapter sessionAdapter;

    public SessionAdapter getSessionAdapter()
    {
        return sessionAdapter;
    }

    /**
     * Set SessionAdapter
     *
     * @param sa SessionAdapter
     */
    @Override
    public void setSessionAdapter( SessionAdapter sa )
    {
        sessionAdapter = sa;
    }

    /**
     * Get session variable by name if current sessionAdapter is not null
     *
     * @param name session variable name
     * @return session variable value
     */
    @Override
    public Object getSessionVar( String name )
    {
        if( sessionAdapter == null )
        {
            return null;
        }

        return sessionAdapter.getVar( name );
    }

    /**
     * Set session variable by name if current sessionAdapter is not null
     *
     * @param name session variable name
     * @param value session variable value
     */
    @Override
    public void setSessionVar( String name, Object value )
    {
        if( sessionAdapter == null )
        {
            return;
        }

        sessionAdapter.setVar( name, value );
    }

    /**
     * Remove session variable by name if current sessionAdapter is not null
     *
     * @param name session variable name for remove
     */
    @Override
    public void removeSessionVar( String name )
    {
        if( sessionAdapter == null )
        {
            return;
        }

        sessionAdapter.removeVar( name );
    }

//    @Override
//    public boolean isSplitParamOperation()
//    {
//        return this instanceof SplitParamOperation;
//    }


    public boolean isDisableCancel()
    {
        return false; 
    }

//    @Override
//    public ApplicationInfoComponent.ApplicationInfo getAppInfo()
//    {
//        return appInfo;
//    }
//
//    /**
//     * Set application info
//     *
//     * @param ai application info
//     */
//    @Override
//    public void setAppInfo( ApplicationInfoComponent.ApplicationInfo ai )
//    {
//        this.appInfo = ai;
//    }

//    private boolean topLevel = true;
//
//    @Override
//    public boolean isTopLevel()
//    {
//        return topLevel;
//    }
//
//    @Override
//    public void setTopLevel( boolean value )
//    {
//        topLevel = value;
//    }
//
//    @Override
//    public Map getCustomLocalization( DatabaseService connector ) throws Exception
//    {
//        return null;
//    }
//
//    @Override
//    public String getTcloneId()
//    {
//        return tcloneId;
//    }

    /**
     * Inheritors of this method can override parameters list for the current operation.
     *
     * @param out unused parameter, can be null
     * @param connector unused parameter, can be null
     * @param presetValues unused parameter, can be null
     * @throws Exception
     * @return null
     */
    @Override
    public DynamicPropertySet getParameters( Writer out, DatabaseService connector, Map<String, String> presetValues )
            throws Exception
    {
        return null;
    }

    @Override
    public Object getStoredParameters() {
        return null;
    }

    /**
     * Inheritors of this method may override actions that are performed as a result of this operation.
     *
     * @param out unused parameter, can be null
     * @param connector unused parameter, can be null
     */
    @Override
    public void invoke( Writer out, DatabaseService connector )
            throws Exception
    {
    }

//    /**
//     * Returns entity name for which the operation query is binded to.
//     *
//     * @param connector connector to the DB
//     * @return name entity if fromQuery not empty or fromQuery not equal symbol zero.
//     * @throws Exception
//     */
//    protected String getFromQueryEntity( DatabaseService connector )
//            throws Exception
//    {
//        if( Utils.isEmpty( fromQuery ) || "0".equals( fromQuery ) )
//        {
//            return null;
//        }
//
//        return new QueryText( true, connector, fromQuery ).tableName;
//    }
//
//    /**
//     * Reads record from entity by index in 'records' array
//     *
//     * @param connector connector to the DB
//     * @param i number record
//     * @return value of type DynamicPropertySet
//     * @throws Exception
//     */

//    public DynamicPropertySet getRecord(DatabaseService connector, int i )
//            throws Exception
//    {
//        String pk = primaryKey;
//        if( pk == null )
//        {
//            pk = Utils.findPrimaryKeyName( connector, entity );
//        }
//        boolean isNumeric = Utils.isNumericColumn( connector, entity, pk );
//        String sql = "SELECT * FROM " +  connector.getAnalyzer().quoteIdentifier( entity + Utils.ifNull( tcloneId, "" ) ) +
//                " WHERE " +  connector.getAnalyzer().quoteIdentifier( pk ) +
//                " IN " + Utils.toInClause( Collections.singletonList( records[ i ] ), isNumeric );
//
//        return new QRec( connector, sql );
//    }

    @Override
    public String[] getRecordIDs()
    {
        return records;
    }

//    /**
//     * Reads all record from entity listed in 'records' array
//     *
//     * @param connector connector to the DB
//     * @return value type array of DynamicPropertySet
//     * @throws Exception
//     */
//    @Override
//    public DynamicPropertySet[] getRecords( DatabaseService connector )
//            throws Exception
//    {
//        ArrayList<DynamicPropertySet> al = new ArrayList<DynamicPropertySet>( records.length );
//        for( int i = 0; i < records.length; i++ )
//            al.add( getRecord( connector, i ) );
//        return al.toArray( new DynamicPropertySet[ 0 ] );
//    }

    public static Object getPresetObject( Map presets, String name )
    {
        Object ret = presets.get( name );
        
        if( ret == null )
        {
            ret = presets.get( name.toUpperCase() );
        }
        if( ret == null )
        {
            ret = presets.get( name.toLowerCase() );
        }
        return ret;
    }


    public static String getPreset( Map presets, String name )
    {
        Object ret = getPresetObject( presets, name );
        if( ret instanceof Object[] )
        {
            ret = ( ( Object[] )ret )[ 0 ]; 
        }
        return ( String )ret;
    }

//    /**
//     * Get record for user name from current userInfo
//     *
//     * @param connector
//     * @return value of type DynamicPropertySet
//     * @throws Exception
//     */
//    public DynamicPropertySet getUserRecord( DatabaseService connector )
//            throws Exception
//    {
//        return getUserRecord( connector, userInfo.getUserName() );
//    }

//    /**
//     * Get record for userName
//     *
//     * @param connector
//     * @param userName
//     * @return value of type DynamicPropertySet
//     * @throws Exception
//     */
//    public DynamicPropertySet getUserRecord( DatabaseService connector, String userName )
//            throws Exception
//    {
//        String sql = "SELECT * FROM users " +
//                " WHERE user_name = '" + Utils.safestr( connector, userName ) + "'";
//        return new JDBCRecordAdapterAsQuery( connector, sql );
//    }
//
//    /**
//     * Returns true, if column in specified entity can contains numeric data.
//     * </br>Otherwise returns false.
//     * </br>Numeric data: BIGINT, DECIMAL, DOUBLE, FLOAT, INTEGER, NUMERIC, REAL, SMALLINT, TINYINT.
//     *
//     * @param connector connector to the DB
//     * @param entity entity name
//     * @param column column name
//     * @return if column is numeric type.
//     * @throws SQLException
//     */
//
//    static boolean isNumericColumn( DatabaseService connector, String entity, String column )
//            throws SQLException
//    {
//        if( column == null )
//        {
//            return false;
//        }
//
//        boolean isNumeric = false;
//        RSWrapper rs = null;
//        Connection conn = null;
//
//        try
//        {
//            conn = connector.getConnection();
//
//            rs = Utils.getEntityColumns( connector, conn, entity );
//            while( rs.next() )
//            {
//                String cmpCol = rs.getString( 4 );
//                if( column.equalsIgnoreCase( cmpCol ) )
//                {
//                    short type = rs.getShort( /*"DATA_TYPE"*/ 5 );
//                    isNumeric =
//                            type == Types.BIGINT ||
//                            type == Types.DECIMAL ||
//                            type == Types.DOUBLE ||
//                            type == Types.FLOAT ||
//                            type == Types.INTEGER ||
//                            type == Types.NUMERIC ||
//                            type == Types.REAL ||
//                            type == Types.SMALLINT ||
//                            type == Types.TINYINT;
//                    break;
//                }
//            }
//        }
//        finally
//        {
//            connector.releaseConnection( conn );
//        }
//
//        return isNumeric;
//    }
//
//    /**
//     * Returns true, if column in specified entity can contains date data.
//     * </br>Otherwise returns false.
//     * </br>Date data: DATE, TIME, TIMESTAMP.
//     *
//     * @param connector connector to the DB
//     * @param entity entity name
//     * @param column column name
//     * @return if column is date type.
//     * @throws SQLException
//     */
//    static boolean isDateColumn( DatabaseService connector, String entity, String column )
//            throws SQLException
//    {
//        if( column == null )
//        {
//            return false;
//        }
//
//        boolean isDateTime = false;
//        RSWrapper rs = null;
//        Connection conn = null;
//
//        try
//        {
//            conn = connector.getConnection();
//
//            rs = Utils.getEntityColumns( connector, conn, entity );
//            while( rs.next() )
//            {
//                String cmpCol = rs.getString( 4 );
//                if( column.equalsIgnoreCase( cmpCol ) )
//                {
//                    if( connector.isSQLite() )
//                    {
//                        String typeName = rs.getString( /*"TYPE_NAME"*/ 6 );
//                        isDateTime = "DATE".equalsIgnoreCase( typeName ) ||
//                                     "DATETIME".equalsIgnoreCase( typeName ) ||
//                                     "TIMESTAMP".equalsIgnoreCase( typeName );
//                        break;
//                    }
//
//                    short type = rs.getShort( /*"DATA_TYPE"*/ 5 );
//                    isDateTime =
//                            type == Types.DATE ||
//                            type == Types.TIMESTAMP;
//                    if( !isDateTime && connector.isSQLServerJTDS() )
//                    {
//                        String typeName = rs.getString( /*"TYPE_NAME"*/ 6 );
//                        isDateTime = type == Types.VARCHAR && "date".equals( typeName );
//                    }
//
//                    break;
//                }
//            }
//        }
//        finally
//        {
//            connector.releaseConnection( conn );
//        }
//
//        return isDateTime;
//    }
//
//    /**
//     * Get collection operations.
//     */
//    // supposed to be visible
//    @Override
//    public Operation[] getCollectionOperations()
//    {
//        return null;
//    }
//
//    /**
//     * Get owner operations.
//     */
//    @Override
//    public Operation[] getOwnerOperations()
//    {
//        return null;
//    }
//
//    protected static String findRedirectEncoding( OperationSupport op, Map<String, Object> extra )
//    {
//        String enc = extra.get( HttpConstants.CHARSET_PARAM ) != null ? extra.get( HttpConstants.CHARSET_PARAM ).toString() : null;
//        if( enc == null && op instanceof SystemOperation )
//        {
//            Object encObj = ( ( SystemOperation )op ).getAnyParam( HttpConstants.CHARSET_PARAM );
//            if( encObj instanceof String[] )
//            {
//                String[] encArr = ( String[] )encObj;
//                if( encArr.length > 0 )
//                {
//                    enc = encArr[ 0 ];
//                }
//            }
//            else
//            {
//                enc = ( String )encObj;
//            }
//            if( enc == null )
//            {
//                enc = "UTF-8";
//            }
//        }
//        return enc;
//    }
//
//
//    // for silent operations
//    private String redirectURL;
//
//    /**
//     * Make redirect URL.
//     *
//     * <br/>If field redirectURL is not null then return this field.
//     * <br/>If link referer not link per operation or per Query Builder, then return this link referer.
//     * <br/>Otherwise return redirect URL make by the instrumentality of {@link com.beanexplorer.enterprise.operations.HttpRedirectOperation makeRedirectURL}
//     *
//     * @param queryURL URL servlet operation.
//     * @param referer link on the referer, who is creating this operation
//     * @param myPars parameters for servlet
//     * @return
//     */
//    protected String silentRedirectURL( String queryURL, String referer, Map myPars )
//    {
//        if( redirectURL != null )
//            return redirectURL;
//
//        boolean bDecodeError = false;
//        if( referer != null )
//        {
//            String checkReferer = referer;
//            if( LinkProcessor.isServletNamed( referer, "nd", "ndnr", "d", "dnr" ) )
//            {
//                String enc = findRedirectEncoding( this, myPars );
//
//                try
//                {
//                    Utils.DecodeResult dr = Utils.decode( referer, userInfo, enc );
//                    if( dr.errorCode == 0 )
//                    {
//                        checkReferer = dr.decodedUrl;
//                    }
//                }
//                catch( Exception exc )
//                {
//                    Logger.error( cat, "silentRedirectURL", exc );
//                    bDecodeError = true;
//                }
//            }
//
//            if( !bDecodeError &&
//                !( checkReferer.indexOf( "/o?" ) > 0 ||
//                   checkReferer.endsWith( "/o" ) ||
//                   checkReferer.endsWith( "/qb" ) ||
//                   LinkProcessor.isServletNamed( checkReferer, "o", "qb" ) )
//              )
//            {
//                return redirectURL = referer;
//            }
//        }
//
//        return redirectURL = HttpRedirectOperation.makeRedirectURL( queryURL, this, myPars );
//    }
//
//    /**
//     * Create insert operation for entity define as parameter "ent".
//     *
//     * @param connector connector to the DB
//     * @param ent entity name
//     * @return instance insert operation
//     * @throws Exception
//     */
//    // supposed to be used internally
//    public InsertOperation setupInsertOperation( DatabaseService connector, String ent )
//            throws Exception
//    {
//        return setupInsertOperation( connector, ent, true );
//    }
//
//    /**
//     * Create insert operation for entity define as parameter "ent".
//     *
//     * @param connector connector to the DB
//     * @param ent entity name
//     * @param isSilent if true then create silent operation otherwise not silent
//     * @return new instance insert operation
//     * @throws Exception
//     */
//    // supposed to be used internally
//    public InsertOperation setupInsertOperation( DatabaseService connector, String ent, boolean isSilent )
//            throws Exception
//    {
//        String pk = Utils.findPrimaryKeyName( connector, ent );
//        String q = null;
//        String cat = null;
//        if( entity.equals( ent ) )
//        {
//            q = fromQuery;
//            cat = category;
//        }
//        InsertOperation op = isSilent ? new SilentInsertOperation() : new InsertOperation();
//        op.setName( "Insert" );
//        op.initialize( platform, userInfo, ent, pk, new String[ 0 ], q, cat, this.tcloneId );
//        op.setSessionAdapter( sessionAdapter );
//        return op;
//    }
//
//    /**
//     * Create edit operation for entity define as parameter "ent".
//     *
//     * @param connector connector to the DB
//     * @param ent entity name
//     * @param recs data for edit operation
//     * @return new instance edit operation
//     * @throws Exception
//     */
//    // supposed to be used internally
//    public EditOperation setupEditOperation( DatabaseService connector, String ent, String []recs )
//            throws Exception
//    {
//        return setupEditOperation( connector, ent, recs, true );
//    }
//
////    /**
////     * Create edit operation for entity define as parameter "ent".
////     *
////     * @param connector connector to the DB
////     * @param ent entity name
////     * @param recs data for edit operation
////     * @param isSilent
////     * @return new instance edit operation
////     * @throws Exception
////     */
////    // supposed to be used internally
////    public EditOperation setupEditOperation( DatabaseService connector, String ent, String []recs, boolean isSilent )
////            throws Exception
////    {
////        String pk = Utils.findPrimaryKeyName( connector, ent );
////        String q = null;
////        String cat = null;
////        if( entity.equals( ent ) )
////        {
////            q = fromQuery;
////            cat = category;
////        }
////        EditOperation op = isSilent ? new SilentEditOperation() : new EditOperation();
////        op.setName( "Edit" );
////        op.initialize( platform, userInfo, ent, pk, recs, q, cat, this.tcloneId );
////        op.setSessionAdapter( sessionAdapter );
////        return op;
////    }
//
//    /**
//     * Get array of instance operations insert realizing interface {@link com.beanexplorer.enterprise.Operation Operation}.
//     *
//     * @param out
//     * @param connector connector to the DB
//     * @param myFromQuery
//     * @return
//     * @throws Exception
//     */
//    // supposed to be used internally
//    protected Operation[] getInsertCollectionOperations( Writer out, DatabaseService connector, String myFromQuery )
//            throws Exception
//    {
//        ArrayList<Operation> collIns = new ArrayList<Operation>();
//        AccessibleOperationsList ops =
//                connector.getAnalyzer().getAccessibleOperations( userInfo,
//                        ( this instanceof HttpOperationSupport ?
//                                ( ( HttpOperationSupport )this ).contextPrefix :
//                                "dummy"
//                        ),
//                        platform, entity, myFromQuery );
//        ops.setNoInsertsOrDeletions( false );
//        ops.setKnownTypes( TabularFragmentBuilder.useableOperationTypes );
//        ops.openCollections( category );
//
//        while( ops.next() )
//        {
//            if( ops.isStandardInsert() || ops.isSubclassedInsert() )
//            {
//                Operation op = Utils.setupOperation( connector, ops.getId(), userInfo,
//                        null, platform, entity, new String[0],
//                        myFromQuery, null, this.tcloneId );
//
//                op.setSessionAdapter( sessionAdapter );
//                op.setAppInfo( appInfo );
//                op.setTopLevel( false );
//
//                if( op instanceof OperationEnabler &&
//                        !( ( OperationEnabler )op ).isEnabled( connector, Collections.EMPTY_MAP ) )
//                {
//                    continue;
//                }
//
//                collIns.add( op );
//            }
//        }
//
//        return collIns.toArray( new Operation[0] );
//    }
//
//    /**
//     * Get insert operation for owner entity.
//     *
//     * @param out not used
//     * @param connector connector to the DB
//     * @param ownerEntity
//     * @return
//     * @throws Exception
//     */
//    // supposed to be used internally
//    protected Operation getInsertOwnerOperation( Writer out, DatabaseService connector, String ownerEntity )
//            throws Exception
//    {
//        AccessibleOperationsList ops = null;
//        try
//        {
//            ops = connector.getAnalyzer().getAccessibleOperations( userInfo,
//                    ( this instanceof HttpOperationSupport ?
//                            ( ( HttpOperationSupport )this ).contextPrefix :
//                            "dummy"
//                    ),
//                    platform, ownerEntity, "0" );
//            ops.setNoInsertsOrDeletions( false );
//            ops.setKnownTypes( TabularFragmentBuilder.useableOperationTypes );
//            ops.open( null );
//
//            while( ops.next() )
//            {
//                //System.out.println( "ops.getCode() = " + ops.getCode() );
//                if( ops.isStandardInsert() || ops.isSubclassedInsert() )
//                {
//                    Operation op = Utils.setupOperation( connector, ops.getId(), userInfo,
//                            null, platform, ownerEntity, new String[0],
//                            null, null, this.tcloneId );
//
//                    // temporarily because we can only obtain
//                    // last insert id from it
//                    if( !( op instanceof InsertOperation ) )
//                        continue;
//
//                    op.setSessionAdapter( sessionAdapter );
//                    op.setAppInfo( appInfo );
//                    op.setTopLevel( false );
//
//                    if( op instanceof OperationEnabler &&
//                            !( ( OperationEnabler )op ).isEnabled( connector, Collections.EMPTY_MAP ) )
//                    {
//                        continue;
//                    }
//
//                    return op;
//                }
//            }
//
//        }
//        finally
//        {
//            if ( ops != null )
//                ops.closeResultSet();
//        }
//
//        return null;
//    }
//
//    /**
//     *
//     */
//    // supposed to return the same value as getParameters
//    // but without re-creating the bean
//    @Override
//    public Object getStoredParameters()
//    {
//        return null;
//    }
//
//    /**
//     * Could be the entity of general reference.
//     *
//     * @param connector connector to the DB
//     * @param entity entity name
//     * @return true if entity contains generic reference, otherwise return false.
//     * @throws Exception
//     */
//    public static boolean doOwnerSubstitute( DatabaseService connector, String entity )
//            throws Exception
//    {
//        String pk = Utils.findPrimaryKeyName( connector, entity );
//        // check if primary column is also generic reference
//        // if so we will be using actual owner
//        // as owner for collections instead of this record
//        String grsql = "SELECT tr.tableFrom ";
//        grsql += " FROM table_refs tr ";
//        grsql += " WHERE tr.tableTo IS NULL AND tr.tableFrom = '" + Utils.safestr( connector, entity ) + "' ";
//        if( connector.isOracle() )
//        {
//            grsql += " AND tr.columnsFrom IN ('" + Utils.safestr( connector, pk ) + "', '" + Utils.safestr( connector, pk.toUpperCase() ) + "')";
//        }
//        else
//        {
//            grsql += " AND tr.columnsFrom IN ('" + Utils.safestr( connector, pk ) + "')";
//        }
//
//        Object[] genericRefs = Utils.readAsArray( connector, grsql, ReferencesArraysCache.getInstance() );
//        return genericRefs != null && genericRefs.length > 0;
//    }
//
    /**
     * Returns map in which keys from presetValues are established according by
     * {@link com .beanexplorer.enterprise.DatabaseAnalyzer#getIdentifierCase()} values for the specified connector to the database.
     * If presetValues is empty, then return presetValues.
     *
     * @param connector connector to the DB
     * @param presetValues the map preset value
     * @return map preset values map
     */
    protected Map<String, String> mapPresetsForTheDatabase(DatabaseService connector,
                                           DynamicPropertySet record, Map<String, String> presetValues )
    {
        if( presetValues.isEmpty() )
        {
            return presetValues;
        }

        HashMap<String,String> newPresets = new HashMap<>( presetValues.size() );
        if( connector.getAnalyzer().getIdentifierCase() != DatabaseAnalyzer.IdentifierCase.NEUTRAL )
        {
            for( Map.Entry<String, String> entry : presetValues.entrySet() )
            {
                String name = entry.getKey();
                String newName = connector.getAnalyzer().quoteIdentifier( name );
                if( name.equals( newName ) )
                {
                    newPresets.put( connector.getAnalyzer().getCaseCorrectedIdentifier(name), entry.getValue() );
                }
                else
                {
                    newPresets.put( name, entry.getValue() );
                }
            }
            return newPresets;
        }

        for( Map.Entry<String, String> entry : presetValues.entrySet() )
        {
            String name = entry.getKey();
            DynamicProperty prop = record.getProperty( name );
            newPresets.put( prop != null ? prop.getName() : name, entry.getValue() );
        }

        return newPresets;
    }
//
//    /**
//     * Same as
//     * {@link #getTagsListFromQuery( DatabaseService connector, String query, String id ) getTagsListFromQuery} ( DatabaseService connector, String query, String id )
//     * ,difference only in return value.
//     *
//     * @param connector connector to the DB
//     * @param query sql query
//     * @param id  id column name
//     * @return array of style for every query row as string
//     * @throws Exception
//     */
//    public static String[] getTagsFromQuery( DatabaseService connector, String query, String id )
//            throws SQLException
//    {
//        return getTagsListFromQuery( connector, query, id ).toArray( new String[0] );
//    }
//
//    public static String[] getTagsFromQuery( DatabaseService connector, String query, String id, HttpSession session )
//            throws SQLException
//    {
//        List<String> attrNames = Collections.list( session.getAttributeNames() );
//        Map attrMap = new HashMap();
//        for( String attr : attrNames )
//        {
//            attrMap.put( attr, session.getAttribute( attr ) );
//        }
//        return getTagsListFromQuery( connector, query, id, attrMap ).toArray( new String[0] );
//    }
//
//    public void presetsToBean( Map presets, DynamicPropertySet bean )
//    {
//        for( Iterator entries = presets.entrySet().iterator(); entries.hasNext(); )
//        {
//            Map.Entry entry = ( Map.Entry )entries.next();
//            String name = ( String )entry.getKey();
//            DynamicProperty prop = bean.getProperty( name );
//            if( prop == null )
//                continue;
//            Object val = entry.getValue();
//            // do not force arrays
//            if( val != null && val.getClass().isArray() && prop.isReadOnly() )
//                continue;
//            prop.setValue( Utils.changeType( val, prop.getType(), userInfo ) );
//        }
//    }
//
//    /**
//     * Reads tag list from query for inclusion in drop down list
//     *
//     * <br/><br/>Prompt<i>{@link #TAG_DELIMITER _tag_delimiter_}</i>ID<i>{@link #TAG_DELIMITER _tag_delimiter_}</i>css_style
//     *
//     * <br/><br/>where:
//     * <br/> Prompt - extra key identifier row - should be one of the first two columns in the query.
//     *
//     * It is assumed that the column with id record also is one of the first two columns in the query and it is another column than the column
//     * with Propmt value.
//     *
//     * <br/><br/>To specify css style for the row, it is necessary to add one more query column - "___css_style", value of which will be necessary
//     * style.
//     *
//     * <br/><br/>If result query has one column, then it is assumed, that it is string of the folowing format:
//     * "Prompt<i>{@link #TAG_DELIMITER _tag_delimiter_}</i>ID<i>{@link #TAG_DELIMITER _tag_delimiter_}</i>css_style".
//     * That because this value will be added to the resulting array without treatments.
//     *
//     * @param connector connector to the DB
//     * @param query sql query
//     * @param id id column name
//     * @return ArrayList of style for every query row as string
//     * @throws SQLException
//     */
//    // pending!!!
//    // rewrite to use calcUsingQuery
//    public static List<String> getTagsListFromQuery( DatabaseService connector, String query, String id )
//            throws SQLException
//    {
//        // pending!!!
//        // presetValues instead of Collections.EMPTY_MAP
//        return getTagsListFromQuery( connector, query, id, Collections.EMPTY_MAP );
//    }
//
//    public static List<String> getTagsListFromQuery( DatabaseService connector, String query, String id, Map attr )
//            throws SQLException
//    {
//        ResultSet rs = null;
//        ArrayList<String> values = new ArrayList<String>();
//        query = Utils.handleConditionalParts( connector, query, attr );
//        try
//        {
//            rs = connector.executeQuery( query );
//            int colCount = -1;
//            int idColumn = id == null ? 1 : -1;
//            while( rs.next() )
//            {
//                if( colCount < 0 )
//                {
//                    colCount = rs.getMetaData().getColumnCount();
//                }
//                if( colCount >= 2 )
//                {
//                    if( idColumn < 0 )
//                    {
//                        idColumn = rs.findColumn( id );
//                    }
//                    if( idColumn > 0 )
//                    {
//                        String code = rs.getString( idColumn );
//                        String prompt = rs.getString( 3 - idColumn );
//                        if( prompt == null )
//                        {
//                            prompt = code;
//                        }
//                        String tag = prompt + TAG_DELIMITER + code;
//                        if( colCount > 2 )
//                        {
//                            try
//                            {
//                                ResultSetMetaData meta = rs.getMetaData();
//                                for( int i = 3; i <= colCount; i++ )
//                                {
//                                    String name = meta.getColumnLabel( i );
//                                    if( name.equals( CSS_TAG_STYLE ) )
//                                    {
//                                        String style = rs.getString( i );
//                                        if( style != null )
//                                        {
//                                            tag += TAG_DELIMITER + style;
//                                        }
//                                        break;
//                                    }
//                                }
//                            }
//                            catch( SQLException ex )
//                            {
//                                Logger.warn( cat, "Could not acquire ResultSet metadata" );
//                            }
//                        }
//                        values.add( tag );
//                    }
//                }
//                else
//                {
//                    values.add( rs.getString( 1 ) );
//                }
//            }
//        }
//        catch( SQLException exc )
//        {
//            Logger.error( cat, "When getting tags via sql =\n" + query, exc );
//            throw exc;
//        }
//        finally
//        {
//            connector.close( rs );
//            if( values != null && values.size() > 100 )
//            {
//                if( Logger.isDebugEnabled( cat ) )
//                {
//                    Logger.debug( cat, "Too long tag list: " + values.size() + ", sql:" + query );
//                }
//            }
//        }
//        return values;
//    }
//
//    /**
//     * Reads tag list from query stored in the database as standard selection view
//     * for inclusion in drop down list
//     *
//     * @param connector connector to the DB
//     * @param table table name
//     * @return ArrayList of style for every query row as string
//     * @throws Exception
//     */
//    public String[] getTagsFromSelectionView( DatabaseService connector, String table )
//            throws Exception
//    {
//        String prefix = this instanceof HttpOperationSupport ?
//                ( ( HttpOperationSupport )this ).contextPrefix :
//                null;
//        return getTagsFromSelectionView( connector, prefix, userInfo, table );
//    }
//
//    public String[] getTagsFromSelectionView( DatabaseService connector, String table, Map extraParams )
//            throws Exception
//    {
//        String prefix = this instanceof HttpOperationSupport ?
//                ( ( HttpOperationSupport )this ).contextPrefix :
//                null;
//        return getTagsFromSelectionView( connector, prefix, userInfo, table, extraParams );
//    }
//
//    /**
//     * Reads tag list from query stored in the database identified by its name
//     * for inclusion in drop down list
//     *
//     * @param connector connector to the DB
//     * @param table table name
//     * @param viewName view name
//     * @return ArrayList of style for every query row as string
//     * @throws Exception
//     */
//    public String[] getTagsFromCustomSelectionView( DatabaseService connector, String table, String viewName )
//            throws Exception
//    {
//        return getTagsFromCustomSelectionView( connector, table, viewName, null );
//    }
//
//    public String[] getTagsFromCustomSelectionView( DatabaseService connector, String table, String viewName, Map extraParams )
//            throws Exception
//    {
//        String prefix = this instanceof HttpOperationSupport ?
//                ( ( HttpOperationSupport )this ).contextPrefix :
//                null;
//        return getTagsFromCustomSelectionView( connector, prefix, userInfo, table, viewName, extraParams );
//    }
//
//    /**
//     * Gets style array, as string array, for every row query of table`s default selection view.
//     * See also
//     * {@link #getTagsFromQuery(DatabaseService, String, String) getTagsFromQuery(DatabaseService, String, String)}.
//     *
//     * @param connector connector to the DB
//     * @param context value for context placeholder {@link com.beanexplorer.enterprise.DatabaseConstants#CONTEXT_PLACEHOLDER}
//     * @param ui user info
//     * @param table table name
//     * @return ArrayList of style for every query row as string
//     * @throws Exception
//     */
//
//    protected static String[] getTagsFromSelectionView( DatabaseService connector,
//                                                        String context, UserInfo ui, String table, Map extraParams )
//            throws Exception
//    {
//        return getTagsFromCustomSelectionView( connector, context, ui, table, SELECTION_VIEW, extraParams );
//    }
//
//    protected static String[] getTagsFromSelectionView( DatabaseService connector,
//                                                        String context, UserInfo ui, String table )
//            throws Exception
//    {
//        return getTagsFromCustomSelectionView( connector, context, ui, table, SELECTION_VIEW, null );
//    }
//
//    /**
//     * Gets style array, as string array, for every row query of table`s custom selection view.
//     * See also
//     * {@link #getTagsFromQuery(DatabaseService, String, String) getTagsFromQuery(DatabaseService, String, String)}.
//     *
//     * @param connector connector to the DB
//     * @param context value for context placeholder {@link com.beanexplorer.enterprise.DatabaseConstants#CONTEXT_PLACEHOLDER}
//     * @param ui user info
//     * @param table table name
//     * @param viewName view name
//     * @return ArrayList of style for every query row as string
//     * @throws Exception
//     */
//    public static String[] getTagsFromCustomSelectionView( DatabaseService connector,
//                                  String context, UserInfo ui, String table, String viewName, Map<?,?> extraParams )
//            throws Exception
//    {
//        String query = QueryInfo.getQueryText( connector, table, viewName );
//
//        query = Utils.putPlaceholders( connector, query, ui, context );
//
//        if( extraParams != null )
//        {
//            extraParams = new HashMap( extraParams ); // to make it mutable
//            query = Utils.handleConditionalParts( connector, query, extraParams );
//            query = Utils.putRequestParametersFromMap( connector, query, extraParams, ui );
//            if( !extraParams.isEmpty() )
//            {
//                Map realColumns = new HashMap();
//                for( Map.Entry<?,?> entry : extraParams.entrySet() )
//                {
//                    if( Utils.columnExists( connector, table, ( String )entry.getKey() ) )
//                    {
//                        realColumns.put( entry.getKey(), entry.getValue() );
//                    }
//                }
//                if( !realColumns.isEmpty() )
//                {
//                    String pk = Utils.findPrimaryKeyName( connector, table );
//                    query = Utils.addRecordFilter( connector, query, table, pk, realColumns, false, ui );
//                }
//                //System.out.println( "query = " + query );
//            }
//        }
//        query = Utils.putDictionaryValues( connector, query, ui );
//
//        if ( ui instanceof OperationUserInfo && ((OperationUserInfo)ui).getUnrestrictedSession() != null )
//        {
//            HttpSession session = ((OperationUserInfo)ui).getUnrestrictedSession();
//            query = Utils.putSessionVars( connector, query, session );
//            return getTagsFromQuery( connector, query, null, session );
//        }
//
//        return getTagsFromQuery( connector, query, null );
//    }
//
//    /**
//     * Get tag's value from a single tag row "displayName" + TAG_DELIMITER + "value".
//     *
//     * @param tag single tag string
//     * @throws Exception
//     */
//    public static String getTagValue( String tag )
//    {
//        if ( Utils.isEmpty( tag ) || ! tag.contains( TAG_DELIMITER ) )
//            return tag;
//        return tag.substring( tag.indexOf( OperationSupport.TAG_DELIMITER ) + 1 );
//    }
//
//    /**
//     * Get tag's name from a single tag row "displayName" + TAG_DELIMITER + "value".
//     *
//     * @param tag single tag string
//     * @throws Exception
//     */
//    public static String getTagName( String tag )
//    {
//        if ( Utils.isEmpty( tag ) || ! tag.contains( TAG_DELIMITER ) )
//            return tag;
//        return tag.substring( 0, tag.indexOf( OperationSupport.TAG_DELIMITER ) );
//    }
//
//    /**
//     * Adding tags into dynamic property set as attributes for columns of type enum and reference by the information from the Database.
//     *
//     * @param connector DB connector
//     * @param platform type of the page (HTML, WML, XML, XHTML, cHTML, Swing, calculate)
//     * @param ui user info
//     * @param entity entity name
//     * @param pk primary key of the entity
//     * @param bean dynamic property set
//     * @param presetValues values for filtering
//     * @throws Exception
//     */
//    protected static void addTagEditors( DatabaseService connector,
//                                         String platform, UserInfo ui, String entity, String pk,
//                                         DynamicPropertySet bean, Map presetValues, String tcloneId )
//            throws Exception
//    {
//         addTagEditors( connector, platform, ui,  entity, pk, bean, presetValues, false, tcloneId );
//    }
//
//    /**
//     * Adding tags into dynamic property set as attributes for columns of type enum and reference by the information from the Database.
//     *
//     * @param connector DB connector
//     * @param platform type of the page (HTML, WML, XML, XHTML, cHTML, Swing, calculate)
//     * @param ui user info
//     * @param entity entity name
//     * @param pk primary key of the entity
//     * @param bean dynamic property set
//     * @param presetValues values for filtering
//     * @param bNoExternalTables do not read tag lists from external tables
//     * @throws Exception
//     */
//    public static void addTagEditors( DatabaseService connector,
//                                         String platform, UserInfo ui, String entity, String pk,
//                                         DynamicPropertySet bean, Map presetValues,
//                                         boolean bNoExternalTables, String tcloneId )
//            throws Exception
//    {
//        Connection conn = null;
//        // store all columns in the table into columnNameList variable
//        // suitable to for using in WHERE clause
//        StringBuffer columnNameList = new StringBuffer( "( " );
//        try
//        {
//            conn = connector.getConnection();
//            RSWrapper rsMeta = Utils.getEntityColumns( connector, conn, entity );
//            boolean isFirst = true;
//            while( rsMeta.next() )
//            {
//                String name = rsMeta.getString( 4 ); //rsMeta.getString( "COLUMN_NAME" );
//                if( !isFirst )
//                {
//                    columnNameList.append( ", " );
//                }
//                columnNameList.append( "'" ).append( Utils.safestr( connector, name ) ).append( "'" );
//                isFirst = false;
//            }
//        }
//        finally
//        {
////                connector.close( rs );
//            connector.releaseConnection( conn );
//        }
//        columnNameList.append( " )" );
//
//        HashMap enums = loadEntityEnums( connector, entity, bean );
//        HashMap<?, ?> iterEnums = enums;
//        Map<String, Object> caseInsensitivePresets = new TreeMap<String, Object>( String.CASE_INSENSITIVE_ORDER );
//        caseInsensitivePresets.putAll( presetValues );
//        for( Map.Entry<?, ?> en : iterEnums.entrySet() )
//        {
//            Object preset = caseInsensitivePresets.get( en.getKey() );
//            if( preset instanceof String )
//            {
//                List<String>tags = Arrays.asList( ( String [] )en.getValue() );
//                if( "on".equalsIgnoreCase( ( String )preset ) && tags.size() == 2 && tags.contains( "yes" ) && tags.contains( "no" ) )
//                {
//                    bean.setValue( en.getKey().toString(), "yes" );
//                }
//            }
//        }
//
//        if( !bNoExternalTables )
//        {
//            String transitivePresetName = null;
//            for( Iterator entries = presetValues.keySet().iterator(); entries.hasNext(); )
//            {
//                transitivePresetName = ( String )entries.next();
//                //System.out.println( "transitivePresetName: " + transitivePresetName );
//                break;
//            }
//
//            Map transitiveColumns = null;
//            if( transitivePresetName != null )
//            {
//                String sql = " SELECT tr2.tableFrom AS \"tableFrom\", tr2.columnsFrom AS \"columnsFrom\" "
//                        + "   FROM table_refs tr1, table_refs tr2, table_refs tr3 "
//                        + "   WHERE NOT ( tr1.tableFrom = tr2.tableFrom ) "
//                        + "       AND tr1.tableTo = tr2.tableTo AND tr1.columnsTo = tr2.columnsTo "
//                        + "       AND tr1.columnsFrom = tr2.columnsFrom "
//                        + "       AND tr3.tableFrom = tr1.tableFrom AND NOT ( tr3.columnsFrom = tr1.columnsFrom ) "
//                        + "       AND tr3.tableTo = tr2.tableFrom "
//                        + "       AND tr1.tableFrom = '" + entity + "' "
//                        + "       AND tr1.columnsFrom = '" + Utils.safestr( connector, transitivePresetName ) + "' ";
//                transitiveColumns = Utils.readAsMap( connector, sql, ReferencesMapsCache.getInstance() );
//                if( transitiveColumns.isEmpty() )
//                {
//                    transitiveColumns = null;
//                }
//            }
//
//            // next - read referential information available in the database
//            Map refs = connector.getAnalyzer().readReferences( null, ui, entity, columnNameList.toString() );
//
//            for( Iterator entries = refs.entrySet().iterator(); entries.hasNext(); )
//            {
//                Map.Entry entry = ( Map.Entry )entries.next();
//                String name = ( String )entry.getKey();
//                Object presetVal = presetValues.get( name );
//                String[] refRecord = ( String[] )entry.getValue();
//                String tableTo = refRecord[ 0 ];
//                String columnsTo = refRecord[ 1 ];
//                String sql = refRecord[ 2 ];
//                String qID = refRecord[ 3 ];
//                String qname = refRecord[ 4 ];
//
//                DynamicProperty prop = bean.getProperty( name );
//                boolean isReadOnly = prop != null && prop.isReadOnly();
//
//                if( prop != null )
//                {
//                    prop.setAttribute( TABLE_REF, tableTo );
//                }
//
//                String tags[];
//
//                if( !isReadOnly && !Utils.isEmpty( sql ) )
//                {
//                    boolean bAJAXSelectorIsStrict = true;
//                    DynamicPropertySet dps = Utils.extractParamPlaceholdersAsBean( connector, sql, null );
//                    for( Iterator i = dps.propertyIterator(); i.hasNext(); )
//                    {
//                        DynamicProperty dp = ( DynamicProperty )i.next();
//                        if( HttpConstants.SELECTOR.equals( dp.getName() ) )
//                        {
//                             bAJAXSelectorIsStrict = !"no".equals( dp.getAttribute( HttpConstants.SELECTOR_STRICT ) );
//                        }
//                    }
//
//                    if( !bAJAXSelectorIsStrict &&  prop != null )
//                    {
//                        prop.setAttribute( EXTERNAL_TAG_LIST, tableTo );
//                        continue;
//                    }
//                }
//
//                boolean bWasAuto = false;
//                if( !Utils.isEmpty( sql ) )
//                {
//                    // this is needed because names in presetValues can be hacked already with mapPresetsForTheDatabase
//                    sql = Utils.handleConditionalParts( connector, sql, caseInsensitivePresets );
//                    sql = Utils.putPlaceholders( connector, sql, ui, null );
//                    Map pre = caseInsensitivePresets;
//                    if( tcloneId != null )
//                    {
//                        pre = new TreeMap<String, Object>( String.CASE_INSENSITIVE_ORDER );
//                        pre.putAll( caseInsensitivePresets );
//                        pre.put( HttpConstants.CLONED_TABLE_POSTFIX_PARAM, tcloneId );
//                    }
//
//                    sql = Utils.putRequestParametersFromMap( connector, sql, pre, ui );
//                    sql = Utils.putDictionaryValues( connector, sql, ui );
//
//                    if ( ui instanceof OperationUserInfo &&  ((OperationUserInfo)ui).getUnrestrictedSession() != null )
//                    {
//                        sql = Utils.putSessionVars( connector, sql, ((OperationUserInfo)ui).getUnrestrictedSession() );
//                    }
//
//                    // in case it was already handled by putRequestParameters
//                    if( !caseInsensitivePresets.containsKey( transitivePresetName ) )
//                    {
//                        transitiveColumns = null;
//                    }
//                    //System.out.println( "sql1 = " + sql );
//                }
//                else
//                {
//                    sql = "SELECT * FROM " + connector.getAnalyzer().quoteIdentifier( tableTo );
//                    Logger.warn( cat, "addTagEditors: unable to find selection view, SELECT * FROM " + tableTo + " is used" );
//                    bWasAuto = true;
//                }
//
//                if( isReadOnly && !Utils.isEmpty( presetVal ) && bWasAuto )
//                {
//                    tags = new String[]{ "" + presetVal };
//                }
//                else if( !Utils.isEmpty( presetVal ) && ( isReadOnly || presetVal.getClass().isArray() ) )
//                {
//                    // actually in this case we need to read only one value!
//                    String pkt = Utils.findPrimaryKeyName( connector, tableTo );
//                    sql = Utils.addRecordFilter( connector, sql, tableTo, pkt,
//                            Collections.singletonMap( columnsTo, presetVal ), false, ui );
//                    //System.err.println( " ---- Executing \n" + sql );
//                    tags = getTagsFromQuery( connector, sql, null );
//                    /*
//                    for( int i = 0; i < tags.length; i++ )
//                        System.err.println( "Got tag for " + name + ": " + tags[ i ] );
//                    */
//                }
//                else
//                {
//                    String filtCol = null;
//                    if( transitiveColumns != null )
//                    {
//                        filtCol = ( String )transitiveColumns.get( tableTo );
//                    }
//                    String pkt = null;
//                    if( filtCol != null && presetValues.get( transitivePresetName ) != null )
//                    {
//                        pkt = Utils.findPrimaryKeyName( connector, tableTo );
//                        //sql = Utils.prefixQueryWithId( sql, tableTo, pkt );
//
//                        sql = Utils.addRecordFilter( connector, sql, tableTo, pkt,
//                                Collections.singletonMap( filtCol, presetValues.get( transitivePresetName ) ),
//                                false, ui );
//
//                       Logger.info( cat, "Applying transitiveColumns = " + transitiveColumns + ", sql =\n" + sql );
//
//                        //System.out.println( "sql2 = " + sql );
//                    }
//
//                    boolean bMakeExternalList = false;
//                    if( sql.indexOf( "/*no-external-taglist*/" ) < 0 &&
//                        !Arrays.asList( "categories", "entities" ).contains( tableTo ) )
//                    {
//                        if( sql.indexOf( "/*big-selection-view*/" ) >= 0 && prop != null )
//                        {
//                            bMakeExternalList = true;
//                        }
//                        else if( prop != null )
//                        {
//                            String maxRecStr;
//                            if( PLATFORM_WML.equals( platform ) )
//                            {
//                                maxRecStr = Utils.getSystemSetting( connector, "WML_MAX_LIST_SIZE", "10" );
//                            }
//                            else
//                            {
//                                maxRecStr = Utils.getSystemSetting( connector, "HTML_MAX_LIST_SIZE", "250" );
//                            }
//
//                            int maxRecs = Integer.parseInt( maxRecStr );
//
//                            Cache svc = SelectionViewCountsCache.getInstance();
//                            Integer cntObj = ( Integer )svc.get( sql );
//                            if( cntObj != null && cntObj > maxRecs )
//                            {
//                                bMakeExternalList = true;
//                            }
//                            else
//                            {
//                                String origSql = sql;
//                                if( sql.indexOf( QP_SQL_START ) > 0 )
//                                {
//                                    QueryExecuter qe = new QueryExecuter( connector, ui, Collections.emptyMap(), "" );
//                                    try
//                                    {
//                                        qe.makeIterator( tableTo, qname, presetValues );
//                                        sql = qe.getFragmentSupport().getIteratedSQL();
//                                        //System.out.println( "sql = " + sql );
//                                    }
//                                    catch( Exception err )
//                                    {
//                                        Logger.error( cat, "Unable to expand nested sql for " + tableTo + "::" + qname, err );
//                                    }
//                                    finally
//                                    {
//                                        qe.closeIterator();
//                                    }
//                                }
//
//                                String cntQuery = Utils.makeCountQuery( connector, sql, "CNT" );
//                                int cnt = maxRecs + 1;
//                                try
//                                {
//                                    cnt = new QRec( connector, cntQuery ).getInt( "CNT" );
//                                }
//                                catch( SQLException se )
//                                {
//                                    Logger.warn( cat, "Utils.makeCountQuery caused error, query = " + cntQuery, se );
//                                }
//                                svc.put( origSql, new Integer( cnt ) );
//
//                                bMakeExternalList = cnt > maxRecs;
//                            }
//                        }
//                    }
//
//                    if( bMakeExternalList )
//                    {
//                        prop.setAttribute( EXTERNAL_TAG_LIST, tableTo );
//                        Object propVal = prop.getValue();
//                        if( propVal != null )
//                        {
//                            if( pkt == null )
//                            {
//                                pkt = Utils.findPrimaryKeyName( connector, tableTo );
//                            }
//                            sql = Utils.addRecordFilter( connector, sql, tableTo, pkt,
//                                    Collections.singletonMap( columnsTo, propVal ), false, ui );
//                            try
//                            {
//                                tags = getTagsFromQuery( connector, sql, null );
//                            }
//                            catch( Exception exc )
//                            {
//                                if( bWasAuto )
//                                {
//                                    tags = new String[] { exc.getMessage() + TAG_DELIMITER + propVal };
//                                }
//                                else
//                                {
//                                    throw exc;
//                                }
//                            }
//                        }
//                        else
//                        {
//                            tags = new String[0];
//                        }
//
//                        if( tags.length == 0 && prop != null )
//                        {
//                            prop.setValue( null );
//                        }
//                    }
//                    else
//                    {
//                        //System.out.println( "sql: " + sql );
//                        tags = getTagsFromQuery( connector, sql, null );
//                    }
//                }
//                enums.put( name, tags );
//            }
//
//            // handle generic refs where permitted entities are specified
//            String grsql = "SELECT gre.columnsFrom, gre.tableTo ";
//            grsql += " FROM generic_ref_entities gre ";
//            grsql += " WHERE gre.tableFrom = '" + Utils.safestr( connector, entity ) + "' ";
//            grsql += " AND gre.columnsFrom IN " + columnNameList;
//
//            Object[] genericRefs = Utils.readAsArray( connector, grsql, ReferencesArraysCache.getInstance() );
//            for( int i = 0; i < genericRefs.length; i++ )
//            {
//                Object[] pair = ( Object[] )( genericRefs[ i ] );
//                String name = ( String )pair[ 0 ];
//                DynamicProperty prop = bean.getProperty( name );
//                Object tables = prop.getAttribute( EXTERNAL_TAG_LIST );
//                if( tables == null )
//                {
//                    tables = new ArrayList();
//                    prop.setAttribute( EXTERNAL_TAG_LIST, tables );
//                }
//                ( ( ArrayList )tables ).add( pair[ 1 ] );
//                /*
//                   String tags[];
//                   Object propVal = prop.getValue();
//                   if (propVal != null)
//                   {
//                       tags = new String[ 0 ];
//                   } else
//                       tags = new String[ 0 ];
//                   enums.put(name, tags);
//                */
//            }
//        }
//
//        // finally pass tags collected to the bean
//        if( enums.size() < 1 )
//        {
//            return;
//        }
//        for( DynamicProperty prop : bean )
//        {
//            Object tags = enums.get( prop.getName() );
//            if( tags != null )
//            {
//                prop.setAttribute( TAG_LIST_ATTR, tags );
//            }
//        }
//    }
//
//    public static HashMap loadEntityEnums( DatabaseService connector, String entity )
//            throws SQLException
//    {
//        try
//        {
//            return loadEntityEnums( connector, entity, Utils.getTableBean( connector, entity ) );
//        }
//        catch( SQLException se )
//        {
//            throw se;
//        }
//        catch( Exception e )
//        {
//            throw new RuntimeException( e );
//        }
//    }
//
//    public static HashMap loadEntityEnums( DatabaseService connector, String entity, DynamicPropertySet bean )
//            throws SQLException
//    {
//        HashMap enums = new HashMap();
//        // first try to process MySQL's 'enum' types
//        // for example column with the type "enum( 'male', 'female' )"
//        // must be forced to have only two possible values - 'male' and 'female'
//        ResultSet rs = null;
//        if( connector.isMySQL() )
//        {
//            try
//            {
//                rs = connector.executeQuery( "DESC " + connector.getAnalyzer().quoteIdentifier( entity ) );
//                while( rs.next() )
//                {
//                    String name = rs.getString( 1 );
//                    String type = rs.getString( 2 );
//                    if( type.startsWith( "enum(" ) )
//                    {
//                        ArrayList<String> values = new ArrayList<String>();
//                        StringTokenizer st = new StringTokenizer( type.substring( 5 ), ",')" );
//                        while( st.hasMoreTokens() )
//                        {
//                            values.add( st.nextToken() );
//                        }
//                        enums.put( name, values.toArray( new String[0] ) );
//                    }
//                }
//            }
//            finally
//            {
//                connector.close( rs );
//            }
//        }
//
//        // try to read Oracle's constraints
//        // that are stored like sex IN ( 'male', 'female' )
//        if( connector.isOracle() )
//        {
//            String sql = "SELECT uc.SEARCH_CONDITION AS \"Constr\" ";
//            sql += "FROM user_constraints uc ";
//            sql += "WHERE uc.CONSTRAINT_TYPE = 'C' AND uc.TABLE_NAME = UPPER( '" + entity + "' ) ";
//
//            List<String> constraints = Utils.readAsListOfStrings( connector, sql, TableConstraintsCache.getInstance() );
//
//            for( String constr : constraints )
//            {
//                StringTokenizer st = new StringTokenizer( constr.trim() );
//                int nTok = st.countTokens();
//                if( nTok < 3 )
//                {
//                    continue;
//                }
//                String colName = st.nextToken().toUpperCase();
//                String in = st.nextToken();
//                if( !"IN".equalsIgnoreCase( in ) )
//                {
//                    continue;
//                }
//                if( bean != null && bean.getProperty( colName ) == null)
//                {
//                    continue;
//                }
//                ArrayList<String> values = new ArrayList<String>();
//                try
//                {
//                    do
//                    {
//                        String val = st.nextToken( "(,')" );
//                        if( !Utils.isEmpty( val ) )
//                        {
//                            values.add( val );
//                        }
//                    }
//                    while( st.hasMoreTokens() );
//                }
//                catch( NoSuchElementException ignore )
//                {
//                }
//                if( values.size() > 0 )
//                {
//                    enums.put( colName, values.toArray( new String[0] ) );
//                }
//            }
//        }
//
//        // try to read DB2's constraints
//        // that are stored like sex IN ( 'male', 'female' )
//        if( connector.isDb2() )
//        {
//
//            String sql = "SELECT TEXT AS \"Constr\" ";
//            sql += "FROM SYSIBM.SYSCHECKS ";
//            sql += "WHERE TYPE = 'C' AND TBNAME = UPPER( '" + entity + "' ) ";
//
//            List<String> constraints = Utils.readAsListOfStrings( connector, sql, TableConstraintsCache.getInstance() );
//
//            for( String constr : constraints )
//            {
//                StringTokenizer st = new StringTokenizer( constr.trim() );
//                int nTok = st.countTokens();
//                if( nTok < 3 )
//                {
//                    continue;
//                }
//                String colName = st.nextToken().toUpperCase();
//                String in = st.nextToken();
//                if( !"IN".equalsIgnoreCase( in ) )
//                {
//                    continue;
//                }
//                if( bean != null && bean.getProperty( colName ) == null)
//                {
//                    continue;
//                }
//                ArrayList<String> values = new ArrayList<String>();
//                try
//                {
//                    do
//                    {
//                        String val = st.nextToken( "(,')" );
//                        if( !Utils.isEmpty( val ) )
//                        {
//                            values.add( val );
//                        }
//                    }
//                    while( st.hasMoreTokens() );
//                }
//                catch( NoSuchElementException ignore )
//                {
//                }
//                if( values.size() > 0 )
//                {
//                    enums.put( colName, values.toArray( new String[0] ) );
//                }
//            }
//        }
//
//        // try to read SQLServer's constraints
//        // that are stored like sex IN ( 'male', 'female' )
//        // SQL servers keeps them as comments
//        // ([type] = 'JavaScript' or ([type] = 'SQL' or [type] = 'Java'))
//        if( connector.isSQLServer() )
//        {
//            String sql = "SELECT name, definition FROM sys.check_constraints ";
//            sql += "WHERE parent_object_id = OBJECT_ID('" + entity + "') ";
//
//            Map<?,?>checks = null;
//            try
//            {
//                checks = Utils.readAsMap( connector, sql, TableConstraintsCache.getInstance() );
//            }
//            catch( SQLException se )
//            {
//                String likeEnt = entity;
//                if( likeEnt.length() > 9 )
//                {
//                    likeEnt = entity.substring( 0, 9 );
//                }
//
//                sql = "SELECT so.name AS \"name\", sc.text AS \"text\" ";
//                sql += "FROM sysobjects so, syscomments sc  ";
//                sql += "WHERE so.parent_obj = OBJECT_ID( '" + entity + "' ) ";
//                sql += "   AND so.name LIKE 'CK__" + likeEnt + "%' AND sc.ID = so.ID AND LEFT( sc.text, 2 ) = '([' ";
//
//                checks = Utils.readAsMap( connector, sql, TableConstraintsCache.getInstance() );
//            }
//
//            for( Map.Entry<?,?> check : checks.entrySet() )
//            {
//                // extract name stored in CK__queries__type__564B5FDE
//                String ckName = check.getKey().toString();
//                char delim = '\uFFFF';
//                ckName = Utils.subst( ckName, "__", "" + delim + delim, "" );
//                StringTokenizer st = new StringTokenizer( ckName, "" + delim );
//                if( st.countTokens() != 4 )
//                {
//                    continue;
//                }
//                st.nextToken();
//                st.nextToken();
//                String colName = st.nextToken();
//
//                String vals = check.getValue().toString();
//                if( bean != null )
//                {
//                    boolean bFound = false;
//                    for( Iterator<String> props = bean.nameIterator(); props.hasNext(); )
//                    {
//                        String propName = props.next();
//                        //System.out.println( propName + "=>" + colName );
//                        if( propName.startsWith( colName ) )
//                        {
//                            if( vals.indexOf( "[" + propName + "]" ) < 0 )
//                            {
//                                continue;
//                            }
//                            colName = propName;
//                            bFound = true;
//                            break;
//                        }
//                    }
//                    if( !bFound )
//                    {
//                        continue;
//                    }
//                }
//
//                //System.out.println( colName + ": " + vals );
//
//                // ([type] = 'JavaScript' or ([type] = 'SQL' or [type] = 'Java'))
//                vals = Utils.subst( vals, "[" + colName + "] = ", "", "" );
//                vals = Utils.subst( vals, "[" + colName + "]=", "", "" );
//                //System.err.println( vals );
//
//                // ('JavaScript' or ('SQL' or 'Java'))
//                vals = Utils.subst( vals, "' or (", "", "" );
//                vals = Utils.subst( vals, "' OR (", "", "" );
//                //System.err.println( vals );
//
//                // ('JavaScript'SQL' or 'Java'))
//                vals = Utils.subst( vals, "' or ", "", "" );
//                vals = Utils.subst( vals, "' OR ", "", "" );
//                //System.err.println( vals );
//
//                // ('JavaScript'SQL'Java'))
//                vals = vals.substring( 2, vals.lastIndexOf( "')" ) );
//                //System.err.println( vals );
//
//                // JavaScript'SQL'Java
//                st = new StringTokenizer( vals, "'" );
//                ArrayList<String> values = new ArrayList<String>();
//                while( st.hasMoreTokens() )
//                {
//                    String val = st.nextToken();
//                    if( !Utils.isEmpty( val ) )
//                    {
//                        values.add( val );
//                    }
//                }
//                if( values.size() > 0 )
//                {
//                    enums.put( colName, values.toArray( new String[0] ) );
//                }
//            }
//        }
//
//
//        if( connector.isPostgreSQL() )
//        {
//            // Use ANSI SQL complaint schema
//            final String sql = "SELECT cc.column_name, ch.check_clause FROM " +
//                        "information_schema.table_constraints c, " +
//                        "information_schema.constraint_column_usage cc, " +
//                        "information_schema.check_constraints ch " +
//                        "WHERE c.constraint_type = 'CHECK' " +
//                        "AND cc.constraint_name = c.constraint_name " +
//                        "AND cc.constraint_name = ch.constraint_name " +
//                        "AND cc.table_name = '" + Utils.safestr( connector, entity.toLowerCase() ) + "'";
//            Map<?,?>checks = Utils.readAsMap( connector, sql, TableConstraintsCache.getInstance() );
//
//            for( Map.Entry<?,?> entry : checks.entrySet() )
//            {
//                final String colName = entry.getKey().toString().trim();
//                final String check = entry.getValue().toString().trim();
//                // enum-like constraint usually looks like
//                //  ((isdefault)::text = ANY ((ARRAY['no'::character varying, 'yes'::character varying])::text[]))
//
//                final String arrayBegining = "(ARRAY[";
//                int abegin = check.indexOf( arrayBegining );
//                if (abegin == -1)
//                    continue;
//
//                int aend = check.indexOf( "])", abegin );
//                if (aend == -1)
//                    continue;
//
//                String typeList = check.substring( abegin + arrayBegining.length(), aend );
//                StringTokenizer st = new StringTokenizer( typeList, "," );
//                List<String> values = new ArrayList<String>();
//                while (st.hasMoreTokens())
//                {
//                    String t = st.nextToken().trim();
//                    int stInd = t.startsWith( "(" ) ? 1 : 0;
//                    int d = t.indexOf( "::" );
//                    if (d == -1) // wow, that sucks
//                        continue;
//
//                    String val = t.substring( stInd, d );
//
//                    // TODO do it some more effective way
//                    if (val.startsWith( "'" ) && val.endsWith( "'" ))
//                        val = val.substring( 1, val.length() -1 );
//
//                    // now 'val' contains 'yes' or 'no'
//                    values.add(val);
//                }
//                if( values.size() > 0 )
//                {
//                    enums.put( colName, values.toArray( new String[0] ) );
//                }
//            }
//
//        }
//
//        if( connector.isSQLite() )
//        {
//            final String sql = "SELECT sql FROM sqlite_master WHERE type = 'table' AND tbl_name = '" + Utils.safestr( connector, entity ) + "'";
//
//            List<String>checks = Utils.readAsListOfStrings( connector, sql, TableConstraintsCache.getInstance() );
//            if( checks.size() == 1 )
//            {
//                 String orig = checks.get( 0 );
//                 for( DynamicProperty prop : bean )
//                 {
//                     int ind1, ind2;
//                     String vals = null;
//
//                     String pat1 = "/*" + prop.getName().toUpperCase() + ":ENUM(";
//                     ind1 = orig.indexOf( pat1 );
//                     if( ind1 > 0 )
//                     {
//                         ind2 = orig.indexOf( ")", ind1 );
//                         if( ind2 > 0 )
//                         {
//                             vals = orig.substring( ind1 + pat1.length(), ind2 );
//                         }
//                     }
//
//                     String pat2 = "/*" + prop.getName().toUpperCase() + " :ENUM(";
//                     ind1 = orig.indexOf( pat2 );
//                     if( ind1 > 0 )
//                     {
//                         ind2 = orig.indexOf( ")", ind1 );
//                         if( ind2 > 0 )
//                         {
//                             vals = orig.substring( ind1 + pat2.length(), ind2 );
//                         }
//                     }
//
//                     if( vals != null )
//                     {
//                         ArrayList<String> values = new ArrayList<String>();
//                         StringTokenizer st = new StringTokenizer( vals, ",'" );
//                         while( st.hasMoreTokens() )
//                         {
//                             String tok = st.nextToken();
//                             if( !"".equals( tok.trim() ) )
//                             {
//                                 values.add( tok );
//                             }
//                         }
//                         enums.put( prop.getName(), values.toArray( new String[0] ) );
//                     }
//                 }
//            }
//        }
//
//        return enums;
//    }
//
//    /**
//     * This method is used for generic collections to display names of its owner
//     * so instead of 'companies.1' we will see 'DevelopmentOnTheEdge.com'
//     * This must be done only in case if the property is read-only
//     *
//     * @param connector
//     * @param bean
//     * @throws Exception
//     */
//    protected void addOwnerNamesForROProperties( DatabaseService connector, DynamicPropertySet bean )
//            throws Exception
//    {
//        try
//        {
//            String e_sql = "SELECT e.type AS \"et\", tr.columnsFrom AS \"cf\" " +
//                    " FROM entities e, table_refs tr " +
//                    " WHERE tr.tableFrom = e.name AND e.name = '" + entity + "' AND tr.columnsTo IS NULL";
//            QRec eRec = QRec.withCache( connector, e_sql, ReferencesQueriesCache.getInstance() );
//
//            // we are selecting generic references rather than generic collections
//            // therfore commented out
//            //if( !ENTITY_TYPE_GENERIC_COLLECTION.equals( eRec.getString( "et" ) ) )
//            //    return;
//
//            String name = eRec.getString( "cf" );
//            DynamicProperty prop = bean.getProperty( name );
//            if( prop == null )
//            {
//                return;
//            }
//
//
//            if( !prop.isReadOnly() )
//            {
//                return;
//            }
//            String[] tags = ( String[] )prop.getAttribute( TAG_LIST_ATTR );
//            if( tags != null )
//            {
//                return;
//            }
//
//            Object value = prop.getValue();
//            if( !( value instanceof String ) )
//            {
//                return;
//            }
//            int ind = ( ( String )value ).lastIndexOf( '.' );
//            if( ind < 1 )
//            {
//                return;
//            }
//
//            String tableTo = ( ( String )value ).substring( 0, ind );
//            String pk = Utils.findPrimaryKeyName( connector, tableTo );
//
//            QueryExecuter qe = makeQueryExecuter( connector );
//
//            qe.enableLocalization();
//
//            try
//            {
//                qe.makeIterator( tableTo, SELECTION_VIEW,
//                        Collections.singletonMap( pk, ( ( String )value ).substring( ind + 1 ) ) );
//                String[][] vals = qe.calcUsingQuery();
//                if( vals.length != 1 )
//                {
//                    return;
//                }
//                ind = vals[ 0 ].length - 1;
//                if( ind > 1 )
//                {
//                    ind = 1;
//                }
//                String collOwner = vals[ 0 ][ ind ];
//                if( collOwner == null )
//                {
//                    return;
//                }
//                prop.setAttribute( TAG_LIST_ATTR,
//                        new String[]{ collOwner + TAG_DELIMITER + value } );
//            }
//            catch( Exception ign )
//            {
//            }
//            finally
//            {
//                qe.closeIterator();
//            }
//        }
//        catch( QRec.NoRecord ignore )
//        {
//        }
//    }
//
//    public String getRecordName( DatabaseService connector, String entity, String recordID )
//        throws Exception
//    {
//        String pk = Utils.findPrimaryKeyName( connector, entity );
//
//        QueryExecuter qe = makeQueryExecuter( connector );
//
//        qe.enableLocalization();
//
//        try
//        {
//            qe.makeIterator( entity, SELECTION_VIEW, Collections.singletonMap( pk, recordID ) );
//            String[][] vals = qe.calcUsingQuery();
//            if( vals.length != 1 )
//            {
//                return null;
//            }
//            int ind = vals[ 0 ].length - 1;
//            if( ind > 1 )
//            {
//                ind = 1;
//            }
//            String collOwner = vals[ 0 ][ ind ];
//            if( collOwner == null )
//            {
//                return null;
//            }
//            return collOwner;
//        }
//        catch( Exception ign )
//        {
//        }
//        finally
//        {
//            qe.closeIterator();
//        }
//        return null;
//    }
//
//
//    public static String makeBetterDisplayName( String colName )
//    {
//        if( colName.indexOf( ' ' ) >= 0 )
//        {
//            // i.e. stored with spaces in database - already formatted
//            return "";
//        }
//
//
//        if( SQLHelper.SYSTEM_FIELDS.contains( colName ) )
//        {
//            colName = Utils.subst( colName, "___", "" );
//        }
//        else if( colName.toLowerCase().startsWith( ENCRYPT_COLUMN_PREFIX ) )
//        {
//            colName = colName.substring( ENCRYPT_COLUMN_PREFIX.length() );
//        }
//
//        return DynamicPropertySetSupport.makeBetterDisplayName( colName );
//    }
//
//    /**
//     * Set property display name the best advantage for column.
//     * </br>See also
//     * {@link #makeBetterDisplayName(String) makeBetterDisplayName(String)}.
//     *
//     * @param colName column Name
//     * @param prop property for set display name
//     */
//    protected static void makeBetterDisplayName( String colName, DynamicProperty prop )
//    {
//        String label = makeBetterDisplayName( colName );
//
//        if( label.length() > 0 )
//        {
//            prop.setDisplayName( label );
//        }
//    }
//
//    /**
//     * Apply style display properties (bean), against the meta data for given entity.
//     *
//     * This method for each column defines the following properties such as,
//     * display name, short description, read only, expert, auto increment, default value, column size, null value.
//     *
//     * @param connector DB connector
//     * @param entity entity name
//     * @param pk primary key
//     * @param bean properties
//     * @param presetValues map with preset values columns
//     * @param isInsert
//     * @throws Exception
//     */
//
//    public static void applyMetaData( DatabaseService connector, String entity, String pk,
//                                         DynamicPropertySet bean, Map presetValues,
//                                         boolean isInsert )
//            throws Exception
//    {
//        applyMetaData( connector, entity, pk, bean, presetValues, isInsert, null );
//    }
//
//    public static void applyMetaData( DatabaseService connector, String entity, String pk,
//                                         DynamicPropertySet bean, Map presetValues,
//                                         boolean isInsert, String tcloneId )
//            throws Exception
//    {
//        HashSet<String> db2Identities = null, db2Generated = null;
//        if( connector.isDb2() )
//        {
//            db2Identities = DB2SpecialColumnsCache.getIdentities( entity );
//            db2Generated = DB2SpecialColumnsCache.getGenerated( entity );
//            if ( db2Identities == null || db2Generated == null )
//            {
//                db2Identities = new HashSet<String>();
//                db2Generated = new HashSet<String>();
//                String hsql = "SELECT NAME, IDENTITY, GENERATED FROM SYSIBM.SYSCOLUMNS ";
//                hsql += "WHERE TBNAME = UCASE( '" + entity + "' ) AND (IDENTITY = 'Y' OR GENERATED <> '')";
//                ResultSet hrs = null;
//                try
//                {
//                    hrs = connector.executeQuery( hsql );
//                    while( hrs.next() )
//                    {
//                        if ( "Y".equals( hrs.getString( 2 ) ) )
//                            db2Identities.add( hrs.getString( 1 ) );
//                        if ( !Utils.isEmpty( hrs.getString( 3 ) ) )
//                            db2Generated.add( hrs.getString( 1 ) );
//                    }
//                }
//                finally
//                {
//                    connector.close( hrs );
//                }
//                DB2SpecialColumnsCache.putIdentities( entity, db2Identities );
//                DB2SpecialColumnsCache.putGenerated( entity, db2Generated );
//            }
//        }
//
//        Map<String, String> mysql5colInfo = new HashMap<String, String>();
//        if( connector.isMySQL5() || connector.isMySQL41() )
//        {
//            List<DynamicPropertySet> cols = Utils.readAsRecords( connector, "DESC " + entity, MysqlColumnsCache.getInstance() );
//            for( DynamicPropertySet col : cols )
//            {
//                if( !Utils.isEmpty( col.getValueAsString( "Extra" ) ) )
//                {
//                    mysql5colInfo.put( col.getValueAsString( "Field" ), col.getValueAsString( "Extra" ) );
//                }
//            }
//        }
//
//        Map<String, String> sqliteColInfo = new HashMap<String, String>();
//        if( connector.isSQLite() )
//        {
//            List<DynamicPropertySet> cols = Utils.readAsRecords( connector, "pragma table_info(" + entity + ")", MysqlColumnsCache.getInstance() );
//            for( DynamicPropertySet col : cols )
//            {
//                //System.err.println( "col = " + col );
//                if( "1".equals( col.getValueAsString( "pk" ) ) && "integer".equals( col.getValueAsString( "type" ).toLowerCase() ) )
//                {
//                    sqliteColInfo.put( col.getValueAsString( "name" ), "true" );
//                }
//            }
//        }
//
//        Connection conn = null;
//        try
//        {
//            conn = connector.getConnection();
//
//            boolean isDb2AppDriver = connector.isDb2AppDriver();
//            //RSWrapper columnRs = Utils.getEntityColumns( connector, conn, entity + Utils.ifNull( tcloneId, "" ) );
//            RSWrapper columnRs = Utils.getEntityColumns( connector, conn, entity );
//            //if( "persons2households".equals( entity ) ) System.out.println( "entity = " + entity );
//            while( columnRs.next() )
//            {
//                String colName = columnRs.getString( 4 ); //columnRs.getString( "COLUMN_NAME" );
//                //if( "persons2households".equals( entity ) ) System.out.println( "colName = " + colName );
//                DynamicProperty prop = bean.getProperty( colName );
//                if( prop == null )
//                {
//                    continue;
//                }
//
//                if( isDb2AppDriver ) // for some reason it marks everything as read only
//                {
//                    prop.setReadOnly( false );
//                }
//
//                makeBetterDisplayName( colName, prop );
//
//                if( SQLHelper.SYSTEM_FIELDS.contains( colName ) )
//                {
//                    prop.setExpert( true );
//                }
//
//                if( tcloneId != null && SQLHelper.IMPORT_DB_FIELDS.contains( colName ) )
//                {
//                    prop.setExpert( true );
//                }
//
//                String remarks = null;
//                if( connector.isMySQL5() || connector.isMySQL41() )
//                {
//                    remarks = mysql5colInfo.get( colName );
//                }
//                else
//                {
//                    remarks = columnRs.getString( 12 ); //columnRs.getString( "REMARKS" );
//                }
//
//                if( remarks != null && remarks.indexOf( "auto_increment" ) >= 0 )
//                {
//                    // auto increment fields for MySQL
//                    prop.setExpert( true );
//                    prop.setReadOnly( true );
//                    prop.setAttribute( JDBCRecordAdapter.AUTO_IDENTITY, Boolean.TRUE );
//                }
//                else if( !Utils.isEmpty( remarks ) )
//                {
//                    prop.setShortDescription( remarks );
//                }
//
//                String typeName = columnRs.getString( 6 ); //columnRs.getString( "TYPE_NAME" );
//                //if( "persons2households".equals( entity ) ) System.out.println( "---typeName = " + typeName );
//                if( connector.isODBC() && "COUNTER".equals( typeName ) )
//                {
//                    // auto increment fields for MS Jet
//                    prop.setExpert( true );
//                    prop.setReadOnly( true );
//                    prop.setAttribute( JDBCRecordAdapter.AUTO_IDENTITY, Boolean.TRUE );
//                }
//                else if( connector.isSQLServer() && typeName.endsWith( "identity" ) )
//                {
//                    // auto increment fields for MS SQL Server
//                    prop.setExpert( true );
//                    prop.setReadOnly( true );
//                    prop.setAttribute( JDBCRecordAdapter.AUTO_IDENTITY, Boolean.TRUE );
//                }
//                else if( connector.isDb2() && db2Identities.contains( colName ) )
//                {
//                    // auto increment fields for DB2
//                    prop.setExpert( true );
//                    prop.setReadOnly( true );
//                    prop.setAttribute( JDBCRecordAdapter.AUTO_IDENTITY, Boolean.TRUE );
//                }
//                else if( connector.isDb2() && db2Generated.contains( colName ) )
//                {
//                    // generated fields for DB2
//                    prop.setExpert( true );
//                    prop.setReadOnly( true );
//                    prop.setAttribute( WebFormPropertyInspector.PSEUDO_PROPERTY, Boolean.TRUE );
//                }
//                else if( connector.isPostgreSQL() && ( pk == null || colName.equalsIgnoreCase( pk ) || "ID".equalsIgnoreCase( colName ) ) &&
//                         ( "SERIAL".equalsIgnoreCase( typeName ) || "BIGSERIAL".equalsIgnoreCase( typeName ) ) )
//                {
//                    // auto increment fields for DB2
//                    prop.setExpert( true );
//                    prop.setReadOnly( true );
//                    prop.setAttribute( JDBCRecordAdapter.AUTO_IDENTITY, Boolean.TRUE );
//                }
//                else if( connector.isSQLite() && sqliteColInfo.containsKey( colName ) )
//                {
//                    prop.setExpert( true );
//                    prop.setReadOnly( true );
//                    prop.setAttribute( JDBCRecordAdapter.AUTO_IDENTITY, Boolean.TRUE );
//                }
//
///*
//                else if( "YES".equals( columnRs.getString( "IS_AUTOINCREMENT" ) ) )
//                {
//                    prop.setExpert( true );
//                    prop.setReadOnly( true );
//                    prop.setAttribute( JDBCRecordAdapter.AUTO_IDENTITY, Boolean.TRUE );
//                }
//*/
//
//                String defValue = columnRs.getString( 13 ); //columnRs.getString( "COLUMN_DEF" );
//                if( !Utils.isEmpty( defValue ) && "NULL".equalsIgnoreCase( defValue = defValue.trim() ) )
//                {
//                    defValue = null;
//                }
//
//                if( !Utils.isEmpty( defValue ) )
//                {
//                    if( connector.isSQLServer() )
//                    {
//                        while( defValue.startsWith( "(" ) )
//                        {
//                            defValue = defValue.substring( 1, defValue.length() - 1 );
//                        }
//                    }
//
//                    if( defValue.startsWith( "'" ) )
//                    {
//                        defValue = defValue.substring( 1 );
//                    }
//                    if( defValue.endsWith( "'" ) )
//                    {
//                        defValue = defValue.substring( 0, defValue.length() - 1 );
//                    }
//
//                    // trick for Oracle to have auto-generated IDs
//                    //System.out.println( entity + "." + colName + ": defValue = '" + defValue + "'" );
//                    if( connector.isOracle()
//                        && colName.equalsIgnoreCase( pk )
//                        && ( defValue.equalsIgnoreCase( entity ) ||
//                             JDBCRecordAdapter.AUTO_IDENTITY.equals( defValue ) ||
//                             defValue.equalsIgnoreCase( entity + "_" + pk + "_seq" ) ) )
//                    {
//                        prop.setExpert( true );
//                        prop.setReadOnly( true );
//                    }
//
//                    if( !connector.isMySQL5() && !connector.isPostgreSQL() || !"TIMESTAMP".equalsIgnoreCase( typeName ) )
//                    {
//                        prop.setAttribute( BeanInfoConstants.DEFAULT_VALUE, defValue );
//                    }
//
//                    if( connector.isPostgreSQL() )
//                    {
//                        if( JDBCRecordAdapter.AUTO_IDENTITY.equals( defValue ) )
//                        {
//                            // TODO what for it is "expert" ?
//                            prop.setExpert( true );
//                            prop.setReadOnly( true );
//                            prop.setAttribute( JDBCRecordAdapter.AUTO_IDENTITY, Boolean.TRUE );
//                        }
//                        else
//                        {
//                            if( !defValue.startsWith( "nextval" ) )
//                            {
//                                if( defValue.startsWith( "to_date" ) )
//                                {
//                                    //looks  like: to_date('1900-01-01'::text, 'YYYY-MM-DD'::text)
//                                    // convert to: to_date('1900-01-01', 'YYYY-MM-DD')
//                                    defValue = defValue.replaceAll( "::text", "" );
//                                }
//                                else
//                                {
//                                    // remove type cast from 'value'::type
//                                    // but skip it if it's default value taken
//                                    // from automatically generated sequence
//                                    int i = defValue.lastIndexOf( "::" );
//                                    if( i >= 0 )
//                                    {
//                                        defValue = defValue.substring( 0, i );
//                                    }
//                                    if( defValue.charAt( 0 ) == '\'' )
//                                    {
//                                        defValue = defValue.substring( 1 );
//                                    }
//                                    if( defValue.endsWith( "'" ) )
//                                    {
//                                        defValue = defValue.substring( 0, defValue.length() - 1 );
//                                    }
//                                }
//                            }
//                            prop.setAttribute( BeanInfoConstants.DEFAULT_VALUE, defValue );
//                        }
//                    }
//
//                    boolean isFunction = defValue.indexOf( "(" ) > 0 && Character.isLetter( defValue.charAt( 0 ) )
//                                         || connector.isOracle() && "SYSDATE".equals( defValue );
//
//                    //System.out.println( "> defValue = '" + defValue + "'" );
//                    if( isFunction && !defValue.startsWith( "nextval" ) )
//                    {
//                        String hackVal1 = "CONVERT([datetime],'";
//                        String hackVal2 = "CONVERT([date],'";
//                        if( defValue.startsWith( hackVal1 ) )
//                        {
//                            defValue = defValue.substring( hackVal1.length(), hackVal1.length() + 19 );
//                        }
//                        else if( defValue.startsWith( hackVal2 ) )
//                        {
//                            defValue = defValue.substring( hackVal2.length(), hackVal2.length() + 10 );
//                        }
//                        else
//                        {
//                            String sql = connector.getAnalyzer().makeSingleExprSelect( defValue );
//                            //System.out.println( "sql = '" + sql + "'" );
//                            defValue = QRec.withCache( connector, sql, DefaultFunctionValuesCache.getInstance() ).getString();
//                            //System.out.println( "1 < defValue = '" + defValue + "'" );
//                        }
//                        //System.out.println( "2 < defValue = '" + defValue + "'" );
//                        prop.setAttribute( BeanInfoConstants.DEFAULT_VALUE, defValue );
//                    }
//                }
//
//                if( "YES".equals( columnRs.getString( /*"IS_NULLABLE"*/ 18 ) ) )
//                {
//                    prop.setCanBeNull( true );
//                }
//                else if( connector.isSQLServer() && !Utils.isEmpty( defValue ) && defValue.startsWith( "1753-01-01 00:00:00" ) )
//                {
//                    //System.out.println( " ----- defValue = '" + defValue + "'" );
//                    prop.setAttribute( BeanInfoConstants.DEFAULT_VALUE, Utils.currentTimestamp() );
//                    prop.setAttribute( InsertOperation.PUT_DBMS_DATETIME_PLACEHOLDER_FLAG, Boolean.TRUE );
//                }
//                else if( Utils.isEmpty( defValue ) || defValue.startsWith( "0000-00-00" ) || defValue.startsWith( "0001-01-01" ) )
//                {
//                    String type = typeName.toUpperCase();
//                    if( "DATE".equals( type ) )
//                    {
//                        prop.setAttribute( BeanInfoConstants.DEFAULT_VALUE, Utils.currentDate() );
//                        prop.setAttribute( InsertOperation.PUT_DBMS_DATE_PLACEHOLDER_FLAG, Boolean.TRUE );
//                    }
//                    else if( "DATETIME".equals( type ) || "TIMESTAMP".equals( type ) || "TIMESTAMP(6)".equals( type ) )
//                    {
//                        prop.setAttribute( BeanInfoConstants.DEFAULT_VALUE, Utils.currentTimestamp() );
//                        prop.setAttribute( InsertOperation.PUT_DBMS_DATETIME_PLACEHOLDER_FLAG, Boolean.TRUE );
//                    }
//                }
//
//                if( !Utils.isEmpty( defValue ) )
//                {
//                    String type = typeName.toUpperCase();
//                    if( "FLOAT".equals( type ) )
//                    {
//                        prop.setAttribute( BeanInfoConstants.DEFAULT_VALUE, new Float( defValue ) );
//                    }
//                    else if( "DOUBLE".equals( type ) )
//                    {
//                        prop.setAttribute( BeanInfoConstants.DEFAULT_VALUE, new Double( defValue ) );
//                    }
//                    else if( "TIME".equals( type ) )
//                    {
//                        java.sql.Time timeValue = new java.sql.Time( new SimpleDateFormat( "HH:mm:ss" ).parse( defValue ).getTime() );
//                        prop.setAttribute( BeanInfoConstants.DEFAULT_VALUE, timeValue );
//                    }
//                    else if( "DATE".equals( type ) && !defValue.startsWith( "0000-00-00" ) && !defValue.startsWith( "0001-01-01" ) )
//                    {
//                        java.sql.Date dateValue = new java.sql.Date( new SimpleDateFormat( "yyyy-MM-dd" ).parse( defValue ).getTime() );
//                        prop.setAttribute( BeanInfoConstants.DEFAULT_VALUE, dateValue );
//                    }
//                }
//
//                if( "CLOB".equals( prop.getAttribute( JDBCRecordAdapter.DATABASE_TYPE_NAME ) ) )
//                {
//                    if( connector.isDb2() )
//                    {
//                        prop.setAttribute( COLUMN_SIZE_ATTR, columnRs.getString( /*"COLUMN_SIZE"*/ 7 ) );
//                    }
//                    else
//                    {
//                        // TODO remove this hardcoding for other bases
//                        prop.setAttribute( COLUMN_SIZE_ATTR, "1048576" );
//                    }
//                }
//                else
//                {
//                    if( connector.isSQLite() )
//                    {
//                        String type = typeName.toUpperCase();
//                        if( type.startsWith( "VARCHAR(" ) && type.endsWith( ")" ) )
//                        {
//                             prop.setAttribute( COLUMN_SIZE_ATTR, type.substring( 8, type.length() - 1 ).trim() );
//                             //System.out.println( typeName + "-->" + typeName.substring( 8, typeName.length() - 1 ) );
//                        }
//                        else if( type.startsWith( "DATE" ) || type.startsWith( "TIME" ) ||
//                            type.startsWith( "FLOAT" ) || type.startsWith( "DOUBLE" ) || type.startsWith( "DECIMAL" ) ||
//                            type.startsWith( "INTEGER" )
//                          )
//                        {
//                             prop.setAttribute( COLUMN_SIZE_ATTR, "50" );
//                             //System.out.println( typeName + "-->" + typeName.substring( 8, typeName.length() - 1 ) );
//                        }
//                        else
//                        {
//                             prop.setAttribute( COLUMN_SIZE_ATTR, columnRs.getString( /*"COLUMN_SIZE"*/ 7 ).trim() );
//                        }
//                    }
//                    else
//                    {
//                         prop.setAttribute( COLUMN_SIZE_ATTR, columnRs.getString( /*"COLUMN_SIZE"*/ 7 ) );
//                    }
//                }
//
//                Object presetVal = presetValues.get( colName );
//
//                if( presetVal != null )
//                {
//                    if( !isInsert || !presetVal.getClass().isArray() )
//                    {
//                        prop.setReadOnly( true );
//                    }
//                    // otherwise i.e. insert and array of presets
//                    // assume that taglist will be provided
//                }
//            }
//        }
//        finally
//        {
//
////        connector.close( columnRs );
//            connector.releaseConnection( conn );
//        }
//
//        /*
//         * Temporarily commented out - it seems the idea of pseudo properties requires more thought
//         *
//         Iterator iter = presetValues.keySet().iterator();
//         while(iter.hasNext())
//         {
//             String key = (String)iter.next();
//             PropertyDescriptor pd = bean.getPropertyDescriptor(key);
//             if (pd == null)
//             {
//                 Object value = presetValues.get(key);
//                 DynamicProperty prop = new DynamicProperty( key, value.getClass(), value);
//                 prop.setAttribute( BeanInfoConstants.READ_ONLY,  Boolean.TRUE );
//                 prop.setAttribute( WebFormPropertyInspector.PSEUDO_PROPERTY,  Boolean.TRUE );
//                 makeBetterDisplayName( key, pd );
//                 bean.add(prop);
//             }
//         }
//         */
//
//        if( connector.isDb2() )
//        {
//            String db2ConstrSql = "SELECT TEXT FROM SYSCAT.CHECKS WHERE tabName = " + Utils.safestr( connector, entity, true ).toUpperCase();
//            ResultSet rs = null;
//            try
//            {
//                rs = connector.executeQuery( db2ConstrSql );
//                while( rs.next() )
//                {
//                    String constraint = rs.getString( 1 ).trim();
//                    if( constraint.endsWith( "IS NOT NULL" ) )
//                    {
//                        String field = constraint.substring( 0, constraint.length() - "IS NOT NULL".length() ).trim();
//                        DynamicProperty prop = bean.getProperty( field );
//                        if( prop != null )
//                            prop.setCanBeNull( false );
//                    }
//                }
//            }
//            finally
//            {
//                connector.close( rs );
//            }
//        }
//
//    }
//
//    /**
//     * Apply short descriptions for columns of the passed entity.
//     * <br/>Can apply short description for property (if connector is Oracle),
//     * or property type and it's value (if connector is SQL Server 2005).
//     *
//     * @param connector DB connector
//     * @param entity entity name
//     * @param pk primary key
//     * @param bean
//     * @param locale use if connector is SQL Server 2005
//     * @throws Exception
//     */
//    protected static void applyColumnDescriptions( DatabaseService connector, String entity, String pk,
//                                                   DynamicPropertySet bean, UserInfo userInfo )
//            throws Exception
//    {
//        if( "colorSchemes".equalsIgnoreCase( entity ) )
//        {
//            for( Iterator<DynamicProperty> props = bean.propertyIterator(); props.hasNext(); )
//            {
//                DynamicProperty prop = props.next();
//                String name = prop.getName();
//                if( name.equalsIgnoreCase( "ID" ) )
//                    continue;
//                if( name.equalsIgnoreCase( "name" ) )
//                    continue;
//                if( name.equalsIgnoreCase( "contextID" ) )
//                    continue;
//                prop.setAttribute( COLOR_PICKER, Boolean.TRUE );
//            }
//        }
//
//        if( connector.isOracle() )
//        {
//            String sql = "SELECT ucc.COLUMN_NAME AS \"Col\", ucc.COMMENTS as \"Descr\" ";
//            sql += "FROM user_col_comments ucc ";
//            sql += "WHERE ucc.table_name = UPPER( '" + entity + "' ) AND ucc.COMMENTS IS NOT NULL";
//            ResultSet rs = null;
//            try
//            {
//                rs = connector.executeQuery( sql );
//                while( rs.next() )
//                {
//                    DynamicProperty prop = bean.getProperty( rs.getString( "Col" ) );
//                    if( prop == null )
//                    {
//                        continue;
//                    }
//                    prop.setShortDescription( rs.getString( "Descr" ) );
//                }
//            }
//            finally
//            {
//                connector.close( rs );
//            }
//        }
//        else if( connector.isSQLServer2005() )
//        {
//            String sql = "SELECT sc.name AS \"columnName\", sep.name AS \"propName\", CAST( sep.value AS VARCHAR(1000) ) AS \"propValue\" ";
//            sql += "FROM sys.extended_properties sep, sys.columns sc ";
//            sql += "WHERE sep.major_id = sc.object_id AND sep.minor_id = sc.column_id  ";
//            sql += "    AND OBJECT_ID( '" + entity + "' ) = sc.object_id ";
//            ResultSet rs = null;
//            try
//            {
//                rs = connector.executeQuery( sql );
//                while( rs.next() )
//                {
//                    DynamicProperty prop = bean.getProperty( rs.getString( "columnName" ) );
//                    if( prop == null )
//                    {
//                        continue;
//                    }
//                    if( "JavaClass".equals( rs.getString( "propName" ) ) )
//                    {
//                        Class newType = Class.forName( rs.getString( "propValue" ) );
//                        prop.setType( newType );
//                        prop.setValue( Utils.changeType( prop.getValue(), newType, userInfo ) );
//                    }
//                }
//            }
//            catch( SQLException exc )
//            {
//                Logger.warn( cat, "applyColumnDescriptions: Are we running on Azure?: " + exc.getMessage() );
//            }
//            finally
//            {
//                connector.close( rs );
//            }
//        }
//    }
//
//    public static void disableValidation( DynamicPropertySet bean )
//            throws Exception
//    {
//        for( Iterator<DynamicProperty> props = bean.propertyIterator(); props.hasNext(); )
//        {
//            DynamicProperty prop = props.next();
//            prop.setAttribute( Validation.RULES_ATTR, Collections.EMPTY_MAP );
//        }
//    }
//
//    /**
//     * Get properties ( {@link com.beanexplorer.beans.DynamicProperty} ) for the specified entity. This method uses
//     * {@link com.beanexplorer.enterprise.Utils#getEntityColumns(DatabaseService, Connection, String)} to retrieve dynamic properties.
//     *
//     * @param connector DB connector
//     * @param entity entity name
//     * @return set properties for entity
//     * @throws SQLException
//     */
//    public static DynamicPropertySet getBeanFromEmptyTable(
//            DatabaseService connector, String entity )
//            throws SQLException
//    {
//        //System.out.println( "getBeanFromEmptyTable = " + entity );
//        Connection conn = null;
//        ResultSet rs = null;
//        DynamicPropertySet parameters = null;
//
//        try
//        {
//            conn = connector.getConnection();
//            rs = Utils.getEntityColumnsAsResultset( connector.getAnalyzer(), conn, entity );
//            parameters = new EntityRecordAdapter( connector, rs );
//        }
//        finally
//        {
//            connector.close( rs );
//            connector.releaseConnection( conn );
//        }
//        return parameters;
//    }
//
//    protected void addCategoryAttributes( DatabaseService connector,
//        DynamicPropertySet bean, boolean isInsert )
//            throws Exception
//    {
//        //if( !Utils.hasFeature( connector, Features.CATEGORY_ATTRIBUTES ) )
//        if( !Utils.hasModule( connector, "attributes" ) )
//        {
//            return;
//        }
//
//        if( Utils.isEmpty( category ) )
//        {
//            return;
//        }
//
//        String sql = "SELECT ID, publicID, name, type, canBeNull, defaultValue, dictionary ";
//        sql += "FROM attributes WHERE categoryID = " + Utils.safePKValue( connector, "categories", category );
//
//        List<DynamicPropertySet> attrs = Utils.readAsRecords( connector, sql, CategorizedAttributeCache.getInstance() );
//        if( attrs.size() == 0 )
//            return;
//
//        Map valueMap = Collections.EMPTY_MAP;
//        if( !isInsert && records.length == 1 ) // editing single record
//        {
//            String recsSql = "SELECT attributeID, value FROM attributeValues WHERE recordID = '" +
//                entity + "." + records[ 0 ] + "'";
//            valueMap = Utils.readAsMap( connector, recsSql );
//        }
//
//        for( DynamicPropertySet attr : attrs )
//        {
//            String ID = attr.getValueAsString( "ID" );
//            String name = attr.getValueAsString( "publicID" );
//            String displayName = attr.getValueAsString( "name" );
//            String type = attr.getValueAsString( "type" );
//            boolean canBeNull = "yes".equals( attr.getValueAsString( "canBeNull" ) );
//            String defValue = attr.getValueAsString( "defaultValue" );
//            String dict = attr.getValueAsString( "dictionary" );
//
//            Class clazz = String.class;
//            if( type.equals( "number" ) )
//                clazz = Double.class;
//            else if( type.equals( "date" ) )
//                clazz = java.sql.Date.class;
//            else if( type.equals( "currency" ) )
//                clazz = Double.class;
//
//            DynamicProperty prop = new DynamicProperty( name, displayName, clazz );
//            prop.setAttribute( BeanInfoConstants.GROUP_ID, "attributes" );
//            prop.setAttribute( BeanInfoConstants.GROUP_NAME, localizedMessage( "Additional properties" ) );
//            prop.setAttribute( CATEGORY_ATTRIBUTE_ID, ID );
//            prop.setCanBeNull( canBeNull );
//            String valStr = ( String )valueMap.get( ID );
//            if( valStr == null && isInsert )
//                valStr = defValue;
//            prop.setValue( Utils.changeType( valStr, clazz ) );
//            if( !Utils.isEmpty( dict ) )
//            {
//                String []tags = getTagsFromSelectionView( connector, dict );
//                prop.setAttribute( TAG_LIST_ATTR, tags );
//            }
//            bean.add( prop );
//        }
//    }
//
//    protected void saveCategoryAttributes( DatabaseService connector,
//        DynamicPropertySet bean, String []owners, boolean isInsert )
//            throws Exception
//    {
//        //if( !Utils.hasFeature( connector, Features.CATEGORY_ATTRIBUTES ) )
//        if( !Utils.hasModule( connector, "attributes" ) )
//        {
//            return;
//        }
//
//        if( Utils.isEmpty( category ) )
//        {
//            return;
//        }
//
//        DatabaseAnalyzer an = connector.getAnalyzer();
//
//        String importId = Utils.ifNull( tcloneId, "" );
//        java.sql.Date actualityDate = Utils.currentDate();
//
//        BatchExecutor batch = new BatchExecutor( connector, true );
//        for( Iterator<DynamicProperty> props = bean.propertyIterator(); props.hasNext(); )
//        {
//            DynamicProperty prop = props.next();
//            Object ID = prop.getAttribute( CATEGORY_ATTRIBUTE_ID );
//            if( ID == null )
//            {
//                continue;
//            }
//            if( !isInsert )
//            {
//                String savePrev = connector.getAnalyzer().makeInsertIntoWithAutoIncrement( "attributeValueHistory" + importId, "ID" ) +
//                   " ownerID, attributeID, value, beginDate, endDate, importID, whoInserted___, whoModified___, creationDate___, modificationDate___ ) " +
//                    connector.getAnalyzer().makeInsertAsSelectWithAutoIncrement() +
//                   " ownerID, attributeID, value, beginDate, '" + actualityDate + "', importID, whoInserted___, whoModified___, creationDate___, modificationDate___ " +
//                   " FROM attributeValues" + importId + " WHERE ownerID IN " + Utils.toInClause( owners, false, entity + "." ) +
//                   "     AND attributeID = " + Utils.safePKValue( connector, "attributes", ID );
//
//                batch.add( savePrev );
//
//                batch.add( "DELETE attributeValues" + importId + " WHERE " +
//                   " attributeID = " + Utils.safePKValue( connector, "attributes", ID ) +
//                   " AND ownerID IN " + Utils.toInClause( owners, false, entity + "." ) );
//            }
//
//            Object value = prop.getValue();
//            if( value == null )
//            {
//                continue;
//            }
//
//            value = value instanceof java.util.Date ? new java.sql.Date( ( ( java.util.Date )value ).getTime() ).toString() : value;
//
//            String sql = an.makeInsertIntoWithAutoIncrement( "attributeValues" + importId, "ID" );
//            sql += "attributeID, ownerID, value, beginDate, importID,";
//            sql += DatabaseConstants.WHO_INSERTED_COLUMN_NAME + ", " + DatabaseConstants.CREATION_DATE_COLUMN_NAME;
//            sql += " ) ";
//            sql += an.makeInsertAsSelectWithAutoIncrement();
//            sql += Utils.safePKValue( connector, "attributes", ID ) + ", ";
//            sql += an.makeGenericRefExpr( entity, primaryKey ) + ", ";
//            sql += Utils.safestr( connector, value.toString(), true ) + ", ";
//            sql += "'" + actualityDate + "', " + importId + ", ";
//            sql += Utils.safestr( connector, userInfo.getUserName(), true ) + ", " + an.getCurrentDateTimeExpr();
//            sql += " FROM " + entity + " WHERE " + primaryKey + " IN " +
//                 Utils.toInClause( owners, Utils.isNumericColumn( connector, entity, primaryKey ) );
//            batch.add( sql );
//        }
//        batch.flush();
//    }
//
//    public static String[] stripDisplayNamesFromTagList( String []tags )
//    {
//        if( tags == null || tags.length < 1 )
//        {
//            return tags;
//        }
//        ArrayList<String> ret = new ArrayList<String>();
//        for( int i = 0; i < tags.length; i++ )
//        {
//            String[] pair = tags[ i ].split( TAG_DELIMITER );
//            String value = pair.length > 1 ? pair[ 1 ] : pair[ 0 ];
//            ret.add( value );
//        }
//        return ret.toArray( new String[0] );
//    }
//
//
//    /**
//     * Get current user info.
//     */
//    @Override
//    public final UserInfo getUserInfo()
//    {
//        return userInfo;
//    }
//
    /**
     * Get entity name.
     */
    @Override
    public final String getEntity()
    {
        return entity;
    }

    /**
     * Get operation name.
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Set operation name.
     */
    @Override
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Get operation name.
     */
    @Override
    public final String getOrigName()
    {
        return origName;
    }

    /**
     * Set operation name.
     */
    @Override
    public final void setOrigName( String name )
    {
        this.origName = name;
    }
//
//    public final String getFromQuery()
//    {
//        return fromQuery;
//    }
//
//    /**
//     * Get operLogID.
//     */
//    @Override
//    public final String getOperLogId()
//    {
//        return operLogId;
//    }
//
//    /**
//     * Set operLogID.
//     */
//    @Override
//    public final void setOperLogId( String operLogId )
//    {
//        this.operLogId = operLogId;
//    }
//
//    /**
//     * Create new instance of QueryExecuter.
//     * Same as {@link #QueryExecuter(DatabaseService, UserInfo, SessionAdapter, Map, String) QueryExecuter(DatabaseService, UserInfo, SessionAdapter, Map, String)}
//     *
//     * <br/>if current operation type of HttpOperationSupport then prefix passed in constructor QueryExecuter, otherwise null.
//     * <br/>if current operation type of SystemOperation then map parameters passed in constructor QueryExecuter, otherwise empty map.
//     *
//     * @param connector DB connector
//     * @return new instance of QueryExecuter
//     */
//    public QueryExecuter makeQueryExecuter( DatabaseService connector )
//    {
//        String prefix = this instanceof HttpOperationSupport ?
//                ( ( HttpOperationSupport )this ).contextPrefix :
//                null;
//
//        Map pars = this instanceof SystemOperation ?
//                ( ( SystemOperation )this ).getCompleteParamTable() :
//                ( Collections.EMPTY_MAP );
//
//        return new QueryExecuter( connector, userInfo, sessionAdapter, pars, prefix );
//    }
//
//    public QueryExecuter makeQueryExecuter( DatabaseService connector, ParamHelper pars )
//    {
//        String prefix = this instanceof HttpOperationSupport ?
//                ( ( HttpOperationSupport )this ).contextPrefix :
//                null;
//
//        return new QueryExecuter( connector, userInfo, sessionAdapter, pars.getCompleteParamTable(), prefix );
//    }
//
//    public String[][] getTagsUsingQueryExecuter( DatabaseService connector,String table, String queryName, Map qeparams ) throws Exception
//    {
//        QueryExecuter qe = makeQueryExecuter( connector );
//        try
//        {
//            qe.enableLocalization();
//            qe.makeIterator( table, queryName, qeparams );
//
//            String[][] ret = qe.calcUsingQuery();
//            if( ret != null && ret.length > Integer.parseInt( Utils.getSystemSetting( connector, "HTML_MAX_LIST_SIZE", "250" ) ) )
//            {
//                Utils.singleLog( "WARN", cat, "getTagsUsingQueryExecuter: Too long tag list: " + ret.length + ", table:" + table + ", qName:" + queryName );
//            }
//            return ret;
//        }
//        finally
//        {
//            qe.closeIterator();
//        }
//    }
//
//    /**
//     * This is replacement to deprecated calcUsingQuery
//     * @param connector
//     * @param qeparams
//     * @return
//     * @throws Exception
//     */
//    public String[][] calcUsingQueryExecuter( DatabaseService connector,String table, String queryName, Map qeparams ) throws Exception
//    {
//        QueryExecuter qe = makeQueryExecuter( connector );
//        try
//        {
//            qe.makeIterator( table, queryName, qeparams );
//            return qe.calcUsingQuery();
//        }
//        finally
//        {
//            qe.closeIterator();
//        }
//    }
//
//    /**
//     * This is replacement to deprecated calcUsingQuery
//     * @param connector
//     * @param qeparams
//     * @return
//     * @throws Exception
//     */
//    public String[][] calcUsingQueryExecuter( DatabaseService connector,String table, String queryName, Map qeparams, boolean showFieldNames )
//        throws Exception
//    {
//        QueryExecuter qe = makeQueryExecuter( connector );
//        try
//        {
//            if( showFieldNames )
//            {
//                qe.enableLocalization();
//            }
//            qe.makeIterator( table, queryName, qeparams );
//            return qe.calcUsingQuery( null, showFieldNames );
//        }
//        finally
//        {
//            qe.closeIterator();
//        }
//    }
//
//    public String[][] calcUsingQueryExecuter(
//       DatabaseService connector,String table, String queryName, Map qeparams, String[] columns
//       ) throws Exception
//    {
//        QueryExecuter qe = makeQueryExecuter( connector );
//        try
//        {
//            qe.makeIterator( table,queryName, qeparams );
//            return qe.calcUsingQuery( columns, false );
//        }
//        finally
//        {
//            qe.closeIterator();
//        }
//    }
//
//    /**
//     * When selection query has "___css_style" column, make it working like in obsolete getTagsFromQuery()
//     * @param connector
//     * @param table
//     * @param queryName
//     * @param qeparams
//     * @return
//     * @throws Exception
//     */
//    public Object calcUsingQueryExecuterUseCssStyle( DatabaseService connector, String table, String queryName, Map qeparams )
//            throws Exception
//    {
//        String[][] tags = calcUsingQueryExecuter( connector, table, queryName, qeparams );
//        return convertToCssStyleTags( tags );
//    }
//
//    /**
//     * For using with String[][] tags = calcUsingQueryExecuter( connector, "properties", "welfareDeliverFinder", values );
//     * @see http://erp2/wiki/jsp/Wiki?%26%231042%3B%26%231099%3B%26%231087%3B%26%231072%3B%26%231076%3B%26%231072%3B%26%231102%3B%26%231097%3B%26%231080%3B%26%231081%3B+%26%231089%3B%26%231087%3B%26%231080%3B%26%231089%3B%26%231086%3B%26%231082%3B+-+%26%231074%3B%26%231088%3B%26%231091%3B%26%231095%3B%26%231085%3B%26%231091%3B%26%231102%3B+%26%231082%3B%26%231086%3B%26%231076%3B%26%231086%3B%26%231084%3B
//     * @param tags
//     * @return
//     */
//    protected Object convertToCssStyleTags( String[][] tags )
//    {
//        if( tags.length == 0 || tags[ 0 ].length < 3 )
//        {
//            return tags;
//        }
//
//        String[] result = new String[tags.length];
//        for( int i = 0; i < tags.length; i++ )
//        {
//            result[ i ] = tags[ i ][ 1 ] + TAG_DELIMITER + tags[ i ][ 0 ] + TAG_DELIMITER + tags[ i ][ 2 ];
//        }
//        return result;
//    }
//
//
//    /**
//     * @deprecated use QueryExecuter instead
//     **/
//    public String[][] calcUsingQuery( DatabaseService connector, String table, String queryName, Map params )
//            throws Exception
//    {
//        return calcUsingQuery( connector, table, queryName, params, null );
//    }
//
//    /**
//     * @deprecated use QueryExecuter instead
//     **/
//    public String[][] calcUsingQuery( DatabaseService connector, String table, String queryName, Map params,
//                                      String[] columns )
//            throws Exception
//    {
//        String prefix = this instanceof HttpOperationSupport ?
//                ( ( HttpOperationSupport )this ).contextPrefix :
//                null;
//
//        return calcUsingQuery( connector, prefix, userInfo, table, queryName, params, sessionAdapter, columns );
//    }
//
//    /**
//     * @deprecated use QueryExecuter instead
//     **/
//    public static String[][] calcUsingQuery( DatabaseService connector,
//                                             String context, UserInfo ui,
//                                             String table, String queryName,
//                                             Map params, SessionAdapter sessionAdapter,
//                                             String[] columns )
//            throws Exception
//    {
//        return calcUsingQuery( connector, context, ui, table, queryName, params, sessionAdapter, null, columns, false );
//    }
//
//    /**
//     * Descendant QueryInfo.
//     */
//    protected static class ReplicatedQueryInfo extends QueryInfo
//    {
//        /**
//         * Allows you to set the field replicated.
//         * @param isReplicated
//         */
//        public ReplicatedQueryInfo( boolean isReplicated )
//        {
//            this.replicated = isReplicated;
//        }
//    }
//
//    public Iterator makeQueryIteratorUsingQuery( DatabaseService connector,
//                                                 String table, String queryName, Map params)
//            throws Exception
//    {
//        String prefix = this instanceof HttpOperationSupport ?
//                ( ( HttpOperationSupport )this ).contextPrefix :
//                null;
//
//        return makeQueryIteratorUsingQuery(connector, prefix, userInfo, table, queryName, params, sessionAdapter, null, null, false);
//    }
//
//    /**
//     * Creating query iterator for the specified query of the table. Before executing the query, placeholders in the query are substituting
//     * for their's values.
//     *
//     * Query result can be limited using parameter from the map params -
//     * {@link com.beanexplorer.enterprise.HttpConstants#JS_RESULT_LIMIT_PARAM HttpConstants.JS_RESULT_LIMIT_PARAM}.
//     * Also, {@link com.beanexplorer.enterprise.HttpConstants#SEARCH_PARAM HttpConstants.SEARCH_PARAM}
//     * specifies, that values of the map params can contains regular expression value.
//     *
//     * @param connector DB connector
//     * @param context value for context placeholder {@link com.beanexplorer.enterprise.DatabaseConstants#CONTEXT_PLACEHOLDER}
//     * @param ui user info
//     * @param table table name
//     * @param queryName query name
//     * @param params pairs (column_name - column_value), where values are using to filter the query result
//     * @param sessionAdapter
//     * @param messages unused
//     * @param columns unused
//     * @param showFieldNames unused
//     * @return
//     * @throws Exception
//     */
//    public static Iterator makeQueryIteratorUsingQuery( DatabaseService connector,
//                                                        String context, UserInfo ui,
//                                                        String table, String queryName,
//                                                        Map params, SessionAdapter sessionAdapter,
//                                                        Map messages,
//                                                        String[] columns, boolean showFieldNames )
//            throws Exception
//    {
//        QueryText qt = new QueryText( connector, table, queryName );
//        String query = qt.query;
//        String type = qt.type;
//        boolean isReplicated = qt.isReplicated;
//
//        if( Utils.isEmpty( query ) )
//        {
//            QueryInfo qi = connector.getAnalyzer().getQueryInfo( ui,
//                    null, PLATFORM_HTML, null, table, null, null );
//
//            qi.initAuto( connector, ui, PLATFORM_HTML, table, null );
//            query = qi.getQuery();
//        }
//
//        boolean isSearch = false;
//        long limit = Long.MAX_VALUE;
//        Map myParams = Collections.EMPTY_MAP;
//        if( params != null )
//        {
//            myParams = new HashMap( params );
//            if( myParams.get( HttpConstants.SEARCH_PARAM ) != null )
//            {
//                myParams.remove( HttpConstants.SEARCH_PARAM );
//                isSearch = true;
//            }
//
//            if( myParams.get( HttpConstants.JS_RESULT_LIMIT_PARAM ) != null )
//            {
//                limit = Long.parseLong( ( String )myParams.get( HttpConstants.JS_RESULT_LIMIT_PARAM ) );
//                myParams.remove( HttpConstants.JS_RESULT_LIMIT_PARAM );
//            }
//        }
//
//        MapParamHelper parHelper = new MapParamHelper( myParams );
//
//        query = Utils.handleConditionalParts( connector, query, myParams );
//        query = Utils.putPlaceholders( connector, query, ui, context );
//        query = Utils.putRequestParameters( connector, query, parHelper, ui );
//        query = Utils.putDictionaryValues( connector, query, ui );
//
//        if( !parHelper.getCompleteParamTable().isEmpty() && QUERY_TYPE_1D.equals( type ) )
//        {
//            String pk = Utils.findPrimaryKeyName( connector, table );
//            query = Utils.prefixQueryWithId( connector, query, table, pk );
//            query = Utils.addRecordFilter( connector, query, table, pk, parHelper.getCompleteParamTable(), isSearch, ui );
//        }
//
//        if( sessionAdapter != null )
//        {
//            query = Utils.putSessionVars( connector, query, sessionAdapter );
//        }
//        else
//        {
//            //tags should be removed from result query
//            query = Utils.putSessionVars( connector, query, Collections.EMPTY_MAP );
//        }
//
//
//        Iterator queryIterator = null;
//        if( !QUERY_TYPE_CUSTOM.equals( type ) )
//        {
//
//            queryIterator = QueryIteratorFactory.make1DIterator(
//                    new ReplicatedQueryInfo( isReplicated ),
//                    ui,
//                    null, /* rs */
//                    null, /* params */
//                    connector,
//                    0,
//                    limit,
//                    0,
//                    limit
//            );
//
//            try
//            {
//                ( ( Query1DIterator )queryIterator ).executeQuery( query );
//            }
//            catch( SQLException exc )
//            {
//                Logger.error( cat, "calcUsingQuery error in\n" + query );
//                throw exc;
//            }
//        }
//        else
//        {
//            Object[] args = { ui, parHelper, connector, 0L, Long.valueOf( Long.MAX_VALUE ) };
//            queryIterator = ( QueryIterator )Class.forName( query ).getConstructor(
//                    new Class[]{ UserInfo.class, ParamHelper.class, DatabaseService.class, long.class, long.class }
//            ).newInstance( args );
//        }
//        return queryIterator;
//
//    }
//
//
//    /**
//     * @deprecated use QueryExecuter instead
//     **/
//    public static String[][] calcUsingQuery( DatabaseService connector,
//                                             String context, UserInfo ui,
//                                             String table, String queryName,
//                                             Map params, SessionAdapter sessionAdapter, Map messages,
//                                             String[] columns, boolean showFieldNames )
//            throws Exception
//    {
//        ArrayList ret = new ArrayList();
//        Iterator queryIterator = null;
//        try
//        {
//            queryIterator = makeQueryIteratorUsingQuery( connector, context, ui, table, queryName, params,
//                    sessionAdapter, messages, columns, showFieldNames );
//            boolean first = true;
//            while( queryIterator.hasNext() )
//            {
//                List checkList = columns == null ? null : Arrays.asList( columns );
//                DynamicPropertySet calc = ( DynamicPropertySet )queryIterator.next();
//                if( showFieldNames && first )
//                {
//                    ArrayList fieldNames = getColumnNames( calc, columns, messages );
//                    ret.add( fieldNames.toArray( new String[0] ) );
//                    first = false;
//                }
//                ArrayList vals = new ArrayList();
//                if( checkList == null ) // all columns
//                {
//                    for( Iterator props = calc.propertyIterator(); props.hasNext(); )
//                    {
//                        DynamicProperty prop = ( DynamicProperty )props.next();
//                        if( prop.isHidden() )
//                            continue;
//
//                        Object val = prop.getValue();
//                        val = FragmentContextSupport.formatValue( val, prop, ui, true );
//                        vals.add( String.valueOf( val ) );
//                    }
//                }
//                else
//                {
//                    for( int i = 0; i < columns.length; i++ )
//                    {
//                        DynamicProperty prop = calc.getProperty( columns[ i ] );
//                        if( prop == null )
//                            continue;
//
//                        Object val = prop.getValue();
//                        val = FragmentContextSupport.formatValue( val, prop, ui, true );
//                        vals.add( String.valueOf( val ) );
//                    }
//                }
//                ret.add( vals.toArray( new String[ 0 ] ) );
//            }
//
//            return ( String[][] )ret.toArray( new String[0][0] );
//        }
//        finally
//        {
//            if( queryIterator instanceof ResultSetQueryIterator )
//            {
//                ( ( ResultSetQueryIterator )queryIterator ).closeResultSet();
//
//            }
//        }
//    }
//
//    /**
//     * Get a list of names localized columns for given columns(columns) contained in the set of properties(dps).
//     *
//     * If the columns are empty, then result will be localized messages for the set of the properties (dps).
//     *
//     * @param dps set of properties
//     * @param columns names columns
//     * @param messages map with localized messages
//     * @return list localized names columns
//     */
//    public static ArrayList getColumnNames( DynamicPropertySet dps, String[] columns, Map messages )
//    {
//        ArrayList result = new ArrayList();
//        List checkList = columns == null ? null : Arrays.asList( columns );
//        for( Iterator props = dps.propertyIterator(); props.hasNext(); )
//        {
//            DynamicProperty prop = ( DynamicProperty )props.next();
//            String name = prop.getName();
//            if( checkList == null ) // all columns
//            {
//                if( !prop.isHidden() )
//                {
//                    if( messages != null )
//                        name = Utils.getMessage( messages, name );
//                    result.add( name );
//                }
//            }
//            else
//            {
//                if( checkList.contains( name ) )
//                {
//                    if( messages != null )
//                        name = Utils.getMessage( messages, name );
//                    result.add( name );
//                }
//                if( result.size() == columns.length )
//                {
//                    break;
//                }
//            }
//        }
//        return result;
//    }
//
//    /**
//     * The prefix constant for localized message.
//     * <br/>This "{{{".
//     */
//    public static final String LOC_MSG_PREFIX = "{{{";
//
//    /**
//     * The postfix constant for localized message.
//     * <br/>This "}}}".
//     */
//    public static final String LOC_MSG_POSTFIX = "}}}";
//
//    /**
//     * Make wrapping for localizable message.
//     *
//     * @param msg the message
//     * @return wrapping message
//     */
//    public static String localizedMessage( String msg )
//    {
//        return LOC_MSG_PREFIX + msg + LOC_MSG_POSTFIX;
//    }
//
//    /**
//     * Send email with parameter type="text/plain".
//     *
//     * <br/><br/>Same as,
//     * {@link #sendEmail(DatabaseService, InternetAddress, InternetAddress[], String, String, String)}
//     * only parameter "from" - "MAIL_FROM_ADDRESS" or "MAIL_FROM_NAME", parameter "to" as string.
//     *
//     * @param connector DB connector
//     * @param to
//     * @param subject
//     * @param body
//     * @throws Exception
//     */
//    public void sendPlainEmail( DatabaseService connector, String to, String subject, String body )
//            throws Exception
//    {
//        sendEmail( connector, null,
//                new InternetAddress[]{ new InternetAddress( to ) }, subject, body, "text/plain" );
//    }
//
//    /**
//     * Send email with parameter type="text/html".
//     *
//     * <br/><br/>Same as,
//     * {@link #sendEmail(DatabaseService, InternetAddress, InternetAddress[], String, String, String)}
//     * only parameter "from" - "MAIL_FROM_ADDRESS" or "MAIL_FROM_NAME", parameter "to" as string.
//     *
//     * @param connector DB connector
//     * @param to
//     * @param subject
//     * @param body
//     * @throws Exception
//     */
//    public void sendHtmlEmail( DatabaseService connector, String to, String subject, String body )
//            throws Exception
//    {
//        sendEmail( connector, null,
//                new InternetAddress[]{ new InternetAddress( to ) }, subject, body, "text/html" );
//    }
//
//    /**
//     * Same as,
//     * {@link #sendEmailReal(DatabaseService, InternetAddress, InternetAddress[], String, String, String, Map, Locale) sendEmailReal(DatabaseService, InternetAddress, InternetAddress[], String, String, String, Map, Locale)}
//     * only map of localized messages extract from a current operation.
//     *
//     * @param connector DB connector
//     * @param from
//     * @param to
//     * @param subject
//     * @param body
//     * @param type
//     * @throws Exception
//     */
//    public void sendEmail( DatabaseService connector,
//                           InternetAddress from, InternetAddress[] to, String subject, String body, String type )
//            throws Exception
//    {
//        Map locMessages = Utils.readLocalizedMessages(
//                connector, userInfo.getLocale(), entity, getOrigName() );
//
//        sendEmailReal( connector, from, to, subject, body, type, locMessages, userInfo );
//    }
//
//    protected static void sendEmailReal( DatabaseService connector,
//                                         InternetAddress from, InternetAddress[] to, String subject, String body, String type,
//                                         Map locMessages, UserInfo userInfo )
//            throws Exception
//    {
//        sendEmailReal( connector, from, to, subject, body, type, locMessages, userInfo, null );
//    }
//    /**
//     * Send email passed localized.
//     *
//     * @param connector DB connector
//     * @param from
//     * @param to
//     * @param subject
//     * @param body
//     * @param type
//     * @param locMessages
//     * @param locale
//     * @param process
//     * @throws Exception
//     */
//    public static void sendEmailReal( DatabaseService connector,
//                                         InternetAddress from, InternetAddress[] to, String subject, String body, String type,
//                                         Map locMessages, UserInfo userInfo, ProcessHandle process )
//            throws Exception
//    {
//        String enc = "UTF-8";
//        MimeMessage2 message = Utils.createMimeMessage( connector );
//
//
//        StringWriter writer = new StringWriter();
//        LocalizingWriter lWriter = new LocalizingWriter( userInfo, writer, locMessages );
//
//        if( subject != null && subject.indexOf( LOC_MSG_PREFIX ) >= 0 )
//        {
//            lWriter.write( subject );
//            lWriter.flush();
//            lWriter.close();
//            subject = writer.getBuffer().toString();
//
//            writer = new StringWriter();
//            lWriter = new LocalizingWriter( userInfo, writer, locMessages );
//        }
//        else
//        {
//            subject = Utils.getMessage( locMessages, subject );
//        }
//
//        lWriter.write( body );
//        lWriter.flush();
//        lWriter.close();
//
//        if( from != null )
//        {
//            message.setFrom( from );
//        }
//
//        message.setSubject( subject, enc );
//        message.addRecipients( Message.RecipientType.TO, to );
//
//        message.setDataHandler(
//                Utils.getEmailBodyDataHandler(
//                        writer.getBuffer().toString().getBytes( enc ),
//                        type + "; charset=" + enc )
//        );
//
//        try
//        {
//            Transport.send( message );
//        }
//        finally
//        {
//            // if javax.mail.Session has debug, write accumulated bytes into ProcessHandle
//            if ( process != null && message.getSession().getDebug() )
//            {
//                PrintStream debugOut = message.getSession().getDebugOut();
//                if ( debugOut instanceof MimeMessage2.PrintStream2 )
//                {
//                    ByteArrayOutputStream s = ((MimeMessage2.PrintStream2)debugOut).getOutput();
//                    process.message( Utils.replaceLFCRwithBR ( Utils.removeHTMLTags( s.toString() ) ) );
//                    s.reset();
//                }
//            }
//        }
//    }
//
//    /**
//     * Create operation by name (opName) for entity (entity).
//     * <br/>Same as,
//     * {@link #setupOperation(DatabaseService, String, String, String[], UserInfo, String) setupOperation(DatabaseService, String, String, String[], UserInfo, String)}
//     *
//     * @param connector DB connector
//     * @param opName operation name
//     * @param entity entity name
//     * @param records records ID's
//     * @param ui user info
//     * @return
//     * @throws Exception
//     */
//    public static Operation setupOperation( DatabaseService connector, String opName,
//                                            String entity, String[] records, UserInfo ui ) throws Exception
//    {
//        return setupOperation( connector, opName, entity, records, ui, null );
//    }
//
//
//    public static Operation setupOperation( DatabaseService connector, String opName,
//                                            String entity, String[] records, UserInfo ui, String queryId ) throws Exception
//    {
//        return setupOperation( connector, opName, entity, records, ui, queryId, null );
//    }
//
//    /**
//     * Create operation by name (opName) for entity (entity).
//     * <br/>Same as,
//     * {@link com.beanexplorer.enterprise.Utils#setupOperation(DatabaseService, String, UserInfo, String, String, String, String[], String, String, String)}
//     * only referer and category equals ""  and platform="HTML".
//     * <br/>For this operation set application info from DB (table systemsettings).
//     *
//     * @param connector DB connector
//     * @param opName    operation name
//     * @param entity    entity name
//     * @param records   records ID's
//     * @param ui        user info
//     * @param queryId   query ID
//     * @return
//     * @throws Exception
//     */
//    public static Operation setupOperation( DatabaseService connector, String opName,
//                                            String entity, String[] records, UserInfo ui, String queryId, String tcloneId ) throws Exception
//    {
//        String query = "SELECT ID FROM operations " +
//                       "WHERE table_name='" + entity + "' AND " + "name='" + Utils.safestr( connector, opName ) + "'";
//        String opId = QRec.withCache( connector, query, SetupOperationCache.getInstance() ).getString();
//
//        Operation op = Utils.setupOperation( connector, opId, ui, "", "HTML", entity, records, queryId, "", tcloneId );
//        ApplicationInfoComponent.ApplicationInfo ai = null;//Utils.getAppInfo( WebAppInitializer.getStoredServletContext(), connector, null, ui );
//        op.setAppInfo( ai );
//        return op;
//    }
//
//    public Operation setupOperationExt( DatabaseService connector, UserInfo ui,
//        String entity, String opName, String[] records ) throws Exception
//    {
//        String query = "SELECT ID FROM operations " +
//                       "WHERE table_name='" + entity + "' AND " + "name='" + Utils.safestr( connector, opName ) + "'";
//        String opId = QRec.withCache( connector, query, SetupOperationCache.getInstance() ).getString();
//
//        Operation op = Utils.setupOperation( connector, opId, ui, "", "HTML", entity, records, this.fromQuery, "", this.tcloneId );
//        op.setAppInfo( this.getAppInfo() );
//        op.setSessionAdapter( this.getSessionAdapter() );
//        OperationFragmentHelper.loadOperationExtenders( connector, op );
//        return op;
//    }
//
//    public final DynamicPropertySet getParametersExt( Writer out, DatabaseService connector, Map presets ) throws Exception
//    {
//        DynamicPropertySet pars = ( DynamicPropertySet )getParameters( out, connector, new HashMap( presets ) );
//        OperationFragmentHelper.invokeExtenders( "getParameters", new StringWriter(), connector, this, pars, new HashMap( presets ) );
//        return pars;
//    }
//
//    public final void invokeExt( Writer out, DatabaseService connector ) throws Exception
//    {
//        Utils.doInvokeOperation( this, connector, out );
//    }
//
//    private List<OperationExtender> extenders;
//
//    @Override
//    public List<OperationExtender> getExtenders()
//    {
//        return extenders;
//    }
//
//    @Override
//    public void setExtenders( List<OperationExtender> extenders )
//    {
//        this.extenders = extenders;
//    }
//
//    private String queueID;
//
//    @Override
//    public String getQueueID()
//    {
//        return queueID;
//    }
//
//    @Override
//    public void setQueueID( String queueID )
//    {
//        this.queueID = queueID;
//    }
//
//    private String crumbID;
//
//    @Override
//    public String getCrumbID()
//    {
//        return crumbID;
//    }
//
//    @Override
//    public void setCrumbID( String crumbID )
//    {
//        this.crumbID = crumbID;
//    }
//
//    protected boolean clearAffectedCaches( DatabaseService connector, DynamicPropertySet parameters )
//    {
//        if( parameters == null )
//           return CacheFactory.clearByAffectingTable( entity );
//        if( !"querySettings".equals( entity ) )
//           return CacheFactory.clearByAffectingTable( entity );
//
//        String key = "" + userInfo.getUserName() + "." + parameters.getValue( "queryID" );
//        //System.out.println( "key = " + key );
//        QuerySettingsCache.getInstance().remove( key );
//        return true;
//    }
//
//    @Override
//    public InterruptMonitor getInterruptMonitor()
//    {
//        return interruptChecker;
//    }
//
//    @Override
//    public void setInterruptMonitor( InterruptMonitor interruptChecker )
//    {
//        this.interruptChecker = interruptChecker;
//    }
//
//    protected boolean isInvokedOffline()
//    {
//        return getQueueID() != null;
//    }
//
//    @Override
//    public boolean saveOperLogParamsImmeditely()
//    {
//        return false;
//    }
//
//    @Override
//    public String getDescription( DatabaseService connector )
//    {
//        String className = this.getClass().getCanonicalName();
//        String []path = className.split( "\\." );
//        className = path[ path.length - 1 ];
//
//        String localizedOpName = getName();
//
//        if( userInfo != null ) // initialized
//        {
//            try
//            {
//                localizedOpName = Utils.getMessage(
//                    Utils.readLocalizedMessages( connector, userInfo.getLocale(), entity, DatabaseConstants.L10N_TOPIC_OPERATION_NAME ),
//                    localizedOpName );
//            }
//            catch( Exception ignore ) {}
//        }
//
//        return "" + localizedOpName + " [" + className + "]";
//    }

    @Override
    public Status getResult()
    {
        return result;
    }

    protected void setResult( Status status )
    {
        result = status;
    }

//    Map internalPresetsForOperationLogs = Collections.EMPTY_MAP;
//
//    public void internalSavePresetsForOperationLogs( Map presets )
//    {
//        internalPresetsForOperationLogs = new HashMap( presets );
//    }
//
//    public Object getInternalPresetsForOperationLogs( String name )
//    {
//        return internalPresetsForOperationLogs.get( name );
//    }
//
//    public static void addOnChangeHandlerToProperty( DynamicProperty prop, String code )
//    {
//        Map extras = ( Map )prop.getAttribute( BeanInfoConstants.EXTRA_ATTRS );
//        if( extras == null || extras.isEmpty() )
//        {
//            extras = Collections.singletonMap( "onChange", code );
//        }
//        else
//        {
//            extras = new HashMap( extras );
//            boolean isSet = false;
//            for( Iterator entries = extras.entrySet().iterator(); entries.hasNext(); )
//            {
//                Map.Entry entry = ( Map.Entry ) entries.next();
//                String name = ( String ) entry.getKey();
//                if( "onChange".equalsIgnoreCase( name ) )
//                {
//                    entry.setValue( code + "; " + entry.getValue() );
//                    isSet = true;
//                    break;
//                }
//            }
//            if( !isSet )
//            {
//                extras.put( "onChange", code );
//            }
//        }
//        prop.setAttribute( BeanInfoConstants.EXTRA_ATTRS, extras );
//    }
//
//    public static void addUniqueDatabaseCheck(
//         DynamicProperty prop, String entity, String column, String message )
//    {
//        addUniqueDatabaseCheck( prop, entity, column, message, null );
//    }
//
//    public static void addUniqueDatabaseCheck(
//            DynamicProperty prop, String entity, String column, String message, Map extras )
//    {
//        Validation.UniqueStruct us = new Validation.UniqueStruct();
//        us.entity = entity;
//        us.column = column;
//        us.message = message;
//        us.extraParams = extras;
//        prop.setAttribute( Validation.RULES_ATTR, Collections.singletonMap( Validation.UNIQUE, us ) );
//    }
//
//    public static void removeNamedProperties( DynamicPropertySet params, String name )
//    {
//        for( String propName : new ArrayList<String>( params.asMap().keySet() ) )
//        {
//            if( propName.startsWith( name ) )
//            {
//                params.remove( propName );
//            }
//        }
//    }
    
//    @Override
//    public void setDatabase( EntityAccess<EntityModel<RecordModel>> database )
//    {
//        if( database == null )
//        {
//            throw new IllegalArgumentException( "null" );
//        }
//        this.database = database;
//    }
//
//    @Override
//    public EntityAccess<EntityModel<RecordModel>> getDatabase()
//    {
//        if( database == null )
//        {
//            throw new UnsupportedOperationException( "Database model plugin has been excluded from release." );
//        }
//        return this.database;
//    }

    public DynamicPropertySet getTableBean(String entityName) throws Exception
    {
        Project project = ServerModules.getServiceProvider().getProject();
        Entity entity = project.getEntity(entityName);

        BeModelElement scheme = entity.getAvailableElement("Scheme");

        if(scheme == null)return null;

        BeModelElement columns = ((TableDef) scheme).get("Columns");

        DynamicPropertySet bean = new DynamicPropertySetSupport();


        ((BeCaseInsensitiveCollection<ColumnDef>) columns).stream()
                .filter(x -> !x.getName().equals(entity.getPrimaryKey()))
                .map(OperationSupport::getDynamicProperty)
                .forEach(bean::add);

        return bean;
    }

    static DynamicProperty getDynamicProperty(ColumnDef columnDef)
    {
        DynamicProperty dynamicProperty = new DynamicProperty(columnDef.getName(), getTypeClass(columnDef.getType()));
        dynamicProperty.setAttribute(BeanInfoConstants.COLUMN_SIZE_ATTR, columnDef.getType().getSize());

        return dynamicProperty;
    }

    private static Class<?> getTypeClass(SqlColumnType columnType)
    {
        switch( columnType.getTypeName() )
        {
            case SqlColumnType.TYPE_BIGINT:
                return Long.class;
            case SqlColumnType.TYPE_INT:
                return Integer.class;
            case SqlColumnType.TYPE_DECIMAL:
                return Double.class;
            case SqlColumnType.TYPE_BOOL:
                return Boolean.class;
            case SqlColumnType.TYPE_DATE:
                return Date.class;
            case SqlColumnType.TYPE_TIMESTAMP:
                return Time.class;
            default:
                return String.class;
        }
    }

}