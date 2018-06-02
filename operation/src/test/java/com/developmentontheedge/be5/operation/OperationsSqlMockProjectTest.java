package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.operation.services.OperationsFactory;
import com.developmentontheedge.be5.base.BaseModule;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.operation.test.ErrorProcessing;
import com.developmentontheedge.be5.test.BaseTestUtils;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;


public abstract class OperationsSqlMockProjectTest extends BaseTestUtils
{
    private static Injector injector;
    protected static OperationsFactory operations;

    @ClassRule
    public static TemporaryFolder tmp = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws Exception
    {
        initProjectWithOperation();

        injector = initInjector(
            Modules.override(new BaseModule()).with(new BaseDbMockTestModule()),
            new OperationModule()
        );

        operations = injector.getInstance(OperationsFactory.class);
    }

    @Override
    public Injector getInjector()
    {
        return injector;
    }

    private static void initProjectWithOperation() throws IOException
    {
        Path path = tmp.newFolder().toPath();
        Project prj = new Project("test");
        Entity entity = new Entity( "testEntity", prj.getApplication(), EntityType.TABLE );
        DataElementUtils.save( entity );
        com.developmentontheedge.be5.metadata.model.Operation op = com.developmentontheedge.be5.metadata.model.Operation.createOperation( "ErrorProcessing",
                com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_JAVA, entity );

        op.setCode(ErrorProcessing.class.getCanonicalName());
        DataElementUtils.save( op );

        try
        {
            Serialization.save( prj, path );
            ModuleLoader2.loadAllProjects(Collections.singletonList(path.resolve("project.yaml").toUri().toURL()));
        }
        catch (IOException | ProjectSaveException e1)
        {
            e1.printStackTrace();
        }
    }
}
