package com.developmentontheedge.be5.modules.core.genegate

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationContext
import com.google.common.collect.ImmutableMap


class TestOperation extends CoreEntityFields.DocTypesFieldsOperationSupport implements CoreEntityFields.DocTypes2Fields
{
    @Inject CoreEntityModels entities

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps = sqlHelper.getDpsForColumns(getInfo().getEntity(), [CODE, Name])

        dps[CODE] << [
                RELOAD_ON_CHANGE, true,
                VALIDATION_RULES, [rules:"digits", range:[0,10]],
                MULTIPLE_SELECTION_LIST, true,
        ]

        dps[Name] << [CAN_BE_NULL, false]

        dps << [
                name, Name2,
                RELOAD_ON_CHANGE, true,
                VALIDATION_RULES, [unique:["docTypes"]],
        ]

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {

    }

    class Rules {
        static void rules(String... ruleNames){}
//        public final String unique = "unique"
//        public final String range = "range"
//        public final String digits = "digits"
    }
    //Rules rules = new Rules();
}
