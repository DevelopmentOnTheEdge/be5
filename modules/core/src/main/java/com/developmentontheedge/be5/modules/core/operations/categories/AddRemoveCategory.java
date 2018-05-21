package com.developmentontheedge.be5.modules.core.operations.categories;

import com.developmentontheedge.be5.modules.core.services.impl.CategoriesHelper;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operations.support.GOperationSupport;
import com.developmentontheedge.be5.util.DpsUtils;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.DynamicProperty;
import com.google.common.collect.ObjectArrays;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.beans.BeanInfoConstants.TAG_LIST_ATTR;


public class AddRemoveCategory extends GOperationSupport
{
    @Inject CategoriesHelper categoriesHelper;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        if( context.getRecords().length == 0 )
            return null;

        DynamicProperty prop = new DynamicProperty( "categoryID", "Category", Long.class );
        prop.setAttribute( TAG_LIST_ATTR, helper.getTagsFromSelectionView(
                "categories", Collections.singletonMap( "entity", getInfo().getEntityName() )) );
        dps.add( prop );

        prop = new DynamicProperty( "operationType", "Operation", String.class );

        prop.setAttribute( TAG_LIST_ATTR, new String[][]{
                {"Add", "Add to this category and parents"},
                {"Remove", "Remove from this category and children"} } );
        prop.setValue("Add");
        dps.add( prop );

        return DpsUtils.setValues(dps, presetValues);
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        if( context.getRecords().length == 0 )
        {
            setResult(OperationResult.error("No records were selected"));
            return;
        }

        Long categoryID = dps.getValueAsLong( "categoryID" );


        String entity = getInfo().getEntityName();
        String pk = getInfo().getPrimaryKey();

        if( "Add".equals( dps.getValue( "operationType" ) ) )
        {
            List<Long> categories = categoriesHelper.getParentCategories(categoryID);

            delete(entity, categories);

            db.insert("INSERT INTO classifications (recordID, categoryID)" +
                    "SELECT CONCAT('"+entity+".', e."+pk+"), c.ID " +
                    "FROM "+entity+" e, categories c " +
                    "WHERE e."+pk+" IN " + Utils.inClause(context.getRecords().length) +
                    "  AND c.ID     IN " + Utils.inClause(categories.size()),
                    ObjectArrays.concat(context.getRecords(), categories.toArray(), Object.class));
        }
        else
        {
            List<Long> categories = categoriesHelper.getChildCategories(categoryID);

            delete(entity, categories);
        }

    }

    private void delete(String entity, List<Long> categories)
    {
        db.update("DELETE FROM classifications " +
                        "WHERE recordID   IN " + Utils.inClause(context.getRecords().length) +
                        "  AND categoryID IN " + Utils.inClause(categories.size()),
                ObjectArrays.concat(
                        Utils.addPrefix(entity + ".", context.getRecords()),
                        categories.toArray(),
                        Object.class));
    }

}
