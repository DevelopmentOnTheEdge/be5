package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.OperationService;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.server.services.OperationLogging;
import com.developmentontheedge.be5.web.Request;
import com.google.inject.OutOfScopeException;
import org.junit.Test;

import javax.inject.Provider;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryOperationServiceUnitTest
{
    @Test
    public void uploadLinkOutsideOfARequest()
    {
        UserInfoProvider userInfoProvider = mock(UserInfoProvider.class);
        when(userInfoProvider.getUserName()).thenReturn("anna");
        Provider<Request> noRequest = () -> {
            throw new OutOfScopeException("Cannot access scoped object outside of a request");
        };
        QueryOperationService service = new QueryOperationService(mock(Meta.class), mock(UserAwareMeta.class),
                userInfoProvider, mock(QueryExecutorFactory.class), mock(OperationExecutor.class),
                mock(OperationService.class), mock(OperationLogging.class), noRequest, new McpFileStore());

        Map<String, Object> upload = service.createUpload("Contract");

        String url = (String) upload.get("uploadUrl");
        assertTrue(url, url.startsWith("/api/mcp/upload/"));
    }
}
