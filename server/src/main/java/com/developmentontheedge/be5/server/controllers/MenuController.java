package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.server.helpers.MenuHelper;
import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.server.servlet.support.ApiControllerSupport;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


public class MenuController extends ApiControllerSupport implements Controller
{
    private final MenuHelper menuHelper;

    @Inject
    public MenuController(MenuHelper menuHelper)
    {
        this.menuHelper = menuHelper;
    }

    /**
     * Generated JSON sample:
     * <pre>
     * <code>
     *   { "root": [
     *     { "title": "Entity With All Records", "action": {"name":"ajax", "arg":"entity.query"} },
     *     { "title": "Some Entity", children: [
     *       { "title": "Query1", "action": {"name":"url", "arg":"https://www.google.com"} }
     *     ] }
     *   ] }
     * </code>
     * </pre>
     */
    @Override
    public void generate(Request req, Response res, String requestSubUrl)
    {
        switch (requestSubUrl)
        {
            case "":
                res.sendAsJson(new MenuResponse(menuHelper.collectEntities(false, EntityType.TABLE)));
                return;
            case "dictionary":
                res.sendAsJson(new MenuResponse(menuHelper.collectEntities(false, EntityType.DICTIONARY)));
                return;
            case "withIds":
                res.sendAsJson(new MenuResponse(menuHelper.collectEntities(true, EntityType.TABLE)));
                return;
            default:
                res.sendErrorAsJson("Unknown action", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public static class MenuResponse
    {
        final List<MenuHelper.RootNode> root;

        MenuResponse(List<MenuHelper.RootNode> root)
        {
            this.root = root;
        }

        public List<MenuHelper.RootNode> getRoot()
        {
            return root;
        }
    }
}
