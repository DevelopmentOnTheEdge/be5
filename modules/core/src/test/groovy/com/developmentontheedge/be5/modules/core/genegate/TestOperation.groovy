package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport


class TestOperation extends OperationSupport
{
    @Inject CoreEntityModels entities

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        entities.fields.docTypes.with {
            dps = sqlHelper.getDpsForColumns(getInfo().getEntity(), [CODE, Name])

            dps[CODE] << [ RELOAD_ON_CHANGE : true ]

            dps[Name] << [ CAN_BE_NULL : false ]
        }

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {

    }
}
