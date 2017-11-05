package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.env.impl.Be5Injector;
import com.developmentontheedge.be5.env.impl.YamlBinder;


public class Be5
{

    public static Injector createInjector()
    {
        return new Be5Injector(new YamlBinder());
    }

    public static Injector createInjector(Binder binder)
    {
        return new Be5Injector(Stage.DEVELOPMENT, binder);
    }

    public static Injector createInjector(Stage stage, Binder binder)
    {
        return new Be5Injector(stage, binder);
    }

}
