package com.developmentontheedge.be5.components;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import com.developmentontheedge.be5.api.services.Logger;

public class LegacyServletProviderImpl implements LegacyServletProvider
{
    final Map<String, HttpServlet> legacyServlets = new HashMap<>();

    public LegacyServletProviderImpl(Logger logger)
    {
        /** TODO
        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        for (IConfigurationElement element : extensionRegistry
                .getConfigurationElementsFor("com.developmentontheedge.enterprise.components.LegacyServlet"))
        {
            try
            {
                Class<? extends HttpServlet> aClass = Platform.getBundle(element.getContributor().getName())
                        .loadClass(element.getAttribute("class"))
                        .asSubclass(HttpServlet.class);

                legacyServlets.put(element.getAttribute("name"), aClass.newInstance());
            }
            catch (InvalidRegistryObjectException | InstantiationException | IllegalAccessException
                    | ClassNotFoundException e)
            {
                logger.error(e);
            }
        }
        */
    }

    public HttpServlet get(String name)
    {
        return legacyServlets.get(name);
    }
}