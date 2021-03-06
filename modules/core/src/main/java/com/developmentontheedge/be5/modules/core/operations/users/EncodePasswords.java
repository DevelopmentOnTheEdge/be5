package com.developmentontheedge.be5.modules.core.operations.users;

import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.databasemodel.util.DpsUtils;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.modules.core.services.impl.Pbkdf2PasswordEncoder;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.TransactionalOperation;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class EncodePasswords extends OperationSupport implements TransactionalOperation
{
    private DynamicPropertySet params = new DynamicPropertySetSupport();

    @Inject private LoginService loginService;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addLabel(params, "Used: " + loginService.getClass().getSimpleName());
        params.add(new DynamicProperty("input", "Please input 'encode password'", String.class));
        return DpsUtils.setValues(params, presetValues);
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        if (!params.getValueAsString("input").equals("encode password"))
        {
            setResult(OperationResult.error("Please input 'encode password'"));
        }
        List<RecordModel<String>> users = database.<String>getEntity("users").toList();
        Pbkdf2PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();
        for (RecordModel<String> user : users)
        {
            char[] user_pass = user.getValueAsString("user_pass").toCharArray();
            user.update(Collections.singletonMap("user_pass", passwordEncoder.encode(user_pass)));
        }
    }
}
