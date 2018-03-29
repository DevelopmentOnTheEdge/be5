package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.api.impl.ResponseImpl;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.model.Action;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.PrintWriter;
import java.util.Collections;

import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.api.RestApiConstants.TIMESTAMP_PARAM;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ResponseTest extends Be5ProjectTest
{

    private Response response;
    private HttpServletResponse rawResponse;
    private PrintWriter writer;

    @Before
    public void init() throws Exception
    {
        writer = mock(PrintWriter.class);

        rawResponse = mock(HttpServletResponse.class);
        when(rawResponse.getWriter()).thenReturn(writer);

        response = new ResponseImpl(rawResponse);
    }

//    @Test
//    public void sendErrorsAsJson() throws Exception {
//        Action call = new Action("call", "test/path");
//        response.sendErrorsAsJson(call);
//
//        verify(writer).append(doubleQuotes("{'value':{'arg':'test/path','name':'call'}}"));
//        verify(writer).flush();
//    }

    @Test
    public void sendAsRawJson()
    {
        Action call = new Action("call", "test/path");
        response.sendAsRawJson(call);

        verify(writer).append(doubleQuotes("{'arg':'test/path','name':'call'}"));
    }

    @Test
    public void sendErrorSysDev()
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);

        response.sendError(Be5Exception.internal("testMsg"));

        verify(rawResponse).setContentType("application/json;charset=UTF-8");
        //verify(rawResponse).setCharacterEncoding(StandardCharsets.UTF_8.name());
        verify(writer).append(contains(doubleQuotes("'detail':")));
        verify(writer).flush();
    }

    @Test
    public void sendErrorNotSysDev()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);

        response.sendError(Be5Exception.internal("testMsg"));

        verify(rawResponse).setContentType("application/json;charset=UTF-8");
        //verify(rawResponse).setCharacterEncoding(StandardCharsets.UTF_8.name());
        verify(writer).append(doubleQuotes("{'errors':[{'status':'500','title':''}]}"));
        verify(writer).flush();
    }

    @Test
    public void getRawResponse()
    {
        assertEquals(rawResponse, response.getRawResponse());
    }

    @XmlRootElement(name = "ActionForXml")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActionForXml
    {
        String name;
        String arg;

        public ActionForXml(){}
        ActionForXml(String name, String arg)
        {
            this.name = name;
            this.arg = arg;
        }
    }

    @Test
    public void sendAsXml()
    {
        ActionForXml call = new ActionForXml("call", "test/path");
        response.sendAsXml(ActionForXml.class, call);

        verify(rawResponse).setContentType("application/xml;charset=UTF-8");
        verify(writer).append(doubleQuotes("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n" +
                "<ActionForXml>\n" +
                "    <name>call</name>\n" +
                "    <arg>test/path</arg>\n" +
                "</ActionForXml>\n"));
    }

    @Test
    public void testJsonObject()
    {
        JsonApiModel jsonApiModel = JsonApiModel.data(new ResourceData("testType", "test", Collections.singletonMap(SELF_LINK, "url")),
                Collections.singletonMap(TIMESTAMP_PARAM, 1503291145939L));
        response.sendAsJson(jsonApiModel);

        verify(writer).append(doubleQuotes("{" +
                "'data':{'attributes':'test','links':{'self':'url'},'type':'testType'}," +
                "'meta':{'_ts_':1503291145939}" +
        "}"));
    }

}