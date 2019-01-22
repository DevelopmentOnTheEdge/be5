package com.developmentontheedge.be5.web.impl;

import com.google.common.collect.ObjectArrays;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @see <a href="http://www.javapractices.com/topic/TopicAction.do?Id=221">Wrap file upload requests</a>
 */
public class FileUploadWrapper extends HttpServletRequestWrapper
{
    /**
     * Constructor.
     */
    public FileUploadWrapper(HttpServletRequest aRequest)
    {
        super(aRequest);
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        try
        {
            List<FileItem> fileItems = upload.parseRequest(aRequest);
            convertToMaps(fileItems);
        }
        catch (FileUploadException ex)
        {
            throw new IllegalArgumentException("Cannot parse underlying request: " + ex.toString());
        }
    }

    /**
     * Return all request parameter names, for both regular controls and file upload
     * controls.
     */
    @Override
    public Enumeration<String> getParameterNames()
    {
        Set<String> allNames = new LinkedHashSet<>();
        allNames.addAll(fRegularParams.keySet());
        allNames.addAll(fFileParams.keySet());
        return Collections.enumeration(allNames);
    }

    /**
     * Return the parameter value. Applies only to regular parameters, not to
     * file upload parameters.
     * <p>
     * <P>If the parameter is not present in the underlying request,
     * then <tt>null</tt> is returned.
     * <P>If the parameter is present, but has no  associated value,
     * then an empty string is returned.
     * <P>If the parameter is multivalued, return the first value that
     * appears in the request.
     */
    @Override
    public String getParameter(String aName)
    {
        String[] values = fRegularParams.get(aName);
        if (values == null)
        {
            return null;
        }
        else if (values.length == 0)
        {
            //param name known, but no values present
            return "";
        }
        else
        {
            //return first value in list
            return values[FIRST_VALUE];
        }
    }

    /**
     * Return the parameter values. Applies only to regular parameters,
     * not to file upload parameters.
     */
    @Override
    public String[] getParameterValues(String aName)
    {
        return fRegularParams.get(aName);
    }

    /**
     * Return a {@code Map<String, List<String>>} for all regular parameters.
     * Does not return any file upload parameters at all.
     */
    @Override
    public Map<String, String[]> getParameterMap()
    {
        return Collections.unmodifiableMap(fRegularParams);
    }

    /**
     * Return a {@code List<FileItem>}, in the same order as they appear
     * in the underlying request.
     */
    public List<FileItem> getFileItems()
    {
        return new ArrayList<FileItem>(fFileParams.values());
    }

    /**
     * Return the {@link FileItem} of the given name.
     * <P>If the name is unknown, then return <tt>null</tt>.
     */
    public FileItem getFileItem(String aFieldName)
    {
        return fFileParams.get(aFieldName);
    }

    // PRIVATE

    /**
     * Store regular params only. May be multivalued (hence the List).
     */
    private final Map<String, String[]> fRegularParams = new LinkedHashMap<>();

    /**
     * Store file params only.
     */
    private final Map<String, FileItem> fFileParams = new LinkedHashMap<>();
    private static final int FIRST_VALUE = 0;

    private void convertToMaps(List<FileItem> aFileItems)
    {
        for (FileItem item : aFileItems)
        {
            if (isFileUploadField(item))
            {
                fFileParams.put(item.getFieldName(), item);
                fRegularParams.put(item.getFieldName(), new String[]{item.getName()});
            }
            else
            {
                if (alreadyHasValue(item))
                {
                    addMultivaluedItem(item);
                }
                else
                {
                    addSingleValueItem(item);
                }
            }
        }
    }

    private boolean isFileUploadField(FileItem aFileItem)
    {
        return !aFileItem.isFormField();
    }

    private boolean alreadyHasValue(FileItem aItem)
    {
        return fRegularParams.get(aItem.getFieldName()) != null;
    }

    private void addSingleValueItem(FileItem aItem)
    {
        fRegularParams.put(aItem.getFieldName(), new String[]{aItem.getString()});
    }

    private void addMultivaluedItem(FileItem aItem)
    {
        String[] values = fRegularParams.get(aItem.getFieldName());
        fRegularParams.put(aItem.getFieldName(), ObjectArrays.concat(values, aItem.getString()));
    }
}
