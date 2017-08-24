package com.developmentontheedge.be5.util;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class Utils
{
    //todo parametrize: <T> T changeType( Object val, T valClass )
    public static Object changeType( Object val, Class valClass )
    {
        if( val == null )
        {
            return null;
        }

        if( val.getClass().equals( valClass ) )
        {
            return val;
        }

//        if( Scriptable.NOT_FOUND.equals( val ) )
//        {
//            return null;
//        }

/*
        if( val instanceof NativeString )
        {
            val = val.toString();
        }
*/

//        if( valClass.isEnum() )
//        {
//            Method[] methods = valClass.getDeclaredMethods();
//            for( Method method : methods )
//            {
//                if( method.isAnnotationPresent( IsValueOf.class ) )
//                {
//                    try
//                    {
//                        return method.invoke( valClass, val );
//                    }
//                    catch( Exception e )
//                    {
//                        throw new IllegalArgumentException( e );
//                    }
//                }
//            }
//            try
//            {
//                Method method = valClass.getMethod( "valueOf", val.getClass() );
//                return method.invoke( valClass, val );
//            }
//            catch( Exception ignored )
//            {
//                // Enumerate all enum constants.
//                for( Object enumValue : valClass.getEnumConstants() )
//                {
//                    if( enumValue.toString().equals( val.toString() ) )
//                    {
//                        return enumValue;
//                    }
//                }
//            }
//            throw new IllegalArgumentException( "Illegal value for enum " + valClass.getName() + ": " + val );
//        }

        if( val.getClass().isArray() && valClass.isArray() &&
                !val.getClass().getComponentType().equals( valClass.getComponentType() ) )
        {
            Object[] vals = ( Object[] )val;
            if( vals.length == 0 )
            {
                return null;
            }
            List<Object> out = new ArrayList<>();
            for( int i = 0; i < vals.length; i++ )
            {
                Object newVal = changeType( vals[ i ], valClass.getComponentType() );
                //when submitted String array cannot be converted to Timestamp array, it may mean
                //that although the column type is Timestamp, it should be handled as java.sql.Date type
                //we just return the _val_ unchanged, it will be checked in addRecordFilter() and there will
                // be an attempt to parse it as Date
                if( java.sql.Timestamp.class.equals( valClass.getComponentType() ) && newVal.equals( vals[ i ] ) )
                {
                    return val;
                }

                out.add( newVal );
            }
            return out.toArray( ( Object[] )java.lang.reflect.Array.newInstance( valClass.getComponentType(), 0 ) );
        }

/*        if( val.getClass().isArray() && !valClass.isArray() )
        {
            Object []vals = ( Object [] )val;
            if( vals.length == 0 )
                return null;
            val = vals[ 0 ];
            if( val == null )
            {
                return null;
            }
        }
*/
        if( java.sql.Date.class.equals( valClass ) &&
                val instanceof java.util.Date )
        {
            return new java.sql.Date( ( ( java.util.Date )val ).getTime() );
        }

        if( java.sql.Date.class.equals( valClass ) &&
                val instanceof java.util.Calendar )
        {
            return new java.sql.Date( ( ( java.util.Calendar )val ).getTime().getTime() );
        }

        if( java.util.Date.class.equals( valClass ) &&
                val instanceof java.sql.Date )
        {
            return new java.util.Date( ( ( java.sql.Date )val ).getTime() );
        }

        if( java.util.Date.class.equals( valClass ) &&
                val instanceof XMLGregorianCalendar)
        {
            XMLGregorianCalendar xcal = ( XMLGregorianCalendar ) val;
            return xcal.toGregorianCalendar().getTime();
        }

        if( java.sql.Date.class.equals( valClass ) &&
                val instanceof XMLGregorianCalendar )
        {
            XMLGregorianCalendar xcal = ( XMLGregorianCalendar ) val;
            return new java.sql.Date( xcal.toGregorianCalendar().getTime().getTime() );
        }

        if ( javax.xml.datatype.XMLGregorianCalendar.class.equals( valClass ) &&
                val instanceof java.util.Date  )
        {
            try
            {
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime( ( java.util.Date )val );
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            }
            catch( DatatypeConfigurationException ignore )
            {
                return val;
            }
        }

        if ( javax.xml.datatype.XMLGregorianCalendar.class.equals( valClass ) &&
                val instanceof String  )
        {
            try
            {
                return DatatypeConverter.parseDateTime( ( String )val );
            }
            catch( IllegalArgumentException ignore )
            {
                return val;
            }
        }


        if( java.util.Date.class.equals( valClass ) &&
                val instanceof java.sql.Timestamp )
        {
            return new java.util.Date( ( ( java.sql.Timestamp )val ).getTime() );
        }

        if( String.class.equals( valClass ) && "org.mozilla.javascript.NativeString".equals( val.getClass().getName() ) )
        {
            return val.toString();
        }

//        if( ( val instanceof java.sql.Date || val instanceof java.sql.Timestamp ) && String.class.equals( valClass ) )
//        {
//            try
//            {
//                return localizedValue( val, userInfo );
//            }
//            catch( Exception exc )
//            {
//                Logger.error( cat, "changeType: something wrong with date '" + val + "'" );
//                return null;
//            }
//        }

        if( val instanceof Boolean && String.class.equals( valClass ) )
        {
            return val.toString();
        }

        if( val instanceof Number )
        {
            if( String.class.equals( valClass ) )
            {
                return val.toString();
            }
            if( int.class.equals( valClass ) || Integer.class.equals( valClass ) )
            {
                return Integer.valueOf( ( ( Number )val ).intValue() );
            }
            if( long.class.equals( valClass ) || Long.class.equals( valClass ) )
            {
                return Long.valueOf( ( ( Number )val ).longValue() );
            }
            if( BigDecimal.class.equals( valClass ) )
            {
                return new BigDecimal( ( ( Number )val ).doubleValue() );
            }
        }

        if( "org.mozilla.javascript.NativeString".equals( val.getClass().getName() ) )
        {
            val = val.toString();
        }

        if( !( val instanceof String ) )
        {
            return val;
        }

        if( "".equals( val ) )
        {
            return null;
        }

        try
        {
            if( Double.class.equals( valClass ) || double.class.equals( valClass ) )
            {
                return Double.valueOf( fixNumber( ( String )val, false ) );
            }
            if( Float.class.equals( valClass ) || float.class.equals( valClass ) )
            {
                return Float.valueOf( fixNumber( ( String )val, false ) );
            }
            if( Byte.class.equals( valClass ) || byte.class.equals( valClass ) )
            {
                return Byte.valueOf( fixNumber( ( String )val, true ) );
            }
            if( Short.class.equals( valClass ) || short.class.equals( valClass ) )
            {
                return Short.valueOf( fixNumber( ( String )val, true ) );
            }
            if( Integer.class.equals( valClass ) || int.class.equals( valClass ) )
            {
                return Integer.valueOf( fixNumber( ( String )val, true ) );
            }
            if( Long.class.equals( valClass ) || long.class.equals( valClass ) )
            {
                //System.out.println( "GOTCHA!!!!" );
                return Long.valueOf( fixNumber( ( String )val, true ) );
            }
            if( Boolean.class.equals( valClass ) || boolean.class.equals( valClass ) )
            {
                String s = ( ( String )val ).toLowerCase();
                return Boolean.valueOf( "true".equals( s ) || "on".equals( s ) ||
                        "yes".equals( s ) || "1".equals( s ) );
            }

            if( BigDecimal.class.equals( valClass ) )
            {
                return new BigDecimal( fixNumber( ( String )val, false ) );
            }
            if( BigInteger.class.equals( valClass ) )
            {
                return new BigInteger( fixNumber( ( String )val, false ) );
            }

//            if( java.util.Date.class.equals( valClass ) )
//            {
//                DateFormat df = DateFormat.getDateInstance( DateFormat.DEFAULT, userInfo.getLocale() );
//                df.setLenient( false );
//                java.util.Date parsed = null;
//                try
//                {
//                    parsed = df.parse( ( String )val );
//                }
//                catch( ParseException pe )
//                {
//                    parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_DATE );
//                }
//                return parsed;
//            }
//            if( java.sql.Date.class.equals( valClass ) )
//            {
//                DateFormat df = DateFormat.getDateInstance( DateFormat.DEFAULT, userInfo.getLocale() );
//                df.setLenient( false );
//                java.util.Date parsed = null;
//                try
//                {
//                    parsed = df.parse( ( String )val );
//                }
//                catch( ParseException pe )
//                {
//                    parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_DATE );
//                }
//
//                //System.out.println( "parsed = " + parsed );
//
//                return new java.sql.Date( parsed.getTime() );
//            }
//            if( java.sql.Time.class.equals( valClass ) )
//            {
//                DateFormat df = DateFormat.getTimeInstance( DateFormat.DEFAULT, userInfo.getLocale() );
//                df.setLenient( false );
//                java.util.Date parsed = null;
//                try
//                {
//                    parsed = df.parse( ( String )val );
//                }
//                catch( ParseException pe )
//                {
//                    parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_TIME );
//                }
//
//                return new java.sql.Time( parsed.getTime() );
//            }
//            if( java.sql.Timestamp.class.equals( valClass ) )
//            {
//                String str = ( String )val;
//
//                boolean isHtml5 = str.length() == 16 && str.charAt(10) == 'T';
//                if( isHtml5 )
//                {
//                    val = Utils.subst( str, "T", " " ) + ":00";
//                }
//
//                java.util.Date parsed = null;
//                DateFormat df = DateFormat.getDateTimeInstance( DateFormat.DEFAULT, DateFormat.DEFAULT, userInfo.getLocale() );
//                df.setLenient( false );
//                try
//                {
//                    parsed = df.parse( ( String )val );
//                }
//                catch( ParseException pe )
//                {
//                    try
//                    {
//                        parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_DATETIME );
//                    }
//                    catch( ParseException pe2 )
//                    {
//                        df = DateFormat.getDateInstance( DateFormat.DEFAULT, userInfo.getLocale() );
//                        try
//                        {
//                            parsed = df.parse( ( String )val );
//                        }
//                        catch( ParseException pe3 )
//                        {
//                            parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_DATE );
//                        }
//                    }
//                }
//
//                return new java.sql.Timestamp( parsed.getTime() );
//            }
//            if ( javax.xml.datatype.XMLGregorianCalendar.class.equals( valClass )  )
//            {
//                GregorianCalendar gc = new GregorianCalendar();
//                DateFormat df = DateFormat.getDateInstance( DateFormat.DEFAULT, userInfo.getLocale() );
//                df.setLenient( false );
//                java.util.Date parsed = null;
//                try
//                {
//                    parsed = df.parse( ( String )val );
//                }
//                catch( ParseException pe )
//                {
//                    parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_DATE );
//                }
//                gc.setTime( parsed );
//                return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
//            }

            if( File.class.equals( valClass ) )
            {
                return new File( ( String )val );
            }
//        }
//        catch( ParseException ignore )
//        {
        }
        catch( NumberFormatException ignore )
        {
//        }
//        catch( DatatypeConfigurationException ignore )
//        {
        }
        return val;
    }

    public static String fixNumber( String number, boolean isInt )
    {
        if( number == null )
        {
            return null;
        }
        number = subst( number, " ", "" );
        int pointInd = number.lastIndexOf( '.' );
        int commaInd = number.lastIndexOf( ',' );

        if( commaInd > 0 && pointInd > commaInd )
        {
            return subst( number, ",", "" );
        }
        if( !isInt && commaInd > 0 )
        {
            return subst( number, ",", "." );
        }

        if( isInt )
        {
            try
            {
                Long.valueOf( number );
            }
            catch( NumberFormatException nfe )
            {
                if( number.endsWith( ".0" ) )
                {
                    return subst( number, ".0", "" );
                }
            }
        }

        return number;
    }


    /**
     * Substitute string "fromText" in "text" for string "toText".
     * Substituted text will be returned as a result.
     *
     * @param text text, where fromText is substituting for another text.
     * @param fromText text for substituting
     * @param toText text, that is substituting fromText
     * @return returns substituted text
     */
    public static String subst( String text, String fromText, String toText )
    {
        return subst( text, fromText, toText, "" );
    }

    /**
     * Substitute string "fromText" in "text" for another string.
     * Substitution string is "toText" or, if "toText" is empty (isEmpty), then "defText".
     * Substituted text will be returned as a result.
     *
     * @param text text, where fromText is substituting for another text.
     * @param fromText text for substituting
     * @param toText text, that is substituting fromText
     * @param defText text, that is substituting fromText, if "toText" is empty (isEmpty)
     * @return returns substituted text
     */
    public static String subst( String text, String fromText, String toText, String defText )
    {
        if( text == null )
        {
            return null;
        }
        int prevPos = 0;
        String newText = toText == null || "".equals( toText ) ? defText : toText;
        for( int pos = text.indexOf( fromText, prevPos ); pos >= 0;
             pos = text.indexOf( fromText, prevPos + newText.length() ) )
        {
            prevPos = pos;
            text = new StringBuffer( text ).replace( pos, pos + fromText.length(), newText ).toString();
        }
        return text;
    }

}
