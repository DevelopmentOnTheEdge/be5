package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.api.services.Be5MainSettings;

public class Be5MainSettingsForTest implements Be5MainSettings
{
    @Override
    public int getCacheSize(String name) {
        return 0;
    }
}
