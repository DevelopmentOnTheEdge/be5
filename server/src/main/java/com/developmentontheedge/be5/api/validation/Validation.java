package com.developmentontheedge.be5.api.validation;

import java.util.Arrays;
import java.util.List;

public interface Validation extends com.developmentontheedge.beans.Validation
{
    enum Status
    {
        SUCCESS, WARNING, ERROR;

        @Override
        public String toString()
        {
            return this.name().toLowerCase();
        }
    }

    /**
     * List of default validation rules. You can extend this list
     * in your app by simply adding new rules into it.
     */
    List<AbstractRule> defaultRules = Arrays.asList(
            //new RequiredRule(),
            new IntegerRule(),
            new NumberRule(),
            new EmailRule(),
            new UrlRule(),
            new DateRule()
    );
}
