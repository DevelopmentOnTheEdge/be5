/** $Id: ParamHelper.java,v 1.6 2007/12/11 11:14:13 chernyh Exp $ */

package com.developmentontheedge.be5.components.impl.model;

import java.util.Hashtable;

/**
 * This interface describes helper for getting params passed by different
 * ways: by http request or manually formed set of params.
 * @author Andrey Chernyh
 * @since 26 Dec 2003
 */
public interface ParamHelper
{
    void remove(String name);
    String get(String name);
    String getStrict(String name);
    String[] getValues(String name);
    void put(String name, String value) throws Exception;
    void put(String name, String[] value) throws Exception;

    Hashtable getCompleteParamTable();
    Hashtable getNonStandardTable();
}
