package com.developmentontheedge.be5.metadata.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Files2
{
    private static class Copier implements FileVisitor<Path>
    {
        private int count = 0;
        private final Path source;
        private final Path target;
        private final Predicate<Path> shouldCopy;

        Copier(final Path source, final Path target)
        {
            this(source, target, path -> true);
        }

        Copier(final Path source, final Path target, final Predicate<Path> copy)
        {
            this.source = source;
            this.target = target;
            this.shouldCopy = copy;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException
        {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
        {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            final int baseNameCount = source.getNameCount();
            final int fileNameCount = file.getNameCount();
            final List<String> parts = new ArrayList<>();

            for (int i = baseNameCount; i < fileNameCount; i++)
                parts.add(file.getName(i).toString());

            if (!parts.isEmpty() && parts.get(parts.size() - 1).equals(".gitignore"))
                return FileVisitResult.CONTINUE;


            final Path targetFile = target.resolve(String.join("/", parts));

            if (shouldCopy.test(file))
            {
                Files.createDirectories(targetFile.getParent());
                Files.deleteIfExists(targetFile);
                Files.copy(file, targetFile);
                count++;
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException
        {
            return FileVisitResult.CONTINUE;
        }

        public int getCount()
        {
            return count;
        }
    }

    private static class Collector implements FileVisitor<Path>
    {

        private final Path path;
        private final Predicate<String> select;
        private final List<String> relativePaths;

        Collector(final Path path, final Predicate<String> select)
        {
            this.path = path;
            this.select = select;
            this.relativePaths = new ArrayList<>();
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
        {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
        {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            final int baseNameCount = path.getNameCount();
            final int fileNameCount = file.getNameCount();
            final List<String> parts = new ArrayList<>();

            for (int i = baseNameCount; i < fileNameCount; i++)
                parts.add(file.getName(i).toString());

            final String relativePath = String.join("/", parts);

            if (select.test(relativePath))
                relativePaths.add(relativePath);

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
        {
            return FileVisitResult.CONTINUE;
        }

        List<String> getRelativePaths()
        {
            return relativePaths;
        }
    }

    private static class DeleteFileVisitor extends SimpleFileVisitor<Path>
    {
        private int count = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) count++;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
        {
            Files.deleteIfExists(dir);
            return FileVisitResult.CONTINUE;
        }

        public int getCount()
        {
            return count;
        }
    }

    /**
     * Not intended to be instantiated.
     */
    private Files2()
    {
    }

    public static int copyAll(final Path from, final Path to) throws IOException
    {
        Copier copier = new Copier(from, to);
        Files.walkFileTree(from, copier);
        return copier.getCount();
    }

    public static int copyAll(final Path from, final Path to, final Predicate<Path> copy) throws IOException
    {
        Copier copier = new Copier(from, to, copy);
        Files.walkFileTree(from, copier);
        return copier.getCount();
    }

    public static int deleteAll(final Path from) throws IOException
    {
        DeleteFileVisitor deleteFileVisitor = new DeleteFileVisitor();
        Files.walkFileTree(from, deleteFileVisitor);
        return deleteFileVisitor.getCount();
    }

    public static String[] collectRelativePaths(final Path path, final Predicate<String> select) throws IOException
    {
        final Collector collector = new Collector(path, select);
        Files.walkFileTree(path, collector);

        return collector.getRelativePaths().toArray(new String[0]);
    }

    public static Predicate<Path> byExtension(final String extension)
    {
        final String suffix = "." + extension;
        return input -> input.getFileName().toString().endsWith(suffix);
    }

    public static boolean contentEq(final Path path, final String content)
    {
        try
        {
            return Arrays.equals(content.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(path));
        }
        catch (IOException e)
        {
            return false;
        }
    }
}
