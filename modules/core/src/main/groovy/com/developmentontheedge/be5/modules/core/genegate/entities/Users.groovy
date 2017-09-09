package com.developmentontheedge.be5.modules.core.genegate.entities

import com.developmentontheedge.be5.api.helpers.DpsHelper
import com.developmentontheedge.be5.api.services.SqlService
import com.developmentontheedge.be5.api.validation.Validator
import com.developmentontheedge.be5.databasemodel.impl.EntityModelBase
import com.developmentontheedge.be5.metadata.model.Entity


class Users extends EntityModelBase
{
    class UsersModel
    {
        Map<String,String> map = new HashMap<>()

        void user_name(String user_name) {
            map.put("user_name", user_name)
        }

        void user_pass(String user_pass) {
            map.put("user_pass", user_pass)
        }

        void emailAddress(String emailAddress) {
            map.put("emailAddress", emailAddress)
        }

        void registrationDate(java.sql.Date registrationDate) {
            map.put("registrationDate", registrationDate.toString())
        }

        void attempt(Integer attempt) {
            map.put("attempt", attempt.toString())
        }

        void data(Object data) {
            map.put("data", data.toString())
        }
    }

    Users(SqlService db, DpsHelper dpsHelper, Validator validator, Entity entity)
    {
        super(db, dpsHelper, validator, entity)
    }

    String insert(@DelegatesTo(strategy = Closure.DELEGATE_ONLY,
            value = UsersModel.class) final Closure config)
    {
        UsersModel builder = new UsersModel()
        Closure code = config.rehydrate(builder, builder, builder)
        code.setResolveStrategy(Closure.DELEGATE_ONLY)
        code.call()

        return add(builder.map)
    }
}
