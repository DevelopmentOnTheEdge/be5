package com.developmentontheedge.be5.base.services.impl;

import com.developmentontheedge.be5.base.BaseTest;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.sql.format.dbms.Dbms;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;


public class DataSourceServiceImplTest extends BaseTest
{
    @Inject private DataSourceService dataSourceService;

    @Test
    public void test() throws Exception
    {
        BasicDataSource dataSource = (BasicDataSource)dataSourceService.getDataSource();
        assertEquals("jdbc:h2:~/profileForIntegrationTestsServer", dataSource.getUrl());
        assertEquals("sa", dataSource.getUsername());

        assertEquals(Dbms.H2, dataSourceService.getDbms());
    }

}