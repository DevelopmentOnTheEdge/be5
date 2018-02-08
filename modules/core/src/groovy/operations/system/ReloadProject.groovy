package system

import com.developmentontheedge.be5.api.services.ProjectProvider
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationSupport


class ReloadProject extends OperationSupport
{
    @Inject ProjectProvider projectProvider

    @Override
    void invoke(Object parameters) throws Exception
    {
        projectProvider.reloadProject()
    }
}
