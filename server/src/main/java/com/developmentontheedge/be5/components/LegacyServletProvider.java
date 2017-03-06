package com.developmentontheedge.be5.components;

import javax.servlet.http.HttpServlet;

public interface LegacyServletProvider
{
	public HttpServlet get(String name);
}