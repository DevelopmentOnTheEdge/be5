package com.developmentontheedge.be5.operation.databasemodel.impl;


import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.Validator;
import com.developmentontheedge.be5.api.services.SqlHelper;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.databasemodel.EntityModel;
import com.developmentontheedge.be5.operation.databasemodel.EntityModelAdapter;
import com.developmentontheedge.be5.operation.databasemodel.MethodProvider;
import com.developmentontheedge.be5.operation.databasemodel.OperationModel;
import com.developmentontheedge.be5.operation.databasemodel.QueryModel;
import com.developmentontheedge.be5.operation.databasemodel.RecordModel;
import com.developmentontheedge.be5.operation.databasemodel.groovy.EntityModelMetaClass;
import com.developmentontheedge.be5.operation.databasemodel.groovy.GroovyRegister;
import com.developmentontheedge.be5.operation.databasemodel.groovy.QueryModelMetaClass;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetBlocked;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstSelect;
import com.google.common.collect.ObjectArrays;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class EntityModelBase<R extends EntityModelBase.RecordModelBase> implements EntityModelAdapter<R>
{
    static
    {
        GroovyRegister.registerMetaClass( EntityModelMetaClass.class, EntityModelBase.class );
        //GroovyRegister.registerMetaClass( RecordModelMetaClass.class, RecordModelBase.class );
        GroovyRegister.registerMetaClass( QueryModelMetaClass.class, QueryModelBase.class );
    }

    private SqlService db;
    private SqlHelper sqlHelper;

    final private Entity entity;


    public EntityModelBase(SqlService db, SqlHelper sqlHelper, Entity entity)
    {
        this.db = db;
        this.sqlHelper = sqlHelper;

        this.entity = entity;
    }

    @Override
    public RecordModel get( Map<String, String> values )
    {
        Objects.requireNonNull(values);

        //String sql = "SELECT * FROM " + entity.getName() + " WHERE " + sqlHelper.generateConditionsSql(values);
        AstSelect sql = Ast
                .select(AstDerivedColumn.ALL)
                .from(entity.getName())
                .where(values);

        DynamicPropertySet dps = db.select(sql.format(), DpsHelper::createDps, values.values().toArray());
        return new RecordModelBase( dps );
    }

    @Override
    public long count()
    {
        return count( Collections.emptyMap() );
    }
    
    @Override
    public long count( Map<String, String> conditions ) {
        Objects.requireNonNull(conditions);

        //String sql = "SELECT COUNT(*) FROM " + entity.getName() + " WHERE " + sqlHelper.generateConditionsSql(conditions);
        AstSelect sql = Ast
                .select(AstDerivedColumn.COUNT)
                .from(entity.getName())
                .where(conditions);

        return db.getLong(sql.format(), conditions.values().toArray());
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
        return count() == 0;
    }

    @Override
    public boolean contains( Map<String, String> values )
    {
        Objects.requireNonNull(values);
        return count(values) != 0;
    }

    @Override
    public Long add( Map<String, String> values )
    {
        Objects.requireNonNull(values);
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
    public RecordModel get( Long id )
    {
        Objects.requireNonNull(id);
        try
        {
            return new RecordModelBase( id );
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
        Objects.requireNonNull(firstId);
        return removeForce( firstId, otherId );
    }

    @Override
    final public int removeForce( Long firstId, final Long... otherId )
    {
        Objects.requireNonNull(firstId);
        return db.update(sqlHelper.generateDeleteInSql(entity, otherId.length + 1),
                ObjectArrays.concat(firstId, otherId));
//        if( keys.length != 0 )
//        {
//            try
//            {
//                StringBuilder deleteSql = new StringBuilder( DeleteOperation.getDeleteSql( connector, getUserInfo(), getEntityName(), getTcloneId(), false ) );
//                deleteSql.append( " WHERE " ).append( analyzer.quoteIdentifier( getPrimaryKeyName() ) ).append( " IN " ).append( Utils.toInClause( keys ) );
//                deleteSql.append( "AND" ).append( getAdditionalConditions() );

//                    count = connector.executeUpdate( deleteSql.toString() );
    }

    @Override
    // TODO make is deleted column check and handle it
    public int remove( Map<String, String> values )
    {
        Objects.requireNonNull(values);
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
        Objects.requireNonNull(values);
        return new MultipleRecordsBase<List<R>>().get( values );
    }

    @Override
    public RecordModel[] toArray( Map<String, String> values )
    {
        Objects.requireNonNull(values);
        MultipleRecordsBase<R[]> records = new MultipleRecordsBase<>();
        records.setHandler( new MultipleRecordsBase.ArrayHandler<>() );
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
        Objects.requireNonNull(values);
        Objects.requireNonNull(lambda);

        MultipleRecordsBase<List<T>>  records = new MultipleRecordsBase<>();
        MultipleRecordsBase.LambdaDPSHandler<R, T> handler = new AbstractMultipleRecords.LambdaDPSHandler<>( lambda );
        records.setHandler( handler );
        records.get( values );
        return handler.getResult();
    }

//    private boolean isDictionary()
//    {
//        return EntityType.DICTIONARY == entity.getType();
//    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[ entityName = " + getEntityName() + " ]";
    }

    @Override
    final public Long addForce( Map<String, String> values )
    {
        Objects.requireNonNull(values);
        DynamicPropertySet dps = sqlHelper.getEntityDps(entity, values);
        Validator.checkAndCast(dps);

        return db.insert(sqlHelper.generateInsertSql(entity, dps), sqlHelper.getValues(dps));
    }

    @Override
    final public void setForce( Long id, String propertyName, String value )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(propertyName);
        Objects.requireNonNull(value);
        setForce( id, Collections.singletonMap( propertyName, value ) );
    }

    @Override
    public void set( Long id, Map<String, String> values )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(values);
        setForce( id, values );
    }

    @Override
    final public void setForce( Long id, Map<String, String> values )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(values);
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

    private class MultipleRecordsBase<T> extends AbstractMultipleRecords<T>
    {
        MultipleRecordsBase()
        {
            super(entity);
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
        RecordModelBase(DynamicPropertySet dps)
        {
            super( dps );
        }

        RecordModelBase(Long id) throws SQLException
        {
            super( sqlHelper.getRecordById( entity, id ) );
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

            MethodProviderBase( Method method )
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
                    System.arraycopy(args, 0, fullArgs, 1, args.length);
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
