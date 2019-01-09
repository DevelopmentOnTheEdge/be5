package com.developmentontheedge.be5.metadata.util;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ModuleUtilsTest
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Project getProject() throws ProjectSaveException
    {
        Project app = new Project("app");
        Module appModule = new Module("module", app.getModules());
        DataElementUtils.save(appModule);
        Entity appEntity = new Entity("mentity", appModule, EntityType.DICTIONARY);
        DataElementUtils.save(appEntity);
        Query appQ1 = new Query("q1", appEntity);
        appQ1.setQuery("QUERY1 customized");
        DataElementUtils.save(appQ1);
        Query appQ3 = new Query("q3", appEntity);
        appQ3.setQuery("QUERY3");
        appQ3.setTitleName("Query3 title");
        DataElementUtils.save(appQ3);
        return app;
    }

    private Project getModule() throws ProjectSaveException
    {
        Project module = new Project("module", true);
        Entity entity = new Entity("mentity", module.getApplication(), EntityType.TABLE);
        DataElementUtils.save(entity);
        Query q1 = new Query("q1", entity);
        q1.setQuery("QUERY1");
        q1.setTitleName("Query1 title");
        DataElementUtils.save(q1);
        Query q2 = new Query("q2", entity);
        q2.setQuery("QUERY2");
        q2.setTitleName("Query2 title");
        DataElementUtils.save(q2);
        return module;
    }

    @Test
    public void testMergeAllModules() throws Exception
    {
        Project module = getModule();
        Project app = getProject();

        LoadContext ctx = new LoadContext();
        ModuleLoader2.mergeAllModules(app, Collections.singletonList(module), ctx);
        ctx.check();
        assertEquals(Collections.singleton("mentity"), app.getEntityNames());
        assertEquals(EntityType.DICTIONARY, app.getEntity("mentity").getType());
        BeModelCollection<Query> queries = app.getEntity("mentity").getQueries();
        assertEquals("QUERY1 customized", queries.get("q1").getQuery());
        assertEquals("Query1 title", queries.get("q1").getTitleName());
        assertEquals("QUERY2", queries.get("q2").getQuery());
        assertEquals("Query2 title", queries.get("q2").getTitleName());
        assertEquals("QUERY3", queries.get("q3").getQuery());
        assertEquals("Query3 title", queries.get("q3").getTitleName());
    }

    @Test
    public void testMergeAllModulesWithLoadRead() throws Exception
    {
        Path tpmProjectPath = tmp.newFolder().toPath();
        Path tpmModulePath = tmp.newFolder().toPath();

        Project module = getModule();
        Serialization.save(module, tpmModulePath);

        Project app = getProject();
        Serialization.save(app, tpmProjectPath);

        ArrayList<URL> urls = new ArrayList<>();
        urls.add(tpmModulePath.resolve("project.yaml").toUri().toURL());
        urls.add(tpmProjectPath.resolve("project.yaml").toUri().toURL());
        ModuleLoader2.loadAllProjects(urls, new NullLogger());

        assertNotNull(ModuleLoader2.getModulePath("app", new NullLogger()).resolve("project.yaml"));

        app = ModuleLoader2.getModulesMap().get("app");
        ModuleLoader2.mergeModules(app, new NullLogger());

        assertEquals(Collections.singleton("mentity"), app.getEntityNames());
        assertEquals(EntityType.DICTIONARY, app.getEntity("mentity").getType());
        BeModelCollection<Query> queries = app.getEntity("mentity").getQueries();
        assertEquals("QUERY1 customized", queries.get("q1").getQuery());
        assertEquals("Query1 title", queries.get("q1").getTitleName());
        assertEquals("QUERY2", queries.get("q2").getQuery());
        assertEquals("Query2 title", queries.get("q2").getTitleName());
        assertEquals("QUERY3", queries.get("q3").getQuery());
        assertEquals("Query3 title", queries.get("q3").getTitleName());
    }

    @Test
    @Ignore
    public void testModuleNames()
    {
//        Set<String> modules = ModuleUtils.getAvailableModules();
//        assertTrue( modules.contains( "security" ) );
//        assertTrue( modules.contains( "realty" ) );
//        assertFalse( modules.contains( "ru.social" ) );
    }

    @Test
    @Ignore
    public void testValidateModules() throws ProjectLoadException, ReadException
    {
//        for ( String module : ModuleUtils.getAvailableModules() )
//        {
//            LoadContext context = new LoadContext();
//            Project project = ModuleUtils.loadModule( module, context );
//            String warnings = StreamEx.of( context.getWarnings() )
//                .remove( warn -> warn.getMessage().startsWith( "Unable to load icon" ) )
//                .joining( "\n" );
//            if(!warnings.isEmpty())
//                fail(warnings);
//            assertNotNull( project );
//            ModuleUtils.addModuleScripts( project );
//            List<ProjectElementException> errors = project.getErrors();
//            if ( !errors.isEmpty() )
//            {
//                fail( StreamEx.of( errors ).joining( "\n" ) );
//            }
//        }
    }
}
