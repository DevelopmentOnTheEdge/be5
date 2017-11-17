package com.developmentontheedge.be5.entitygen.experimental.genegate;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.entitygen.experimental.genegate.repositories.ProvincesRepository;


public class CoreEntityModels
{
    public CoreEntityModels(SqlService sqlService, DpsHelper dpsHelper, Meta meta, OperationHelper operationHelper,
                            OperationService operationService, UserAwareMeta userAwareMeta, Validator validator)
    {
        provinces = new ProvincesRepository(sqlService, dpsHelper, validator, operationHelper,
                operationService, meta, userAwareMeta, meta.getEntity("provinces"));
    }

    public final ProvincesRepository provinces;

}
