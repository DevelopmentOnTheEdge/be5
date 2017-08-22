package com.developmentontheedge.be5.modules.core.services

import com.developmentontheedge.be5.api.services.SqlService
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

class UtilsSystemSettingTest extends AbstractProjectIntegrationH2Test
{
    DatabaseModel database = injector.get(DatabaseModel.class)
    SqlService db = injector.getSqlService()

    CoreUtils utils = injector.get(CoreUtils.class)

    //todo Inject annotation
    //@Inject CoreUtils utils

    @Before
    void before(){
        db.update("DELETE FROM systemSettings")
    }

    @Test
    void getSystemSettingInSection() throws Exception
    {
        database.systemSettings << [ section_name: "system", setting_name: "app_name", setting_value: "Be5 Application" ]
        assertEquals "Be5 Application", utils.
                getSystemSettingInSection("system", "app_name", "Be5 Application")
    }

}