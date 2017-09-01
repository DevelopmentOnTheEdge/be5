package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.services.Be5MainSettings
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test
import org.junit.Test

import static org.junit.Assert.*


class Be5MainSettingsImplTest extends AbstractProjectIntegrationH2Test
{
    @Inject Be5MainSettings be5MainSettings

    @Test
    void getCacheSize()
    {
        assertEquals 0, be5MainSettings.getCacheSize("User settings")
    }
}