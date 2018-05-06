package system

import com.developmentontheedge.be5.api.services.ProjectProvider
import javax.inject.Inject
import com.developmentontheedge.be5.operation.support.OperationSupport


class ReloadProject extends OperationSupport
{
    @Inject ProjectProvider projectProvider

    @Override
    void invoke(Object parameters) throws Exception
    {
        projectProvider.reloadProject()
    }
}
