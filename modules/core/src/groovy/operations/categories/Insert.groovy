package categories

import com.developmentontheedge.be5.operation.GOperationSupport

class Insert extends GOperationSupport
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpForColumns(dps, getInfo().getEntity(),
                ["entity", "name", "parentID", "description"], presetValues)

        def entities = meta.getOrderedEntities(userInfo.getLanguage())

        //todo utils for entitiesTags, tags as one dimensional array String[]
        def list = new ArrayList<String[]>()
        for(def entity : entities){
            list.add([entity.getName(), entity.getName()] as String[])
        }

        dps.edit("entity") {
            TAG_LIST_ATTR = list as String[][]
        }

        return dpsHelper.setOperationParams(dps, context.operationParams)
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        database.categories.add(dps)
    }
}
