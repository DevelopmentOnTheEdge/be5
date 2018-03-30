package com.developmentontheedge.be5.model.jsonapi;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;

import java.util.Map;

/**
 * http://jsonapi.org/format/#errors-processing
 */
public class ErrorModel
{
//    private String id;
    private String status;
    private String title;
    private String code;
    private String detail;
    private Map<String, String> links;

//    private Object source;
//    private Object meta;

    public ErrorModel(String status, String title)
    {
        this.status = status;
        this.title = title;
    }

    public ErrorModel(String status, String title, Map<String, String> links)
    {
        this.status = status;
        this.title = title;
        this.links = links;
    }

    public ErrorModel(String status, String title, String code, String detail, Map<String, String> links)
    {
        this.status = status;
        this.title = title;
        this.code = code;
        this.detail = detail;
        this.links = links;
    }

    public ErrorModel(Be5Exception e, String additionalMessage, Map<String, String> links)
    {
        this(getHttpStatusCode(e), e.getTitle(), Be5Exception.getMessage(e) + additionalMessage,
                Be5Exception.exceptionAsString(e), links);
    }

    public ErrorModel(Be5Exception e, Map<String, String> links)
    {
        this(getHttpStatusCode(e), e.getTitle(), Be5Exception.getMessage(e), Be5Exception.exceptionAsString(e), links);
    }

    public ErrorModel(Be5Exception e)
    {
        this(getHttpStatusCode(e), e.getTitle(), Be5Exception.getMessage(e), Be5Exception.exceptionAsString(e), null);
    }

    public String getStatus()
    {
        return status;
    }

    public String getCode()
    {
        return code;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDetail()
    {
        return detail;
    }

    public Map<String, String> getLinks()
    {
        return links;
    }

    @Override
    public String toString()
    {
        return "ErrorModel{" +
                "status='" + status + '\'' +
                ", title='" + title + '\'' +
                ", code='" + code + '\'' +
                ", detail='" + detail + '\'' +
                ", links=" + links +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErrorModel that = (ErrorModel) o;

        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (detail != null ? !detail.equals(that.detail) : that.detail != null) return false;
        return links != null ? links.equals(that.links) : that.links == null;
    }

    @Override
    public int hashCode()
    {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (detail != null ? detail.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        return result;
    }

    private final static String httpCode404 = "404";
    private final static String httpCode403 = "403";
    private final static String httpCode500 = "500";

    private static String getHttpStatusCode(Be5Exception e)
    {
        if (e.getCode().isNotFound())return httpCode404;
        if (e.getCode().isAccessDenied())return httpCode403;
        return httpCode500;
    }
}
