package com.developmentontheedge.be5.entitygen.experimental.genegate;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.entitygen.experimental.genegate.repositories.ProvincesRepository;


public class CoreEntityModels
{
    public CoreEntityModels(SqlService sqlService, DpsHelper dpsHelper, Meta meta, OperationHelper operationHelper,
                            OperationExecutor operationExecutor, Validator validator)
    {
        provinces = new ProvincesRepository(sqlService, dpsHelper, validator, operationHelper,
                operationExecutor, meta, meta.getEntity("provinces"));
    }

    public final ProvincesRepository provinces;

}
