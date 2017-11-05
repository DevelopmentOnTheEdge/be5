package com.developmentontheedge.be5.entitygen

import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.Be5ProjectTest
import org.junit.Test


class DatabaseModelTest extends Be5ProjectTest
{
    @Inject DatabaseModel databaseModel

    @Test
    void insert()
    {
        databaseModel.countries << [
                "ID": "23",
                "name": "test",
                "telCode": "test"
        ]

        databaseModel.categories.get("4")

    }

}
