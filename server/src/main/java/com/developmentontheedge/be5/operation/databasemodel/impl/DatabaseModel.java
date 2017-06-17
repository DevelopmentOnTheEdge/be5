package com.developmentontheedge.be5.operation.databasemodel.impl;

import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlHelper;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.operation.databasemodel.EntityAccess;
import com.developmentontheedge.be5.operation.databasemodel.EntityModel;
import com.developmentontheedge.be5.operation.databasemodel.RecordModel;
import com.developmentontheedge.be5.operation.databasemodel.groovy.DatabaseModelMetaClass;
import com.developmentontheedge.be5.operation.databasemodel.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.operation.databasemodel.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.operation.databasemodel.groovy.GroovyRegister;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySetDecorator;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Objects;

/**
 * Database model based on concept of object-oriented database system
 *
 * @author ruslan
 */
final public class DatabaseModel implements EntityAccess<EntityModel<RecordModel>>
{
    static
    {
        GroovyRegister.registerMetaClass( DatabaseModelMetaClass.class, DatabaseModel.class );
        GroovyRegister.registerMetaClass( DynamicPropertyMetaClass.class, DynamicProperty.class );
        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetSupport.class );
        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetDecorator.class );

//        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetScriptable.class );
//        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetLazy.class );
//        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, QRec.class );
//        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, JDBCRecordAdapterAsQuery.class );
//        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, EntityRecordAdapter.class );
    }

    private SqlService sqlService;
    private SqlHelper sqlHelper;
    private Meta meta;

    public DatabaseModel(SqlService sqlService, SqlHelper sqlHelper, Meta meta)
    {
        this.sqlService = sqlService;
        this.sqlHelper = sqlHelper;
        this.meta = meta;
    }

    @Override
    public EntityModel getEntity( String entityName )
    {
        Objects.requireNonNull(entityName);
        return new EntityModelBase(sqlService, sqlHelper, meta.getEntity(entityName));
    }

}
