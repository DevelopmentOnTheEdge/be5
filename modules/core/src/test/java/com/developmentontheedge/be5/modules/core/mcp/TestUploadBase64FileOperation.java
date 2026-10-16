package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.databasemodel.util.DpsUtils;
import com.developmentontheedge.be5.server.model.Base64File;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A file as the UI sends it in a field of the type Base64File.
 */
public class TestUploadBase64FileOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dps.add(new DynamicProperty("picture", "Picture", Base64File.class));
        return DpsUtils.setValues(dps, presetValues);
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        Base64File file = (Base64File) ((DynamicPropertySet) parameters).getValue("picture");
        setResultFinished(file.getName() + " " + file.getMimeTypes() + " "
                + new String(file.getData(), StandardCharsets.UTF_8));
    }
}
