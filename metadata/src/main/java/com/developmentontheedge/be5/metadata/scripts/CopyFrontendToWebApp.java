package com.developmentontheedge.be5.metadata.scripts;


public class CopyFrontendToWebApp extends ScriptSupport<CopyFrontendToWebApp>
{
    @Override
    public void execute() throws ScriptException
    {
        FrontendUtils.copyToWebApp(projectPath.toString());
    }

    @Override
    public CopyFrontendToWebApp me()
    {
        return this;
    }
}
