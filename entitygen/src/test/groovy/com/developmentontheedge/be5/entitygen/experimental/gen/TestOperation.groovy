package com.developmentontheedge.be5.entitygen.experimental.gen

import com.developmentontheedge.be5.entitygen.experimental.genegate.CoreEntityModels
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.GOperationSupport


class TestOperation extends GOperationSupport
{
    @Inject CoreEntityModels entities

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps = dpsHelper.addDpForColumns(dps, getInfo().getEntity(), ["name", "countryID"], context.getOperationParams())

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
    void invoke(Object parameters) throws Exception
    {
        //DynamicPropertySet.metaClass.$ = { String name -> delegate.getValue(name) }
        entities.provinces.add {
            name      = dps.$name
            countryID = dps.$countryID
        }
    }

}
