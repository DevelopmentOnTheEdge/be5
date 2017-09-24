/** $Id: CheckDateServlet.java,v 1.6 2013/07/24 04:35:27 dimka Exp $ */

package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.be5.util.DateUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckIntervalServlet
{
    public static final String URI = "json/checkInterval";
    public static final String TYPE_PARAM = "type";
    public static final String IS_SEARCH = "isSearch";

//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
//    {
//        Map params = getParams( request ).getCompleteParamTable( true );
//        String typeName = ( String )params.get( TYPE_PARAM );
//        String fieldName = ( String )params.get( HttpConstants.FIELD_NAME_PARAM );
//        String fieldValue = ( String )params.get( fieldName );
//        String intervalFrom = ( String )params.get( HttpConstants.INTERVAL_FROM_PARAM );
//        String intervalTo = ( String )params.get( HttpConstants.INTERVAL_TO_PARAM );
//        if ( params.get( IS_SEARCH ) != null && Boolean.parseBoolean( params.get( IS_SEARCH ).toString() ) )
//            fieldValue = stripSearchChars( fieldValue );
//        try
//        {
//            Class type = java.sql.Date.class;
//            if( !"java.sql.Date".equals( typeName ) )
//            {
//                type = Utils.classForName( typeName );
//            }
//            boolean result = false;
//            // return true for isSearch && ( '%' || 'is empty' || 'is not empty' ) - it's ok for filter operation
//            if( !Utils.isEmpty( fieldValue ) && "true".equals( params.get( IS_SEARCH ) ) &&
//                    ( fieldValue.contains( "%" ) || HttpSearchOperation.UNDEFINED_VALUE.equalsIgnoreCase( fieldValue ) || HttpSearchOperation.DEFINED_VALUE.equalsIgnoreCase( fieldValue ) ) )
//            {
//                result = true;
//            }
//            else if( !Utils.isEmpty( type ) && !Utils.isEmpty( fieldValue ) )
//            {
//                result = check( type, fieldValue, intervalFrom, intervalTo, getUserInfo( request ) );
//            }
//            response.getWriter().write( String.valueOf( result ) );
//        }
//        catch( ClassNotFoundException e )
//        {
//            throw new ServletException( e );
//        }
//    }

    private static boolean check( Class type, Object value, String intervalFrom, String intervalTo )
    {
        Object changed = Utils.changeType( value, type );
        if( !type.isInstance( changed ) )
        {
            return false; 
        }

        if( short.class.equals( type ) || Short.class.equals( type ) )
        {
           return ( ( Short )Utils.changeType( intervalFrom, type ) ) <= ( ( Number )changed ).shortValue() &&
                  ( ( Short )Utils.changeType( intervalTo, type ) ) >= ( ( Number )changed ).shortValue();
        }
        if( int.class.equals( type ) || Integer.class.equals( type ) )
        {
           return ( ( Integer )Utils.changeType( intervalFrom, type ) ) <= ( ( Number )changed ).intValue() &&
                  ( ( Integer )Utils.changeType( intervalTo, type ) ) >= ( ( Number )changed ).intValue();
        }
        if( long.class.equals( type ) || Long.class.equals( type ) )
        {
           return ( ( Long )Utils.changeType( intervalFrom, type ) ) <= ( ( Number )changed ).longValue() &&
                  ( ( Long )Utils.changeType( intervalTo, type ) ) >= ( ( Number )changed ).longValue();
        }
        if( Number.class.isAssignableFrom( type ) )
        {
           return Double.parseDouble( Utils.fixNumber( intervalFrom, false ) ) <= ( ( Number )changed ).doubleValue() &&
                  Double.parseDouble( Utils.fixNumber( intervalTo, false ) ) >= ( ( Number )changed ).doubleValue();
        }
        if( type.equals( java.util.Date.class ) )
        {
           java.sql.Date fDate = ( java.sql.Date )Utils.changeType( intervalFrom, type );
           java.sql.Date tDate = DateUtils.addDays( ( java.sql.Date )Utils.changeType( intervalTo, type ), 1 );
           return DateUtils.isBetween( ( java.sql.Date )changed, fDate, tDate );
        }
        if( type.equals( java.sql.Date.class ) )
        {
           java.sql.Date fDate = ( java.sql.Date )Utils.changeType( intervalFrom, type );
           java.sql.Date tDate = DateUtils.addDays( ( java.sql.Date )Utils.changeType( intervalTo, type ), 1 );
           return DateUtils.isBetween( ( java.sql.Date )changed, fDate, tDate );
        }

        return false; 
    }

    private static final Pattern pat = Pattern.compile( "^(!|<=?|>=?)\\s*(.*)$" );
    
    private static String stripSearchChars( String value )
    {
        Matcher mat = pat.matcher( value );
        if ( mat.matches() )
            value = mat.group( 2 );
        return value;
    }
}
