package src.groovy.operations

import com.developmentontheedge.be5.model.beans.DynamicPropertyGBuilder
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport


class TestAutocomplete extends OperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps << [
            name                   : "reasonMulti",
            DISPLAY_NAME           : "Множественный выбор",
            MULTIPLE_SELECTION_LIST: true,
            TAG_LIST_ATTR          : [["fired", "Уволен"], ["other", "Иная причина"]],
            value                  : ["vacation","sick"]
        ]

        def gBuilder = new DynamicPropertyGBuilder()

        dps << gBuilder.of{
            name                    "reasonMulti"
            DISPLAY_NAME            "Множественный выбор"
            MULTIPLE_SELECTION_LIST true
            TAG_LIST_ATTR           ([["fired", "Уволен"], ["other", "Иная причина"]])
            value                   (["vacation","sick"])
        }


        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
    }

}
