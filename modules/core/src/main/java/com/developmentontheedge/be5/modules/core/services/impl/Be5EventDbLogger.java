package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.services.events.Be5EventLogger;
import com.developmentontheedge.be5.server.services.events.EventManager;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.server.services.events.EventManager.ACTION_QUERY;

public class Be5EventDbLogger implements Be5EventLogger
{
    public static final Logger log = Logger.getLogger(Be5EventDbLogger.class.getName());

    private static final String EVENT_LOG_TABLE = "be5events";
    private static final String EVENT_DB_LOGGING_FEATURE = "eventDbLogging";
    private final UserInfoProvider userInfoProvider;
    private final DatabaseModel database;

    @Inject
    public Be5EventDbLogger(EventManager eventManager, DatabaseModel database,
                            ProjectProvider projectProvider, UserInfoProvider userInfoProvider)
    {
        this.database = database;
        this.userInfoProvider = userInfoProvider;
        if (projectProvider.get().hasFeature(EVENT_DB_LOGGING_FEATURE))
        {
            eventManager.addListener(this);
        }
    }

    @Override
    public void queryCompleted(Query query, Map<String, Object> parameters, long startTime, long endTime)
    {
        storeRecord(userInfoProvider.getUserName(), userInfoProvider.getRemoteAddr(),
                startTime, endTime, ACTION_QUERY, query.getEntity().getName(), query.getName(), null);
    }

    @Override
    public void queryError(Query query, Map<String, Object> parameters, long startTime, long endTime, String exception)
    {
        storeErrorRecord(userInfoProvider.getUserName(), userInfoProvider.getRemoteAddr(),
                startTime, endTime, ACTION_QUERY, query.getEntity().getName(), query.getName(), exception);
    }

    //
//    /**
//     *
//     * @param eventLogTableName table name for event logger
//     * @param connector
//     */
//    public EventLoggerImpl(String eventLogTableName, )
//    {
//        if ( eventLogTableName != null )
//            this.eventLogTableName = eventLogTableName;
//        else
//            this.eventLogTableName = EVENT_LOG_TABLE_NAME_DEFAULT;
//
//        try
//        {
//            maxResultSize = Utils.getColumnSize( connector, this.eventLogTableName.toUpperCase(), "RESULT" );
//            if( connector.isOracle() )
//            {
//                // Under Oracle prepared statement uses UTF-8 to transfer the data
//                // which means that actual data from string may be bigger
//                // than maxResultSize bytes
//                maxResultSize -= 256 + 1;
//            }
//            Logger.info( cat, "maxResultSize set to " + maxResultSize );
//        }
//        catch( SQLException exc )
//        {
//            Logger.error( cat, "When getting maxResultSize", exc );
//        }
//
//        for( String event: Utils.getSystemSetting( connector, "EVENT_LOGGER_IGNORE", "" ).split( "," ) )
//        {
//            ignoreEvents.add( event );
//        }
//        SystemSettings.registerChangeListener( this );
//
//        currTime = connector.getAnalyzer().getCurrentDateTimeExpr();
//        endColumn = connector.getAnalyzer().quoteIdentifier( "END" );
//    }
//
//    private TreeSet<String> ignoreEvents = new TreeSet<String>( String.CASE_INSENSITIVE_ORDER );
//
//    protected boolean isIgnore( String title )
//    {
//        return ignoreEvents.contains( title );
//    }
//

    private void storeErrorRecord(String user_name, String remoteAddr, long startTime, long endTime,
                             String action, String entity, String title, String exception)
    {
        database.getEntity(EVENT_LOG_TABLE).add(new HashMap<String, Object>() {{
            put("user_name", user_name);
            put("IP", remoteAddr);
            put("startTime", new Timestamp(startTime));
            put("endTime", new Timestamp(endTime));
            put("action", action);
            put("entity", entity);
            put("title", title);
            put("exception", exception);
        }});
    }

    private void storeRecord(String user_name, String remoteAddr, long startTime, long endTime,
                                 String action, String entity, String title, String result)
    {
        database.getEntity(EVENT_LOG_TABLE).add(new HashMap<String, Object>() {{
            put("user_name", user_name);
            put("IP", remoteAddr);
            put("startTime", new Timestamp(startTime));
            put("endTime", new Timestamp(endTime));
            put("action", action);
            put("entity", entity);
            put("title", title);
            put("result", result);
        }});
    }
//
//    protected Map queries = Collections.synchronizedMap( new WeakHashMap() );
//
//    @Override
//    public void queryStarted( final int pageID, final String login, final String entity, final String title,
//         final QueryPageInfo qpi ) throws VetoException
//    {
//        if( isIgnore( entity + "::" + title ) )
//        {
//            return;
//        }
//
//         = Utils.getDefaultConnector();
//
//        final ServletRequest request = qpi.getServletRequest();
//
//        String action = qpi.getAction();
//        if( null == action )
//            action = EventManager.ACTION_QUERY;
//
//        final String action1 = action;
//        final Map<String, String[]> params = getParams( qpi.getParamHelper() );
//
//        final String start = connector.getAnalyzer().getCurrentDateTimeExpr();
//
//        String remoteAddr = "NULL";
//        if( request != null )
//        {
//            remoteAddr = Utils.getRemoteAddr( connector, request );
//            if( remoteAddr != null )
//                remoteAddr = Utils.safestr( connector, remoteAddr, true );
//        }
//
//        final String remoteAddr1 = remoteAddr;
//
//        try
//        {
//            final String logLID = storeRecord( connector, login, remoteAddr1, start, action1, entity, title );
//            if( logLID != null )
//            {
//                queries.put( pageID, logLID );
//            }
//
//            Utils.queuedRunnable( new QueuedStatement.Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    try
//                    {
//                        final  = getConnector();
//
//                        if( logLID != null )
//                        {
//                            logParametersAsBatch( logLID, connector, params );
//                        }
//                    }
//                    catch( Exception ex )
//                    {
//                        Logger.error( cat, "When logging queryStarted", ex );
//                    }
//                }
//            } );
//        }
//        catch( Exception ex )
//        {
//            Logger.error( cat, "When logging queryStarted", ex );
//        }
//    }
//
//    @Override
//    public void queryDenied( int pageID, QueryPageInfo qpi, VetoException reason )
//    {
//    }
//
//    @Override
//    public void queryCompleted( int pageID, final QueryPageInfo qpi )
//    {
//        final String id = (String)queries.remove( pageID );
//        if( id != null )
//        {
//            updateResult( Utils.getDefaultConnector(), id, qpi.getResult(), qpi.getException(), null );
//        }
//    }
//
//    // operations
//    protected Map operations = Collections.synchronizedMap( new WeakHashMap() );
//
//    @Override
//    public void operationStarted( final int opPageID, final String login, final String entity, final String title,
//         final OperationInfo opInfo ) throws VetoException
//    {
//        if( isIgnore( entity + "::" + title ) )
//        {
//            return;
//        }
//
//         = Utils.getDefaultConnector();
//
//        final ParamHelper parHelper = opInfo.getParamHelper();
//
//        final ServletRequest request = opInfo.getServletRequest();
//
//        String action = EventManager.ACTION_OPERATION;
//
//        final String action1 = action;
//        final Map<String, String[]> params = getParams( parHelper );
//
//        final String start = connector.getAnalyzer().getCurrentDateTimeExpr();
//
//        String remoteAddr = "NULL";
//        if( request != null )
//        {
//            remoteAddr = Utils.getRemoteAddr( connector, request );
//            if( remoteAddr != null )
//                remoteAddr = Utils.safestr( connector, remoteAddr, true );
//        }
//
//        final String remoteAddr1 = remoteAddr;
//
//        try
//        {
//            final String logLID = storeRecord( connector, login, remoteAddr1, start, action1, entity, title );
//            if( logLID != null )
//            {
//                operations.put( opPageID, logLID );
//            }
//
//            Utils.queuedRunnable( new QueuedStatement.Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    try
//                    {
//                        final  = getConnector();
//
//                        if( logLID != null )
//                        {
//                            logParametersAsBatch( logLID, connector, params );
//                        }
//                    }
//                    catch( Exception ex1 )
//                    {
//                        Logger.error( cat, "*When logging operationStarted", ex1 );
//                    }
//                }
//            } );
//        }
//        catch( Exception ex )
//        {
//            Logger.error( cat, "When logging operationStarted", ex );
//        }
//    }
//
//    @Override
//    public void operationDenied( int opPageID, OperationInfo opInfo, VetoException reason )
//    {
//    }
//
//    @Override
//    public void operationCompleted( int opPageID, final OperationInfo opInfo )
//    {
//        final String id = (String)operations.remove( opPageID );
//        if( id != null )
//        {
//            updateResult( Utils.getDefaultConnector(), id, opInfo.getResult(), opInfo.getException(), null );
//        }
//    }
//
//    protected Map servlets = Collections.synchronizedMap( new WeakHashMap() );
//
//    @Override
//    public void servletStarted( final ServletInfo info ) throws VetoException
//    {
//        if( isIgnore( info.getTitle() ) )
//        {
//            return;
//        }
//
//         = Utils.getDefaultConnector();
//
//        UserInfo ui = info.getUserInfo();
//        final ParamHelper parHelper = info.getParamHelper();
//
//        final ServletRequest request = info.getServletRequest();
//
//        String action = info.getAction() == null ? EventManager.ACTION_SERVLET : info.getAction();
//
//        String entity = "";
//        String title = info.getTitle();
//
//        if( "OperationParamsServlet".equals( title ) )
//        {
//            HttpParamHelper h = ( HttpParamHelper )parHelper;
//            entity = h.getStrict( HttpConstants.TABLE_NAME_PARAM );
//            title = h.getStrict( HttpConstants.OPERATION_NAME_PARAM );
//            action = "operation";
//            boolean isInvoke = h.getValues( com.beanexplorer.enterprise.dynamic.operations.OperationParamsServlet.IS_INVOKE ) != null;
//            if( !isInvoke )
//            {
//                title += " (parameters)";
//            }
//        }
//        else
//        {
//            Operation op = info.getOperation();
//            if( op != null )
//            {
//                entity = op.getEntity();
//                //title  = op.getName();
//                if( ui == null )
//                {
//                    ui = op.getUserInfo();
//                }
//            }
//        }
//        final String login = ui.getUserName();
//
//        final Map<String, String[]> params = getParams( parHelper );
//
///*
//        if( Utils.isEmpty( entity ) && parHelper instanceof HttpParamHelper )
//        {
//            System.out.println( "----------------------------------------------" );
//            System.out.println( "" + parHelper );
//        }
//*/
//
//        final String action1 = action;
//        final String entity1 = entity;
//
//        final String start = connector.getAnalyzer().getCurrentDateTimeExpr();
//
//        String remoteAddr = "NULL";
//        if( request != null )
//        {
//            remoteAddr = Utils.getRemoteAddr( connector, request );
//            if( remoteAddr != null )
//                remoteAddr = Utils.safestr( connector, remoteAddr, true );
//        }
//
//        final String remoteAddr1 = remoteAddr;
//
//        try
//        {
//            final String logLID = storeRecord( connector, login, remoteAddr1, start, action1, entity1, title );
//            if( logLID != null )
//            {
//                servlets.put( info, logLID );
//            }
//
//            Utils.queuedRunnable( new QueuedStatement.Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    try
//                    {
//                        final  = getConnector();
//
//                        if( logLID != null )
//                        {
//                            logParametersAsBatch( logLID, connector, params );
//                        }
//                    }
//                    catch( Exception ex1 )
//                    {
//                        Logger.error( cat, "When logging servletStarted", ex1 );
//                    }
//                }
//            } );
//        }
//        catch( Exception ex )
//        {
//            Logger.error( cat, "When logging servletStarted", ex );
//        }
//    }
//
//    @Override
//    public void servletCompleted( final ServletInfo info )
//    {
//        if( isIgnore( info.getTitle() ) )
//        {
//            return;
//        }
//
//        final String id = (String)servlets.remove( info );
//        if( id != null )
//        {
//            updateResult( Utils.getDefaultConnector(), id, info.getResult(), info.getException(), null );
//        }
//    }
//
//    @Override
//    public void servletDenied( ServletInfo si, VetoException reason )
//    {
//    }
//
//    @Override
//    public void processStateChanged(ProcessInfo pi)
//    {
//    }
//
//    protected void updateResult( String id, String result, Exception exception, Map<?,?> extras )
//    {
//        if( Utils.isEmpty(result) )
//        {
//            result = null;
//        }
//        else if( maxResultSize > 0 && result.length() > maxResultSize )
//        {
//            result = result.substring(0, maxResultSize);
//        }
//
//        String sql = "UPDATE " + eventLogTableName + " SET " + endColumn + "=" + currTime;
//        if( result != null )
//        {
//            sql += ", result = " + Utils.safestr( connector, result, true );
//        }
//        if( exception != null )
//        {
//            String excStr = Utils.trimStackAsString( exception, 15 );
//            sql += ", exception = " + Utils.safestr( connector, excStr, true );
//        }
//        if( extras != null && !extras.isEmpty() )
//        {
//            for( Map.Entry<?,?> entry : extras.entrySet() )
//            {
//                String name = (String)entry.getKey();
//                String value = (String)entry.getValue();
//                sql += ", " + name + " = " + Utils.safestr( connector, value, true );
//            }
//        }
//        sql += " WHERE ID = " + Utils.safeIdValue( connector, eventLogTableName, "ID", id );
//
//        Utils.queuedUpdate( sql );
//    }
//
//    void logParametersAsBatch(String logLID,final Map<String, String[]> params) throws SQLException
//    {
//        if( params != null)
//        {
//            BatchExecutor batch = new BatchExecutor( connector, true );
//
//            if( !Utils.isNumericColumn( connector, "eventParams", "logID" ) )
//            {
//                logLID = "'" + logLID + "'";
//            }
//
//            for( Map.Entry<String, String[]> entry : params.entrySet() )
//            {
//                final String pname = entry.getKey();
//                if( Utils.isEmpty( pname ) )
//                {
//                    continue;
//                }
//
//                if( "_".equals( pname ) )
//                    continue;
//                if( HttpConstants.TIMESTAMP_PARAM.equals( pname ) )
//                    continue;
//                if( HttpConstants.CRYPTED_URL_PARAM.equals( pname ) )
//                    continue;
//                if( HttpConstants.OPERATION_CACHE_ID_PARAM.equals( pname ) )
//                    continue;
//
//                if( HttpConstants.TABLE_NAME_PARAM.equals( pname ) )
//                    continue; // no point - already logged as table_name
//                if( HttpConstants.QUERY_ID_PARAM.equals( pname ) )
//                    continue; // of no use - since changed with every update of metadata
//                if( HttpConstants.CHARSET_PARAM.equals( pname ) )
//                    continue; // of no use
//                if( pname.startsWith( HttpConstants.INTERNAL_PARAM_PREFIX + "Execute" ) )
//                    continue; // of no use
//
//                String[] vals = entry.getValue();
//                if( vals == null || vals.length < 1 )
//                    continue;
//
//                int valueColumnSize = Utils.getColumnSize( connector, "eventParams", "paramValue" );
//                for( int i = 0; i < vals.length; i++ )
//                {
//                    String value = vals[ i ];
//                    if ( value != null && valueColumnSize > 3 && value.length() > valueColumnSize )
//                    {
//                        int trimSize = valueColumnSize - 4;
//                        if( trimSize > 3000 )
//                        {
//                            trimSize = 3000;
//                        }
//                        value = value.substring( 0, trimSize ) + "...";
//                    }
//
//                    String up_sql1 = connector.getAnalyzer().makeInsertIntoWithAutoIncrement(
//                        "eventParams", "ID" );
//
//                    up_sql1 += " logID, paramName, paramValue ) ";
//                    up_sql1 += connector.getAnalyzer().makeInsertValuesWithAutoIncrement();
//                    up_sql1 += logLID + ", " + Utils.safestr( connector, pname, true ) + ", " + Utils.safestr( connector, value, true ) + " )";
//
//                    //connector.executeUpdate( up_sql1 );
//                    batch.add( up_sql1 );
//                }
//            }
//
//            batch.flush();
//        }
//    }
//
//    void logParameters(String logLID,final Map<String, String[]> params)
//        throws SQLException
//    {
//        if( params != null)
//        {
//            if( !Utils.isNumericColumn( connector, "eventParams", "logID" ) )
//            {
//                logLID = "'" + logLID + "'";
//            }
//
//            for( Map.Entry<String, String[]> entry : params.entrySet() )
//            {
//                final String pname = entry.getKey();
//                if( Utils.isEmpty( pname ) )
//                {
//                    continue;
//                }
//
//                if( "_".equals( pname ) )
//                    continue;
//                if( HttpConstants.TIMESTAMP_PARAM.equals( pname ) )
//                    continue;
//                if( HttpConstants.CRYPTED_URL_PARAM.equals( pname ) )
//                    continue;
//                if( HttpConstants.OPERATION_CACHE_ID_PARAM.equals( pname ) )
//                    continue;
//
//                if( HttpConstants.TABLE_NAME_PARAM.equals( pname ) )
//                    continue; // no point - already logged as table_name
//                if( HttpConstants.QUERY_ID_PARAM.equals( pname ) )
//                    continue; // of no use - since changed with every update of metadata
//                if( HttpConstants.CHARSET_PARAM.equals( pname ) )
//                    continue; // of no use
//                if( pname.startsWith( HttpConstants.INTERNAL_PARAM_PREFIX + "Execute" ) )
//                    continue; // of no use
//
//                String[] vals = entry.getValue();
//                if( vals == null || vals.length < 1 )
//                    continue;
//
//                int valueColumnSize = Utils.getColumnSize( connector, "eventParams", "paramValue" );
//                for( int i = 0; i < vals.length; i++ )
//                {
//                    String value = vals[ i ];
//                    if ( value != null && valueColumnSize > 3 && value.length() > valueColumnSize )
//                    {
//                        int trimSize = valueColumnSize - 4;
//                        if( trimSize > 3000 )
//                        {
//                            trimSize = 3000;
//                        }
//                        value = value.substring( 0, trimSize ) + "...";
//                    }
//
//                    String up_sql1 = connector.getAnalyzer().makeInsertIntoWithAutoIncrement(
//                        "eventParams", "ID" );
//
//                    up_sql1 += " logID, paramName, paramValue ) ";
//                    up_sql1 += connector.getAnalyzer().makeInsertValuesWithAutoIncrement();
//                    up_sql1 += logLID + ", " + Utils.safestr( connector, pname, true ) + ", " + Utils.safestr( connector, value, true ) + " )";
//
//                    Utils.queuedUpdate( up_sql1 );
//                }
//            }
//        }
//    }
//
//    /**
//     * Clone parameter to prevent usage of paramHelper in separate thread.
//     *
//     * @param paramHelper
//     * @return
//     */
//    private static Map<String, String[]> getParams(ParamHelper paramHelper)
//    {
//        if( paramHelper == null )
//        {
//            return null;
//        }
//
//        final Map paramTable = paramHelper.getCompleteParamTable();
//        final Map<String, String[]> params = new HashMap<String, String[]>( paramTable.size() );
//
//        for( final Object o : paramTable.keySet() )
//        {
//            final String name = HttpParamHelper.mapNameIn( (String)o );
//            params.put( name, paramHelper.getValues( name ) );
//        }
//
//        return params;
//    }
//
////    public void destroy()
////    {
////        final String start = connector.getAnalyzer().getCurrentDateTimeExpr();
////        try
////        {
////            storeRecord( connector, "(system)", null, start, "other", null, "System stop" );
////        }
////        catch( SQLException e )
////        {
////            Logger.error( cat, "Storing stop record", e );
////        }
////    }
////
//    public void init()
//    {
//        final String start = connector.getAnalyzer().getCurrentDateTimeExpr();
//        try
//        {
//            storeRecord( connector, "(system)", null, start, "other", null, "System start" );
//        }
//        catch( SQLException e )
//        {
//            Logger.error( cat, "Storing start record", e );
//        }
//    }

}
