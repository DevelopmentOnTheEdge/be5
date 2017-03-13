package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.util.Delegator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;

public class LegacyServletConfig implements ServletConfig {
	private final String name;

	public LegacyServletConfig(String name) {
		this.name = name;
	}

	@Override
	public String getServletName() {
		return name;
	}

	@Override
	public ServletContext getServletContext() {
	    Object origServletContext = System.getProperties().get("com.developmentontheedge.be5.servletContext");
	    return Delegator.on(origServletContext, ServletContext.class);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return Collections.emptyEnumeration();
	}

	@Override
	public String getInitParameter(String arg) {
		return null;
	}
}