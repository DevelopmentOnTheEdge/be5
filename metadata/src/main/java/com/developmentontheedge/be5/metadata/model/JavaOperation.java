package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.beans.annot.PropertyName;

import java.util.List;

@PropertyName("Operation")
public class JavaOperation extends Operation
{
    protected JavaOperation(String name, Entity entity)
    {
        super(name, OPERATION_TYPE_JAVA, entity);
    }

    @Override
    public List<ProjectElementException> getErrors()
    {
        List<ProjectElementException> result = super.getErrors();
        try
        {
            Class.forName(getCode());
        }
        catch (ClassNotFoundException e)
        {
            result.add(new ProjectElementException(getCompletePath(), "code", e));
        }
        return result;
    }
}
