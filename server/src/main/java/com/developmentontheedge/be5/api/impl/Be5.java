package com.developmentontheedge.be5.api.impl;

import javax.servlet.ServletContext;

import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.be5.api.InitializerContext;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.env.ServletContexts;
import com.developmentontheedge.be5.servlets.ForwardingServletContext;
import com.developmentontheedge.dbms.SimpleConnector;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The platform class.
 * 
 * @author asko
 */
public class Be5 {
    public static Logger log = Logger.getLogger(Be5.class.getName());
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
        private static DbmsConnector connector = createDbmsConnector();

        private static DbmsConnector createDbmsConnector() {
            ServletContext servletContext = ServletContexts.getServletContext();
            ServletContext supportingIdeServletContext = new SupportingIdeServletContext(servletContext);

            DbmsConnector connector = findConnector(supportingIdeServletContext);//TODO Utils.findConnector(supportingIdeServletContext, null);
            
            return connector;
        }
    }
    
    /**
     * Returns a database connector for the current application.
     * @deprecated use {@link DatabaseService} that can be got from component parameters or an initializer context
     * @see ServiceProvider#getDatabaseService()
     */
    @Deprecated
    public static DbmsConnector getDbmsConnector() {
        return ConnectorHolder.connector;
    }

    /**
     * Creating DB connector.
     *
     * @param sc servlet context
     * @return DbmsConnector
     */
    public static DbmsConnector findConnector(ServletContext sc )
    {
        String dataSource = sc.getInitParameter( "dataSource" );
        String connectString = sc.getInitParameter( "connectString" );
        String connectionCharset = sc.getInitParameter( "dbConnectionCharset" );
        boolean forceDateFormat = sc.getInitParameter( "forceDateFormatInConnector" ) != null
                && Arrays.asList( "TRUE", "YES", "1", "ON" ).contains( sc.getInitParameter( "forceDateFormatInConnector" ).toUpperCase() );

        try
        {
            //TODO connect username password
            return new SimpleConnector(Rdbms.getRdbms(connectString).getType(), connectString, "TODO","");
        }
        catch (SQLException e)
        {
            log.log(Level.SEVERE, "not create SimpleConnector", e);
            return null;
        }
    }
    
}
