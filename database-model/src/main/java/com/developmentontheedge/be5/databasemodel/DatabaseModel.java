package com.developmentontheedge.be5.databasemodel;

import com.developmentontheedge.be5.databasemodel.groovy.GDynamicPropertySetMetaClass;
import com.developmentontheedge.be5.databasemodel.impl.EntityModelBase;
import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.databasemodel.helpers.ColumnsHelper;
import com.developmentontheedge.be5.database.impl.SqlHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.databasemodel.groovy.DatabaseModelImplMetaClass;
import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.databasemodel.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.model.QRec;
import com.developmentontheedge.be5.model.beans.GDynamicPropertySetSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySetDecorator;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Database model based on concept of object-oriented database system
 *
 * @author ruslan
 */
final public class DatabaseModel implements EntityAccess
{
    static
    {
        GroovyRegister.registerMetaClass( DatabaseModelImplMetaClass.class, DatabaseModel.class );
        GroovyRegister.registerMetaClass( DynamicPropertyMetaClass.class, DynamicProperty.class );
        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetSupport.class );
        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetDecorator.class );

        GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, QRec.class );

        GroovyRegister.registerMetaClass( GDynamicPropertySetMetaClass.class, GDynamicPropertySetSupport.class );
    }

    private final DbService sqlService;
    private final SqlHelper sqlHelper;
    private final ColumnsHelper columnsHelper;
    private final Meta meta;

    @Inject
    public DatabaseModel(DbService sqlService, SqlHelper sqlHelper, ColumnsHelper columnsHelper,
                         Meta meta)
    {
        this.sqlService = sqlService;
        this.sqlHelper = sqlHelper;
        this.columnsHelper = columnsHelper;
        this.meta = meta;
    }

    @Override
    public <T> EntityModel<T> getEntity( String entityName )
    {
        Objects.requireNonNull(entityName);
        Entity entity = meta.getEntity(entityName);

        if (entity == null)throw Be5Exception.unknownEntity(entityName);

        return new EntityModelBase<>(sqlService, sqlHelper, columnsHelper, meta, entity);
    }
}
