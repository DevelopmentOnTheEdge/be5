package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import one.util.streamex.StreamEx;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class QueryTest
{
    @Test
    public void testBasics() throws ProjectElementException
    {
        Project prj = new Project("test");
        Entity e = new Entity("e", prj.getApplication(), EntityType.TABLE);
        DataElementUtils.save(e);
        assertTrue(Arrays.asList(Query.getQueryTypes()).contains("1D"));
        Query query = new Query("query", e);
        query.setQuery("SELECT * FROM ${entity.getName()}");
        DataElementUtils.save(query);
        assertEquals("SELECT * FROM e", query.getFinalQuery());
        query.setType(QueryType.JAVASCRIPT);
        assertFalse(query.isFileNameHidden());
        assertTrue(query.isFromApplication());

        assertTrue(query.getErrors().isEmpty());
        query.setType(QueryType.GROOVY);
        assertTrue(query.getErrors().isEmpty());

//        query.setType( "FOO" );
//        assertEquals(1, query.getErrors().size());

        query.getOperationNames().setValuesArray(new String[]{"Insert", "Delete", "Delete", "Update"});
        assertEquals(StreamEx.of("Delete", "Insert", "Update").toSet(), query.getOperationNames().getFinalValues());
        query.removeOperation("Insert");
        assertEquals(StreamEx.of("Delete", "Update").toSet(), query.getOperationNames().getFinalValues());

        Operation op = Operation.createOperation("Test", Operation.OPERATION_TYPE_JAVA, e);
        DataElementUtils.save(op);
        query.setParametrizingOperationName("Test");
        assertSame(op, query.getParametrizingOperation());
    }

    @Test
    public void testQuickFilters()
    {
        Project prj = new Project("test");
        Entity e = new Entity("e", prj.getApplication(), EntityType.TABLE);
        DataElementUtils.save(e);
        Query qf = new Query("qf", e);
        DataElementUtils.save(qf);
        Query query = new Query("query", e);
        DataElementUtils.save(query);
        assertEquals(0, query.getQuickFilters().length);
        assertTrue(qf.getDependentElements().isEmpty());
        QuickFilter filter = new QuickFilter("filter", query);
        filter.setTargetQueryName("qf");
        DataElementUtils.save(filter);
        assertArrayEquals(new QuickFilter[]{filter}, query.getQuickFilters());
        Collection<BeModelElement> dep = qf.getDependentElements();
        assertEquals(1, dep.size());
        assertSame(filter, dep.iterator().next());
    }

    @Test
    public void testClone() throws Exception
    {
        Project prj = new Project("test");
        Entity e = new Entity("e", prj.getApplication(), EntityType.TABLE);
        DataElementUtils.save(e);
        Query query = new Query("query", e);
        DataElementUtils.save(query);
        Query query2 = (Query) query.clone(query.getOrigin(), query.getName());
        TestHelpers.checkEquality(query, query2);
    }
}
