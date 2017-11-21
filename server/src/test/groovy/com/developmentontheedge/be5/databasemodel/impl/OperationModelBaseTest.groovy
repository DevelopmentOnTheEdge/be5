package com.developmentontheedge.be5.databasemodel.impl

import com.developmentontheedge.be5.api.services.OperationExecutor
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.Be5ProjectTest
import org.junit.Test

import static org.junit.Assert.*


class OperationModelBaseTest extends Be5ProjectTest
{
    @Inject OperationExecutor operationExecutor

    @Test
    void execute()
    {
        //new OperationModelBase(operationExecutor, "")
    }

}