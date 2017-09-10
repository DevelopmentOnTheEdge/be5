package com.developmentontheedge.be5.modules.core.genegate;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;

public class CoreEntityModels
{
    public CoreEntityModels(SqlService sqlService, DpsHelper dpsHelper, Meta meta, Validator validator)
    {
        users = new Users(sqlService, dpsHelper, validator, meta.getEntity("users"));
    }

    public Users users;
    //public UserRoles user_roles;

    public CoreEntityFields fields = new CoreEntityFields();
}
