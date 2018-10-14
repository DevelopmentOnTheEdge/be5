package com.developmentontheedge.be5.base.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PARAM;
import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PRESETS_PARAM;


public class FilterUtil
{
    public static Map<String, Object> getOperationParamsWithoutFilter(Map<String, Object> operationParams)
    {
        if (!operationParams.containsKey(SEARCH_PARAM))
        {
            return operationParams;
        }

        if (operationParams.get(SEARCH_PRESETS_PARAM) == null)
        {
            return Collections.emptyMap();
        }

        List<String> contextParamNames = Arrays.asList(((String) operationParams.get(SEARCH_PRESETS_PARAM)).split(","));

        return operationParams.entrySet()
                .stream()
                .filter(e -> contextParamNames.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> getFilterParams(Map<String, Object> params)
    {
        if (!params.containsKey(SEARCH_PARAM))
        {
            return Collections.emptyMap();
        }

        if (params.get(SEARCH_PRESETS_PARAM) == null)
        {
            return params.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().startsWith("_"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        List<String> contextParamNames = Arrays.asList(((String) params.get(SEARCH_PRESETS_PARAM)).split(","));

        return params.entrySet()
                .stream()
                .filter(e -> !e.getKey().startsWith("_"))
                .filter(e -> !contextParamNames.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
                return Arrays.asList(((String) params.get(SEARCH_PRESETS_PARAM)).split(","));
            }
            else
            {
                return Collections.emptyList();
            }
        }
    }
}
