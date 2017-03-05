package com.developmentontheedge.be5.metadata.serialization.yaml;

import static com.developmentontheedge.be5.metadata.serialization.SerializationConstants.*;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import one.util.streamex.StreamEx;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.QueryType;

import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfileType;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfiles;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfilesRoot;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.metadata.model.Daemons;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.EntitiesFactory;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityItem;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.FreemarkerCatalog;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.FreemarkerScriptOrCatalog;
import com.developmentontheedge.be5.metadata.model.Icon;
import com.developmentontheedge.be5.metadata.model.IndexColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexDef;
import com.developmentontheedge.be5.metadata.model.JavaScriptForm;
import com.developmentontheedge.be5.metadata.model.JavaScriptForms;
import com.developmentontheedge.be5.metadata.model.JavaScriptOperationExtender;
import com.developmentontheedge.be5.metadata.model.LanguageLocalizations;
import com.developmentontheedge.be5.metadata.model.LanguageStaticPages;
import com.developmentontheedge.be5.metadata.model.Localizations;
import com.developmentontheedge.be5.metadata.model.ManagedFileType;
import com.developmentontheedge.be5.metadata.model.MassChange;
import com.developmentontheedge.be5.metadata.model.MassChanges;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationExtender;
import com.developmentontheedge.be5.metadata.model.PageCustomization;
import com.developmentontheedge.be5.metadata.model.PageCustomizations;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.ProjectFileStructure;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.developmentontheedge.be5.metadata.model.QuickFilter;
import com.developmentontheedge.be5.metadata.model.Role;
import com.developmentontheedge.be5.metadata.model.RoleGroup;
import com.developmentontheedge.be5.metadata.model.SecurityCollection;
import com.developmentontheedge.be5.metadata.model.SourceFile;
import com.developmentontheedge.be5.metadata.model.SourceFileCollection;
import com.developmentontheedge.be5.metadata.model.SourceFileOperation;
import com.developmentontheedge.be5.metadata.model.SpecialRoleGroup;
import com.developmentontheedge.be5.metadata.model.StaticPage;
import com.developmentontheedge.be5.metadata.model.StaticPages;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.TableRef;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.metadata.model.Templates;
import com.developmentontheedge.be5.metadata.model.ViewDef;
import com.developmentontheedge.be5.metadata.model.base.BeElementWithProperties;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.be5.metadata.model.base.BeVectorCollection;
import com.developmentontheedge.be5.metadata.model.base.DataElementPath;
import com.developmentontheedge.be5.metadata.serialization.Field;
import com.developmentontheedge.be5.metadata.serialization.Fields;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.ProjectFileSystem;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.serialization.SerializationConstants;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;
import com.developmentontheedge.be5.metadata.util.ObjectCache;
import com.developmentontheedge.be5.metadata.util.Strings2;
import com.developmentontheedge.beans.util.Beans;

public class YamlDeserializer
{
    
    private class BaseDeserializer
    {
        
        protected final Path path;

        public BaseDeserializer( final Path path )
        {
            Objects.requireNonNull( path );
            this.path = path;
        }
        
        public BaseDeserializer()
        {
            this.path = null;
        }
        
        protected void readFields( BeModelElement target, Map<String, Object> content, List<Field> fields )
        {
            Collection<String> customizableProperties = target.getCustomizableProperties();
            
            for ( final Field field : fields )
            {
                if ( field.name.equals( "name" ) || ( customizableProperties.contains( field.name ) && !content.containsKey( field.name ) ) )
                {
                    continue;
                }
                
                try
                {
                    Class<?> type = Beans.getBeanPropertyType( target, field.name );
                    final Object value = readField( content, field, type );
                    
                    if(value != null || !type.isPrimitive())
                        Beans.setBeanPropertyValue( target, field.name, value );
                    
                    target.customizeProperty( field.name );
                }
                catch ( final Exception e )
                {
                    loadContext.addWarning( new ReadException(e, target, path, "Error reading field "+field.name ) );
                }
            }
        }
        
        private Object readField( Map<String, Object> content, Field field, Class<?> klass )
        {
            Object fieldValue = content.get( field.name );
            if ( fieldValue == null )
            {
                return field.defaultValue;
            }
            if ( !(fieldValue instanceof Boolean) && !(fieldValue instanceof Number) && !(fieldValue instanceof String))
            {
                throw new IllegalArgumentException( "Invalid value: expected scalar" );
            }
            final String value = fieldValue.toString();
            
            try
            {
                return castValue( klass, value );
            }
            catch ( InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e )
            {
                throw new AssertionError();
            }
        }
        
        private /*static*/ Object castValue( final Class<?> klass, final String value ) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
        {
            if ( klass == Boolean.class || klass == boolean.class )
            {
                return Boolean.parseBoolean( value );
            }
            
            if ( klass == Integer.class || klass == int.class )
            {
                return Integer.parseInt( value );
            }
            
            if ( klass == Long.class || klass == long.class )
            {
                return Long.parseLong( value );
            }
            
            if( klass == String.class )
            {
                return value;
            }
            
            return klass.getConstructor( String.class ).newInstance( value );
        }
        
        protected void readDocumentation( final Map<String, Object> source, final BeModelElement target )
        {
            final String documenation = ( String ) source.get( SerializationConstants.TAG_COMMENT );
            target.setComment( documenation != null ? documenation : "" );
        }
        
        protected void readUsedExtras( final Map<String, Object> source, final BeModelElement target ) throws ReadException
        {
            final Object serializedExtras = source.get( SerializationConstants.TAG_EXTRAS );
            
            if ( serializedExtras != null )
            {
                final List<String> extras = asStrList( serializedExtras );
                if(extras.contains( "" ))
                    loadContext.addWarning( new ReadException( target, path, "Extras tag contains empty string: probably it's incorrectly specified in YAML" ) );
                target.setUsedInExtras( extras.toArray( new String[extras.size()] ) );
            }
        }
        
        protected void readProperties( final Map<String, Object> elementBody, final BeElementWithProperties target ) throws ReadException
        {
            final Object serializedProperties = elementBody.get( TAG_PROPERTIES );
            
            if ( serializedProperties == null )
            {
                return;
            }
            
            for ( final Object serializedProperty : asList( serializedProperties ) )
            {
                final Map<String, Object> serializedProperty0 = asMap( serializedProperty );
                
                if ( serializedProperty0.size() != 1 )
                {
                    loadContext.addWarning( new ReadException( path, "Property should contain a key-value pair" ) );
                    continue;
                }
                
                Entry<String, Object> entry = serializedProperty0.entrySet().iterator().next();
                final String name = entry.getKey();
                final String value = asStr( entry.getValue() );
                target.setProperty( name, value );
            }
        }
        
        protected void readIcon( final Map<String, Object> element, final Icon icon )
        {
            try
            {
                if ( element.containsKey( SerializationConstants.ATTR_ICON ) )
                {
                    icon.setMetaPath( ( String ) element.get( SerializationConstants.ATTR_ICON ) );
                    icon.load();
                    icon.setOriginModuleName( icon.getOwner().getProject().getProjectOrigin() );
                    ( ( BeModelCollection<?> ) icon.getOwner() ).customizeProperty( "icon" );
                }
            }
            catch ( final ReadException e )
            {
                loadContext.addWarning( e );
            }
        }
        
        @SuppressWarnings( "unchecked" )
        protected List<String> readList( final Map<String, Object> element, final String attributeName )
        {
            final Object value = element.get( attributeName );
            
            if ( value instanceof List )
                return Collections.unmodifiableList( ( List<String> ) value );
            
            final String strValue = ( String ) value;
            
            if ( strValue == null || strValue.trim().isEmpty() )
            {
                return Collections.emptyList();
            }
            
            final List<String> result = new ArrayList<>();
            
            for ( String item : strValue.trim().split( ";" ) )
            {
                item = item.trim();
                if ( !item.isEmpty() )
                {
                    result.add( item );
                }
            }
            
            return result;
        }
        
        protected List<String> asStrList( Object object ) throws ReadException
        {
            if ( object == null )
                return Collections.emptyList();
            if ( object instanceof String )
                return Collections.singletonList( ( String ) object );
            if ( object instanceof List )
            {
                final List<?> list = ( List<?> ) object;
                final List<String> strings = new ArrayList<>();
                for ( Object element : list )
                    if ( element instanceof String )
                        strings.add( ( String ) element );
                    else
                        throw new ReadException( path, "Invalid file format: string expected" );
                return strings;
            }
            
            throw new ReadException( path, "Invalid file format: list or string expected" );
        }
        
        @SuppressWarnings( "unchecked" )
        protected List<Object> asList( Object object ) throws ReadException
        {
            if ( object instanceof List )
                return ( List<Object> ) object;
            
            throw new ReadException( path, "Invalid file format: list expected" );
        }
        
        protected String asStr( Object object ) throws ReadException 
        {
            if ( object instanceof String )
                return ( String ) object;
            
            throw new ReadException( path, "Invalid file format: string expected" );
        }
        
        protected boolean nullableAsBool( Object object ) throws ReadException
        {
            if ( object == null )
                return false;
            
            if ( object instanceof Boolean )
                return ( boolean ) object;
            
            if ( object.toString().equals( "true" ) )
                return true;
            
            if ( object.toString().equals( "false" ) )
                return false;
            
            throw new ReadException( path, "Invalid file format: boolean expected" );
        }
        
        @SuppressWarnings( "unchecked" )
        protected List<Map<String, Object>> asMaps( Object object )
        {
            if ( object instanceof List )
            {
                final List<?> list = ( List<?> ) object;
                final List<Map<String, Object>> maps = new ArrayList<>();
                for ( Object element : list )
                    if ( element instanceof Map )
                        maps.add( ( Map<String, Object> ) element );
                return maps;
            }
            
            if ( object instanceof Map )
            {
                final List<Map<String, Object>> splitted = new ArrayList<>();
                final Map<String, Object> map = ( Map<String, Object> ) object;
                for ( Map.Entry<String, Object> entry : map.entrySet() )
                {
                    final String name = entry.getKey();
                    final Object value = entry.getValue();
                    final Map<String, Object> adaptedEntry = new LinkedHashMap<>();
                    adaptedEntry.put( name, value );
                    splitted.add( adaptedEntry );
                }
                return splitted;
            }
            
            return Collections.emptyList();
        }
        
        protected List<Map.Entry<String, Object>> asPairs( Object object ) throws ReadException
        {
            if ( object instanceof List )
            {
                final List<?> list = ( List<?> ) object;
                final List<Map.Entry<String, Object>> pairs = new ArrayList<>();
                for ( Object listPair : list )
                {
                    if ( !( listPair instanceof Map ) )
                        throw new ReadException( path, "Invalid file format: pair expected" );
                    
                    @SuppressWarnings( "unchecked" )
                    final Map<String, Object> map = ( Map<String, Object> ) listPair;
                    
                    if ( map.size() != 1 )
                        throw new ReadException( path, "Invalid file format: pair expected" );
                    
                    pairs.add( map.entrySet().iterator().next() );
                }
                return pairs;
            }
            
            throw new ReadException( path, "Invalid file format: list of pairs expected" );
        }
        
        // public -> protected
        @SuppressWarnings( "unchecked" )
        public Map<String, Object> asMap( Object object ) throws ReadException
        {
            if ( object instanceof Map )
                return ( Map<String, Object> ) object;
            throw new ReadException( path, "Invalid file format: map expected" );
        }
        
        protected Map<String, Object> asMapOrEmpty( Object object ) throws ReadException
        {
            if(object == null)
                return Collections.emptyMap();
            return asMap(object);
        }
        
        @SuppressWarnings( "unchecked" )
        protected Map<String, Object> getRootMap( Object object, String name ) throws ReadException
        {
            try
            {
                if(!(object instanceof Map))
                    throw new IllegalArgumentException("Invalid file format: map expected");
                Map<String, Object> topLevelMap = ( Map<String, Object> ) object;
                if(!topLevelMap.containsKey( name ))
                    throw new IllegalArgumentException("Invalid file format: top-level element '"+name+"' must be present");
                if(topLevelMap.size() > 1)
                    throw new IllegalArgumentException("Invalid file format: there must be only one top-level element '"+name+"'");
                Object rootObject = topLevelMap.get( name );
                if(!(rootObject instanceof Map))
                    throw new IllegalArgumentException("Invalid file format: top-level element '"+name+"' must be a map");
                return ( Map<String, Object> ) rootObject;
            }
            catch ( IllegalArgumentException e )
            {
                throw new ReadException( path, e.getMessage() );
            }
        }
        
        protected void checkChildren( BeModelElement context, Map<String, Object> element, Object... allowedFields )
        {
            Set<String> allowed = getAllowedFields( allowedFields );
            
            for ( String name : element.keySet() )
            {
                if ( !allowed.contains( name ) )
                {
                    String message = "Unknown child element found: " + name + "possible values: " + allowed;
                    loadContext.addWarning( new ReadException( context, path, message ) );
                }
            }
        }
        
        protected Set<String> getAllowedFields( Object... allowedFields )
        {
            Set<String> allowed = new HashSet<>();
            
            for ( Object allowedField : allowedFields )
            {
                if ( allowedField instanceof Field )
                {
                    allowed.add( ( ( Field ) allowedField ).name );
                }
                else if ( allowedField instanceof Collection )
                {
                    allowed.addAll( getAllowedFields( ( ( Collection<?> ) allowedField ).toArray() ) );
                }
                else
                    allowed.add( allowedField.toString() );
            }
            
            return allowed;
        }
        
        @SuppressWarnings( "unchecked" )
        void save(BeModelElement element)
        {
            if(element == null)
                return;
            @SuppressWarnings( "rawtypes" )
            BeModelCollection origin = element.getOrigin();
            if(origin == null)
                return;
            if(origin.contains( element.getName() ))
                loadContext.addWarning( new ReadException( element, path, "Duplicate element" ) );
            else
                origin.put( element );
        }
    }
    
    private abstract class FileDeserializer extends BaseDeserializer
    {
        protected final Node content;
        
        private FileDeserializer( final String content, final Path path ) throws ReadException
        {
            super( path );
            
            try
            {
                this.content = new Yaml().compose( new StringReader( content ) );
            }
            catch ( MarkedYAMLException e )
            {
                throw new ReadException(
                    new Exception( ( e.getProblemMark().getLine() + 1 ) + ":" + ( e.getProblemMark().getColumn() + 1 ) + ": "
                    + e.getMessage() ), path, ReadException.LEE_INVALID_STRUCTURE );
            }
            catch( YAMLException e )
            {
                throw new ReadException( new Exception( e.getMessage() ), path, ReadException.LEE_INVALID_STRUCTURE );
            }
        }
        
        public FileDeserializer( final Path path ) throws ReadException
        {
            this( ProjectFileSystem.read( path ), path );
        }
        
        public FileDeserializer()
        {
            content = null;
        }
        
        public void deserialize() throws ReadException
        {
            doDeserialize( Serialization.derepresent( content ) );
        }
        
        protected abstract void doDeserialize( Object serializedRoot ) throws ReadException;
        
        @SuppressWarnings( "unused" )
        protected Node getNodeByObject( Object object )
        {
            return null; // TODO implement me
        }
    }
    
    private class StaticPagesDeserializer extends FileDeserializer
    {
        private final BeModelCollection<LanguageStaticPages> target;

        public StaticPagesDeserializer( final Path path, final BeModelCollection<LanguageStaticPages> target ) throws ReadException
        {
            super( path );
            this.target = target;
        }

        @SuppressWarnings( "unchecked" )
        @Override
        protected void doDeserialize( Object serializedRoot )
        {
            if ( !( serializedRoot instanceof Map ) )
                return;
            
            final Map<String, Object> root = ( Map<String, Object> ) serializedRoot;
            final Map<String, Object> pagesByLanguage = ( Map<String, Object> ) root.get( "pages" );
            
            if ( pagesByLanguage == null )
                return;
            
            for ( final String language : pagesByLanguage.keySet() )
            {
                final LanguageStaticPages langPages = new LanguageStaticPages( language, target );
                final Map<String, Object> serializedPages = ( Map<String, Object> ) pagesByLanguage.get( language );
                
                for ( final String pageName : serializedPages.keySet() )
                {
                    final Object serializedContent = serializedPages.get( pageName );
                    final StaticPage page = new StaticPage( pageName, langPages );
                    final String content;
                    
                    if ( serializedContent instanceof String )
                    {
                        content = ( String ) serializedContent;
                    }
                    else if ( serializedContent instanceof Map )
                    {
                        // a. file: <fileName>
                        // b. code: file: <fileName>
                        //    customizations: <map>
                        // c. code: <content>
                        //    customizations: <map>
                        
                        final Map<String, Object> mapPageContent = ( Map<String, Object> ) serializedContent;
                        if ( mapPageContent.containsKey( "file" ) )
                        {
                            final String fileName = ( String ) mapPageContent.get( "file" );
                            page.setFileName( fileName );
                            content = readStaticPageFileContent( fileName );
                        }
                        else
                        {
                            final Object codeObj = mapPageContent.get( SerializationConstants.TAG_CODE );
                            
                            if ( codeObj instanceof String )
                                content = ( String ) codeObj;
                            else if ( codeObj instanceof Map )
                            {
                                final String fileName = ( ( Map<String, String> ) codeObj ).get( "file" );
                                page.setFileName( fileName );
                                content = readStaticPageFileContent( fileName );
                            }
                            else
                                content = "";
                            
                            readCustomizations( mapPageContent, page, false );
                        }
                    }
                    else
                    {
                        content = "";
                    }
                    
                    page.setContent( content );
                    DataElementUtils.saveQuiet( page );
                }
                
                
                DataElementUtils.saveQuiet( langPages );
            }
            
            target.getProject().getAutomaticDeserializationService().registerFile( path, ManagedFileType.PAGES );
        }
        
        private String readStaticPageFileContent( final String fileName )
        {
            try
            {
                return getFileSystem().readStaticPageFileContent( fileName );
            }
            catch ( final ReadException e )
            {
                loadContext.addWarning( e );
            }
            
            return "";
        }
        
    }
    
    private class ConnectionProfilesDeserializer extends FileDeserializer
    {
        private final BeConnectionProfiles target;
        
        /**
         * Creates a deserializer that is not bound with any file. Used to deserialize connection profiles from memory.
         */
        public ConnectionProfilesDeserializer( final BeConnectionProfiles target )
        {
            super();
            this.target = target;
        }

        /**
         * A normal way to create the deserializer.
         */
        public ConnectionProfilesDeserializer( final Path path, final BeConnectionProfileType type, final BeConnectionProfilesRoot target ) throws ReadException
        {
            super( path );
            this.target = new BeConnectionProfiles( type, target );
        }

        @Override
        protected void doDeserialize( Object serializedRoot ) throws ReadException
        {
            final Map<String, Object> serializedConnectionProfilesBody = asMap( asMap( serializedRoot ).get( TAG_CONNECTION_PROFILES ) );
            final Map<String, Object> profilesMap = asMap( serializedConnectionProfilesBody.get( TAG_CONNECTION_PROFILES_INNER ) );
            
            for ( Map.Entry<String, Object> serializedProfile : profilesMap.entrySet() )
            {
                try
                {
                    final String profileName = serializedProfile.getKey();
                    final Map<String, Object> serializedProfileBody = asMap( serializedProfile.getValue() );
                    final BeConnectionProfile connectionProfile = deserializeConnectionProfile( profileName, serializedProfileBody );
                    
                    save( connectionProfile );
                }
                catch ( ReadException e )
                {
                    loadContext.addWarning( e.attachElement( target ) );
                }
            }
            
            // default connection profile can be deserialized after
            // deserialization of connection profiles
            Object defProfileObj = serializedConnectionProfilesBody.get( "defaultProfileName" );
            if(defProfileObj instanceof String)
            {
                target.getProject().setConnectionProfileName( (String)defProfileObj );
            }
            DataElementUtils.saveQuiet( target );
            
            target.getProject().getAutomaticDeserializationService().registerFile( path, ManagedFileType.CONNECTION_PROFILES );
        }
        
        public BeConnectionProfile deserializeConnectionProfile( final String profileName, final Map<String, Object> serializedProfileBody ) throws ReadException
        {
            final BeConnectionProfile connectionProfile = new BeConnectionProfile( profileName, target );
            readFields( connectionProfile, serializedProfileBody, Fields.connectionProfile() );
            readFields( connectionProfile, serializedProfileBody, Fields.connectionProfileRead() );
            readProperties( serializedProfileBody, connectionProfile );
            
            List<String> propertiesToRequest = readList( serializedProfileBody, TAG_REQUESTED_PROPERTIES );
            
            if ( propertiesToRequest != null )
            {
                connectionProfile.setPropertiesToRequest( propertiesToRequest.toArray( new String[propertiesToRequest.size()] ) );
            }
            
            if ( Strings2.isNullOrEmpty( connectionProfile.getProviderId() ) )
                connectionProfile.setProviderId( connectionProfile.getDefaultProviderId() );
            
            if ( Strings2.isNullOrEmpty( connectionProfile.getDriverDefinition() ) )
                connectionProfile.setDriverDefinition( connectionProfile.getDefaultDriverDefinition() );
            
            return connectionProfile;
        }
        
        public BeConnectionProfiles getResult()
        {
            return target;
        }
    }
    
    private class CustomizationDeserializer extends FileDeserializer
    {

        private final Module target;
        private boolean replace = false;

        public CustomizationDeserializer( final Path path, final Module target ) throws ReadException
        {
            super( path );
            this.target = target;
        }
        
        CustomizationDeserializer replace()
        {
            replace = true;
            return this;
        }

        @Override
        protected void doDeserialize( Object serializedRoot ) throws ReadException
        {
            readCustomizations( asMap( serializedRoot ), target, replace );
            target.getProject().getAutomaticDeserializationService().registerFile( path, ManagedFileType.CUSTOMIZATION );
        }
        
        private PageCustomizations getResult()
        {
            return target.getPageCustomizations();
        }
        
    }
    
    private class DaemonsDeserializer extends FileDeserializer
    {

        private final BeModelCollection<Daemon> target;

        public DaemonsDeserializer( final Path path, final BeModelCollection<Daemon> target ) throws ReadException
        {
            super( path );
            this.target = target;
        }
        
        @SuppressWarnings( "unchecked" )
        @Override
        protected void doDeserialize( Object serializedRoot ) throws ReadException
        {
            final Map<String, Object> daemonsByName = getRootMap( serializedRoot, TAG_DAEMONS );
            
            for ( final String daemonName : daemonsByName.keySet() )
            {
                final Object daemonContent = daemonsByName.get( daemonName );
                if ( !( daemonContent instanceof Map ) )
                    continue;
                final Daemon daemon = new Daemon( daemonName, target );
                final Map<String, Object> daemonContentMap = ( Map<String, Object> ) daemonContent;
                readFields( daemon, daemonContentMap, Fields.daemon() );
                readUsedExtras( daemonContentMap, daemon );
                readDocumentation( daemonContentMap, daemon );
                checkChildren( daemon, daemonContentMap, Fields.daemon(), TAG_COMMENT, TAG_EXTRAS );
                DataElementUtils.saveQuiet( daemon );
            }
            
            target.getProject().getAutomaticDeserializationService().registerFile( path, ManagedFileType.DAEMONS );
        }
        
    }
    
    private class EntityDeserializer extends FileDeserializer
    {

        private final String name;
        private final Module module;
        private Entity result;
        
        public EntityDeserializer()
        {
            name = null;
            module = null;
        }

        public EntityDeserializer( Module module, String name ) throws ReadException
        {
            super( getFileSystem().getEntityFile( module.getName(), name ) );
            this.name = name;
            this.module = module;
        }

        /**
         * Can throw a {@link RuntimeException} with {@link ReadException} as its cause.
         * @throws ReadException 
         */
        @SuppressWarnings( "unchecked" )
        @Override
        protected void doDeserialize( Object serializedRoot ) throws ReadException
        {
            if ( !( serializedRoot instanceof Map ) )
            {
                throw new ReadException( path, "Expected YAML map on the top level" );
            }
            
            final Map<String, Object> serialized = ( Map<String, Object> ) serializedRoot;
            
            if ( !( serialized.containsKey( name ) ) )
            {
                throw new ReadException( path, "YAML map should start with entity name '"+name+"', found instead: "+serialized.keySet() );
            }
            
            final Map<String, Object> entityContent = ( Map<String, Object> ) serialized.get( name );
            this.result = readEntity( name, entityContent, module );
        }

        private Entity readEntity( final String name, final Map<String, Object> entityContent, final Module module ) throws ReadException
        {
            Entity entity = new Entity( name, module, null );
            final boolean isFromApp = module == module.getProject().getApplication();
            
            String template = (String)entityContent.get( ATTR_ENTITY_TEMPLATE );
            Entity templateEntity = null;
            if(template != null)
            {
                templateEntity = getTemplates().getEntity( template );
                if ( templateEntity == null )
                {
                    loadContext.addWarning( new ReadException( entity, path, "Unknown template name specified: " + template ) );
                }
                else if ( !isFromApp )
                {
                    loadContext.addWarning( new ReadException( entity, path, "Cannot use template with non-application entity" ) );
                    templateEntity = null;
                }
            }
            
            final String type = ( String ) entityContent.get( ATTR_ENTITY_TYPE );
            if(type == null)
            {
                if ( isFromApp && templateEntity == null )
                {
                    loadContext.addWarning( new ReadException( entity, path, "Entity has no type" ) );
                }
            } else
            {
                EntityType entityType = EntityType.forSqlName( type );
                if(entityType == null)
                {
                    loadContext.addWarning( new ReadException( entity, path, "Entity type is invalid: "+type ) );
                } else
                {
                    entity.setType( entityType );
                }
            }
            
            if ( fileSystem != null )
            {
                entity.setLinkedFile( getFileSystem().getEntityFile( module.getName(), entity.getName() ) );
            }
            
            readDocumentation( entityContent, entity );
            readUsedExtras( entityContent, entity );
            
            readFields( entity, entityContent, Fields.entity() );
            readCustomizations( entityContent, entity, false );
            readIcon( entityContent, entity.getIcon() );
            
            new SchemeDeserializer( ).deserialize( entityContent, entity );
            
            final List<Map<String, Object>> operationsList = asMaps( entityContent.get( "operations" ) );
            
            for ( Map<String, Object> operationElement : operationsList )
            {
                for ( Map.Entry<String, Object> operationPair : operationElement.entrySet() ) // should have only one element
                {
                    try
                    {
                        save( readOperation( operationPair.getKey(), asMap(operationPair.getValue()), entity ) );
                    }
                    catch ( ReadException e )
                    {
                        Operation operation = Operation.createOperation( operationPair.getKey(), Operation.OPERATION_TYPE_JAVA, entity );
                        save( operation );
                        loadContext.addWarning( e.attachElement( operation ) );
                    }
                }
            }
            
            final List<Map<String, Object>> queriesList = asMaps( entityContent.get( "queries" ) );
            
            for ( Map<String, Object> queryElement : queriesList )
            {
                for ( Map.Entry<String, Object> queryPair : queryElement.entrySet() ) // should have only one element
                {
                    try
                    {
                        if ( queryPair.getKey().equals( Query.SPECIAL_TABLE_DEFINITION )
                            || queryPair.getKey().equals( Query.SPECIAL_LOST_RECORDS ) )
                        {
                            loadContext.addWarning( new ReadException(
                                    entity.getQueries().getCompletePath().getChildPath( queryPair.getKey() ), path, "Illegal query name: '"
                                        + queryPair.getKey()
                                        + "'. Such query is managed by BE automatically and should not appear in metadata." ) );
                            continue;
                        }
                        save( readQuery( queryPair.getKey(), asMap(queryPair.getValue()), entity ) );
                    }
                    catch ( ReadException e )
                    {
                        Query query = new Query( queryPair.getKey(), entity );
                        save(query);
                        loadContext.addWarning( e.attachElement( query ) );
                    }
                }
            }
            
            checkChildren( entity, entityContent, Fields.entity(), "type", TAG_COMMENT, TAG_EXTRAS, TAG_CUSTOMIZATIONS, ATTR_ICON,
                    "operations", "queries", "scheme", TAG_REFERENCES, ATTR_ENTITY_TEMPLATE );
            
            if(templateEntity != null)
            {
                entity.merge( templateEntity, false, !fuseTemplate );
            }
            
            return entity;
        }
        
        public Entity getEntity()
        {
            return result;
        }
        
        private Operation readOperation( final String name, final Map<String, Object> operationElement, final Entity entity ) throws ReadException
        {
            final Operation operation = Operation.createOperation( name, ( String ) operationElement.get( ATTR_OPERATION_TYPE ), entity );
            readDocumentation( operationElement, operation );
            
            readFields( operation, operationElement, Fields.operation() );
            readUsedExtras( operationElement, operation );
            readCustomizations( operationElement, operation, false );
            readIcon( operationElement, operation.getIcon() );
            
            operation.setOriginModuleName( getProjectOrigin() );
            
            if ( operation instanceof SourceFileOperation )
            {
                if ( operationElement.containsKey( ATTR_FILEPATH ) )
                {
                    final SourceFileOperation fileOperation = (SourceFileOperation)operation;
                    final String filepath = ( String ) operationElement.get( ATTR_FILEPATH );
                    final String nameSpace = fileOperation.getFileNameSpace();
                    SourceFile sourceFile = project.getApplication().getSourceFile( nameSpace, filepath );
                    if(sourceFile == null)
                    {
                        sourceFile = project.getApplication().addSourceFile( nameSpace, filepath );
                        sourceFile.setLinkedFile( getFileSystem().getNameSpaceFile( nameSpace, filepath ) );
                    }
                    fileOperation.setFileName( sourceFile.getName() );
                    fileOperation.customizeProperty( "code" );
                }
            }
            else
            {
                String text = ( String ) operationElement.get( TAG_CODE );
                if ( !Strings2.isNullOrEmpty( text ) )
                {
                    operation.setCode( text );
                    operation.customizeProperty( "code" );
                }
            }
            
            readRoles( operationElement, operation );
            readExtenders( operationElement, operation );
            checkChildren( operation, operationElement, Fields.operation(), TAG_CODE, ATTR_ICON, TAG_EXTRAS, TAG_CUSTOMIZATIONS, TAG_COMMENT, ATTR_FILEPATH, ATTR_ROLES, "extenders", "type" );
            return operation;
        }
        
        private void readExtenders( final Map<String, Object> operationElement, final Operation operation )
        {
            final List<Map<String, Object>> extendersElement = asMaps( operationElement.get( "extenders" ) );
            
            for ( Map<String, Object> extenderElement : extendersElement )
            {
                final OperationExtender extender;
                
                if ( extenderElement.containsKey( ATTR_CLASS_NAME ) )
                {
                    extender = new OperationExtender( operation, getProjectOrigin() );
                    extender.setClassName( ( String ) extenderElement.get( ATTR_CLASS_NAME ) );
                }
                else
                {
                    SourceFile sourceFile;
                    try
                    {
                        final String filepath = ( String ) extenderElement.get( ATTR_FILEPATH );
                        if ( filepath == null ) // error
                        {
                            throw new ReadException( path, "Extender: no "+ATTR_FILEPATH+" attribute found" ); 
                        }
                        sourceFile = project.getApplication().getSourceFile( SourceFileCollection.NAMESPACE_JAVASCRIPT_EXTENDER, filepath );
                        if(sourceFile == null)
                        {
                            sourceFile = project.getApplication().addSourceFile( SourceFileCollection.NAMESPACE_JAVASCRIPT_EXTENDER, filepath );
                            sourceFile.setLinkedFile( getFileSystem().getJavaScriptExtenderFile( filepath ) );
                        }
                    }
                    catch ( ReadException e )
                    {
                        loadContext.addWarning( e.attachElement( operation ) );
                        continue;
                    }
                    final JavaScriptOperationExtender jsExtender = new JavaScriptOperationExtender( operation, getProjectOrigin() );
                    extender = jsExtender;
                    jsExtender.setFileName( sourceFile.getName() );
                }
                readFields( extender, extenderElement, Fields.extender() );
                DataElementUtils.saveQuiet( extender );
                checkChildren( extender, extenderElement, Fields.extender(), ATTR_FILEPATH, ATTR_CLASS_NAME );
            }
        }

        private Query readQuery( final String name, final Map<String, Object> queryElement, final Entity entity ) throws ReadException
        {
            final Query query = new Query( name, entity );
            readDocumentation( queryElement, query );
            
            readFields( query, queryElement, Fields.query() );
            readUsedExtras( queryElement, query );
            readCustomizations( queryElement, query, false );
            readIcon( queryElement, query.getIcon() );
            
            query.setOriginModuleName( getProjectOrigin() );

            String text;
            
            switch ( query.getType() )
            {
            case STATIC:
                text = ( String ) queryElement.get( ATTR_QUERY_CODE );
                break;
                
            case GROOVY:
                final String groovyFileName = ( String ) queryElement.get( "file" );
                // try to read 'code' if there's no 'file'
                if ( groovyFileName == null )
                {
                    text = ( String ) queryElement.get( TAG_CODE );
                    query.setFileName( query.getName().replace( ':', '_' ) + ".groovy" );
                }
                else
                {
                    text = getFileSystem().readGroovyQuery( groovyFileName.replace( ':', '_' ) );
                    query.setFileName( groovyFileName );
                }
                break;
                
            case JAVASCRIPT:
                final String jsFileName = ( String ) queryElement.get( "file" );
                // try to read 'code' if there's no 'file'
                if ( jsFileName == null )
                {
                    text = ( String ) queryElement.get( TAG_CODE );
                    query.setFileName( query.getName().replace( ':', '_' ) + ".js" );
                }
                else
                {
                    text = getFileSystem().readJavaScriptQuery( jsFileName.replace( ':', '_' ) );
                    query.setFileName( jsFileName );
                }
                break;
                
            default:
                text = ( String ) queryElement.get( TAG_CODE );
                break;
            }

            // setQuerySettings must be called before setQuery
            // as setQuerySettings causes Freemarker initialization if query is not empty
            // and Freemarker may initialize incorrectly when project is not completely loaded
            if(queryElement.containsKey( "settings" ))
            {
                query.setQuerySettings( readQuerySettings(queryElement, query) );
            }
            
            if ( text != null )
            {
                query.setQuery( text );
                query.customizeProperty( "query" );
            }
            
            readQuickFilters(queryElement, query);
            readRoles( queryElement, query );
            if ( queryElement.containsKey( ATTR_QUERY_OPERATIONS ) )
            {
                query.getOperationNames().parseValues( stringCache( readList( queryElement, ATTR_QUERY_OPERATIONS ) ) );
                query.customizeProperty( "operationNames" );
            }
            checkChildren( query, queryElement, Fields.query(), TAG_EXTRAS, TAG_COMMENT, TAG_CUSTOMIZATIONS, ATTR_ICON, ATTR_QUERY_CODE, TAG_CODE,
                    ATTR_QUERY_OPERATIONS, "quickFilters", ATTR_ROLES, TAG_SETTINGS, "file" );
            return query;
        }

        private void readRoles( final Map<String, Object> element, final EntityItem item )
        {
            if ( element.containsKey( ATTR_ROLES ) )
            {
                item.getRoles().parseRoles( stringCache( readList( element, ATTR_ROLES ) ) );
                item.customizeProperty( "roles" );
            }
        }
        
        private QuerySettings[] readQuerySettings( final Map<String, Object> queryElement, final Query query )
        {
            final Set<String> allRoles = Collections.singleton( '@' + SpecialRoleGroup.ALL_ROLES_GROUP );
            final List<QuerySettings> result = new ArrayList<>();
            final List<Map<String, Object>> settingsList = asMaps( queryElement.get( "settings" ) );
            
            try
            {
                for ( Map<String, Object> settingsElement : settingsList )
                {
                    for ( Entry<String, Object> settingsPair : settingsElement.entrySet() ) // should be only one pair
                    {
                        if ( !( settingsPair.getKey().equals( "settings" ) ) ) // incorrect
                            continue;
                        
                        final Map<String, Object> settingsContent = asMap( settingsPair.getValue() );
                        final QuerySettings settings = new QuerySettings( query );
                        readFields( settings, settingsContent, Fields.querySettings() );
                        final List<String> roles = stringCache ( readList( settingsContent, ATTR_ROLES ) );
                        
                        if ( roles.isEmpty() )
                        {
                            settings.getRoles().parseRoles( allRoles );
                        }
                        else
                        {
                            settings.getRoles().parseRoles( roles );
                        }
                        
                        result.add( settings );
                    }
                }
            }
            catch ( ReadException e )
            {
                loadContext.addWarning( e.attachElement( query ) );
            }
            
            return result.toArray( new QuerySettings[result.size()] );
        }

        private void readQuickFilters( final Map<String, Object> queryElement, final Query query )
        {
            Map<String, Object> filterElements;
            try
            {
                filterElements = asMapOrEmpty( queryElement.get( "quickFilters" ) );
            }
            catch ( ReadException e )
            {
                loadContext.addWarning( e.attachElement( query ) );
                return;
            }

            for ( final Map.Entry<String, Object> filterElement : filterElements.entrySet() )
            {
                final QuickFilter filter = new QuickFilter( filterElement.getKey(), query );
                try
                {
                    readFields( filter, asMap( filterElement.getValue() ), Fields.quickFilter() );
                }
                catch ( ReadException e )
                {
                    loadContext.addWarning( e.attachElement( filter ) );
                }
                filter.setOriginModuleName( getProjectOrigin() );
                DataElementUtils.saveQuiet( filter );
            }
        }
        
        private String getProjectOrigin()
        {
            return (project == null ? module.getProject() : project).getProjectOrigin();
        }
        
    }
    
    private class FormsDeserializer extends FileDeserializer
    {
        
        private final BeModelCollection<JavaScriptForm> target;

        public FormsDeserializer( Path path, BeModelCollection<JavaScriptForm> target ) throws ReadException
        {
            super( path );
            this.target = target;
        }

        @Override
        protected void doDeserialize( Object serializedRoot ) throws ReadException
        {
            final Map<String, Object> serializedForms = asMap( asMap( serializedRoot ).get( TAG_JS_FORMS ) );
            
            readForms( serializedForms );
            target.getProject().getAutomaticDeserializationService().registerFile( path, ManagedFileType.FORMS );
        }

        private void readForms( final Map<String, Object> serializedForms )
        {
            for ( Map.Entry<String, Object> serializedForm : serializedForms.entrySet() )
            {
                try
                {
                    readForm( serializedForm );
                }
                catch ( ReadException e )
                {
                    loadContext.addWarning( e.attachElement( target ) );
                }
            }
        }
        
        private void readForm( Map.Entry<String, Object> serializedForm ) throws ReadException
        {
            final String name = serializedForm.getKey();
            final Map<String, Object> serializedFormBody = asMap( serializedForm.getValue() );
            final JavaScriptForm form = new JavaScriptForm( name, target );
            readFields( form, serializedFormBody, Fields.jsForms() );
            DataElementUtils.saveQuiet( form );
            final Path file = form.getLinkedFile();
            
            if ( file == null )
            {
                loadContext.addWarning( new ReadException( form, path, "File cannot be resolved for module " + form.getModuleName() ) );
            }
            else
            {
                try
                {
                    form.load();
                }
                catch ( ReadException e )
                {
                    loadContext.addWarning( e.attachElement( form ) );
                }
            }
        }
        
    }
    
    private class LocalizationDeserializer extends FileDeserializer
    {
        private final String lang;
        private final LanguageLocalizations target;

        public LocalizationDeserializer( final String lang, final Path path, final Localizations target ) throws ReadException
        {
            super( path );
            this.lang = lang;
            this.target = new LanguageLocalizations( lang, target );
        }

        @Override
        protected void doDeserialize( final Object serializedRoot ) throws ReadException
        {
            final Map<String, Object> localizationContent = asMap( asMap( serializedRoot ).get( lang ) );
            final List<Object> serializedEntitiesLocalization = asList( localizationContent.get( TAG_ENTITIES ) );
            
            for ( Object serializedEntityLocalization : serializedEntitiesLocalization )
            {
                readEntityLocalization( asMap( serializedEntityLocalization ) );
            }
            
            save( target );
            readDocumentation( localizationContent, target );
            target.getProject().getAutomaticDeserializationService().registerFile( path, ManagedFileType.LOCALIZATION );
        }
        
        private LanguageLocalizations getResult()
        {
            return target;
        }

        private void readEntityLocalization( Map<String, Object> serializedEntityLocalization ) throws ReadException
        {
            if ( serializedEntityLocalization.size() != 1 )
            {
                loadContext.addWarning( new ReadException( path, "Each entity localization should have only one key that reperesents an entity name" ) );
                return;
            }
            
            final String entityName = serializedEntityLocalization.keySet().iterator().next();
            final List<Object> serializedBlocks = asList( serializedEntityLocalization.get( entityName ) );
            
            readBlocks( entityName, serializedBlocks );
        }

        private void readBlocks( final String entityName, final List<Object> serializedBlocks )
        {
            for ( final Object serializedBlock : serializedBlocks )
            {
                try
                {
                    readBlock( entityName, asMap( serializedBlock ) );
                }
                catch ( ReadException e )
                {
                    loadContext.addWarning( e.attachElement( target ) );
                }
            }
        }

        private void readBlock( final String entityName, final Map<String, Object> serializedBlock ) throws ReadException
        {
            final List<String> topics = asStrList( serializedBlock.get( ATTR_LOCALIZATION_TOPICS ) );
            final List<Object> serializedEntries = asList( serializedBlock.get( TAG_LOCALIZATION_ENTRIES ) );
            
            for ( Object serializedEntry : serializedEntries )
            {
                readEntry( entityName, topics, asMap( serializedEntry ) );
            }
        }

        private void readEntry( String entityName, List<String> topics, Map<String, Object> serializedEntry ) throws ReadException
        {
            if ( serializedEntry.size() != 1 )
            {
                loadContext.addWarning( new ReadException( path, "Each localization entry should have only one key" ) );
                return;
            }
            
            final String name = serializedEntry.keySet().iterator().next();
            final String value = asStr( serializedEntry.get( name ) );
            
            if ( !target.addLocalization( entityName, topics, name, value ) )
            {
                loadContext.addWarning( new ReadException( target.get( entityName ), path, "Duplicate localization: topics = " + topics + "; name = " + name ) );
            }
        }
        
    }
    
    private class MacrosDeserializer extends FileDeserializer
    {
        
        private final Module module;

        public MacrosDeserializer( final Module module ) throws ReadException
        {
            super( ProjectFileSystem.getProjectFile( ModuleUtils.getModulePath( module.getName() ) ) );
            this.module = module;
        }

        @Override
        protected void doDeserialize( Object serializedRoot ) throws ReadException
        {
            final Path root = ModuleUtils.getModulePath( module.getName() );
            final Map<String, Object> serializedModule = asMap( serializedRoot );
            final Map<String, Object> serializedModuleBody = asMap( serializedModule.values().iterator().next() );
            final String projectName = root.getFileName().toString(); // not sure if this has any sense
            final boolean isModule = true;
            final Project project = new Project( projectName, isModule ); /* dummy project, required to create a file structure */
            project.setLocation( root );
            
            final Object serializedPfs = serializedModuleBody.get( TAG_PROJECT_FILE_STRUCTURE );
            
            if ( serializedPfs == null )
            {
                project.setProjectFileStructure( new ProjectFileStructure( project ) );
            }
            else
            {
                project.setProjectFileStructure( readProjectFileStructure( this, asMap( serializedPfs ), project ) );
            }
            
            YamlDeserializer.this.fileSystem = new ProjectFileSystem( project );
            
            if ( module.getMacroCollection() == null )
            {
                DataElementUtils.saveQuiet( new FreemarkerCatalog( Module.MACROS, module ) );
            }
            
            readMacroFiles( this, serializedModuleBody, module.getMacroCollection() );
        }
        
    }
    
    private class MassChangesDeserializer extends FileDeserializer
    {
        private final MassChanges target;

        public MassChangesDeserializer( final Path path, final MassChanges target ) throws ReadException
        {
            super(path);
            this.target = target;
        }

        @Override
        protected void doDeserialize( Object serializedRoot ) throws ReadException
        {
            Object changes = asMap( serializedRoot ).get( TAG_MASS_CHANGES );
            if(!(changes instanceof List))
                throw new ReadException(path, "Top-element must be a list");
            @SuppressWarnings( "unchecked" )
            List<Object> changesList = (List<Object>)changes;
            for(Object massChangeObject: changesList)
            {
                Map<String, Object> massChangeElement = asMap(massChangeObject);
                Object selectObject = massChangeElement.get( "select");
                if(selectObject == null)
                {
                    throw new ReadException( path, "'select' string must be present in each massChange" );
                }
                if(!(selectObject instanceof String))
                {
                    throw new ReadException( path, "'select' value must be a string" );
                }
                String selectString = (String)selectObject;
                MassChange massChange = new MassChange( selectString, target, massChangeElement );
                readDocumentation( massChangeElement, massChange );
                save( massChange );
            }
            
            target.getProject().getAutomaticDeserializationService().registerFile( path, ManagedFileType.MASS_CHANGES );
        }
        
        private MassChanges getResult()
        {
            return target;
        }
    }
    
    public class ProjectDeserializer extends FileDeserializer
    {

        public ProjectDeserializer( final Path path ) throws ReadException
        {
            super( path );
        }
        
        public void readMacroFiles( final Map<String, Object> serializedModuleBody, final FreemarkerCatalog macroFiles ) throws ReadException
        {
            YamlDeserializer.this.readMacroFiles( this, serializedModuleBody, macroFiles );
        }
        
        public ProjectFileStructure readProjectFileStructure( final Map<String, Object> serializedPfs, final Project project )
        {
            return YamlDeserializer.this.readProjectFileStructure( this, serializedPfs, project );
        }

        @Override
        protected void doDeserialize( Object serializedRoot ) throws ReadException
        {
            final Map<String, Object> serializedProject = asMap( serializedRoot );
            final String projectName = serializedProject.keySet().iterator().next(); // ignore root.getFileName().toString();
            final Map<String, Object> serializedProjectBody = asMap( serializedProject.get( projectName ) );
            final Project project = new Project( projectName, nullableAsBool( serializedProjectBody.get( ATTR_MODULE_PROJECT ) ) );
            project.setLocation( path.getParent() );
            
            // Read the file structure
            final Object serializedPfs = serializedProjectBody.get( TAG_PROJECT_FILE_STRUCTURE );
            
            if ( serializedPfs != null )
            {
                project.setProjectFileStructure( readProjectFileStructure( asMap( serializedPfs ), project ) );
            }
            
            // project file system is required to set the project here
            YamlDeserializer.this.setProject( project );

            readDocumentation( serializedProjectBody, project );
            readFields( project, serializedProjectBody, Fields.project() );
            
            final Object serializedBugtrackers = serializedProjectBody.get( TAG_BUGTRACKERS );
            
            if ( serializedBugtrackers != null )
                for ( Map.Entry<String, Object> pair : asMap( serializedBugtrackers ).entrySet() )
                    project.addConnectedBugtracker( pair.getKey(), pair.getValue().toString() );

            // Create the file system
            YamlDeserializer.this.fileSystem = new ProjectFileSystem( project );

            // Read rest content

            readSecurity( project.getSecurityCollection() );
            project.setFeatures( asStrList( serializedProjectBody.get( ATTR_FEATURES ) ) );

            final Object serializedApplication = serializedProjectBody.get( TAG_APPLICATION );
            final Module application = readApplication( serializedApplication, project );
            
            readDaemons( application.getDaemonCollection() );
            
            if ( Files.exists( getFileSystem().getMassChangesFile() ) )
            {
                YamlDeserializer.this.readMassChanges( application.getMassChangeCollection() );
            }
            
            final List<Module> modules = readModuleReferences( serializedProjectBody, project );

            for ( final Module module : modules )
            {
                DataElementUtils.saveQuiet( module );
            }

            readProperties( serializedProjectBody, project );
            readLocalizations( serializedProjectBody, project );
            readScripts( serializedProjectBody, application.getFreemarkerScripts() );
            readMacroFiles( serializedProjectBody, application.getMacroCollection() );
            readConnectionProfiles( project.getConnectionProfiles() );
            
            Path selectedProfileFile = getFileSystem().getSelectedProfileFile();
            if(Files.exists( selectedProfileFile ))
            {
                project.setConnectionProfileName( ProjectFileSystem.read( selectedProfileFile ).trim() );
            }
            
            if ( !project.isModuleProject() )
            {
                readForms( application.getCollection( Module.JS_FORMS, JavaScriptForm.class ) );
                readStaticPages( project.getApplication().getStaticPageCollection() );
            }

            readCustomization( project.getApplication() );
            project.getAutomaticDeserializationService().registerFile( path, ManagedFileType.PROJECT );
        }
        
        private void readLocalizations( final Map<String, Object> serializedProjectBody, final Project project ) throws ReadException
        {
            final Object serializedL10ns = serializedProjectBody.get( ATTR_LOCALIZATIONS );
            
            if ( serializedL10ns != null )
            {
                YamlDeserializer.this.readLocalizations( asStrList( serializedL10ns ), project.getApplication().getLocalizations() );
            }
        }

        private void readConnectionProfiles( final BeConnectionProfilesRoot target )
        {
            YamlDeserializer.this.readConnectionProfiles( target );
        }

        private void readSecurity( final SecurityCollection securityCollection )
        {
            YamlDeserializer.this.readSecurity( securityCollection );
        }

        private List<Module> readModuleReferences( final Map<String, Object> projectElement, final Project project ) throws ReadException
        {
            final List<Module> modules = new ArrayList<>();
            final Object serializedModules = projectElement.get( TAG_MODULES );
            
            if ( serializedModules == null )
                return modules;
            
            final DataElementPath modulesPath = project.getModules().getCompletePath();

            for ( final Map.Entry<String, Object> serializedModule : asPairs( serializedModules ) )
            {
                final String name = serializedModule.getKey();
                
                try
                {
                    modules.add( readModuleReference( name, asMap( serializedModule.getValue() ), project ) );
                }
                catch ( Exception e )
                {
                    loadContext.addWarning( new ReadException( e, name == null ? modulesPath : modulesPath.getChildPath( name ), path ) );
                }
            }

            return modules;
        }

        private Module readModuleReference( final String moduleName, final Map<String, Object> serializedModuleBody, final Project project ) throws ReadException
        {
            final Module module = new Module( moduleName, project.getModules() );
            final Object entities = serializedModuleBody.get( TAG_ENTITIES );
            
            if ( entities != null )
            {
                for ( final String entityName : asStrList( entities ) )
                {
                    readEntity( module, entityName );
                }
            }

            final Object serializedExtras = serializedModuleBody.get( TAG_EXTRAS );
            
            if ( serializedExtras != null )
            {
                final List<String> extras = asStrList( serializedExtras );
                module.setExtras( extras.toArray( new String[extras.size()] ) );
            }

            return module;
        }

        private Module readApplication( final Object entities, final Project project ) throws ReadException
        {
            final Module application = new Module( project.getProjectOrigin(), project );
            project.setApplication( application );

            if ( entities != null )
            {
                for ( final String entityName : asStrList( entities ) )
                {
                    readEntity( application, entityName );
                }
            }
            
            return application;
        }

        private void readEntity( final Module module, final String entityName )
        {
            try
            {
                final Path file = getFileSystem().getEntityFile( module.getName(), entityName );
                final Entity entity = YamlDeserializer.this.readEntity( module, entityName );
                
                EntitiesFactory.addToModule( entity, module );
                entity.getProject().getAutomaticDeserializationService().registerFile( file, ManagedFileType.ENTITY );
            }
            catch ( ReadException e )
            {
                loadContext.addWarning( e.attachElement( module ) );
            }
        }
        
        private void readScripts( final Map<String, Object> serializedProjectBody, final FreemarkerCatalog scripts ) throws ReadException
        {
            final Object serializedScripts = serializedProjectBody.get( TAG_SCRIPTS );
            
            if ( serializedScripts == null )
                return;
            
            for ( final String scriptName : asStrList( serializedScripts ) )
            {
                final Path scriptsFile = getFileSystem().getScriptFile( scriptName );
                try
                {
                    FreemarkerCatalog parent = scripts;
                    String[] pathComponents = DataElementPath.create(scriptName).getPathComponents();
                    for(int i=0; i<pathComponents.length-1; i++)
                    {
                        FreemarkerScriptOrCatalog newParent = parent.get( pathComponents[i] );
                        if(newParent instanceof FreemarkerScript)
                        {
                            loadContext.addWarning( new ReadException( new Exception("Cannot create catalog for script "+scriptName+": script with the same name exists"), newParent.getCompletePath(), path ) );
                            continue;
                        }
                        if(newParent == null)
                        {
                            newParent = new FreemarkerCatalog( pathComponents[i], parent );
                            DataElementUtils.saveQuiet( newParent );
                        }
                        parent = ( FreemarkerCatalog ) newParent;
                    }
                    FreemarkerScriptOrCatalog scriptOrCatalog = parent.get( pathComponents[pathComponents.length-1] );
                    if(scriptOrCatalog instanceof FreemarkerCatalog)
                    {
                        loadContext.addWarning( new ReadException( new Exception("Cannot create script "+scriptName+": catalog with the same name exists"), scriptOrCatalog.getCompletePath(), path ) );
                        continue;
                    }
                    final FreemarkerScript script = new FreemarkerScript( pathComponents[pathComponents.length-1], parent );
                    if ( Files.exists( scriptsFile ) )
                    {
                        script.setLinkedFile( scriptsFile );
                    }
                    DataElementUtils.saveQuiet( script );
                }
                catch ( final Exception e )
                {
                    loadContext.addWarning( new ReadException( e, scripts.getCompletePath().getChildPath( scriptName ), scriptsFile ) );
                }
            }
        }
    }
    
    private class SecurityDeserializer extends FileDeserializer
    {
        
        private final SecurityCollection target;

        public SecurityDeserializer( final Path path, final SecurityCollection target ) throws ReadException
        {
            super( path );
            this.target = target;
        }

        @Override
        protected void doDeserialize( Object serializedRoot ) throws ReadException
        {
            final Map<String, Object> serializedContent = asMap( asMap( serializedRoot ).get( TAG_SECURITY ) );
            final Map<String, Object> serializedRoles = asMap( serializedContent.get( TAG_ROLES ) );
            final Map<String, Object> serializedGroups = asMap( serializedContent.get( TAG_ROLE_GROUPS ) );
            
            readRoles( serializedRoles );
            readGroups( serializedGroups );
            checkChildren( target, serializedContent, TAG_ROLES, TAG_ROLE_GROUPS );
            target.getProject().getAutomaticDeserializationService().registerFile( path, ManagedFileType.SECURITY );
        }
        
        private void readRoles( final Map<String, Object> serializedRoles )
        {
            for ( final Map.Entry<String, Object> serializedRole : serializedRoles.entrySet() )
            {
                try
                {
                    readRole( serializedRole );
                }
                catch ( ReadException e )
                {
                    loadContext.addWarning( e.attachElement( target.getRoleCollection() ) );
                }
            }
        }

        private void readRole( final Map.Entry<String, Object> serializedRole ) throws ReadException
        {
            final Map<String, Object> serializedRoleContent = asMap( serializedRole.getValue() );
            final String roleName = serializedRole.getKey();
            final Role role = new Role( roleName, target.getRoleCollection() );
            readDocumentation( serializedRoleContent, role );
            readUsedExtras( serializedRoleContent, role );
            save( role );
            checkChildren( role, serializedRoleContent, TAG_COMMENT, TAG_EXTRAS );
        }
        
        private void readGroups( Map<String, Object> serializedGroups )
        {
            for ( final Map.Entry<String, Object> serializedGroup : serializedGroups.entrySet() )
            {
                try
                {
                    readGroup( serializedGroup );
                }
                catch ( ReadException e )
                {
                    loadContext.addWarning( e.attachElement( target.getRoleGroupCollection() ) );
                }
            }
        }

        private void readGroup( Entry<String, Object> serializedGroup ) throws ReadException
        {
            final String roleGroupName = serializedGroup.getKey();
            final List<String> roles = asStrList( serializedGroup.getValue() );
            final RoleGroup roleGroup = new RoleGroup( roleGroupName, target.getRoleGroupCollection() );
            
            roleGroup.getRoleSet().parseRoles( roles );
            save( roleGroup );
        }
        
        public SecurityCollection getResult()
        {
            return target;
        }
        
    }
    
    private static class TableRefStructure
    {
        String tableTo;
        String columnTo;
        String view = DatabaseConstants.SELECTION_VIEW;
        String[] permittedTables;
        
        void applyTo( TableReference tableRef )
        {
            tableRef.setTableTo( tableTo );
            
            if ( tableTo != null && columnTo.isEmpty() ) // primary key
            {
                // no warnings, but the field will be empty
                // and it will be interpreted as a primary key
                tableRef.setColumnsTo( "" );
            }
            else
            {
                tableRef.setColumnsTo( columnTo );
            }
            
            tableRef.setPermittedTables( permittedTables );
            tableRef.setViewName( view );
        }
    }
    
    private class SchemeDeserializer extends BaseDeserializer
    {
        
        /**
         * Creates a scheme deserializer with empty path.
         */
        public SchemeDeserializer()
        {
            super();
        }
        
        public void deserialize( Map<String, Object> serializedEntityBody, Entity entity ) throws ReadException
        {
            readScheme( serializedEntityBody, entity );
            readReferences( serializedEntityBody, entity );
        }
        
        private void readReferences( final Map<String, Object> serializedEntityBody, final Entity entity ) throws ReadException
        {
            final Map<String, Object> serializedReferences;
            
            try
            {
                serializedReferences = asMapOrEmpty( serializedEntityBody.get( TAG_REFERENCES ) );
            }
            catch ( ReadException e )
            {
                loadContext.addWarning( e.attachElement( entity ) );
                return;
            }
            
            for ( final String name : serializedReferences.keySet() )
            {
                final String columnFrom = name;
                final Object content = serializedReferences.get( name );
                
                final TableRefStructure tableRefStructure = toTableReference( content );
                
                if ( tableRefStructure != null )
                {
                    final BeModelCollection<TableRef> references = entity.getOrCreateTableReferences();
                    final String tableRefName = TableRef.nameFor( columnFrom, tableRefStructure.tableTo );
                    final TableRef tableRef = new TableRef( tableRefName, columnFrom, references );
                    tableRef.setOriginModuleName( project.getProjectOrigin() );
                    if(content instanceof Map)
                        readUsedExtras( asMap(content), tableRef );
                    tableRefStructure.applyTo( tableRef );
                    DataElementUtils.saveQuiet( tableRef );
                }
            }
        }
        
        private void readScheme( Map<String, Object> serializedEntityBody, Entity entity ) throws ReadException
        {
            final Object serializedScheme = serializedEntityBody.get( TAG_SCHEME );
            
            if ( !( serializedScheme instanceof Map ) )
                return;

            @SuppressWarnings( "unchecked" )
            final Map<String, Object> schemeContent = ( Map<String, Object> ) serializedScheme;
            
            readSchemeContent( entity, schemeContent );
        }

        private void readSchemeContent( Entity entity, final Map<String, Object> schemeContent ) throws ReadException
        {
            if ( schemeContent.get( TAG_VIEW_DEFINITION ) instanceof String )
            {
                save( readViewDef( schemeContent, entity ) );
            }
            else
            {
                save( readTableDef( schemeContent, entity ) );
            }
        }
        
        private TableDef readTableDef( Map<String, Object> tableContent, Entity entity ) throws ReadException
        {
            final TableDef tableDef = new TableDef( entity );
            
            readDocumentation( tableContent, tableDef );
            readTableColumns( tableContent, tableDef );
            readTableIndices( tableContent, tableDef );
            readFields(tableDef, tableContent, Fields.tableDef());
            readUsedExtras( tableContent, tableDef );
            checkChildren( tableDef, tableContent, Fields.tableDef(), TAG_EXTRAS, TAG_COMMENT, TAG_INDICES, TAG_COLUMNS );
            
            return tableDef;
        }

        private void readTableIndices( Map<String, Object> tableContent, final TableDef tableDef )
        {
            final BeVectorCollection<IndexDef> indices = tableDef.getIndices();
            final List<Map<String, Object>> indicesList = asMaps( tableContent.get( TAG_INDICES ) );
            
            try
            {
                for ( Map<String, Object> indexElement : indicesList )
                {
                    for ( Map.Entry<String, Object> indexPair : indexElement.entrySet() ) // should be only one pair
                    {
                        IndexDef index = readIndexDef( indexPair.getKey(), asMap( indexPair.getValue() ), indices );
                        save( index );
                    }
                }
            }
            catch ( ReadException e )
            {
                loadContext.addWarning( e.attachElement( indices ) );
            }
        }

        private void readTableColumns( Map<String, Object> tableContent, final TableDef tableDef )
        {
            final BeVectorCollection<ColumnDef> columns = tableDef.getColumns();
            final List<Map<String, Object>> columnsList = asMaps( tableContent.get( TAG_COLUMNS ) );
            
            try
            {
                for ( Map<String, Object> columnElement : columnsList )
                {
                    for ( Map.Entry<String, Object> columnPair : columnElement.entrySet() ) // should be only one pair
                    {
                        ColumnDef column = readColumnDef( columnPair.getKey(), asMap( columnPair.getValue() ), columns );
                        save( column );
                    }
                }
            }
            catch ( ReadException e )
            {
                loadContext.addWarning( e.attachElement( columns ) );
            }
        }
        
        public ColumnDef readColumnDef( String columnName, Map<String, Object> columnElement, BeVectorCollection<ColumnDef> parent ) throws ReadException
        {
            ColumnDef column = new ColumnDef( columnName, parent );
            readDocumentation( columnElement, column );
            readFields( column, columnElement, Fields.columnDef() );
            readUsedExtras( columnElement, column );

            final Object serializedOldNames = columnElement.get( SerializationConstants.TAG_OLD_NAMES );
            
            if ( serializedOldNames != null )
            {
                final List<String> oldNames = asStrList( serializedOldNames );
                column.setOldNames( oldNames.toArray( new String[oldNames.size()] ) );
            }
            
            column.setOriginModuleName( column.getProject().getProjectOrigin() );
            
            final TableRefStructure tableRefStructure = toTableReference( columnElement.get( TAG_REFERENCE ) );
            
            if ( tableRefStructure != null )
            {
                tableRefStructure.applyTo( column );
            }
            
            checkChildren( column, columnElement, Fields.columnDef(), TAG_COMMENT, TAG_EXTRAS, TAG_REFERENCE, TAG_OLD_NAMES );
            
            return column;
        }
        
        public IndexDef readIndexDef( String indexName, Map<String, Object> indexElement, BeVectorCollection<IndexDef> parent ) throws ReadException
        {
            IndexDef index = new IndexDef( indexName, parent );
            index.setOriginModuleName( index.getProject().getProjectOrigin() );
            readDocumentation( indexElement, index );
            readFields( index, indexElement, Fields.indexDef() );
            readUsedExtras( indexElement, index );
            List<String> cols = asStrList( indexElement.get( TAG_COLUMNS ) );
            
            for ( String indexColumnStr : cols )
            {
                DataElementUtils.saveQuiet( IndexColumnDef.createFromString( indexColumnStr, index ) );
            }
            
            checkChildren( index, indexElement, Fields.indexDef(), TAG_COMMENT, TAG_EXTRAS, TAG_COLUMNS );
            
            return index;
        }
        
        private ViewDef readViewDef( final Map<String, Object> schemeContent, final Entity entity )
        {
            final ViewDef viewDef = new ViewDef( entity );
            readDocumentation( schemeContent, viewDef );
            
            final String viewDefinition = ( String ) schemeContent.get( TAG_VIEW_DEFINITION );
            assert viewDefinition != null;
            
            viewDef.setDefinition( viewDefinition );
            readFields(viewDef, schemeContent, Fields.viewDef());
            checkChildren( viewDef, schemeContent, Fields.viewDef(), TAG_VIEW_DEFINITION, TAG_COMMENT, TAG_EXTRAS );
            
            return viewDef;
        }
        
        private TableRefStructure toTableReference( Object content )
        {
            TableRefStructure tableRef = new TableRefStructure();
            
            if ( content instanceof String )
            {
                final String joined = ( String ) content;
                final List<String> splittedTo = StreamEx.split(joined, "\\.").toList();
                
                if ( splittedTo.size() == 1 )
                {
                    final String tableTo = splittedTo.get( 0 );
                    final String columnTo = "";
                    tableRef.tableTo = tableTo;
                    tableRef.columnTo = columnTo;
                    
                    return tableRef;
                }
                
                if ( splittedTo.size() >= 2 )
                {
                    final String tableTo = splittedTo.get( 0 );
                    final String columnTo = Strings2.joinTail( ".", splittedTo );
                    tableRef.tableTo = tableTo;
                    tableRef.columnTo = columnTo;
                    
                    return tableRef;
                }
            }
            else if ( content instanceof List )
            {
                final List<?> tablesRaw = ( List<?> ) content;
                final List<String> tables2 = new ArrayList<>();
                
                for ( Object tableRaw : tablesRaw )
                {
                    tables2.add( String.valueOf( tableRaw ) );
                }
                
                tableRef.permittedTables = tables2.toArray( new String[tables2.size()] );
                
                return tableRef;
            }
            else if ( content instanceof Map )
            {
                final Map<?, ?> map = ( Map<?, ?> ) content;
                final Object view = map.get( "view" );
                final Object to = map.get( "to" );
                
                if ( view != null )
                {
                    tableRef.view = String.valueOf( view );
                }
                
                if ( to instanceof String )
                {
                    final String joined = ( String ) to;
                    final List<String> splittedTo = StreamEx.split(joined, "\\.").toList();
                    
                    if ( splittedTo.size() == 1 )
                    {
                        final String tableTo = splittedTo.get( 0 );
                        final String columnTo = "";
                        tableRef.tableTo = tableTo;
                        tableRef.columnTo = columnTo;
                        
                        return tableRef;
                    }
                    
                    if ( splittedTo.size() >= 2 )
                    {
                        final String tableTo = splittedTo.get( 0 );
                        final String columnTo = Strings2.joinTail( ".", splittedTo );
                        tableRef.tableTo = tableTo;
                        tableRef.columnTo = columnTo;
                        
                        return tableRef;
                    }
                }
                else
                {
                    final List<?> tablesRaw = ( to instanceof List ) ? ( List<?> ) to : new ArrayList<>();
                    final List<String> tables2 = new ArrayList<>();
                    
                    for ( Object tableRaw : tablesRaw )
                    {
                        tables2.add( String.valueOf( tableRaw ) );
                    }
                    
                    tableRef.permittedTables = tables2.toArray( new String[tables2.size()] );
                    
                    return tableRef;
                }
            }
            
            return null;
        }
        
    }
    
    private final LoadContext loadContext;
    private Project project;
    private ProjectFileSystem fileSystem;
    private final ObjectCache<String> strings = new ObjectCache<>();
    private Project templates;
    // Whether the entity template should be fused with entity (false to inherit)
    private final boolean fuseTemplate;
    
    public List<String> stringCache( Collection<String> collection )
    {
        if ( collection == null )
            return null;
        List<String> result = new ArrayList<>();
        for(String str : collection)
        {
            result.add(strings.get( str ));
        }
        return result;
    }

    public static TableDef readTableDef( final LoadContext loadContext, Project project, String tableName, Map<String, Object> tableDefHash ) throws ReadException
    {
        /*
         * Information about primary key is contained in an entity, so any entity is required to deserialize some set of columns.
         * See ColumnDef#isPrimaryKey().
         */
        Entity entity = new Entity( tableName, project.getApplication(), EntityType.TABLE );
        return schemeDeserializer( loadContext ).readTableDef( tableDefHash, entity );
    }
    
    public static ColumnDef readColumnDef( final LoadContext loadContext, Project project, final String columnName, final Map<String, Object> columnContent ) throws ReadException
    {
        /*
         * Information about primary key is contained in an entity, so any entity is required to deserialize some set of columns.
         * See ColumnDef#isPrimaryKey().
         */
        Entity entity = new Entity( "table", project.getApplication(), EntityType.TABLE );
        return schemeDeserializer( loadContext ).readColumnDef( columnName, columnContent, new TableDef( entity ).getColumns() );
    }
    
    private static SchemeDeserializer schemeDeserializer( final LoadContext loadContext )
    {
        return new YamlDeserializer( loadContext ).new SchemeDeserializer();
    }
    
    public static Entity readEntity( final LoadContext loadContext, final String entityName, final Map<String, Object> content, final Module module)
    {
        YamlDeserializer yamlDeserializer = new YamlDeserializer( loadContext );
        yamlDeserializer.setProject( module.getProject() );
        EntityDeserializer entityDeserializer = yamlDeserializer.new EntityDeserializer();
        
        try
        {
            return entityDeserializer.readEntity( entityName, content, module );
        }
        catch ( ReadException e )
        {
            Entity entity = new Entity( entityName, module, EntityType.TABLE );
            loadContext.addWarning( e.attachElement( entity ) );
            return entity;
        }
    }
    
    public static Query readQuery( final LoadContext loadContext, final String queryName, final Map<String, Object> content, final Entity entity)
    {
        YamlDeserializer yamlDeserializer = new YamlDeserializer( loadContext );
        yamlDeserializer.setProject( entity.getProject() );
        EntityDeserializer entityDeserializer = yamlDeserializer.new EntityDeserializer();
        try
        {
            return entityDeserializer.readQuery( queryName, content, entity );
        }
        catch ( ReadException e )
        {
            Query query = new Query( queryName, entity );
            loadContext.addWarning( e.attachElement( query ) );
            return query;
        }
    }
    
    public static Operation readOperation( final LoadContext loadContext, final String operationName, final Map<String, Object> content, final Entity entity)
    {
        YamlDeserializer yamlDeserializer = new YamlDeserializer( loadContext );
        yamlDeserializer.setProject( entity.getProject() );
        EntityDeserializer entityDeserializer = yamlDeserializer.new EntityDeserializer();
        
        try
        {
            return entityDeserializer.readOperation( operationName, content, entity );
        }
        catch ( ReadException e )
        {
            Operation operation = Operation.createOperation( operationName, Operation.OPERATION_TYPE_JAVA, entity );
            loadContext.addWarning( e.attachElement( operation ) );
            return operation;
        }
    }
    
    /**
     * Parses a connection profile. Doesn't save it to connection profiles
     * collection.
     * 
     * @param serialized
     *            Must contain a serialized JSON with a map as a root element.
     *            This map should have only one key, that represents a
     *            connection profile name. The value should contain pairs of connection profile fields.
     * @throws ReadException
     * @throws ClassCastException
     * @see Fields#connectionProfile()
     * @see Fields#connectionProfileRead()
     */
    public static BeConnectionProfile deserializeConnectionProfile( final LoadContext loadContext, final String serialized, final Project project ) throws ReadException
    {
        @SuppressWarnings( "unchecked" ) // unsafe
        final LinkedHashMap<String, Object> namedProfile = ( LinkedHashMap<String, Object> ) new Yaml().load( serialized );
        final String profileName = namedProfile.keySet().iterator().next();
        @SuppressWarnings( "unchecked" ) // unsafe
        final Map<String, Object> serializedProfileBody = ( Map<String, Object> ) namedProfile.get( profileName );
        final BeConnectionProfile profile = readConnectionProfile( loadContext, profileName, serializedProfileBody, project );
        return profile;
    }
    
    /**
     * Parses a connection profile. Doesn't save it to connection profiles collection.
     * 
     * @param serializedProfileBody just a map of properties
     * @throws ReadException
     */
    private static BeConnectionProfile readConnectionProfile( final LoadContext loadContext, final String profileName, final Map<String, Object> serializedProfileBody, final Project project ) throws ReadException
    {
        final YamlDeserializer yamlDeserializer = new YamlDeserializer( loadContext );
        final ConnectionProfilesDeserializer connectionProfilesDeserializer = yamlDeserializer.new ConnectionProfilesDeserializer( project
                .getConnectionProfiles().getLocalProfiles() );
        final BeConnectionProfile connectionProfile = connectionProfilesDeserializer.deserializeConnectionProfile( profileName,
                serializedProfileBody );
        
        return connectionProfile;
    }
    
    private Project getTemplates() throws ReadException
    {
        if(templates == null)
        {
            templates = Templates.getTemplatesProject();
            templates.mergeHostProject( project );
        }
        return templates;
    }

    public YamlDeserializer( final LoadContext loadContext )
    {
        this(loadContext, false);
    }
    
    public YamlDeserializer( final LoadContext loadContext, boolean fuseTemplate )
    {
        this.loadContext = loadContext;
        this.fuseTemplate = fuseTemplate;
    }
    
    public Project readProject( final Path root ) throws ReadException
    {
        final ProjectDeserializer projectDeserializer = new ProjectDeserializer( ProjectFileSystem.getProjectFile( root ) );
        projectDeserializer.deserialize();
        
        return this.project;
    }
    
    public Entity reloadEntity( final Entity oldEntity ) throws ReadException
    {
        this.fileSystem = new ProjectFileSystem( oldEntity.getProject() );
        this.setProject( oldEntity.getProject() );
        final Entity entity = this.readEntity( oldEntity.getModule(), oldEntity.getName() );
        
        if ( oldEntity.getPrototype() != null )
        {
            @SuppressWarnings( "unchecked" )
            final BeModelCollection<BeModelElement> prototype = ( BeModelCollection<BeModelElement> ) oldEntity.getPrototype();
            entity.merge( prototype, true, true );
        }
        
        EntitiesFactory.addToModule( entity, oldEntity.getModule() );

        return entity;
    }
    
    public LanguageLocalizations reloadLocalization( final Path path, final Localizations localizations ) throws ReadException
    {
        final String lang = path.getFileName().toString().replaceFirst( "\\.\\w+$", "" );
        final LocalizationDeserializer localizationDeserializer = new LocalizationDeserializer( lang, path, localizations );
        localizationDeserializer.deserialize();
        
        return localizationDeserializer.getResult();
    }
    
    public MassChanges reloadMassChanges( final Path path, final Module application ) throws ReadException
    {
        final MassChanges massChanges = application.newMassChangeCollection();
        final MassChangesDeserializer massChangesDeserializer = new MassChangesDeserializer( path, massChanges );
        massChangesDeserializer.deserialize();
        DataElementUtils.saveQuiet( massChangesDeserializer.getResult() );
        
        return massChangesDeserializer.getResult();
    }
    
    public SecurityCollection reloadSecurityCollection( final Path path, final Project project ) throws ReadException
    {
        final SecurityCollection security = project.newSecurityCollection();
        final SecurityDeserializer securityDeserializer = new SecurityDeserializer( path, security );
        securityDeserializer.deserialize();
        DataElementUtils.saveQuiet( securityDeserializer.getResult() );
        
        return securityDeserializer.getResult();
    }
    
    public BeConnectionProfiles reloadConnectionProfiles( final Path path, final BeConnectionProfileType type, final BeConnectionProfilesRoot target ) throws ReadException
    {
        final ConnectionProfilesDeserializer profilesDeserializer = new ConnectionProfilesDeserializer( path, type, target );
        profilesDeserializer.deserialize();
        
        return profilesDeserializer.getResult();
    }
    
    public PageCustomizations reloadCustomizations( final Path path, final Module target ) throws ReadException
    {
        this.project = target.getProject();
        final CustomizationDeserializer deserializer = new CustomizationDeserializer( path, target ).replace();
        deserializer.deserialize();
        
        return deserializer.getResult();
    }
    
    public Daemons reloadDaemons( final Path path, final Module target ) throws ReadException
    {
        final Daemons daemons = new Daemons( target );
        final DaemonsDeserializer deserializer = new DaemonsDeserializer( path, daemons );
        deserializer.deserialize();
        DataElementUtils.saveQuiet( daemons );
        
        return daemons;
    }
    
    public JavaScriptForms reloadForms( final Path path, final Module target ) throws ReadException
    {
        final JavaScriptForms forms = new JavaScriptForms( target ); 
        final FormsDeserializer deserializer = new FormsDeserializer( path, forms );
        deserializer.deserialize();
        DataElementUtils.saveQuiet( forms );
        
        return forms;
    }
    
    public StaticPages reloadPages( final Path path, final Module target ) throws ReadException
    {
        final StaticPages pages = new StaticPages( target );
        final StaticPagesDeserializer deserializer = new StaticPagesDeserializer( path, pages );
        deserializer.deserialize();
        DataElementUtils.saveQuiet( pages );
        
        return pages;
    }
    
    public void loadMacroFiles( final Module module ) throws ReadException
    {
        new MacrosDeserializer( module ).deserialize();
    }
    
    private void setProject( Project project )
    {
        if ( this.project != null )
            throw new IllegalStateException();
        
        this.project = project;
        this.fileSystem = null;
    }
    
    private ProjectFileSystem getFileSystem()
    {
        if(this.fileSystem == null)
        {
            this.fileSystem = new ProjectFileSystem( project );
        }
        return fileSystem;
    }

    private Entity readEntity( final Module module, final String name ) throws ReadException
    {
        try
        {
            final EntityDeserializer entityDeserializer = new EntityDeserializer( module, name );
            entityDeserializer.deserialize();
            return entityDeserializer.getEntity();
        }
        catch ( ReadException e )
        {
            throw e.attachElement( module );
        }
    }
    
    private void readStaticPages( final BeModelCollection<LanguageStaticPages> target )
    {
        if ( project == null )
            throw new IllegalStateException();
        
        try
        {
            new StaticPagesDeserializer( getFileSystem().getStaticPagesFile(), target ).deserialize();
        }
        catch ( final ReadException e )
        {
            loadContext.addWarning( e.attachElement( target ) );
        }
    }
    
    private void readSecurity( final SecurityCollection target )
    {
        if ( project == null )
            throw new IllegalStateException();
        
        try
        {
            new SecurityDeserializer( getFileSystem().getSecurityFile(), target ).deserialize();
        }
        catch ( final ReadException e )
        {
            loadContext.addWarning( e.attachElement( target ) );
        }
    }
    
    private void readCustomization( final Module target )
    {
        if ( project == null )
            throw new IllegalStateException();
        
        try
        {
            new CustomizationDeserializer( getFileSystem().getCustomizationFile(), target ).deserialize();
        }
        catch ( final ReadException e )
        {
            loadContext.addWarning( e.attachElement( target ) );
        }
        catch( Exception e)
        {
            loadContext.addWarning( new ReadException( e, target, getFileSystem().getCustomizationFile() ) );
        }
    }
    
    /**
     * Used with customizations (module), entity, query, operation and static page.
     */
    @SuppressWarnings( "unchecked" )
    private void readCustomizations( final Map<String, Object> serialized, final BeVectorCollection<?> target, boolean replace )
    {
        if ( project == null )
            throw new IllegalStateException();
        
        final Map<String, Object> serializedCustomizations = ( Map<String, Object> ) serialized.get( "customizations" );
        
        if ( serializedCustomizations == null || serializedCustomizations.isEmpty() )
            return;
        
        final BeVectorCollection<PageCustomization> customizations = replace ? new PageCustomizations( target ) : target.getOrCreateCollection( PageCustomization.CUSTOMIZATIONS_COLLECTION, PageCustomization.class );
        
        try
        {
            for ( final String name : serializedCustomizations.keySet() )
            {
                final Map<String, Object> content = ( Map<String, Object> ) serializedCustomizations.get( name );
                final List<String> splitted = StreamEx.split(name, "\\.").toList();
                final String type;
                final String domain;
                
                if ( splitted.size() == 1 )
                {
                    type = "";
                    domain = splitted.get( 0 );
                }
                else
                {
                    type = splitted.get( splitted.size() - 1 );
                    splitted.remove( splitted.size() - 1 );
                    domain = String.join( ".", splitted );
                }
                
                final PageCustomization customization = new PageCustomization( type, domain, customizations );
                customization.setCode( ( String ) content.get( TAG_CODE ) );
                customization.setOriginModuleName( project.getProjectOrigin() );
                DataElementUtils.saveQuiet( customization );
            }
        }
        catch ( Exception e )
        {
            loadContext.addWarning( new ReadException( e, target, project.getLocation() ) );
        }
        
        if ( replace )
            DataElementUtils.save( customizations );
    }
    
    private void readDaemons( final BeModelCollection<Daemon> daemonCollection )
    {
        try
        {
            new DaemonsDeserializer( getFileSystem().getDaemonsFile(), daemonCollection ).deserialize();
        }
        catch ( final ReadException e )
        {
            loadContext.addWarning( e );
        }
    }
    
    private void readMassChanges( final MassChanges massChangeCollection )
    {
        try
        {
            new MassChangesDeserializer( getFileSystem().getMassChangesFile(), massChangeCollection ).deserialize();
        }
        catch ( final ReadException e )
        {
            loadContext.addWarning( e.attachElement( massChangeCollection ) );
        }
    }
    
    private void readConnectionProfiles( BeConnectionProfilesRoot target )
    {
        for ( final BeConnectionProfileType type : BeConnectionProfileType.values() )
        {
            try
            {
                readConnectionProfiles( type, target );
            }
            catch ( final ReadException e )
            {
                loadContext.addWarning( e );
            }
        }
    }
    
    private void readConnectionProfiles( BeConnectionProfileType type, BeConnectionProfilesRoot target ) throws ReadException
    {
        Path connectionProfilesFile = getFileSystem().getConnectionProfilesFile( type );
        
        if ( target.getProject().isModuleProject() && !Files.exists( connectionProfilesFile ) )
            return;
        
        if ( type == BeConnectionProfileType.LOCAL && !Files.exists( connectionProfilesFile ) )
        {
            target.put( new BeConnectionProfiles( BeConnectionProfileType.LOCAL, target ) );
            return;
        }
        
        new ConnectionProfilesDeserializer( connectionProfilesFile, type, target ).deserialize();
    }

    private void readForms( BeModelCollection<JavaScriptForm> formCollection )
    {
        try
        {
            new FormsDeserializer( getFileSystem().getJavaScriptFormsFile(), formCollection ).deserialize();
        }
        catch ( final ReadException e )
        {
            loadContext.addWarning( e );
        }
    }
    
    private void readLocalizations( final List<String> languages, final Localizations localizations )
    {
        for ( final String lang : languages )
        {
            try
            {
                new LocalizationDeserializer( lang, getFileSystem().getLocalizationFile( lang ), localizations ).deserialize();
            }
            catch ( ReadException e )
            {
                loadContext.addWarning( e.attachElement( localizations ) );
            }
        }
    }
    
    private void readMacroFiles( BaseDeserializer deserializer, Map<String, Object> serializedModuleBody, FreemarkerCatalog macroFiles ) throws ReadException
    {
        final Object includes = serializedModuleBody.get( TAG_MACRO_FILES );
        
        if ( includes == null )
            return;
        
        for ( final String scriptName : deserializer.asStrList( includes ) )
        {
            final Path macroFile = getFileSystem().getMacroFile( scriptName );
            try
            {
                final FreemarkerScript script = new FreemarkerScript( scriptName, macroFiles );
                if ( Files.exists( macroFile ) )
                {
                    script.setLinkedFile( macroFile );
                }
                DataElementUtils.saveQuiet( script );
            }
            catch ( final Exception e )
            {
                loadContext.addWarning( new ReadException( e, macroFiles.getCompletePath().getChildPath( scriptName ), macroFile ) );
            }
        }
    }
    
    private ProjectFileStructure readProjectFileStructure( BaseDeserializer deserializer, final Map<String, Object> serializedPfs, final Project project )
    {
        final ProjectFileStructure pfs = new ProjectFileStructure( project );
        deserializer.readFields( pfs, serializedPfs, Fields.projectFileStructure() );
        
        return pfs;
    }
    
}
