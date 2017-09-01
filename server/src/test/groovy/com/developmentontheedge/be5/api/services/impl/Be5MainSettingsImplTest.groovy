package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.services.Be5MainSettings
import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test
import org.junit.Test

import static org.junit.Assert.*


class Be5MainSettingsImplTest extends AbstractProjectIntegrationH2Test
{
    @Test
    void getCacheSize()
    {
        def get = injector.get(Be5MainSettings.class)
        assertEquals 0, get.getCacheSize("User settings")
    }
}