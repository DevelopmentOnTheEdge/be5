package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.util.HashUrl;

import static com.google.common.base.Preconditions.checkNotNull;

public class FrontendAction
{
    private static final FrontendAction DEFAULT_ACTION = new FrontendAction(Type.DEFAULT_ACTION, null);
    private static final FrontendAction GO_BACK = new FrontendAction(Type.GO_BACK, null);
    private static final FrontendAction REFRESH_ALL = new FrontendAction(Type.REFRESH_ALL, null);

    private enum Type
    {
        DEFAULT_ACTION, GO_BACK, RENDER_HTML, REDIRECT, REFRESH_ALL
    }

    private final Type type;

    private final String value;

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

    public static FrontendAction goRefreshAll()
    {
        return REFRESH_ALL;
    }

    public static FrontendAction redirect(HashUrl redirectUrl)
    {
        checkNotNull(redirectUrl);
        return new FrontendAction(Type.REDIRECT, redirectUrl.toString());
    }

    public static FrontendAction renderHtml(String content)
    {
        checkNotNull(content);
        return new FrontendAction(Type.RENDER_HTML, content);
    }

    public Type getType()
    {
        return type;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FrontendAction that = (FrontendAction) o;

        if (type != that.type) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }
}
