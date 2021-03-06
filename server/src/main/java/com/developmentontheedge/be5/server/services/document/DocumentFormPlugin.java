package com.developmentontheedge.be5.server.services.document;

import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.FormGenerator;
import com.developmentontheedge.be5.util.JsonUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.TOP_FORM;


public class DocumentFormPlugin implements DocumentPlugin
{
    private final FormGenerator formGenerator;
    private final UserAwareMeta userAwareMeta;

    @Inject
    public DocumentFormPlugin(FormGenerator formGenerator, UserAwareMeta userAwareMeta,
                              DocumentGenerator documentGenerator)
    {
        this.formGenerator = formGenerator;
        this.userAwareMeta = userAwareMeta;
        documentGenerator.addDocumentPlugin(TOP_FORM, this);
    }

    @Override
    public ResourceData addData(Query query, Map<String, Object> parameters)
    {
        String topForm = (String) JsonUtils.getMapFromJson(query.getLayout()).get(TOP_FORM);
        if (topForm != null)
        {
            if (userAwareMeta.hasAccessToOperation(query.getEntity().getName(), query.getName(), topForm))
            {
                ResourceData operationResourceData = formGenerator.generate(query.getEntity().getName(),
                        query.getName(), topForm, parameters, Collections.emptyMap());
                operationResourceData.setId(TOP_FORM);

                return operationResourceData;
            }
        }

        return null;
    }

}
