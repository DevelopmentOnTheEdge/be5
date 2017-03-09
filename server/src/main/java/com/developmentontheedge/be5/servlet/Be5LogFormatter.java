package com.developmentontheedge.be5.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class Be5LogFormatter extends Formatter
{
    @Override
    public String format(LogRecord record)
    {
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString().trim().replace( "\n", "\n|" );
        }
        return Instant.ofEpochMilli( record.getMillis() ) + " " + record.getLevel()+ " "+record.getMessage()+throwable+"\n";
    }
}
