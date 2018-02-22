package users

import com.developmentontheedge.be5.api.services.LoginService
import com.developmentontheedge.be5.components.FrontendConstants
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.GOperationSupport
import com.developmentontheedge.be5.operation.OperationResult


class Login extends GOperationSupport
{
    @Inject LoginService loginService

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpForColumns(dps, getInfo().getEntity(), ["user_name", "user_pass"], presetValues)

        dps.edit("user_pass") { CAN_BE_NULL = false; PASSWORD_FIELD = true }

        return dps
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        if (!loginService.login(request, dps.getValueAsString("user_name"), dps.getValueAsString("user_pass")))
        {
            setResult(OperationResult.error("Access denied"))
        }
        else
        {
            setResult(OperationResult.redirect(FrontendConstants.REFRESH_ALL))
        }
    }
}
