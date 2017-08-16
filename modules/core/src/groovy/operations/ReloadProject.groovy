import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport


class ReloadProject extends OperationSupport implements Operation
{
    @Override
    void invoke(Object parameters, OperationContext context) throws Exception {
        injector.getProjectProvider().reloadProject()
    }
}
