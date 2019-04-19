package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.SpecialRoleGroup;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static com.developmentontheedge.be5.metadata.util.ProjectTestUtils.createScript;
import static com.developmentontheedge.be5.metadata.util.ProjectTestUtils.getProject;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class WatchDirTest
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void test() throws Exception
    {
        Path path = tmp.newFolder().toPath();
        Project project = getProject("test");
        Serialization.save(project, path);

        boolean modify[] = new boolean[]{false};

        WatchDir watcher = null;
        try
        {
            watcher = new WatchDir(Collections.singletonMap("main", project))
                    .onModify(onModify -> modify[0] = true)
                    .start();

            while (!modify[0])
            {
                Thread.sleep(100);

                createScript(project, "Post-db", "INSERT INTO entity (name) VALUES ('foo')" + new Random().nextInt());
                Serialization.save(project, path);
            }

            assertTrue(modify[0]);
            watcher.stop();

            do
            {
                Thread.sleep(100);

                modify[0] = false;
                Serialization.save(project, path);
            }
            while (modify[0]);

            assertFalse(modify[0]);
        }
        finally
        {
            if (watcher != null) watcher.stop();
        }
    }

    @Test
    public void testChangeFile() throws Exception
    {
        Path path = tmp.newFolder().toPath();
        Project project = getProject("test");
        Entity entity = ProjectTestUtils.createEntity(project, "entity", "ID");
        ProjectTestUtils.createQuery(entity, "All records",
                Arrays.asList('@' + SpecialRoleGroup.ALL_ROLES_EXCEPT_GUEST_GROUP, "-User"));
        Serialization.save(project, path);

        boolean modify[] = new boolean[]{false};

        WatchDir watcher = null;
        try
        {
            watcher = new WatchDir(Collections.singletonMap("main", project))
                    .onModify(onModify -> modify[0] = true)
                    .start();

            while (!modify[0])
            {
                Thread.sleep(100);

                File entityFile = path.resolve("src/meta/entities/entity.yaml").toFile();
                try(PrintWriter output = new PrintWriter(new FileWriter(entityFile,true)))
                {
                    output.printf("%s\r\n", "NEWLINE");
                }
                catch (Exception e) {}
            }

            assertTrue(modify[0]);
            watcher.stop();
        }
        finally
        {
            if (watcher != null) watcher.stop();
        }
    }
}
