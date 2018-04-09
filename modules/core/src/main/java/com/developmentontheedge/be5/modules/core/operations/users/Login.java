package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.CoreUtils;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.api.FrontendConstants;
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
    @Inject protected UserAwareMeta userAwareMeta;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpForColumns(dps, getInfo().getEntity(), Arrays.asList("user_name", "user_pass"), context.getOperationParams(), presetValues);

        DynamicProperty user_pass = dps.getProperty("user_pass");
        user_pass.setAttribute(CAN_BE_NULL, false);
        user_pass.setAttribute(PASSWORD_FIELD, true);

        return dps;
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        String username = dps.getValueAsString("user_name");
        if (loginService.loginCheck(username, dps.getValueAsString("user_pass")))
        {
            loginService.saveUser(username, request);
            postLogin(parameters);

            if(context.getOperationParams().get("withoutUpdateUserInfo") == null){
                setResult(OperationResult.finished(null, FrontendConstants.UPDATE_USER_INFO));
            }else{
                setResult(OperationResult.finished());
            }
        }
        else
        {
            setResult(OperationResult.error(userAwareMeta
                    .getLocalizedExceptionMessage("Incorrect username or password.")));
        }
    }

    public void postLogin( Object parameters )
    {

    }
}
