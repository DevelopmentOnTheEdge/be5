package com.developmentontheedge.be5.operation;

public class OperationContext
{
    // ////////////////////////////////////////////////////////////////////////
    // Properties
    //

    public final String[] records;

    public OperationContext(String[] records)
    {
        this.records = records;
    }

    public String[] getRecordIDs()
    {
        return records;
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
