package com.developmentontheedge.be5.web;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

/**
 * Request injected to components.
 *
 * @author asko
 * @see Controller
 */
public interface Request extends SessionAccess, ParametersAccess
{
    Session getSession();

    Session getSession(boolean create);

    /**
     * For tables - may be need refactoring to use Map<String, Object> getValuesFromJson()
     */
    //Map<String, String> getValuesFromJsonAsStrings(String parameterName) throws Be5Exception;

//    /**
//     * For operation with POJO
//     * @param parameterName
//     * @param clazz
//     * @param <T>
//     * @return
//     * @throws Be5Exception
//     */
//    <T> T getValuesFromJson(String parameterName, Class<T> clazz) throws Be5Exception;

    /**
     * Returns a remaining part of the request URI after the component ID.
     */
    String getRequestUri();

    /**
     * Returns the IP address of the client or last proxy that sent the request.
     */
    String getRemoteAddr();

    /**
     * Low-level request.
     */
    HttpServletRequest getRawRequest();

    /**
     * Low-level session.
     */
    HttpSession getRawSession();

    String getServerUrl();

    String getServerUrlWithContext();

    String getContextPath();

    String getBody();

    Locale getLocale();

    ServletInputStream getInputStream() throws IOException;
}
