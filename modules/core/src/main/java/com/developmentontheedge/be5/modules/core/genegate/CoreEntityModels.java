package com.developmentontheedge.be5.modules.core.genegate;

import com.developmentontheedge.be5.api.helpers.SqlHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.modules.core.genegate.entities.Users;

public class CoreEntityModels
{
    public CoreEntityModels(SqlService sqlService, SqlHelper sqlHelper, Meta meta, Validator validator)
    {
        users = new Users(sqlService, sqlHelper, validator, meta.getEntity("users"));
    }

    public Users users;
    //public UserRoles user_roles;

    public CoreEntityFields fields = new CoreEntityFields();
}
