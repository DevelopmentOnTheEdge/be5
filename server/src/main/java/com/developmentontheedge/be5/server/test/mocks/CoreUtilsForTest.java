package com.developmentontheedge.be5.server.test.mocks;

import com.developmentontheedge.be5.api.services.CoreUtils;

import java.util.Map;

public class CoreUtilsForTest implements CoreUtils
{
    @Override
    public String getSystemSettingInSection(String section, String param, String defValue)
    {
        return null;
    }

    @Override
    public void setSystemSettingInSection(String section, String param, String value)
    {

    }

    @Override
    public Map<String, String> getSystemSettingsInSection(String section)
    {
        return null;
    }

    @Override
    public String getSystemSetting(String param)
    {
        return null;
    }

    @Override
    public String getSystemSetting(String param, String defValue)
    {
        return defValue;
    }

    @Override
    public boolean getBooleanSystemSetting(String param, boolean defValue)
    {
        return defValue;
    }

    @Override
    public boolean getBooleanSystemSetting(String param)
    {
        return false;
    }

    @Override
    public String getModuleSetting(String module, String param)
    {
        return null;
    }

    @Override
    public String getModuleSetting(String module, String param, String defValue)
    {
        return defValue;
    }

    @Override
    public boolean getBooleanModuleSetting(String module, String param, boolean defValue)
    {
        return defValue;
    }

    @Override
    public boolean getBooleanModuleSetting(String module, String param)
    {
        return false;
    }

    @Override
    public String getUserSetting(String user, String param)
    {
        return null;
    }

    @Override
    public void setUserSetting(String user, String param, String value)
    {

    }

    @Override
    public void removeUserSetting(String user, String param)
    {

    }

    @Override
    public String getSystemSettingInSection(String section, String param)
    {
        return null;
    }
}
