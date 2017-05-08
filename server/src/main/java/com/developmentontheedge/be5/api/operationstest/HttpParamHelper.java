package com.developmentontheedge.be5.api.operationstest;

public class HttpParamHelper
{
    private static final String ENC_ENDING = "_enc";
    private static final String ENC_STARTING = "enc_";

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

    /**
     * Decodes encoded property name.
     *
     * @param propName
     * @return
     */
    public static String mapNameIn( String propName )
    {
        String newName = propName;
        boolean bDecode = newName.startsWith( ENC_STARTING ) && newName.endsWith( ENC_ENDING );
        if( bDecode )
        {
            String in = newName.substring( ENC_STARTING.length(), newName.length() - ENC_ENDING.length() );
            char[] cs = new char[in.length() / 4];
            for( int i = 0; i < cs.length; i++ )
            {
                cs[ i ] = ( char )Integer.valueOf( in.substring( i * 4, i * 4 + 4 ), 16 ).intValue();
            }
            newName = new String( cs );
        }

        return newName.replace( '#', '/' );
    }
}
