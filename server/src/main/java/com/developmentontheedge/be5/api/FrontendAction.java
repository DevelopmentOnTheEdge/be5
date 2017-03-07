package com.developmentontheedge.be5.api;

import java.util.Objects;

import com.developmentontheedge.be5.util.HashUrl;

public class FrontendAction
{

    private static final FrontendAction DEFAULT_ACTION = new FrontendAction(Type.DEFAULT_ACTION, null); // <- by default Gson don't serialize null values at all
    private static final FrontendAction GO_BACK = new FrontendAction(Type.GO_BACK, null);
    
    /**
     * Don't use this class directly in Java as it's not a part of the API.
     */
    public static enum Type
    {
        DEFAULT_ACTION, GO_BACK, RENDER_HTML, REDIRECT
    }
    
    /**
     * Don't use this field directly in Java as it's not a part of the API.
     */
    public final Type type;
    /**
     * Don't use this field directly in Java as it's not a part of the API.
     * Note that this field can be null depending on the type of the action.
     * The class of this field can be changed in future.
     */
    public final String value;
    
    private FrontendAction(Type type, String value)
    {
        this.type = type;
        this.value = value;
    }
    
    public static FrontendAction defaultAction()
    {
        return DEFAULT_ACTION;
    }
    
    public static FrontendAction goBack()
    {
        return GO_BACK;
    }
    
    public static FrontendAction redirect(HashUrl redirectUrl)
    {
        Objects.requireNonNull(redirectUrl);
        return new FrontendAction(Type.REDIRECT, redirectUrl.toString());
    }
    
    public static FrontendAction renderHtml(String content)
    {
        Objects.requireNonNull(content);
        return new FrontendAction(Type.RENDER_HTML, content);
    }
    
}
