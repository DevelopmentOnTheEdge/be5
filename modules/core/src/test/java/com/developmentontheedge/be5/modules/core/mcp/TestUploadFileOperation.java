package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.databasemodel.util.DpsUtils;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import org.apache.commons.fileupload.FileItem;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Files as the UI uploads them in a multipart request: the value of a field is the name of its file.
 */
public class TestUploadFileOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dps.add(new DynamicProperty("document", "Document", File.class));
        DynamicProperty attachments = new DynamicProperty("attachments", "Attachments", File.class);
        attachments.setAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST, true);
        attachments.setCanBeNull(true);
        dps.add(attachments);
        DynamicProperty comment = new DynamicProperty("comment", "Comment", String.class);
        comment.setCanBeNull(true);
        dps.add(comment);
        return DpsUtils.setValues(dps, presetValues);
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        DynamicPropertySet dps = (DynamicPropertySet) parameters;
        StringBuilder result = new StringBuilder(describe(getFileItem((String) dps.getValue("document"))));
        Object[] attachments = (Object[]) dps.getValue("attachments");
        if (attachments != null)
        {
            for (Object name : attachments) result.append(" + ").append(describe(getFileItem((String) name)));
        }
        setResultFinished(result + " / " + dps.getValue("comment"));
    }

    private static String describe(FileItem file)
    {
        return file.getName() + " " + file.getContentType() + " " + new String(file.get(), StandardCharsets.UTF_8);
    }
}
