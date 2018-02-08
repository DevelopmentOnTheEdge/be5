package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.util.HashUrl;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Immutable wrapper for {@link com.developmentontheedge.be5.metadata.model.Operation}
 */
public class OperationInfo
{
    private Operation operationModel;
    private String queryName;//todo move to OperationContext

    public OperationInfo(String queryName, Operation operationModel)
    {
        this.queryName = queryName;
        this.operationModel = operationModel; 
    }

    // ////////////////////////////////////////////////////////////////////////
    // Properties
    //

    /** @PENDING */

    //public RoleSet getRoles()                       { return operationModel.getRoles();   }
    //public String getWellKnownName()                { return operationModel.getWellKnownName();   }
    //public String getNotSupported()                 { return operationModel.getNotSupported();  }
    //public Long getContextID()                      { return operationModel.getContextID(); }
    //public boolean isSecure()                       { return operationModel.isSecure(); }
    //public Icon getIcon()                           { return operationModel.getIcon();  }
    //public BeModelCollection<OperationExtender> getExtenders()

    public Operation getModel()                     { return operationModel;  }
    public String getName()                         { return operationModel.getName();  }
    public String getType()                         { return operationModel.getType();  }
    public String getCode()                         { return operationModel.getCode();  }
    public int getRecords()                         { return operationModel.getRecords();  }
    public String getVisibleWhen()                  { return operationModel.getVisibleWhen();  }
    public int getExecutionPriority()               { return operationModel.getExecutionPriority();  }
    public String getLogging()                      { return operationModel.getLogging();  }  
    public boolean isConfirm()                      { return operationModel.isConfirm();  }
    public Long getCategoryID()                     { return operationModel.getCategoryID(); }
    public Entity getEntity()                       { return operationModel.getEntity();  }

    public String getEntityName()
    {
        return operationModel.getEntity().getName();
    }

    public String getQueryName()
    {
        return queryName;
    }

    public OperationResult redirectThisOperation(String[] records, Map<String, String> redirectParams)
    {
        HashUrl hashUrl = new HashUrl(FrontendConstants.FORM_ACTION, getEntity().getName(), getQueryName(), getName())
                .named(redirectParams);
        if(records.length > 0)
        {
            hashUrl = hashUrl.named("selectedRows", Arrays.stream(records).collect(Collectors.joining(",")));
        }

        return OperationResult.redirect(hashUrl);
    }

    public OperationResult redirectThisOperationNewId(Object newID, Map<String, String> redirectParams)
    {
        HashUrl hashUrl = new HashUrl(FrontendConstants.FORM_ACTION, getEntity().getName(), getQueryName(), getName())
                .named(redirectParams)
                .named("selectedRows", newID.toString());

        return OperationResult.redirect(hashUrl);
    }

}
