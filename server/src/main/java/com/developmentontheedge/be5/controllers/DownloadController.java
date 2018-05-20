package com.developmentontheedge.be5.controllers;

import com.developmentontheedge.be5.api.Controller;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.support.ControllerSupport;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.databasemodel.RecordModel;
import com.developmentontheedge.be5.api.services.databasemodel.DatabaseModel;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * Example url
 * api/download?_t_=attachments&_typeColumn_=mimeType&_charsetColumn_=mimeCharset&_filenameColumn_=name&_dataColumn_=data&_download_=yes&ID=7326
 */
public class DownloadController extends ControllerSupport implements Controller
{
    private final DatabaseModel database;

    @Inject
    public DownloadController(DatabaseModel database)
    {
        this.database = database;
    }

    @Override
    public void generate(Request req, Response res)
    {
        String entity         = req.getNonEmpty("_t_");
        String ID             = req.getNonEmpty("ID");

        String typeColumn     = req.getOrDefault("_typeColumn_", "mimeType");
        String filenameColumn = req.getOrDefault("_filenameColumn_", "name");
        String dataColumn     = req.getOrDefault("_dataColumn_", "data");

        String charsetColumn  = req.get("_charsetColumn_");
        boolean download      = "yes".equals(req.get("_download_"));

        RecordModel record = database.getEntity(entity).get(ID);

        String filename    = record.getValueAsString(filenameColumn);
        String contentType = record.getValueAsString(typeColumn);
        Object data        = record.getValue(dataColumn);
        String charset = MoreObjects.
                firstNonNull(charsetColumn != null ? record.getValueAsString(charsetColumn) : null, Charsets.UTF_8.name());

        InputStream in;

        if (data instanceof byte[])//postgres, mysql
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

        res.sendFile(download, filename, contentType, charset, in);
    }

}
