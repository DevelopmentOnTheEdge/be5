package com.developmentontheedge.be5.operation;

import java.util.Map;
import java.util.Objects;

public class OperationContext
{
    // ////////////////////////////////////////////////////////////////////////
    // Properties
    //

    private final String[] records;
    private final String queryName;
    //todo private final Map<String, String> queryParams;

    public OperationContext(String[] records, String queryName)
    {
        Objects.requireNonNull(records);
        Objects.requireNonNull(queryName);
        this.records = records;
        this.queryName = queryName;
    }

    public String[] getRecordIDs()
    {
        return records;
    }

    public String getQueryName()
    {
        return queryName;
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
