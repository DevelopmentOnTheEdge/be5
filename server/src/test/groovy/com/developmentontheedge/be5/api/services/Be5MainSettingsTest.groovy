package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.inject.Inject
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import org.junit.Test

import static org.junit.Assert.*


class Be5MainSettingsTest extends Be5ProjectDBTest
{
    @Inject Be5MainSettings be5MainSettings

    @Test
    void getCacheSize()
    {
        assertEquals 0, be5MainSettings.getCacheSize("User settings")
    }
}