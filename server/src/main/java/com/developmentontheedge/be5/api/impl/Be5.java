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

    /**
     * This is an utility class.
     */
    private Be5() {
        throw new AssertionError(); // shouldn't be created
    }

}
