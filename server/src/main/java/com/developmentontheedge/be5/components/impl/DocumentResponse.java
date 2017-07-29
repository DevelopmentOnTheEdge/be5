package com.developmentontheedge.be5.components.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.TablePresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;

public class DocumentResponse
{
    
    public static DocumentResponse of(Response res)
    {
        return new DocumentResponse(res);
    }
    
    private final Response res;

    public DocumentResponse(Response res)
    {
        checkNotNull(res);
        this.res = res;
    }

    public void sendStaticPage(String content)
    {
        checkNotNull(content);
        res.sendAsJson(FrontendConstants.STATIC_ACTION, content);
    }

    public void send(TablePresentation tablePresentation)
    {
        checkNotNull(tablePresentation);
        res.sendAsJson(FrontendConstants.TABLE_ACTION, tablePresentation);
    }
    
    public void send(Either<FormPresentation, OperationResult> formOrResult)
    {
        checkNotNull(formOrResult);
        formOrResult.apply(this::send, this::send);
    }
    
    public void send(FormPresentation form)
    {
        checkNotNull(form);
        res.sendAsJson(FrontendConstants.FORM_ACTION, form);
    }

    public void send(OperationResult result)
    {
        checkNotNull(result);
        res.sendAsJson(FrontendConstants.OPERATION_RESULT, result);
    }

//    public void send(FormTable formTable)
//    {
//        checkNotNull(formTable);
//        res.sendAsJson("formTable", formTable);
//    }

    public void send(MoreRows moreRows)
    {
        checkNotNull(moreRows);
        res.sendAsRawJson(moreRows);
    }
    
}
