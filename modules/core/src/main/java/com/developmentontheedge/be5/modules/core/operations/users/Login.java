package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.server.FrontendActions;
import com.developmentontheedge.be5.server.services.users.UserService;
import com.developmentontheedge.be5.server.operations.support.GOperationSupport;
import com.developmentontheedge.be5.server.services.users.UserInfoModelService;
import com.developmentontheedge.beans.DPBuilder;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;

import static com.developmentontheedge.beans.BeanInfoConstants.PASSWORD_FIELD;


public class Login extends GOperationSupport
{
    @Inject
    protected LoginService loginService;
    @Inject
    protected UserService userHelper;
    @Inject
    protected UserInfoModelService userInfoModelService;
    @Inject
    protected CoreUtils coreUtils;
    @Inject
    protected UserAwareMeta userAwareMeta;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpForColumns(params, getInfo().getEntity(), Arrays.asList("user_name", "user_pass"),
                context.getParams(), presetValues);

        new DPBuilder(params.getProperty("user_pass")) {{
            nullable = false;
            attr(PASSWORD_FIELD, true);
        }}.build();

        String rememberMeTitle = userAwareMeta.getColumnTitle("users", "Remember me");
        params.add(new DPBuilder("rememberMe", rememberMeTitle) {{
            type = Boolean.class;
            nullable = true;
            value = presetValues.get("rememberMe");
        }}.build());

        return params;
    }

    @Override
    public void invoke(Object parameters)
    {
        String username = params.getValueAsString("user_name");
        boolean rememberMe = params.getValue("rememberMe") != null;

        if (loginService.loginCheck(username, params.getValueAsString("user_pass").toCharArray()))
        {
            userHelper.saveUser(username, rememberMe);
            if (context.getParams().get("withoutUpdateUserInfo") == null)
            {
                setResultFinished(FrontendActions.updateUserAndOpenDefaultRoute(userInfoModelService.getUserInfoModel()));
            }
            else
            {
                setResultFinished();
            }
        }
        else
        {
            setResult(OperationResult.error(userAwareMeta
                    .getLocalizedExceptionMessage("Incorrect username or password.")));
        }
    }

}
