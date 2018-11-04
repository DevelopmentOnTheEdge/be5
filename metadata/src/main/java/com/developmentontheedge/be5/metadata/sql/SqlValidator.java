package com.developmentontheedge.be5.metadata.sql;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.base.DataElementPath;

import java.util.ArrayList;
import java.util.List;

public class SqlValidator
{
    public void validate(Project project)
    {
        List<ProjectElementException> errors = new ArrayList<>();
        for (String entityName : project.getEntityNames())
        {
            for (Query query : project.getEntity(entityName).getQueries())
            {
                validateQuery(query, errors);
            }
        }

        for (FreemarkerScript freemarkerScript : project.getMacroCollection().getScripts())
        {
            validateScript(freemarkerScript, errors);
        }

        ProjectElementException.checkErrors(errors);
    }

    private void validateScript(FreemarkerScript script, List<ProjectElementException> result)
    {
        ProjectElementException error = script.getResult().getError();
        if (error != null)
        {
            DataElementPath path = script.getCompletePath();
            if (error.getPath().equals(path.toString()))
                result.add(error);
            else
                result.add(new ProjectElementException(path, "source", error));
        }
    }

    private void validateQuery(Query query, List<ProjectElementException> result)
    {
        ProjectElementException error = query.getQueryCompiled().getError();
        if (error != null)
        {
            DataElementPath path = query.getCompletePath();
            if (error.getPath().equals(path.toString()))
                result.add(error);
            else
                result.add(new ProjectElementException(path, "query", error));
        }
    }
}
