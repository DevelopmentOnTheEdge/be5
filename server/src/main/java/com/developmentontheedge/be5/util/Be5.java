package com.developmentontheedge.be5.util;

import com.developmentontheedge.be5.api.services.impl.LogConfigurator;
import com.developmentontheedge.be5.env.Binder;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.Stage;
import com.developmentontheedge.be5.env.impl.Be5Injector;


public class Be5
{
    public static Injector createInjector(Stage stage, Binder binder)
    {
        Be5Injector injector = new Be5Injector(stage, binder);
        injector.get(LogConfigurator.class);
        return injector;
    }

}