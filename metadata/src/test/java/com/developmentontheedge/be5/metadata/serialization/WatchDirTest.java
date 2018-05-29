package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.model.Project;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Random;

import static com.developmentontheedge.be5.metadata.util.ProjectTestUtils.createScript;
import static com.developmentontheedge.be5.metadata.util.ProjectTestUtils.getProject;
import static org.junit.Assert.*;


public class WatchDirTest
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void test() throws Exception
    {
        Path path = tmp.newFolder().toPath();
        Project project = getProject("test");
        Serialization.save( project, path );

        boolean modify[] = new boolean[]{false};

        WatchDir watcher = null;
        try{
            watcher = new WatchDir(Collections.singletonMap("main", project))
                    .onModify( onModify -> modify[0] = true)
                    .start();

            while (!modify[0]){
                Thread.sleep(100);

                createScript( project, "Post-db", "INSERT INTO entity (name) VALUES ('foo')" + new Random().nextInt());
                Serialization.save( project, path );

                System.out.println(modify[0]);
            }

            assertTrue(modify[0]);
            watcher.stop();

            do{
                Thread.sleep(100);

                modify[0] = false;
                Serialization.save( project, path );
            }
            while (modify[0]);

            assertFalse(modify[0]);
        }
        finally
        {
            if(watcher != null)watcher.stop();
        }
    }
}