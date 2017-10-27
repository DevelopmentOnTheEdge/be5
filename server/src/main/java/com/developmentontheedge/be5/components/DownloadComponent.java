package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Injector;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Example url
 * api/download?_t_=attachments&_typeColumn_=mimeType&_charsetColumn_=mimeCharset&_filenameColumn_=name&_dataColumn_=data&_download_=yes&ID=7326
 */
public class DownloadComponent implements Component
{

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        String entity         = req.getNonEmpty("_t_");
        String ID             = req.get("ID");
        String typeColumn     = req.get("_typeColumn_");
        String filenameColumn = req.get("_filenameColumn_");
        String dataColumn     = req.getNonEmpty("_dataColumn_");
        String charsetColumn  = req.get("_charsetColumn_");
        //String encoding       = req.get("_enc_");
        boolean download      = "yes".equals(req.get("_download_"));

        RecordModel record = injector.get(DatabaseModel.class).getEntity(entity).get(ID);


        String filename = record.getValueAsString(filenameColumn);
        String contentType = record.getValueAsString(typeColumn);
        String charset = MoreObjects.firstNonNull(record.getValueAsString(charsetColumn), Charsets.UTF_8.name());
        Object data = record.getValue(dataColumn);
        InputStream in;


        if (data instanceof byte[])
        {
            in = new ByteArrayInputStream((byte[])data);
        }
//        else if (data instanceof Blob)
//        {
//            in = ((Blob) data).getBinaryStream();
//        }
//        else if (data instanceof String)
//        {
//            in = new ByteArrayInputStream(((String) data).getBytes(charset));
//        }
        else
        {
            throw Be5Exception.internal("Unknown data type");
        }

        HttpServletResponse response = res.getRawResponse();

        response.setContentType(contentType + "; charset=" + charset);
        //response.setCharacterEncoding(encoding);

        if (download)
        {
            response.setHeader("Content-disposition","attachment; filename=" + UrlEscapers.urlFormParameterEscaper().escape(filename));
        }
        else
        {
            response.setHeader("Content-disposition","filename=" + UrlEscapers.urlFormParameterEscaper().escape(filename));
        }

        try
        {
            ByteStreams.copy(in, response.getOutputStream());
        }
        catch (IOException e)
        {
            throw Be5Exception.internal(e);
        }
    }

}
