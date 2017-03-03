package com.developmentontheedge.be5.metadata.sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import one.util.streamex.StreamEx;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;
import com.developmentontheedge.be5.metadata.freemarker.FreemarkerUtils;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.DdlElement;
import com.developmentontheedge.be5.metadata.model.EntitiesFactory;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityLocalizations;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Icon;
import com.developmentontheedge.be5.metadata.model.IndexColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexDef;
import com.developmentontheedge.be5.metadata.model.JavaScriptForm;
import com.developmentontheedge.be5.metadata.model.JavaScriptOperationExtender;
import com.developmentontheedge.be5.metadata.model.LanguageStaticPages;
import com.developmentontheedge.be5.metadata.model.Localizations;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationExtender;
import com.developmentontheedge.be5.metadata.model.PageCustomization;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.developmentontheedge.be5.metadata.model.QuickFilter;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.StaticPage;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.TableRef;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.metadata.model.ViewDef;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.model.base.BeVectorCollection;
import com.developmentontheedge.be5.metadata.sql.pojo.DaemonInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.EntityInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.ExtenderInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.IconInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.IndexInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.LocalizationInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.OperationInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.PageCustomizationInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.QueryInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.QueryPerRoleInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.QuerySettingsInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.QuickFilterInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.SqlColumnInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.StaticPageInfo;
import com.developmentontheedge.be5.metadata.sql.type.DbmsTypeManager;
import com.developmentontheedge.be5.metadata.sql.type.DefaultTypeManager;
import com.developmentontheedge.be5.metadata.util.DerivedController;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import com.developmentontheedge.be5.metadata.util.ObjectCache;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.ExtendedSqlException;

/**
 * Reads Project model from BeanExplorer metadata.
 * 
 * @pending - make all methods static  
 */
public class SqlModelReader 
{
    public static final String ORPHANS_MODULE_NAME = "beanexplorer_orphans";
    private static final String COMMENT_START = "<!--";
    private static final String COMMENT_END = "-->";
    private static final Pattern ON_LOAD_PATTERN = Pattern.compile( "^\\s*if\\(\\s*isIE\\(\\)\\s*\\)\\s*\\{\\s*"
        + "var\\s*_w_onload\\s*=\\s*window\\.onload;\\s*"
        + "window.onload\\s*=\\s*_w_onload\\s*\\?\\s*function\\(\\)\\s*\\{\\s*"
        + "_w_onload\\(\\);\\s*(\\w+)\\(\\);\\s*\\}\\s*"
        + "\\:\\s*\\1;\\s*\\}\\s*"
        + "else\\s*\\{\\s*"
        + "window.addEventListener\\(\"load\",\\s*\\1,\\s*false\\);\\s*\\}", Pattern.MULTILINE);
    
    private static final List<String> PREDEFINED_COLUMN_NAMES = Arrays.asList( "whoInserted___", "whoModified___", "creationDate___",
            "modificationDate___", "activeFrom", "activeTo", "ID", "CODE" );
    
    public static final int READ_META = 0x01;
    public static final int READ_TABLEDEFS = 0x02;
    public static final int READ_LOCALIZATIONS = 0x04;
    public static final int READ_SECURITY = 0x08;
    public static final int READ_ALL = READ_TABLEDEFS | READ_LOCALIZATIONS | READ_META | READ_SECURITY;
    
    public static final int LOG_ERRORS = 0x10;
    public static final int USE_HEURISTICS = 0x20;
    public static final int SKIP_APPLICATION = 0x40;
    public static final int READ_ORPHANS_MODULE = 0x80;
    public static final int ADVANCED_HEURISTICS = 0x100;
    
    private final ObjectCache<String> strings = new ObjectCache<>();

    public enum Active 
    {
        YES("yes"),
        NO("no");
        
        private String active;
        
        Active(String active)
        {
            this.active = active;
        }
        
        public String getActive()
        {
            return active;
        }
        
        public static boolean parseBoolean(String str)
        {
            return YES.getActive().equals( str );
        }
    }
    
    private BeSqlExecutor sql;
    private Rdbms rdbms;
    private int mode;
    private final List<String> warnings = new ArrayList<>();
    private String skippedOrigin;
    
    // cache
    private List<EntityInfo>     entities;
    private Set<String>          roles;
    private Map<String, String>  tableTypes;
    private List<DaemonInfo>     daemonInfos;
    private List<StaticPageInfo> staticPages;
    private Map<String, List<OperationInfo>>         operations;
    private Map<String, List<QueryInfo>>             queries;
    private Map<Long, QueryInfo>                     id2query;
    private Map<Long, List<String>>                  rolesByOperation;
    private Map<Long, List<QueryPerRoleInfo>>        rolesByQuery;
    private Map<Long, List<Long>>                    operationsByQuery;
    private Map<Long, List<QuickFilterInfo>>         quickFilters;
    private Map<Long, List<QuerySettingsInfo>>       querySettings;
    private Map<Long, List<ExtenderInfo>>            operationExtenders;
    private Map<String, List<LocalizationInfo>>      localizationInfos;
    private Map<String, IconInfo>                    icons;
    private Map<String, List<PageCustomizationInfo>> pageCustomizations;
    private Map<String, List<SqlColumnInfo>>         columns;
    private Map<String, List<IndexInfo>>             indices;
    private Map<String, Map<String, Set<String>>>    genericRefEntities;
    private Map<String, String>                      jsForms;
    
    private final Map<String, String>                columnNames = new HashMap<>(); 
    private String defSchema = null;
    
    public SqlModelReader( DatabaseConnector connector )
    {
        this( connector, READ_ALL | USE_HEURISTICS );
    }
    
    /**
     * Initializes model reader with the given database connector.
     *
     * @param connector Database connector.
     */
    public SqlModelReader( DatabaseConnector connector, int mode )
    {
        try
        {
            this.sql = new BeSqlExecutor( connector );
            this.rdbms = DatabaseUtils.getRdbms( connector );
            this.mode = mode;
        }
        catch( IOException e )
        {
            // File with SQL queries not found
            throw new RuntimeException( e );
        }
    }
    
    /**
     * Initializes model reader with the given database connector.
     *
     * @param connector Database connector.
     */
    public SqlModelReader( BeSqlExecutor executor, int mode )
    {
        this.sql = executor;
        this.rdbms = DatabaseUtils.getRdbms( executor.getConnector() );
        this.mode = mode;
    }
    
    public Project readProject(final String name) throws ExtendedSqlException, SQLException, ProcessInterruptedException
    {
        return readProject(name, false);
    }

    public Project readProject(final String name, final boolean moduleProject) throws ExtendedSqlException, SQLException, ProcessInterruptedException
    {
        return readProject( new NullLogger(), name, moduleProject );
    }

    public Project readProject(final ProcessController controller, final String name) throws ExtendedSqlException, SQLException, ProcessInterruptedException
    {
        return readProject(controller, name, false);
    }

    public Project readProject(final ProcessController controller, final String name, final boolean moduleProject) throws ExtendedSqlException, SQLException, ProcessInterruptedException
    {
        Project project = new Project(name, moduleProject);
        readProject(project, controller);
        return project;
    }

    private void readProject( final Project project, ProcessController controller ) throws ExtendedSqlException, SQLException, ProcessInterruptedException
    {
        long time = System.currentTimeMillis();
        this.skippedOrigin = (mode & SKIP_APPLICATION) == 0 ? "" : project.getProjectOrigin();
        cache(new DerivedController( controller, 0, 0.9, "Caching" ));
        
        addColumnNameGuesses();
        
        project.setDatabaseSystem(rdbms);
        if((mode & READ_SECURITY) != 0)
        {
            controller.setOperationName( "Processing security..." );
            project.setRoles( roles );
        }
        controller.setProgress( 0.92 );
        if((mode & READ_META) != 0)
        {
            controller.setOperationName( "Processing meta..." );
            Module module = null;
            for ( final EntityInfo entityInfo : entities )
            {
                final String moduleName = entityInfo.getOrigin();
                
                if ( module == null || !moduleName.equals( module.getName() ) )
                {
                    // started to read entities from another module =>
                    // finalize the previous module with remaining data
                    finalizeModule( module ); // #1
                    
                    module = createModule( moduleName, project );
                }
                
                final String sqlName = entityInfo.getType();
                final Entity entity = createEntity( sqlName, entityInfo, module );
                
                if ( entity != null ) // metadata or incorrect type, ignore
                {
                    EntitiesFactory.addToModule( entity, module );
                }
            }
            
            // finalize the last module as we couldn't
            // apply #1 to the last one
            finalizeModule( module );
            
            if(!project.isModuleProject())
            {
                addStaticPages( project.getApplication() );
                addJavaScriptForms( project.getApplication() );
            }
        }
        controller.setProgress( 0.95 );
        if((mode & READ_LOCALIZATIONS) != 0)
        {
            controller.setOperationName( "Processing localization..." );
            for(Entry<String, List<LocalizationInfo>> entry : localizationInfos.entrySet())
            {
                Module module = project.getModule( entry.getKey() );
                if(module == null)
                {
                    module = new Module(entry.getKey(), project.getModules());
                    DataElementUtils.saveQuiet( module );
                }
                addLocalizations( module, entry.getValue() );
            }
        }
        controller.setProgress( 0.98 );
        if((mode & READ_ORPHANS_MODULE) != 0)
        {
            readOrphans( project );
        }
        if((mode & LOG_ERRORS) != 0)
        {
            controller.setOperationName( "Checking errors..." );
            checkFinally();
            if(!warnings.isEmpty())
            {
                System.err.println( warnings.size()+" warning(s) during loading the project from "+sql.getConnector().getConnectString() );
                Collections.sort( warnings );
                for(String warning : warnings)
                {
                    System.err.println(warning);
                }
            }
        }
        //System.err.println( "SQL reading: "+(System.currentTimeMillis()-time) );
        controller.setOperationName( "SQLs read in " + ( System.currentTimeMillis() - time ) + "ms." );
        controller.setProgress( 1.0 );
    }

    private void addColumnNameGuesses()
    {
        if((mode & USE_HEURISTICS) == 0)
            return;
        for(String predefinedColumnName : PREDEFINED_COLUMN_NAMES)
        {
            columnNames.put( predefinedColumnName.toLowerCase(), predefinedColumnName );
        }
        for(EntityInfo entity : entities)
        {
            String name = entity.getName();
            String columnName = name+"ID";
            columnNames.put( columnName.toLowerCase(Locale.ENGLISH), columnName );
            if(name.endsWith( "ies" ))
            {
                columnName = name.substring( 0, name.length()-3 )+"yID";
                columnNames.put( columnName.toLowerCase(Locale.ENGLISH), columnName );
            }
            if(name.endsWith( "s" ))
            {
                columnName = name.substring( 0, name.length()-1 )+"ID";
                columnNames.put( columnName.toLowerCase(Locale.ENGLISH), columnName );
            }
        }
        if((mode & ADVANCED_HEURISTICS) == 0)
            return;
        StreamEx.ofValues(queries).flatMap(List::stream).map( QueryInfo::getQuery )
            .flatMap( Pattern.compile( "\\W+" )::splitAsStream )
            .distinct()
            .filter( word -> !word.equals( word.toUpperCase(Locale.ENGLISH) ) &&
                    !word.equals( word.toLowerCase(Locale.ENGLISH) ))
            .forEach( word -> columnNames.putIfAbsent( word.toLowerCase(Locale.ENGLISH), word ) );
    }

    private Module createModule( final String moduleName, final Project project )
    {
        final Module module;
        
        if ( moduleName.equals( project.getProjectOrigin() ) )
        {
            module = project.getApplication();
        }
        else
        {
            module = new Module( moduleName, project.getModules() );
            DataElementUtils.saveQuiet( module );
        }
        
        return module;
    }
    
    public List<String> getWarnings()
    {
        return warnings;
    }

    /**
     * @return special "orphans" module which contains the tables which appear to the database, but absent in the entities table
     */
    private Module readOrphans(Project project) throws ExtendedSqlException, SQLException
    {
        Module module = new Module( ORPHANS_MODULE_NAME, project.getModules() );
        if(columns != null)
        {
            for(String tableName : columns.keySet())
            {
                Entity entity = new Entity( tableName, module, EntityType.TABLE );
                DataElementUtils.save( entity );
            }
        }
        addTableDefs( module );
        DataElementUtils.save( module );
        return module;
    }

    private void finalizeModule( Module module ) throws ExtendedSqlException, SQLException
    {
        if ( module != null )
        {
            if((mode & READ_TABLEDEFS) != 0)
            {
                addTableDefs( module );
            }
            addTableRefs( module.getName(), module );
            addDaemons( module );
            addCustomizations( module );
        }
    }
    
    private void addCustomizations( Module module )
    {
        for(String domain : PageCustomization.getDomains( module ))
        {
            String prefix = "/"+domain;
            List<PageCustomizationInfo> infos = pageCustomizations.remove( prefix );
            if(infos == null)
                continue;
            for(PageCustomizationInfo info : infos)
            {
                if(module.getName().equals( info.getOrigin() ))
                {
                    createPageCustomization( module, domain, "/"+domain, info );
                }
            }
        }
    }
    
    private void addStaticPages( Module module )
    {
        for(StaticPageInfo staticPageInfo: staticPages)
        {
            BeVectorCollection<LanguageStaticPages> langPages = module.getStaticPageCollection();
            LanguageStaticPages languageStaticPages = langPages.get( staticPageInfo.getLang() );
            if(languageStaticPages == null)
            {
                languageStaticPages = new LanguageStaticPages( staticPageInfo.getLang(), langPages );
                DataElementUtils.saveQuiet( languageStaticPages );                
            }
            String name = staticPageInfo.getName();
            StaticPage page = new StaticPage( name, languageStaticPages );
            page.setContent( staticPageInfo.getContent() );
            page.setId( staticPageInfo.getId() );
            createPageCustomizations( name, page );
            DataElementUtils.saveQuiet( page );
        }
    }
    
    private void addJavaScriptForms( Module module )
    {
        BeModelCollection<JavaScriptForm> formsCollection = module.getCollection( Module.JS_FORMS, JavaScriptForm.class );
        for(Entry<String, String> entry: jsForms.entrySet())
        {
            JavaScriptForm form = new JavaScriptForm( entry.getKey(), formsCollection );
            form.setSource( entry.getValue() );
            DataElementUtils.saveQuiet( form );
        }
    }
    
    private void addDaemons( Module module )
    {
        for(DaemonInfo daemonInfo : daemonInfos)
        {
            if ( module.getName().equals( daemonInfo.getOrigin() ) )
            {
                Daemon daemon = new Daemon(daemonInfo.getName(), module.getDaemonCollection());
                daemon.setClassName( daemonInfo.getClassName() );
                daemon.setConfigSection( daemonInfo.getConfigSection() );
                daemon.setDaemonType( daemonInfo.getDaemonType() );
                daemon.setDescription( daemonInfo.getDescription() );
                daemon.setSlaveNo( daemonInfo.getSlaveNo() );
                DataElementUtils.saveQuiet( daemon );
            }
        }
    }

    private void addLocalizations( Module module, List<LocalizationInfo> moduleLocalizations )
    {
        Localizations localizations = module.getLocalizations();
        for(LocalizationInfo localization : moduleLocalizations)
        {
            if((mode & USE_HEURISTICS) != 0)
            {
                if(localization.getTopics().size() == 1 && localization.getTopics().get( 0 ).equals( EntityLocalizations.DISPLAY_NAME_TOPIC ))
                {
                    localizations.addLocalization( localization.getLangcode(), localization.getEntity(), localization.getTopics(),
                            EntityLocalizations.DISPLAY_NAME_TOPIC, localization.getMessage() );
                    continue;
                }
            }
            localizations.addLocalization( localization.getLangcode(), localization.getEntity(), localization.getTopics(),
                    localization.getMessagekey(), localization.getMessage() );
        }
    }

    private List<OperationInfo> operationsByEntity( final String entityName )
    {
        final List<OperationInfo> result = operations.get( entityName );
        return result == null ? Collections.<OperationInfo>emptyList() : result;
    }
    
    private List<QueryInfo> queriesByEntity( final String entityName )
    {
        final List<QueryInfo> result = queries.get( entityName );
        return result == null ? Collections.<QueryInfo>emptyList() : result;
    }

    private void cache(ProcessController controller) throws ExtendedSqlException, SQLException, ProcessInterruptedException
    {
        double totalWork = 0;
        int worked = 0;
        if ( ( mode & READ_TABLEDEFS ) != 0 )
        {
            totalWork+=140;
        }
        if ( ( mode & READ_SECURITY ) != 0 )
        {
            totalWork+=1;
        }
        if ( ( mode & READ_META ) != 0 )
        {
            totalWork+=40;
        }
        if ( ( mode & READ_LOCALIZATIONS ) != 0 )
        {
            totalWork+=100;
        }
        if ( ( mode & LOG_ERRORS ) != 0 )
        {
            totalWork+=2;
        }
        if ( ( mode & READ_TABLEDEFS ) != 0 )
        {
            controller.setOperationName( "Reading schema" );
            this.defSchema  = rdbms.getSchemaReader().getDefaultSchema( sql );
            this.tableTypes = rdbms.getSchemaReader().readTableNames( sql, defSchema,
                    new DerivedController( controller, worked / totalWork, ( worked + 10 ) / totalWork ) );
            controller.setProgress( (worked+=10)/totalWork );
            this.columns = rdbms.getSchemaReader().readColumns( sql, defSchema,
                    new DerivedController( controller, worked / totalWork, ( worked + 90 ) / totalWork ) );
            controller.setProgress( (worked+=90)/totalWork );
            this.indices = rdbms.getSchemaReader().readIndices( sql, defSchema,
                    new DerivedController( controller, worked / totalWork, ( worked + 40 ) / totalWork ) );
            controller.setProgress( (worked+=40)/totalWork );
        }
        if ( ( mode & READ_SECURITY ) != 0 )
        {
            controller.setOperationName( "Reading roles" );
            this.roles              = readAllRoles();
            controller.setProgress( (worked+=1)/totalWork );
        }
        if ( ( mode & READ_META ) != 0 )
        {
            controller.setOperationName( "Reading entities" );
            this.entities           = readAllEntities();
            controller.setProgress( (worked+=1)/totalWork );
            controller.setOperationName( "Reading queries" );
            this.queries            = readAllQueries();
            this.id2query           = indexQueries(this.queries);
            controller.setProgress( (worked+=5)/totalWork );
            controller.setOperationName( "Reading operations" );
            this.operations         = readAllOperations();
            controller.setProgress( (worked+=5)/totalWork );
            controller.setOperationName( "Reading query roles" );
            this.rolesByQuery       = readAllQueryRoles();
            controller.setProgress( (worked+=5)/totalWork );
            controller.setOperationName( "Reading operation roles" );
            this.rolesByOperation   = readAllOperationRoles();
            controller.setProgress( (worked+=5)/totalWork );
            controller.setOperationName( "Reading operations per query" );
            this.operationsByQuery  = readAllQueryOperations();
            controller.setProgress( (worked+=3)/totalWork );
            controller.setOperationName( "Reading quick filters" );
            this.quickFilters       = readAllQuickFilters();
            controller.setProgress( (worked+=1)/totalWork );
            controller.setOperationName( "Reading query settings" );
            this.querySettings      = readAllQuerySettings();
            controller.setProgress( (worked+=1)/totalWork );
            controller.setOperationName( "Reading icons" );
            this.icons              = readAllIcons();
            controller.setProgress( (worked+=1)/totalWork );
            controller.setOperationName( "Reading JavaScript forms" );
            this.jsForms            = readAllJavaScriptForms();
            controller.setProgress( (worked+=3)/totalWork );
            controller.setOperationName( "Reading extenders" );
            this.operationExtenders = readAllOperationExtenders();
            controller.setProgress( (worked+=3)/totalWork );
            controller.setOperationName( "Reading static pages" );
            this.staticPages        = readAllStaticPages();
            controller.setProgress( (worked+=2)/totalWork );
            controller.setOperationName( "Reading daemons" );
            this.daemonInfos        = readAllDaemons();
            controller.setProgress( (worked+=1)/totalWork );
            controller.setOperationName( "Reading customizations" );
            this.pageCustomizations = readAllCustomizations();
            controller.setProgress( (worked+=3)/totalWork );
            controller.setOperationName( "Reading generic refs" );
            this.genericRefEntities = readAllGenericRefEntities();
            controller.setProgress( (worked+=1)/totalWork );
        }
        if ( ( mode & READ_LOCALIZATIONS ) != 0 )
        {
            controller.setOperationName( "Reading localizations" );
            this.localizationInfos = readAllLocalizations();
            controller.setProgress( (worked+=100)/totalWork );
        }
        if ( ( mode & LOG_ERRORS ) != 0 )
        {
            controller.setOperationName( "Checking errors" );
            checkCache();
            controller.setProgress( (worked+=2)/totalWork );
        }
        assert(worked == totalWork);
    }

    private void checkCache()
    {
        Map<String, String> entityNames = new HashMap<>();
        for(EntityInfo entity : entities)
        {
            entityNames.put( entity.getName(), entity.getOrigin() );
        }
        for(Entry<String, List<QueryInfo>> entry : queries.entrySet())
        {
            String entityModule = entityNames.get( entry.getKey() );
            Set<String> queryNames = new HashSet<>();
            for(QueryInfo info : entry.getValue())
            {
                if(queryNames.contains( info.getName() ))
                {
                    warnings.add("Duplicate query name "+entry.getKey()+"."+info.getName());
                }
                queryNames.add( info.getName() );
                if ( !info.getName().equals( Query.SPECIAL_LOST_RECORDS ) && Project.APPLICATION.equals( entityModule )
                    && !info.getOrigin().equals( Project.APPLICATION ) )
                {
                    warnings.add("Query "+entry.getKey()+"."+info.getName()+" is defined in the module "+info.getOrigin()+", but entity is defined in the application");
                }
            }
            if(entityModule == null)
            {
                warnings.add( "There are "+(entry.getValue().size())+" queries belonging to missing entity '"+entry.getKey()+"': "+queryNames );
            }
        }
        for(Entry<String, List<OperationInfo>> entry : operations.entrySet())
        {
            String entityModule = entityNames.get( entry.getKey() );
            Set<String> operationNames = new HashSet<>();
            for(OperationInfo info : entry.getValue())
            {
                operationNames.add( info.getName() );
                if(Project.APPLICATION.equals( entityModule ) && !info.getOrigin().equals( Project.APPLICATION ))
                {
                    warnings.add("Operation "+entry.getKey()+"."+info.getName()+" is defined in the module "+info.getOrigin()+", but entity is defined in the application");
                }
            }
            if(entityModule == null)
            {
                warnings.add( "There are "+(entry.getValue().size())+" operations belonging to missing entity '"+entry.getKey()+"': "+operationNames );
            }
        }
    }

    private void checkFinally()
    {
        if(pageCustomizations != null)
        {
            for(List<PageCustomizationInfo> infos : pageCustomizations.values())
            {
                for(PageCustomizationInfo info : infos)
                {
                    warnings.add( "Unknown page customization: "+info.getKey() );
                }
            }
        }
        if(icons != null)
        {
            for(IconInfo icon : icons.values())
            {
                warnings.add( "Unknown icon: "+icon.getOwnerId()+"/"+icon.getName() );
            }
        }
    }

    private Map<Long, QueryInfo> indexQueries(Map<String, List<QueryInfo>> queries)
    {
        Map<Long, QueryInfo> id2query = new HashMap<>();
        for(List<QueryInfo> list: queries.values())
        {
            for(QueryInfo info : list)
            {
                id2query.put( info.getID(), info );
            }
        }
        return id2query;
    }

    private void setIcon( Icon icon )
    {
        IconInfo iconInfo = icons.remove( icon.getOwnerID() );
        if(iconInfo != null)
        {
            icon.setName( iconInfo.getName() );
            icon.setData( iconInfo.getData() );
            icon.setOriginModuleName( iconInfo.getOrigin() );
        }
    }

    private void addTableRefs(String moduleName, Module module) throws ExtendedSqlException, SQLException
    {
        Rdbms databaseSystem = module.getProject().getDatabaseSystem();
        DbmsTypeManager typeManager = databaseSystem == null ? new DefaultTypeManager() : databaseSystem.getTypeManager();
        ResultSet rs = sql.executeNamedQuery("selectTableRefs", moduleName);
        try
        {
            while(rs.next())
            {
                final String tableFrom = rs.getString(1);
                final String columnFrom = rs.getString(2);
                final String tableTo = rs.getString(3);
                final String columnTo = rs.getString(4);
                final Entity entity = module.getEntity( tableFrom );
                
                if ( entity == null )
                {
                    warnings.add( "There's no entity definition '" + tableFrom + "' that is used in reference." );
                    continue;
                }
                
                TableReference tableRef = null;
                final DdlElement tableDef = entity.getScheme();
                
                if ( tableDef != null )
                {
                    if(tableDef instanceof TableDef)
                    {
                        ColumnDef columnDef = ( ( TableDef ) tableDef ).findColumn( columnFrom );
                        if(columnDef != null)
                        {
                            SqlColumnType type = columnDef.getType();
                            if(type.getTypeName().equals( typeManager.getKeyType() ))
                                type.setTypeName( SqlColumnType.TYPE_KEY );
                        }
                        tableRef = columnDef;
                    }
                }
                else
                {
                    warnings.add( "There's no table definition '" + tableFrom + "' that is used in reference." );
                }
                
                if ( tableRef == null )
                {
                    TableRef tableRef0 = new TableRef(TableRef.nameFor( columnFrom, tableTo), columnFrom.toLowerCase(), entity.getOrCreateTableReferences());
                    tableRef = tableRef0;
                    DataElementUtils.saveQuiet(tableRef);
                }
                
                tableRef.setTableTo(tableTo);
                if ( columnTo != null )
                {
                    tableRef.setColumnsTo( columnTo.toLowerCase() );
                }
                
                tableRef.setViewName(rs.getString(5));
                Map<String, Set<String>> tableMap = genericRefEntities.get( tableFrom );
                if(tableMap != null)
                {
                    Set<String> permittedTables = tableMap.get( columnFrom );
                    if(permittedTables != null)
                    {
                        tableRef.setPermittedTables( permittedTables.toArray( new String[permittedTables.size()] ) );
                    }
                }
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
    }
    
    private void addTableDefs( Module module ) throws ExtendedSqlException, SQLException
    {
        Rdbms databaseSystem = module.getProject().getDatabaseSystem();
        DbmsTypeManager typeManager = databaseSystem == null ? new DefaultTypeManager() : databaseSystem.getTypeManager();
        boolean casePreserved = typeManager.normalizeIdentifierCase( "aA" ).equals( "aA" );
        for ( Entity entity : module.getEntities() )
        {
            final String table = entity.getName();
            if ( !"TABLE".equals( tableTypes.get( table.toLowerCase() ) ) )
                continue;
            List<SqlColumnInfo> columnInfos = columns.remove( table.toLowerCase() );
            if(columnInfos == null)
                continue;
            TableDef tableDef = new TableDef(entity);
            Map<String, String> changedColumnNames = new HashMap<>();
            for(SqlColumnInfo info : columnInfos)
            {
                info.withCache(strings);
                // "isDeleted___" column is maintained by BeanExplorer
                if(info.getName().equalsIgnoreCase( "isdeleted___" ))
                    continue;
                if ( info.getName().equalsIgnoreCase( entity.getPrimaryKey() ) )
                {
                    changedColumnNames.put( info.getName(), entity.getPrimaryKey() );
                    info.setName( entity.getPrimaryKey() );
                }
                else if ( ( mode & USE_HEURISTICS ) != 0 && !casePreserved )
                {
                    String guessedColumnName = columnNames.get( info.getName().toLowerCase(Locale.ENGLISH) );
                    if ( guessedColumnName == null && info.getName().toLowerCase().endsWith( "id" ) )
                    {
                        guessedColumnName = info.getName().substring( 0, info.getName().length() - 2 ) + "ID";
                    }
                    if ( guessedColumnName != null )
                    {
                        changedColumnNames.put( info.getName(), guessedColumnName );
                        info.setName( guessedColumnName );
                    }
                }
                ColumnDef column = new ColumnDef( info.getName(), tableDef.getColumns() );

                column.setType( createColumnType( info ) );
                typeManager.correctType( column.getType() );
                column.setPrimaryKey( info.getName().equalsIgnoreCase( entity.getPrimaryKey() ) );
                column.setCanBeNull( info.isCanBeNull() );
                String defaultValue = info.getDefaultValue();
                column.setAutoIncrement( info.isAutoIncrement() );
                if(!info.isAutoIncrement())
                {
                    column.setDefaultValue( defaultValue );
                }
                if(column.isPrimaryKey() && typeManager.getKeyType().equals( typeManager.getTypeClause( column.getType() ) ))
                {
                    column.getType().setTypeName( SqlColumnType.TYPE_KEY );
                }
                column.setOriginModuleName( module.getName() );
                DataElementUtils.saveQuiet( column );
            }
            List<IndexInfo> indexInfos = indices.get( table.toLowerCase(Locale.ENGLISH) );
            if(indexInfos != null)
            {
                INDEX: for ( IndexInfo info : indexInfos )
                {
                    if((mode & USE_HEURISTICS) != 0 && !casePreserved)
                    {
                        info.setName( info.getName().toUpperCase(Locale.ENGLISH) );
                    }
                    IndexDef index = new IndexDef( info.getName(), tableDef.getIndices() );
                    index.setUnique( info.isUnique() );
                    for(String indexCol : info.getColumns())
                    {
                        IndexColumnDef indexColumnDef = IndexColumnDef.createFromString( indexCol, index );
                        if(changedColumnNames.containsKey( indexColumnDef.getName() ))
                        {
                            indexColumnDef = indexColumnDef.clone( indexColumnDef.getOrigin(), changedColumnNames.get( indexColumnDef.getName() ) );
                        }
                        if(tableDef.getColumns().get( indexColumnDef.getName() ) == null)
                        {
                            if((mode & LOG_ERRORS) != 0)
                            {
                                warnings.add( "Unsupported functional index found: " + index.getName() + " (problem is here: "
                                    + indexCol + "); skipped" );
                            }
                            continue INDEX;
                        }
                        DataElementUtils.saveQuiet( indexColumnDef );
                    }
                    if(index.isUnique() && index.getSize() == 1)
                    {
                        IndexColumnDef indexColumnDef = index.iterator().next();
                        if(!indexColumnDef.isFunctional())
                        {
                            if(tableDef.getColumns().get( indexColumnDef.getName() ).isPrimaryKey()) // Do not store primary key index
                                continue;
                            if ( index.getName().equalsIgnoreCase( table + "_pkey" ) )
                            {
                                entity.setPrimaryKey( indexColumnDef.getName() );
                                continue;
                            }
                        }
                    }
                    DataElementUtils.saveQuiet( index );
                }
            }
            DataElementUtils.saveQuiet( tableDef );
        }

        if ( !sql.getConnector().isMySQL() )
            return;
        // For MySQL only now*/
        for ( Entity entity : module.getEntities() )
        {
            final String table = entity.getName();
            if ( !"VIEW".equals( tableTypes.get( table.toLowerCase() ) ) )
                continue;
            String createTable;
            ResultSet rs = sql.executeNamedQuery( "sql.getTableDefinition", table );
            try
            {
                if ( !rs.next() )
                    continue;
                createTable = rs.getString( 2 );
            }
            finally
            {
                sql.getConnector().close( rs );
            }
            int as = createTable.indexOf( " AS " );
            if ( as < 0 )
                continue;
            createTable = createTable.substring( as + " AS ".length() );
            ViewDef def = new ViewDef( entity );
            def.setDefinition( createTable );
            DataElementUtils.saveQuiet( def );
        }
    }
    
    private static SqlColumnType createColumnType( final SqlColumnInfo info )
    {
        SqlColumnType type = new SqlColumnType();
        String[] enumValues = info.getEnumValues();
        if ( enumValues != null )
        {
            if ( isBool( enumValues ) )
            {
                type.setTypeName( SqlColumnType.TYPE_BOOL );
            }
            else
            {
                type.setTypeName( SqlColumnType.TYPE_ENUM );
                Arrays.sort( enumValues );
                type.setEnumValues( enumValues );
            }
        }
        else
        {
            type.setTypeName( info.getType() );
            type.setSize( info.getSize() );
            type.setPrecision( info.getPrecision() );
        }
        return type;
    }

    protected static boolean isBool( final String[] enumValues )
    {
        if ( enumValues.length != 2 )
        {
            return false;
        }

        final String val0 = enumValues[0];
        final String val1 = enumValues[1];

        return isNoYes( val0, val1 ) || isNoYes( val1, val0 );
    }

    private static boolean isNoYes( final String val0, final String val1 )
    {
        return val0.equals( "no" ) && val1.equals( "yes" );
    }
    
    private Map<String, String> readAllJavaScriptForms() throws SQLException, ExtendedSqlException
    {
        Map<String, String> result = new HashMap<>();
        if((mode & SKIP_APPLICATION) != 0 || !sql.hasTable( "javascriptforms" ))
            return result;
        ResultSet rs = sql.executeNamedQuery( "selectJavaScriptForms" );
        try
        {
            while ( rs.next() )
            {
                final String formName = rs.getString( 1 );
                byte[] bytes = rs.getBytes( 2 );
                if(bytes == null)
                {
                    throw sql.getException( new IllegalStateException( "Unexpected empty data for JavaScript form " + formName ),
                            "selectJavaScriptForms" );
                }
                final String content = new String(bytes, StandardCharsets.UTF_8);
                result.put( formName, content );
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
        return result;
    }

    private Map<String, Map<String, Set<String>>> readAllGenericRefEntities() throws ExtendedSqlException, SQLException
    {
        Map<String, Map<String, Set<String>>> result = new HashMap<>();
        ResultSet rs = sql.executeNamedQuery( "selectGenericRefEntities" );
        try
        {
            while ( rs.next() )
            {
                String tableFrom = strings.get( rs.getString( 1 ) );
                String columnFrom = strings.get( rs.getString( 2 ) );
                String tableTo = strings.get( rs.getString( 3 ) );
                Map<String, Set<String>> tableMap = result.get( tableFrom );
                if ( tableMap == null )
                {
                    tableMap = new HashMap<>();
                    result.put( tableFrom, tableMap );
                }
                Set<String> permittedTables = tableMap.get( columnFrom );
                if ( permittedTables == null )
                {
                    permittedTables = new HashSet<>();
                    tableMap.put( columnFrom, permittedTables );
                }
                permittedTables.add( tableTo );
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
        return result;
    }

    private Map<String, IconInfo> readAllIcons() throws ExtendedSqlException
    {
        return sql.stream( rs -> {
                IconInfo info = new IconInfo();
                info.setOwnerId( rs.getString( 1 ) );
                info.setName( rs.getString( 2 ) );
                info.setMimeType( strings.get( rs.getString( 3 ) ) );
                info.setData( rs.getBytes( 4 ) );
                info.setOrigin( strings.get( rs.getString( 5 ) ) );
                return info;
            }, "selectIcons" )
            .remove( info -> skippedOrigin.equals( info.getOrigin() ) )
            .toMap( IconInfo::getOwnerId, Function.identity() );
    }
    
    private List<DaemonInfo> readAllDaemons() throws ExtendedSqlException
    {
        return sql.stream( rs -> {
                DaemonInfo daemon = new DaemonInfo();
                daemon.setID( rs.getLong( 1 ) );
                daemon.setName( rs.getString( 2 ) );
                daemon.setClassName( rs.getString( 3 ) );
                daemon.setConfigSection( rs.getString( 4 ) );
                daemon.setDaemonType( strings.get( rs.getString( 5 ) ) );
                daemon.setDescription( rs.getString( 6 ) );
                daemon.setSlaveNo( rs.getInt( 7 ) );
                daemon.setOrigin( strings.get( rs.getString( 8 ) ) );
                return daemon;
            }, "selectAllDaemons" )
            .remove( daemon -> skippedOrigin.equals( daemon.getOrigin() ) )
            .toList();
    }

    private List<StaticPageInfo> readAllStaticPages() throws ExtendedSqlException
    {
        if((mode & SKIP_APPLICATION) != 0)
            return Collections.emptyList();
        return sql.stream( rs -> {
            StaticPageInfo page = new StaticPageInfo();
            page.setId( rs.getLong( 1 ) );
            page.setLang( strings.get( rs.getString( 2 ) ) );
            page.setName( rs.getString( 3 ) );
            page.setContent( rs.getString( 4 ) );
            return page;
        }, "selectAllStaticPages" ).toList();
    }

    private Map<Long, List<QuickFilterInfo>> readAllQuickFilters() throws ExtendedSqlException
    {
        return sql.stream( rs -> {
                String origin = strings.get( rs.getString( 6 ) );
                QuickFilterInfo info = new QuickFilterInfo();
                info.setQueryID( rs.getLong( 1 ) );
                info.setFilter_param( rs.getString( 2 ) );
                info.setName( rs.getString( 3 ) );
                info.setFilterQueryName( rs.getString( 4 ) );
                info.setFilteringClass( rs.getString( 5 ) );
                info.setOrigin( origin );
                return info;
            }, "selectQuickFilters" )
            .remove( info -> skippedOrigin.equals( info.getOrigin() ) )
            .groupingBy( QuickFilterInfo::getQueryID );
    }

    private Map<Long, List<QuerySettingsInfo>> readAllQuerySettings() throws ExtendedSqlException
    {
        return sql.stream( rs -> {
                QuerySettingsInfo info = new QuerySettingsInfo();
                info.setQueryID( rs.getLong( 1 ) );
                info.setRole_name( strings.get( rs.getString( 2 ) ) );
                info.setMaxRecordsPerPage( rs.getInt( 3 ) );
                info.setMaxRecordsPerPrintPage( rs.getInt( 4 ) );
                info.setMaxRecordsInDynamicDropDown( rs.getInt( 5 ) );
                info.setColorSchemeID( rs.getLong( 6 ) );
                if(rs.wasNull())
                    info.setColorSchemeID( null );
                info.setAutoRefresh( rs.getInt( 7 ) );
                info.setBeautifier( strings.get( rs.getString( 8 ) ) );
                return info;
            }, "selectQuerySettings" )
            .groupingBy( QuerySettingsInfo::getQueryID );
    }

    private Map<Long, List<ExtenderInfo>> readAllOperationExtenders() throws ExtendedSqlException
    {
        return sql.stream( rs -> {
                String origin = strings.get( rs.getString( 3 ) );
                ExtenderInfo info = new ExtenderInfo();
                info.setOperID( rs.getLong( 1 ) );
                info.setRole( strings.get( rs.getString( 2 ) ) );
                info.setModule_name( origin );
                info.setClass_name( rs.getString( 4 ) );
                info.setJsCode( rs.getString( 5 ) );
                info.setInvokeOrder( rs.getInt( 6 ) );
                return info;
            }, "selectOperationExtensions" )
            .remove( info -> skippedOrigin.equals( info.getModule_name() ) )
            .groupingBy( ExtenderInfo::getOperID );
    }

    private List<EntityInfo> readAllEntities() throws ExtendedSqlException
    {
        return sql.stream( rs -> {
                final EntityInfo entityInfo = new EntityInfo();
                entityInfo.setName( rs.getString( 1 ) );
                entityInfo.setDisplayName( rs.getString( 2 ) );
                entityInfo.setPrimaryKeyColumn( strings.get( rs.getString( 3 ) ) );
                entityInfo.setType( rs.getString( 4 ) );
                entityInfo.setOrigin( strings.get( rs.getString( 5 ) ) );
                return entityInfo;
            }, "selectAllEntities" )
            .remove( info -> skippedOrigin.equals( info.getOrigin() ) )
            .toList();
    }
    
    private Map<String, List<QueryInfo>> readAllQueries() throws ExtendedSqlException, SQLException
    {
        final Map<String, List<QueryInfo>> queriesInfo = new HashMap<>();
        
        ResultSet rs = sql.executeNamedQuery( "selectAllQueries" );
        try
        {
            while ( rs.next() )
            {
                final String origin            = strings.get( rs.getString( 23 ) );
                final long id                  = rs.getLong( 1 );
                final String tableName         = rs.getString( 2 );
                final String name              = rs.getString( 3 );
                final String menuName          = rs.getString( 4 );
                final String titleName         = rs.getString( 5 );
                final String type              = strings.get( rs.getString( 6 ) );
                final String notSupported      = rs.getString( 7 );
                final String query             = rs.getString( 8 );
                final String hashQuery         = rs.getString( 9 );
                final String newDataCheckQuery = rs.getString( 10 );
                final String isInvisible       = rs.getString( 11 );
                final String isSecure          = rs.getString( 12 );
                final String isSlow            = rs.getString( 13 );
                final String isCacheable       = rs.getString( 14 );
                final String isReplicated      = rs.getString( 15 );
                Long contextID = rs.getLong( 16 );
                if (rs.wasNull()) { contextID = null; }
                Long paramOperID = rs.getLong( 17 );
                if (rs.wasNull()) { paramOperID = null; }
                Long categoryID = rs.getLong( 18 );
                if (rs.wasNull()) { categoryID = null; }
                Long mergeToID = rs.getLong( 19 );
                if (rs.wasNull()) { mergeToID = null; }
                final String shortDescription = rs.getString( 20 );
                final String messageWhenEmpty = rs.getString( 21 );
                final String wellKnownName    = rs.getString( 22 );
                
                final QueryInfo info = new QueryInfo();
                
                info.setID( id );
                info.setTable_name( tableName );
                info.setName( name );
                info.setMenuName( menuName );
                info.setTitleName( titleName );
                info.setType( type );
                info.setNotSupported( notSupported );
                info.setQuery( query );
                info.set___hashQuery( hashQuery );
                info.setNewDataCheckQuery( newDataCheckQuery );
                info.setIsInvisible( isInvisible );
                info.setIsSecure( isSecure );
                info.setIsSlow( isSlow );
                info.setIsCacheable( isCacheable );
                info.setIsReplicated( isReplicated );
                info.setContextID( contextID );
                info.setParamOperID( paramOperID );
                info.setCategoryID( categoryID );
                info.setContextID( contextID );
                info.setMergeToID( mergeToID );
                info.setShortDescription( shortDescription );
                info.setMessageWhenEmpty( messageWhenEmpty );
                info.setWellKnownName( wellKnownName );
                info.setOrigin( origin );

                List<QueryInfo> list = queriesInfo.get( tableName );
                if(list == null)
                {
                    list = new ArrayList<>();
                    queriesInfo.put( tableName, list );
                }
                list.add( info );
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
        
        return queriesInfo;
    }
    
    private Map<String, List<OperationInfo>> readAllOperations() throws ExtendedSqlException, SQLException
    {
        final Map<String, List<OperationInfo>> operationsInfo = new HashMap<>();
        
        ResultSet rs = sql.executeNamedQuery( "selectAllOperations" );
        try
        {
            while ( rs.next() )
            {
                final String origin = strings.get( rs.getString( 16 ) );
                final long id = rs.getLong( 1 );
                final String tableName = rs.getString( 2 );
                final String name = rs.getString( 3 );
                final String type = strings.get( rs.getString( 4 ) );
                final String notSupported = rs.getString( 5 );
                final String code = rs.getString( 6 );
                final String hashCode = rs.getString( 7 );
                final int requiredRecordSetSize = rs.getInt( 8 );
                final int executionPriority = rs.getInt( 9 );
                final String logging = rs.getString( 10 );
                final String isSecure = rs.getString( 11 );
                final String isConfirm = rs.getString( 12 );
                Long contextID = rs.getLong( 13 );
                if ( rs.wasNull() )
                {
                    contextID = null;
                }
                Long categoryID = rs.getLong( 14 );
                if ( rs.wasNull() )
                {
                    categoryID = null;
                }
                final String wellKnownName = rs.getString( 15 );
                
                final OperationInfo info = new OperationInfo();
                info.setID( id );
                info.setTable_name( tableName );
                info.setName( name );
                info.setType( type );
                info.setNotSupported( notSupported );
                info.setCode( code );
                info.set___hashCode( hashCode );
                info.setRequiredRecordSetSize( requiredRecordSetSize );
                info.setExecutionPriority( executionPriority );
                info.setLogging( logging );
                info.setIsSecure( isSecure );
                info.setIsConfirm( isConfirm );
                info.setContextID( contextID );
                info.setCategoryID( categoryID );
                info.setWellKnownName( wellKnownName );
                info.setOrigin( origin );
                
                List<OperationInfo> list = operationsInfo.get( tableName );
                if(list == null)
                {
                    list = new ArrayList<>();
                    operationsInfo.put( tableName, list );
                }
                list.add( info );
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
        
        return operationsInfo;
    }
    
    private Set<String> readAllRoles() throws ExtendedSqlException
    {
        return new HashSet<>(sql.readStringList( "selectAllRoles" ));
    }
    
    private Map<Long, List<QueryPerRoleInfo>> readAllQueryRoles() throws ExtendedSqlException, SQLException
    {
        final Map<Long, List<QueryPerRoleInfo>> rolesByQuery = new HashMap<>();
        
        ResultSet rs = sql.executeNamedQuery( "selectQueryRolePairs" );
        try
        {
            while ( rs.next() )
            {
                final long queryId = rs.getLong( 1 );
                QueryPerRoleInfo info = new QueryPerRoleInfo();
                info.setRole( strings.get( rs.getString( 2 ) ) );
                info.setDefault( Active.parseBoolean( rs.getString( 3 ) ) );
                
                List<QueryPerRoleInfo> queryRoles = rolesByQuery.get( queryId );
                
                if ( queryRoles == null )
                {
                    queryRoles = new ArrayList<>();
                    rolesByQuery.put( queryId, queryRoles );
                }
                
                queryRoles.add( info );
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
        
        return rolesByQuery;
    }

    private Map<Long, List<String>> readAllOperationRoles() throws ExtendedSqlException, SQLException
    {
        final Map<Long, List<String>> rolesByOperation = new HashMap<>();
        
        ResultSet rs = sql.executeNamedQuery( "selectOperationRolePairs" );
        try
        {
            while ( rs.next() )
            {
                final long operationId = rs.getLong( 1 );
                final String roleName = strings.get( rs.getString( 2 ) );
                
                List<String> operationRoles = rolesByOperation.get( operationId );
                
                if ( operationRoles == null )
                {
                    operationRoles = new ArrayList<>();
                    rolesByOperation.put( operationId, operationRoles );
                }
                
                operationRoles.add( roleName );
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
        
        return rolesByOperation;
    }
    
    private Map<Long, List<Long>> readAllQueryOperations() throws ExtendedSqlException, SQLException
    {
        final Map<Long, List<Long>> operationsByQuery = new HashMap<>();
        
        ResultSet rs = sql.executeNamedQuery( "selectOperationQueryPairs" );
        try
        {
            while ( rs.next() )
            {
                final long operationId = rs.getLong( 1 );
                final long queryId = rs.getLong( 2 );
                
                List<Long> queryOperations = operationsByQuery.get( queryId );
                
                if ( queryOperations == null )
                {
                    queryOperations = new ArrayList<>();
                    operationsByQuery.put( queryId, queryOperations );
                }
                
                queryOperations.add( operationId );
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
        
        return operationsByQuery;
    }
    
    private Map<String, List<LocalizationInfo>> readAllLocalizations() throws SQLException, ExtendedSqlException
    {
        final Map<String, List<LocalizationInfo>> localizations = new HashMap<>();
        LocalizationInfo info = null;
        ResultSet rs = sql.executeNamedQuery( "selectAllLocalizations" );
        try
        {
            while ( rs.next() )
            {
                final String origin = strings.get( rs.getString(1) );
                if(skippedOrigin.equals( origin ))
                    continue;
                final String langCode = strings.get( rs.getString( 2 ) );
                final String entity = rs.getString( 3 );
                final String messageKey = rs.getString( 4 );
                final String message = rs.getString( 5 );
                final String topic = strings.get( rs.getString( 6 ) );
                if ( info == null || !langCode.equals( info.getLangcode() ) || !entity.equals( info.getEntity() )
                    || !messageKey.equals( info.getMessagekey() ) || !message.equals( info.getMessage() ) )
                {
                    info = new LocalizationInfo();
                    info.setLangcode( langCode );
                    info.setEntity( entity );
                    info.setMessagekey( messageKey );
                    info.setMessage( message );
                    List<LocalizationInfo> originLocalizations = localizations.get( origin );
                    
                    if ( originLocalizations == null )
                    {
                        originLocalizations = new ArrayList<>();
                        localizations.put( origin, originLocalizations );
                    }
                    originLocalizations.add( info );
                }
                info.addTopic( topic );
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
        return localizations;
    }
    
    private Map<String, List<PageCustomizationInfo>> readAllCustomizations() throws SQLException, ExtendedSqlException
    {
        Map<String, List<PageCustomizationInfo>> customizations = new HashMap<>();
        ResultSet rs = sql.executeNamedQuery( "selectAllCustomizations" );
        try
        {
            while(rs.next())
            {
                String origin = strings.get( rs.getString( 1 ) );
                if(skippedOrigin.equals( origin ))
                    continue;
                PageCustomizationInfo info = new PageCustomizationInfo();
                info.setOrigin( origin );
                info.setKey( rs.getString( 2 ) );
                info.setType( strings.get( rs.getString( 3 ) ) );
                info.setRole( strings.get( rs.getString( 4 ) ) );
                info.setValue( rs.getString( 5 ) );
                
                int pos = info.getKey().lastIndexOf( '.' );
                String subKey = pos > 0 ? info.getKey().substring( 0, pos ) : info.getKey();
                
                List<PageCustomizationInfo> list = customizations.get( subKey );
                if ( list == null )
                {
                    list = new ArrayList<>();
                    customizations.put( subKey, list );
                }
                list.add( info );
            }
        }
        finally
        {
            sql.getConnector().close( rs );
        }
        return customizations;
    }
    
    private Entity createEntity( final String type, final EntityInfo entityInfo, final Module module )
    {
        final Entity entity;
        
        try
        {
            entity = EntitiesFactory.createBySqlName( type, entityInfo.getName(), module );
        }
        catch ( IllegalArgumentException e )
        {
            return null;
        }
        
        String displayName = entityInfo.getDisplayName();
        entity.setOrder( "" );
        if(displayName.startsWith( COMMENT_START ))
        {
            int pos = displayName.indexOf( COMMENT_END );
            if(pos > 0)
            {
                entity.setOrder( displayName.substring( COMMENT_START.length(), pos ) );
                displayName = displayName.substring( pos+COMMENT_END.length() );
            }
        }
        entity.setDisplayName( displayName );
        entity.setPrimaryKey( entityInfo.getPrimaryKeyColumn() );
        
        setIcon(entity.getIcon());
        
        createPageCustomizations( entity.getName(), entity );
        
        final List<OperationInfo> operationInfos = operationsByEntity( entityInfo.getName() );
        final BeModelCollection<Operation> entityOperations = entity.getOperations();
        
        Map<Long, Operation> id2operation = new HashMap<>();
        
        for ( final OperationInfo operationInfo : operationInfos )
        {
            // Skip Undelete operations as they are maintained by BE
            if(operationInfo.getName().equals( "Undelete" ))
                continue;
            final Operation operation = createOperation( entity, operationInfo );
            id2operation.put(operationInfo.getID(), operation);
            DataElementUtils.putQuiet( entityOperations, operation );
        }
        
        final List<QueryInfo> queryInfos = queriesByEntity( entityInfo.getName() );
        final BeModelCollection<Query> entityQueries = entity.getQueries();
        
        for ( final QueryInfo queryInfo : queryInfos )
        {
            // Skip lost records queries as they are maintained by BE
            if(queryInfo.getName().equals( Query.SPECIAL_LOST_RECORDS ))
                continue;
            // Table definition queries are handled specially: they added automatically to every table having tableDef 
            if(queryInfo.getName().equals( Query.SPECIAL_TABLE_DEFINITION ))
                continue;
            final Query query = createQuery( entity, queryInfo, id2operation );
            DataElementUtils.putQuiet( entityQueries, query );
        }
        
        return entity;
    }

    private Operation createOperation(Entity entity, OperationInfo operationInfo)
    {
        Operation operation = Operation.createOperation( operationInfo.getName(), operationInfo.getType(), entity );
        operation.setNotSupported( operationInfo.getNotSupported() );
        operation.setRecords( operationInfo.getRequiredRecordSetSize() );
        operation.setExecutionPriority( operationInfo.getExecutionPriority() );
        operation.setLogging( operationInfo.getLogging() );
        operation.setSecure( Active.parseBoolean( operationInfo.getIsSecure() ) );
        operation.setConfirm( Active.parseBoolean( operationInfo.getIsConfirm() ) );
        operation.setContextID( operationInfo.getContextID() );
        operation.setCategoryID( operationInfo.getCategoryID() );
        operation.setWellKnownName( operationInfo.getWellKnownName() );
        operation.getRoles().setValues( getRolesByOperation( operationInfo.getID() ) );
        if((mode & USE_HEURISTICS) != 0)
        {
            operation.getRoles().foldSystemGroup();
        }
        // we need to set origin in advance, because otherwise setCode will not work correctly
        operation.setOriginModuleName( operationInfo.getOrigin() );
        operation.setCode( operationInfo.getCode() == null ? "" : operationInfo.getCode() );
        setIcon( operation.getIcon() );
        for ( ExtenderInfo info : getExtendersByOperation( operationInfo.getID() ) )
        {
            createExtender( operation, info );
        }
        createPageCustomizations( entity.getName()+"."+operation.getName(), operation );
        // set origin module again, because changing values above resets origin to projectOrigin
        operation.setOriginModuleName( operationInfo.getOrigin() );
        return operation;
    }

    private void createExtender( Operation operation, ExtenderInfo info )
    {
        OperationExtender extender;
        if ( JavaScriptOperationExtender.JAVASCRIPT_EXTENDER_CLASS_NAME.equals( info.getClass_name() ) )
        {
            extender = new JavaScriptOperationExtender( operation, info.getModule_name() );
            ( ( JavaScriptOperationExtender ) extender ).setCode( info.getJsCode() );
        }
        else
        {
            extender = new OperationExtender( operation, info.getModule_name() );
            extender.setClassName( info.getClass_name() );
        }
        extender.setInvokeOrder( info.getInvokeOrder() );
        DataElementUtils.saveQuiet( extender );
    }
    
    private Query createQuery(Entity entity, QueryInfo queryInfo, Map<Long, Operation> id2operation)
    {
        Query query = new Query( queryInfo.getName(), entity );
        query.setMenuName( queryInfo.getMenuName() );
        query.setTitleName( queryInfo.getTitleName() );
        query.setType( queryInfo.getType() );
        query.setNotSupported( queryInfo.getNotSupported() );
        query.setQuery( FreemarkerUtils.escapeFreemarker( queryInfo.getQuery() ) );
        query.setNewDataCheckQuery( queryInfo.getNewDataCheckQuery() );
        query.setInvisible( Active.parseBoolean( queryInfo.getIsInvisible() ) );
        query.setSecure( Active.parseBoolean( queryInfo.getIsSecure() ) );
        query.setSlow( Active.parseBoolean( queryInfo.getIsSlow() ) );
        query.setCacheable( Active.parseBoolean( queryInfo.getIsCacheable() ) );
        query.setReplicated( Active.parseBoolean( queryInfo.getIsReplicated() ) );
        query.setContextID( queryInfo.getContextID() );
        query.setParametrizingOperation( id2operation.get( queryInfo.getParamOperID() ) );
        query.setCategoryID( queryInfo.getCategoryID() );
        query.setShortDescription( queryInfo.getShortDescription() );
        query.setMessageWhenEmpty( queryInfo.getMessageWhenEmpty() );
        query.setWellKnownName( queryInfo.getWellKnownName() );
        Set<String> roles = new HashSet<>();
        for ( QueryPerRoleInfo info : getRolesByQuery( queryInfo.getID() ) )
        {
            roles.add( info.getRole() );
            if ( info.isDefault() )
            {
                query.setDefaultView( true );
            }
        }
        query.getRoles().setValues( roles );
        if((mode & USE_HEURISTICS) != 0)
        {
            query.getRoles().foldSystemGroup();
        }
        setIcon( query.getIcon() );
        
        for(Long id : getOperationByQuery( queryInfo.getID() ))
        {
            Operation operation = id2operation.get( id );
            if(operation != null)
                query.addOperation( operation );
        }
        
        for(QuickFilterInfo info : getQuickFiltersByQuery( queryInfo.getID() ))
        {
            QuickFilter filter = new QuickFilter( info.getName(), query );
            filter.setFilteringClass( info.getFilteringClass() );
            filter.setQueryParam( info.getFilter_param() );
            filter.setTargetQueryName( info.getFilterQueryName() );
            filter.setOriginModuleName( info.getOrigin() );
            DataElementUtils.saveQuiet( filter );
        }
        
        List<QuerySettings> querySettings = new ArrayList<>();
        for(QuerySettingsInfo info : getQuerySettingsByQuery( queryInfo.getID() ))
        {
            QuerySettings settings = new QuerySettings(query);
            if(info.getRole_name() != null)
            {
                settings.getRoles().add( info.getRole_name() );
            }
            settings.setMaxRecordsPerPage( info.getMaxRecordsPerPage() );
            settings.setMaxRecordsPerPrintPage( info.getMaxRecordsPerPrintPage() );
            settings.setMaxRecordsInDynamicDropDown( info.getMaxRecordsInDynamicDropDown() );
            settings.setColorSchemeID( info.getColorSchemeID() );
            settings.setAutoRefresh( info.getAutoRefresh() );
            settings.setBeautifier( info.getBeautifier() );
            querySettings.add( settings );
        }
        if(!querySettings.isEmpty())
        {
            query.setQuerySettings( querySettings.toArray( new QuerySettings[querySettings.size()] ) );
        }
        if(queryInfo.getMergeToID() != null)
        {
            QueryInfo templateQuery = id2query.get( queryInfo.getMergeToID() );
            if(templateQuery != null)
            {
                query.setTemplateQueryName( templateQuery.getTable_name() + "." + templateQuery.getName() );
            }
        }
        createPageCustomizations( entity.getName()+"."+query.getName(), query );
        query.setOriginModuleName( queryInfo.getOrigin() );
        return query;
    }
    
    private void createPageCustomizations(String location, BeVectorCollection<?> parent)
    {
        for(String domain : PageCustomization.getDomains( parent ))
        {
            String prefix = '/' + domain + '.' + location; 
            List<PageCustomizationInfo> infos = pageCustomizations.remove( prefix );
            if(infos != null)
            {
                for(PageCustomizationInfo info : infos)
                {
                    createPageCustomization( parent, domain, prefix, info );
                }
            }
        }
    }

    private void createPageCustomization( BeVectorCollection<?> parent, String domain, String prefix, PageCustomizationInfo info )
    {
        BeVectorCollection<PageCustomization> origin = parent.getOrCreateCollection( PageCustomization.CUSTOMIZATIONS_COLLECTION, PageCustomization.class );
        PageCustomization customization = new PageCustomization( strings.get( info.getKey().substring( prefix.length() + 1 ) ),
                strings.get( domain ), origin );
        String value = FreemarkerUtils.escapeFreemarker( info.getValue() );
        if((mode & USE_HEURISTICS) != 0 && customization.getType().equals( PageCustomization.TYPE_JS ))
        {
            Matcher matcher = ON_LOAD_PATTERN.matcher( info.getValue() );
            if(matcher.find())
            {
                value = "<@_onLoad '"+matcher.group( 1 )+"'/>"+FreemarkerUtils.escapeFreemarker( info.getValue().substring( matcher.end() ) );
            }
        }
        customization.setCode( value );
        customization.setOriginModuleName( info.getOrigin() );
        String role = info.getRole();
        customization.setRoles( role == null || role.isEmpty() ? Collections.<String> emptyList() : Collections
                .singletonList( role ) );
        boolean merged = false;
        for(PageCustomization other : origin)
        {
            if(other.merge( customization ))
            {
                merged = true;
                break;
            }
        }
        if(!merged)
        {
            DataElementUtils.saveQuiet( customization );
        }
    }

    private List<String> getRolesByOperation( final long id )
    {
        final List<String> operationRoles = rolesByOperation.get( id );
        
        if ( operationRoles == null )
        {
            return Collections.emptyList();
        }
        
        return operationRoles;
    }
    
    private List<ExtenderInfo> getExtendersByOperation( final long id )
    {
        final List<ExtenderInfo> operationRoles = operationExtenders.get( id );
        
        if ( operationRoles == null )
        {
            return Collections.emptyList();
        }
        
        return operationRoles;
    }
    
    private List<QueryPerRoleInfo> getRolesByQuery( final long id )
    {
        final List<QueryPerRoleInfo> queryRoles = rolesByQuery.get( id );
        
        if ( queryRoles == null )
        {
            return Collections.emptyList();
        }
        
        return queryRoles;
    }
    
    private List<Long> getOperationByQuery( final long id )
    {
        final List<Long> queryOperations = operationsByQuery.get( id );
        if( queryOperations == null )
        {
            return Collections.emptyList();
        }
        return queryOperations;
    }
    
    private List<QuickFilterInfo> getQuickFiltersByQuery( final long id )
    {
        List<QuickFilterInfo> quickFilters = this.quickFilters.get( id );
        if ( quickFilters == null )
        {
            return Collections.emptyList();
        }
        return quickFilters;
    }

    private List<QuerySettingsInfo> getQuerySettingsByQuery( final long id )
    {
        List<QuerySettingsInfo> quickFilters = this.querySettings.get( id );
        if ( quickFilters == null )
        {
            return Collections.emptyList();
        }
        return quickFilters;
    }
}
