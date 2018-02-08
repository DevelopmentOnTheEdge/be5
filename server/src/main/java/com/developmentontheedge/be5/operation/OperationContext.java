package com.developmentontheedge.be5.operation;

import java.util.Map;
import java.util.Objects;


public class OperationContext
{
    // ////////////////////////////////////////////////////////////////////////
    // Properties
    //

    public final String[] records;
    public final String queryName;
    private final Map<String, Object> operationParams;

    public OperationContext(String[] records, String queryName, Map<String, Object> operationParams)
    {
        Objects.requireNonNull(records);
        Objects.requireNonNull(operationParams);

        this.records = records;
        this.queryName = queryName;
        this.operationParams = operationParams;
    }

    public String[] getRecords()
    {
        return records;
    }

    public String getQueryName()
    {
        return queryName;
    }

    public Map<String, Object> getOperationParams()
    {
        return operationParams;
    }

    //String platform, UserInfo ui, String[] records, String fromQuery, String category, String tcloneId

    
    /* AppInfo getAppInfo();
    interface SessionAdapter extends Serializable
    {
        Enumeration getVarNames();
        Object getVar( String name );
        void setVar( String name, Object value );
        void removeVar( String name );
        Map<String, Object> getVarsAsMap();
    }

    void setSessionAdapter( SessionAdapter sa );

    Object getSessionVar( String name );
    void setSessionVar( String name, Object value );

    void removeSessionVar( String name );

    boolean isDisableCancel();

    boolean isSplitParamOperation();

    boolean isTopLevel();
    void setTopLevel( boolean value );

    String getQueueID();
    void setQueueID( String ID );

    String getCrumbID();
    void setCrumbID( String ID );

*/    
}
