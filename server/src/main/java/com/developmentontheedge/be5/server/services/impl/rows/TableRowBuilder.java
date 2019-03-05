package com.developmentontheedge.be5.server.services.impl.rows;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.SqlBoolColumnType;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.server.model.table.CellModel;
import com.developmentontheedge.be5.server.model.table.ColumnModel;
import com.developmentontheedge.be5.server.model.table.RawCellModel;
import com.developmentontheedge.be5.server.model.table.RowModel;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.inject.Inject;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;
import static com.developmentontheedge.be5.query.util.QueryUtils.shouldBeSkipped;

public class TableRowBuilder
{
    private final UserInfoProvider userInfoProvider;
    private final CoreUtils coreUtils;

    @Inject
    TableRowBuilder(UserInfoProvider userInfoProvider, CoreUtils coreUtils)
    {
        this.userInfoProvider = userInfoProvider;
        this.coreUtils = coreUtils;
    }

    public List<RowModel> collectRows(Query query, List<QRec> list)
    {
        PropertiesToRowTransformer propertiesToRowTransformer = new PropertiesToRowTransformer();
        ArrayList<RowModel> rows = new ArrayList<>();
        for (DynamicPropertySet properties : list)
        {
            replaceBlob(properties);
            rows.add(generateRow(query, properties, propertiesToRowTransformer));
        }
        return rows;
    }

    public List<ColumnModel> collectColumns(Query query, List<QRec> list)
    {
        if (list.size() > 0)
        {
            return collectColumns(query, list.get(0));
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private List<ColumnModel> collectColumns(Query query, QRec properties)
    {
        List<ColumnModel> columns = new ArrayList<>();

        for (DynamicProperty property : properties)
        {
            if (!shouldBeSkipped(property))
            {
                String quick = getQuickOptionState(query, property);
                Boolean nosort = DynamicPropertyMeta.get(property).get("nosort") != null ? true : null;
                columns.add(new ColumnModel(
                        property.getName(),
                        property.getDisplayName(),
                        quick,
                        nosort));
            }
        }

        return columns;
    }

    private String getQuickOptionState(Query query, DynamicProperty property)
    {
        Map<String, String> quickOption = DynamicPropertyMeta.get(property).get("quick");
        if (quickOption == null) return null;

        String savedQuick = (String) coreUtils.getColumnSettingForUser(
                query.getEntity().getName(), query.getName(), property.getName(), userInfoProvider.getUserName())
                .get("quick");
        if (savedQuick != null) return savedQuick;

        if ("true".equals(quickOption.get("visible")))
            return SqlBoolColumnType.YES;
        else
            return SqlBoolColumnType.NO;
    }

    private RowModel generateRow(Query query, DynamicPropertySet properties,
                                 PropertiesToRowTransformer transformer)
            throws AssertionError
    {
        List<RawCellModel> cells = transformer.collectCells(properties); // can contain hidden cells
        List<CellModel> processedCells = processCells(cells); // only visible cells

        String rowId = transformer.getRowId(properties);
        if (query.getType() == QueryType.D1 && rowId == null)
        {
            throw Be5Exception.internal(ID_COLUMN_LABEL + " not found.");
        }
        return new RowModel(rowId, processedCells);
    }

    /**
     * Processes each cell's content and selects only visible cells.
     *
     * @param cells raw cells
     *              columns.size() == cells.size()
     */
    private List<CellModel> processCells(List<RawCellModel> cells)
    {
        List<CellModel> processedCells = new ArrayList<>();

        for (RawCellModel cell : cells)
        {
            if (!cell.hidden)
            {
                processedCells.add(new CellModel(cell.content, cell.options));
            }
        }

        return processedCells;
    }

    public static void replaceBlob(DynamicPropertySet properties)
    {
        for (DynamicProperty dp : properties)
        {
            if (dp.getValue() == null) continue;
            if (dp.getValue().getClass() == byte[].class || dp.getValue() instanceof Blob)
            {
                dp.setValue("Blob");
                dp.setType(String.class);
            }
        }
    }
}
