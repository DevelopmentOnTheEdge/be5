package com.developmentontheedge.be5.env;

public enum Stage
{
    /**
     * We want fast startup times at the expense of runtime performance and some up front error
     * checking.
     */
    DEVELOPMENT,

    /** We want to catch errors as early as possible and take performance hits up front. */
    PRODUCTION
}