package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.Be5MainSettings;

public class Be5MainSettingsImpl implements Be5MainSettings
{
    @Override
    public int getCacheSize()
    {
        return 1000;
    }
}
