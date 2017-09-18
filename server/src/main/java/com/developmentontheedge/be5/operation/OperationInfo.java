package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.components.impl.model.ActionHelper;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.util.HashUrl;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Immutable wrapper for {@link com.developmentontheedge.be5.metadata.model.Operation}
 */
public class OperationInfo
{
    private Operation operationModel;
    private String queryName;

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

    public Operation getModel()                        { return operationModel;  }
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

    public String getQueryName()
    {
        return queryName;
    }

    public OperationResult redirectThisOperation(String[] records)
    {
        HashUrl hashUrl = new HashUrl(FrontendConstants.FORM_ACTION, getEntity().getName(), getQueryName(), getName());
        if(records.length > 0)
        {
            hashUrl = hashUrl.named("selectedRows", Arrays.stream(records).collect(Collectors.joining(",")));
        }

        return OperationResult.redirect(hashUrl.toString());
    }

}
