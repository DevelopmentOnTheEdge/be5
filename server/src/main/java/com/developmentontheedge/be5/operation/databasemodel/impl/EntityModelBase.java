package com.developmentontheedge.be5.operation.databasemodel.impl;


import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlHelper;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.operation.databasemodel.EntityModel;
import com.developmentontheedge.be5.operation.databasemodel.EntityModelAdapter;
import com.developmentontheedge.be5.operation.databasemodel.EntityModelWithCondition;
import com.developmentontheedge.be5.operation.databasemodel.MethodProvider;
import com.developmentontheedge.be5.operation.databasemodel.OperationModel;
import com.developmentontheedge.be5.operation.databasemodel.QueryModel;
import com.developmentontheedge.be5.operation.databasemodel.RecordModel;
import com.developmentontheedge.be5.operation.databasemodel.groovy.EntityModelMetaClass;
import com.developmentontheedge.be5.operation.databasemodel.groovy.GroovyRegister;
import com.developmentontheedge.be5.operation.databasemodel.groovy.QueryModelMetaClass;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetBlocked;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class EntityModelBase<R extends EntityModelBase.RecordModelBase> implements EntityModelAdapter<R>
{
    static
    {
        //TODO load class via reflection ( Class.forName )
        try
        {
            GroovyRegister.registerMetaClass( EntityModelMetaClass.class, EntityModelBase.class );
            //GroovyRegister.registerMetaClass( RecordModelMetaClass.class, RecordModelBase.class );
            GroovyRegister.registerMetaClass( QueryModelMetaClass.class, QueryModelBase.class );
        }
        catch( NoClassDefFoundError e )
        {
            // some class has been excluded
        }
    }

    private DatabaseService databaseService;
    private SqlService db;
    private SqlHelper sqlHelper;

    final private Entity entity;
    final private DatabaseModel database;


    public EntityModelBase(DatabaseService databaseService, SqlService db, SqlHelper sqlHelper, DatabaseModel database, Entity entity)
    {
        this.databaseService = databaseService;
        this.db = db;
        this.sqlHelper = sqlHelper;
        this.database = database;

        this.entity = entity;
    }

//    private EntityModelBase(DatabaseModel database, UserInfo userInfo, String entity )
//    {
//        this( database, userInfo, entity, null );
//    }
//
//    private EntityModelBase( DatabaseModel database, UserInfo userInfo, String entity, String tcloneId )
//    {
//        this( database, userInfo, entity, tcloneId, false );
//    }
//
//    protected EntityModelBase(DatabaseModel database, UserInfo userInfo, String entity )
//    {
//        this.database = database;
////        this.connector = database.getConnector();
////        this.analyzer = connector.getAnalyzer();
//        this.userInfo = userInfo;
//        this.entity = entity;
//        this.tcloneId = tcloneId;
//        //this.cache = EntityModelLocalCache.getInstance();
//        this.forceCache = forceCache;
//
//        try
//        {
////            this.primaryKey = Utils.findPrimaryKeyNameUniversal( connector, entity );
////            this.entityType = Utils.getEntityType( connector, entity );
//        }
//        catch( NullPointerException ignore )
//        {
//            // primary key or entity type may not have been found
//        }
//        catch( Exception e )
//        {
//            throw new RuntimeException( e );
//        }
//
//    }

    @Override
    public R get( Map<String, String> values )
    {
//        String tableName = entity.getName();
//        DynamicPropertySet valuesDps = sqlHelper.getTableDps(entity, values);

        String sql = "SELECT * FROM " + entity.getName() + " WHERE " + sqlHelper.generateConditionsSql(entity, values);

        DynamicPropertySet dps = db.select(sql, DpsHelper::createDps, values.values().toArray());
        return ( R )new RecordModelBase( dps );
    }

    @Override
    public long count()
    {
        return count( Collections.emptyMap() );
    }
    
    @Override
    public long count( Map<String, String> allConditions )
    {
        String sql = "SELECT COUNT(*) FROM " + entity.getName() + " WHERE " + sqlHelper.generateConditionsSql(entity, allConditions);
        return db.getLong(sql);
    }

    @Override
    public String getAdditionalConditions()
    {
        StringBuilder sql = new StringBuilder();
//        try
//        {
//            if( Utils.columnExists( connector, getEntityName(), DatabaseConstants.IS_DELETED_COLUMN_NAME ) )
//            {
//                sql.append( " " );
//                sql.append( DatabaseConstants.IS_DELETED_COLUMN_NAME );
//                sql.append( " != 'yes'" );
//            }
//            else
//            {
//                sql.append( " 1 = 1" );
//            }
//        }
//        catch( SQLException e )
//        {
//            String reason = "Can't fetch " + DatabaseConstants.IS_DELETED_COLUMN_NAME + " column";
//            Logger.error( cat, reason );
//            throw new EntityModelException( reason, e );
//        }
        return sql.toString();
    }

    @Override
    public boolean isEmpty()
    {
        return isSqlResultEmpty( getAdditionalConditions() );
    }

    private boolean isSqlResultEmpty( String ... conditions )
    {
//        StringBuilder query = new StringBuilder( "SELECT 1 FROM " )
//                .append( getTableName() )
//                .append( " WHERE 1 = 1" );
//
//        for( String condition : conditions )
//        {
//            if( !condition.isEmpty() )
//            {
//                query.append( " AND " ).append( condition );
//            }
//        }
//
//        try
//        {
//            ResultSet rs = connector.executeQuery( query.toString() );
//            try
//            {
//                return !rs.next();
//            }
//            finally
//            {
//                connector.close( rs );
//            }
//        }
//        catch( SQLException e )
//        {
//            throw new RuntimeException( e );
//        }
        return false;
    }

    @Override
    public boolean contains( Map<String, String> values )
    {
        String conditionsSql;

        conditionsSql = "";//Utils.getConditionsSql( connector, getEntityName(), getPrimaryKeyName(), values, getTcloneId() );

        return !isSqlResultEmpty( conditionsSql );
    }

    @Override
    public Long add( Map<String, String> values )
    {
        return addForce( values );
    }

    @Override
    public boolean containsAll( Collection<Map<String, String>> c )
    {
        return c.stream().allMatch( this::contains );
    }

    @Override
    public List<Long> addAll( final Collection<Map<String, String>> c )
    {
        final List<Long> keys = new ArrayList<>( c.size() );
        for( Map<String, String> values : c )
        {
            keys.add( add( values ) );
        }
        return keys;
    }

    @Override
    public int removeAll( Collection<Map<String, String>> c )
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public R get( Long id )
    {
        try
        {
            return ( R )new RecordModelBase( id );
        }
        catch( SQLException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void set( Long id, String propertyName, String value )
    {
        setForce( id, propertyName, value );
    }

    @Override
    public int remove( Long firstId, final Long... otherId )
    {
        return removeForce( firstId, otherId );
    }

    @Override
    final public int removeForce( Long firstId, final Long... otherId )
    {
//        String[] keys = new String[ otherId.length + 1 ];
//        if( otherId.length != 0 )
//        {
//            System.arraycopy( otherId, 0, keys, 1, keys.length - 1 );
//        }
//        keys[ 0 ] = firstId;
        int count = 0;
//        if( keys.length != 0 )
//        {
//            try
//            {
//                StringBuilder deleteSql = new StringBuilder( DeleteOperation.getDeleteSql( connector, getUserInfo(), getEntityName(), getTcloneId(), false ) );
//                deleteSql.append( " WHERE " ).append( analyzer.quoteIdentifier( getPrimaryKeyName() ) ).append( " IN " ).append( Utils.toInClause( keys ) );
//                deleteSql.append( "AND" ).append( getAdditionalConditions() );
//                try
//                {
//                    count = connector.executeUpdate( deleteSql.toString() );
//                }
//                catch( SQLException e )
//                {
//                    throw new EntityModelSQLException( getEntityName(), deleteSql.toString(), e );
//                }
//                if( isDictionary() )
//                {
//                    clearDictionaryCache();
//                }
//            }
//            catch( Exception e )
//            {
//                throw new RuntimeException( e );
//            }
//        }
        return count;
    }

    @Override
    // TODO make is deleted column check and handle it
    public int remove( Map<String, String> values )
    {
//        String sql = "DELETE FROM " + getEntityName() + Utils.ifNull( getTcloneId(), "" ) + "\n" +
//                     "WHERE " + getAdditionalConditions();
//        if( !values.isEmpty() )
//        {
//            sql += "\nAND " + Utils.paramsToCondition( connector, getEntityName(), values );
//        }
//        else
//        {
//            throw new EntityModelException( "Value map can't be empty!", getEntityName() );
//        }
//        try
//        {
//            int result = connector.executeUpdate( sql );
//            if( isDictionary() )
//            {
//                clearDictionaryCache();
//            }
//            return result;
//        }
//        catch( SQLException e )
//        {
//            throw new RuntimeException( e );
//        }
        return 0;
    }

    @Override
    public String getTableName()
    {
        return getEntityName();
    }

//    @Override
//    public String getTcloneId()
//    {
//        return this.tcloneId;
//    }
//
//    /**
//     *
//     * @deprecated class must be immutable
//     */
//    @Deprecated
//    public void setTcloneId( String tcloneId )
//    {
//        this.tcloneId = tcloneId;
//    }

    @Override
    public String getEntityName()
    {
        return this.entity.getName();
    }

    @Override
    public String getPrimaryKeyName()
    {
        return entity.getPrimaryKey();
    }

    @Override
    public List<R> toList()
    {
        return toList( Collections.emptyMap() );
    }

    @Override
    public RecordModel[] toArray()
    {
        return toArray( Collections.emptyMap() );
    }
    
    @Override
    public List<R> toList( Map<String, String> values )
    {
        return new MultipleRecordsBase<List<R>>( databaseService, getEntityName() ).get( values );
    }

    @Override
    public RecordModel[] toArray( Map<String, String> values )
    {
        MultipleRecordsBase<R[]> records = new MultipleRecordsBase<>( databaseService, getEntityName() );
        records.setHandler( new AbstractMultipleRecords.ArrayHandler<>() );
        return records.get( values );
    }

    @Override
    public List<R> collect()
    {
        return toList();
    }

    @Override
    public <T> List<T> collect( Map<String, String> values, BiFunction<R, Integer, T> lambda )
    {
        MultipleRecordsBase<List<T>> records = new MultipleRecordsBase<>( databaseService, getEntityName() );
        AbstractMultipleRecords.LambdaDPSHandler<R, T> handler = new AbstractMultipleRecords.LambdaDPSHandler<>( lambda );
        records.setHandler( handler );
        records.get( values );
        return handler.getResult();
    }

    private boolean isDictionary()
    {
        return EntityType.DICTIONARY == entity.getType();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[ entityName = " + getEntityName() + " ]";
    }

    @Override
    final public Long addForce( Map<String, String> values )
    {
        DynamicPropertySet dps = sqlHelper.getTableDps(entity, values);

        return db.insert(sqlHelper.generateInsertSql(dps, entity), sqlHelper.getValues(dps));
    }

    @Override
    final public void setForce( Long id, String propertyName, String value )
    {
        setForce( id, Collections.singletonMap( propertyName, value ) );
    }

    @Override
    public void set( Long id, Map<String, String> values )
    {
        setForce( id, values );
    }

    @Override
    final public void setForce( Long id, Map<String, String> values )
    {
//        String sql;
//        try
//        {
//            sql = Utils.getUpdateSQL( connector, getUserInfo(), getEntityName(), new String[]{ id }, values );
//        }
//        catch( Exception e )
//        {
//            String reason = "Error generating update SQL.";
////            Logger.error( cat, reason, e );
////            throw new EntityModelException( reason, e );
//            throw Be5Exception.internal(e, reason);
//        }
//        try
//        {
//            Pair<Boolean,String> clobResult = Utils.updateWithCLOBs(
//                 connector, sql, values, getEntityName(), getPrimaryKeyName(), false );
//            if( !clobResult.getFirst() )
//            {
//                connector.executeUpdate( sql );
//            }
//            if( isDictionary() )
//            {
//                clearDictionaryCache();
//            }
//        }
//        catch( SQLException e )
//        {
//            throw new EntityModelSQLException( getEntityName(), sql, e );
//        }
    }

//    @Deprecated
//    public static class EntityModelBaseWithCondition extends EntityModelBase implements EntityModelWithCondition
//    {
//        final private String conditions;
//
//        protected EntityModelBaseWithCondition( DatabaseModel database, UserInfo userInfo, String entityName, String tcloneId, boolean forceCache, List<String> conditions )
//        {
//            super( database, userInfo, entityName, tcloneId, forceCache );
//            StringBuilder sb = new StringBuilder();
//            for( String condition : conditions )
//            {
//                sb.append( " AND " ).append( condition ).append( "\n" );
//            }
//            this.conditions = sb.toString();
//        }
//
//        @Override
//        public String getAdditionalConditions()
//        {
//            StringBuilder conditions = new StringBuilder()
//                    .append( "( " )
//                    .append( super.getAdditionalConditions() )
//                    .append( " )" );
//            conditions.append( this.conditions );
//
//            return conditions.toString();
//        }
//
//        @Override
//        public UserInfo getUserInfo()
//        {
//            return null;
//        }
//
//        @Override
//        public String toString()
//        {
//            return super.toString() + "\nconditions: [" + this.conditions + "]";
//        }
//
//    }

    private class MultipleRecordsBase<T> extends AbstractMultipleRecords<T>
    {
        public MultipleRecordsBase(DatabaseService databaseService, String tableName )
        {
            super( databaseService, tableName );
        }

        @Override
        public RecordModel createRecord( DynamicPropertySet dps )
        {
            return new RecordModelBase( dps );
        }

        @Override
        public String getAdditionalConditions()
        {
            return EntityModelBase.this.getAdditionalConditions();
        }

    }

    @Override
    public void makeClonedTable( boolean cloneIndexes ) throws TableAlreadyExistsException
    {
//        if( getTcloneId() == null )
//        {
//            return;
//        }
//
//        if( isTableExists() )
//        {
//            throw new TableAlreadyExistsException( getTableName(), "Cloned table already exists" );
//        }
//
//        String sql = analyzer.makeTableLikeExpr( getEntityName(), getTableName() );
//        try
//        {
//            connector.executeUpdate( sql );
//        }
//        catch( SQLException e )
//        {
//            throw new EntityModelSQLException( getEntityName(), sql, e );
//        }
//
//        if( cloneIndexes )
//        {
//            List<String> sqlList = analyzer.makeIndexesLikeExpr( getEntityName(), getTableName() );
//            try
//            {
//                connector.executeBatch( sqlList );
//            }
//            catch( SQLException e )
//            {
//                throw new EntityModelSQLException( getEntityName(), sqlList.stream().collect( Collectors.joining( "\n," ) ), e );
//            }
//        }
    }

    @Override
    public boolean dropClonedTable()
    {
        return false;//getTcloneId() != null && analyzer.dropTableIfExists( getTableName() );
    }
//
//    @Override
//    public boolean isTableExists()
//    {
//        return meta.getEntity(getTableName()) != null;
//    }

    @Override
    public QueryModel getQuery(String queryName, Map<String, String> params )
    {
        return new QueryModelBase( queryName, params );
    }

    @Override
    public OperationModel getOperation( String operationName )
    {
        return new OperationModelBase( operationName );
    }

    @Override
    public Long leftShift( Map<String, String> values )
    {
        return this.add( values );
    }

    @Override
    public RecordModel call( Map<String, String> values)
    {
        return this.get( values );
    }

    @Override
    public RecordModel getAt(Long id)
    {
        return null;
    }

    @Override
    public QueryModel getQuery( String queryName ) 
    {
        return new QueryModelBase( queryName, Collections.emptyMap() );
    }

    @Override
    public boolean equals( Object o )
    {
        if( !( o instanceof EntityModel) )
        {
            return false;
        }
        EntityModel e = ( EntityModel )o;
        return e.getEntityName().equals( getEntityName() );
    }

    @SuppressWarnings( "serial" )
    class RecordModelBase extends DynamicPropertySetBlocked implements RecordModel
    {
        public RecordModelBase(DynamicPropertySet dps)
        {
            super( dps );
        }

        protected RecordModelBase(Long id) throws SQLException
        {
            super( sqlHelper.getRecordById( entity, id ) );
        }

        @Override
        public EntityModelWithCondition getEntity(String entityName )
        {
//            String sql = "SELECT "
//                    + "\n   columnsFrom AS columnFrom,"
//                    + "\n   columnsTo AS columnTo"
//                    + "\n FROM table_refs"
//                    + "\n WHERE tableFrom = '" + getEntityName() + "'"
//                    + "\n AND   tableTo = '" + entityName + "'";
//            try
//            {
//                QRec tableRef;
//                try
//                {
//                    tableRef = new QRec( connector, sql );
//                }
//                catch( NoRecord e )
//                {
//                    sql = "SELECT "
//                            + "\n   columnsTo AS columnFrom,"
//                            + "\n   columnsFrom AS columnTo"
//                            + "\n FROM table_refs tr1"
//                            + "\n WHERE tr1.tableFrom = '" + entityName + "' "
//                            + "\n AND tr1.tableTo = '" + getEntityName() + "'";
//                    tableRef = new QRec( connector, sql );
//                }
//                String columnName = tableRef.getString( "columnTo" );
//                String columnValue = getValueAsString( tableRef.getString( "columnFrom" ) );
//                Map<String, String> condition = Collections.singletonMap( columnName, columnValue );
//                List<String> conditionList = Collections.singletonList( Utils.paramsToCondition( connector, entityName, condition ) );
//                return new EntityModelBaseWithCondition( database, getUserInfo(), entityName, getTcloneId(), forceCache, conditionList );
//            }
//            catch( NoRecord e )
//            {
//                throw new ReferenceNotFoundException( getEntityName(), entityName );
//            }
//            catch( SQLException e )
//            {
//                throw new RuntimeException( e );
//            }
            return null;
        }

        @Override
        public Long getId()
        {
            return (Long) delegateDps.getValue(entity.getPrimaryKey());
        }

        @Override
        public void remove()
        {
            int b = EntityModelBase.this.remove( getId() );
            // TODO check state if record been removed
            if( b != 1 )
            {
                //Logger.error( cat, "Record is missing. ID = " + getId(), new EntityModelException( getEntityName() ) );
            }
        }

        @Override
        public String toString()
        {
            return super.toString() + " { " + this.getClass().getSimpleName() + " [ " + getPrimaryKeyName() + " = " + getId() + " ] }";
        }

        @Override
        public Object invokeMethod( String methodName, Object... arguments )
        {
            Method method = ExtendedModels.getInstance().getMethod( EntityModelBase.this, methodName );
            return new MethodProviderBase( method ).invoke( arguments );
        }

        public MethodProvider getMethod(String methodName )
        {
            Method method = ExtendedModels.getInstance().getMethod( EntityModelBase.this, methodName );
            return new MethodProviderBase( method );
        }

        @Override
        public void update( String propertyName, String value )
        {
            EntityModelBase.this.set( getId(), propertyName, value );
            super.setValueHidden( propertyName, value );
        }

        @Override
        public void update( Map<String, String> values )
        {
            EntityModelBase.this.set( getId(), values );
            for( String propertyName : values.keySet() )
            {
                if( super.hasProperty( propertyName ) )
                {
                    super.setValueHidden( propertyName, values.get( propertyName ) );
                }
            }
        }

        @Override
        public void setValue( String propertyName, Object value )
        {
            throw new IllegalAccessError( "You can't use this operation. Use EntityModel#set() to update value in database." );
        }

        public class MethodProviderBase implements MethodProvider
        {
            protected final Method method;

            protected MethodProviderBase( Method method )
            {
                this.method = method;
            }

            @Override
            public Object invoke()
            {
                return invoke( new Object[]{} );
            }

            @Override
            public Object invoke( Object... args )
            {
                try
                {
                    Object[] fullArgs = new Object[ args.length + 1 ];
                    fullArgs[ 0 ] = RecordModelBase.this;
                    for( int i = 0; i < args.length; i++ )
                    {
                        fullArgs[ i + 1 ] = args[ i ];
                    }
                    return method.invoke( EntityModelBase.this, fullArgs );
                }
                catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
                {
                    throw new RuntimeException( e );
                }
            }
        }
    }
}
