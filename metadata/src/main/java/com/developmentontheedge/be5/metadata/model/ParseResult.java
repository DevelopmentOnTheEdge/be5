package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.beans.annot.PropertyName;

public class ParseResult
{
    private final String result;
    private final ProjectElementException error;

    public ParseResult(String result)
    {
        this.error = null;
        this.result = result;
    }

    public ParseResult(ProjectElementException error)
    {
        this.error = error;
        this.result = null;
    }

    public String validate() throws ProjectElementException
    {
        if (error != null)
            throw error;
        return result;
    }

    @PropertyName("Result")
    public String getResult()
    {
        return result;
    }

    @PropertyName("Error")
    public ProjectElementException getError()
    {
        return error;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParseResult that = (ParseResult) o;

        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        return error != null ? error.equals(that.error) : that.error == null;
    }
}
