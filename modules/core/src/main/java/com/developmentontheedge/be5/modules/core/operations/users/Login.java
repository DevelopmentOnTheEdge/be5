package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.api.services.CoreUtils;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.operation.GOperationSupport;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.beans.DynamicProperty;

import java.util.Arrays;
import java.util.Map;

import static com.developmentontheedge.beans.BeanInfoConstants.CAN_BE_NULL;
import static com.developmentontheedge.beans.BeanInfoConstants.PASSWORD_FIELD;


public class Login extends GOperationSupport
{
    @Inject protected LoginService loginService;
    @Inject protected CoreUtils coreUtils;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpForColumns(dps, getInfo().getEntity(), Arrays.asList("user_name", "user_pass"), presetValues);

        DynamicProperty user_pass = dps.getProperty("user_pass");
        user_pass.setAttribute(CAN_BE_NULL, false);
        user_pass.setAttribute(PASSWORD_FIELD, true);

        return dps;
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        if (loginService.login(request, dps.getValueAsString("user_name"), dps.getValueAsString("user_pass")))
        {
            postLogin(parameters);
            setResult(OperationResult.redirect(FrontendConstants.REFRESH_ALL));
        }
        else
        {
            setResult(OperationResult.error("Access denied"));
        }
    }

    public void postLogin( Object parameters )
    {

    }
}
