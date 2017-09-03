package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport


class TestOperation extends OperationSupport implements CoreEntityFields.DocTypesFields
{
    @Inject CoreEntityModels entities

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps = sqlHelper.getDpsForColumns(getInfo().getEntity(), [CODE, Name])

        dps[CODE] << [
                RELOAD_ON_CHANGE, true,
                VALIDATION_RULES, ["sf","dsf"],
                MULTIPLE_SELECTION_LIST, true,
        ]

        dps[Name] << [CAN_BE_NULL: false]

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {

    }
}
