package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import static com.developmentontheedge.be5.metadata.util.ProjectTestUtils.createOperation;
import static org.junit.Assert.assertEquals;

public class MassChangeTest
{
    @Test
    public void testMassChange()
    {
        Project project = new Project("test");
        Entity entity = new Entity("entity", project.getApplication(), EntityType.TABLE);
        DataElementUtils.save(entity);
        Query queryToChange = new Query("queryToChange", entity);
        DataElementUtils.save(queryToChange);
        Query query = new Query("query", entity);
        DataElementUtils.save(query);
        MassChange mc = new MassChange("Query[name*=\"Change\"]", project.getApplication().getMassChangeCollection(),
                Collections.singletonMap("type", QueryType.D2));
        assertEquals("type", mc.getPropertiesString());
        assertEquals("Query[name*=Change]", mc.getSelectorString());
        DataElementUtils.save(mc);
        assertEquals(QueryType.D1, queryToChange.getType());
        assertEquals(QueryType.D1, query.getType());
        LoadContext context = new LoadContext();
        project.applyMassChanges(context);
        context.check();
        assertEquals(QueryType.D2, entity.getQueries().get("queryToChange").getType());
        assertEquals(QueryType.D1, entity.getQueries().get("query").getType());
    }

    @Test
    @Ignore
    public void testMassChangeOperations()
    {
        Project project = new Project("test");
        Entity entity = new Entity("entity", project.getApplication(), EntityType.TABLE);

        Operation operation = createOperation(entity, "op");
        Operation operation2 = createOperation(entity, "op2");

        MassChange mc = new MassChange("Operation[name*=\"op\"]", project.getApplication().getMassChangeCollection(),
                Collections.singletonMap("records", 1));
        assertEquals("records", mc.getPropertiesString());
        assertEquals("Operation[name*=op]", mc.getSelectorString());
        DataElementUtils.save(mc);
        assertEquals(0, operation.getRecords());
        assertEquals(0, operation2.getRecords());
        LoadContext context = new LoadContext();
        project.applyMassChanges(context);
        context.check();
        assertEquals(1, entity.getOperations().get("op").getRecords());
        assertEquals(0, entity.getOperations().get("op2").getRecords());
    }
}
