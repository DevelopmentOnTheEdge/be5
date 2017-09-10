package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport

import static com.developmentontheedge.be5.model.beans.DynamicPropertyGBuilder.*
import static com.developmentontheedge.be5.modules.core.genegate.CoreEntityFields.UsersFields.*


class TestOperation extends OperationSupport
{
    @Inject CoreEntityModels entities

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps = dpsHelper.getDpsForColumns(getInfo().getEntity(), [user_name, user_pass, attempt, registrationDate])

        add(dps) {
            name             = user_name
            DISPLAY_NAME     = "Test"
            RELOAD_ON_CHANGE = true
            MULTIPLE_SELECTION_LIST = true
            READ_ONLY        = true
        }

        dps[registrationDate] << [
                RELOAD_ON_CHANGE: true,
                MULTIPLE_SELECTION_LIST: true,
        ]

        dps[attempt] << [CAN_BE_NULL: false]

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        entities.users.insert{
            user_name        = dps.$user_name
            registrationDate = dps.$registrationDate
            attempt          = dps.$attempt
            user_pass        = "sdfsefr4r34"
        }
    }

}
