package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.api.helpers.UserHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.junit.Test;

import static org.junit.Assert.*;

public class ServerModuleTest
{
    @Test
    public void configure()
    {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new ServerModule());
        injector.getInstance(UserHelper.class);
    }
}