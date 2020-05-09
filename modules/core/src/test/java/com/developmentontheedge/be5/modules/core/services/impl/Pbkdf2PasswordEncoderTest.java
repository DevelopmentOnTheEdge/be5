package com.developmentontheedge.be5.modules.core.services.impl;

import org.junit.Test;
import org.junit.Ignore;

import java.util.Base64;

import static org.junit.Assert.*;

public class Pbkdf2PasswordEncoderTest
{
    @Test
    @Ignore 
    public void check() throws Exception
    {
        Pbkdf2PasswordEncoder authentication = new Pbkdf2PasswordEncoder( 1, 8 );
        String saltedHash = new Pbkdf2PasswordEncoder( 1, 8 ).encode("test".toCharArray());
        assertEquals(16, Base64.getDecoder().decode(saltedHash.split("\\$")[0]).length);
        assertEquals(true, authentication.check("test".toCharArray(), saltedHash));
        assertEquals(false, authentication.check("test2".toCharArray(), saltedHash));
    }
}
