package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LocalizationTest extends CoreBe5ProjectDbMockTest
{
    @Test
    public void test_INTERNAL_ERROR_IN_OPERATION()
    {
        assertEquals("Произошла внутренняя ошибка в операции: users.Login",
                userAwareMeta.getLocalizedBe5ErrorMessage(
                    Be5Exception.internalInOperation(
                            meta.getOperation("users", "Login"), new RuntimeException("test"))));
    }

}
