package com.developmentontheedge.be5.env;


public class Be5
{

    public static Injector createInjector()
    {
        return new Be5Injector(new YamlBinder());
    }

    public static Injector createInjector(Binder binder)
    {
        return new Be5Injector(binder);
    }

}
