package com.developmentontheedge.be5.beans;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.be5.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Record adapter, that uses query as the query for single record result.
 */
public class JDBCRecordAdapterAsQuery extends RecordEx
{
    // used only once, during initialization
    //transient private ResultSet rs;

    // for clone
//    protected JDBCRecordAdapterAsQuery() { initialized = true; }

//    boolean keepRS4Blob;

    /**
     * Reads the first record of thsq SQL query as Dynamic Property Set
     *
     * <br><br>To get record values, you can use:
     * <br/>{@link #getString(String) getString(String)}
     * <br/>{@link #getInt(String) getInt(String)}
     * <br/>{@link #getLong(String) getLong(String)}.  
     *
     * @param sql sql query
     * @throws SQLException
     */
//    public JDBCRecordAdapterAsQuery(String sql)
//    {
    //StringBuffer query = new StringBuffer( sql );
    //connector.getAnalyzer().optimizeRecordRange( query, 0, 1 );

    //System.out.println( "before query.toString() = " + query.toString() );
    //rs = null;//connector.executeQuery( query.toString() );
    //System.out.println( "after query.toString() = " + query.toString() );
//            if( !rs.next() )
//                throw new NoRecord( "No record produced by JDBCRecordAdapterAsQuery, SQL query is " + query );
    //initialize();
//            if( connector.isDb2() )
//            {
//                /* temporary code to change settings - convert Blobs or not */
//                Cache systemSettingsCache = SystemSettingsCache.getInstance();
//                String ssSql = "SELECT setting_value FROM systemSettings WHERE setting_name = 'fullyMaterializeLobData'" +
//                        " AND section_name = 'system'";
//                List vals = Utils.readAsList( connector, ssSql, systemSettingsCache );
//                if( vals.size() > 0 && ( "yes".equals(  vals.get(0) ) || "true".equals(  vals.get(0) ) ) )
//                {
//                    for( DynamicProperty prop : this )
//                    {
//                        if( prop.getValue() instanceof Blob )
//                        {
//                            prop.setType( byte[].class );
//                            Blob val = ( Blob )prop.getValue();
//                            prop.setValue( val.getBytes( 1l, ( int )val.length() ) );
//                        }
//                    }
//                }
//            }
//
//    }

    /**
     * Retrieves first value. Useful when we need only one column
     *
     * @return value of column
     */
    public Object getValue()
    {
        return properties.get(0).getValue();
    }

    /**
     * Retrieves first value as a string.
     *
     * @return value of column
     */
    public String getString()
    {
        return valToString(getValue());
    }

    private static String valToString(Object val)
    {
        try
        {
            if (val == null)
                return null;
            if (val instanceof Blob)
                return new String(((Blob) val).getBytes(1, (int) ((Blob) val).length()));
            if (val instanceof byte[])
                return new String((byte[]) val, "UTF-8");
        } catch (UnsupportedEncodingException | SQLException e)
        {
            return null;
        }
        return val.toString();
    }

    /**
     * Retrieves first value as an int.
     *
     * @return integer value of column or <code>null</code> if it's <code>null</code>.
     */
    public Integer getInt()
    {
        return (null == getValue()) ? null : Integer.valueOf(getValue().toString());
    }

    /**
     * Retrieves first value as a long.
     *
     * @return long value of column or <code>null</code> if it's <code>null</code>.
     */
    public Long getLong()
    {
        return (null == getValue()) ? null : Long.valueOf(getValue().toString());
    }

    /**
     * Retrieves value of the specified column name as string.
     *
     * @param name column
     * @return value of column
     */
    public String getString(String name)
    {
        return valToString(getValue(name));
    }

    /**
     * Retrieves value of the specified column name as int.
     *
     * @param name column
     * @return value of column
     */
    public int getInt(String name)
    {
        return Integer.parseInt(getValue(name).toString());
    }

    /**
     * Retrieves value of the specified column name as long.
     *
     * @param name column
     * @return value of column
     */
    public long getLong(String name)
    {
        return Long.parseLong(getValue(name).toString());
    }

    public java.sql.Date getDate(String name)
    {
        java.util.Date date = (java.util.Date) getValue(name);
        if (date == null)
        {
            return null;
        }
        return new java.sql.Date(date.getTime());
    }

    public java.sql.Date getDate()
    {
        java.util.Date date = (java.util.Date) getValue();
        if (date == null)
        {
            return null;
        }
        return new java.sql.Date(date.getTime());
    }

    public InputStream getBinaryStream() throws SQLException
    {
        Object val = getValue();
        if (val == null)
        {
            return null;
        } else if (val instanceof byte[])
        {
            return new ByteArrayInputStream((byte[]) val);
        }
        return new BlobInputStream((Blob) val, properties.get(0).getName());
    }

    public InputStream getBinaryStream(String name) throws SQLException
    {
        Object val = getValue(name);

        if (val == null)
        {
            return null;
        } else if (val instanceof byte[])
        {
            return new ByteArrayInputStream((byte[]) val);
        }
        return new BlobInputStream((Blob) val, name);
    }

    public static final class BlobInputStream extends InputStream
    {
        Blob blob;
        InputStream is;
        String name;

        boolean isClosed;

        public BlobInputStream(Blob blob, String name) throws SQLException
        {
            this.blob = blob;
            this.name = name;
            is = blob.getBinaryStream();
        }

        @Override
        public int read() throws IOException
        {
            int ret = is.read();
            if (ret == -1)
            {
                //System.out.println( "autoclose: " + name );
                close();
            }
            return ret;
        }

        @Override
        public int read(byte[] b) throws IOException
        {
            int ret = is.read(b);
            if (ret < 1)
            {
                //System.out.println( "autoclose: " + name );
                close();
            }
            return ret;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            int ret = is.read(b, off, len);
            if (ret < 1)
            {
                //System.out.println( "autoclose: " + name );
                close();
            }
            return ret;
        }

        @Override
        public long skip(long n) throws IOException
        {
            return is.skip(n);
        }

        @Override
        public int available() throws IOException
        {
            return is.available();
        }

        @Override
        public void close() throws IOException
        {
            if (isClosed)
            {
                return;
            }

            try
            {
                is.close();
                is = null;
                Method pmeth = blob.getClass().getMethod("isTemporary", new Class[0]);
                if (pmeth != null)
                {
                    boolean isTemporary = (Boolean) pmeth.invoke(blob, new Object[0]);
                    if (isTemporary)
                    {
                        pmeth = blob.getClass().getMethod("freeTemporary", new Class[0]);
                        if (pmeth != null)
                        {
                            pmeth.invoke(blob, new Object[0]);
                        }
                    }
                }
            } catch (NoSuchMethodException ignore)
            {
            } catch (Exception exc)
            {
                throw new IOException(exc.getMessage(), exc);
            } finally
            {
                isClosed = true;
                blob = null;
            }
        }

        @Override
        public void mark(int readlimit)
        {
            is.mark(readlimit);
        }

        @Override
        public void reset() throws IOException
        {
            is.reset();
        }

        @Override
        public boolean markSupported()
        {
            return is.markSupported();
        }

        @Override
        protected void finalize() throws Throwable
        {
            close();
        }
    }
}
