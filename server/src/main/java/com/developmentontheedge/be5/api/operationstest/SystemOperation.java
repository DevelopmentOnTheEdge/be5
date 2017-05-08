/* $Id: SystemOperation.java,v 1.5 2012/02/12 15:19:46 zha Exp $ */

package com.developmentontheedge.be5.api.operationstest;

import java.util.Map;

public interface SystemOperation
{
    boolean storeParamTableWithEmptyValues();

    Map getCompleteParamTable();

    void setCompleteParamTable(Map params);

    Object getAnyParam(String name);

    String getRequestUrl();

    void setRequestUrl(String url);
}
