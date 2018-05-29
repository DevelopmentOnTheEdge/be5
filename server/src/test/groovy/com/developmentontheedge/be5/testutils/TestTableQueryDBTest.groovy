package com.developmentontheedge.be5.testutils

import com.developmentontheedge.be5.base.services.ProjectProvider
import com.developmentontheedge.be5.database.DbService

import javax.inject.Inject
import com.developmentontheedge.be5.server.test.ServerBe5ProjectDBTest
import org.junit.Before

abstract class TestTableQueryDBTest extends ServerBe5ProjectDBTest
{
    @Inject public DbService db
    @Inject public ProjectProvider projectProvider

    @Before
    void testTableQueryDBTestBefore()
    {
        db.update("delete from testtable")
        db.insert("insert into testtable (name, value) VALUES (?, ?)","tableModelTest", "1")

        db.update("delete from testtUser")
        db.insert("insert into testtUser (name, value) VALUES (?, ?)","tableModelTest", "user1")
        db.insert("insert into testtUser (name, value) VALUES (?, ?)","tableModelTest", "user2")
    }
}
