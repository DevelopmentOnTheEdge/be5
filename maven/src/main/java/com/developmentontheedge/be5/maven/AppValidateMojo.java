package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.scripts.AppValidate;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Usage example:
 * mvn be5:validate -DBE5_DEBUG=true
 */
@Mojo( name = "validate")
public class AppValidateMojo extends Be5Mojo
{
    @Parameter (property = "BE5_RDBMS")
    String rdbmsName;

    @Parameter (property = "BE5_SKIP_VALIDATION")
    boolean skipValidation = false;

    @Parameter (property = "BE5_CHECK_QUERY")
    String queryPath;

    @Parameter (property = "BE5_CHECK_ROLES")
    boolean checkRoles;

    @Parameter (property = "BE5_CHECK_DDL")
    String ddlPath;

    @Parameter (property = "BE5_SAVE_PROJECT")
    boolean saveProject;

    @Override
    public void execute()
    {
        new AppValidate()
                .setLogger(logger)
                .setBe5ProjectPath(projectPath.toPath())
                .setCheckQueryPath(queryPath)
                .setDdlPath(ddlPath)
                .setCheckRoles(checkRoles)
                .setRdbmsName(rdbmsName)
                .setSkipValidation(skipValidation)
                .setSaveProject(saveProject)
                .execute();
    }
}
