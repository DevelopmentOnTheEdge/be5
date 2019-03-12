package com.developmentontheedge.be5.operation.services;

import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.GroovyOperation;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.operation.OperationsSqlMockProjectTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class OperationServiceExtendTest extends OperationsSqlMockProjectTest
{
    @Inject private UserAwareMeta userAwareMeta;
    @Inject private GroovyOperationLoader groovyOperationLoader;
    @Inject private ProjectProvider projectProvider;

    @Before
    public void setUp()
    {
        setStaticUserInfo(RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @Test
    public void getSuperOperationSimpleNameTest()
    {
        Operation operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend");

        assertEquals("CustomOperation", groovyOperationLoader.getSimpleSuperClassName((GroovyOperation) operation));

        operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend2");

        assertEquals("OperationWithExtend", groovyOperationLoader.getSimpleSuperClassName((GroovyOperation) operation));
    }

    @Test
    public void getSuperOperationCanonicalNameTest()
    {
        Operation operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend");
        assertEquals("testtableAdmin.CustomOperation.groovy",
                groovyOperationLoader.getCanonicalSuperClassName((GroovyOperation) operation));

        operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend2");
        assertEquals("testtableAdmin.OperationWithExtend.groovy",
                groovyOperationLoader.getCanonicalSuperClassName((GroovyOperation) operation));
    }

    @Test
    public void testLoadSuperOperation()
    {
        projectProvider.reloadProject();

        Operation operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend");
        assertEquals(new ArrayList<String>(Arrays.asList("testtableAdmin.CustomOperation.groovy")),
                groovyOperationLoader.preloadSuperOperation((GroovyOperation) operation));
    }

}
