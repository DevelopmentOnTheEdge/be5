package com.developmentontheedge.be5.api.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.util.ParseRequestUtils;


import static com.developmentontheedge.be5.api.RestApiConstants.TIMESTAMP_PARAM;


public class RequestImpl implements Request
{
    public static final Logger log = Logger.getLogger(RequestImpl.class.getName());

    private final HttpServletRequest rawRequest;
    private final String requestUri;
    private final String remoteAddr;
    
    public RequestImpl(HttpServletRequest rawRequest, String requestUri)
    {
        this.rawRequest = rawRequest;
        this.requestUri = requestUri;
        this.remoteAddr = getClientIpAddr(rawRequest);
    }

    @Override
    public Object getAttribute(String name)
    {
    	HttpSession session = rawRequest.getSession();
        return session == null ? null : session.getAttribute(name);
    }
    
    @Override
    public void setAttribute(String name, Object value)
    {
    	HttpSession session = rawRequest.getSession();
    	if(session != null)
    	    session.setAttribute(name, value);
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

//
//    @Override
//    public <T> T getValuesFromJson(String parameterName, Class<T> clazz) throws Be5Exception
//    {
//        return jsonb.fromJson(get(parameterName), clazz);
//    }

    @Override
    public Map<String, Object> getValuesFromJson(String parameter) throws Be5Exception
    {
        String valuesString = get(parameter);

        try
        {
            return ParseRequestUtils.getValuesFromJson(get(parameter));
        }
        catch (ClassCastException e)
        {
            throw Be5Exception.invalidRequestParameter(e, parameter, valuesString);
        }
    }
//
//    /**
//     * for query
//     */
//    @Override
//    public Map<String, String> getValuesFromJsonAsStrings(String parameter) throws Be5Exception
//    {
//        String valuesString = get(parameter);
//        if(Strings.isNullOrEmpty(valuesString))
//        {
//            return Collections.emptyMap();
//        }
//
//        Map<String, String> fieldValues = new HashMap<>();
//
//        try
//        {
//            JsonObject values = (JsonObject) new JsonParser().parse(valuesString);
//            for (Map.Entry entry: values.entrySet())
//            {
//                fieldValues.put(entry.getKey().toString(), ((JsonElement)entry.getValue()).getAsString());
//            }
////            JsonArray values = (JsonArray) new JsonParser().parse(valuesString);
////            for (int i = 0; i < values.size(); i++)
////            {
////                JsonObject pair = (JsonObject) values.get(i);
////                String name = pair.get("name").getAsString();
////                String value = pair.get("value").getAsString();
////                if( !"".equals(value) )
////                {
////                    fieldValues.put(name, value);
////                }
////            }
//        }
//        catch (ClassCastException e)
//        {
//            throw Be5Exception.invalidRequestParameter(log, e, parameter, valuesString);
//        }
//		return fieldValues;
//    }

	@Override
    public Map<String, String[]> getParameters()
    {
        return Collections.unmodifiableMap((Map<String, String[]>)rawRequest.getParameterMap());
    }
    
	@Override
    public String getRequestUri()
    {
        return requestUri;
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
    public String getServletContextRealPath(String s)
    {
        return getRawRequest().getSession().getServletContext().getRealPath(s);
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
    public String getBaseUrl()
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
            throw Be5Exception.internal(e);
        }

        return sb.toString();
    }

    @Override
    public Object getDefaultMeta()
    {
        return Collections.singletonMap(TIMESTAMP_PARAM, get(TIMESTAMP_PARAM));
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
