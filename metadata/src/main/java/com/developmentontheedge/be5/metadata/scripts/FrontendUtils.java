package com.developmentontheedge.be5.metadata.scripts;

import com.developmentontheedge.be5.metadata.util.Files2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class FrontendUtils
{
    private static final Logger log = Logger.getLogger(FrontendUtils.class.getName());

    public static void copyToWebApp(String baseDir)
    {
        String out = "";
        Path base = Paths.get(baseDir);
        Path frontendBuildDirectory = base.resolve("target-frontend");
        Path templatesDir = Paths.get(baseDir, "/src/main/webapp/WEB-INF/templates");
        Path staticDir = Paths.get(baseDir, "/src/main/webapp/static");

        try
        {
            if (templatesDir.toFile().isDirectory())
            {
                int count = Files2.deleteAll(templatesDir);
                out += "\nDeleting " + count + " files in directory " + templatesDir.toAbsolutePath();
            }
            if (staticDir.toFile().isDirectory())
            {
                int count = Files2.deleteAll(staticDir);
                out += "\nDeleting " + count + " files in directory " + staticDir.toAbsolutePath();
            }

            int htmlCount = Files2.copyAll(frontendBuildDirectory, templatesDir, f -> f.toString().endsWith(".html"));
            out += "\nCopying " + htmlCount + " files to " + templatesDir.toAbsolutePath();
            int staticCount = Files2.copyAll(frontendBuildDirectory.resolve("static"), staticDir);
            out += "\nCopying " + staticCount + " files to " + staticDir.toAbsolutePath();
            log.info(out);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
