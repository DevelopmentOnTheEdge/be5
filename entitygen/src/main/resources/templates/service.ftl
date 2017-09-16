package com.developmentontheedge.be5.modules.core.generate;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.modules.core.generate.entities.*;


public class ${projectName}
{
    public ${projectName}(SqlService sqlService, DpsHelper dpsHelper, Meta meta, Validator validator)
    {
      <#list entityNames as entityName>
          ${entityName} = new ${entityName?cap_first}(sqlService, dpsHelper, validator, meta.getEntity("${entityName}"));
      </#list>
    }

  <#list entityNames as entityName>
    public final ${entityName?cap_first} ${entityName};
  </#list>

}
