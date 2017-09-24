package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.modules.core.genegate.fields.ProvincesFields as p
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport

//import static com.developmentontheedge.be5.modules.core.genegate.CoreEntityFields.UsersFields.*


class TestOperation extends OperationSupport
{
    @Inject CoreEntityModels entities

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps = dpsHelper.getDpsForColumns(getInfo().getEntity(), [p.name, p.countryID])

        add(dps) {
            name             = p.name
            DISPLAY_NAME     = "Test"
            RELOAD_ON_CHANGE = true
            MULTIPLE_SELECTION_LIST = true
            READ_ONLY        = true
        }

        edit(dps, p.countryID) {
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
