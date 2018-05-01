package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.api.helpers.ColumnsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.impl.SqlHelper;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.databasemodel.groovy.RecordModelMetaClass;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.databasemodel.OperationModel;
import com.developmentontheedge.be5.databasemodel.QueryModel;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.databasemodel.groovy.EntityModelMetaClass;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.databasemodel.groovy.QueryModelMetaClass;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstSelect;
import com.google.common.collect.ObjectArrays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.IS_DELETED_COLUMN_NAME;
import static java.util.Collections.emptyMap;


public class EntityModelBase<T> implements EntityModel<T>
{
    static
    {
        GroovyRegister.registerMetaClass( EntityModelMetaClass.class, EntityModelBase.class );
        GroovyRegister.registerMetaClass( RecordModelMetaClass.class, RecordModelBase.class );
        GroovyRegister.registerMetaClass( QueryModelMetaClass.class, QueryModelBase.class );
    }

    private final SqlService db;
    private final SqlHelper sqlHelper;
    private final ColumnsHelper columnsHelper;
    private final DpsHelper dpsHelper;
    private final OperationHelper operationHelper;
    private final OperationExecutor operationExecutor;
    private final Meta meta;

    private final Entity entity;

    EntityModelBase(SqlService db, SqlHelper sqlHelper, ColumnsHelper columnsHelper, DpsHelper dpsHelper, OperationHelper operationHelper,
                           OperationExecutor operationExecutor, Meta meta, Entity entity)
    {
        this.db = db;
        this.sqlHelper = sqlHelper;
        this.columnsHelper = columnsHelper;
        this.dpsHelper = dpsHelper;
        this.operationHelper = operationHelper;
        this.operationExecutor = operationExecutor;
        this.meta = meta;

        this.entity = entity;
    }

    @Override
    public RecordModel<T> get( Map<String, ? super Object> conditions )
    {
        return getColumns(Collections.emptyList(), conditions);
    }

    @Override
    public RecordModel<T> get( T id )
    {
        return get(Collections.singletonMap(getPrimaryKeyName(), id));
    }

    @Override
    public RecordModel<T> getColumns( List<String> columns, T id )
    {
        return getColumns(columns, Collections.singletonMap(getPrimaryKeyName(), id));
    }

    @Override
    public RecordModel<T> getColumns( List<String> columns, Map<String, ? super Object> conditions )
    {
        Objects.requireNonNull(conditions);
        checkPrimaryKey(conditions);

        AstSelect sql = Ast.select(addPrimaryKeyColumnIfNotEmpty(columns))
                .from(entity.getName())
                .where(conditions);

        DynamicPropertySet dps = db.select(sql.format(),
                rs -> dpsHelper.addDpWithoutTags(new DynamicPropertySetSupport(), entity, rs),
                conditions.values().toArray());

        return getRecordModel(dps);
    }

    private RecordModel<T> getRecordModel(DynamicPropertySet dps)
    {
        if(dps == null)return null;

        T primaryKey = (T)dps.getProperty(getPrimaryKeyName()).getValue();

        return new RecordModelBase<>(primaryKey, this, dps );
    }

    private List<String> addPrimaryKeyColumnIfNotEmpty(List<String> columns)
    {
        List<String> columnsWithPK = columns;
        if(columns.size() > 0 && !columns.contains(getPrimaryKeyName()))
        {
            columnsWithPK = new ArrayList<>(columns);
            columnsWithPK.add(getPrimaryKeyName());
        }
        return columnsWithPK;
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
    public boolean containsAll( Collection<Map<String, ? super Object>> c )
    {
        return c.stream().allMatch( this::contains );
    }

    @Override
    public <R> R add( Map<String, ? super Object> values )
    {
        Objects.requireNonNull(values);

        values = new LinkedHashMap<>(values);
        values.values().removeIf(Objects::isNull);

        columnsHelper.addInsertSpecialColumns(entity, values);
        columnsHelper.checkDpsColumns(entity, values);

        return sqlHelper.insert(entity.getName(), values);
    }

    @Override
    final public <R> R add( DynamicPropertySet dps )
    {
        Objects.requireNonNull(dps);

        return add(dpsHelper.toLinkedHashMap(dps));
//        validator.checkErrorAndCast(dps);
//
//        dpsHelper.addInsertSpecialColumns(entity, dps);
//        dpsHelper.checkDpsColumns(entity, dps);
//
//        return db.insert(dpsHelper.generateInsertSql(entity, dps), dpsHelper.getValues(dps));
    }

    @Override
    public <R> List<R> addAll( final Collection<Map<String, ? super Object>> c )
    {
        final List<R> keys = new ArrayList<>( c.size() );
        for( Map<String, ? super Object> values : c )
        {
            keys.add( add( values ) );
        }
        return keys;
    }

    @Override
    public int set( T id, String propertyName, Object value )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(propertyName);
        Objects.requireNonNull(value);
        return this.set( id, Collections.singletonMap( propertyName, value ) );
    }

    @Override
    public int set( T id, Map<String, ? super Object> values )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(values);

        values = new LinkedHashMap<>(values);

        columnsHelper.addUpdateSpecialColumns(entity, values);

//        DynamicPropertySet dps = new DynamicPropertySetSupport();
//        dpsHelper.addDpForColumnsBase(dps, entity, values.keySet(), values);

        return sqlHelper.update(entity.getName(), getPrimaryKeyName(), checkPrimaryKey(id), values);
    }

    @Override
    public int set(T id, DynamicPropertySet dps )
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(dps);

        return set(id, dpsHelper.toLinkedHashMap(dps));

//        validator.checkErrorAndCast(dps);
//        dpsHelper.addUpdateSpecialColumns(entity, dps);
//
//        return db.update(dpsHelper.generateUpdateSqlForOneKey(entity, dps),
//                ObjectArrays.concat(dpsHelper.getValues(dps), checkPrimaryKey(id)));
    }
//
//    @Override
//    public int removeAll( Collection<Map<String, ? super Object>> c )
//    {
//        throw new UnsupportedOperationException( "not implemented" );
//    }

    @Override
    @SuppressWarnings("unchecked")
    public final int remove(T id)//removed final T... otherId - 'possible heap pollution from parameterized varargs'
    {
        Objects.requireNonNull(id);
        return removeWhereColumnIn(getPrimaryKeyName(), (T[])new Object[]{id});
    }

    @Override
    public int remove( T[] ids )
    {
        return removeWhereColumnIn(getPrimaryKeyName(), ids);
    }

    @Override
    public int removeWhereColumnIn(String columnName, T[] ids)
    {
        Objects.requireNonNull(columnName);
        Objects.requireNonNull(ids);
        if(columnName.equals(getPrimaryKeyName()))checkPrimaryKey(ids);

        ColumnDef columnDef = meta.getColumn(entity, columnName);

        Map<String, ColumnDef> columns = meta.getColumns(entity);

        if(columns.containsKey( IS_DELETED_COLUMN_NAME ))
        {
            //sqlHelper.update(entity.getName())
            return db.update(dpsHelper.generateDeleteInSql(entity, columnDef.getName(), ids.length),
                    ObjectArrays.concat(columnsHelper.getDeleteSpecialValues(entity),
                            Utils.changeTypes(ids, meta.getColumnType(columnDef)), Object.class)
            );
        }
        else
        {
            //return sqlHelper.deleteIn(entity.getName(), columnName, ids);
            return db.update(dpsHelper.generateDeleteInSql(entity, columnDef.getName(), ids.length),
                    ObjectArrays.concat(columnsHelper.getDeleteSpecialValues(entity),
                            Utils.changeTypes(ids, meta.getColumnType(columnDef)), Object.class)
            );
        }
    }

    @Override
    public int removeAll()
    {
        return remove(emptyMap());
    }

    @Override
    public int remove( Map<String, ? super Object> conditions )
    {
        Objects.requireNonNull(conditions);
        return db.update(dpsHelper.generateDelete(entity, conditions),
                ObjectArrays.concat(columnsHelper.getDeleteSpecialValues(entity),
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
    public List<RecordModel<T>> toList()
    {
        return toList( emptyMap() );
    }

    @Override
    public RecordModel<T>[] toArray()
    {
        return toArray( emptyMap() );
    }
    
    @Override
    public List<RecordModel<T>> toList( Map<String, ? super Object> conditions )
    {
        Objects.requireNonNull(conditions);

        String sql = Ast.selectAll().from(entity.getName()).where(conditions).format();

        return operationHelper.readAsRecords(sql, conditions.values().toArray()).stream()
                .map(this::getRecordModel)
                .collect(Collectors.toList());
    }

    @Override
    public RecordModel<T>[] toArray( Map<String, ? super Object> conditions )
    {
        Objects.requireNonNull(conditions);

        List<RecordModel<T>> recordModels = toList(conditions);
        RecordModel<T>[] arr = new RecordModel[recordModels.size()];
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

//    @Override
//    public void setMany( Map<String, String> values, Map<String, String> conditions )
//    {
//        Objects.requireNonNull(values);
//        Objects.requireNonNull(conditions);
//        setForceMany(values, conditions);
//    }

    private void checkPrimaryKey(Map<String, ? super Object> conditions)
    {
        for (Map.Entry<String, ? super Object> entry : conditions.entrySet()) {
            if(entry.getKey().equalsIgnoreCase(getPrimaryKeyName())) checkPrimaryKey((T)entry.getValue());
        }
    }

    private void checkPrimaryKey(T[] ids)
    {
        for (T id : ids) {
            checkPrimaryKey(id);
        }
    }

    private T checkPrimaryKey(T id)
    {
        Class<?> primaryKeyColumnType = meta.getColumnType(entity, getPrimaryKeyName());

        if(id.getClass() != primaryKeyColumnType)
        {
            throw new RuntimeException("Primary key must be " + primaryKeyColumnType.getSimpleName());
        }

        return id;
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
                .setQueryName("from another operation")
                .setOperationName(operationName);
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
