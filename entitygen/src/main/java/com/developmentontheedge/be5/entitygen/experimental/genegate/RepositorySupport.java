package com.developmentontheedge.be5.entitygen.experimental.genegate;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.databasemodel.impl.EntityModelBase;
import com.developmentontheedge.be5.entitygen.experimental.genegate.fields.ProvincesFields;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.sql.format.Ast;
import com.developmentontheedge.sql.model.AstSelect;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.io.Serializable;
import java.util.Collections;


public abstract class RepositorySupport<T, ID extends Serializable> implements Repository<T, ID>
{
    public BeanHandler<T> beanHandler;
    public BeanListHandler<T> beanListHandler;

    protected final EntityModelBase entityModelBase;
    protected final DpsHelper dpsHelper;
    protected final OperationHelper operationHelper;
    protected final SqlService db;

    public final Entity entity;
    protected final String entityName;

    public static ProvincesFields fields;
    public String primaryKeyName;

    public RepositorySupport(SqlService db, DpsHelper dpsHelper, Validator validator, OperationHelper operationHelper,
                             OperationService operationService, Meta meta, UserAwareMeta userAwareMeta, Entity entity)
    {
        entityModelBase = new EntityModelBase(db, dpsHelper, validator, operationHelper,
                operationService, meta, userAwareMeta, entity);
        this.entity = entityModelBase.getEntity();
        this.entityName = entity.getName();

        this.db = db;
        this.dpsHelper = dpsHelper;
        this.operationHelper = operationHelper;
    }

    @Override
    public Long count()
    {
        return db.getLong(Ast.selectCount().from(entityName).format());
    }

    @Override
    public void remove(ID primaryKey)
    {
        entityModelBase.remove(primaryKey.toString());
    }

    @Override
    public void removeAll()
    {
        db.update(dpsHelper.generateDelete(entity, Collections.emptyMap()));
    }

    @Override
    public boolean exists(ID primaryKey)
    {
        return findOne(primaryKey) != null;
    }

    @Override
    public T findOne(ID primaryKey)
    {
        AstSelect sql = Ast.selectAll().from(entityName).where(primaryKeyName, primaryKey);

        return db.query(sql.format(), beanHandler, primaryKey);
    }

    @Override
    public Iterable<T> findAll()
    {
        AstSelect sql = Ast.selectAll().from(entityName);

        return db.query(sql.format(), beanListHandler);
    }

}
