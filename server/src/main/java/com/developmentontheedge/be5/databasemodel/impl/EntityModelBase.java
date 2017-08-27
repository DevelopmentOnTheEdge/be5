package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.annotations.DirtyRealization;
import com.developmentontheedge.be5.api.helpers.Validator;
import com.developmentontheedge.be5.api.helpers.SqlHelper;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.databasemodel.groovy.RecordModelMetaClass;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.databasemodel.EntityModelAdapter;
import com.developmentontheedge.be5.databasemodel.OperationModel;
import com.developmentontheedge.be5.databasemodel.QueryModel;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.databasemodel.groovy.EntityModelMetaClass;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.databasemodel.groovy.QueryModelMetaClass;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstSelect;
import com.google.common.collect.ObjectArrays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;


public class EntityModelBase<R extends RecordModelBase> implements EntityModelAdapter<R>
{
    static
    {
        GroovyRegister.registerMetaClass( EntityModelMetaClass.class, EntityModelBase.class );
        GroovyRegister.registerMetaClass( RecordModelMetaClass.class, RecordModelBase.class );
        GroovyRegister.registerMetaClass( QueryModelMetaClass.class, QueryModelBase.class );
    }

    private final SqlService db;
    private final SqlHelper sqlHelper;
    private final Validator validator;

    private final Entity entity;


    public EntityModelBase(SqlService db, SqlHelper sqlHelper, Validator validator, Entity entity)
    {
        this.db = db;
        this.sqlHelper = sqlHelper;
        this.validator = validator;

        this.entity = entity;
    }

    @Override
    public RecordModel get( Map<String, String> conditions )
    {
        Objects.requireNonNull(conditions);

        AstSelect sql = Ast
                .selectAll()
                .from(entity.getName())
                .where(conditions);

        DynamicPropertySet dps = db.select(sql.format(), rs -> sqlHelper.getDps(entity, rs), castValues(entity, conditions));
        return dps == null ? null : new RecordModelBase( this, dps );
    }

    @DirtyRealization(comment="move to sqlHelper, use castToType")
    private Object[] castValues(Entity entity, Map<String, String> stringValues){
        DynamicPropertySet dps = sqlHelper.getDps(entity);
        sqlHelper.getDps(entity);

        Object[] values = new Object[stringValues.size()];

        int i=0;
        for(Map.Entry<String,String> entry : stringValues.entrySet())
        {
            values[i] = castValue(dps, entry.getKey(), entry.getValue());
        }
        return values;
    }

    @DirtyRealization(comment="move to sqlHelper, use castToType")
    private Object castValue(DynamicPropertySet dps, String name, String value){
        return validator.getTypedValueFromString(dps.getProperty(name).getType(), value);
    }

    @Override
    public long count()
    {
        return count( Collections.emptyMap() );
    }
    
    @Override
    public long count( Map<String, String> conditions ) {
        Objects.requireNonNull(conditions);

        AstSelect sql = Ast.selectCount().from(entity.getName()).where(conditions);

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
    public boolean contains( Map<String, String> conditions )
    {
        Objects.requireNonNull(conditions);
        return count(conditions) != 0;
    }

    @Override
    public String add( Map<String, String> values )
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
    public List<String> addAll( final Collection<Map<String, String>> c )
    {
        final List<String> keys = new ArrayList<>( c.size() );
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
    public RecordModel get( String id )
    {
        return get(Collections.singletonMap(entity.getPrimaryKey(), "" + id));
    }

    @Override
    public void set( String id, String propertyName, String value )
    {
        setForce( id, propertyName, value );
    }

    @Override
    public int remove( String firstId, final String... otherId )
    {
        Objects.requireNonNull(firstId);
        return removeForce( firstId, otherId );
    }

    @Override
    final public int removeForce( String firstId, final String... otherId )
    {
        Objects.requireNonNull(firstId);
        return db.update(sqlHelper.generateDeleteInSql(entity, otherId.length + 1),
                sqlHelper.getDeleteValuesWithSpecial(entity, ObjectArrays.concat(firstId, otherId)));
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
    final public String addForce( Map<String, String> values )
    {
        Objects.requireNonNull(values);
        DynamicPropertySet dps = sqlHelper.getDpsForColumns(entity, values.keySet());
        sqlHelper.setValuesWithSpecial(dps, entity, values);

        validator.checkErrorAndCast(dps);
        Object insert = db.insert(sqlHelper.generateInsertSql(entity, dps), sqlHelper.getValues(dps));

        return insert != null ? insert.toString() : null;
    }

    @Override
    final public void setForce( String id, String propertyName, String value )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(propertyName);
        Objects.requireNonNull(value);
        setForce( id, Collections.singletonMap( propertyName, value ) );
    }

    @Override
    public void set( String id, Map<String, String> values )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(values);
        setForce( id, values );
    }

    @Override
    public void setMany( Map<String, String> values, Map<String, String> conditions )
    {
        Objects.requireNonNull(values);
        Objects.requireNonNull(conditions);
        setForceMany(values, conditions);
    }

    @Override
    final public void setForce( String id, Map<String, String> values )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(values);

        Object pkValue = sqlHelper.castToTypePrimaryKey(entity, id);

        DynamicPropertySet dps = db.select(
                Ast.selectAll().from(entity.getName()).where(entity.getPrimaryKey(), id).format(),
                rs -> sqlHelper.getDpsWithoutAutoIncrement(entity, rs), pkValue);

        sqlHelper.updateValuesWithSpecial(dps, values);

        db.update(sqlHelper.generateUpdateSqlForOneKey(entity, dps),
                ObjectArrays.concat(sqlHelper.getValues(dps), pkValue));
    }

    @Override
    public void setForceMany(String propertyName, String value, Map<String, String> conditions)
    {
        Objects.requireNonNull(propertyName);
        Objects.requireNonNull(value);
        Objects.requireNonNull(conditions);
        setForceMany( Collections.singletonMap( propertyName, value ), conditions);
    }

    @Override
    public void setForceMany(Map<String, String> values, Map<String, String> conditions)
    {
        Objects.requireNonNull(values);
        Objects.requireNonNull(conditions);

        DynamicPropertySet dps = db.select(
                Ast.selectAll().from(entity.getName()).where(conditions).limit(1).format(),
                rs -> sqlHelper.getDpsForColumns(entity, values.keySet(), rs), castValues(entity, conditions));

        sqlHelper.updateValuesWithSpecial(dps, values);

        db.update(sqlHelper.generateUpdateSqlForConditions(entity, dps, conditions),
                ObjectArrays.concat(sqlHelper.getValues(dps), castValues(entity, conditions), Object.class));
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
            return new RecordModelBase( EntityModelBase.this, dps );
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

}
