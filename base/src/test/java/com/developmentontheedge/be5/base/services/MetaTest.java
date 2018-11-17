package com.developmentontheedge.be5.base.services;

import com.developmentontheedge.be5.base.BaseTest;
import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import org.junit.Test;

import javax.inject.Inject;

import java.util.Collection;
import java.util.List;

import static com.developmentontheedge.be5.metadata.model.EntityType.COLLECTION;
import static com.developmentontheedge.be5.metadata.model.EntityType.GENERIC_COLLECTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MetaTest extends BaseTest
{
    @Inject
    private Meta meta;

    @Test
    public void getTableReferences()
    {
        meta.getTableReferences(COLLECTION).size();

        assertEquals(1, meta.getTableReferences(COLLECTION).size());
        assertEquals(2, meta.getTableReferences(GENERIC_COLLECTION).size());

        assertEquals("testGenCollection", meta.getTableReferences(GENERIC_COLLECTION).get(0).getTableFrom());
        assertEquals("recordID", meta.getTableReferences(GENERIC_COLLECTION).get(0).getColumnsFrom());
        assertEquals(null, meta.getTableReferences(GENERIC_COLLECTION).get(0).getTableTo());
        assertEquals(null, meta.getTableReferences(GENERIC_COLLECTION).get(0).getColumnsTo());

        assertEquals("testGenCollection", meta.getTableReferences(GENERIC_COLLECTION).get(1).getTableFrom());
        assertEquals("categoryID", meta.getTableReferences(GENERIC_COLLECTION).get(1).getColumnsFrom());
        assertEquals("testtable", meta.getTableReferences(GENERIC_COLLECTION).get(1).getTableTo());
        assertEquals("ID", meta.getTableReferences(GENERIC_COLLECTION).get(1).getColumnsTo());
    }

    @Test
    public void getEntities()
    {
        List<Entity> entities = meta.getEntities();
        assertTrue(entities.stream().anyMatch(e -> e.getName().equals("testtableAdmin")));

        List<Entity> genericCollectionEntities = meta.getEntities(EntityType.GENERIC_COLLECTION);
        assertEquals(1, genericCollectionEntities.size());
        assertEquals("testGenCollection", genericCollectionEntities.get(0).getName());
    }

    @Test
    public void getOrderedEntities()
    {
        List<Entity> entities = meta.getOrderedEntities("ru");
        assertEquals("testtable", entities.get(0).getName());
        assertEquals("testtableAdmin", entities.get(1).getName());
    }

    @Test
    public void getOrderedEntity()
    {
        Entity entity = meta.getEntity("testtable");
        assertEquals("testtable", entity.getName());
    }

    @Test
    public void getColumnType() throws Exception
    {
        Entity testtableAdmin = meta.getEntity("testtableAdmin");

        assertEquals(Long.class, meta.getColumnType(testtableAdmin, "ID"));
        assertEquals(String.class, meta.getColumnType(testtableAdmin, "name"));
        assertEquals(Integer.class, meta.getColumnType(testtableAdmin, "value"));
    }

    @Test
    public void daemonsTest()
    {
        Collection<Daemon> daemons = meta.getDaemons();

        assertEquals(1, daemons.size());
        daemons.iterator().hasNext();
        Daemon daemon = daemons.iterator().next();
        assertEquals("path.to.Daemon", daemon.getClassName());
        assertEquals("periodic", daemon.getDaemonType());
    }
}
