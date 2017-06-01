package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.FrontendAction;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.util.Either;

public interface OperationService
{
    Either<FormPresentation, FrontendAction> generate(Request req);

    FrontendAction execute(Request req);
}
