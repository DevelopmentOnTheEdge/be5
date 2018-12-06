package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.operation.model.TransactionalOperation;
import com.developmentontheedge.be5.server.operations.support.GOperationSupport;
import com.developmentontheedge.beans.DynamicPropertyBuilder;
import com.google.inject.Stage;

import javax.inject.Inject;
import java.util.Map;

import static com.developmentontheedge.be5.databasemodel.util.DpsUtils.setValues;
import static com.developmentontheedge.be5.operation.services.validation.ValidationRules.pattern;
import static com.developmentontheedge.beans.BeanInfoConstants.PASSWORD_FIELD;
import static com.developmentontheedge.beans.BeanInfoConstants.VALIDATION_RULES;


public class ChangePassword extends GOperationSupport implements TransactionalOperation
{
    @Inject private LoginService loginService;
    @Inject private Stage stage;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        params.add(new DynamicPropertyBuilder("user_pass", String.class)
                .title(userAwareMeta.getLocalizedOperationField(getInfo().getEntityName(), getInfo().getName(), "Current password"))
                .attr(PASSWORD_FIELD, true).get());

        params.add(new DynamicPropertyBuilder("new_user_pass", String.class)
                .title(userAwareMeta.getLocalizedOperationField(getInfo().getEntityName(), getInfo().getName(), "New password"))
                .attr(PASSWORD_FIELD, true).get());

        params.add(new DynamicPropertyBuilder("new_user_pass2", String.class)
                .title(userAwareMeta.getLocalizedOperationField(getInfo().getEntityName(), getInfo().getName(), "Confirm new password"))
                .attr(PASSWORD_FIELD, true).get());

        if (stage == Stage.PRODUCTION)
        {
            params.getAsBuilder("new_user_pass").attr(VALIDATION_RULES, pattern(".{6}", userAwareMeta
                    .getLocalizedExceptionMessage("The minimum password length is $1 characters")
                    .replace("$1", "6")));
        }


        return setValues(params, presetValues);
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        if (!loginService.loginCheck(userInfo.getUserName(), params.getValueAsString("user_pass").toCharArray()))
        {
            validator.setError(params.getProperty("user_pass"), userAwareMeta
                    .getLocalizedExceptionMessage("Current password not correct"));
            return;
        }
        if (!params.getValueAsString("new_user_pass").equals(params.getValueAsString("new_user_pass2")))
        {
            validator.setError(params.getProperty("new_user_pass2"), userAwareMeta
                    .getLocalizedExceptionMessage("New passwords not equals"));
            return;
        }

        String newPass = loginService.finalPassword(params.getValueAsString("new_user_pass").toCharArray());
        db.update("UPDATE users SET user_pass = ? WHERE user_name = ?", newPass, userInfo.getUserName());
        setResultFinished(userAwareMeta.getLocalizedExceptionMessage("Password has been changed"));
    }
}
