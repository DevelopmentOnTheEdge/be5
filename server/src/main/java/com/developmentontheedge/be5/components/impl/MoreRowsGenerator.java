package com.developmentontheedge.be5.components.impl;

import java.util.List;
import java.util.Map;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Query;

/**
 * Generates some JSON for deferred loading of data using automatic Ajax calls.
 * This class gets and generates JSON compatible with DataTables jQuery plug-in.
 * DataTables documentation: http://www.datatables.net/examples/server_side/defer_loading.html
 * 
 * @author asko
 */
public class MoreRowsGenerator {
    
    private final Injector injector;

    public MoreRowsGenerator(Injector injector) {
        this.injector = injector;
    }
    
    /**
     * Generates more rows for a table.
     * @throws IllegalArgumentException
     */
    public MoreRows generate(Request req) {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        Map<String, String> parametersMap = req.getStringValues(RestApiConstants.VALUES);
        String startStr = req.getNonEmpty(RestApiConstants.START);
        String lengthStr = req.getNonEmpty(RestApiConstants.LENGTH);
        String drawStr = req.getNonEmpty(RestApiConstants.DRAW);
        String selectableStr = req.getNonEmpty(RestApiConstants.SELECTABLE);
        String totalNumberOfRowsStr = req.getNonEmpty(RestApiConstants.TOTAL_NUMBER_OF_ROWS);
        
        int draw;
        int start;
        int totalNumberOfRows;
        int length;
        
        try
        {
            draw = Integer.parseInt(drawStr);
            start = Integer.parseInt(startStr);
            length = Integer.parseInt(lengthStr);
            totalNumberOfRows = Integer.parseInt(totalNumberOfRowsStr);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException();
        }
        
        boolean selectable = Boolean.parseBoolean(selectableStr);
        Query query = UserAwareMeta.get(injector).getQuery(entityName, queryName);
        
        return generateMoreRows(query, req, parametersMap, selectable, draw, start, length, totalNumberOfRows);
    }
    
    /**
     * Runs a query and generates some more rows.
     */
    private MoreRows generateMoreRows(Query query, Request parameters, Map<String, String> parametersMap, boolean selectable, int draw, int offset, int limit, int totalNumberOfRows) {
        List<List<Object>> rows = runForMoreRows(query, parameters, parametersMap, selectable, offset, limit);
        
        return new MoreRows(draw, totalNumberOfRows, totalNumberOfRows, rows);
    }
    
    /**
     * Runs a query and generates some more rows.
     * @param limit 
     */
    private List<List<Object>> runForMoreRows(Query query, Request req, Map<String, String> parametersMap, boolean selectable, int offset, int limit) {
        TableModel table = TableModel.from(injector, query, parametersMap, req, selectable).offset(offset).limit(limit).build();
        return new MoreRowsBuilder(selectable).build(table);
    }
    
}
