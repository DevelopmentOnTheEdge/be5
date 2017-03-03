package com.developmentontheedge.be5.metadata.model.editors;

import java.util.Set;

import com.developmentontheedge.be5.metadata.util.ModuleUtils;
import com.developmentontheedge.beans.editors.GenericMultiSelectEditor;

public class FeaturesSelector extends GenericMultiSelectEditor
{
    private static String[] features;
    
    @Override
    protected Object[] getAvailableValues()
    {
        return getFeatures();
    }

    public static String[] getFeatures()
    {
        if(features == null)
        {
            Set<String> featuresSet = ModuleUtils.getAvailableFeatures();
            features = featuresSet.toArray( new String[featuresSet.size()] );
        }
        return features.clone();
    }
}
