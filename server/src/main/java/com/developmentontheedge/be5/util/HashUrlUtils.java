package com.developmentontheedge.be5.util;

import com.developmentontheedge.be5.api.FrontendConstants;
import com.developmentontheedge.be5.operation.Operation;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HashUrlUtils
{
    public static HashUrl getUrl(Operation operation)
    {
        HashUrl hashUrl = new HashUrl(FrontendConstants.FORM_ACTION,
                operation.getInfo().getEntityName(), operation.getContext().getQueryName(), operation.getInfo().getName())
                    .named(operation.getRedirectParams());

        if(operation.getContext().getRecords().length > 0)
        {
            hashUrl = hashUrl.named("selectedRows", Arrays.stream(operation.getContext().getRecords()).collect(Collectors.joining(",")));
        }

        return hashUrl;
    }
}
