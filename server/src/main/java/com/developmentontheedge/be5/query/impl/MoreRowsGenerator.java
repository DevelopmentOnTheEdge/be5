package com.developmentontheedge.be5.query.impl;

import java.util.List;
import java.util.Map;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.query.impl.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Query;

/**
 * Generates some JSON for deferred loading of data using automatic Ajax calls.
 * This class gets and generates JSON compatible with DataTables jQuery plug-in.
 * DataTables documentation: http://www.datatables.net/examples/server_side/defer_loading.html
 * 
 * @author asko
 */
@Deprecated
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
        Map<String, String> parametersMap = req.getValuesFromJsonAsStrings(RestApiConstants.VALUES);
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
        Query query = injector.get(UserAwareMeta.class).getQuery(entityName, queryName);
        
        return generateMoreRows(query, req, parametersMap, selectable, draw, start, length, totalNumberOfRows);
    }
    
    /**
     * Runs a query and generates some more rows.
     */
    private MoreRows generateMoreRows(Query query, Request parameters, Map<String, String> parametersMap, boolean selectable, int draw, int offset, int limit, int totalNumberOfRows) {
        List<List<Object>> rows = runForMoreRows(query, parameters, parametersMap, selectable, offset, limit);
        //draw,
        return new MoreRows(totalNumberOfRows, totalNumberOfRows, rows);
    }
    
    /**
     * Runs a query and generates some more rows.
     * @param limit 
     */
    private List<List<Object>> runForMoreRows(Query query, Request req, Map<String, String> parametersMap, boolean selectable, int offset, int limit) {
        TableModel table = TableModel
                .from(query, parametersMap, injector)
                .sortOrder(req.getInt("order[0][column]", -1), "desc".equals(req.get("order[0][dir]")))
                .offset(offset)
                .limit(limit)
                .build();

        return new MoreRowsBuilder(selectable).build(table);
    }
    
}
