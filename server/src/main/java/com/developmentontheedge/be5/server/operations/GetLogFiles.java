package com.developmentontheedge.be5.server.operations;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.server.operations.support.DownloadOperationSupport;
import com.developmentontheedge.be5.server.util.RequestUtils;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.beans.DynamicPropertyBuilder;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.net.MediaType;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GetLogFiles extends DownloadOperationSupport
{
    private File logFile;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySetSupport params = new DynamicPropertySetSupport();
        logFile = getCurrentLogFile();
        if (logFile == null)
        {
            setResult(OperationResult.error("Log file not found"));
            return null;
        }

        params.add(new DynamicPropertyBuilder("current", Boolean.class)
                .title("Load Current log file?")
                .reloadOnChange()
                .nullable()
                .value(!"false".equals(presetValues.get("current")))
                .get());

        if ((boolean) params.getValue("current"))
        {
            params.add(new DynamicPropertyBuilder("logFile", String.class)
                    .value(logFile.getName())
                    .readonly()
                    .get());
        }
        else
        {
            params.add(new DynamicPropertyBuilder("access_log", Boolean.class)
                    .title("Show access logs?")
                    .reloadOnChange()
                    .nullable()
                    .value("true".equals(presetValues.get("access_log")))
                    .get());

            boolean showAccessLog = (boolean) params.getValue("access_log");

            List<String> list = new ArrayList<>();
            File folder = logFile.getParentFile();
            for (final File fileEntry : folder.listFiles())
            {
                if (!showAccessLog && fileEntry.getName().contains("access_log")) continue;
                list.add(fileEntry.getName());
            }
            params.add(new DynamicPropertyBuilder("logFile", String.class)
                    .title("Log file")
                    .tags(list.toArray(new String[0]))
                    .value(presetValues.get("logFile"))
                    .get());
            logFile = folder.toPath().resolve((String) presetValues.get("logFile")).toFile();
        }

        return params;
    }

    @Override
    public void invokeWithResponse(Response res, Object parameters) throws Exception
    {
        DataInputStream inputStream = new DataInputStream(new FileInputStream(logFile));
        RequestUtils.sendFile(res, true, logFile.getName(), "text/csv",
                StandardCharsets.UTF_8.name(), inputStream);
    }

    private File getCurrentLogFile() throws FileNotFoundException
    {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (ch.qos.logback.classic.Logger logger : context.getLoggerList())
        {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();)
            {
                Appender<ILoggingEvent> appender = index.next();

                if (appender instanceof FileAppender)
                {
                    FileAppender<ILoggingEvent> fa = (FileAppender<ILoggingEvent>) appender;
                    ResilientFileOutputStream rfos = (ResilientFileOutputStream) fa.getOutputStream();
                    return rfos.getFile();
                }
            }
        }
        return null;
    }
}
