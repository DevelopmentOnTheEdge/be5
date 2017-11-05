package ${packageName};

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import ${packageName}.entities.*;


public class ${serviceClassName}
{
    public ${serviceClassName}(SqlService sqlService, DpsHelper dpsHelper, OperationHelper operationHelper,
                    UserAwareMeta userAwareMeta, Meta meta, Validator validator, OperationService operationService)
    {
      <#list entityNames as entityName>
          ${entityName} = new ${entityName?cap_first}(sqlService, dpsHelper, validator, operationHelper,
                                            operationService, meta, userAwareMeta, meta.getEntity("${entityName}"));
      </#list>
    }

  <#list entityNames as entityName>
    public final ${entityName?cap_first} ${entityName};
  </#list>

}
