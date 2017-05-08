/** $Id: Operation.java,v 1.70 2014/02/24 03:56:27 dimka Exp $ */

package com.developmentontheedge.be5.api.operationstest;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.beans.BeanInfoConstants;

import java.io.Serializable;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Map;

public interface Be5Operation extends Serializable
{
    enum Status
    {
        PENDING( "pending" ),
        SCHEDULED( "scheduled" ),
        IN_PROGRESS( "in progress" ),
        FINISHED( "finished" ),
        ERROR( "error" ),
        PAUSED( "paused" ),
        CANCELLED( "cancelled" ),
        INTERRUPTING( "interrupting" ),
        ABORTED( "aborted" ),
        STOPPED( "stopped" ),
        LOCKED( "locked" ),
        DELAYED( "delayed" ),
        TEMPLATE( "template" );

        private final String value;

        private Status( String value )
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static Status parse( String str )
        {
            for( Status s : values() )
            {
                if( s.value.equals( str ) )
                    return s;
            }
            return null;
        }
    }

    String TAG_LIST_ATTR = WebFormPropertyInspector.TAG_LIST_ATTR;
    String EXTERNAL_TAG_LIST = WebFormPropertyInspector.EXTERNAL_TAG_LIST;
    String DYNAMIC_TAG_LIST_ATTR = WebFormPropertyInspector.DYNAMIC_TAG_LIST_ATTR;
    String MULTIPLE_SELECTION_LIST = WebFormPropertyInspector.MULTIPLE_SELECTION_LIST;
    String INPUT_SIZE_ATTR  = WebFormPropertyInspector.INPUT_SIZE_ATTR;
    String COLUMN_SIZE_ATTR = WebFormPropertyInspector.COLUMN_SIZE_ATTR;
    String PASSWORD_FIELD = WebFormPropertyInspector.PASSWORD_FIELD;
    String RICH_TEXT = WebFormPropertyInspector.RICH_TEXT;
    String COLOR_PICKER = WebFormPropertyInspector.COLOR_PICKER;
    String RELOAD_ON_CHANGE = BeanInfoConstants.RELOAD_ON_CHANGE;
    String CUSTOM_INPUT_TYPE_ATTR = BeanInfoConstants.CUSTOM_INPUT_TYPE_ATTR;

    String CATEGORY_ATTRIBUTE_ID = "category-attribute-id";

    String TABLE_REF = "table-ref";

    String CONDITIONALLY_NOT_NULL = "conditionally-not-null";

    String ORIG_PROPERTY_NAME_ATTR = BeanInfoConstants.ORIG_PROPERTY_NAME_ATTR;
    String ORIG_PROPERTY_ENTITY_ATTR = BeanInfoConstants.ORIG_PROPERTY_ENTITY_ATTR;

    void initialize(String platform, UserInfo ui, String entity, String primaryKey, String[] records, String fromQuery, String category, String tcloneId);

    String[] getRecordIDs();

    Object getParameters(Writer out, DatabaseService connector, Map presetValues) throws Exception;
    Object getStoredParameters();
    //Operation []getCollectionOperations();
    //Operation []getOwnerOperations();

    //Map getCustomLocalization(DatabaseService connector) throws Exception;

    void invoke(Writer out, DatabaseService connector) throws Exception;

    //UserInfo getUserInfo();

    String getName();
    void setName(String name);

    String getOrigName();
    void setOrigName(String name);

//    String getFromQuery();
//
//    String getOperLogId();
//    void setOperLogId(String name);

    String getEntity();

    //String getTcloneId();

    //ApplicationInfoComponent.ApplicationInfo getAppInfo();
    //void setAppInfo(ApplicationInfoComponent.ApplicationInfo ai);

    //DynamicPropertySet[] getRecords(DatabaseService connector)throws Exception;

    interface SessionAdapter extends Serializable
    {
        Enumeration getVarNames();
        Object getVar(String name);
        void setVar(String name, Object value);
        void removeVar(String name);
        Map<String, Object> getVarsAsMap();
    }
//
//    interface InterruptMonitor
//    {
//        boolean isInterrupted();
//        void setInterrupted(boolean interrupted);
//    }
//

    void setSessionAdapter(SessionAdapter sa);

    Object getSessionVar(String name);
    void setSessionVar(String name, Object value);

    void removeSessionVar(String name);
//
//    boolean isDisableCancel();
//
//    boolean isSplitParamOperation();
//
//    boolean isTopLevel();
//    void setTopLevel(boolean value);
//
//    String getQueueID();
//    void setQueueID(String ID);
//
//    String getCrumbID();
//    void setCrumbID(String ID);
//
////    List<OperationExtender> getExtenders();
////    void setExtenders(List<OperationExtender> extenders);
//
//    InterruptMonitor getInterruptMonitor();
//    void setInterruptMonitor(InterruptMonitor interruptChecker);
//
//    String getDescription(DatabaseService connector);

    Status getResult();
    
//    boolean saveOperLogParamsImmeditely();
//
//    void cleanUp();
}
