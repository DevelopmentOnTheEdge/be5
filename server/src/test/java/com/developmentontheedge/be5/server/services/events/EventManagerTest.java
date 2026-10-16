package com.developmentontheedge.be5.server.services.events;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
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

    @Test
    public void queryAndOperationAreKnownByArguments()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        Be5EventTestLogger.clearMock();
        Query query = meta.getQuery("testtable", "All records");
        testEvents.query(query, Collections.emptyMap());
        verify(Be5EventTestLogger.mock).queryCompleted(eq(query), eq(Collections.emptyMap()), anyLong(), anyLong());

        Operation operation = createOperation("testtable", "All records", "Insert", "");
        testEvents.operation(operation, Collections.emptyMap());
        verify(Be5EventTestLogger.mock).operationCompleted(eq(operation), eq(Collections.emptyMap()),
                anyLong(), anyLong());
        verify(Be5EventTestLogger.mock, never()).logCompleted(any(), any(), any(), anyLong(), anyLong());
    }

    public static class TestEvents
    {
        @LogBe5Event
        void query(Query query, Map<String, Object> parameters)
        {
        }

        @LogBe5Event
        void operation(Operation operation, Map<String, Object> values)
        {
        }

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
