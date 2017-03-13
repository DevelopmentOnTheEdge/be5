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
	private MainServletImpl mainServletImpl = null;
       
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
		 * This property is required to load the project.
		 */
        System.getProperties().put("com.developmentontheedge.be5.servletContext", config.getServletContext());
		
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
		args.add("com.developmentontheedge.be5.empty");
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
        	while(System.getProperties().get("com.developmentontheedge.be5.platformClass") == null) {
        		Thread.sleep(100);
        	}
        	WebSocketServlet.setMain(getMainServletImpl());
            getMainServletImpl().init(config);
		} catch (Exception e) {
			throw new RuntimeException("Unable to get main servlet class", e);
		}
	}

	/**
	 * @see Servlet#destroy()
	 */
	@Override
	public void destroy() {
		System.getProperties().put("com.developmentontheedge.be5.exit", "true");
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
            MainServletImpl mainServletImpl = getMainServletImpl();
            mainServletImpl.respond(request, response, request.getMethod(), request.getRequestURI(), request.getParameterMap());
	    }
        catch (Exception e)
        {
	        throw new ServletException(e);
	    }
        
        return;
    }

    private MainServletImpl getMainServletImpl() throws Exception {
        if (mainServletImpl == null)
        {
			mainServletImpl = new MainServletImpl();
        }
        return mainServletImpl;
    }

}
