package com.developmentontheedge.sql.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AstTableRefTest {

    @Test
    public void as() throws Exception {
        assertEquals("users AS \"mainUsers\"",
                AstTableRef.as("users", "mainUsers").toString());
    }

    @Test
    public void as1() throws Exception {
        assertEquals("users AS \"mainUsers\"",
                AstTableRef.as(new AstTableName( "users" ),
                        new AstIdentifierConstant( "mainUsers", true )).toString());
    }

    @Test
    public void as2() throws Exception {
        assertEquals("users AS mainUsers",
                AstTableRef.as(new AstTableName( "users" ),
                        new AstIdentifierConstant( "mainUsers", false )).toString());
    }

}