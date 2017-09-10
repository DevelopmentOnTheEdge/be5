package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport
import com.developmentontheedge.beans.DynamicPropertySet

import static com.developmentontheedge.be5.model.beans.DynamicPropertyGBuilder.add
import static com.developmentontheedge.be5.model.beans.DynamicPropertyGBuilder.edit
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

        edit(dps, registrationDate) {
            RELOAD_ON_CHANGE = true
            MULTIPLE_SELECTION_LIST = true
        }

        edit(dps, attempt) {CAN_BE_NULL = false}

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        //DynamicPropertySet.metaClass.$ = { String name -> delegate.getValue(name) }
        entities.users.insert{
            user_name        = dps.$user_name
            registrationDate = dps.$registrationDate
            attempt          = dps.$attempt
            user_pass        = "sdfsefr4r34"
        }
    }

}
