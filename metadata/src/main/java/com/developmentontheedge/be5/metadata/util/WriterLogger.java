package com.developmentontheedge.be5.metadata.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WriterLogger implements ProcessController
{
    PrintStream ps;
    
    public WriterLogger()
    {
        this(System.err);
    }
    
    public WriterLogger(PrintStream ps)
    {
        this.ps = ps;
    }

    @Override
    public void setOperationName( String name )
    {
        ps.println( new SimpleDateFormat( "HH:mm:ss" ).format( new Date() ) + ": " + name );
    }

    @Override
    public void setProgress( double progress )
    {
        ps.printf("%.2f%%%n", progress*100);
    }
}
