package com.developmentontheedge.be5.server.operations;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import com.developmentontheedge.be5.server.operations.support.DownloadOperationSupport;
import com.developmentontheedge.be5.server.util.RequestUtils;
import com.developmentontheedge.be5.web.Response;
import com.google.common.net.MediaType;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class GetLogFiles extends DownloadOperationSupport
{
    @Override
    public void invokeWithResponse(Response res, Object parameters) throws Exception
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
                    File file = rfos.getFile();

                    DataInputStream inputStream = new DataInputStream(new FileInputStream(file));
                    RequestUtils.sendFile(res, true, "test.txt", MediaType.PLAIN_TEXT_UTF_8.type(),
                            StandardCharsets.UTF_8.name(), inputStream);
                    return;
                }
            }
        }
        res.sendHtml("FileAppender not found");
    }
}
