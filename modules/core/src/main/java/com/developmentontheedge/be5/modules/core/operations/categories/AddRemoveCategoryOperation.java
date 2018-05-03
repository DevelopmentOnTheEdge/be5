package com.developmentontheedge.be5.modules.core.operations.categories;

import com.developmentontheedge.be5.operation.GOperationSupport;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.DynamicProperty;
import com.google.common.collect.ObjectArrays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.beans.BeanInfoConstants.TAG_LIST_ATTR;


public class AddRemoveCategoryOperation extends GOperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        if( context.getRecords().length == 0 )
            return null;

        DynamicProperty prop = new DynamicProperty( "categoryID", "Category", String.class );
        prop.setShortDescription( "Category to be assigned or removed" );
        prop.setAttribute( TAG_LIST_ATTR, helper.getTagsFromSelectionView(
                "categories", Collections.singletonMap( "entity", getInfo().getEntityName() )) );
        dps.add( prop );

        prop = new DynamicProperty( "operationType", "Operation", String.class );
        prop.setShortDescription( "Whether to add or remove" );
        //todo support prop.setAttribute( TAG_LIST_ATTR, new String[]{ "Add", "Remove" } );
        prop.setAttribute( TAG_LIST_ATTR, new String[][]{ {"Add", "Add"}, {"Remove", "Remove"} } );
        dps.add( prop );

        prop = new DynamicProperty( "parents", "Repeat for parent categories", Boolean.class );
        dps.add( prop );

        return dpsHelper.setValues(dps, presetValues);
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        if( context.getRecords().length == 0 )
        {
            setResult(OperationResult.error("No records were selected"));
            return;
        }

        String category = dps.getValueAsString( "categoryID" );

        List<Long> categories;

        if((boolean)dps.getValue( "parents" ))
        {
            categories = getWithParentCategories(category);
        }
        else
        {
            categories = Collections.singletonList(Long.parseLong(category));
        }

        //String catList = Utils.toInClause( categories, isNumericCategoryID );

        String entity = getInfo().getEntityName();
        String pk = getInfo().getPrimaryKey();

        if( "Add".equals( dps.getValue( "operationType" ) ) )
        {
            db.insert("INSERT INTO classifications (recordID, categoryID)" +
                    "SELECT CONCAT('"+entity+".', e."+pk+"), c.ID " +
                    "FROM "+entity+" e, categories c " +
                    "WHERE e."+pk+" IN " + Utils.inClause(context.getRecords().length) +
                    "  AND c.ID     IN " + Utils.inClause(categories.size()),
                    ObjectArrays.concat(
                            Utils.changeTypes(context.getRecords(), meta.getColumnType(getInfo().getEntity(), pk)),
                            categories.toArray(),
                            Object.class));

//            out.write( localizedMessage( "Category" ) + " " + catList + " " +
//                    localizedMessage( "was added to" ) + " <b>" + updateCount + "</b> " +
//                    localizedMessage( "records in" ) + " <i>" + entity + "</i><br />" );
            return;
        }
        db.update("DELETE FROM classifications " +
                  "WHERE recordID   IN " + Utils.inClause(context.getRecords().length) +
                  "  AND categoryID IN " + Utils.inClause(categories.size()),
                  ObjectArrays.concat(
                        Utils.addPrefix(context.getRecords(), entity + "."),
                        categories.toArray(),
                        Object.class));

//        out.write( localizedMessage( "Category" ) + " " + catList + " " +
//                localizedMessage( "was removed from" ) + " <b>" + updateCount + "</b> " +
//                localizedMessage( "records in" ) + " <i>" + entity + "</i><br />" );
    }

    private List<Long> getWithParentCategories(String category)
    {
        List<Long> categories = new ArrayList<>();
        Long cat = category != null ? Long.parseLong(category) : null;

        while (cat != null) {
            categories.add(cat);

            cat = db.getLong("SELECT c1.parentID FROM categories c1 WHERE c1.ID = ?", cat);
        }

        return categories;
    }

}
