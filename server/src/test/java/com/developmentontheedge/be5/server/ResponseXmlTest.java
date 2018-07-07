package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.server.util.Jaxb;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.impl.ResponseImpl;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.PrintWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ResponseXmlTest extends ServerBe5ProjectTest
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
//    public void sendErrorsAsJson() {
//        Action call = new Action("call", "test/path");
//        response.sendErrorsAsJson(call);
//
//        verify(writer).append(doubleQuotes("{'value':{'arg':'test/path','name':'call'}}"));
//        verify(writer).flush();
//    }

//    @Test
//    public void sendErrorSysDev()
//    {
//        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);
//
//        response.sendError(Be5Exception.internal("testMsg"));
//
//        verify(rawResponse).setContentType("application/json;charset=UTF-8");
//        //verify(rawResponse).setCharacterEncoding(StandardCharsets.UTF_8.name());
//        verify(writer).append(contains(doubleQuotes("'detail':")));
//        verify(writer).flush();
//    }

//    @Test
//    public void sendErrorNotSysDev()
//    {
//        initUserWithRoles(RoleType.ROLE_GUEST);
//
//        response.sendError(Be5Exception.internal("testMsg"));
//
//        verify(rawResponse).setContentType("application/json;charset=UTF-8");
//        //verify(rawResponse).setCharacterEncoding(StandardCharsets.UTF_8.name());
//        verify(writer).append(doubleQuotes("{'errors':[{'status':'500','title':''}]}"));
//        verify(writer).flush();
//    }

    @XmlRootElement(name = "ActionForXml")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActionForXml
    {
        String name;
        String arg;

        public ActionForXml()
        {
        }

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
        response.sendXml(Jaxb.toXml(ActionForXml.class, call));

        verify(rawResponse).setContentType("application/xml;charset=UTF-8");
        verify(writer).append(doubleQuotes("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n" +
                "<ActionForXml>\n" +
                "    <name>call</name>\n" +
                "    <arg>test/path</arg>\n" +
                "</ActionForXml>\n"));
    }

}