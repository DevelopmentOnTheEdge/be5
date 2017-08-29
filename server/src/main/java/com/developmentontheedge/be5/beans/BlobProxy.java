// $Id: BlobProxy.java,v 1.6 2014/02/11 09:48:16 lan Exp $

package com.developmentontheedge.be5.beans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;

/**
 * Implementation of the Blob interface for JDBC drivers which doesn't implement 
 * it properly (like PostgreSQL). Made dynamic proxy to avoid problems with different 
 * Blob interfaces in JDKs 1.4-1.6. 
 * 
 * @author puz
 *
 */
public class BlobProxy implements InvocationHandler
{

    private BlobImpl impl;

    private BlobProxy(InputStream is)
    {
        this.impl = new BlobImpl( is );
    }
    
    public static Blob newInstanse(InputStream is)
    {
        return (Blob)java.lang.reflect.Proxy.newProxyInstance( Blob.class.getClassLoader(), new Class[] {Blob.class}, new BlobProxy( is ) );
    }

    /**
     * Implement only getBinaryStream(), getBytes(), length() from java.sql.Blob. 
     * 
     */
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        try
        {
            if( ( "getBinaryStream".equals( m.getName() ) && m.getParameterTypes().length == 0 ) 
                    || "getBytes".equals( m.getName() )
                    || "toString".equals( m.getName() )
                    || "hashCode".equals( m.getName() )
                    || "length".equals( m.getName() ) )
            {
                return impl.getClass().getMethod( m.getName(), m.getParameterTypes() ).invoke( impl, args );
            }            

            throw new RuntimeException( m.getName() + ": Not implemented: " + String.valueOf( m ) );
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
        catch( Exception e )
        {
            throw new RuntimeException( "unexpected invocation exception: " + e.getMessage() );
        }
    }

    /**
     * Simplest implementation.
     * 
     * @author puz
     *
     */
    private static class BlobImpl
    {
        private byte[] bytes;
        private InputStream inputStream;

        public BlobImpl(InputStream inputStream)
        {
            this.inputStream = inputStream;
        }

        private void readBytes() throws IOException
        {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;

            while( ( len = inputStream.read( buffer ) ) >= 0 )
                out.write( buffer, 0, len );

            inputStream.close();
            out.close();
            bytes = out.toByteArray();
        }

        public InputStream getBinaryStream() throws IOException
        {
            if( bytes == null )
            {
                readBytes(); 
            }
            return new ByteArrayInputStream( bytes );
        }

        public byte[] getBytes(long pos, int length) throws IOException
        {
            if( bytes == null )
            {
                readBytes(); 
            }
            byte[] copy = new byte[length];
            System.arraycopy( bytes, (int)pos - 1, copy, 0, Math.min( bytes.length - (int)pos + 1, (int)length ) );
            return copy;
        }

        public long length() throws IOException
        {
            if( bytes == null )
            {
                readBytes(); 
            }
            return bytes.length;
        }

        public int hashCode()
        {
            try
            {
                if( bytes == null )
                {
                    readBytes(); 
                }
                return bytes.hashCode() + 1;
            }     
            catch( Exception e )
            {
                throw new RuntimeException( "unexpected hashCode exception: " + e.getMessage() );
            }
        }

        public String toString()
        {
            return "BlobImpl";
        }
    }
}
