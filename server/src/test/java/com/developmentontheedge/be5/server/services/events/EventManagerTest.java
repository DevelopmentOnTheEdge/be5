package com.developmentontheedge.be5.server.services.events;

import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Test;

import javax.inject.Inject;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class EventManagerTest extends ServerBe5ProjectTest
{
    @Inject TestEvents testEvents;

    @Test
    public void logCompleted()
    {
        testEvents.test(false);
        verify(Be5EventTestLogger.mock).logCompleted(eq("TestEvents"), eq("test"),
                any(), anyLong(), anyLong());
    }

    @Test
    public void logException()
    {
        try {
            testEvents.test(true);
        } catch (Throwable ignore) {}
        verify(Be5EventTestLogger.mock).logException(eq("TestEvents"), eq("test"),
                any(), anyLong(), anyLong(), eq("test"));
    }

    @Test
    public void testThrowExceptionWithEmptyMessage()
    {
        try {
            testEvents.throwExceptionWithEmptyMessage();
        } catch (Throwable ignore) {}
        verify(Be5EventTestLogger.mock).logException(eq("TestEvents"), eq("throwExceptionWithEmptyMessage"),
                any(), anyLong(), anyLong(), eq("Exception (empty message)"));
    }

    public static class TestEvents
    {
        @LogBe5Event
        void test(boolean throwException)
        {
            if (throwException) throw new RuntimeException("test");
        }

        @LogBe5Event
        void throwExceptionWithEmptyMessage()
        {
            throw new NullPointerException();
        }
    }
}
