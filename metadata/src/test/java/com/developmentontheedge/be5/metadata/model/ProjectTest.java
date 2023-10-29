package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProjectTest
{
    @Test
    public void testGetLanguages()
    {
        Project project = new Project("test");
        assertArrayEquals(new String[0], project.getLanguages());
        project.getApplication().getLocalizations().addLocalization("ru", "test", Arrays.asList("myTopic"), "foo", "bar");
        assertArrayEquals(new String[]{"ru"}, project.getLanguages());
    }

    @Test
    public void testConnectionProfile()
    {
        Project project = new Project("test");
        assertEquals("test", project.getAppName());
        BeConnectionProfile profile = new BeConnectionProfile("myprofile", project.getConnectionProfiles().getLocalProfiles());
        profile.setConnectionUrl("jdbc:mysql://localhost:3306/db");
        DataElementUtils.save(profile);
        BeConnectionProfile profile2 = new BeConnectionProfile("myprofile2", project.getConnectionProfiles().getLocalProfiles());
        profile2.setConnectionUrl("jdbc:postgresql://localhost:5432/db");
        DataElementUtils.save(profile2);
        project.setConnectionProfileName("myprofile2");
        assertEquals("test", project.getAppName());
        assertEquals(Rdbms.POSTGRESQL, project.getDatabaseSystem());

        project.setConnectionProfileName("myprofile");
        assertEquals("test", project.getAppName());
        assertEquals(Rdbms.MYSQL, project.getDatabaseSystem());
    }

    @Test
    public void testModulesBasics()
    {
        Project project = new Project("test");
        Module module = new Module("module", project.getModules());
        DataElementUtils.save(module);
        Module module2 = new Module("module2", project.getModules());
        DataElementUtils.save(module2);
        assertArrayEquals(new String[]{"application", "module", "module2"}, project.getApplicationAndModuleNames());
        Module app = project.getApplication();
        assertEquals(Arrays.asList(module, module2, app), project.getModulesAndApplication());
    }

    @Test
    public void testMergeTemplate() throws ProjectElementException
    {
        Project project = new Project("test");
        FreemarkerScript script = new FreemarkerScript("script", project.getApplication().getFreemarkerScripts());
        DataElementUtils.saveQuiet(script);
        script.setSource("Hello from freemarker! ${project.getAppName()}");
        assertEquals("Hello from freemarker! test", project.mergeTemplate(script).validate());
    }

    @Test
    public void testClone() throws Exception
    {
        Project project = new Project("test");
        Entity tab1 = new Entity("tab1", project.getApplication(), EntityType.TABLE);
        DataElementUtils.saveQuiet(tab1);
        Operation tab1o = Operation.createOperation("o", Operation.OPERATION_TYPE_JAVA, tab1);
        tab1o.setCode("java.test.operation");
        DataElementUtils.saveQuiet(tab1o);
        Query tab1q = new Query("q", tab1);
        DataElementUtils.saveQuiet(tab1q);
        Module module = new Module("testModule", project.getModules());
        DataElementUtils.saveQuiet(module);
        Entity tab2 = (Entity) tab1.clone(module.getOrCreateEntityCollection(), "tab1");
        DataElementUtils.saveQuiet(tab2);
        assertEquals(tab1, tab2);
        tab2.getOperations().get("o").setWellKnownName("qqq");
        assertFalse(tab1.equals(tab2));
        tab1.getOperations().get("o").setWellKnownName("qqq");
        assertEquals(tab1, tab2);
    }

    @Test
    public void testCapabilities()
    {
        Project project = new Project("test");
        project.setDatabaseSystem(Rdbms.MYSQL);
        assertTrue(project.hasCapability("db:mysql"));
        assertFalse(project.hasCapability("db:postgres"));
        assertFalse(project.hasCapability("dbcap:fnindex"));
        project.setDatabaseSystem(Rdbms.POSTGRESQL);
        assertFalse(project.hasCapability("db:mysql"));
        assertTrue(project.hasCapability("db:postgres"));
        assertTrue(project.hasCapability("dbcap:fnindex"));

        project.setProperty("TEST", "value");
        assertTrue(project.hasCapability("var:TEST"));
        assertFalse(project.hasCapability("var:TEST2"));
        assertTrue(project.hasCapability("var:TEST=value"));
        assertFalse(project.hasCapability("var:TEST=value2"));
        assertTrue(project.hasCapability("!var:TEST=value2"));

        assertFalse(project.hasCapability("foo:"));

        assertFalse(project.hasCapability("feature:logging"));
        project.setFeatures(Collections.singleton("logging"));
        assertTrue(project.hasCapability("feature:logging"));

        assertFalse(project.hasCapability("module:mod"));
        Module module = new Module("mod", project.getModules());
        DataElementUtils.save(module);
        assertTrue(project.hasCapability("module:mod"));

        assertFalse(project.hasCapability("extra:mod::myextra"));
        assertTrue(project.hasCapability("!extra:mod::myextra"));
        module.setExtras(new String[]{"myextra"});
        assertTrue(project.hasCapability("extra:mod::myextra"));
        assertFalse(project.hasCapability("!extra:mod::myextra"));
    }
}
