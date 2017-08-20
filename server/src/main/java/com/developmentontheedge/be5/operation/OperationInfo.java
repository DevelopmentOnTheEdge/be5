package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.components.impl.model.ActionHelper;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.util.HashUrl;

/**
 * Immutable wrapper for {@link com.developmentontheedge.be5.metadata.model.Operation}
 */
public class OperationInfo
{
    private Operation operationModel; 
    
    public OperationInfo(Operation operationModel)
    {
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

    public String getUrl(OperationContext context){
        return ActionHelper.toAction(context.getQueryName(), operationModel).arg;
    }
}
