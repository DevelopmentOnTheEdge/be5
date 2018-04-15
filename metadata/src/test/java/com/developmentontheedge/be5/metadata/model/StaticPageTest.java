package com.developmentontheedge.be5.metadata.model;

import static com.developmentontheedge.be5.metadata.util.ProjectTestUtils.createStaticPage;
import static org.junit.Assert.*;

import org.junit.Test;


public class StaticPageTest
{
    @Test
    public void testStaticPage()
    {
        Project proj = new Project("test");
        createStaticPage(proj, "en", "page", "Content");

        assertEquals("Content", proj.getStaticPageContent( "en", "page" ));
    }
}
