package com.developmentontheedge.be5.web.impl;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;


public class RequestImpl implements Request
{
    public static final Logger log = Logger.getLogger(RequestImpl.class.getName());

    private final HttpServletRequest rawRequest;
    private final String remoteAddr;
    
    public RequestImpl(HttpServletRequest rawRequest)
    {
        this.rawRequest = rawRequest;
        this.remoteAddr = getClientIpAddr(rawRequest);
    }

    @Override
    public Object getAttribute(String name)
    {
        return rawRequest.getSession().getAttribute(name);
    }
    
    @Override
    public void setAttribute(String name, Object value)
    {
    	rawRequest.setAttribute(name, value);
    }

    @Override
    public Session getSession()
    {
        return new SessionImpl(rawRequest.getSession());
    }

    @Override
    public Session getSession(boolean create)
    {
        HttpSession rawSession = rawRequest.getSession(create);
        if(rawSession == null)return null;
        return new SessionImpl(rawSession);
    }

    @Override
    public String getSessionId()
    {
        return getSession().getSessionId();
    }

    @Override
    public String get(String name)
    {
        return rawRequest.getParameter(name);
    }

    @Override
    public List<String> getList(String name)
    {
        return Arrays.asList(getParameterValues(name));
    }

    @Override
    public String[] getParameterValues(String name)
    {
        String[] values = rawRequest.getParameterValues(name + "[]");
        if(values == null)
        {
            String value = rawRequest.getParameter(name);
            if(value != null){
                return new String[]{value};
            }else{
                return new String[]{};
            }
        }
        return values;
    }

	@Override
    public Map<String, String[]> getParameters()
    {
        return Collections.unmodifiableMap((Map<String, String[]>)rawRequest.getParameterMap());
    }
    
	@Override
    public String getRequestUri()
    {
        return rawRequest.getRequestURI();
    }
    
    @Override
    public String getRemoteAddr()
    {
        return remoteAddr;
    }

    @Override
    public Locale getLocale()
    {
        return rawRequest.getLocale();
    }

    @Override
	public HttpServletRequest getRawRequest()
    {
		return rawRequest;
	}

    @Override
    public HttpSession getRawSession()
    {
        return getRawRequest().getSession();
    }

    @Override
    public String getServerUrl()
    {
        String scheme = rawRequest.getScheme() + "://";
        String serverName = rawRequest.getServerName();
        String serverPort = (rawRequest.getServerPort() == 80) ? "" : ":" + rawRequest.getServerPort();
        return scheme + serverName + serverPort;
    }

    @Override
    public String getServerUrlWithContext()
    {
        String contextPath = rawRequest.getContextPath();
        return getServerUrl() + contextPath;
    }

    @Override
    public String getContextPath()
    {
        return rawRequest.getContextPath();
    }

    @Override
    public String getBody()
    {
        StringBuilder sb = new StringBuilder();

        try(BufferedReader br = rawRequest.getReader()){
            String str;
            while( (str = br.readLine()) != null )
            {
                sb.append(str);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    /**
     * https://stackoverflow.com/a/15323776
     * @return remote address of a client
     */
    private String getClientIpAddr(HttpServletRequest request)
    {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
