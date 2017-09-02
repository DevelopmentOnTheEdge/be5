package com.developmentontheedge.be5.modules.core.genegate.entities

import com.developmentontheedge.be5.api.helpers.SqlHelper
import com.developmentontheedge.be5.api.services.SqlService
import com.developmentontheedge.be5.api.validation.Validator
import com.developmentontheedge.be5.databasemodel.impl.EntityModelBase
import com.developmentontheedge.be5.metadata.model.Entity


class Users extends EntityModelBase
{
    class UsersModel extends HashMap<String,String>
    {
        void user_name(String user_name) {
            this.put("user_name", user_name)
        }

        void user_pass(String user_pass) {
            this.put("user_pass", user_pass)
        }

        void emailAddress(String emailAddress) {
            this.put("emailAddress", emailAddress)
        }

        void registrationDate(java.sql.Date registrationDate) {
            this.put("registrationDate", registrationDate.toString())
        }

        void attempt(Integer attempt) {
            this.put("attempt", attempt.toString())
        }

        void data(Object data) {
            this.put("data", data.toString())
        }
    }

    Users(SqlService db, SqlHelper sqlHelper, Validator validator, Entity entity)
    {
        super(db, sqlHelper, validator, entity)
    }

    String insert(@DelegatesTo(UsersModel.class) final Closure config)
    {
        final UsersModel model = new UsersModel()
        model.with config

        return add(model)
    }
}
