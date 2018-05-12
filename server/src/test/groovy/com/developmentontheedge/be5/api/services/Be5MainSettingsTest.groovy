package com.developmentontheedge.be5.api.services

import org.junit.Ignore

import javax.inject.Inject
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest
import org.junit.Test

import static org.junit.Assert.*


class Be5MainSettingsTest extends ServerBe5ProjectDBTest
{
    @Inject Be5MainSettings be5MainSettings

    @Test
    @Ignore//TODO
    void getCacheSize()
    {
        assertEquals 0, be5MainSettings.getCacheSize("User settings")
    }
}