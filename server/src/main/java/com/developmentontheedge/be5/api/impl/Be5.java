package com.developmentontheedge.be5.api.impl;

import javax.servlet.ServletContext;

import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.enterprise.DatabaseConnector;
import com.developmentontheedge.be5.api.InitializerContext;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.env.ServletContexts;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.servlets.ForwardingServletContext;

/**
 * The platform class.
 * 
 * @author asko
 */
public class Be5 {
    
    /**
     * This is an utility class.
     */
    private Be5() {
        throw new AssertionError(); // shouldn't be created
    }
    
    /**
     * Developers are free to use the initial web.xml.
     * If the web.xml wasn't patched by replacing placeholders,
     * then developer parameters will be used automatically.
     * 
     * @author asko
     */
    private static class SupportingIdeServletContext extends ForwardingServletContext {

        public SupportingIdeServletContext(ServletContext servletContext) {
            super(servletContext);
        }
        
        @Override
        public String getInitParameter(String name) {
            if (name.equals("connectString"))
            {
                String temp = super.getInitParameter(name);
                if (temp != null && temp.contains("@"))
                {
                    String replacement = super.getInitParameter("developmentConnectString");
                    assert replacement != null;
                    return replacement;
                }
            }
            
            return super.getInitParameter(name);
        }
        
    }
    
    /**
     * The singleton implementation, guarantees that the connector will be created only once and lazily
     * (when the 'getDbmsConnector()' will be called for the first time).
     */
    private static class ConnectorHolder {
        private static DbmsConnector connector = createDatabaseConnector();

        private static DbmsConnector createDatabaseConnector() {
            ServletContext servletContext = ServletContexts.getServletContext();
            ServletContext supportingIdeServletContext = new SupportingIdeServletContext(servletContext);

            DbmsConnector connector = null;//TODO Utils.findConnector(supportingIdeServletContext, null);
            
            return connector;
        }
    }
    
    /**
     * Returns a database connector for the current application.
     * @deprecated use {@link DatabaseService} that can be got from component parameters or an initializer context
     * @see Request#getDatabaseService()
     * @see InitializerContext#getServiceProvider()
     * @see ServiceProvider#getDatabaseService()
     */
    @Deprecated
    public static DbmsConnector getDatabaseConnector() {
        return ConnectorHolder.connector;
    }
    
}
