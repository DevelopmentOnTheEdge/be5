package com.developmentontheedge.be5.metadata.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class StaticPageTest
{
    @Test
    public void testStaticPage()
    {
        Project proj = new Project("test");
        LanguageStaticPages lsp = new LanguageStaticPages( "en", proj.getApplication().getStaticPageCollection() );
        DataElementUtils.save( lsp );
        StaticPage staticPage = new StaticPage("page", lsp);
        DataElementUtils.save( staticPage );
        staticPage.setComment( "Comment" );
        staticPage.setContent( "Content" );
        assertEquals("Content", proj.getStaticPageContent( "en", "page" ));
    }
}
