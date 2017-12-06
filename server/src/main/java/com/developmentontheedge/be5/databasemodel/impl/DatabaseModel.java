package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.api.sql.SqlExecutorVoid;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.databasemodel.groovy.GDynamicPropertySetMetaClass;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.model.QRec;
import com.developmentontheedge.be5.databasemodel.EntityAccess;
import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.databasemodel.groovy.DatabaseModelMetaClass;
import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.model.beans.GDynamicPropertySetSupport;
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

        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, QRec.class );

        GroovyRegister.registerMetaClass( GDynamicPropertySetMetaClass.class, GDynamicPropertySetSupport.class );
    }

    private final SqlService sqlService;
    private final DpsHelper dpsHelper;
    private final OperationHelper operationHelper;
    private final Meta meta;
    private final Validator validator;
    private final OperationExecutor operationExecutor;


    public DatabaseModel(SqlService sqlService, DpsHelper dpsHelper, OperationHelper operationHelper,
                         Meta meta, Validator validator, OperationExecutor operationExecutor)
    {
        this.sqlService = sqlService;
        this.dpsHelper = dpsHelper;
        this.operationHelper = operationHelper;
        this.meta = meta;
        this.validator = validator;
        this.operationExecutor = operationExecutor;
    }

    @Override
    public EntityModel getEntity( String entityName )
    {
        Objects.requireNonNull(entityName);
        Entity entity = meta.getEntity(entityName);

        if (entity == null)throw Be5Exception.unknownEntity(entityName);

        return new EntityModelBase(sqlService, dpsHelper, validator, operationHelper,
                                   operationExecutor, meta, entity);
    }

    public <T> T transactionWithResult(SqlExecutor<T> executor)
    {
        return sqlService.transactionWithResult(executor);
    }

    public void transaction(SqlExecutorVoid executor)
    {
        sqlService.transaction(executor);
    }
}
