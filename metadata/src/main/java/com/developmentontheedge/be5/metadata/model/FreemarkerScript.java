package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.model.base.DataElementPath;
import com.developmentontheedge.be5.metadata.model.base.TemplateElement;
import com.developmentontheedge.beans.annot.PropertyName;

public class FreemarkerScript extends SourceFile implements TemplateElement, FreemarkerScriptOrCatalog
{
    public FreemarkerScript(String name, FreemarkerCatalog origin)
    {
        super(name, origin);
    }

    @PropertyName("Result")
    public ParseResult getResult()
    {
        return getProject().mergeTemplate(this);
    }

    @Override
    public String getTemplateCode()
    {
        return getSource();
    }

    public String getRelativePath(FreemarkerCatalog scripts)
    {
        DataElementPath basePath = scripts.getCompletePath();
        return getCompletePath().getPathDifference(basePath);
    }

}
