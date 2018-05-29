package com.developmentontheedge.be5.api.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;

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
        checkNotNull(contentType);
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
        checkNotNull(string);
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
                throw Be5Exception.internal(e);
            }
        }
        
        return out;
    }
    
    HttpServletResponse getRawResponse()
    {
        return rawResponse;
    }
    
}
