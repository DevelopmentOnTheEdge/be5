package com.developmentontheedge.be5.metadata.sql;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.ParseResult;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.base.DataElementPath;
import com.developmentontheedge.sql.model.SqlQuery;

import java.util.ArrayList;
import java.util.List;

import static com.developmentontheedge.be5.metadata.Features.BE_SQL_QUERIES_FEATURE;

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
        String queryText;
        if (query.getProject().hasFeature(BE_SQL_QUERIES_FEATURE))
        {
            queryText = query.getFinalQuery();
        }
        else
        {
            ParseResult parseResult = query.getQueryCompiled();
            ProjectElementException error = parseResult.getError();
            if (error != null)
            {
                DataElementPath path = query.getCompletePath();
                if (error.getPath().equals(path.toString()))
                    result.add(error);
                else
                    result.add(new ProjectElementException(path, "query", error));
                return;
            }
            queryText = parseResult.getResult();
        }

        if (query.isSqlQuery())
        {
            try
            {
                SqlQuery.parse(queryText);
            }
            catch (RuntimeException e)
            {
                result.add(new ProjectElementException(query.getCompletePath(), "query", e));
            }
        }
    }
}
