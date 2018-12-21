package com.developmentontheedge.be5.query.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.LayoutUtils;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.impl.TableBuilder;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import com.developmentontheedge.be5.query.services.TableModelService;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

import static com.developmentontheedge.be5.query.QueryConstants.LIMIT;
import static com.developmentontheedge.be5.query.QueryConstants.OFFSET;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_DIR;


public class TableModelServiceImpl implements TableModelService
{
    private final UserAwareMeta userAwareMeta;
    private final CoreUtils coreUtils;
    private final GroovyRegister groovyRegister;
    private final Injector injector;
    private final QueryExecutorFactory queryService;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public TableModelServiceImpl(UserAwareMeta userAwareMeta, CoreUtils coreUtils, GroovyRegister groovyRegister,
                                 Injector injector, QueryExecutorFactory queryService, UserInfoProvider userInfoProvider)
    {
        this.userAwareMeta = userAwareMeta;
        this.coreUtils = coreUtils;
        this.groovyRegister = groovyRegister;
        this.injector = injector;
        this.queryService = queryService;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TableModel getTableModel(Query query, Map<String, Object> parameters)
    {
        int orderColumn = Integer.parseInt((String) parameters.getOrDefault(ORDER_COLUMN, "-1"));
        String orderDir = (String) parameters.getOrDefault(ORDER_DIR, "asc");
        int offset = Integer.parseInt((String) parameters.getOrDefault(OFFSET, "0"));
        int limit = Integer.parseInt((String) parameters.getOrDefault(LIMIT, Integer.toString(Integer.MAX_VALUE)));

        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        if (limit == Integer.MAX_VALUE)
        {
            limit = Integer.parseInt(LayoutUtils.getLayoutObject(query).getOrDefault("defaultPageLimit",
                    coreUtils.getSystemSetting("be5_defaultPageLimit", "10")).toString());
        }

        try
        {
            return new TableBuilder(query, parameters, userInfoProvider.get(), queryService,
                    userAwareMeta, coreUtils, groovyRegister, injector)
                    .sortOrder(orderColumn, orderDir)
                    .offset(offset)
                    .limit(Math.min(limit, maxLimit))
                    .build();
        }
        catch (Throwable e)
        {
            throw Be5Exception.internalInQuery(query, e);
        }
    }
}
