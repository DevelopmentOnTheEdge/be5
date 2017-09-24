package com.developmentontheedge.be5.modules.core.genegate;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.databasemodel.impl.EntityModelBase;
import com.developmentontheedge.be5.metadata.model.Entity;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class Users extends EntityModelBase
{
    public Users(SqlService db, DpsHelper dpsHelper, Validator validator, Entity entity)
    {
        super(db, dpsHelper, validator, entity);
    }

    public String insert(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = UsersModel.class) final Closure config)
    {
        UsersModel builder = new UsersModel();
        Closure code = config.rehydrate(builder, builder, builder);
        code.setResolveStrategy(Closure.DELEGATE_FIRST);
        code.call();

        return add(builder.getMap());
    }

    public String insert(Object owner, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = UsersModel.class) final Closure config)
    {
        UsersModel builder = new UsersModel();
        Closure code = config.rehydrate(builder, owner, owner);
        code.setResolveStrategy(Closure.DELEGATE_FIRST);
        code.call();

        return add(builder.getMap());
    }

    public class UsersModel
    {
        public void setUser_name(String user_name)
        {
            map.put("user_name",  user_name);
        }

        public void setUser_pass(String user_pass)
        {
            map.put("user_pass", user_pass);
        }

        public void setEmailAddress(String emailAddress)
        {
            map.put("emailAddress",  emailAddress);
        }

        public void setRegistrationDate(Date registrationDate)
        {
            map.put("registrationDate",  registrationDate);
        }

        public void setAttempt(Integer attempt)
        {
            map.put("attempt", attempt);
        }

        public void setData(Date data)
        {
            map.put("data",  data);
        }

        public Map<String, Object> getMap()
        {
            return map;
        }

        private Map<String, Object> map = new HashMap<>();
    }

}
