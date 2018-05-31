package com.developmentontheedge.be5.web.impl;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;


/**
 * Guarantees correct usage of the low-level response.
 * 
 * @author asko
 */
public class RawResponseWrapper {

    private final HttpServletResponse rawResponse;
    private PrintWriter out = null;
    private boolean contentTypeIsSet = false;
    //private boolean characterEncodingIsSet = false;

    RawResponseWrapper(HttpServletResponse rawResponse)
    {
        this.rawResponse = rawResponse;
    }

    public void setContentType(String contentType)
    {
        Objects.requireNonNull(contentType);
        checkState(!contentTypeIsSet);
        rawResponse.setContentType(contentType);
        contentTypeIsSet = true;
    }

    public void setStatus(int status)
    {
        rawResponse.setStatus(status);
    }

//    void setCharacterEncoding(Charset charset)
//    {
//        checkNotNull(charset);
//        checkState(!characterEncodingIsSet);
//        rawResponse.setCharacterEncoding(charset.name());
//        characterEncodingIsSet = true;
//    }
    
    public void append(String string)
    {
        Objects.requireNonNull(string);
        getWriter().append(string);
    }

    public void flush()
    {
        getWriter().flush();
    }
    
    /**
     * Note that this method is not pure (constant).
     */
    private PrintWriter getWriter()
    {
        checkState(contentTypeIsSet);
        //checkState(characterEncodingIsSet);
        
        if (out == null)
        {
            try
            {
                out = rawResponse.getWriter();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        
        return out;
    }
    
    HttpServletResponse getRawResponse()
    {
        return rawResponse;
    }

    private static void checkState(boolean expression)
    {
        if (!expression) {
            throw new IllegalStateException();
        }
    }
}
