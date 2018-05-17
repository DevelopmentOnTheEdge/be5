package src.groovy.operations.testTags

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.operations.SilentInsertOperation
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport


class OperationWithCanBeNull extends SilentInsertOperation implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        dps << [
                name       : "CODE",
                value      : presetValues.get("CODE")
        ]

        dps << [
                name       : "referenceTest",
                value      : presetValues.get("referenceTest"),
                CAN_BE_NULL: true
        ]

        dps << [
                name       : "testLong",
                TYPE       : Long,
                value      : presetValues.get("testLong"),
                CAN_BE_NULL: true
        ]

        return dps
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        setResult(OperationResult.finished(((DynamicPropertySet)parameters).getValueAsString("referenceTest")))
    }

}
