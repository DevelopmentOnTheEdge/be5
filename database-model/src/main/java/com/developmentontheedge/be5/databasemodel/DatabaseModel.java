package com.developmentontheedge.be5.databasemodel;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.groovy.DatabaseModelImplMetaClass;
import com.developmentontheedge.be5.databasemodel.helpers.ColumnsHelper;
import com.developmentontheedge.be5.databasemodel.helpers.SqlHelper;
import com.developmentontheedge.be5.databasemodel.impl.EntityModelBase;
import com.developmentontheedge.be5.groovy.meta.GroovyRegister;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.model.Entity;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Database model based on concept of object-oriented database system
 *
 * @author ruslan
 */
public final class DatabaseModel implements EntityAccess
{
    static
    {
        GroovyRegister.registerMetaClass(DatabaseModelImplMetaClass.class, DatabaseModel.class);
    }

    private final DbService db;
    private final SqlHelper sqlHelper;
    private final ColumnsHelper columnsHelper;
    private final Meta meta;

    @Inject
    public DatabaseModel(DbService db, SqlHelper sqlHelper, ColumnsHelper columnsHelper,
                         Meta meta)
    {
        this.db = db;
        this.sqlHelper = sqlHelper;
        this.columnsHelper = columnsHelper;
        this.meta = meta;
    }

    @Override
    public <T> EntityModel<T> getEntity(String entityName)
    {
        Objects.requireNonNull(entityName);
        Entity entity = meta.getEntity(entityName);

        return new EntityModelBase<>(db, sqlHelper, columnsHelper, meta, entity);
    }

    public <T> EntityModel<T> getAt(String entityName)
    {
        return getEntity(entityName);
    }
}
