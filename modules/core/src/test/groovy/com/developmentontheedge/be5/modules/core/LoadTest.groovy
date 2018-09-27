package com.developmentontheedge.be5.modules.core

import com.developmentontheedge.be5.base.services.ProjectProvider
import com.developmentontheedge.be5.metadata.exception.ProjectElementException
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException
import com.developmentontheedge.be5.metadata.model.DataElementUtils
import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.metadata.model.EntityType
import com.developmentontheedge.be5.metadata.model.Module
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection
import com.developmentontheedge.be5.metadata.scripts.AppValidate
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class LoadTest extends CoreBe5ProjectDbMockTest
{
    @Inject
    ProjectProvider projectProvider

    @Test
    void testLoadModuleCore() throws IOException, URISyntaxException, ProjectLoadException
    {
        assertEquals("be5CoreTestProject", projectProvider.get().getAppName())

        BeModelCollection<Module> modules = projectProvider.get().getModules()
        assertNotNull(modules.get("core"))
    }

    @Test
    void validate()
    {
        new AppValidate().setBe5ProjectPath("./").execute()
    }

    @Test
    void testPredefinedMacros() throws ProjectElementException
    {
        def project = projectProvider.get()

        Map<String, Object> dataModel = new HashMap<>()
        dataModel.put("project", project)
        Entity entity = new Entity("myTable", project.getApplication(), EntityType.TABLE)
        DataElementUtils.saveQuiet(entity)

        Query query = new Query("All records", entity)
        DataElementUtils.saveQuiet(query)
        query.setQuery("SELECT * FROM ${entity.getName()}")
        Query query2 = new Query("Copy", entity)
        DataElementUtils.saveQuiet(query2)

        query2.setQuery("<@_copyAllRecordsQuery/>")
        assertEquals("SELECT * FROM myTable", query2.getQueryCompiled().validate())

        query2.setQuery("SELECT <@_bold>name</@_bold> FROM myTable")
        assertEquals("SELECT ( '<b>' || name || '</b>' ) FROM myTable", query2.getQueryCompiled().validate())

        query2.setQuery("SELECT <@_bold><@_italic>name</@></@> FROM myTable")
        assertEquals("SELECT ( '<b>' || ( '<i>' || name || '</i>' ) || '</b>' ) FROM myTable",
                query2.getQueryCompiled().validate())
    }
}
