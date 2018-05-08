package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Controller;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.MenuHelper;

import com.developmentontheedge.be5.api.impl.ControllerSupport;
import com.developmentontheedge.be5.metadata.model.EntityType;

import com.google.inject.Inject;
import java.util.List;


public class Menu extends ControllerSupport implements Controller
{
    private final MenuHelper menuHelper;

    @Inject
    public Menu(MenuHelper menuHelper)
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
    public void generate(Request req, Response res)
    {
        switch (req.getRequestUri())
        {
            case "":
                res.sendAsRawJson(new MenuResponse(menuHelper.collectEntities(false, EntityType.TABLE)));
                return;
            case "dictionary":
                res.sendAsRawJson(new MenuResponse(menuHelper.collectEntities(false, EntityType.DICTIONARY)));
                return;
            case "withIds":
                res.sendAsRawJson(new MenuResponse(menuHelper.collectEntities(true, EntityType.TABLE)));
                return;
            default:
                res.sendUnknownActionError();
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
