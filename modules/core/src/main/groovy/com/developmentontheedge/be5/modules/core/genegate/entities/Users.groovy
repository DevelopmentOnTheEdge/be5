package com.developmentontheedge.be5.modules.core.genegate.entities

import com.developmentontheedge.be5.api.helpers.SqlHelper
import com.developmentontheedge.be5.api.services.SqlService
import com.developmentontheedge.be5.api.validation.Validator
import com.developmentontheedge.be5.databasemodel.impl.EntityModelBase
import com.developmentontheedge.be5.metadata.model.Entity


class Users extends EntityModelBase
{
    class UsersModel
    {
        private String user_name
        private String user_pass
        private String emailAddress
        private Date registrationDate
        private int attempt
        private Object data

        void user_name(String user_name) {
            this.user_name = user_name
        }

        void user_pass(String user_pass) {
            this.user_pass = user_pass
        }

        void emailAddress(String emailAddress) {
            this.emailAddress = emailAddress
        }

        void registrationDate(Date registrationDate) {
            this.registrationDate = registrationDate
        }

        void attempt(int attempt) {
            this.attempt = attempt
        }

        void data(Object data) {
            this.data = data
        }
    }

    Users(SqlService db, SqlHelper sqlHelper, Validator validator, Entity entity) {
        super(db, sqlHelper, validator, entity)
    }

    String insert(@DelegatesTo(UsersModel.class) final Closure config) {
        final UsersModel model = new UsersModel()
        model.with config
        
        return "123"
        //return super.a
    }
}
