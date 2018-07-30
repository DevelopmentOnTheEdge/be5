package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;

import javax.inject.Inject;
import java.util.Map;


public class DocumentFormPlugin
{
    private final UserInfoProvider userInfoProvider;
    private final UserAwareMeta userAwareMeta;

    @Inject
    public DocumentFormPlugin(UserInfoProvider userInfoProvider, UserAwareMeta userAwareMeta)
    {
        this.userInfoProvider = userInfoProvider;
        this.userAwareMeta = userAwareMeta;
    }

    private ResourceData getJsonApiModel(Query query, Map<String, Object> parameters, TableModel tableModel)
    {
        //List<ResourceData> included = new ArrayList<>();

//todo add as plugin
//        String topForm = (String) ParseRequestUtils.getValuesFromJson(query.getLayout()).get(TOP_FORM);
//        if (topForm != null)
//        {
//            Optional<TableOperationPresentation> topFormOperationPresentation =
//                    data.getOperations().stream().filter(x -> x.getName().equals(topForm)).findAny();
//
//            if (topFormOperationPresentation.isPresent())
//            {
//                ResourceData operationResourceData = formGenerator.generate(query.getEntity().getName(), query.getName(), topForm, new String[]{}, parameters, Collections.emptyMap());
//                operationResourceData.setId("topForm");
//
//                included.add(operationResourceData);
//
//                data.getOperations().remove(topFormOperationPresentation.get());
//            }
//        }

        return null;
    }

}
