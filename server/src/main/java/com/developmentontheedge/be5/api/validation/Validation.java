package com.developmentontheedge.be5.api.validation;

import java.util.Arrays;
import java.util.List;

public interface Validation extends com.developmentontheedge.beans.Validation
{
    /**
     * List of default validation rules. You can extend this list
     * in your app by simply adding new rules into it.
     */
    List<AbstractRule> defaultRules = Arrays.asList(
            new RequiredRule(),
            new IntegerRule(),
            new NumberRule(),
            new EmailRule(),
            new UrlRule(),
            new DateRule()
    );
}
