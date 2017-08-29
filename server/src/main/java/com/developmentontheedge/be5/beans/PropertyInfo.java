package com.developmentontheedge.be5.beans;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Note: This class should comply with JavaBeans specification
 * because it used together with {@link java.beans.XMLEncoder}. 
 */
public class PropertyInfo extends DynamicPropertySetSupport
{
    private String initStr;

    PropertyInfo reuse;

    private static PropertyInfo empty = new PropertyInfo();

    public PropertyInfo()
    {
    }

    public PropertyInfo(String initializer )
    {
        setInit( initializer );
    }

    private PropertyInfo(String initializer, boolean checkCache )
    {
        setInit( initializer, checkCache );
    }

    public void setInit( String initializer )
    {
        setInit( initializer, true );
    }

    public void setInit( String initializer, boolean checkCache )
    {
//        Cache cache = PropertyInfoCache.getInstance();
//        if( checkCache && ( reuse = ( PropertyInfo )cache.get( initializer ) ) != null )
//        {
//             return;
//        }

        initStr = initializer;
        StringTokenizer st = new StringTokenizer( initializer, " </" );
        setType( st.nextToken() );
        try
        {
            if( initializer.indexOf( '\'' ) >= 0 || initializer.indexOf( '\"' ) >= 0 )
            {
                while( st.hasMoreTokens() )
                {
                    String name = st.nextToken( " =" );
                    st.nextToken( "'\"" ); // ignore '='
                    String value = st.nextToken( "'\"" );
                    st.nextToken( " " );
                    add( new DynamicProperty( name, String.class, value ) );
                }
            }
            else
            {
                while( st.hasMoreTokens() )
                {
                    String name = st.nextToken( " =" );
                    String value = st.nextToken( " =/>" );
                    add( new DynamicProperty( name, String.class, value ) );
                }
            }
        }
        catch( java.util.NoSuchElementException ignore ) { /*ignore*/ }
        
        //cache.put( initializer, this );
    }

    public String getInit()
    {
        return reuse != null ? reuse.initStr : initStr;
    }

    public String getAttr( String name )
    {
        return reuse != null ? ( String )reuse.getValue( name ) : ( String )super.getValue( name );
    }

    private String type;
    public String getType() { return reuse != null ? reuse.type : type; }
    public void setType( String type )
    { 
        if( reuse != null )
        {
            throw new RuntimeException( "Not permitted because cached value is used!" );
        }   
        this.type = type; 
    }

    public Object getValue(String name)
    {
        if( reuse != null )
        {
            throw new RuntimeException( "PropertyInfo.getValue shouldn't be invoked directly!" );
        }

        return super.getValue( name );
    }

    @Override
    public String toString()
    {
        if( reuse != null )
        {
            return reuse.toString();
        }

        StringWriter out = new StringWriter();
        printProperties(out, "");
        return "PropertyInfo: \n" + out.toString();
    }

    public Map<String, Object> asMap()
    {
        if( reuse != null )
        {
            return reuse.asMap();
        }
        return super.asMap();
    }

    public Iterator<DynamicProperty> propertyIterator()
    {
        if( reuse != null )
        {
            return reuse.propertyIterator();
        }
        return super.propertyIterator();
    }

    @Override
    public boolean equals( Object o )
    {
        if( reuse != null )
        {
            return reuse.equals( o );
        }

        if ( o == null || ! (o instanceof PropertyInfo) )
            return false;
        
        PropertyInfo p = (PropertyInfo)o;
        
        if( ( getType() == null && p.getType() != null ) || ( getType() != null && !getType().equals( p.getType() ) ) )
            return false;
        
        return asMap().equals( p.asMap() );
    }

    public static String quickExtract( String pistr, String attr )
    {
        String start = " " + attr + "=\""; 
        int ind1 = pistr.indexOf( start );
        if( ind1 < 0 )
        {
            return null;
        }
        int ind2 = pistr.indexOf( "\"", ind1 + start.length() );
        if( ind2 > 0 )
        {
            return pistr.substring( ind1 + start.length(), ind2 );  
        }
        return null;                  
    }

    public static PropertyInfo withCache( String initializer )
    {
//        Cache cache = PropertyInfoCache.getInstance();
//        PropertyInfo ret = ( PropertyInfo )cache.get( initializer );
//        if( ret != null )
//        {
//            return ret;
//        }

        return new PropertyInfo( initializer, false );
    } 

    public static PropertyInfo getEmpty()
    {
        return empty; 
    }

}
