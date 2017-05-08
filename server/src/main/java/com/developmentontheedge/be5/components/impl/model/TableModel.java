package com.developmentontheedge.be5.components.impl.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetAsMap;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.QueryExecutor;
import com.developmentontheedge.be5.metadata.model.Query;

/**
 * // TODO localizer
 */
public class TableModel
{
    public static class Builder
    {
        private final Query query;
        private final QueryExecutor execution;
        private boolean selectable;
        private int limit = Integer.MAX_VALUE;
        private final int sortColumn;
        private final boolean desc;
        private final UserAwareMeta userAwareMeta;

        private Builder(Query query, Map<String, String> parametersMap, Request req, ServiceProvider serviceProvider, boolean selectable)
        {
            this.query = query;
            this.selectable = selectable;
            this.sortColumn = req.getInt("order[0][column]", -1) + (selectable ? -1 : 0);
            this.desc = "desc".equals(req.get("order[0][dir]"));
            this.execution = new Be5QueryExecutor(query, parametersMap, req, serviceProvider);
            this.execution.sortOrder(sortColumn, desc);
            this.userAwareMeta = UserAwareMeta.get(serviceProvider);
        }

        public Builder offset(int offset)
        {
            this.execution.offset( offset );
            return this;
        }

        public Builder limit(int limit)
        {
            this.execution.limit( limit );
            this.limit = limit;
            return this;
        }

        public long count()
        {
            return execution.count();
        }

        public TableModel build()
        {
            List<ColumnModel> columns = new ArrayList<>();
            List<RowModel> rows = new ArrayList<>();

            collectColumnsAndRows( query.getEntity().getName(), query.getName(), execution.execute(), selectable, columns, rows, limit );

            boolean hasAggregate = addAggregateRowIfNeeded(rows);

            filterWithRoles(columns, rows);

            return new TableModel( selectable, columns, rows, rows.size() < limit ? (long)rows.size() : null , hasAggregate);
        }

        /*
        * com.beanexplorer.enterprise.query.TabularFragmentBuilder.filterBeanWithRoles()
        * */
        void filterWithRoles(List<ColumnModel> columns, List<RowModel> rows){
            if(rows.size() == 0)return;
            List<String> currRoles = UserInfoHolder.getCurrentRoles();

            List<CellModel> firstLine = rows.get(0).cells;
            for (int i = firstLine.size()-1; i >= 0; i--) {
                Map<String, String> columnRoles = firstLine.get(i).options.get(DatabaseConstants.COL_ATTR_ROLES);

                if( columnRoles == null )
                {
                    continue;
                }

                String roles = columnRoles.get("name");
                List<String> roleList = Arrays.asList( roles.split( "," ) );
                List<String> forbiddenRoles = roleList.stream().filter(x -> x.startsWith("!")).collect(Collectors.toList());

                roleList.removeAll( forbiddenRoles );

                boolean hasAccess = false;

                if(roleList.stream().anyMatch(currRoles::contains))
                {
                    hasAccess = true;
                }

                if (!hasAccess && !forbiddenRoles.isEmpty() && currRoles.stream().anyMatch(x -> !forbiddenRoles.contains(x)))
                {
                    hasAccess = true;
                }

                if( !hasAccess )
                {
                    for (RowModel rowModel : rows) {
                        rowModel.getCells().remove(i);
                    }
                    columns.remove(i);
                }
            }
        }

        private boolean addAggregateRowIfNeeded(List<RowModel> rows)
        {
            if(rows.size() == 0 || rows.get(0).cells.stream()
                    .noneMatch(x -> x.options.containsKey(DatabaseConstants.COL_ATTR_AGGREGATE)))return false;

            List<RowModel> aggregateRow = new ArrayList<>();

            collectColumnsAndRows( query.getEntity().getName(), query.getName(), execution.executeAggregate(), selectable, new ArrayList<>(), aggregateRow, limit );

            List<CellModel> firstLine = aggregateRow.get(0).cells;
            double[] resD = new double[firstLine.size()];

            for (RowModel row : aggregateRow)
            {
                for (int i = 0; i < firstLine.size(); i++)
                {
                    Map<String, String> aggregate = firstLine.get(i).options.get(DatabaseConstants.COL_ATTR_AGGREGATE);
                    if(aggregate != null) {
                        Double add;
                        if (row.getCells().get(i).content instanceof List) {
                            add = Double.parseDouble((String)((List) ((List) row.getCells().get(i).content).get(0)).get(0) );
                        } else {
                            add = Double.parseDouble((String) row.getCells().get(i).content);
                        }
                        if("Number".equals(aggregate.get("type")))
                        {
                            switch (aggregate.get("function")) {
                                case "COUNT":
                                    resD[i]++;
                                    break;
                                case "SUM":
                                case "AVG":
                                    resD[i] += add;
                                    break;
                                default:
                                    throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
                            }
                        }
                        else
                        {
                            throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
                        }
                    }
                }
            }
            for (int i = 0; i < firstLine.size(); i++)
            {
                Map<String, String> aggregate = firstLine.get(i).options.get(DatabaseConstants.COL_ATTR_AGGREGATE);
                if(aggregate != null)
                {
                    if("Number".equals(aggregate.get("type")))
                    {
                        switch (aggregate.get("function")) {
                            case "SUM":
                            case "COUNT":
                                break;
                            case "AVG":
                                resD[i] /= aggregateRow.size();
                                break;
                            default:
                                throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
                        }
                    }
                    else
                    {
                        throw Be5Exception.internal("aggregate not support function: " + aggregate.get("function"));
                    }
                }
            }
            List<CellModel> res = new ArrayList<>();
            for (int i = 0; i < firstLine.size(); i++)
            {
                Map<String, String> aggregate = firstLine.get(i).options.get(DatabaseConstants.COL_ATTR_AGGREGATE);
                Map<String, Map<String, String>> options = new HashMap<>();
                Object content = "";
                if (aggregate != null)
                {
                    content = resD[i];
                    options.put("css", Collections.singletonMap("class", aggregate.getOrDefault("cssClass","")));
                    options.put("format", Collections.singletonMap("mask", aggregate.getOrDefault("format", "")));
                }
                else
                {
                    if(i==0)
                    {
                        content = userAwareMeta.getColumnTitle(query.getEntity().getName(), query.getName(),
                                "total").orElse("total");
                    }
                }
                res.add(new CellModel(content, options));
            }

            rows.add(new RowModel("aggregate", res));
            return true;
        }

        /**
         * @param maxRows rows per page
         */
        private void collectColumnsAndRows(String entityName, String queryName, List<DynamicPropertySet> stream, boolean selectable, List<ColumnModel> columns,
                                           List<RowModel> rows, int maxRows)
        {
            for (DynamicPropertySet properties : stream) {
                if (columns.isEmpty()) {
                    columns.addAll(new PropertiesToRowTransformer(entityName, queryName, properties, userAwareMeta).collectColumns());
                }
                rows.add(generateRow(entityName, queryName, selectable, properties, columns));
            }
        }

        private RowModel generateRow(String entityName, String queryName, boolean selectable, DynamicPropertySet properties, List<ColumnModel> columns) throws AssertionError
        {
            PropertiesToRowTransformer transformer = new PropertiesToRowTransformer(entityName, queryName, properties, userAwareMeta);
            List<RawCellModel> cells = transformer.collectCells(); // can contain hidden cells
            addRowClass(cells);
            List<CellModel> processedCells = processCells( cells ); // only visible cells
            String id = selectable ? transformer.getRowId() : null;

            return new RowModel( id, processedCells );
        }

        private void addRowClass(List<RawCellModel> cells)
        {
            Optional<String> addClassName = cells.stream().filter(x -> x.name.equals(DatabaseConstants.CSS_ROW_CLASS))
                    .map(x -> x.content).findFirst();

            if(addClassName.isPresent())
            {
                for (RawCellModel cell: cells)
                {
                    if(cell.options.get("grouping") != null)continue;
                    Map<String, String> css = cell.options.putIfAbsent("css", new HashMap<>());
                    if(css == null) css = cell.options.get("css");

                    String className = css.getOrDefault("class", "");
                    css.put("class", className + " " + addClassName.get());
                }
            }
        }

        /**
         * Processes each cell's content and selects only visible cells.
         * @param cells raw cells
         * columns.size() == cells.size()
         */
        private List<CellModel> processCells(List<RawCellModel> cells)
        {
            List<CellModel> processedCells = new ArrayList<>();
            DynamicPropertySet previousCells = new DynamicPropertySetAsMap();

            for (RawCellModel cell : cells)
            {
                Object processedContent = execution.formatCell(cell, previousCells);
                previousCells.add(new DynamicProperty(cell.name, processedContent == null ? String.class
                        : processedContent.getClass(), processedContent));
                if (!cell.hidden)
                {
                    processedCells.add(new CellModel(processedContent, cell.options));
                }
            }

            return processedCells;
        }

    }

    public static Builder from(Query query, Map<String, String> parametersMap, Request req, ServiceProvider serviceProvider)
    {
        return from( query, parametersMap, req, serviceProvider, false);
    }

    public static Builder from(Query query, Map<String, String> parametersMap, Request req, ServiceProvider serviceProvider, boolean selectable)
    {
        return new Builder( query, parametersMap, req, serviceProvider, selectable);
    }

    public static class ColumnModel
    {
        private final String title;
        private final String name;

        ColumnModel(String name, String title)
        {
            Objects.requireNonNull( title );
            this.title = title;
            this.name = name;
        }


        public String getName()
        {
            return name;
        }

        public String getTitle()
        {
            return title;
        }
    }

    public static class RowModel
    {
        private final List<CellModel> cells;
        private final String id;

        /**
         * @param id can be null
         */
        RowModel(String id, List<CellModel> cells)
        {
            Objects.requireNonNull( cells );
            this.id = id;
            this.cells = cells;
        }

        public List<CellModel> getCells()
        {
            return cells;
        }

        /**
         * Returns an identifier. Never returns null.
         * @throws NullPointerException if null
         */
        public String getId()
        {
            return Objects.requireNonNull( id );
        }
    }

    /**
     * Can be legacy descriptional cell.
     *
     * @author asko
     */
    public static class RawCellModel
    {
        public final String name;
        public final String content;
        public final Map<String, Map<String, String>> options;
        public final boolean hidden;

        RawCellModel(String name, String content, Map<String, Map<String, String>> options, boolean hidden)
        {
            this.name = name;
            this.content = content;
            this.options = options;
            this.hidden = hidden;
        }

    }

    /**
     * Result rendered cell.
     *
     * @author asko
     */
    public static class CellModel
    {
        /**
         * A string or a list of strings.
         */
        public final Object content;
        public final Map<String, Map<String, String>> options;

        CellModel(Object content, Map<String, Map<String, String>> options)
        {
            this.content = content;
            this.options = options;
        }

    }

    private final boolean selectable;
    private final List<ColumnModel> columns;
    private final List<RowModel> rows;
    private final Long totalNumberOfRows;
    private final boolean hasAggregate;

    private TableModel(boolean selectable, List<ColumnModel> columns, List<RowModel> rows, Long totalNumberOfRows, boolean hasAggregate)
    {
        this.selectable = selectable;
        this.columns = Collections.unmodifiableList( columns );
        this.rows = Collections.unmodifiableList( rows );
        this.totalNumberOfRows = totalNumberOfRows;
        this.hasAggregate = hasAggregate;
    }

    public boolean isSelectable()
    {
        return selectable;
    }

    public List<ColumnModel> getColumns()
    {
        return columns;
    }

    /**
     * Returns prepared rows. Note that this table doesn't contain all the SQL table rows.
     * @see TableModel#getTotalNumberOfRows()
     */
    public List<RowModel> getRows()
    {
        return rows;
    }

    /**
     * Counts all rows.
     */
    public Long getTotalNumberOfRows()
    {
        return totalNumberOfRows;
    }

    public boolean isHasAggregate() {
        return hasAggregate;
    }
}
