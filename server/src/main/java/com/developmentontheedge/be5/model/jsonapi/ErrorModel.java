package com.developmentontheedge.be5.model.jsonapi;

import java.util.Map;


/**
 * http://jsonapi.org/format/#errors-processing
 */
public class ErrorModel
{
    private String id;
    private Map<String, String> links;

    private String status;
    private String title;
    private String code;
    private String detail;

    private Object source;
    private Object meta;

    public ErrorModel(String status, String title)
    {
        this.status = status;
        this.title = title;
    }

    public ErrorModel(String status, String title, String code, String detail)
    {
        this.status = status;
        this.code = code;
        this.title = title;
        this.detail = detail;
    }

    public String getId()
    {
        return id;
    }

    public Map<String, String> getLinks()
    {
        return links;
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

    public Object getSource()
    {
        return source;
    }

    public Object getMeta()
    {
        return meta;
    }

    @Override
    public String toString()
    {
        return "ErrorModel{" +
                "id='" + id + '\'' +
                ", links=" + links +
                ", status='" + status + '\'' +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", source=" + source +
                ", meta=" + meta +
                '}';
    }
}
