package src.groovy.operations

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport

class TestGroovyOp extends OperationSupport implements Operation
{

    @Override
    Object getParameters(Map<String, String> presetValues) throws Exception
    {
        dps << [name: "name", DISPLAY_NAME: "Name"]

        dps << [
                name            : "number",
                DISPLAY_NAME    : "Number",
                value           : 1,
                TYPE            : Long,
                TAG_LIST_ATTR   : ['A': 1, 'B': 2, 'C': 3, 'D': 4],
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
