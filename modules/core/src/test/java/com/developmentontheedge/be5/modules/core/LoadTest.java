package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.scripts.AppValidate;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;

public class LoadTest extends CoreBe5ProjectDbMockTest
{
    @Inject
    private ProjectProvider projectProvider;

    @Test
    public void testLoadModuleCore() throws IOException, URISyntaxException, ProjectLoadException
    {
        Assert.assertEquals("be5CoreTestProject", projectProvider.get().getAppName());

        BeModelCollection<Module> modules = projectProvider.get().getModules();
        Assert.assertNotNull(modules.get("core"));
    }

    @Test
    public void validate()
    {
        new AppValidate().setBe5ProjectPath("./").execute();
    }

    @Test
    public void testPredefinedMacros() throws ProjectElementException
    {
        Project project = projectProvider.get();

        Entity entity = new Entity("myTable", project.getApplication(), EntityType.TABLE);
        DataElementUtils.saveQuiet(entity);

        Query query = new Query("All records", entity);
        DataElementUtils.saveQuiet(query);
        query.setQuery("SELECT * FROM " + entity.getName());
        Query query2 = new Query("Copy", entity);
        DataElementUtils.saveQuiet(query2);

        query2.setQuery("<@_copyAllRecordsQuery/>");
        Assert.assertEquals("SELECT * FROM myTable", query2.getFinalQuery());

        query2.setQuery("SELECT <@_bold>name</@_bold> FROM myTable");
        Assert.assertEquals("SELECT ( '<b>' || name || '</b>' ) FROM myTable", query2.getFinalQuery());

        query2.setQuery("SELECT <@_bold><@_italic>name</@></@> FROM myTable");
        Assert.assertEquals("SELECT ( '<b>' || ( '<i>' || name || '</i>' ) || '</b>' ) FROM myTable", query2.getFinalQuery());
    }
}
