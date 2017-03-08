package com.developmentontheedge.be5.components.impl.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.components.impl.model.TableModel.ColumnModel;
import com.developmentontheedge.be5.components.impl.model.TableModel.RawCellModel;
import com.developmentontheedge.be5.util.MoreStrings;

/**
 * Parses properties in terms of tables.
 */
class PropertiesToRowTransformer
{

    private final String entityName;
    private final String queryName;
    private final DynamicPropertySet properties;
    private final UserAwareMeta userAwareMeta;
    private final Object localizer; // TODO
    private SimpleDateFormat dateFormatter = new SimpleDateFormat( "dd.MM.yyyy" );
    private SimpleDateFormat timestampFormatter = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
    
    /**
     * 
     * @param properties represent a row
     * @param localizer 
     */
    PropertiesToRowTransformer(String entityName, String queryName, DynamicPropertySet properties, UserAwareMeta userAwareMeta, Object localizer)
    {
        this.entityName = entityName;
        this.queryName = queryName;
        this.properties = properties;
        this.userAwareMeta = userAwareMeta;
        this.localizer = localizer;
    }

    /**
     * Returns a row identifier or empty string if the given properties contains no identifier.
     */
    String getRowId()
    {
        Object idObject = properties.getValue( DatabaseConstants.ID_COLUMN_LABEL );

        if( idObject != null )
        {
            return MapParamHelper.mapNameOut( String.valueOf( idObject ) );
        }

        return ""; // error
    }

    /**
     * Constructs and collects columns. Note that this list contains no hidden columns (of any kind).
     */
    List<ColumnModel> collectColumns()
    {
        List<ColumnModel> columns = new ArrayList<>();

        for( DynamicProperty property : properties )
        {
            if( !shouldBeSkipped( property ) ) {
                columns.add(new ColumnModel(property.getName(), userAwareMeta.getColumnTitle(entityName, queryName, property.getName()).orElse(property.getName())));
            }
        }

        return columns;
    }

    /**
     * Glues and constructs cells.
     * @see RecordEx#preprocessProperties(DatabaseConnector, List<DynamicProperty>, Map<String, DynamicProperty>)
     */
    List<RawCellModel> collectCells()
    {
//        TODO support: collect all values, GLUE_COLUMN_PREFIX
//        // mutable map of properties
//        Map<String, StringBuilder> temp = new LinkedHashMap<>();
//
//        // see RecordEx#preprocessProperties/3
//        for( DynamicProperty property : properties )
//        {
//            String name = property.getName();
//            if( name.startsWith( RecordEx.GLUE_COLUMN_PREFIX ) )
//            {
//                appendProperty( property, temp );
//                continue;
//            }
//            temp.put( name, new StringBuilder( toString( property ) ) );
//        }
//
//        List<RawCellModel> cells = new ArrayList<>();
//
//        // collect all values
//        for( Entry<String, StringBuilder> entry : temp.entrySet() )
//        {
//            String cellName = entry.getKey();
//            String cellContent = entry.getValue().toString();
//            boolean hidden = shouldBeSkipped( cellName );
//            cells.add( new RawCellModel( cellName, localizer.localize( cellContent ), null, hidden ) );
//        }

        List<RawCellModel> cells = new ArrayList<>();

        for( DynamicProperty property : properties )
        {
            String cellName = property.getName();
            String cellContent = toString(property);
            // TODO
            // cellContent = localizer.localize( cellContent );
            boolean hidden = shouldBeSkipped( cellName );
            
            cells.add(new RawCellModel(cellName, cellContent, DynamicPropertyMeta.get(property), hidden));
        }

        return cells;
    }

    private void appendProperty(DynamicProperty property, Map<String, StringBuilder> properties)
    {
        String targetName = property.getName().substring( DatabaseConstants.GLUE_COLUMN_PREFIX.length() );
        StringBuilder mutableStr = properties.get( targetName );

        if( mutableStr == null )
        {
            throw new IllegalStateException( "Expected column '" + targetName + "'" );
        }

        mutableStr.append( toString( property ) );
    }

    private String toString(DynamicProperty property)
    {
        Object value = property.getValue();
        if(value == null)return "";

        if(property.getType() == java.sql.Date.class){
            return dateFormatter.format(value);
        }

        if(property.getType() == java.sql.Time.class){
            String timestamp = timestampFormatter.format(value);
            if(timestamp.startsWith("01.01.1970"))
            {
                timestamp = timestamp.substring(11);
            }
            return timestamp;
        }

        return value.toString();
    }

    private boolean shouldBeSkipped(DynamicProperty property)
    {
        return shouldBeSkipped( property.getDisplayName() );
    }

    private boolean shouldBeSkipped(String propertyName)
    {
        return MoreStrings.startsWithAny( propertyName, DatabaseConstants.EXTRA_HEADER_COLUMN_PREFIX, DatabaseConstants.HIDDEN_COLUMN_PREFIX,
        		DatabaseConstants.GLUE_COLUMN_PREFIX );
    }

}
