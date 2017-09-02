package com.developmentontheedge.be5.modules.core.entity.genegate;

import com.developmentontheedge.be5.api.helpers.SqlHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.databasemodel.impl.EntityModelBase;

public class CoreEntityModels
{
    private final SqlService sqlService;
    private final SqlHelper sqlHelper;
    private final Meta meta;
    private final Validator validator;


    public CoreEntityModels(SqlService sqlService, SqlHelper sqlHelper, Meta meta, Validator validator)
    {
        this.sqlService = sqlService;
        this.sqlHelper = sqlHelper;
        this.meta = meta;
        this.validator = validator;

        users = getEntity("users");
        user_roles = getEntity("user_roles");
    }

    public EntityModel users;
    public EntityModel user_roles;

    private EntityModel getEntity(String entityName)
    {
        return new EntityModelBase(sqlService, sqlHelper, validator, meta.getEntity(entityName));
    }

}
