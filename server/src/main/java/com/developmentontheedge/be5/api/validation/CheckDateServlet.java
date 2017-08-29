/** $Id: CheckDateServlet.java,v 1.6 2013/07/24 04:35:27 dimka Exp $ */

package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.be5.util.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servlet for checking validity of dates provided by user.
 * Since BE supports various locales including most popular ones,
 * we need to validate dates on server using its API.
 *
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 * @see <a href="http://docs.jquery.com/Plugins/Validation/Methods/remote#options">AJAX validation method</a>
 */
public class CheckDateServlet
{
    public static final String URI = "json/checkDate";
    public static final String TYPE_PARAM = "type";
    public static final String IS_SEARCH = "isSearch";

//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
//    {
//        Map params = getParams( request ).getCompleteParamTable( true );
//        String typeName = ( String )params.get( TYPE_PARAM );
//        String fieldName = ( String )params.get( HttpConstants.FIELD_NAME_PARAM );
//        String fieldValue = ( String )params.get( fieldName );
//        if ( params.get( IS_SEARCH ) != null && Boolean.parseBoolean( params.get( IS_SEARCH ).toString() ) )
//            fieldValue = stripSearchChars( fieldValue );
//        try
//        {
//            Class type = java.sql.Date.class;
//            if( !"java.sql.Date".equals( typeName ) )
//            {
//                type = Utils.classForName( typeName );
//            }
//
//            boolean result = false;
//            // return true for isSearch && ( '%' || 'is empty' || 'is not empty' ) - it's ok for filter operation
//            if( !Utils.isEmpty( fieldValue ) && "true".equals( params.get( IS_SEARCH ) ) &&
//                    ( fieldValue.contains( "%" ) || HttpSearchOperation.UNDEFINED_VALUE.equalsIgnoreCase( fieldValue ) || HttpSearchOperation.DEFINED_VALUE.equalsIgnoreCase( fieldValue ) ) )
//            {
//                result = true;
//            }
//            else if( !Utils.isEmpty( type ) && !Utils.isEmpty( fieldValue ) )
//            {
//                result = check( type, fieldValue, getUserInfo( request ) );
//            }
//            response.getWriter().write( String.valueOf( result ) );
//        }
//        catch( ClassNotFoundException e )
//        {
//            throw new ServletException( e );
//        }
//    }

    private static boolean check( Class type, Object value )
    {
        Object changed = Utils.changeType( value, type );
        //System.out.println( "changed = " + changed ); 
        return type.isInstance( changed );
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
