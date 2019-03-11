package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.server.services.ErrorModelHelper;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.servlet.support.JsonApiModelController;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.inject.Stage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;

@Singleton
public class ReloadProjectController extends JsonApiModelController
{
    private final ErrorModelHelper errorModelHelper;
    private final ProjectProvider projectProvider;
    private final Stage stage;

    @Inject
    public ReloadProjectController(ErrorModelHelper errorModelHelper, ProjectProvider projectProvider, Stage stage)
    {
        this.errorModelHelper = errorModelHelper;
        this.projectProvider = projectProvider;
        this.stage = stage;
    }

    @Override
    public JsonApiModel generateJson(Request req, Response res, String subUrl)
    {
        Map<String, String> links = Collections.singletonMap(SELF_LINK,
                new HashUrl(STATIC_ACTION, "reloadProject").toString());

        if (stage == Stage.DEVELOPMENT)
        {
            try
            {
                projectProvider.reloadProject();
                return data(new ResourceData("text", "ok", links));
            }
            catch (Be5Exception e)
            {
                return error(errorModelHelper.getErrorModel(e, links));
            }
        }
        else
        {
            return error(errorModelHelper.getErrorModel(
                    Be5Exception.internal("Only in DEVELOPMENT stage"), links));
        }
    }
}
