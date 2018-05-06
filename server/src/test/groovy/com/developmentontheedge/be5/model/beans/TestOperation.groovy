package com.developmentontheedge.be5.model.beans

import com.developmentontheedge.be5.operation.support.GOperationSupport
import com.developmentontheedge.be5.operation.Operation

class TestOperation extends GOperationSupport implements Operation
{

    @Override
    Object getParameters(Map<String, Object> presetValues)
    {
        dps.add {
            name      = "test1"
            TYPE      = Integer
            READ_ONLY = true
            value     = 12
        }

        dps.add("test2", "Test Value") {
            MULTIPLE_SELECTION_LIST = true
            TAG_LIST_ATTR = helper.getTagsFromSelectionView("asdasd")
        }

        dps.edit("test1") { value = 123 }

        return dps
    }

    @Override
    void invoke(Object parameters)
    {
        database.testtableAdmin << [
            "test1": dps["test1"],
            "test2": dps["test2"]
        ]

        database.testtableAdmin << [
                "test1": dps.$test1,
                "test2": dps.$test2
        ]
    }

}
