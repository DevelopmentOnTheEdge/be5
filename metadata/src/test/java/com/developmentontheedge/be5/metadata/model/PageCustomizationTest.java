package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PageCustomizationTest
{
    @Test
    public void testDomains()
    {
        Project prj = new Project("test");
        Entity e = new Entity( "e", prj.getApplication(), EntityType.TABLE );
        DataElementUtils.save( e );
        Operation operation = Operation.createOperation( "op", Operation.OPERATION_TYPE_JAVA, e );
        DataElementUtils.save( operation );
        Query q = new Query( "q", e );
        DataElementUtils.save( q );
        StaticPage sp = new StaticPage( "sp", prj.getApplication().getStaticPageCollection() );
        DataElementUtils.save( sp );
        List<String> domains = Arrays.asList(PageCustomization.getDomains( prj.getApplication() ));
        assertTrue(domains.contains( PageCustomization.DOMAIN_INDEX ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_STATIC_PAGE_FOOTER ));
        domains = Arrays.asList(PageCustomization.getDomains( e ));
        assertTrue(domains.contains( PageCustomization.DOMAIN_OPERATION ));
        assertTrue(domains.contains( PageCustomization.DOMAIN_QUERY ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_INDEX ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_STATIC_PAGE_FOOTER ));
        domains = Arrays.asList(PageCustomization.getDomains( operation ));
        assertTrue(domains.contains( PageCustomization.DOMAIN_OPERATION ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_QUERY ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_INDEX ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_STATIC_PAGE_FOOTER ));
        domains = Arrays.asList(PageCustomization.getDomains( q ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_OPERATION ));
        assertTrue(domains.contains( PageCustomization.DOMAIN_QUERY ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_INDEX ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_STATIC_PAGE_FOOTER ));
        domains = Arrays.asList(PageCustomization.getDomains( sp ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_OPERATION ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_QUERY ));
        assertFalse(domains.contains( PageCustomization.DOMAIN_INDEX ));
        assertTrue(domains.contains( PageCustomization.DOMAIN_STATIC_PAGE_FOOTER ));
    }
    
    @Test
    public void testBasics() throws ProjectElementException
    {
        Project prj = new Project("test");
        Entity e = new Entity( "e", prj.getApplication(), EntityType.TABLE );
        DataElementUtils.save( e );
        Operation operation = Operation.createOperation( "op", Operation.OPERATION_TYPE_JAVA, e );
        DataElementUtils.save( operation );
        PageCustomization pcProj = new PageCustomization( "css", PageCustomization.DOMAIN_QUERY_HEADER, prj.getApplication().getOrCreateCollection(
                PageCustomization.CUSTOMIZATIONS_COLLECTION, PageCustomization.class ) );
        assertEquals("query.header.css", pcProj.getName());
        PageCustomization pcEnt = new PageCustomization( "css", PageCustomization.DOMAIN_QUERY_HEADER, e.getOrCreateCollection(
                PageCustomization.CUSTOMIZATIONS_COLLECTION, PageCustomization.class ) );
        assertEquals("query.header.e.css", pcEnt.getName());
        PageCustomization pcOp = new PageCustomization( "css", PageCustomization.DOMAIN_OPERATION_BUTTONS_HEADER, operation.getOrCreateCollection(
                PageCustomization.CUSTOMIZATIONS_COLLECTION, PageCustomization.class ) );
        PageCustomization pcOp2 = new PageCustomization( "css", PageCustomization.DOMAIN_OPERATION_BUTTONS_HEADER, operation.getOrCreateCollection(
                PageCustomization.CUSTOMIZATIONS_COLLECTION, PageCustomization.class ) );
        DataElementUtils.save( pcOp );
        assertEquals("operation.buttons.header.e.op.css", pcOp.getName());
        
        pcOp.setCode( "Hello ${project.getName()}!" );
        assertEquals("Hello test!", pcOp.getResult().validate());
        
        pcOp.setRoles( Arrays.asList( "Administrator", "Guest" ) );
        assertArrayEquals( new String[] {"Administrator", "Guest"}, pcOp.getRolesArray() );
        pcOp2.setRoles( Arrays.asList("User") );
        assertFalse(pcOp.merge( pcOp2 ));
        assertNotEquals( pcOp, pcOp2 );
        assertArrayEquals( new String[] {"Administrator", "Guest"}, pcOp.getRolesArray() );
        pcOp2.setCode( pcOp.getCode() );
        assertNotEquals( pcOp, pcOp2 );
        assertTrue(pcOp.merge( pcOp2 ));
        assertArrayEquals( new String[] {"Administrator", "Guest", "User"}, pcOp.getRolesArray() );
        pcOp2.merge( pcOp );
        assertEquals( pcOp, pcOp2 );
        assertTrue(pcOp.getErrors().isEmpty());
        PageCustomization pcOpErr = new PageCustomization( "css", PageCustomization.DOMAIN_QUERY_FOOTER, operation.getOrCreateCollection(
                PageCustomization.CUSTOMIZATIONS_COLLECTION, PageCustomization.class ) );
        pcOpErr.setCode( "" );
        DataElementUtils.save(pcOpErr);
        assertEquals(1, pcOpErr.getErrors().size());
    }
}
