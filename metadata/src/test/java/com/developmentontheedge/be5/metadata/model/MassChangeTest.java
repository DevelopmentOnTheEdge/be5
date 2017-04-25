package com.developmentontheedge.be5.metadata.model;

import static org.junit.Assert.*;

import java.util.Collections;

import com.developmentontheedge.be5.metadata.QueryType;
import org.junit.Ignore;
import org.junit.Test;

import com.developmentontheedge.be5.metadata.serialization.LoadContext;

public class MassChangeTest
{
    @Test
    @Ignore
    public void testMassChange()
    {
        Project project = new Project( "test" );
        Entity entity = new Entity( "entity", project.getApplication(), EntityType.TABLE );
        DataElementUtils.save(entity);
        Query queryToChange = new Query( "queryToChange", entity );
        DataElementUtils.save(queryToChange);
        Query query = new Query( "query", entity );
        DataElementUtils.save(query);
        MassChange mc = new MassChange( "Query[name*=\"Change\"]", project.getApplication().getMassChangeCollection(),
                Collections.singletonMap( "type", QueryType.D2 ) );
        assertEquals("type", mc.getPropertiesString());
        assertEquals("Query[name*=Change]", mc.getSelectorString());
        DataElementUtils.save(mc);
        assertEquals(QueryType.D1, queryToChange.getType());
        assertEquals(QueryType.D1, query.getType());
        LoadContext context = new LoadContext();
        project.applyMassChanges( context );
        context.check();
        assertEquals(QueryType.D2, entity.getQueries().get( "queryToChange" ).getType());
        assertEquals(QueryType.D1, entity.getQueries().get( "query" ).getType());
    }
}
