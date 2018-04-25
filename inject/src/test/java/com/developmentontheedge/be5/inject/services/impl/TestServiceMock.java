package com.developmentontheedge.be5.inject.services.impl;

import com.developmentontheedge.be5.inject.services.TestService;

import static org.mockito.Mockito.mock;

public class TestServiceMock implements TestService
{
    public static TestService mock = mock(TestService.class);

    @Override
    public String call(String value)
    {
        return mock.call(value);
    }

    public static void clearMock()
    {
        mock = mock(TestService.class);
    }
}
