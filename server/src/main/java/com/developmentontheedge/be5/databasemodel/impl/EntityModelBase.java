package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.databasemodel.groovy.RecordModelMetaClass;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.databasemodel.OperationModel;
import com.developmentontheedge.be5.databasemodel.QueryModel;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.databasemodel.groovy.EntityModelMetaClass;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.databasemodel.groovy.QueryModelMetaClass;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstSelect;
import com.google.common.collect.ObjectArrays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;


public class EntityModelBase<R extends RecordModelBase> implements EntityModel<R>
{
    static
    {
        GroovyRegister.registerMetaClass( EntityModelMetaClass.class, EntityModelBase.class );
        GroovyRegister.registerMetaClass( RecordModelMetaClass.class, RecordModelBase.class );
        GroovyRegister.registerMetaClass( QueryModelMetaClass.class, QueryModelBase.class );
    }

    private final SqlService db;
    private final DpsHelper dpsHelper;
    private final OperationHelper operationHelper;
    private final OperationExecutor operationExecutor;
    private final Validator validator;
    private final Meta meta;

    private final Entity entity;


    public EntityModelBase(SqlService db, DpsHelper dpsHelper, Validator validator, OperationHelper operationHelper,
                           OperationExecutor operationExecutor, Meta meta, Entity entity)
    {
        this.db = db;
        this.dpsHelper = dpsHelper;
        this.operationHelper = operationHelper;
        this.validator = validator;
        this.operationExecutor = operationExecutor;
        this.meta = meta;

        this.entity = entity;
    }

    @Override
    public RecordModel get( Map<String, ? super Object> conditions )
    {
        Objects.requireNonNull(conditions);

        AstSelect sql = Ast
                .selectAll()
                .from(entity.getName())
                .where(conditions);

        DynamicPropertySet dps =
                db.select(sql.format(), rs -> {
                    DynamicPropertySet newDps = new DynamicPropertySetSupport();
                    dpsHelper.addDpWithoutTags(newDps, entity, rs);
                    return newDps;
                }, conditions.values().toArray());

        return dps == null ? null : new RecordModelBase( this, dps );
    }

    @Override
    public long count()
    {
        return count( Collections.emptyMap() );
    }
    
    @Override
    public long count( Map<String, ? super Object> conditions ) {
        Objects.requireNonNull(conditions);

        AstSelect sql = Ast.selectCount().from(entity.getName()).where(conditions);

        return db.getLong(sql.format(), conditions.values().toArray());
    }

    @Override
    public boolean isEmpty()
    {
        return count() == 0;
    }

    @Override
    public boolean contains( Map<String, ? super Object> conditions )
    {
        Objects.requireNonNull(conditions);
        return count(conditions) != 0;
    }

    @Override
    public String add( Map<String, ? super Object> values )
    {
        Objects.requireNonNull(values);
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addDpForColumnsWithoutTags(dps, entity, values.keySet(), values);

        return add( dps );
    }

    @Override
    public boolean containsAll( Collection<Map<String, ? super Object>> c )
    {
        return c.stream().allMatch( this::contains );
    }

    @Override
    public List<String> addAll( final Collection<Map<String, ? super Object>> c )
    {
        final List<String> keys = new ArrayList<>( c.size() );
        for( Map<String, ? super Object> values : c )
        {
            keys.add( add( values ) );
        }
        return keys;
    }

    @Override
    public int removeAll( Collection<Map<String, ? super Object>> c )
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public RecordModel get( String id )
    {
        return get(Collections.singletonMap(entity.getPrimaryKey(), dpsHelper.castToTypePrimaryKey(entity, id)));
    }

    @Override
    public RecordModel get( Long id )
    {
        return get(Collections.singletonMap(entity.getPrimaryKey(), dpsHelper.castToTypePrimaryKey(entity, id)));
    }

    @Override
    public void set( String id, String propertyName, String value )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(propertyName);
        Objects.requireNonNull(value);
        this.set( id, Collections.singletonMap( propertyName, value ) );
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
        return db.update(dpsHelper.generateDeleteInSql(entity, otherId.length + 1),
                ObjectArrays.concat(dpsHelper.getDeleteSpecialValues(entity),
                        dpsHelper.castToTypePrimaryKey(entity, ObjectArrays.concat(firstId, otherId)), Object.class)
        );
    }

    @Override
    public int removeAll(){
        return remove(emptyMap());
    }

    @Override
    public int remove( Map<String, ? super Object> conditions )
    {
        Objects.requireNonNull(conditions);
        return db.update(dpsHelper.generateDelete(entity, conditions),
                ObjectArrays.concat(dpsHelper.getDeleteSpecialValues(entity),
                        conditions.values().toArray(), Object.class)
        );
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
    public List<RecordModel> toList()
    {
        return toList( emptyMap() );
    }

    @Override
    public RecordModel[] toArray()
    {
        return toArray( emptyMap() );
    }
    
    @Override
    public List<RecordModel> toList( Map<String, ? super Object> conditions )
    {
        Objects.requireNonNull(conditions);

        String sql = Ast.selectAll().from(entity.getName()).where(conditions).format();

        return operationHelper.readAsRecords(sql, conditions.values().toArray()).stream()
                .map(dps -> new RecordModelBase( EntityModelBase.this, dps ))
                .collect(Collectors.toList());
    }

    @Override
    public RecordModel[] toArray( Map<String, ? super Object> conditions )
    {
        Objects.requireNonNull(conditions);

        List<RecordModel> recordModels = toList(conditions);
        RecordModel[] arr = new RecordModel[recordModels.size()];
        return recordModels.toArray( arr );
    }
//
//    @Override
//    public List<RecordModel> collect()
//    {
//        return toList();
//    }
//
//    @Override
//    public <T> List<T> collect( Map<String, ? super Object> values, BiFunction<R, Integer, T> lambda )
//    {
//        Objects.requireNonNull(values);
//        Objects.requireNonNull(lambda);
//
//        MultipleRecordsBase<List<T>>  records = new MultipleRecordsBase<>();
//        MultipleRecordsBase.LambdaDPSHandler<R, T> handler = new AbstractMultipleRecords.LambdaDPSHandler<>( lambda );
//        records.setHandler( handler );
//        records.get( values );
//        return handler.getResult();
//    }

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
    final public String add( DynamicPropertySet dps )
    {
        Objects.requireNonNull(dps);

        validator.checkErrorAndCast(dps);
        dpsHelper.checkDpsColumns(entity, dps);
        dpsHelper.addInsertSpecialColumns(entity, dps);

        Object insert = db.insert(dpsHelper.generateInsertSql(entity, dps), dpsHelper.getValues(dps));

        return insert != null ? insert.toString() : null;
    }

    @Override
    public void set( String id, Map<String, ? super Object> values )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(values);

        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addDpForColumnsWithoutTags(dps, entity, values.keySet(), values);

        this.set( id, dps );
    }

//    @Override
//    public void setMany( Map<String, String> values, Map<String, String> conditions )
//    {
//        Objects.requireNonNull(values);
//        Objects.requireNonNull(conditions);
//        setForceMany(values, conditions);
//    }

    @Override
    public void set(String id, DynamicPropertySet dps )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(dps);

        Object pkValue = dpsHelper.castToTypePrimaryKey(entity, id);

        validator.checkErrorAndCast(dps);
        dpsHelper.addUpdateSpecialColumns(entity, dps);

        int count = db.update(dpsHelper.generateUpdateSqlForOneKey(entity, dps),
                ObjectArrays.concat(dpsHelper.getValues(dps), pkValue));
        //todo return count;
    }
//
//    @Override
//    public void setForceMany(String propertyName, String value, Map<String, String> conditions)
//    {
//        Objects.requireNonNull(propertyName);
//        Objects.requireNonNull(value);
//        Objects.requireNonNull(conditions);
//        setForceMany( Collections.singletonMap( propertyName, value ), conditions);
//    }

//    /**
//     * in process
//     */
//    @Override
//    public void setForceMany(Map<String, ? super Object> values, Map<String, ? super Object> conditions)
//    {
//        Objects.requireNonNull(values);
//        Objects.requireNonNull(conditions);
//
//        DynamicPropertySet dps = db.select(
//                Ast.selectAll().from(entity.getName()).where(conditions).limit(1).format(),
//                rs -> dpsHelper.addDpForColumns(entity, values.keySet(), rs), castValues(entity, conditions));
//
//        dpsHelper.updateValuesWithSpecial(dps, values);
//
//        db.update(dpsHelper.generateUpdateSqlForConditions(entity, dps, conditions),
//                ObjectArrays.concat(dpsHelper.getValuesFromJson(dps), castValues(entity, conditions), Object.class));
//    }

    @Override
    public QueryModel getQuery(String queryName, Map<String, ? super Object> params )
    {
        return new QueryModelBase( queryName, params );
    }

    @Override
    public OperationModel getOperation( String operationName )
    {
        return new OperationModelBase(meta, operationExecutor)
                .setEntityName(entity.getName())
                .setQueryName("from another operation");
    }

    @Override
    public QueryModel getQuery( String queryName ) 
    {
        return new QueryModelBase( queryName, emptyMap() );
    }

    @Override
    public Entity getEntity()
    {
        return entity;
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
