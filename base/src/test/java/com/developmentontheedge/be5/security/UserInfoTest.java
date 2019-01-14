package com.developmentontheedge.be5.security;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import static java.util.Collections.singletonList;

public class UserInfoTest
{
    @Test
    public void testSerializable() throws Exception
    {
        UserInfo ui = new UserInfo("test", singletonList("test"), singletonList("test"));
        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(ui);
    }
}
