package com.developmentontheedge.be5.mcpserver;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class McpServlet extends HttpServlet
{
    private final McpController mcpController;

    @Inject
    public McpServlet(McpController mcpController)
    {
        this.mcpController = mcpController;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    {
        try
        {
            McpRequest request = new McpRequest(req);
            McpResponse response = new McpResponse(resp);
            mcpController.handle(request, response);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static class McpRequest implements Request
    {
        private final HttpServletRequest req;

        McpRequest(HttpServletRequest req)
        {
            this.req = req;
        }

        @Override
        public java.util.Map<String, String[]> getParameters()
        {
            return req.getParameterMap();
        }

        @Override
        public java.util.List<String> getList(String parameter)
        {
            String[] values = getParameterValues(parameter);
            if (values == null) return new java.util.ArrayList<>();
            return java.util.Arrays.asList(values);
        }

        @Override
        public String[] getParameterValues(String name)
        {
            return req.getParameterValues(name);
        }

        @Override
        public String get(String parameter)
        {
            String[] values = req.getParameterValues(parameter);
            return values != null && values.length > 0 ? values[0] : null;
        }

        @Override
        public String getBody()
        {
            try
            {
                StringBuilder sb = new StringBuilder();
                java.io.BufferedReader reader = req.getReader();
                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line);
                }
                return sb.toString();
            }
            catch (IOException e)
            {
                return null;
            }
        }

        @Override
        public String getSessionId()
        {
            return req.getSession() != null ? req.getSession().getId() : null;
        }

        @Override
        public Object getAttribute(String name)
        {
            return req.getSession() != null ? req.getSession().getAttribute(name) : null;
        }

        @Override
        public void setAttribute(String name, Object value)
        {
            if (req.getSession() != null)
            {
                req.getSession().setAttribute(name, value);
            }
        }

        @Override
        public com.developmentontheedge.be5.web.Session getSession()
        {
            return null;
        }

        @Override
        public com.developmentontheedge.be5.web.Session getSession(boolean create)
        {
            return null;
        }

        @Override
        public String getRequestUri()
        {
            return req.getRequestURI();
        }

        @Override
        public String getRemoteAddr()
        {
            return req.getRemoteAddr();
        }

        @Override
        public HttpServletRequest getRawRequest()
        {
            return req;
        }

        @Override
        public javax.servlet.http.HttpSession getRawSession()
        {
            return req.getSession();
        }

        @Override
        public String getServerUrl()
        {
            return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
        }

        @Override
        public String getServerUrlWithContext()
        {
            return getServerUrl() + req.getContextPath();
        }

        @Override
        public String getContextPath()
        {
            return req.getContextPath();
        }

        @Override
        public java.util.Locale getLocale()
        {
            return req.getLocale();
        }

        @Override
        public javax.servlet.ServletInputStream getInputStream() throws IOException
        {
            return req.getInputStream();
        }

        @Override
        public javax.servlet.http.Cookie[] getCookies()
        {
            return req.getCookies();
        }
    }

    private static class McpResponse implements Response
    {
        private static final javax.json.bind.Jsonb JSONB = javax.json.bind.JsonbBuilder.create();
        private final HttpServletResponse resp;

        McpResponse(HttpServletResponse resp)
        {
            this.resp = resp;
        }

        @Override
        public void sendAsJson(Object value)
        {
            try
            {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(JSONB.toJson(value));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void sendAsJson(Object value, int status)
        {
            resp.setStatus(status);
            sendAsJson(value);
        }

        @Override
        public void setStatus(int status)
        {
            resp.setStatus(status);
        }

        @Override
        public void sendJson(String json)
        {
            try
            {
                resp.setContentType("application/json");
                resp.getWriter().write(json);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void sendHtml(String html)
        {
            try
            {
                resp.setContentType("text/html");
                resp.getWriter().write(html);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void sendXml(String xml)
        {
            try
            {
                resp.setContentType("application/xml");
                resp.getWriter().write(xml);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void sendYaml(String yaml)
        {
            try
            {
                resp.setContentType("application/yaml");
                resp.getWriter().write(yaml);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public HttpServletResponse getRawResponse()
        {
            return resp;
        }

        @Override
        public void redirect(String location)
        {
            try
            {
                resp.sendRedirect(location);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void addCookie(javax.servlet.http.Cookie cookie)
        {
            resp.addCookie(cookie);
        }

        @Override
        public java.io.OutputStream getOutputStream() throws IOException
        {
            return resp.getOutputStream();
        }
    }
}