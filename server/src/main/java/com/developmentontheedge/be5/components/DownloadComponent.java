package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;

public class DownloadComponent implements Component
{

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
//        List<String> segments = Urls.extractPath(req.getRequestUri());
//        String entityName = segments.get(0);
//        String queryName = segments.get(1);
//        Query query = injector.getProject().getEntity(entityName).getQueries().get(queryName);
//        Map<String, String> legacyGetParameters = QueryStrings.extract(query.getQuery());
//        String entity         = legacyGetParameters.get("_t_");
//        String name           = legacyGetParameters.get("name");
//        String typeColumn     = legacyGetParameters.get("_typeColumn_");
//        String filenameColumn = legacyGetParameters.get("_filenameColumn_");
//        String dataColumn     = legacyGetParameters.get("_dataColumn_");
//        String charsetColumn  = legacyGetParameters.get("_charsetColumn_");
//        String encoding       = legacyGetParameters.get("_enc_");
//        boolean download      = legacyGetParameters.get("_download_").equals("yes");
//        DynamicPropertySet record;
//
//        try
//        {
//            record = Utils.getRecordById(injector.getDbmsConnector(), entity, "name", name, "", null);
//        }
//        catch (NoRecord e1)
//        {
//            // do nothing
//            return;
//        }
//        catch (SQLException e1)
//        {
//            throw Be5Exception.internal(e1);
//        }
//
//        String filename = record.getValueAsString(filenameColumn);
//        String contentType = record.getValueAsString(typeColumn);
//        String charset = MoreObjects.firstNonNull(record.getValueAsString(charsetColumn), Charsets.UTF_8.name());
//        Object data = record.getValue(dataColumn);
//        InputStream in;
//
//        try
//        {
//            if (data instanceof Blob)
//            {
//                in = ((Blob) data).getBinaryStream();
//            }
//            else if (data instanceof String)
//            {
//                in = new ByteArrayInputStream(((String) data).getBytes(charset));
//            }
//            else
//            {
//                throw Be5Exception.internal("Unknown data type");
//            }
//        }
//        catch (SQLException e)
//        {
//            throw Be5Exception.internal(e);
//        }
//        catch (UnsupportedEncodingException e)
//        {
//            throw Be5Exception.internal(e);
//        }
//
//        HttpServletResponse response = res.getRawResponse();
//
//        response.setContentType(contentType);
//        response.setCharacterEncoding(encoding);
//
//        if (download)
//        {
//            response.setHeader("Content-disposition","attachment; filename=" + UrlEscapers.urlFormParameterEscaper().escape(filename));
//        }
//
//        try
//        {
//            ByteStreams.copy(in, response.getOutputStream());
//        }
//        catch (IOException e)
//        {
//            throw Be5Exception.internal(e);
//        }
    }

}
