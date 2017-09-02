package com.developmentontheedge.be5.modules.core.services

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.modules.core.genegate.CoreEntityModels
import com.developmentontheedge.be5.test.Be5ProjectTest
import groovy.transform.TypeChecked
import org.junit.Test

import static org.junit.Assert.*

@TypeChecked
class EntitiesTest extends Be5ProjectTest
{
    @Inject CoreEntityModels entities

    @Test
    void name()
    {
        String id = entities.users.insert{
            user_name        "test"
            attempt          345
            registrationDate new Date()
        }
        assertEquals "123", id
    }
}
