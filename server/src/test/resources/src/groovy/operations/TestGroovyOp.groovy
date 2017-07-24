package src.groovy.operations

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport

class TestGroovyOp extends OperationSupport implements Operation
{

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps << [name: "name", DISPLAY_NAME: "Name"]

        dps << [
                name            : "number",
                DISPLAY_NAME    : "Number",
                value           : presetValues.get("number", "1"),
                TYPE            : Long,
                TAG_LIST_ATTR   : [['value', 'Label'], ['value2', 'Label 2']] as String[][],
                RELOAD_ON_CHANGE: true]

        return dps;
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        //String sql = generateSql( connector, false );
        //db.insert(sql);
    }

}
