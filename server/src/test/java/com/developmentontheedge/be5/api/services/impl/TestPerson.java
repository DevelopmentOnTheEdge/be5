package com.developmentontheedge.be5.api.services.impl;

import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

public class TestPerson
{
    public static final BeanHandler<TestPerson> beanHandler = new BeanHandler<>(TestPerson.class);
    public static final BeanListHandler<TestPerson> beanListHandler = new BeanListHandler<>(TestPerson.class);

    private long id;
    private String name;
    private String password;
    private String email;

    public TestPerson(){}

    public TestPerson(long id, String name, String password, String email)
    {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getPassword()
    {
        return password;
    }

    public String getEmail()
    {
        return email;
    }
}
