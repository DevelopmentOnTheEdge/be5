package com.developmentontheedge.be5.components.impl.model;

import com.google.common.base.Strings;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapParamHelper implements ParamHelper
{
    private static final String ENC_ENDING = "_enc";
    private static final String ENC_STARTING = "enc_";

    private final Map<String, String> map;

    public MapParamHelper(Map<String, String> map)
    {
        this.map = new LinkedHashMap<>(map);
    }
    
    @Override
    public void remove(String name)
    {
        map.remove(name);
    }

    @Override
    public String get(String name)
    {
        return map.get(name);
    }

    /**
     * Returns null if the parameter is empty.
     */
    @Override
    public String getStrict(String name)
    {
        return Strings.emptyToNull(map.get(name));
    }

    @Override
    public String[] getValues(String name)
    {
        if (map.containsKey(name))
        {
            return new String[0];
        }
        
        return new String[] { map.get(name) };
    }
    
    @Override
    public void put(String name, String value) throws Exception
    {
        map.put(name, value);
    }

    @Override
    public void put(String name, String[] value) throws Exception
    {
        map.put(name, value[0]);
    }

    @Override
    public Hashtable<String, String> getCompleteParamTable()
    {
        return new Hashtable<>(map);
    }
    
    @Override
    public Hashtable<String, String> getNonStandardTable()
    {
        return new Hashtable<>(map);
    }

    /**
     * Encodes property name for using it by the user.
     *
     * @param propName property name
     * @return encoded property name
     */
    public static String mapNameOut( String propName )
    {
        String newName = propName.replace( '/', '#' );
        char[] cs = newName.toCharArray();
        boolean bEncode = false;
        for( int i = 0; i < cs.length; i++ )
        {
            int ch = cs[ i ];
            if( ch > 127 )
            {
                bEncode = true;
                break;
            }
        }
        if( bEncode )
        {
            StringBuffer out = new StringBuffer();
            for( int i = 0; i < cs.length; i++ )
            {
                int ch = cs[ i ];
                byte hi = ( byte )( ( ch & 0xFF00 ) >> 8 );
                byte lo = ( byte )( ch & 0xFF );
                String hiStr = Integer.toHexString( hi );
                if( hiStr.length() == 1 )
                {
                    hiStr = "0" + hiStr;
                }
                String loStr = Integer.toHexString( lo );
                if( loStr.length() == 1 )
                {
                    loStr = "0" + loStr;
                }
                out.append( hiStr ).append( loStr );
            }

            newName = ENC_STARTING + out.toString() + ENC_ENDING;
        }
        return newName;
    }

}
