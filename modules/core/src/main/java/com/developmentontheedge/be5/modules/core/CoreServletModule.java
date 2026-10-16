package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.modules.core.controllers.CategoriesController;
import com.developmentontheedge.be5.modules.core.controllers.SaveQuickColumnSetting;
import com.developmentontheedge.be5.modules.core.mcp.McpServlet;
import com.developmentontheedge.be5.modules.core.mcp.McpUploadServlet;
import com.developmentontheedge.be5.modules.core.mcp.oauth.McpOAuthServlet;
import com.developmentontheedge.be5.server.ServerServletModule;
import com.google.inject.servlet.ServletModule;


public class CoreServletModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        install(new ServerServletModule());
        serve("/api/categories*").with(CategoriesController.class);
        serve("/api/quick*").with(SaveQuickColumnSetting.class);
        // before /api/mcp*, which matches it too: the first matching servlet serves a request
        serve("/api/mcp/upload/*").with(McpUploadServlet.class);
        serve("/api/mcp*").with(McpServlet.class);
        // the OAuth metadata is also looked up at the root of the host (RFC 8414, RFC 9728)
        serve("/api/oauth/*", "/.well-known/oauth-protected-resource*", "/.well-known/oauth-authorization-server*")
                .with(McpOAuthServlet.class);
    }
}
