package ${packageName};

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.databasemodel.impl.EntityModelBase;
import com.developmentontheedge.be5.metadata.model.Entity;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


public class ${entityClassName} extends EntityModelBase
{
    public ${entityClassName}(SqlService db, DpsHelper dpsHelper, Validator validator, OperationHelper operationHelper,
                    OperationService operationService, Meta meta, UserAwareMeta userAwareMeta, Entity entity)
    {
        super(db, dpsHelper, validator, operationHelper, operationService, meta, userAwareMeta, entity);
    }

    public String insert(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ${entityClassName}Model.class) final Closure config)
    {
        ${entityClassName}Model builder = new ${entityClassName}Model();

        config.setResolveStrategy( Closure.DELEGATE_FIRST );
        config.setDelegate( builder );
        config.call();

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
