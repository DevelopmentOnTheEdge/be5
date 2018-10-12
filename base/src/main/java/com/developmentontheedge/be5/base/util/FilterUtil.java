package com.developmentontheedge.be5.base.util;

import java.util.ArrayList;
import java.util.Arrays;
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

        List<String> notFilterParams = Arrays.asList(((String) operationParams.get(SEARCH_PRESETS_PARAM)).split(","));

        return operationParams.entrySet()
                .stream()
                .filter(e -> notFilterParams.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static List<String> getSearchPresetNames(Map<String, Object> params)
    {
        if (!params.containsKey(SEARCH_PARAM))
        {
            return params.entrySet()
                    .stream()
                    .filter(x -> x.getValue() != null)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
        else
        {
            if (params.get(SEARCH_PRESETS_PARAM) != null)
            {
                return Arrays.asList(((String) params.get(SEARCH_PRESETS_PARAM)).split(","));
            }
            else
            {
                return new ArrayList<>();
            }
        }
    }
}
