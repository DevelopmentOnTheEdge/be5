package com.developmentontheedge.be5.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.equinox.launcher.Main;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet(description = "Routing requests", urlPatterns = { "/api/*" }, loadOnStartup = 1)
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Object mainServletImpl = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
        super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		/*
		 * Preconditions.
		 */
		String connectString = config.getServletContext().getInitParameter("connectString");
		String dataSource = config.getServletContext().getInitParameter("dataSource");
		if (connectString == null && dataSource == null)
		{
		    /*
		     * Add a context parameter to the web.xml, e.g.:
		     * <web-app ...>
		     *   [...]
		     * 
		     *   <context-param>
		     *     <param-name>connectString</param-name>
		     *     <param-value>jdbc:postgresql://localhost:5432/condo;user=condo;password=condo</param-value>
		     *   </context-param>
		     *   
		     *   [...]
		     * </web-app>
		     */
		    throw new IllegalStateException("Either 'connectString' or 'dataSource' context parameter is required. Add it to the web.xml.");
		}
		
		/*
		 * This property is required to load the project.
		 */
        System.getProperties().put("com.beanexplorer.be5.servletContext", config.getServletContext());
		
        /*
         * Eclipse runtime options, see here:
         * http://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fmisc%2Fruntime-options.html
         */
		final List<String> args = new ArrayList<>();
		args.add("-install");
        String realPath = config.getServletContext().getRealPath("/");
        if(realPath == null)
        {
            throw new IllegalStateException(
                    "Cannot initialize the application: config.getServletContext().getRealPath(\"/\") returned null! Probably \"unpackWars\" is \"false\"." );
        }
        args.add(realPath);
		args.add("-application");
		args.add("com.beanexplorer.be5.empty");
		args.add("-noExit");
		
		/*
		 * This thread is required to run an OSGi application,
		 * because the OSGi environment lives while an application
		 * handling its events, so running this main method would block
		 * the current thread.
		 */
        new Thread("OSGi environment thread") {
            @Override
            public void run() {
                try
                {
                    Main.main(args.toArray(new String[args.size()]));
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }.start();
        
        try {
        	while(System.getProperties().get("com.beanexplorer.be5.platformClass") == null) {
        		Thread.sleep(100);
        	}
        	WebSocketServlet.setMain(getMainServletImpl());
        	Reflection.on(getMainServletImpl()).call("init", config);
		} catch (Exception e) {
			throw new RuntimeException("Unable to get main servlet class", e);
		}
	}

	/**
	 * @see Servlet#destroy()
	 */
	@Override
	public void destroy() {
		System.getProperties().put("com.beanexplorer.be5.exit", "true");
		try {
			Reflection.on(getMainServletImpl()).call("destroy");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    respond(request, response);
	}
	
	/**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        respond(request, response);
    }
    
    private void respond(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try
        {
    	    Object mainServletImpl = getMainServletImpl();
            Reflection.on(mainServletImpl).call("respond", request, response, request.getMethod(), request.getRequestURI(), request.getParameterMap());
	    }
        catch (Exception e)
        {
	        throw new ServletException(e);
	    }
        
        return;
    }

    private Object getMainServletImpl() throws Exception {
        if (mainServletImpl == null)
        {
            Class<?> platformClass = (Class<?>) System.getProperties().get("com.beanexplorer.be5.platformClass");
            Object bundle = Reflection.on(platformClass).call("getBundle", "com.beanexplorer.be5");
            Class<?> mainServletImplClass = (Class<?>) Reflection.on(bundle).call("loadClass", "com.beanexplorer.enterprise.servlets.MainServletImpl");
            mainServletImpl = mainServletImplClass.newInstance();
        }
        
        return mainServletImpl;
    }

}
