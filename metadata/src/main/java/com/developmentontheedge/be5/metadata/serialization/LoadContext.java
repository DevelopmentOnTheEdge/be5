package com.developmentontheedge.be5.metadata.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.developmentontheedge.be5.metadata.exception.ReadException;

import one.util.streamex.StreamEx;

public class LoadContext
{
    private final List<ReadException> warnings = new ArrayList<>();
    
    public void addWarning(ReadException ex)
    {
        throw new RuntimeException(ex);
        //warnings.add( ex );
    }
    
    public List<ReadException> getWarnings()
    {
        return Collections.unmodifiableList( warnings );
    }
    
    public void check() throws IllegalStateException
    {
        if(!warnings.isEmpty())
        {
            throw new IllegalStateException( "There are " + warnings.size() + " errors:\n" + StreamEx.of( warnings ).joining( "\n" ) );
        }
    }
}
