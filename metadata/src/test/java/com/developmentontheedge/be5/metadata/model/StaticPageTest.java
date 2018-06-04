package com.developmentontheedge.be5.metadata.model;

import org.junit.Test;

import static com.developmentontheedge.be5.metadata.util.ProjectTestUtils.createStaticPage;
import static org.junit.Assert.assertEquals;


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
