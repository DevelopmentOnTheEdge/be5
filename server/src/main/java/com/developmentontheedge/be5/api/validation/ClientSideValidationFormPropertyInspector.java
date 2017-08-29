/** $Id: ClientSideValidationFormPropertyInspector.java,v 1.47 2014/04/12 09:35:36 zha Exp $ */
package com.developmentontheedge.be5.api.validation;

//import com.developmentontheedge.enterprise.DatabaseConnector;
//import com.developmentontheedge.enterprise.Features;
//import com.developmentontheedge.enterprise.HttpConstants;
//import com.developmentontheedge.enterprise.HttpParamHelper;
//import com.developmentontheedge.enterprise.Operation;
//import com.developmentontheedge.enterprise.Utils;
//import com.developmentontheedge.enterprise.caches.Cache;
//import com.developmentontheedge.enterprise.caches.CacheFactory;
//import com.developmentontheedge.enterprise.operation.FormEmitter;
//import com.developmentontheedge.enterprise.operations.HttpSearchOperation;
//import com.developmentontheedge.logging.Logger;
//import com.developmentontheedge.logging.LoggingHandle;
//import com.developmentontheedge.model.ComponentModel;
//import com.developmentontheedge.model.Property;
//import com.developmentontheedge.web.html.HtmlFormBeautifier;
//import com.developmentontheedge.web.html.HtmlFormPropertyInspector;
//import net.sf.json.JSONObject;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class used to perform client-side validation of the bean properties.
 * The validation rules can be defined in any of the following ways:
 * <ul>
 * <li>
 * The class may guess how to validate the bean by default.
 * For example, if the bean contains property with type Integer, then the "digits only"
 * rule will be applied and so on. You can see {@link Validation#defaultRules}
 * for complete set of rules that can be applied by default.
 * </li>
 * <li>
 * The class loads validation rules from database table named "validationRules".
 * Each row in this table represent a validation rule for a particular property.
 * <b>If any such rule exists for the property, all default rules will be ignored.</b>
 * </li>
 * <li>
 * The bean may already have validation rules contained in the property attribute
 * named {@link Validation#RULES_ATTR}. If so, only that rules will be applied.
 * <b>The value of the {@link Validation#RULES_ATTR} attribute should be a map
 * containing rule names as keys and corresponding messages as values.</b>
 * </li>
 * </ul>
 *
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 * @author Yaroslav Rudykh
 * @see <a href="http://erp.developmentontheedge.com/q?_t_=projectBlogs&_qn_=Project%20Blogs&ID=2826">Blog</a>
 * @see <a href="http://docs.jquery.com/Plugins/Validation">jQuery validation plugin documentation</a>
 * @see <a href="http://erp.developmentontheedge.com/wiki/jsp/Wiki?%26%231042%3B%26%231072%3B%26%231083%3B%26%231080%3B%26%231076%3B%26%231072%3B%26%231094%3B%26%231080%3B%26%231103%3B+%26%231092%3B%26%231086%3B%26%231088%3B%26%231084%3B+%26%231085%3B%26%231072%3B+%26%231082%3B%26%231083%3B%26%231080%3B%26%231077%3B%26%231085%3B%26%231090%3B%26%231077%3B">Wiki</a>
 */
public class ClientSideValidationFormPropertyInspector implements Validation
{
//
//
//    /**
//     * @inheritDoc
//     */
//    @Override
//    public boolean generate( FormEmitter emitter, Operation op, Writer out, HtmlFormBeautifier htmlFormBeautifier ) throws Exception
//    {
//        DatabaseConnector connector = emitter.getDatabaseConnector();
//        if( !emitter.getParameters().isHttpunit() &&
//            Utils.hasFeature( connector, Features.VALIDATION ) )
//        {
//            Set<String> usedRules = prepareBean( connector, op.getEntity(), emitter.getMessages() );
//            if( !usedRules.isEmpty() )
//            {
//                generateScript( connector, op, out, emitter.getMessages(), usedRules );
//            }
//        }
//        return super.generate( emitter, op, out, htmlFormBeautifier );
//    }
//
//    /**
//     * For each bean property this method checks if the property already has validation rules.
//     * If so, then it does nothing. Otherwise it loads validation rules for the specified entity
//     * from database and adds them to the property.
//     *
//     * @param connector Ubiquitous database connector.
//     * @param entityName Name of the validated entity.
//     * @param userInfo Information about the currently logged-in user.
//     * @return The Set of rules used in the specified entity.
//     */
//    private Set<String> prepareBean(
//            DatabaseConnector connector,
//            String entityName,
//            Map locMessages )
//    {
//        ComponentModel model = getComponentModel();
//        Set<String> usedRules = new HashSet<String>();
//        for( int i = 0; i < model.getPropertyCount(); i++ )
//        {
//            Property property = model.getPropertyAt( i );
//            if( ( null != property ) && !property.isReadOnly() && property.isVisible( Property.SHOW_USUAL ) )
//            {
//                Map<String, String> ruleMap = ( Map<String, String> )property.getAttribute( RULES_ATTR );
//                if( null == ruleMap )
//                {
//                    Map<String, String> attrs = getValidationAttributes( connector, entityName, property, locMessages );
//                    if( null != attrs )
//                    {
//                        property.setAttribute( RULES_ATTR, attrs );
//                        usedRules.addAll( attrs.keySet() );
//                    }
//                }
//                else
//                {
//                    usedRules.addAll( ruleMap.keySet() );
//                }
//            }
//        }
//        return usedRules;
//    }
//
//    /**
//     * Writes jQuery validation code to the output.
//     *
//     * @param connector Database connector.
//     * @param out Output writer.
//     * @param localizedMessages Localized messages.
//     * @param usedRules Collection of rules used in current operation.
//     * @throws Exception Error while generating script.
//     */
//    private void generateScript( DatabaseConnector connector,
//            Operation op,
//            Writer out,
//            Map localizedMessages,
//            Set<String> usedRules ) throws Exception
//    {
//        JSONObject rules = new JSONObject();
//        JSONObject messages = new JSONObject();
//        getJsonRulesAndMessages( op.getEntity(), rules, messages, localizedMessages, op instanceof HttpSearchOperation );
//
//        // Add CSS for error messages.
//        out.write( "<style type=\"text/css\">\n" +
//                   "label.error {\n" +
//                   "    color: red;\n" +
//                   "    margin: 10px;\n" +
//                   "}\n" +
//                   "</style>\n" );
//
//        String buildNo = Utils.getSystemSetting( connector, "BUILD_NUMBER", "0" );
//        // Add common methods.
//        //out.write( "<script type=\"text/javascript\" src=\"lib/jquery-plugins/validate/1.16/jquery.validate.js?" + buildNo + "\"></script>\n" );
//        out.write( "<script type=\"text/javascript\" src=\"lib/jquery-plugins/validate/jquery.validate.js?" + buildNo + "\"></script>\n" );
//        out.write( "<script type=\"text/javascript\" src=\"lib/jquery-plugins/validate/jquery.validate.methods.js?" + buildNo + "\"></script>\n" );
//
//        // Add custom methods stored in DB.
//        List<ValidationMethod> methods = getValidationMethods( connector, usedRules );
//        if( ( methods != null ) && ( methods.size() > 0 ) )
//        {
//            out.write( "<script type=\"text/javascript\">\n" );
//            out.write( "<!--\n" );
//            StringBuffer sb = new StringBuffer();
//            for( ValidationMethod method : methods )
//            {
//                sb.append( "jQuery.validator.addMethod(\"" )
//                        .append( method.getRule() )
//                        .append( "\", " )
//                        .append( method.getCode() )
//                        .append( ", \"" )
//                        .append( method.getDefaultMessage() )
//                        .append( "\");\n\n" );
//            }
//            out.write( sb.toString() + "\n//-->\n</script>\n" );
//        }
//
//        String serverValidate = "if( validate( form ) && serverValidate( form, realSubmit ) ) {};\n";
//        if( !op.isServerValidate() )
//        {
//            serverValidate = "if( validate( form ) ) { realSubmit( form ); };\n";
//        }
//
//        // Add initialization script.
//        out.write( "<script type=\"text/javascript\">\n" +
//                   "function realSubmit( form ) {\n" +
//                   "    if( form['_internal_Execute_hidden'] ){\n" +
//                   "        form['_internal_Execute_hidden'].value = 'Execute';\n" +
//                   "    }\n" +
//                   "    disableExecButtons();\n" +
//                   "    form.submit();\n" +
//                   "};\n\n" +
//                   "jQuery(document).ready(function() {\n" +
//                   "    // Hack on top of jQuery validation plugin.\n" +
//                   "    jQuery( '#" + ID_FORM_EXECUTE_BUTTON + "' ).removeAttr( 'onclick' );\n" +
//                   "    var form = jQuery( '#" + ID_FORM_PROPERTY_INSPECTOR + "' );\n" +
//                   "    var old = form.submit;\n" +
//                   "    form.submit = function( handler ){\n" +
//                   "        var decorator = function( event ){\n" +
//                   "            // Update RTE fields before validation.\n" +
//                   "            if ( __BE_hasRte ) validateRte( form  );\n" +
//                   "            return handler( event );\n" +
//                   "        }\n" +
//                   "        old.apply( form, [ decorator ] );\n" +
//                   "    };\n" +
//                   "    form.removeAttr( 'onsubmit' );\n" +
//                   "    form.validate({\n" +
//                   "        ignoreTitle : true,\n" +
//                   "        rules: " + rules + ",\n" +
//                   "        messages: " + messages + ",\n" +
//                   "        submitHandler: function operationFormSubmitHandler( form ){\n" + serverValidate + "}\n" +
//                   "    });\n" +
//                   "});\n" +
//                   "</script>\n" );
//    }
//
//    /**
//     * Returns validation rules and messages for the given property as a JSON objects.
//     * This method uses {@link Validation#RULES_ATTR} attributes of the bean
//     * properties to determine validation rules.
//     *
//     * @param entityName Name of the bean's entity.
//     * @param outRules JSON object to put validation rules.
//     * @param outMessages JSON object to put validation messages.
//     * @param localizedMessages Localized messages.
//     * @param isSearch if true, pattern2 method is used for regex which succeeds on values containing `%'
//     * @throws Exception Some JSON-related error.
//     * @see <a href="http://docs.jquery.com/Plugins/Validation">jQuery validation plug-in documentation</a> for complete set of validation rules.
//     */
//    private void getJsonRulesAndMessages(
//            String entityName,
//            JSONObject outRules,
//            JSONObject outMessages,
//            Map localizedMessages,
//            boolean isSearch ) throws Exception
//    {
//        ComponentModel model = getComponentModel();
//        for( int i = 0; i < model.getPropertyCount(); i++ )
//        {
//            Property property = model.getPropertyAt( i );
//            if( ( null != property ) && !property.isReadOnly() && property.isVisible( Property.SHOW_USUAL ) )
//            {
//                JSONObject rules = new JSONObject();
//                JSONObject messages = new JSONObject();
//                Map<String, ?> ruleAttr = ( Map<String, ?> )property.getAttribute( RULES_ATTR );
//                //System.out.println( "ruleAttr = " + ruleAttr );
//                if( null != ruleAttr )
//                {
//                    for( Map.Entry<String, ?> entry : ruleAttr.entrySet() )
//                    {
//                        String entity = entityName;
//                        String queryName = null;
//                        String column = property.getName();
//
//                        Object intervalFrom = null;
//                        Object intervalTo = null;
//
//                        String key = entry.getKey();
//                        Object value = isSearch ? "search" : Boolean.TRUE;
//
//                        String message = null;
//
//                        Map<String,String> extraParams = null;
//                        if( entry.getValue() instanceof Validation.UniqueStruct )
//                        {
//                            Validation.UniqueStruct us = ( Validation.UniqueStruct )entry.getValue();
//                            entity = us.entity;
//                            column = us.column;
//                            message = us.message;
//                            extraParams = us.extraParams;
//                        }
//                        else if( entry.getValue() instanceof Validation.QueryStruct )
//                        {
//                            Validation.QueryStruct us = ( Validation.QueryStruct )entry.getValue();
//                            entity = us.entity;
//                            queryName = us.query;
//                            message = us.message;
//                            extraParams = us.extraParams;
//                        }
//                        else if( entry.getValue() instanceof Validation.IntervalStruct )
//                        {
//                            Validation.IntervalStruct us = ( Validation.IntervalStruct )entry.getValue();
//                            intervalFrom = us.intervalFrom;
//                            intervalTo = us.intervalTo;
//                            message = us.message;
//                        }
//                        else
//                        {
//                            message = entry.getValue().toString();
//                        }
//
//                        // TODO: get rid of if/else somehow
//                        if( UNIQUE.equals( key ) )
//                        {
//                            key = REMOTE;
//                            value = CheckUniqueServlet.URI + "?" +
//                                    HttpConstants.TABLE_NAME_PARAM + "=" + entity + "&" +
//                                    HttpConstants.FIELD_NAME_PARAM + "=" + column + "&" +
//                                    HttpConstants.PROP_NAME_PARAM + "=" + property.getName();
//                            if( extraParams != null )
//                            {
//                                for( Map.Entry<String,String> entryEl : extraParams.entrySet() )
//                                {
//                                    value = "" + value + "&" + entryEl.getKey() + "=" + entryEl.getValue();
//                                }
//                            }
//
//                            rules.put( REMOTE, value );
//                            if( !Utils.isEmpty( message ) )
//                            {
//                                messages.put( REMOTE, Utils.getMessage( localizedMessages, message ) );
//                            }
//                            // no usual rule
//                            continue;
//                        }
//                        else if( QUERY.equals( key ) )
//                        {
//                            key = REMOTE;
//                            value = CheckQueryServlet.URI + "?" +
//                                    HttpConstants.TABLE_NAME_PARAM + "=" + entity + "&" +
//                                    HttpConstants.QUERY_NAME_PARAM + "=" + queryName + "&" +
//                                    HttpConstants.PROP_NAME_PARAM + "=" + property.getName();
//                            if( extraParams != null )
//                            {
//                                for( Map.Entry<String,String> entryEl : extraParams.entrySet() )
//                                {
//                                    value = "" + value + "&" + entryEl.getKey() + "=" + entryEl.getValue();
//                                }
//                            }
//
//                            rules.put( REMOTE, value );
//                            if( !Utils.isEmpty( message ) )
//                            {
//                                messages.put( REMOTE, Utils.getMessage( localizedMessages, message ) );
//                            }
//                            // no usual rule
//                            continue;
//                        }
//                        else if( DATE.equals( key ) )
//                        {
//                            // 2 rules: remote & usual
//                            Class <?> clazz = AbstractRule.getClassByOwner( property );
//                            String remoteValue = CheckDateServlet.URI + "?" +
//                                    CheckDateServlet.TYPE_PARAM + "=" + clazz.getName() + "&" +
//                                    HttpConstants.FIELD_NAME_PARAM + "=" + HttpParamHelper.mapNameOut( column ) +
//                                    ( isSearch ? ( "&" + CheckDateServlet.IS_SEARCH + "=true" ) : "" );
//
//                            rules.put( REMOTE, remoteValue );
//                            if( !Utils.isEmpty( message ) )
//                            {
//                                messages.put( REMOTE, Utils.getMessage( localizedMessages, message ) );
//                            }
//                        }
//                        else if( INTERVAL.equals( key ) )
//                        {
//                            Class <?> clazz = AbstractRule.getClassByOwner( property );
//                            String remoteValue = CheckIntervalServlet.URI + "?" +
//                                    CheckDateServlet.TYPE_PARAM + "=" + clazz.getName() + "&" +
//                                    HttpConstants.FIELD_NAME_PARAM + "=" + HttpParamHelper.mapNameOut( column ) + "&" +
//                                    HttpConstants.INTERVAL_FROM_PARAM + "=" + intervalFrom + "&" +
//                                    HttpConstants.INTERVAL_TO_PARAM + "=" + intervalTo +
//                                    ( isSearch ? ( "&" + CheckIntervalServlet.IS_SEARCH + "=true" ) : "" );
//
//                            rules.put( REMOTE, remoteValue );
//                            if( !Utils.isEmpty( message ) )
//                            {
//                                messages.put( REMOTE, Utils.getMessage( localizedMessages, message ) );
//                            }
//
//                            // no usual rule
//                            continue;
//                        }
//                        else if( key.matches( "\\/.*\\/[gim]*" ) )
//                        {
//                            // String containing JavaScript RegExp (e.g. /^[a-z]+$/i)
//                            value = key;
//                            // pattern2 is kept for search, as we have only 1 parameter per validation
//                            // method; the rest of methods in lib/jquery.validate.methods.js take parameter
//                            // (either true or "search")
//                            key = isSearch ? PATTERN2 : PATTERN;
//                        }
//                        // end of TODO
//
//                        //System.out.println( "key = " + key );
//                        //System.out.println( "value = " + value );
//
//                        rules.put( key, value );
//                        if( !Utils.isEmpty( message ) )
//                        {
//                            messages.put( key, Utils.getMessage( localizedMessages, message ) );
//                        }
//                    }
//                }
//                if( rules.size() > 0 && property.getAttribute( Operation.EXTERNAL_TAG_LIST ) == null )
//                {
//                    outRules.put( HttpParamHelper.mapNameOut( property.getName() ), rules );
//                }
//                if( messages.size() > 0 && property.getAttribute( Operation.EXTERNAL_TAG_LIST ) == null  )
//                {
//                    outMessages.put( HttpParamHelper.mapNameOut( property.getName() ), messages );
//                }
//            }
//        }
//    }
//
//    /**
//     * Tries to load validation rules for the given property from database.
//     * If no rules exist in database, then return default ones.
//     *
//     * @param connector Database connector
//     * @param entityName Name of the entity.
//     * @param userInfo Information about the currently logged-in user.
//     * @param property Bean property.
//     * @return Collection of rules for the given entity with property names as keys and lists of rules as values.
//     */
//    private static Map<String, String> getValidationAttributes(
//            DatabaseConnector connector,
//            String entityName,
//            Property property,
//            Map locMessages )
//    {
//        Map<String, Collection<AbstractRule>> ruleMap =
//                getPersistentRules( connector, entityName );
//
//        Collection<AbstractRule> rules =
//                ( ( null != ruleMap ) && ruleMap.containsKey( property.getName() ) )
//                ? ruleMap.get( property.getName() )
//                : defaultRules;
//
//        Map<String, String> result = new LinkedHashMap<String, String>();
//        for( AbstractRule rule : rules )
//        {
//            if( rule.isApplicable( property ) )
//            {
//                result.put( rule.getRule(), Utils.getMessage( locMessages, rule.getMessage() ) );
//                //System.out.println( property.getName() + ": " + result );
//            }
//        }
//        return result;
//    }
//
//    /**
//     * Loads validation rules from database, caches results if any.
//     *
//     * @param connector Database connector
//     * @param entityName Name of the entity
//     * @param userInfo Information about the currently logged-in user.
//     * @return Collection of rules for the given entity with property names as keys and lists of rules as values.
//     */
//    private static Map<String, Collection<AbstractRule>> getPersistentRules(
//            DatabaseConnector connector,
//            String entityName )
//    {
//        Cache cache = CacheFactory.getCacheInstance( RULES_CACHE_ENTRY );
//        Map<String, Collection<AbstractRule>> map = ( Map<String, Collection<AbstractRule>> )cache.get( entityName );
//
//        if( null == map )
//        {
//            try
//            {
//                String sql = "SELECT " + ENTITY_NAME + " AS \"entityName\", "
//                             + PROPERTY_NAME + " AS \"propertyName\","
//                             + connector.getAnalyzer().quoteIdentifier( RULE ) + " AS \"rule\","
//                             + MESSAGE + " AS \"message\""
//                             + " FROM " + RULES_TABLE_NAME
//                             + " WHERE " + ENTITY_NAME + " = " + Utils.safestr( connector, entityName, true );
//
//                List<PersistentRule> rules = Utils.readAsList( connector, sql, PersistentRule.class );
//                map = new LinkedHashMap<String, Collection<AbstractRule>>();
//                for( PersistentRule rule : rules )
//                {
//                    String name = rule.getPropertyName();
//                    if( !map.containsKey( name ) )
//                    {
//                        map.put( name, new ArrayList<AbstractRule>() );
//                    }
//                    map.get( name ).add( rule );
//                }
//                cache.put( entityName, map );
//            }
//            catch( Exception e )
//            {
//                Logger.error( cat, "Unable to load client-side validation rules from database", e );
//            }
//        }
//        return map;
//    }
//
//    /**
//     * Loads validation methods from database, caches results if any.
//     *
//     * @param connector Database connector
//     * @param userInfo Information about the currently logged-in user.
//     * @param rules Map of Rules used
//     * @return Collection of methods.
//     */
//    private static List<ValidationMethod> getValidationMethods(
//            DatabaseConnector connector,
//            Set<String> rules )
//    {
//        List<ValidationMethod> methods = new LinkedList<ValidationMethod>();
//        for( String rule : rules )
//        {
//            ValidationMethod method = getValidationMethod( connector, rule );
//
//            if( method != null )
//            {
//                methods.add( method );
//            }
//        }
//        return methods;
//    }
//
//    private static ValidationMethod getValidationMethod(
//            DatabaseConnector connector,
//            String rule )
//    {
//        Cache cache = CacheFactory.getCacheInstance( METHODS_CACHE_ENTRY );
//        ValidationMethod method = ( ValidationMethod )cache.get( rule );
//        if( method == null )
//        {
//            try
//            {
//                String sql = "SELECT " + connector.getAnalyzer().quoteIdentifier( RULE ) + " AS \"rule\", "
//                             + CODE + " AS \"code\","
//                             + DEFAULT_MESSAGE + " AS \"defaultMessage\""
//                             + " FROM " + METHODS_TABLE_NAME
//                             + " WHERE " + connector.getAnalyzer().quoteIdentifier( RULE ) + " = " + Utils.safestr( connector, rule, true );
//
//                List<ValidationMethod> methods = Utils.readAsList( connector, sql, ValidationMethod.class );
//
//                if( methods.size() > 0 )
//                {
//                    method = methods.get( 0 );
//                    cache.put( rule, method );
//                }
//                else
//                {
//                    ValidationMethod missing = new ValidationMethod();
//                    missing.setMissing( true );
//                    cache.put( rule, missing );
//                    return null;
//                }
//            }
//            catch( Exception e )
//            {
//                Logger.error( cat, "Unable to load client-side validation methods from database", e );
//            }
//        }
//        else if( method.isMissing() )
//        {
//            return null;
//        }
//        return method;
//    }
}
