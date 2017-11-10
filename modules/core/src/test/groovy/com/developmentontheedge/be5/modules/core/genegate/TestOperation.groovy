package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.GOperationSupport
import com.developmentontheedge.be5.operation.OperationContext


class TestOperation extends GOperationSupport
{
    @Inject CoreEntityModels entities

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps = dpsHelper.addDpForColumns(getInfo().getEntity(), ["name", "countryID"])

        dps.add("name", "Test") {
            RELOAD_ON_CHANGE = true
            MULTIPLE_SELECTION_LIST = true
            READ_ONLY        = true
        }

        dps.edit("countryID") {
            RELOAD_ON_CHANGE = true
            MULTIPLE_SELECTION_LIST = true
        }

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        //DynamicPropertySet.metaClass.$ = { String name -> delegate.getValue(name) }
        entities.provinces.add {
            name      = dps.$name
            countryID = dps.$countryID
        }
    }

}
