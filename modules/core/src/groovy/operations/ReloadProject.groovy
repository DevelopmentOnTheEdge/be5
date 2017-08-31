import com.developmentontheedge.be5.api.services.ProjectProvider
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport


class ReloadProject extends OperationSupport implements Operation
{
    @Inject ProjectProvider projectProvider

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception {
        projectProvider.reloadProject()
    }
}
