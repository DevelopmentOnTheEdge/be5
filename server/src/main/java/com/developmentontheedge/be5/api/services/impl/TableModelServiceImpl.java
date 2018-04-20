package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.CoreUtils;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.api.services.TableModelService;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.TableBuilder;
import com.developmentontheedge.be5.query.impl.TableModel;
import com.developmentontheedge.be5.util.LayoutUtils;

import java.util.Map;

import static com.developmentontheedge.be5.api.RestApiConstants.LIMIT;
import static com.developmentontheedge.be5.api.RestApiConstants.OFFSET;
import static com.developmentontheedge.be5.api.RestApiConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.api.RestApiConstants.ORDER_DIR;


public class TableModelServiceImpl implements TableModelService
{
    private final UserAwareMeta userAwareMeta;
    private final CoreUtils coreUtils;
    private final GroovyRegister groovyRegister;
    private final Injector injector;

    public TableModelServiceImpl(UserAwareMeta userAwareMeta, CoreUtils coreUtils, GroovyRegister groovyRegister, Injector injector)
    {
        this.userAwareMeta = userAwareMeta;
        this.coreUtils = coreUtils;
        this.groovyRegister = groovyRegister;
        this.injector = injector;
    }

    @Override
    public TableModel getTableModel(Query query, Map<String, String> parameters)
    {
        switch (query.getType())
        {
            case D1:
            case D1_UNKNOWN:
                return getSqlTableModel(query, parameters);
            case GROOVY:
                return getGroovyTableModel(query, parameters);
            default:
                throw Be5Exception.internal("Unknown action type '" + query.getType() + "'");
        }
    }

    private TableModel getSqlTableModel(Query query, Map<String, String> parameters)
    {
        int orderColumn = Integer.parseInt(parameters.getOrDefault(ORDER_COLUMN, "-1"));
        String orderDir = parameters.getOrDefault(ORDER_DIR, "asc");
        int offset      = Integer.parseInt(parameters.getOrDefault(OFFSET, "0"));
        int limit = Integer.parseInt(parameters.getOrDefault(LIMIT, Integer.toString(Integer.MAX_VALUE)));

        parameters.remove(ORDER_COLUMN);
        parameters.remove(ORDER_DIR);
        parameters.remove(OFFSET);
        parameters.remove(LIMIT);

        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        if(limit == Integer.MAX_VALUE)
        {
            //todo move defaultPageLimit, to getQuerySettings(query)
            limit = Integer.parseInt(LayoutUtils.getLayoutObject(query).getOrDefault("defaultPageLimit",
                    coreUtils.getSystemSetting("be5_defaultPageLimit", "10")).toString());
        }

        return TableModel
                .from(query, parameters, injector)
                .sortOrder(orderColumn, orderDir)
                .offset(offset)
                .limit(Math.min(limit, maxLimit))
                .build();
    }

    private TableModel getGroovyTableModel(Query query, Map<String, String> parameters)
    {
        try
        {
            Class aClass = groovyRegister.getClass(query.getEntity() + query.getName(),
                    query.getQuery(), query.getFileName());

            if(aClass != null)
            {
                TableBuilder tableBuilder = (TableBuilder) aClass.newInstance();

                tableBuilder.initialize(query, parameters);
                injector.injectAnnotatedFields(tableBuilder);

                return tableBuilder.getTableModel();
            }
            else
            {
                throw Be5Exception.internal("Class " + query.getQuery() + " is null." );
            }
        }
        catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
        {
            throw new UnsupportedOperationException( "Groovy feature has been excluded", e );
        }
    }
}
