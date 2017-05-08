/** $Id: WebFormPropertyInspector.java,v 1.110 2014/02/04 13:52:34 zha Exp $ */

package com.developmentontheedge.be5.api.operationstest;

// @pending Should we be aware of enterprise package?


import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.model.ArrayProperty;
import com.developmentontheedge.beans.model.ComponentFactory;
import com.developmentontheedge.beans.model.ComponentModel;
import com.developmentontheedge.beans.model.Property;
import com.developmentontheedge.beans.model.SimpleProperty;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

abstract public class WebFormPropertyInspector implements Serializable
{
    private static final Logger log = Logger.getLogger(WebFormPropertyInspector.class.getName());


    public static final String ESCAPED_PROPERTY_NAME = BeanInfoConstants.ESCAPED_PROPERTY_NAME;
    public static final String TAG_LIST_ATTR = BeanInfoConstants.TAG_LIST_ATTR;
    public static final String DYNAMIC_TAG_LIST_ATTR = BeanInfoConstants.DYNAMIC_TAG_LIST_ATTR;
    public static final String EXTERNAL_TAG_LIST = BeanInfoConstants.EXTERNAL_TAG_LIST;
    public static final String MULTIPLE_SELECTION_LIST = BeanInfoConstants.MULTIPLE_SELECTION_LIST;
    public static final String RICH_TEXT = BeanInfoConstants.RICH_TEXT;
    public static final String OLD_RICH_TEXT = BeanInfoConstants.OLD_RICH_TEXT;
    public static final String PASSWORD_FIELD = BeanInfoConstants.PASSWORD_FIELD;
    public static final String PSEUDO_PROPERTY = BeanInfoConstants.PSEUDO_PROPERTY;
    public static final String COLOR_PICKER = BeanInfoConstants.COLOR_PICKER;
    public static final String SKIP_SERVER_NULL_CHECK = BeanInfoConstants.SKIP_SERVER_NULL_CHECK;

    // attributes for text area size
    public static final String INPUT_SIZE_ATTR = BeanInfoConstants.INPUT_SIZE_ATTR;
    public static final String COLUMN_SIZE_ATTR = BeanInfoConstants.COLUMN_SIZE_ATTR;
    public static final String NCOLUMNS_ATTR = BeanInfoConstants.NCOLUMNS_ATTR;
    public static final String NROWS_ATTR = BeanInfoConstants.NROWS_ATTR;

    // trubute to Opera which doesn't pass through checkboxes unless they can be null
    // we cannot simply drop the flag CAN_BE_NULL since we may break bean's behavior
    public static final String FORCE_CAN_BE_NULL = "force-can-be-null";
    protected static final String NL = "\n";

    public static final String ATTR_ALT = "alt";
    public static final String ATTR_MASK = "data-mask";
    public static final String ATTR_MASK_TYPE = "type";
    public static final String ATTR_MASK_REGEXP = "data-inputmask-regex";

    private FileHandler fileHandler;

    public void setFileHandler( FileHandler fileHandler )
    {
        this.fileHandler = fileHandler;
    }

    /**
     * ComponentModel that is currently assotiated with PropertyInspector.
     */
    private Object exploredBean;
    private ComponentModel componentModel = null;

    public void explore( Object bean )
    {
        if( bean == null )
        {
            componentModel = null;
            exploredBean = null;
            return;
        }
        ComponentModel mdl = ComponentFactory.getModel( exploredBean = bean);
        setComponentModel( mdl );
    }

    public Object getExploredBean()
    {
        return exploredBean;
    }


    /**
     * Indicates what properties (usual, expert, hidden,preferred) should be displayed.
     */
    private int propertyShowMode = Property.SHOW_USUAL | Property.SHOW_PREFERRED;

    /**
     * Gets current component model.
     *
     * @return the current component model
     * @pending to be declared protected
     */
    protected ComponentModel getComponentModel()
    {
        return componentModel;
    }

    /**
     * Sets current component model.
     *
     * @param componentModel the current component model
     * @pending to be declared protected
     * @deprecated
     */
    public void setComponentModel( ComponentModel componentModel )
    {
        if( componentModel == null )
        {
            return;
        }

        this.componentModel = componentModel;
    }

    /**
     * Returns current show mode.
     *
     * @return Property display mode
     * @see Property#SHOW_USUAL
     * @see Property#SHOW_EXPERT
     * @see Property#SHOW_HIDDEN
     * @see Property#SHOW_PREFERRED
     */
    public int getPropertyShowMode()
    {
        return propertyShowMode;
    }

    /**
     * Sets show mode.
     *
     * @param propertyShowMode the new show mode
     * @see Property#SHOW_USUAL
     * @see Property#SHOW_EXPERT
     * @see Property#SHOW_HIDDEN
     * @see Property#SHOW_PREFERRED
     */
    public void setPropertyShowMode( int propertyShowMode )
    {
        this.propertyShowMode = propertyShowMode;
    }

    public static boolean isJavaClassProperty( Property property )
    {
        if( !property.getName().equals( "class" ) )
            return false;

        //if( property.getOwner() instanceof DynamicProperty )
        //    return false;

        if( property.getOwner() instanceof Property.PropWrapper &&
            ( ( Property.PropWrapper )property.getOwner() ).getOwner() != null )
            return false;

        //if( property.getOwner() instanceof Property.PropWrapper )
        //   System.out.println( "property.getOwner().getClass() = " + ( ( Property.PropWrapper )property.getOwner() ).getOwner().getClass() );

        return true;
    }


    protected void assignValueToProperty( Property prop, Object val, UserInfo userInfo ) throws Exception
    {
        Class valClass = prop.getValueClass();
        try
        {
            if( Boolean.class.equals( valClass ) || boolean.class.equals( valClass ) )
            {
                String s = ( String )val;
                if( "TRUE".equalsIgnoreCase( s ) || "ON".equalsIgnoreCase( s ) ||
                    "YES".equalsIgnoreCase( s ) || "1".equalsIgnoreCase( s ) )
                {
                    prop.setValue( Boolean.TRUE );
                }
                else
                {
                    prop.setValue( Boolean.FALSE );
                }
                return;
            }

            String[] tags = getTags( prop );
            boolean canBeNull = prop.getBooleanAttribute( BeanInfoConstants.CAN_BE_NULL );

            if( tags != null && isBooleanTags( tags ) && !canBeNull ) // i.e. data from check box
            {
                String s = ( String )val;


                String[] tagSplit_0 = tags[ 0 ].split( "\u0000" );
                String tag0 = tagSplit_0.length > 1 ? tagSplit_0[ 1 ] : tags[ 0 ];

                String[] tagSplit_1 = tags[ 1 ].split( "\u0000" );
                String tag1 = tagSplit_1.length > 1 ? tagSplit_1[ 1 ] : tags[ 1 ];

                char first = tag0.charAt( 0 );
                boolean is0isTrue =
                        first == 'Y' || first == 'y' || first == 't' || first == 'T' ||
                        // russian letter 'd' or 'D' from the word DA
                        first == '\u0434' || first == '\u0414' || first == '1' ||
                        ( first == 'O' || first == 'o' ) && tag0.length() == 2;

                if( "ON".equalsIgnoreCase( s ) || "TRUE".equalsIgnoreCase( s ) ||
                    "YES".equalsIgnoreCase( s ) || "1".equalsIgnoreCase( s ) )
                {
                    prop.setValue( is0isTrue ? tag0 : tag1 );
                }
                else
                {
                    prop.setValue( is0isTrue ? tag1 : tag0 );
                }
                return;
            }

//            if( File.class.equals( valClass ) )
//            {
//                if( null != fileHandler )
//                {
//                    try
//                    {
//                        File file = fileHandler.getFile( prop.getName() );
//                        if( null != file )
//                        {
//                            prop.setValue( file );
//                            return;
//                        }
//                    }
//                    catch( IOException e )
//                    {
//                        log.severe(e.getMessage());
//                    }
//                }
//            }

            if( !canBeNull && !Boolean.TRUE.equals( prop.getAttribute( SKIP_SERVER_NULL_CHECK ) ) && ( val == null || "".equals( val ) ) )
            {
                throw new IllegalArgumentException( "NULL value is not permitted for the property \"" + prop.getName() + "\"" );
            }

            if( File.class.equals( valClass ) )
            {
                prop.setValue( ( null != val ) ? new File( ( String )val ) : null );
                return;
            }

//            if( tags != null && val != null && Utils.isEmpty( prop.getAttribute( EXTERNAL_TAG_LIST ) ) )
//            {
//                String valStr = "" + val;
//                boolean bFound = false;
//
//                //form arraylist with tags ids
//                ArrayList tagsValsList = new ArrayList();
//                for( int i = 0; i < tags.length; i++ )
//                {
//                    String tagVal = tags[ i ];
//
//                    String[] tagSplit = tags[ i ].split( "\u0000" );
//
//                    if( tagSplit.length > 1 )
//                    {
//                        tagVal = tagSplit[ 1 ];
//                    }
//
//                    tagsValsList.add( tagVal );
//                }
//                //form arraylist with vals
//                ArrayList valsList = new ArrayList();
//                if( val instanceof String[] )
//                {
//                    String[] valArray = ( String[] )val;
//                    for( int i = 0; i < valArray.length; i++ )
//                    {
//                        String currVal = valArray[ i ];
//                        valsList.add( currVal );
//                    }
//                }
//                else
//                {
//                    valsList.add( valStr );
//                }
//
//                //check that all sent values are legal(occur in tags list)
//                bFound = tagsValsList.containsAll( valsList );
//
//
//                if( !bFound && !Boolean.TRUE.equals( prop.getAttribute( DYNAMIC_TAG_LIST_ATTR ) ) )
//                {
//                    String msg = "The value \"" + valStr + "\" is not permitted for the property \"" + prop.getName() + "\". Property can be null = " + canBeNull + ".";
//                    String possible = " Possible values: { ";
//                    for( int i = 0; i < tags.length; i++ )
//                    {
//                        possible += "'" + tags[ i ] + "'";
//                        if( i != tags.length - 1 )
//                        {
//                            possible += ",";
//                        }
//                    }
//                    possible += " }";
//                    Logger.error( cat, msg + possible );
//                    throw new IllegalArgumentException( msg );
//                }
//
//                if( val instanceof String[] )
//                {
//                    // set value here because changeType will cast it valClass
//                    // taking only first element
//                    // while want to keep an array
//                    prop.setValue( val );
//                    return;
//                }
//
//                if( val instanceof String && prop.getAttribute( MULTIPLE_SELECTION_LIST ) != null )
//                {
//                    // set value here because changeType will cast it valClass
//                    // taking only first element
//                    // while want to keep an array
//                    prop.setValue( new String[] { ( String )val } );
//                    return;
//                }
//            }
//
//            prop.setValue( Utils.changeType( val, valClass, userInfo ) );
        }
        catch( NumberFormatException ignore )
        {
            if( val == null || "".equals( val ) )
            {
                prop.setValue( null );
            }
        }
    }

    private void readProperty( Property prop, Map values, UserInfo userInfo ) throws Exception
    {
        if( prop.isReadOnly() && prop instanceof SimpleProperty )
        {
            // do nothing
            return;
        }

        String nameOut = HttpParamHelper.mapNameOut( prop.getCompleteName() );

        if( prop instanceof SimpleProperty )
        {
            Object val = values.get( nameOut );
            assignValueToProperty( prop, val, userInfo );
            values.remove( nameOut );
            return;
        }

        if( prop instanceof ArrayProperty)
        {
            Object val = values.get( nameOut );
            if( val instanceof String )
            {
                val = new String[]{ ( String )val };
            }
            if( val != null && val.getClass().isArray() )
            {
                assignValueToProperty( prop, val, userInfo );
                values.remove( nameOut );
                return;
            }
        }

        else // array or composite property
        {
            int limit = prop.getVisibleCount( propertyShowMode );
            for( int i = 0; i < limit; i++ )
            {
                Property property = prop.getVisiblePropertyAt( i, propertyShowMode );
                if( isJavaClassProperty( property ) )
                {
                    continue;
                }
                readProperty( property, values, userInfo );
            }
        }
    }

    public String[] getPropertyList()
    {
        int limit = componentModel.getPropertyCount();
        ArrayList<String> vprops = new ArrayList<String>();
        for( int i = 0; i < limit; i++ )
        {
            Property property = componentModel.getPropertyAt( i );
            if( isJavaClassProperty( property ) )
            {
                continue;
            }
            vprops.add( HttpParamHelper.mapNameOut( property.getCompleteName() ) );
        }
        return vprops.toArray( new String[0] );
    }

    public Map<String,Object> getPropertyMap()
    {
        int limit = componentModel.getPropertyCount();
        Map<String,Object> vprops = new HashMap<String,Object>();
        for( int i = 0; i < limit; i++ )
        {
            Property property = componentModel.getPropertyAt( i );
            if( isJavaClassProperty( property ) )
            {
                continue;
            }
            vprops.put( HttpParamHelper.mapNameOut( property.getCompleteName() ), property.getValue() );
        }
        return vprops;
    }

    public String[] getVisibleList()
    {
        int limit = componentModel.getVisibleCount( propertyShowMode );
        ArrayList<String> vprops = new ArrayList<String>();
        for( int i = 0; i < limit; i++ )
        {
            Property property = componentModel.getVisiblePropertyAt( i, propertyShowMode );
            if( isJavaClassProperty( property ) )
            {
                continue;
            }
            vprops.add( HttpParamHelper.mapNameOut( property.getCompleteName() ) );
        }
        return vprops.toArray( new String[0] );
    }

    public boolean hasVisibleDateFields()
    {
        int limit = componentModel.getVisibleCount( propertyShowMode );
        for( int i = 0; i < limit; i++ )
        {
            Property property = componentModel.getVisiblePropertyAt( i, propertyShowMode );
            if( isJavaClassProperty( property ) )
            {
                continue;
            }
            if( property instanceof SimpleProperty)
            {
                Class type = property.getValueClass();
                if( type == null ) // how come????
                {
                    continue;
                }
                if( java.util.Date.class.isAssignableFrom( type ) && !java.sql.Time.class.isAssignableFrom( type ) ||
                    java.sql.Date.class.isAssignableFrom( type ) ||
                    java.sql.Timestamp.class.isAssignableFrom( type )
                        )
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasEditableInputWithAttribute( String attrName )
    {
        int limit = componentModel.getVisibleCount( propertyShowMode );
        for( int i = 0; i < limit; i++ )
        {
            Property property = componentModel.getVisiblePropertyAt( i, propertyShowMode );
            if( isJavaClassProperty( property ) )
            {
                continue;
            }
            if( property.getAttribute( attrName ) != null && !property.isReadOnly() )
            {
                return true;
            }
        }
        return false;
    }

    public void readSubmittedValues( Map values, UserInfo userInfo ) throws Exception
    {
        Map others = new TreeMap( String.CASE_INSENSITIVE_ORDER );
        others.putAll( values );
        int limit = componentModel.getVisibleCount( propertyShowMode );
        ArrayList vprops = new ArrayList();
        // read visibile properties into array in case
        // if visiblity will change as result of setting another property's value
        for( int i = 0; i < limit; i++ )
        {
            Property property = componentModel.getVisiblePropertyAt( i, propertyShowMode );
            //System.out.println( "property = " + property + ", class = " + property.getValueClass() + ", value = " + property.getValue() );
            if( isJavaClassProperty( property ) )
            {
                continue;
            }
            vprops.add( property );
        }

        limit = vprops.size();
        for( int i = 0; i < limit; i++ )
        {
            Property prop = ( Property )vprops.get( i );
            readProperty( prop, others, userInfo );
            //readProperty( prop, values );
        }

        // handle hidden properties for which values were for some reason submitted
        for( Iterator entries = others.entrySet().iterator(); entries.hasNext(); )
        {
            Map.Entry entry = ( Map.Entry )entries.next();
            String name = ( String )entry.getKey();
            String nameIn = HttpParamHelper.mapNameIn( name );
            Property prop = componentModel.findProperty( nameIn );
            //System.out.println( "Processing remaining " + nameIn + " = " + entry.getValue() );
            if( prop != null )
            {
                assignValueToProperty( prop, entry.getValue(), userInfo );
            }
            else
            {
                // needed to make FilterControls to work - adolg
                for ( int i = 0; i < componentModel.getPropertyCount(); i++ )
                {
                    Property p = componentModel.getPropertyAt( i );
                    if ( nameIn.equals( p.getName() ) )
                    {
                        assignValueToProperty( p, entry.getValue(), userInfo );
                        break;
                    }
                }
            }
        }
    }

    public static String[] normalizeTags( Object oTags )
    {
        String[] tags = null;
//        if( oTags instanceof List && ! ( oTags instanceof NativeArray ) )
//        {
//            List tags2 = ( List )oTags;
//            tags = new String[ tags2.size() ];
//            for( int i = 0; i < tags.length; i++ )
//            {
//                if( tags2.get( i ) instanceof String )
//                {
//                    tags[ i ] = "" + tags2.get( i ) + "\u0000" + tags2.get( i );
//                }
//                else if( tags2.get( i ) instanceof String[] )
//                {
//                    String []tags3 = ( String [] )tags2.get( i );
//                    if( tags3.length == 1 )
//                    {
//                        tags[ i ] = tags3[ 0 ] + "\u0000" + tags3[ 0 ];
//                    }
//                    else
//                    {
//                        tags[ i ] = tags3[ 1 ] + "\u0000" + tags3[ 0 ];
//                    }
//                }
//            }
//
//            return tags;
//        }

//        if( oTags instanceof NativeJavaArray )
//        {
//            oTags = ( ( NativeJavaArray )oTags ).unwrap();
//        }
//
//        if( oTags instanceof Wrapper )
//        {
//            oTags = ( ( Wrapper )oTags ).unwrap();
//        }
//
//        if( oTags instanceof NativeArray )
//        {
//            NativeArray jsArr = ( NativeArray )oTags;
//            int len = ( int )jsArr.getLength();
//            tags = new String[len];
//            for( int i = 0; i < len; i++ )
//            {
//                Object jsArrObj = jsArr.get( i, null );
//                if( jsArrObj instanceof NativeArray )
//                {
//                    NativeArray jsArr2 = ( NativeArray )jsArrObj;
//                    if( jsArr2.getLength() == 1 )
//                    {
//                        Object el = jsArr2.get( 0, null );
//                        if( el instanceof Wrapper )
//                        {
//                            el = ( ( Wrapper )el ).unwrap();
//                        }
//
//                        tags[ i ] = "" + el + "\u0000" + el;
//                    }
//                    else
//                    {
//                        Object el0 = jsArr2.get( 0, null );
//                        Object el1 = jsArr2.get( 1, null );
//                        if( el0 instanceof Wrapper )
//                        {
//                            el0 = ( ( Wrapper )el0 ).unwrap();
//                        }
//                        if( el1 instanceof Wrapper )
//                        {
//                            el1 = ( ( Wrapper )el1 ).unwrap();
//                        }
//                        tags[ i ] = "" + el1 + "\u0000" + el0;
//                    }
//                }
//                else
//                {
//                    if( jsArrObj instanceof Wrapper )
//                    {
//                        jsArrObj = ( ( Wrapper )jsArrObj ).unwrap();
//                    }
//                    tags[ i ] = "" + jsArrObj;
//                }
//            }
//        }
//        else if( oTags instanceof Scriptable )
//        {
//            ArrayList<String> list = new ArrayList<String>();
//            Scriptable params = ( Scriptable )oTags;
//            for( Object name : params.getIds() )
//            {
//                Object dispName = params.get( "" + name, params );
//                if( dispName instanceof Wrapper )
//                    dispName = ( ( Wrapper )dispName ).unwrap();
//                list.add( "" + dispName + "\u0000" + name );
//            }
//            tags = list.toArray( new String[ 0 ] );
//        }
//        else
        if( oTags instanceof String[][] )
        {
            String[][] tags2 = ( String[][] )oTags;
            tags = new String[tags2.length];
            for( int i = 0; i < tags.length; i++ )
            {
                if( tags2[ i ].length == 1 )
                {
                    tags[ i ] = tags2[ i ][ 0 ] + "\u0000" + tags2[ i ][ 0 ];
                }
                else
                {
                    tags[ i ] = tags2[ i ][ 1 ] + "\u0000" + tags2[ i ][ 0 ];
                }
            }
        }
        else if( oTags instanceof Map )
        {
            Map tagMap = ( Map )oTags;
            tags = new String[tagMap.size()];
            int i = 0;
            for( Iterator entries = tagMap.entrySet().iterator(); entries.hasNext(); )
            {
                Map.Entry entry = ( Map.Entry )entries.next();
                tags[ i++ ] = "" + entry.getValue() + "\u0000" + entry.getKey();
            }
        }
        else if( oTags instanceof String[] )
        {
            tags = ( String[] )oTags;
        }
        else if( oTags != null )
        {
            Object[] tags2 = ( Object[] )oTags;
            tags = new String[tags2.length];
            for( int i = 0; i < tags.length; i++ )
            {
                if( tags2[ i ] instanceof Object[] )
                {
                    Object[] elem = ( Object[] )tags2[ i ];
                    if( elem.length == 1 )
                    {
                        tags[ i ] = "" + elem[ 0 ] + "\u0000" + elem[ 0 ];
                    }
                    else
                    {
                        tags[ i ] = "" + elem[ 1 ] + "\u0000" + elem[ 0 ];
                    }
                }
                else
                {
                    tags[ i ] = "" + tags2[ i ];
                }
            }
        }
        return tags;
    }

    public static String[] getTags( Property prop )
    {
        Class editorClass = prop.getPropertyEditorClass();
        PropertyEditor editor = null;
        String[] tags = null;
        if( editorClass != null )
        {
            try
            {
                editor = ( PropertyEditor )editorClass.newInstance();

                editor.setValue( prop.getValue() );
                if( !editor.supportsCustomEditor() )
                {
                    tags = editor.getTags();
                }
            }
            catch( Exception exc )
            {
                log.severe( "Cannot instantiate editor of class \"" +
                                   editorClass.getName() + "\" in property \"" +
                                   prop.getCompleteName() + "\"" );
            }
        }

        if( tags == null )
        {
            //System.out.println( prop.getName() + ": noTags = " +  prop.getAttribute( BeanInfoConstants.NO_TAG_LIST ) );
            if( !Boolean.TRUE.equals( prop.getAttribute( BeanInfoConstants.NO_TAG_LIST ) ) )
            {            
                Object oTags = prop.getAttribute( TAG_LIST_ATTR );
                tags = normalizeTags( oTags );
            }
        }

        return tags;
    }

    public static boolean isBooleanTags( String[] tags )
    {
        if( tags == null || tags.length != 2 )
        {
            return false;
        }

        ArrayList tlist = new ArrayList( 2 );

        String[] tagSplit_0 = tags[ 0 ].split( "\u0000" );
        tlist.add( tagSplit_0.length > 1 ? tagSplit_0[ 1 ] : tags[ 0 ] );


        String[] tagSplit_1 = tags[ 1 ].split( "\u0000" );
        tlist.add( tagSplit_1.length > 1 ? tagSplit_1[ 1 ] : tags[ 1 ] );


        tlist.set( 0, ( ( String )tlist.get( 0 ) ).toUpperCase() );
        tlist.set( 1, ( ( String )tlist.get( 1 ) ).toUpperCase() );

        if( tlist.contains( "ON" ) && tlist.contains( "OFF" ) )
        {
            return true;
        }
        if( tlist.contains( "YES" ) && tlist.contains( "NO" ) )
        {
            return true;
        }
        if( tlist.contains( "TRUE" ) && tlist.contains( "FALSE" ) )
        {
            return true;
        }
        // russian words DA/NET
        if( tlist.contains( "\u0414\u0410" ) && tlist.contains( "\u041d\u0415\u0422" ) )
        {
            return true;
        }
        if( tlist.contains( "0" ) && tlist.contains( "1" ) )
        {
            return true;
        }

        return false;
    }
}
