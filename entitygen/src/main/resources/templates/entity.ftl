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

public class ${entityClassName} extends EntityModelBase
{
    public ${entityClassName}(SqlService db, DpsHelper dpsHelper, Validator validator, Entity entity)
    {
        super(db, dpsHelper, validator, entity);
    }

    public String insert(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ${entityClassName}Model.class) final Closure config)
    {
        ${entityClassName}Model builder = new ${entityClassName}Model();
        Closure code = config.rehydrate(builder, builder, builder);
        code.setResolveStrategy(Closure.DELEGATE_FIRST);
        code.call();

        return add(builder.getMap());
    }

    public class ${entityClassName}Model
    {
      <#list columns as column>
        public void set${column.name?cap_first}(${column.type} ${column.name})
        {
            map.put("${column.name}",  ${column.name});
        }
      </#list>

        public Map<String, Object> getMap()
        {
            return map;
        }

        private Map<String, Object> map = new HashMap<>();
    }

}
