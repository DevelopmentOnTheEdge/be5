package com.developmentontheedge.be5.api.impl;

import com.developmentontheedge.be5.AbstractProjectTest;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.model.Action;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResponseTest extends AbstractProjectTest{

    private Response response;
    private HttpServletResponse rawResponse;
    private PrintWriter writer;

    @Before
    public void init() throws Exception {
        writer = mock(PrintWriter.class);

        rawResponse = mock(HttpServletResponse.class);
        when(rawResponse.getWriter()).thenReturn(writer);

        response = new ResponseImpl(rawResponse);
    }

    @Test
    public void sendAsJson() throws Exception {
        Action call = new Action("call", "test/path");
        response.sendAsJson(call);

        verify(writer).append("{\"value\":{\"name\":\"call\",\"arg\":\"test/path\"}}");
        verify(writer).flush();
    }

    @Test
    public void sendAsRawJson() throws Exception {
        Action call = new Action("call", "test/path");
        response.sendAsRawJson(call);

        verify(writer).append("{\"name\":\"call\",\"arg\":\"test/path\"}");
    }

    @Test
    public void sendSuccess() throws Exception {
        response.sendSuccess();

        verify(writer).append("{\"type\":\"ok\"}");
    }

    @Test
    public void sendError() {
        response.sendError(Be5Exception.internal("testMsg"));

        verify(rawResponse).setContentType("application/json");
        verify(rawResponse).setCharacterEncoding(StandardCharsets.UTF_8.name());
        verify(writer).append("{\"type\":\"error\",\"value\":{\"message\":\"\",\"code\":\"INTERNAL_ERROR\"}}");
        verify(writer).flush();
    }

    @Test
    public void sendErrorText()  {
        response.sendError("test msg");

        verify(writer).append("{\"type\":\"error\",\"value\":\"test msg\"}");
    }

    @Test
    public void getRawResponse() throws Exception {
        assertEquals(rawResponse, response.getRawResponse());
    }

    @XmlRootElement(name = "ActionForXml")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActionForXml {

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
    public void sendAsXml() throws Exception {
        ActionForXml call = new ActionForXml("call", "test/path");
        response.sendAsXml(ActionForXml.class, call);

        verify(rawResponse).setContentType("application/xml");
        verify(writer).append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ActionForXml>\n" +
                "    <name>call</name>\n" +
                "    <arg>test/path</arg>\n" +
                "</ActionForXml>\n");
    }

}