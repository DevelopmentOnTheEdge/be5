package metaui

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.metadata.QueryType
import com.developmentontheedge.be5.metadata.model.DataElementUtils
import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.operation.GOperationSupport


class AddQuery extends GOperationSupport
{
    @Inject Meta meta

    String name
    QueryType type
    String entityName

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps.add("entityName", "EntityName"){
            TAG_LIST_ATTR = [["testtable","testtable"]] as String[][]
            RELOAD_ON_CHANGE = true
        }
        dps.add("name", "name")
        dps.add("type", "type"){
            TAG_LIST_ATTR = [["1D","1D"], ["1D_unknown","1D_unknown"], ["Groovy","Groovy"]] as String[][]
            value = "1D_unknown"
        }
        dps.add("roles", "roles"){
            TAG_LIST_ATTR = [["SystemDeveloper", "SystemDeveloper"]] as String[][]
            MULTIPLE_SELECTION_LIST = true
        }

        dps.add("query", "query"){
            EXTRA_ATTRS = [["inputType", "textArea"], ["rows", "10"]] as String[][]
            value = "SELECT * FROM users"
        }

//        if ( QueryType.D1 == type || QueryType.D1_UNKNOWN == type  )
//        {
//            dps.edit("query"){
//                value = "SELECT * FROM ${entityName}"
//            }
//        }

        return dpsHelper.setValues(dps, presetValues)
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        name = dps.getValueAsString("name")
        type = QueryType.fromString(dps.getValueAsString("type"))
        entityName = dps.getValueAsString("entityName")

        final Entity entity = meta.getEntity(entityName)
        final Query query = new Query( name, entity )
        query.setType( type )
        query.setQuery( dps.getValueAsString("query") )

//        query.setInvisible( page.isInvisibleView.getSelection() );
//        query.setOriginModuleName( entity.getProject().getProjectOrigin() );

        DataElementUtils.save( query )
//
//        BeanExplorerProjectProvider.getInstance().structuralChange( query.getOrigin() );
//        BeanExplorerProjectView.selectInSharedInstance( query );
    }
}
