package com.developmentontheedge.be5.metadata;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils
{

    private static final class DirectoryDeleter implements FileVisitor<Path>
    {
        @Override
        public FileVisitResult postVisitDirectory( Path dir, IOException e ) throws IOException
        {
            Files.deleteIfExists( dir );
            
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) throws IOException
        {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile( Path file, BasicFileAttributes arg1 ) throws IOException
        {
            Files.deleteIfExists( file );
            
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed( Path arg0, IOException arg1 ) throws IOException
        {
            return FileVisitResult.CONTINUE;
        }
    }

    public static void deleteRecursively( final Path path ) throws IOException
    {
        Files.walkFileTree( path, new DirectoryDeleter() );
    }

}
