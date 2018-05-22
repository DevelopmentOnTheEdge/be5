package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DataSourceService;
import com.developmentontheedge.be5.api.services.DbService;
import org.junit.Before;
import org.junit.Rule;
import org.zapodot.junit.db.EmbeddedDatabaseRule;

import java.util.logging.LogManager;

import static org.zapodot.junit.db.EmbeddedDatabaseRule.CompatibilityMode.PostgreSQL;


public abstract class Be5ProjectDbBaseTest
{
    @Rule
    public final EmbeddedDatabaseRule databaseRule = EmbeddedDatabaseRule.builder()
            .withMode(PostgreSQL)
            .withInitialSql("CREATE TABLE persons ( id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, password VARCHAR(255) NOT NULL, email VARCHAR(255), age INT);")
            .build();

    protected DbService db;

    @Before
    public void setUpDb()
    {
        DataSourceService databaseService = new DataSourceServiceTestImpl(databaseRule.getDataSource());

        db = new DbServiceImpl(new ConnectionServiceImpl(databaseService), databaseService);
    }

    static {
        LogManager.getLogManager().reset();
    }

}
