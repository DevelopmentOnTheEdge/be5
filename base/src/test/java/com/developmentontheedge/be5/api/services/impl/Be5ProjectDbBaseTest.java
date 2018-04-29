package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.SqlService;
import java.util.logging.LogManager;


public abstract class Be5ProjectDbBaseTest
{
    protected SqlService db;


    static {
        LogManager.getLogManager().reset();
    }
//    private static final Injector injector = new Be5Injector(Stage.TEST, new YamlBinder());

//    @Before
//    public void setUpBe5ProjectDBTest()
//    {
//        injector.injectAnnotatedFields(this);
//    }

    static
    {
        //Project project = injector.get(ProjectProvider.class).getProject();

//        try
//        {
//            new AppDb()
//                    .setLogger(new JULLogger(Logger.getLogger(AppDb.class.getName())))
//                    .setBe5Project(project)
//                    .execute();
//        }
//        catch (MojoFailureException e)
//        {
//            throw new RuntimeException(e);
//        }
    }

}
