package com.developmentontheedge.be5.components.impl.model;

import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.QueryExecutor;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.Unzipper;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetAsMap;
import com.google.common.collect.ImmutableList;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CellFormatter
{
    private static final Unzipper unzipper = Unzipper.on(Pattern.compile("<sql> SubQuery# [0-9]+</sql>")).trim();
    private final Query query;
    private final UserAwareMeta userAwareMeta;
    private final QueryExecutor queryExecutor;

    CellFormatter(Query query, QueryExecutor queryExecutor, UserAwareMeta userAwareMeta, Injector injector)
    {
        this.query = query;
        this.userAwareMeta = userAwareMeta;
        this.queryExecutor = queryExecutor;
    }

    /**
     * Executes subqueries of the cell or returns the cell content itself.
     */
    String formatCell(TableModel.RawCellModel cell, DynamicPropertySet previousCells)
    {
        return format(cell, new RootVarResolver(previousCells));
    }

    private String format(TableModel.RawCellModel cell, VarResolver varResolver)
    {
        ImmutableList<Object> formattedParts = getFormattedPartsWithoutLink(cell, varResolver);

        String formattedContent = StreamEx.of(formattedParts).map(this::print).joining();

        formattedContent = userAwareMeta.getLocalizedCell(formattedContent, query.getEntity().getName(), query.getName());

        if(formattedContent != null) {//TODO && extraQuery == Be5QueryExecutor.ExtraQuery.DEFAULT

            Map<String, String> blankNullsProperties = cell.options.get(DatabaseConstants.COL_ATTR_BLANKNULLS);
            if(blankNullsProperties != null)
            {
                if( formattedContent.equals( "null" ) )
                {
                    formattedContent = blankNullsProperties.getOrDefault("value", "");
                }
            }


            Map<String, String> nullIfProperties = cell.options.get(DatabaseConstants.COL_ATTR_NULLIF);
            if(nullIfProperties != null)
            {
                if( formattedContent.equals( nullIfProperties.get("value") ) )
                {
                    formattedContent = nullIfProperties.getOrDefault("result", "");
                }
            }

            Map<String, String> linkProperties = cell.options.get(DatabaseConstants.COL_ATTR_LINK);
            if(linkProperties != null)
            {
                HashUrl url = new HashUrl("table").positional(linkProperties.get("table"))
                        .positional(linkProperties.getOrDefault("queryName", DatabaseConstants.ALL_RECORDS_VIEW));
                String cols = linkProperties.get("columns");
                String vals = linkProperties.get("using");
                if(cols != null && vals != null)
                {
                    url = url.named(EntryStream.zip(cols.split(","), vals.split(",")).mapValues(varResolver::resolve).toMap());
                }
                cell.options.put(DatabaseConstants.COL_ATTR_LINK, Collections.singletonMap("url", url.toString()));
            }

        }

        return formattedContent;
    }

    private ImmutableList<Object> getFormattedPartsWithoutLink(TableModel.RawCellModel cell, VarResolver varResolver){
        boolean hasLink = cell != null && cell.options.containsKey("link");
        Map<String, String> link = null;
        if(hasLink) {
            link = cell.options.get("link");
            cell.options.remove("link");
        }

        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        unzipper.unzip(cell != null ? cell.content : "", builder::add, subquery ->
                builder.add(toTable(subquery, varResolver))
        );

        ImmutableList<Object> formattedParts = builder.build();

        if(hasLink) {
            cell.options.put("link", link);
        }

        return formattedParts;
    }

    /**
     * Dynamically casts tables to string using default formatting;
     */
    private String print(Object formattedPart)
    {
        if (formattedPart instanceof String)
        {
            return (String) formattedPart;
        }
        else if (formattedPart instanceof List)
        {
            @SuppressWarnings("unchecked")
            List<List<Object>> table = (List<List<Object>>) formattedPart;
            //todo <br/> or ; or ... - add tag support
            return StreamEx.of(table).map(list -> StreamEx.of(list).map(this::print).joining(", ")).joining("<br/> ");
        }
        else
        {
            throw new AssertionError(formattedPart.getClass().getName());
        }
    }

    /**
     * Returns a two-dimensional listDps of processed content. Each element is either a string or a table.
     */
    private List<List<String>> toTable(String subquery, VarResolver varResolver)
    {
        try(StreamEx<DynamicPropertySet> stream = queryExecutor.executeSubQuery(subquery, varResolver)){
            return stream.map(dps -> toRow(dps, varResolver)).toList();
        }
    }

    /**
     * Transforms a set of properties to a listDps. Each element of the listDps is a string or a table.
     */
    private List<String> toRow(DynamicPropertySet dps, VarResolver varResolver)
    {
        DynamicPropertySet previousCells = new DynamicPropertySetAsMap();

        return StreamEx.of(dps.spliterator()).map(property -> {
            String name = property.getName();
            Object value = property.getValue();
            String processedCell = format(new TableModel.RawCellModel(value != null ? value.toString() : ""), new CompositeVarResolver(new RootVarResolver(previousCells), varResolver));
            previousCells.add(new DynamicProperty(name, String.class, processedCell));
            return processedCell;
        }).toList();
    }


    @FunctionalInterface
    public interface VarResolver
    {
        String resolve(String varName);
    }

    private static class RootVarResolver implements VarResolver
    {

        private final DynamicPropertySet dps;

        RootVarResolver(DynamicPropertySet dps)
        {
            this.dps = dps;
        }

        @Override
        public String resolve(String varName)
        {
            String value = dps.getValueAsString(varName);
            return value != null ? value : varName;
        }

    }

    private static class CompositeVarResolver implements VarResolver
    {

        private final VarResolver local;
        private final VarResolver parent;

        CompositeVarResolver(VarResolver local, VarResolver parent)
        {
            this.local = local;
            this.parent = parent;
        }

        @Override
        public String resolve(String varName)
        {
            String value = local.resolve(varName);

            if (value != null)
                return value;

            return parent.resolve(varName);
        }

    }

}