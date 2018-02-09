package com.developmentontheedge.be5.model.jsonapi;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;


/**
 * http://jsonapi.org/format/#errors-processing
 */
public class ErrorModel
{
//    private String id;
//    private Map<String, String> links;

    private String status;
    private String title;
    private String code;
    private String detail;

//    private Object source;
//    private Object meta;

    public ErrorModel(String status, String title)
    {
        this.status = status;
        this.title = title;
    }

    public ErrorModel(String status, String title, String code, String detail)
    {
        this.status = status;
        this.title = title;
        this.code = code;
        this.detail = detail;
    }

    public ErrorModel(Be5Exception e, String additionalMessage)
    {
        this(getHttpStatusCode(e), e.getTitle(), Be5Exception.getMessage(e) + " " + additionalMessage,
                Be5Exception.exceptionAsString(e));
    }

    public ErrorModel(Be5Exception e)
    {
        this(getHttpStatusCode(e), e.getTitle(), Be5Exception.getMessage(e), Be5Exception.exceptionAsString(e));
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

    @Override
    public String toString()
    {
        return "ErrorModel{" +
                ", status='" + status + '\'' +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                '}';
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
