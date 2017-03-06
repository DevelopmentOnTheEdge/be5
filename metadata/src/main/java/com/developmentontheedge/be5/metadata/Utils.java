package com.developmentontheedge.be5.metadata;

import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.dbms.DbmsConnector; 

/**
 * Temporary class for imported and unclassified constants from BeanExplorer EE.
 */
public class Utils 
{
	public static Logger log = Logger.getLogger(Utils.class.getName());
	
    /**
     * This method is doing same actions as {@link #toInClause(Object[] values) toInClause}
     *
     * @param values collection of the values, that needs to be in "IN" clause
     * @return surrounded values
     */
    public static String toInClause( Collection values )
    {
        return toInClause( values, false );
    }

    /**
     * This method is doing same actions as {@link #toInClause(Object[] values) toInClause}
     *
     * @param values collection of the values, that needs to be in "IN" clause
     * @param isNumeric specifies, that values in collection has numeric data type
     * @return surrounded values
     */
    public static String toInClause( Collection values, boolean isNumeric )
    {
        return toInClause( values, isNumeric, null );
    }

    /**
     * Surround specified collection values ( "(", ",", ")" ) for putting them into IN clause.
     * <br/>Example: SELECT * FROM some_table WHERE some_column IN (values[0], values[1], ...)
     *
     * <br/><br/><b>Attention!!!</b> Values (except numeric values) in the collection must be already formatted for SQL syntax.
     * If collection contains numeric data, you must set isNumeric parameter to true.
     *
     * @param isNumeric specifies, that values in collection has numeric data type
     * @param values collection of the values, that needs to be in "IN" clause
     * @param prefix string value to be added to every element in the list
     * @return surrounded values
     */
    public static String toInClause( Collection values, boolean isNumeric, String prefix )
    {
        StringBuilder clause = new StringBuilder( "(" );
        Set set = new HashSet();
        boolean first = true;
        prefix = ( prefix == null ) ? "" : prefix;
        for( Object v : values )
        {
            if ( v == null )
            {
                continue;
            }

            if( isNumeric )
            {
                try
                {
                    if( v != null && !"null".equalsIgnoreCase( v.toString() ) )
                    {
                        Double.parseDouble( v.toString() );
                    }
                }
                catch( NumberFormatException exc )
                {
                    log.log(Level.WARNING, "toInClause: Bad numeric value '" + v + "'");
                    continue;
                }
            }

            if( !set.add( v ) )
            {
                continue;
            }

            if( first )
            {
                first = false;
            }
            else
            {
                clause.append( ',' );
            }

            if( isNumeric )
            {
                clause.append( prefix ).append( v );
            }
            else
            {
                String val = v.toString();
                if( val.startsWith( "'" ) && val.endsWith( "'" ) )
                {
                    clause.append( val );
                }
                else
                {
                    clause.append( "'" ).append( prefix ).append( val ).append( "'" );
                }
            }
        }
        clause.append( ")" );
        return clause.toString();
    }
	

    public static HashMap loadEntityEnums( DbmsConnector connector, String entity, DynamicPropertySet bean )
            throws SQLException
    {
        HashMap enums = new HashMap();
        // first try to process MySQL's 'enum' types
        // for example column with the type "enum( 'male', 'female' )"
        // must be forced to have only two possible values - 'male' and 'female'
        ResultSet rs = null;
        if( connector.isMySQL() )
        {
            try
            {
                rs = connector.executeQuery( "DESC " + connector.getAnalyzer().quoteIdentifier( entity ) );
                while( rs.next() )
                {
                    String name = rs.getString( 1 );
                    String type = rs.getString( 2 );
                    if( type.startsWith( "enum(" ) )
                    {
                        ArrayList<String> values = new ArrayList<String>();
                        StringTokenizer st = new StringTokenizer( type.substring( 5 ), ",')" );
                        while( st.hasMoreTokens() )
                        {
                            values.add( st.nextToken() );
                        }
                        enums.put( name, values.toArray( new String[0] ) );
                    }
                }
            }
            finally
            {
                connector.close( rs );
            }
        }

        // try to read Oracle's constraints
        // that are stored like sex IN ( 'male', 'female' )
        if( connector.isOracle() )
        {
            String sql = "SELECT uc.SEARCH_CONDITION AS \"Constr\" ";
            sql += "FROM user_constraints uc ";
            sql += "WHERE uc.CONSTRAINT_TYPE = 'C' AND uc.TABLE_NAME = UPPER( '" + entity + "' ) ";

            List<String> constraints = Utils.readAsListOfStrings( connector, sql, TableConstraintsCache.getInstance() );

            for( String constr : constraints )
            {
                StringTokenizer st = new StringTokenizer( constr.trim() );
                int nTok = st.countTokens();
                if( nTok < 3 )
                {
                    continue;
                }
                String colName = st.nextToken().toUpperCase();
                String in = st.nextToken();
                if( !"IN".equalsIgnoreCase( in ) )
                {
                    continue;
                }
                if( bean != null && bean.getProperty( colName ) == null)
                {
                    continue;
                }
                ArrayList<String> values = new ArrayList<String>();
                try
                {
                    do
                    {
                        String val = st.nextToken( "(,')" );
                        if( !Utils.isEmpty( val ) )
                        {
                            values.add( val );
                        }
                    }
                    while( st.hasMoreTokens() );
                }
                catch( NoSuchElementException ignore )
                {
                }
                if( values.size() > 0 )
                {
                    enums.put( colName, values.toArray( new String[0] ) );
                }
            }
        }

        // try to read DB2's constraints
        // that are stored like sex IN ( 'male', 'female' )
        if( connector.isDb2() )
        {

            String sql = "SELECT TEXT AS \"Constr\" ";
            sql += "FROM SYSIBM.SYSCHECKS ";
            sql += "WHERE TYPE = 'C' AND TBNAME = UPPER( '" + entity + "' ) ";

            List<String> constraints = Utils.readAsListOfStrings( connector, sql, TableConstraintsCache.getInstance() );

            for( String constr : constraints )
            {
                StringTokenizer st = new StringTokenizer( constr.trim() );
                int nTok = st.countTokens();
                if( nTok < 3 )
                {
                    continue;
                }
                String colName = st.nextToken().toUpperCase();
                String in = st.nextToken();
                if( !"IN".equalsIgnoreCase( in ) )
                {
                    continue;
                }
                if( bean != null && bean.getProperty( colName ) == null)
                {
                    continue;
                }
                ArrayList<String> values = new ArrayList<String>();
                try
                {
                    do
                    {
                        String val = st.nextToken( "(,')" );
                        if( !Utils.isEmpty( val ) )
                        {
                            values.add( val );
                        }
                    }
                    while( st.hasMoreTokens() );
                }
                catch( NoSuchElementException ignore )
                {
                }
                if( values.size() > 0 )
                {
                    enums.put( colName, values.toArray( new String[0] ) );
                }
            }
        }

        // try to read SQLServer's constraints
        // that are stored like sex IN ( 'male', 'female' )
        // SQL servers keeps them as comments
        // ([type] = 'JavaScript' or ([type] = 'SQL' or [type] = 'Java'))
        if( connector.isSQLServer() )
        {
            String sql = "SELECT name, definition FROM sys.check_constraints ";
            sql += "WHERE parent_object_id = OBJECT_ID('" + entity + "') ";

            Map<?,?>checks = null; 
            try
            {
                checks = Utils.readAsMap( connector, sql, TableConstraintsCache.getInstance() ); 
            }
            catch( SQLException se )
            {
                String likeEnt = entity;
                if( likeEnt.length() > 9 )
                {
                    likeEnt = entity.substring( 0, 9 );
                }

                sql = "SELECT so.name AS \"name\", sc.text AS \"text\" ";
                sql += "FROM sysobjects so, syscomments sc  ";
                sql += "WHERE so.parent_obj = OBJECT_ID( '" + entity + "' ) ";
                sql += "   AND so.name LIKE 'CK__" + likeEnt + "%' AND sc.ID = so.ID AND LEFT( sc.text, 2 ) = '([' ";

                checks = Utils.readAsMap( connector, sql, TableConstraintsCache.getInstance() ); 
            }

            for( Map.Entry<?,?> check : checks.entrySet() )
            {
                // extract name stored in CK__queries__type__564B5FDE
                String ckName = check.getKey().toString();
                char delim = '\uFFFF';
                ckName = Utils.subst( ckName, "__", "" + delim + delim, "" );
                StringTokenizer st = new StringTokenizer( ckName, "" + delim );
                if( st.countTokens() != 4 )
                {
                    continue;
                }
                st.nextToken();
                st.nextToken();
                String colName = st.nextToken();

                String vals = check.getValue().toString();
                if( bean != null )
                {
                    boolean bFound = false;
                    for( Iterator<String> props = bean.nameIterator(); props.hasNext(); )
                    {
                        String propName = props.next();
                        //System.out.println( propName + "=>" + colName );
                        if( propName.startsWith( colName ) )
                        {
                            if( vals.indexOf( "[" + propName + "]" ) < 0 )
                            {
                                continue;
                            }
                            colName = propName;
                            bFound = true;
                            break;
                        }
                    }
                    if( !bFound )
                    {
                        continue;
                    }
                }

                //System.out.println( colName + ": " + vals );

                // ([type] = 'JavaScript' or ([type] = 'SQL' or [type] = 'Java'))
                vals = Utils.subst( vals, "[" + colName + "] = ", "", "" );
                vals = Utils.subst( vals, "[" + colName + "]=", "", "" );
                //System.err.println( vals );

                // ('JavaScript' or ('SQL' or 'Java'))
                vals = Utils.subst( vals, "' or (", "", "" );
                vals = Utils.subst( vals, "' OR (", "", "" );
                //System.err.println( vals );

                // ('JavaScript'SQL' or 'Java'))
                vals = Utils.subst( vals, "' or ", "", "" );
                vals = Utils.subst( vals, "' OR ", "", "" );
                //System.err.println( vals );

                // ('JavaScript'SQL'Java'))
                vals = vals.substring( 2, vals.lastIndexOf( "')" ) );
                //System.err.println( vals );

                // JavaScript'SQL'Java
                st = new StringTokenizer( vals, "'" );
                ArrayList<String> values = new ArrayList<String>();
                while( st.hasMoreTokens() )
                {
                    String val = st.nextToken();
                    if( !Utils.isEmpty( val ) )
                    {
                        values.add( val );
                    }
                }
                if( values.size() > 0 )
                {
                    enums.put( colName, values.toArray( new String[0] ) );
                }
            }
        }


        if( connector.isPostgreSQL() )
        {
            // Use ANSI SQL complaint schema
            final String sql = "SELECT cc.column_name, ch.check_clause FROM " +
                        "information_schema.table_constraints c, " +
                        "information_schema.constraint_column_usage cc, " +
                        "information_schema.check_constraints ch " +
                        "WHERE c.constraint_type = 'CHECK' " +
                        "AND cc.constraint_name = c.constraint_name " +
                        "AND cc.constraint_name = ch.constraint_name " +
                        "AND cc.table_name = '" + Utils.safestr( connector, entity.toLowerCase() ) + "'";
            Map<?,?>checks = Utils.readAsMap( connector, sql, TableConstraintsCache.getInstance() ); 

            for( Map.Entry<?,?> entry : checks.entrySet() )
            {
                final String colName = entry.getKey().toString().trim();
                final String check = entry.getValue().toString().trim();
                // enum-like constraint usually looks like
                //  ((isdefault)::text = ANY ((ARRAY['no'::character varying, 'yes'::character varying])::text[]))

                final String arrayBegining = "(ARRAY[";
                int abegin = check.indexOf( arrayBegining );
                if (abegin == -1)
                    continue;

                int aend = check.indexOf( "])", abegin );
                if (aend == -1)
                    continue;

                String typeList = check.substring( abegin + arrayBegining.length(), aend );
                StringTokenizer st = new StringTokenizer( typeList, "," );
                List<String> values = new ArrayList<String>();
                while (st.hasMoreTokens())
                {
                    String t = st.nextToken().trim();
                    int stInd = t.startsWith( "(" ) ? 1 : 0;
                    int d = t.indexOf( "::" );
                    if (d == -1) // wow, that sucks
                        continue;

                    String val = t.substring( stInd, d );

                    // TODO do it some more effective way
                    if (val.startsWith( "'" ) && val.endsWith( "'" ))
                        val = val.substring( 1, val.length() -1 );

                    // now 'val' contains 'yes' or 'no'
                    values.add(val);
                }
                if( values.size() > 0 )
                {
                    enums.put( colName, values.toArray( new String[0] ) );
                }
            }

        }

        if( connector.isSQLite() )
        {
            final String sql = "SELECT sql FROM sqlite_master WHERE type = 'table' AND tbl_name = '" + Utils.safestr( connector, entity ) + "'";
            
            List<String>checks = Utils.readAsListOfStrings( connector, sql, TableConstraintsCache.getInstance() ); 
            if( checks.size() == 1 )
            {
                 String orig = checks.get( 0 ); 
                 for( DynamicProperty prop : bean )
                 {
                     int ind1, ind2;
                     String vals = null; 

                     String pat1 = "/*" + prop.getName().toUpperCase() + ":ENUM(";
                     ind1 = orig.indexOf( pat1 );
                     if( ind1 > 0 )
                     {
                         ind2 = orig.indexOf( ")", ind1 );
                         if( ind2 > 0 )
                         {
                             vals = orig.substring( ind1 + pat1.length(), ind2 );
                         } 
                     }
 
                     String pat2 = "/*" + prop.getName().toUpperCase() + " :ENUM(";
                     ind1 = orig.indexOf( pat2 );
                     if( ind1 > 0 )
                     {
                         ind2 = orig.indexOf( ")", ind1 );
                         if( ind2 > 0 )
                         {
                             vals = orig.substring( ind1 + pat2.length(), ind2 );
                         } 
                     }

                     if( vals != null )
                     {
                         ArrayList<String> values = new ArrayList<String>();
                         StringTokenizer st = new StringTokenizer( vals, ",'" );
                         while( st.hasMoreTokens() )
                         {
                             String tok = st.nextToken();  
                             if( !"".equals( tok.trim() ) )
                             {
                                 values.add( tok );
                             }
                         }
                         enums.put( prop.getName(), values.toArray( new String[0] ) );
                     }
                 }
            }
        }

        return enums;
    }
    
    
}
