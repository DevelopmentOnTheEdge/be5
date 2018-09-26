package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.scripts.AppDropAllTables;
import org.apache.maven.plugins.annotations.Mojo;


@Mojo(name = "drop-all-tables-db")
public class AppDropAllTablesMojo extends Be5Mojo
{
    @Override
    public void execute()
    {
        new AppDropAllTables()
                .setBe5ProjectPath(projectPath.toPath())
                .setProfileName(connectionProfileName)
                .setConnectionPassword(connectionPassword)
                .setLogPath(logPath)
                .setLogger(logger)
                .setDebug(debug)
                .execute();
    }

}
