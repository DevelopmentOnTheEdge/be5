package com.developmentontheedge.be5.operation;

import java.util.Objects;

public class OperationContext
{
    // ////////////////////////////////////////////////////////////////////////
    // Properties
    //

    public final Long[] records;
    public final String queryName;

    public OperationContext(Long[] records, String queryName)
    {
        Objects.nonNull(records);
        Objects.nonNull(queryName);
        this.records = records;
        this.queryName = queryName;
    }

    public Long[] getRecordIDs()
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
