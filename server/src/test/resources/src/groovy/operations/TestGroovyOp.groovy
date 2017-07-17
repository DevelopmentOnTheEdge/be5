package src.groovy.operations

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport

class TestGroovyOp extends OperationSupport implements Operation
{

    @Override
    Object getParameters(Map<String, String> presetValues) throws Exception
    {
        parameters << [name: "name", DISPLAY_NAME: "Name"]

        parameters << [
                name            : "number",
                DISPLAY_NAME    : "Number",
                value           : presetValues.get("number", "1"),
                TYPE            : Long,
                TAG_LIST_ATTR   : [['A', 'a'], ['B', 'b'], ['C', 'c'], ['D', 'd']] as String[][],
                RELOAD_ON_CHANGE: true]

        return parameters;
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        //String sql = generateSql( connector, false );
        //db.insert(sql);
    }

}
