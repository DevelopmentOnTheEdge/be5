package com.developmentontheedge.be5.databasemodel.groovy;

import java.util.Collections;
import java.util.Map;


public class GOperationModelBaseBuilder
{
    public String[] records = new String[]{ };
    public String entityName;
    public String queryName;
    public String operationName;
    public Map<String, ?> presetValues = Collections.emptyMap();
}
