package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.Features;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.beans.annot.PropertyName;

import java.util.List;

import static com.developmentontheedge.be5.metadata.MetadataUtils.getCompiledGroovyClassName;

@PropertyName("Operation")
public class GroovyOperation extends SourceFileOperation
{
    protected GroovyOperation(String name, Entity entity)
    {
        super(name, OPERATION_TYPE_GROOVY, entity);
    }

    @Override
    public String getFileNameSpace()
    {
        return SourceFileCollection.NAMESPACE_GROOVY_OPERATION;
    }

    @Override
    public String getFileExtension()
    {
        return ".groovy";
    }

    @Override
    public List<ProjectElementException> getErrors()
    {
        List<ProjectElementException> result = super.getErrors();
        if (getProject().hasFeature(Features.COMPILED_GROOVY))
        {
            String className = getCompiledGroovyClassName(getFileName());
            try
            {
                Class.forName(className);
            }
            catch (ClassNotFoundException e)
            {
                result.add(new ProjectElementException(getCompletePath(), "file: " + getFileName(), e));
            }
        }
        return result;
    }
}
