package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.TableBuilder;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class EntitiesTest extends AbstractProjectTest
{
    @Test
    public void get() throws Exception
    {
        Query query = injector.getMeta()
                .getQueryIgnoringRoles("_system_", "Entities");
        try
        {
            TableBuilder tableBuilder = (TableBuilder) GroovyRegister.parseClass(query.getQuery()).newInstance();
            TableModel tableModel = tableBuilder
                    .initialize(query, new HashMap<>(), getMockRequest(""), injector)
                    .get();

            assertTrue(tableModel.getRows().size() > 0);
        }
        catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
        {
            throw Be5Exception.internal(e);
        }
    }

}