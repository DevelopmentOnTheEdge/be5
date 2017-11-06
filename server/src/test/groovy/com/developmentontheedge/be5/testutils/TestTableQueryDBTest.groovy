package com.developmentontheedge.be5.testutils

import com.developmentontheedge.be5.api.services.ProjectProvider
import com.developmentontheedge.be5.api.services.SqlService
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.env.Injector
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import org.junit.Before
import org.junit.BeforeClass

abstract class TestTableQueryDBTest extends Be5ProjectDBTest
{
    @Inject public SqlService db
    @Inject public ProjectProvider projectProvider
    @Inject public Injector injector

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
