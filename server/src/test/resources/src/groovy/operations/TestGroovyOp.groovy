package src.groovy.operations

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport
import com.developmentontheedge.beans.DynamicProperty

class TestGroovyOp extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
    {
        dps.add(new DynamicProperty("name", "Name", String.class,
                presetValues.getOrDefault("name", "")));

        dps.add(new DynamicProperty("number", "Number", Long.class,
                presetValues.getOrDefault("number", "0")));

        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        //String sql = generateSql( connector, false );
        //db.insert(sql);
    }

}
