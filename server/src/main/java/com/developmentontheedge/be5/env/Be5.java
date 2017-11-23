package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.env.impl.Be5Injector;


public class Be5
{
    public static Injector createInjector(Binder binder)
    {
        return new Be5Injector(binder);
    }

    public static Injector createInjector(Stage stage, Binder binder)
    {
        return new Be5Injector(stage, binder);
    }

}
