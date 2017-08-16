import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport


class SessionVariablesEdit extends OperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps << [
                name         : "label",
                value        : "Тип: " + request.getAttribute(records[0]).getClass().getName(),
                LABEL_FIELD  : true
        ]

        dps << [
                name         : "newValue",
                TYPE         : request.getAttribute(records[0]).getClass(),
                DISPLAY_NAME : "Новое значение:",
                DEFAULT_VALUE: request.getAttribute(records[0])
        ]

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception {
        request.setAttribute(records[0], dps.$newValue)
    }
}
