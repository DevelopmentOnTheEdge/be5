package com.developmentontheedge.be5.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PARAM;
import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PRESETS_PARAM;


public class FilterUtil
{
    public static Map<String, Object> getContextParams(Map<String, Object> operationParams)
    {
        if (!operationParams.containsKey(SEARCH_PARAM))
        {
            return operationParams.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().startsWith("_"))
                    .collect(Utils.toLinkedMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        if (operationParams.get(SEARCH_PRESETS_PARAM) == null)
        {
            return Collections.emptyMap();
        }

        List<String> contextParamNames = getSearchPresetParamList(operationParams.get(SEARCH_PRESETS_PARAM));

        return operationParams.entrySet()
                .stream()
                .filter(e -> !e.getKey().startsWith("_") && contextParamNames.contains(e.getKey()))
                .collect(Utils.toLinkedMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> getFilterParams(Map<String, Object> params)
    {
        if (!params.containsKey(SEARCH_PARAM))
        {
            return Collections.emptyMap();
        }

        List<String> contextParamNames = params.get(SEARCH_PRESETS_PARAM) == null ? Collections.emptyList() :
                getSearchPresetParamList(params.get(SEARCH_PRESETS_PARAM));

        return params.entrySet()
                .stream()
                .filter(e -> !e.getKey().startsWith("_") && !contextParamNames.contains(e.getKey()))
                .collect(Utils.toLinkedMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static String getSearchPresetParam(Map<String, Object> params)
    {
        return getSearchPresetParam(getSearchPresetNames(params));
    }

    public static String getSearchPresetParam(Collection<String> searchPresets)
    {
        return searchPresets.size() > 0 ? String.join(",", searchPresets) : null;
    }

    public static Collection<String> getSearchPresetNames(Map<String, Object> params)
    {
        if (!params.containsKey(SEARCH_PARAM))
        {
            return params.keySet();
        }
        else
        {
            if (params.get(SEARCH_PRESETS_PARAM) != null)
            {
                return getSearchPresetParamList(params.get(SEARCH_PRESETS_PARAM));
            }
            else
            {
                return Collections.emptyList();
            }
        }
    }

    private static List<String> getSearchPresetParamList(Object param)
    {
        if (param instanceof String) return Collections.singletonList((String) param);
        else return Arrays.asList((String[]) param);
    }
}
