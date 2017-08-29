/** $Id: RecordEx.java,v 1.39 2014/04/27 11:07:21 zha Exp $ */

package com.developmentontheedge.be5.beans;

import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.DynamicProperty;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

abstract public class RecordEx extends JDBCRecordAdapter
{
//    public static final String ORIG_LABEL = "orig-column-label";
//
//    public static final String HIDDEN_COLUMN_PREFIX = "___";
//    public static final String GLUE_COLUMN_PREFIX = "+";
//    public static final String EXTRA_HEADER_COLUMN_PREFIX = ";";
//
//    public static final String REF_TABLE = "ref-table-attribute";
//    public static final String REF_COLUMN = "ref-column-attribute";
//    public static final String REF_VALUE = "ref-value-attribute";
//    public static final String REF_COMMENT = "ref-comment-attribute";
//
//    public static final String PROPERTY_INFO_LIST = "property-info-list";
//
//    public static void extractPropertyInfos( DynamicProperty prop )
//    {
//        String label = prop.getName();
//        if( label.indexOf( ";<" ) < 0 )
//        {
//            return;
//        }
//        StringTokenizer st = new StringTokenizer( label, ";" );
//        if( st.countTokens() > 1 )
//        {
//            String name = st.nextToken(); // ignore label itself
//            prop.setName( name );
//            prop.setDisplayName( name );
//
//            Hashtable list = new Hashtable();
//            while( st.hasMoreTokens() )
//            {
//                PropertyInfo pi = PropertyInfo.withCache( st.nextToken() );
//                list.put( pi.getType(), pi );
//            }
//            prop.setAttribute( PROPERTY_INFO_LIST, list );
//        }
//    }
//
//    public static Map assignPropertyInfos( DynamicProperty prop, String piStr )
//    {
//        StringTokenizer st = new StringTokenizer( piStr, ";" );
//        if( st.countTokens() > 0 )
//        {
//            Hashtable list = new Hashtable();
//            while( st.hasMoreTokens() )
//            {
//                PropertyInfo pi = PropertyInfo.withCache( st.nextToken() );
//                list.put( pi.getType(), pi );
//            }
//            prop.setAttribute( PROPERTY_INFO_LIST, list );
//            return list;
//        }
//        return null;
//    }
//
//    public static void addLink( DynamicProperty prop, String table, String queryName, String using, String columns )
//    {
//        Map piList = ( Map )prop.getAttribute( RecordEx.PROPERTY_INFO_LIST );
//        if( piList == null )
//        {
//            prop.setAttribute( RecordEx.PROPERTY_INFO_LIST, piList = new Hashtable() );
//        }
//        using = Utils.subst( using, "'", "%27" );
//        String links = "<link";
//        links += " table=\"" + table + "\"";
//        links += " queryName=\"" + queryName + "\"";
//        links += " using=\"" + using + "\"";
//        links += " columns=\"" + columns + "\"";
//        links += " />";
//        PropertyInfo pi = PropertyInfo.withCache( links );
//        piList.put( pi.getType(), pi );
//    }
//
//    public static void addGrouping( DynamicProperty prop )
//    {
//        Map piList = ( Map )prop.getAttribute( RecordEx.PROPERTY_INFO_LIST );
//        if( piList == null )
//        {
//            prop.setAttribute( RecordEx.PROPERTY_INFO_LIST, piList = new Hashtable() );
//        }
//        String links = "<grouping />";
//        PropertyInfo pi = PropertyInfo.withCache( links );
//        piList.put( pi.getType(), pi );
//    }

    //private ResultSetQueryIterator queryIterator;

    public RecordEx()
    {
        super( true /*bAddAlways*/ );
    }

//    public RecordEx(ResultSetQueryIterator queryIterator )
//    {
//        super( true /*bAddAlways*/ );
//        this.queryIterator = queryIterator;
//    }
//
    @Override
    protected void initialize()
    {
        super.initialize();
//        preprocessProperties( queryIterator != null ? queryIterator.getConnector() : null,
//                              properties, propHash );
//        //This is for serializability, affects clustered environment.
//        //(ResultSetQueryIterator is not serializable itself)
//        queryIterator = null;
    }
//
//    /**
//     * Make actions dpending on field names:
//     * hide(___xxx), or handle references etc.
//     * @param properties
//     * @param propHash
//     */
//    static void preprocessProperties( DatabaseConnector connector, List<DynamicProperty> properties, Map<String, DynamicProperty> propHash )
//    {
//        int lim = properties.size();
//
//        for( int i = 0; i < lim; i++ )
//        {
//            DynamicProperty prop = properties.get( i );
//
//            String origName = prop.getName();
//            prop.setAttribute( ORIG_LABEL, origName );
//
//            if( origName.startsWith( EXTRA_HEADER_COLUMN_PREFIX ) )
//            {
//                Object val = prop.getValue();
//                if( val instanceof String )
//                {
//                    prop.setName( origName.substring( 1 ) + ";" + val.toString() );
//                }
//            }
////            else if( origName.toLowerCase().startsWith( DatabaseConstants.ENCRYPT_COLUMN_PREFIX ) )
////            {
////                Object val = prop.getValue();
////                if( val instanceof String )
////                {
////                    try
////                    {
////                        prop.setValue( CryptoUtils.decrypt( val.toString() ) );
////                        prop.setAttribute( BeanInfoConstants.PASSWORD_FIELD, Boolean.TRUE );
////                    }
////                    catch( Exception ign )
////                    {
////                        prop.setValue( "Unable to decrypt: " + val );
////                    }
////                }
////            }
//
//            extractPropertyInfos( prop ); // new approach - code below will be removed some day
//
//            String colName = prop.getName();
//
//            String colLabel = colName;
//            String refTable = null;
//            String refColumn = null;
//            String refValue = null;
//
//            StringTokenizer st = new StringTokenizer( colName, ";" );
//            int nTok = st.countTokens();
//            if( nTok == 4 )
//            {
//                colLabel = st.nextToken();
//                refValue = st.nextToken();
//                refTable = st.nextToken();
//                refColumn = st.nextToken();
//                colName = colLabel;
//                prop.setName( colName );
//                prop.setDisplayName( colLabel );
//            }
//
//            if( refTable != null )
//            {
//                prop.setAttribute( REF_TABLE, refTable );
//            }
//            if( refColumn != null )
//            {
//                prop.setAttribute( REF_COLUMN, refColumn );
//            }
//            if( refValue != null )
//            {
//                prop.setAttribute( REF_VALUE, refValue );
//            }
//
//            //System.err.println( "colName = \"" + colName + "\"" );
//            //System.err.println( "colLabel = \"" + colLabel + "\"" );
//            //System.err.println( "prop.getDisplayName() = \"" + prop.getDisplayName() + "\"" );
//
//            // We will use the following convention
//            // if the column label starts with HIDDEN_COLUMN_PREFIX - it is hidden
//            if( colLabel.startsWith( HIDDEN_COLUMN_PREFIX ) )
//            {
//                prop.setHidden( true );
//            }
//            // if the column label starts with GLUE_COLUMN_PREFIX
//            // column's value should be glued to the columsn with the same name
//            else if( colLabel.startsWith( GLUE_COLUMN_PREFIX ) )
//            {
//                String targetName = colLabel.substring( 1 );
//                for( int j = i - 1; j >= 0; j-- )
//                {
//                    DynamicProperty tp = properties.get( j );
//                    if( targetName.equals( tp.getName() ) )
//                    {
//                        Object val = tp.getValue();
//                        if( val instanceof String && prop.getValue() != null )
//                        {
//                            tp.setValue( val.toString() + prop.getValue() );
//                        }
//                        break;
//                    }
//                }
//                //prop.setValue( null ); // we don't need it to anymore
//                prop.setHidden( true );
//            }
//
//            // if the column label starts with EXTRA_HEADER_COLUMN_PREFIX
//            // column's value contains attributes which should not be displayed because
//            // DBMS impose restriction on column label length
//            // For instance Oracle do not allow column label length more than 30 chars
//            if( origName.startsWith( EXTRA_HEADER_COLUMN_PREFIX ) )
//            {
//                String targetName = origName.substring( 1 );
//                for( int j = i - 1; j >= 0; j-- )
//                {
//                    DynamicProperty tp = properties.get( j );
//                    if( targetName.equals( tp.getName() ) )
//                    {
//                        if( refTable != null )
//                        {
//                            tp.setAttribute( REF_TABLE, refTable );
//                        }
//                        if( refColumn != null )
//                        {
//                            tp.setAttribute( REF_COLUMN, refColumn );
//                        }
//                        if( refValue != null )
//                        {
//                            tp.setAttribute( REF_VALUE, refValue );
//                        }
//                        Object list = prop.getAttribute( PROPERTY_INFO_LIST );
//                        if( list != null )
//                        {
//                            Object targetList = tp.getAttribute( PROPERTY_INFO_LIST );
//                            if( targetList == null )
//                            {
//                                tp.setAttribute( PROPERTY_INFO_LIST, list );
//                            }
//                            else
//                            {
//                                ( ( Map )targetList ).putAll( ( Map )list );
//                            }
//                        }
//                        break;
//                    }
//                }
//                prop.setName( colName = origName );
//                //prop.setValue( null ); // we don't need it to anymore
//                prop.setHidden( true );
//            }
//
//            if( !colName.equals( origName ) )
//            {
//                // we do not remove old entry form hash - just in case if someone
//                // will need to fidn property using old name
//                propHash.put( colName, prop );
//            }
//        }
//    }
}
