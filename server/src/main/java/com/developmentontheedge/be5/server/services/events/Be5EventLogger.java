/** $Id: BeanExplorerEventListener.java,v 1.9 2014/02/04 06:57:25 zha Exp $ */
package com.developmentontheedge.be5.server.services.events;


import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.metadata.model.Query;

import java.util.Map;

public interface Be5EventLogger
{
//    void operationDenied(int pageID, OperationInfo opInfo, String reason);
//    void operationCompleted(int pageID, OperationInfo opInfo);

    void queryCompleted(Query query, Map<String, Object> parameters, long estimatedTime);

    void queryError(Query query, Map<String, Object> parameters, Be5Exception e, long estimatedTime);

//    void servletStarted(ServletInfo si);
//    void servletDenied(ServletInfo si, String reason);
//    void servletCompleted(ServletInfo info);

    ///////////////////////////////////////////////////////////////////
    // methods for long processes and daemons
    //

    //void processStateChanged(ProcessInfo pi);
}
