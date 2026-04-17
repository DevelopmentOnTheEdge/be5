package com.developmentontheedge.be5.mcpserver;

import com.google.inject.AbstractModule;

public class McpModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(SchemaService.class);
        bind(McpService.class);
        bind(McpController.class);
    }
}