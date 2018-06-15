package com.developmentontheedge.be5.base.util;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Utils
{
    private static final int DATE_PARSING_MODE_DATE = 0;
    private static final int DATE_PARSING_MODE_TIME = 1;
    private static final int DATE_PARSING_MODE_DATETIME = 2;

    private static Locale[] POPULAR_LOCALES = new Locale[]
    {
            Locale.US,
            new Locale( "ru_RU" ),
            Locale.UK,
            Locale.CANADA,
            Locale.ENGLISH,
            Locale.FRANCE,
            Locale.FRENCH,
            Locale.GERMAN,
            Locale.GERMANY,
            Locale.ITALIAN,
            Locale.ITALY,
            Locale.JAPAN,
            Locale.JAPANESE
    };

    private static final String[] dateFormats = new String[]{ "yyyy-MM-dd" };
    private static final String[] timeFormats = new String[]{ "HH:mm:ss" };
    private static final String[] dateTimeFormats = new String[]{ "yyyy-MM-dd HH:mm:ss" };

    public static String inClause(int count)
    {
        if(count <=0)
        {
            throw Be5Exception.internal("Error in function inClause(int), count value: " + count + ", must be > 0");
        }
        return "(" + IntStream.range(0, count).mapToObj(x -> "?").collect(Collectors.joining(", ")) + ")";
    }

    public static String[] addPrefix(String prefix, Object[] values)
    {
        String[] withPrefix = new String[values.length];
        for (int i=0; i<values.length; i++)
        {
            withPrefix[i] = prefix + values[i];
        }

        return withPrefix;
    }

    /**
     * Check given object for an empty value.
     * <br/>Value is empty, if it equals null or it is instance of type String and it's value doesn't have any symbols, except spaces.
     *
     * @param value value
     * @return returns true, if value is empty, otherwise value is false
     */
    public static boolean isEmpty( Object value )
    {
        if( value == null )
        {
            return true;
        }
        if( value instanceof String && "".equals( ( ( String )value ).trim() ) )
        {
            return true;
        }
        if( value instanceof Object[] && ( ( Object[] )value ).length == 0 )
        {
            return true;
        }
        if( value instanceof Collection && ( ( Collection )value ).isEmpty() )
        {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] changeTypes(Object[] values, Class<T> aClass)
    {
        T[] changeType = (T[]) Utils.changeType(values, getArrayClass(aClass));
        if(changeType == null && values != null)
        {
            return (T[])new Object[0];
        }
        return changeType;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T[]> getArrayClass(Class<T> clazz)
    {
        return (Class<? extends T[]>) Array.newInstance(clazz, 0).getClass();
    }

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
            return out.toArray( ( Object[] )Array.newInstance( valClass.getComponentType(), 0 ) );
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
                return ((Number) val).intValue();
            }
            if( long.class.equals( valClass ) || Long.class.equals( valClass ) )
            {
                return ((Number) val).longValue();
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
                return "true".equals(s) || "on".equals(s) ||
                        "yes".equals(s) || "1".equals(s);
            }

            if( BigDecimal.class.equals( valClass ) )
            {
                return new BigDecimal( fixNumber( ( String )val, false ) );
            }
            if( BigInteger.class.equals( valClass ) )
            {
                return new BigInteger( fixNumber( ( String )val, false ) );
            }

            if( java.util.Date.class.equals( valClass ) )
            {
                DateFormat df = DateFormat.getDateInstance( DateFormat.DEFAULT );
                df.setLenient( false );
                java.util.Date parsed;
                try
                {
                    parsed = df.parse( ( String )val );
                }
                catch( ParseException pe )
                {
                    parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_DATE );
                }
                return parsed;
            }
            if( java.sql.Date.class.equals( valClass ) )
            {
                DateFormat df = DateFormat.getDateInstance( DateFormat.DEFAULT );
                df.setLenient( false );
                java.util.Date parsed;
                try
                {
                    parsed = df.parse( ( String )val );
                }
                catch( ParseException pe )
                {
                    parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_DATE );
                }

                //System.out.println( "parsed = " + parsed );

                return new java.sql.Date( parsed.getTime() );
            }
            if( java.sql.Time.class.equals( valClass ) )
            {
                DateFormat df = DateFormat.getTimeInstance( DateFormat.DEFAULT );
                df.setLenient( false );
                java.util.Date parsed;
                try
                {
                    parsed = df.parse( ( String )val );
                }
                catch( ParseException pe )
                {
                    parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_TIME );
                }

                return new java.sql.Time( parsed.getTime() );
            }
            if( java.sql.Timestamp.class.equals( valClass ) )
            {
                String str = ( String )val;

                boolean isHtml5 = str.length() == 16 && str.charAt(10) == 'T';
                if( isHtml5 )
                {
                    val = Utils.subst( str, "T", " " ) + ":00";
                }

                java.util.Date parsed;
                DateFormat df = DateFormat.getDateTimeInstance( DateFormat.DEFAULT, DateFormat.DEFAULT );
                df.setLenient( false );
                try
                {
                    parsed = df.parse( ( String )val );
                }
                catch( ParseException pe )
                {
                    try
                    {
                        parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_DATETIME );
                    }
                    catch( ParseException pe2 )
                    {
                        df = DateFormat.getDateInstance( DateFormat.DEFAULT );
                        try
                        {
                            parsed = df.parse( ( String )val );
                        }
                        catch( ParseException pe3 )
                        {
                            parsed = parseDateWithOtherLocales( ( String )val, DATE_PARSING_MODE_DATE );
                        }
                    }
                }

                return new java.sql.Timestamp( parsed.getTime() );
            }
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
        }
        catch( ParseException | NumberFormatException ignore )
        {

        }
        return val;
    }


    /**
     * Parse date using popular locales.
     * When failed, throws ParseException.
     * It is needed to try all locales when the date could not
     * be parsed with user's locale.
     *
     * @param val
     * @return parsed date
     * @throws ParseException when none of the locales helps.
     */
    private static Date parseDateWithOtherLocales(String val, int parsingMode ) throws ParseException
    {
        ParseException lastException = null;
        for( int i = 0; i < POPULAR_LOCALES.length; i++ )
        {
            try
            {
                DateFormat df = null;
                //dates and times are handled differently
                switch( parsingMode )
                {
                    case DATE_PARSING_MODE_DATE:
                        df = DateFormat.getDateInstance( DateFormat.DEFAULT, POPULAR_LOCALES[ i ] );
                        break;
                    case DATE_PARSING_MODE_TIME:
                        df = DateFormat.getTimeInstance( DateFormat.DEFAULT, POPULAR_LOCALES[ i ] );
                        break;
                    case DATE_PARSING_MODE_DATETIME:
                        df = DateFormat.getDateTimeInstance( DateFormat.DEFAULT, DateFormat.DEFAULT, POPULAR_LOCALES[ i ] );
                        break;
                    default:
                        df = DateFormat.getDateInstance( DateFormat.DEFAULT, POPULAR_LOCALES[ i ] );
                }
                //return when parsed successfully
                java.util.Date parsed = df.parse( val );
                return parsed;
            }
            catch( ParseException pe )
            {
                //could not parse, continue with other locales.
                lastException = pe;
            }
        }

        //now try other specific date/time patterns
        String[] formats = null;
        switch( parsingMode )
        {
            case DATE_PARSING_MODE_DATE:
                formats = dateFormats;
                break;
            case DATE_PARSING_MODE_TIME:
                formats = timeFormats;
                break;
            case DATE_PARSING_MODE_DATETIME:
                formats = dateTimeFormats;
                break;
            default:
                formats = dateFormats;
        }

        for( int i = 0; i < formats.length; i++ )
        {
            String pattern = formats[ i ];
            SimpleDateFormat sdf = new SimpleDateFormat( pattern );
            try
            {
                return sdf.parse( val );
            }
            catch( ParseException pe )
            {
                //could not parse, continue with other patterns.
                lastException = pe;
            }
        }

        //the string was not parsed, throw an exception.
        throw lastException;
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
     * Try`s array values as an array of value pair and converts it to the Map, where first value is the key.
     *
     * @param values Object[n][2]
     * @return value Map
     */
    public static Map valueMap(Object[][] values )
    {
        if( values == null )
        {
            return null;
        }
        LinkedHashMap map = new LinkedHashMap( values.length );
        for( int i = 0; i < values.length; i++ )
        {
            map.put( values[ i ][ 0 ], values[ i ][ 1 ] );
        }
        return map;
    }

    public static Map valueMap( Object ... values )
    {
        return SimpleCompositeMap.valueMap( values );
    }

    public static Map valueNotNullMap( Object ... values )
    {
        return SimpleCompositeMap.valueNotNullMap( values );
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

    public static <T> T ifNull( Object val, T def )
    {
        if( val != null )
        {
            return ( T )val;
        }
        return def;
    }
//
//    public static boolean isSystemDeveloperORDevMode()
//    {
//        return UserInfoHolder.isSystemDeveloper() || ModuleLoader2.getPathsToProjectsToHotReload().size() > 0;
//    }

    public static <T, K, U> Collector<T, ?, Map<K,U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(keyMapper, valueMapper,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new);
    }

    /**
     * Generates random password, containing 8 symbols using english alphabet and numbers.
     *
     * @param userName user name
     * @return generated password
     */
    public static String newRandomPassword( String userName )
    {
        return newRandomPassword( userName, "abcdefghijklmnopqrstuvwxyz0123456789" );
    }

    /**
     * Generates random password, containing 8 symbols from specified symbols array.
     *
     * @param userName user name
     * @param pool symbols to use in password
     * @return generated password
     */
    public static String newRandomPassword( String userName, String pool )
    {
        StringBuffer pass = new StringBuffer();
        Random random = userName == null ? new Random() : new Random( System.currentTimeMillis() + userName.hashCode() );
        for( int i = 0; i < 8; i++ )
        {
            pass.append( pool.charAt( random.nextInt( pool.length() ) ) );
        }
        return pass.toString();
    }

}
