package com.developmentontheedge.be5.model.beans

import com.developmentontheedge.be5.operation.GOperationSupport
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import groovy.transform.TypeChecked


class TestOperation extends GOperationSupport implements Operation
{

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps.add {
            name      = "test1"
            TYPE      = Integer
            READ_ONLY = true
            value     = 12
        }

        dps.add("test2", "Test Value") {
            MULTIPLE_SELECTION_LIST = true
            TAG_LIST_ATTR = helper.getTagsFromSelectionView(request, "asdasd")
        }

        dps.edit("test1") { value = 123}

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        database.testtableAdmin << [
            "test1": dps["test1"],
            "test2": dps["test2"]
        ]
    }

}
