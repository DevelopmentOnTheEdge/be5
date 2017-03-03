package com.developmentontheedge.be5.metadata.serialization;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;

import one.util.streamex.StreamEx;

public class ModuleLoader
{
    private final List<Path> loadPaths = new ArrayList<>();
    
    public ModuleLoader(Collection<Path> paths)
    {
        loadPaths.addAll( paths );
    }
    
    public Optional<Path> resolveModule(String name)
    {
        return modulePaths()
            .map( path -> path.resolve( name ) )
            .findFirst( ProjectFileSystem::canBeLoaded );
    }
    
    public Project loadModule( String moduleName, LoadContext context ) throws ProjectLoadException
    {
        Path modulePath = resolveModule( moduleName )
                .orElseThrow( () -> new IllegalArgumentException( "Module not found: "+moduleName ) );

        return Serialization.load( modulePath, true, context );
    }
    
    public StreamEx<String> moduleNames()
    {
        return modulePaths()
            .flatMap( path -> {
                try
                {
                    return Files.list( path );
                }
                catch ( IOException e )
                {
                    throw new UncheckedIOException( e );
                }
            })
            .filter( ProjectFileSystem::canBeLoaded )
            .map( path -> path.getFileName().toString() )
            .distinct();
    }

    public boolean isModuleExists(String name)
    {
        return resolveModule(name).isPresent();
    }

    private StreamEx<Path> modulePaths()
    {
        return StreamEx.of(loadPaths).prepend( ModuleUtils.getBasePath().resolve( "modules" ) );
    }
}
