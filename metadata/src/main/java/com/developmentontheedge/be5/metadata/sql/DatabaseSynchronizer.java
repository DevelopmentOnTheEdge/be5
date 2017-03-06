package com.developmentontheedge.be5.metadata.sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.ExtendedSqlException;
import com.developmentontheedge.dbms.SqlExecutor;

import com.developmentontheedge.be5.metadata.freemarker.FreemarkerSqlHandler; 

import com.developmentontheedge.be5.metadata.exception.FreemarkerSqlException;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.DdlElement;
import com.developmentontheedge.be5.metadata.model.Entities;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityItem;
import com.developmentontheedge.be5.metadata.model.EntityLocalizations;
import com.developmentontheedge.be5.metadata.model.FreemarkerCatalog;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.Icon;
import com.developmentontheedge.be5.metadata.model.IndexColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexDef;
import com.developmentontheedge.be5.metadata.model.JavaScriptForm;
import com.developmentontheedge.be5.metadata.model.JavaScriptOperationExtender;
import com.developmentontheedge.be5.metadata.model.LanguageLocalizations;
import com.developmentontheedge.be5.metadata.model.LanguageStaticPages;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationExtender;
import com.developmentontheedge.be5.metadata.model.PageCustomization;
import com.developmentontheedge.be5.metadata.model.ParseResult;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.developmentontheedge.be5.metadata.model.QuickFilter;
import com.developmentontheedge.be5.metadata.model.StaticPage;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.metadata.model.EntityLocalizations.LocalizationRow;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.be5.metadata.model.base.BeVectorCollection;
import com.developmentontheedge.be5.metadata.sql.type.DbmsTypeManager;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.be5.metadata.util.Strings2;
import com.developmentontheedge.be5.metadata.util.WriterLogger;

/**
 * Class to synchronize metadata model with DB.
 */
public class DatabaseSynchronizer
{
    private static final Pattern CLONE_ID = Pattern.compile( "(\\d+)$" );
    private static final String[] REF_OPERATIONS = { "Insert", "Edit", "Clone", "Filter", "lnsert" };
    private final SqlExecutor sql;
    private final Project project;
    private final String projectOrigin;
    private final ProcessController log;

    public enum SyncMode
    {
        ALL, META, LOCALE, DDL, DDL_CLONES, SECURITY
    }

    private final IdCache queryIds = new IdCache();
    private final IdCache operationIds = new IdCache();
    private final List<Query> secondPassQueries = new ArrayList<>();
    private final List<TableReference> secondPassTableRefs = new ArrayList<>();
    private final List<ProjectElementException> warnings = new ArrayList<>();
    private final Rdbms rdbms;

    private static final String ORPHANS_MODULE_NAME = "beanexplorer_orphans";    
    
    /**
     * Initializes the new database synchronizer with the given database
     * connector.
     * 
     * @param connector
     *            Database connector.
     * @throws IOException
     */
    public DatabaseSynchronizer( DbmsConnector connector, Project project ) throws IOException
    {
        this( new WriterLogger(), new SqlExecutor(connector, DatabaseSynchronizer.class.getResource( "sql.properties" )), project );
    }

    public DatabaseSynchronizer( ProcessController logger, SqlExecutor sql, Project project )
    {
        this.log = logger;
        this.sql = sql;
        this.project = project;
        this.projectOrigin = project.getProjectOrigin();
        this.rdbms = Rdbms.getRdbms( sql.getConnector() );
    }

    /**
     * Takes all changes in the whole project and places it into database.
     * 
     * @param oldProject
     * 
     * @param project
     *            Project containing one or more scripts.
     * @throws ExtendedSqlException
     *             SQL execution error.
     */
    public void sync( SyncMode mode, Project oldProject ) throws ExtendedSqlException, ProjectElementException, FreemarkerSqlException
    {
        project.setDatabaseSystem( rdbms );
        if ( mode == SyncMode.DDL || mode == SyncMode.DDL_CLONES || mode == SyncMode.ALL )
        {
            sql.startSection( "Sync schema" );
            String ddlStatements = getDdlStatements( oldProject, mode == SyncMode.DDL_CLONES, false );
            if ( !ddlStatements.isEmpty() )
                log.setOperationName( "[>] Schema" );
            sql.executeMultiple( ddlStatements );
            sql.startSection( null );
        }
        oldProject.getModules().remove( ORPHANS_MODULE_NAME );
        if ( mode == SyncMode.SECURITY || mode == SyncMode.META || mode == SyncMode.ALL )
        {
            syncSecurity( project, oldProject );
        }
        if ( mode == SyncMode.META || mode == SyncMode.ALL )
        {
            cacheIds();
            executeScript( project.getApplication().getFreemarkerScripts().optScript( FreemarkerCatalog.PRE_META_STEP ) );
            List<String> oldModules = oldProject.getModules().names().toList();
            oldModules.removeAll( project.getModules().getNameList() );
            for ( String oldModuleName : oldModules )
            {
                syncModule( oldProject.getModules().get( oldModuleName ), new Module( oldModuleName, project.getModules() ) );
            }
            for ( Module module : project.getModules() )
            {
                Module oldModule = oldProject.getModules().get( module.getName() );
                if ( oldModule == null )
                    oldModule = new Module( module.getName(), oldProject.getModules() );
                syncModule( oldModule, module );
            }
            syncModule( oldProject.getApplication(), project.getApplication() );
            syncSecondPass();
            sql.comment( "Flush delayed inserts" );
            sql.flushDelayedInserts();
            if ( !project.isModuleProject() )
                syncSystemSettings();
            executeScript( project.getApplication().getFreemarkerScripts().optScript( FreemarkerCatalog.POST_META_STEP ) );
            FreemarkerCatalog systemScripts = project.getModule( ModuleUtils.SYSTEM_MODULE ).getFreemarkerScripts();
            if ( systemScripts != null )
                executeScript( systemScripts.optScript( FreemarkerCatalog.POST_META_APP_STEP ) );
        }
        if ( mode == SyncMode.LOCALE || mode == SyncMode.ALL )
        {
            executeScript( project.getApplication().getFreemarkerScripts().optScript( FreemarkerCatalog.PRE_LOCALE_STEP ) );
            Map<LocalizationEntry, LocalizationValue> oldLocalizations = new HashMap<>();
            cacheLocalizations( oldLocalizations, oldProject, project.getModulesAndApplication() );
            Map<LocalizationEntry, LocalizationValue> newLocalizations = new HashMap<>();
            cacheLocalizations( newLocalizations, project, project.getModulesAndApplication() );
            syncLocalization( newLocalizations, oldLocalizations );
            // syncLocalization( oldProject.getApplication().getLocalizations(),
            // project.getApplication().getLocalizations() );
            sql.comment( "Flush delayed inserts" );
            sql.flushDelayedInserts();
            executeScript( project.getApplication().getFreemarkerScripts().optScript( FreemarkerCatalog.POST_LOCALE_STEP ) );
        }
        
        clearAllCache( sql );
    }

    public static void clearAllCache(final SqlExecutor sql)
    {
        try
        {
            sql.startSection( "Clear all caches" );
            setSystemSetting( sql, "system", "CACHES_TO_CLEAR", "all" );
        }
        catch ( ExtendedSqlException e )
        {
            // ignore
        }
    }

    
    public String getDdlStatements( Project oldProject, boolean includeClones, boolean dangerousOnly ) throws ExtendedSqlException
    {
        Map<String, DdlElement> oldSchemes = new HashMap<>();
        Map<String, DdlElement> newSchemes = new HashMap<>();
        Set<String> allNames = new HashSet<>();
        for ( Module module : project.getModulesAndApplication() )
        {
            for ( Entity entity : module.getEntities() )
            {
                DdlElement scheme = entity.isAvailable() ? entity.getScheme() : null;
                if ( scheme != null )
                {
                    String normalizedName = entity.getName().toLowerCase();
                    newSchemes.put( normalizedName, scheme );
                    allNames.add( normalizedName );
                }
            }
        }
        for ( Module module : oldProject.getModulesAndApplication() )
        {
            for ( Entity entity : module.getEntities() )
            {
                DdlElement scheme = entity.isAvailable() ? entity.getScheme() : null;
                if ( scheme != null )
                {
                    String normalizedName = entity.getName().toLowerCase();
                    oldSchemes.put( normalizedName, scheme );
                    if ( !module.getName().equals( ORPHANS_MODULE_NAME ) )
                        allNames.add( normalizedName );
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for ( String entityName : allNames )
        {
            DdlElement oldScheme = oldSchemes.get( entityName );
            DdlElement scheme = newSchemes.get( entityName );

            if(scheme.withoutDbScheme())
            {
                if (scheme == null)
                {
                    sb.append(oldScheme.getDropDdl());
                    continue;
                }
                if (!dangerousOnly)
                {
                    warnings.addAll(scheme.getWarnings());
                }
                if (oldScheme == null)
                {
                    if (!dangerousOnly)
                    {
                        sb.append(scheme.getCreateDdl());
                    }
                    continue;
                }
                if (scheme.equals(oldScheme) || scheme.getDiffDdl(oldScheme, null).isEmpty())
                    continue;
                if (oldScheme.getModule().getName().equals(ORPHANS_MODULE_NAME) &&
                        oldScheme instanceof TableDef && scheme instanceof TableDef)
                    fixPrimaryKey((TableDef) oldScheme, (TableDef) scheme);
                sb.append(dangerousOnly ? scheme.getDangerousDiffStatements(oldScheme, sql) : scheme.getDiffDdl(oldScheme, sql));
            }
            else
            {
                System.out.println("Skip table with schema: " + scheme.getEntityName());
            }
        }
        
        Module orphans = oldProject.getModule( "beanexplorer_orphans"); // SqlModelReader.ORPHANS_MODULE_NAME
        if ( includeClones && orphans != null )
        {
            for ( Entity entity : orphans.getEntities() )
            {
                TableDef cloneDdl = entity.findTableDefinition();
                if(cloneDdl.withoutDbScheme())
                {
                    TableDef ddl = (TableDef) getDdlForClone(newSchemes, entity.getName());
                    if (ddl != null && cloneDdl != null)
                    {
                        String cloneId = entity.getName().substring(ddl.getEntityName().length());
                        Entity curEntity = ddl.getEntity();
                        Entity renamedEntity = curEntity.clone(curEntity.getOrigin(), entity.getName(), false);
                        ddl = renamedEntity.findTableDefinition();
                        syncCloneDdl(cloneDdl, ddl, cloneId);
                        if (!ddl.equals(cloneDdl) && !ddl.getDiffDdl(cloneDdl, null).isEmpty())
                        {
                            sb.append(dangerousOnly ? ddl.getDangerousDiffStatements(cloneDdl, sql) : ddl.getDiffDdl(cloneDdl, sql));
                        }
                    }
                }
                else
                {
                    System.out.println("Skip table with schema: " + cloneDdl.getEntityName());
                }
            }
        }
        return sb.toString();
    }

    // Fix known changes in cloned table and in normal table
    private void syncCloneDdl( TableDef cloneDdl, TableDef mainDdl, String cloneId )
    {
        // Copy special columns
        List<String> specialColumns = Arrays.asList( "transportstatus", "linkrule", "linkstatus", "origid" );
        for(String colName : specialColumns)
        {
            ColumnDef cloneCol = cloneDdl.getColumns().getCaseInsensitive( colName );
            ColumnDef mainCol = mainDdl.getColumns().getCaseInsensitive( colName );
            if(cloneCol != null && mainCol == null)
            {
                DataElementUtils.save( cloneCol.clone( mainDdl.getColumns(), cloneCol.getName() ) );
            }
        }
        
        // Map indexes as clone indexes may have different names
        Function<? super IndexDef, ? extends List<String>> classifier = indexDef -> indexDef.stream().map( IndexColumnDef::getDefinition )
                .toList();
        Map<List<String>, Deque<IndexDef>> ddlMap = cloneDdl.getIndices().stream().groupingTo( classifier, ArrayDeque::new );
        for ( IndexDef indexDef : mainDdl.getIndices().stream().toList() )
        {
            List<String> key = classifier.apply( indexDef );
            Deque<IndexDef> list = ddlMap.get( key );
            IndexDef oldIdx = list == null ? null : list.poll();
            String newName = oldIdx == null ? indexDef.getName() + cloneId : oldIdx.getName();
            mainDdl.renameIndex( indexDef.getName(), newName );
        }
        // Copy indexes for special columns
        ddlMap.values().stream().flatMap( Deque::stream )
                .filter( idx -> specialColumns.stream().anyMatch( col -> idx.getCaseInsensitive( col ) != null ) )
                .map( idx -> idx.clone( mainDdl.getIndices(), idx.getName() ) )
                .forEach( DataElementUtils::save );
        fixPrimaryKey( cloneDdl, mainDdl );
    }

    private void fixPrimaryKey( TableDef orphanDdl, TableDef ddl )
    {
        ColumnDef pk = ddl.getColumns().get( ddl.getEntity().getPrimaryKey() );
        // Orphans have no primary key set: try to set the same column as in original table
        if(pk != null)
        {
            ColumnDef orphanPk = orphanDdl.getColumns().getCaseInsensitive( pk.getName() );
            if(orphanPk != null)
            {
                orphanDdl.getIndicesUsingColumn( orphanPk.getName() ).stream().filter( idx -> idx.getSize() == 1 && idx.isUnique() )
                    .findFirst().ifPresent( idx -> {
                        // Remove primary key index
                        DataElementUtils.remove( idx );
                        orphanDdl.getEntity().setPrimaryKey( orphanPk.getName() );
                        orphanPk.setPrimaryKey( true );
                    });
            }
        }
    }

    private static DdlElement getDdlForClone( Map<String, DdlElement> schemes, String cloneName )
    {
        String name = cloneName.toLowerCase();
        Matcher matcher = CLONE_ID.matcher( name );
        if ( !matcher.find() )
            return null;
        String cloneId = matcher.group();
        return IntStreamEx.range( name.length() - cloneId.length(), name.length() )
                .mapToObj( len -> schemes.get( name.substring( 0, len ) ) ).nonNull().findFirst().orElse( null );
    }

    private void syncSystemSettings() throws ExtendedSqlException
    {
        sql.startSection( "System settings" );
        List<String> moduleNames = new ArrayList<>( project.getModules().getNameList() );
        moduleNames.remove( ModuleUtils.SYSTEM_MODULE );
        setSystemSetting( sql, "system", "MODULES", String.join( ",", moduleNames ) );
        setSystemSetting( sql, "system", "FEATURES", String.join( ",", project.getFeatures() ) );
    }

    public static void setSystemSetting(final SqlExecutor sql, final String category, final String name, final String value) throws ExtendedSqlException
    {
        sql.exec( "sql.delete.system.setting", category, name );
        sql.exec( "sql.insert.system.setting", category, name, value );
    }
    
    public List<ProjectElementException> getWarnings()
    {
        return warnings;
    }

    public Project getProject()
    {
        return project;
    }

    public void executeScript( FreemarkerScript script ) throws ProjectElementException, FreemarkerSqlException
    {
        if ( script == null || script.getSource().trim().isEmpty() )
            return;
        try
        {
            new FreemarkerSqlHandler(sql, false, log).execute(script);
        }
        catch ( ProjectElementException | FreemarkerSqlException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new ProjectElementException( script, e );
        }
        
    }

    private void syncSecondPass() throws ExtendedSqlException
    {
        if ( !secondPassQueries.isEmpty() || !secondPassTableRefs.isEmpty() )
        {
            log.setOperationName( "[>] Second pass" );
            sql.startSection( "Second pass started" );
            for ( Query query : secondPassQueries )
            {
                long id = queryIds.get( query.getEntity().getName(), query.getName() );
                syncQuickFilters( id, query.getQuickFilters() );

                String templateQueryName = query.getTemplateQueryName();
                if ( !templateQueryName.isEmpty() )
                {
                    int pos = templateQueryName.indexOf( '.' );
                    if ( pos > 0 )
                    {
                        String templateEntity = templateQueryName.substring( 0, pos );
                        String templateQuery = templateQueryName.substring( pos + 1 );
                        long mergeToId = queryIds.get( templateEntity, templateQuery );
                        if ( mergeToId != 0 )
                        {
                            sql.exec( "sql.query.mergeToId", mergeToId, id );
                        }
                    }
                }
            }
            for ( TableReference tableRef : secondPassTableRefs )
            {
                long queryId = queryIds.get( tableRef.getTableTo(), tableRef.getViewName() );
                if ( queryId == 0 )
                {
                    warnings.add( new ProjectElementException( tableRef, "Unable to find query '" + tableRef.getViewName()
                        + "' in the entity '" + tableRef.getTableTo() + "'" ) );
                }
                String queryIdStr = queryId == 0 ? null : String.valueOf( queryId );
                sql.execDelayedInsert( "sql.insert.tableref", tableRef.getTableFrom(),
                        rdbms.getTypeManager().normalizeIdentifierCase( tableRef.getColumnsFrom() ), tableRef.getTableTo(), rdbms
                                .getTypeManager().normalizeIdentifierCase( tableRef.getColumnsTo() ), queryIdStr );
            }
        }
    }

    private void syncSecurity( Project project, Project oldProject ) throws ExtendedSqlException
    {
        Set<String> availableRoles = project.getAvailableRoles();
        if ( availableRoles.equals( oldProject.getAvailableRoles() ) )
            return;
        sql.startSection( "Synchronize roles" );
        sql.exec( "sql.delete.roles" );
        log.setOperationName( "[U] " + project.getSecurityCollection().getRoleCollection().getCompletePath() );
        for ( String role : availableRoles )
        {
            sql.execDelayedInsert( "sql.insert.role", role );
        }
        sql.flushDelayedInserts();
    }

    private void syncModule( Module oldModule, Module module ) throws ExtendedSqlException, ProjectElementException
    {
        syncEntities( oldModule, module );
        syncDaemons( oldModule.getDaemonCollection(), module.getDaemonCollection() );
        if ( projectOrigin.equals( module.getName() ) )
        {
            sql.startSection( "Synchronize page customizations" );
            syncPageCustomizations( module, oldModule );
            syncStaticPages( oldModule.getStaticPageCollection(), module.getStaticPageCollection() );
            syncJavaScriptForms( oldModule.getCollection( Module.JS_FORMS, JavaScriptForm.class ),
                    module.getCollection( Module.JS_FORMS, JavaScriptForm.class ) );
        }
    }

    private void syncEntities( Module oldModule, Module module ) throws ExtendedSqlException, ProjectElementException
    {
        BeModelCollection<Entity> oldCollection = oldModule.getEntityCollection();
        BeModelCollection<Entity> collection = module.getEntityCollection();
        if ( oldCollection == null && collection == null )
        {
            return;
        }
        if ( oldCollection == null )
        {
            oldCollection = new Entities( oldModule );
        }
        if ( collection == null )
        {
            collection = new Entities( module );
        }
        syncEntityCollection( oldCollection, collection );
    }

    private void syncStaticPages( BeModelCollection<LanguageStaticPages> oldStaticPages, BeModelCollection<LanguageStaticPages> staticPages ) throws ExtendedSqlException, ProjectElementException
    {
        long maxId = 0;
        for ( LanguageStaticPages langPages : oldStaticPages )
        {
            for ( StaticPage page : langPages )
            {
                if ( page.getId() > maxId )
                {
                    maxId = page.getId();
                }
            }
        }
        Set<String> oldLangs = oldStaticPages.names().toSet();
        List<String> newLangs = staticPages.getNameList();
        boolean clean = oldLangs.isEmpty() && !newLangs.isEmpty();
        if ( clean )
        {
            log.setOperationName( "[A] " + staticPages.getCompletePath() );
        }
        oldLangs.addAll( newLangs );
        for ( String lang : oldLangs )
        {
            LanguageStaticPages oldLangPages = oldStaticPages.get( lang );
            if ( oldLangPages == null )
                oldLangPages = new LanguageStaticPages( lang, null );
            LanguageStaticPages langPages = staticPages.get( lang );
            if ( langPages == null )
                langPages = new LanguageStaticPages( lang, null );
            maxId = syncLanguageStaticPages( oldLangPages, langPages, clean, maxId );
        }
        for ( LanguageStaticPages langPages : staticPages )
        {
            for ( StaticPage page : langPages )
            {
                syncPageCustomizations( page, null );
            }
        }
    }

    private long syncLanguageStaticPages( LanguageStaticPages oldCollection, LanguageStaticPages collection, boolean parentClean, long maxId ) throws ExtendedSqlException
    {
        long curId = maxId;
        Set<String> oldNames = oldCollection.names().toSet();
        List<String> newNames = collection.getNameList();
        if ( oldNames.isEmpty() && newNames.isEmpty() )
        {
            return curId;
        }
        sql.startSection( "Synchronize static pages" );
        boolean clean = oldNames.isEmpty() && !newNames.isEmpty();
        if ( clean && !parentClean )
        {
            log.setOperationName( "[A] " + collection.getCompletePath() );
        }
        oldNames.removeAll( newNames );
        for ( String oldName : oldNames )
        {
            log.setOperationName( "[D] " + collection.getCompletePath().getChildPath( oldName ) );
            sql.exec( "sql.delete.staticPage", collection.getName(), oldName );
        }
        for ( StaticPage page : collection )
        {
            StaticPage other = oldCollection.get( page.getName() );
            if ( !page.equals( other ) )
            {
                if ( !clean && !parentClean )
                {
                    log.setOperationName( "[" + ( other == null ? 'A' : 'U' ) + "] " + page.getCompletePath() );
                }
                if ( other != null )
                {
                    sql.exec( "sql.update.staticPage", page.getContent(), collection.getName(), page.getName() );
                }
                else
                {
                    sql.execDelayedInsert( "sql.insert.staticPage", ++curId, page.getContent(), collection.getName(), page.getName() );
                }
            }
        }
        return curId;
    }

    private void syncLocalization(
        Map<LocalizationEntry, LocalizationValue> newLocalizations,
        Map<LocalizationEntry, LocalizationValue> oldLocalizations ) throws ExtendedSqlException
    {
        Set<LocalizationEntry> toRemove = new HashSet<>( oldLocalizations.keySet() );
        toRemove.removeAll( newLocalizations.keySet() );
        Map<LocalizationEntry, LocalizationValue> toAdd = new HashMap<>( newLocalizations );
        for ( Entry<LocalizationEntry, LocalizationValue> oldEntry : oldLocalizations.entrySet() )
        {
            LocalizationValue newValue = toAdd.get( oldEntry.getKey() );
            if ( newValue == null )
                continue;
            if ( newValue.value.equals( oldEntry.getValue().value ) && newValue.origin.equals( oldEntry.getValue().origin ) )
                toAdd.remove( oldEntry.getKey() );
            else
                toRemove.add( oldEntry.getKey() );
        }
        if ( toAdd.isEmpty() && toRemove.isEmpty() )
            return;
        log.setOperationName( "[>] Localizations" );
        for ( LocalizationEntry entry : toRemove )
        {
            sql.exec( "sql.delete.lang.row", entry.lang, entry.entity, entry.topic, entry.key );
        }
        for ( Entry<LocalizationEntry, LocalizationValue> entry : toAdd.entrySet() )
        {
            sql.execDelayedInsert( "sql.insert.localization", entry.getValue().origin, entry.getKey().lang, entry.getKey().entity,
                    entry.getKey().topic, entry.getKey().key, entry.getValue().value );
        }
    }

    private void syncDaemons( BeModelCollection<Daemon> oldCollection, BeModelCollection<Daemon> collection ) throws ExtendedSqlException
    {
        Set<String> oldNames = new HashSet<>( oldCollection.getAvailableNames() );
        List<String> newNames = collection.getAvailableNames();
        if ( oldNames.isEmpty() && newNames.isEmpty() )
        {
            return;
        }
        sql.startSection( "Synchronize daemons" );
        boolean clean = oldNames.isEmpty() && !newNames.isEmpty();
        if ( clean )
        {
            log.setOperationName( "[A] " + collection.getCompletePath() );
        }
        oldNames.removeAll( newNames );
        for ( String daemonName : oldNames )
        {
            log.setOperationName( "[D] " + collection.getCompletePath().getChildPath( daemonName ) );
            sql.exec( "sql.delete.daemon", daemonName );
        }
        for ( Daemon daemon : collection.getAvailableElements() )
        {
            Daemon other = oldCollection.get( daemon.getName() );
            if ( !daemon.equals( other ) )
            {
                if ( !clean )
                {
                    log.setOperationName( "[" + ( other == null ? 'A' : 'U' ) + "] " + daemon.getCompletePath() );
                }
                if ( other != null )
                {
                    sql.exec( "sql.delete.daemon", other.getName() );
                }
                sql.execDelayedInsert( "sql.insert.daemon", daemon.getName(), daemon.getClassName(), daemon.getConfigSection(),
                        daemon.getDaemonType(), daemon.getDescription(), daemon.getSlaveNo(), daemon.getModule().getName() );
            }
        }
    }

    private void syncJavaScriptForms( BeModelCollection<JavaScriptForm> oldCollection, BeModelCollection<JavaScriptForm> collection ) throws ExtendedSqlException
    {
        Set<String> oldNames = oldCollection.names().toSet();
        List<String> newNames = collection.getNameList();
        if ( oldNames.isEmpty() && newNames.isEmpty() )
        {
            return;
        }
        sql.startSection( "Synchronize JavaScript forms" );
        boolean clean = oldNames.isEmpty() && !newNames.isEmpty();
        if ( clean )
        {
            log.setOperationName( "[A] " + collection.getCompletePath() );
        }
        oldNames.removeAll( newNames );
        for ( String formName : oldNames )
        {
            log.setOperationName( "[D] " + collection.getCompletePath().getChildPath( formName ) );
            sql.exec( "sql.delete.jsform", formName );
        }
        for ( JavaScriptForm form : collection )
        {
            JavaScriptForm other = oldCollection.get( form.getName() );
            if ( !form.equals( other ) )
            {
                if ( !clean )
                {
                    log.setOperationName( "[" + ( other == null ? 'A' : 'U' ) + "] " + form.getCompletePath() );
                }
                if ( other != null )
                {
                    sql.exec( "sql.delete.jsform", other.getName() );
                }
                sql.execInsert( "sql.insert.jsform", form.getName(), form.getSource().getBytes( StandardCharsets.UTF_8 ) );
            }
        }
    }

    private void syncTableReferences( Collection<TableReference> collection, Collection<TableReference> oldCollection ) throws ExtendedSqlException
    {
        Map<String, TableReference> oldNames = new HashMap<>();
        for ( TableReference oldTableReference : oldCollection )
        {
            oldNames.put( oldTableReference.getColumnsFrom().toLowerCase(), oldTableReference );
        }
        Map<String, TableReference> newNames = new HashMap<>();
        for ( TableReference tableRef : collection )
        {
            newNames.put( tableRef.getColumnsFrom().toLowerCase(), null );
        }
        DbmsTypeManager typeManager = rdbms.getTypeManager();
        if ( oldNames.isEmpty() && newNames.isEmpty() )
        {
            return;
        }
        for ( Entry<String, TableReference> entry : newNames.entrySet() )
        {
            entry.setValue( oldNames.remove( entry.getKey() ) );
        }
        for ( TableReference tableRef : oldNames.values() )
        {
            sql.exec( "sql.delete.tableref", tableRef.getTableFrom(), typeManager.normalizeIdentifierCase( tableRef.getColumnsFrom() ) );
            if ( tableRef.getPermittedTables() != null && tableRef.getPermittedTables().length > 0 )
            {
                sql.exec( "sql.delete.genericref", tableRef.getTableFrom(), typeManager.normalizeIdentifierCase( tableRef.getColumnsFrom() ) );
            }
        }
        for ( TableReference tableRef : collection )
        {
            TableReference other = newNames.get( tableRef.getColumnsFrom().toLowerCase() );
            if ( other == null || !tableRef.equalsReference( other ) )
            {
                if ( other != null )
                {
                    sql.exec( "sql.delete.tableref", tableRef.getTableFrom(), typeManager.normalizeIdentifierCase( tableRef.getColumnsFrom() ) );
                    if ( other.getPermittedTables() != null && other.getPermittedTables().length > 0 )
                    {
                        sql.exec( "sql.delete.genericref", other.getTableFrom(), typeManager.normalizeIdentifierCase( other.getColumnsFrom() ) );
                    }
                }
                long queryId = queryIds.get( tableRef.getTableTo(), tableRef.getViewName() );
                if ( tableRef.getTableTo() != null && tableRef.getViewName() != null && queryId == 0 )
                {
                    secondPassTableRefs.add( tableRef );
                    continue;
                }
                String queryIdStr = queryId == 0 ? null : String.valueOf( queryId );
                sql.execDelayedInsert( "sql.insert.tableref", tableRef.getTableFrom(),
                        typeManager.normalizeIdentifierCase( tableRef.getColumnsFrom() ), tableRef.getTableTo(),
                        typeManager.normalizeIdentifierCase( tableRef.getColumnsTo() ), queryIdStr );
                if ( tableRef.getPermittedTables() != null )
                {
                    for ( String permittedTable : tableRef.getPermittedTables() )
                    {
                        sql.execDelayedInsert( "sql.insert.genericref", tableRef.getTableFrom(),
                                typeManager.normalizeIdentifierCase( tableRef.getColumnsFrom() ), permittedTable );
                    }
                }
            }
        }
    }

    private void syncEntityCollection( BeModelCollection<? extends Entity> oldCollection, BeModelCollection<? extends Entity> collection ) throws ExtendedSqlException, ProjectElementException
    {
        Set<String> oldNames = new HashSet<>( oldCollection.getAvailableNames() );
        List<String> newNames = collection.getAvailableNames();
        boolean clean = oldNames.isEmpty() && !newNames.isEmpty();
        if ( clean )
        {
            log.setOperationName( "[A] " + collection.getCompletePath() );
        }
        oldNames.removeAll( newNames );
        for ( String entityName : oldNames )
        {
            log.setOperationName( "[D] " + collection.getCompletePath().getChildPath( entityName ) );
            final Entity entity = oldCollection.get( entityName );
            clearEntityInfo( null, entity );
            syncTableReferences( Collections.<TableReference> emptyList(), entity.getAllReferences() );
        }
        for ( Entity entity : collection.getAvailableElements() )
        {
            Entity other = oldCollection.get( entity.getName() );
            if ( !entity.equals( other, false ) )
            {
                if ( !clean )
                {
                    log.setOperationName( "[" + ( other == null ? 'A' : 'U' ) + "] " + entity.getCompletePath() );
                }
                syncEntity( entity, other );
            }
            syncTableDefinitionQuery( entity );
        }
    }

    private void syncTableDefinitionQuery( Entity e ) throws ExtendedSqlException, ProjectElementException
    {
        long id = queryIds.get( e.getName(), "Table definition" );
        boolean hasDefinition = e.findTableDefinition() != null;
        if ( hasDefinition && id == 0 )
        {
            String code = rdbms == Rdbms.MYSQL ? "desc " + e.getName() : " ";
            Query tableDefQuery = new Query( "Table definition", e )
            {
                @Override
                public ParseResult getQueryCompiled()
                {
                    return new ParseResult( code );
                }
            };
            syncQuery( tableDefQuery, null );
        }
        else if ( !hasDefinition && id != 0 )
        {
            if ( e.getModule().getName().equals( projectOrigin ) || ModuleUtils.isModuleExist( e.getModule().getName() ) )
                removeQueries( Collections.singleton( id ) );
        }
    }

    /**
     * Synchronizes the the given entity model object with database.
     * 
     * @param entity
     *            Model object containing entity info.
     * @param moduleName
     * 
     * @throws ExtendedSqlException
     *             SQL execution error.
     * @throws ProjectElementException
     */
    private void syncEntity( Entity entity, Entity oldEntity ) throws ExtendedSqlException, ProjectElementException
    {
        clearEntityInfo( entity, oldEntity );
        sql.startSection( "Synchronizing entity " + entity.getName() );
        String sqlDisplayName = getSqlDisplayName( entity );
        sql.exec( "sql.insert.entity", entity.getName(), sqlDisplayName, entity.getPrimaryKey(), entity.getType().getSqlName(), entity
                .getModule().getName() );
        syncTableReferences( entity.getAllReferences(),
                oldEntity == null ? Collections.<TableReference> emptyList() : oldEntity.getAllReferences() );
        for ( Operation op : entity.getOperations().getAvailableElements() )
        {
            Operation oldOp = oldEntity == null ? null : oldEntity.getOperations().get( op.getName() );
            if ( !op.equals( oldOp ) )
                syncOperation( op, oldOp );
        }
        // Order is important: queries must be synchronized after operations
        for ( Query q : entity.getQueries().getAvailableElements() )
        {
            Query oldQ = oldEntity == null ? null : oldEntity.getQueries().get( q.getName() );
            if ( !q.equals( oldQ ) )
                syncQuery( q, oldQ );
        }
        syncPageCustomizations( entity, oldEntity );
        insertIcon( entity.getIcon() );
    }

    public static String getSqlDisplayName( Entity entity )
    {
        return getSqlDisplayName( entity.getOrder(), entity.getDisplayName() );
    }

    private static String getSqlDisplayName( String order, String displayName )
    {
        return ( order == null || order.isEmpty() ? "" : "<!--" + order + "-->" ) + ( displayName == null ? "" : displayName );
    }

    private void insertIcon( Icon icon ) throws ExtendedSqlException
    {
        sql.exec( "sql.delete.icon", icon.getOwnerID() );
        if ( icon.getData() == null )
            return;
        sql.execInsert( "sql.insert.icon", icon.getOwnerID(), icon.getName(), icon.getMimeType(), icon.getData(), icon.getOriginModuleName() );
    }

    private void syncOperation( Operation o, Operation oldOp ) throws ExtendedSqlException, ProjectElementException
    {
        Entity e = o.getEntity();
        String code = o.getCode();
        if ( code.isEmpty() )
        {
            if ( o.getModule().getName().equals( projectOrigin ) )
                warnings.add( new ProjectElementException( o.getCompletePath(), "code", "Operation code is empty." ) );
            else
                warnings.add( new ProjectElementException(
                        o.getCompletePath(),
                        "code",
                        "Operation code is empty: probably you tried to customize module operation, but mistyped operation name or module metadata wasn't properly added to the database." ) );
        }
        sql.startSection( "Synchronizing operation " + e.getName() + "." + o.getName() );
        long oldId = operationIds.get( e.getName(), o.getName() );

        final Object[] args = { e.getName(), o.getName(), o.getType(), code, o.getRecords(), o.isSecure(), o.isConfirm(), o.getLogging(),
            o.getNotSupported(), o.getOriginModuleName(), oldId };
        final long id;
        if ( oldId == 0 )
        {
            id = Long.parseLong( sql.execInsert( "sql.insert.operation", args ) );
            operationIds.add( e.getName(), o.getName(), id );
        }
        else
        {
            sql.exec( "sql.enable.insert.identity", "operations" );
            sql.exec( "sql.insert.operation.id", args );
            sql.exec( "sql.disable.insert.identity", "operations" );
            id = oldId;
        }
        sql.comment( "Operation ID = " + id, false );
        syncOperationPerRole( o, id );
        syncExtenders( o );
        syncPageCustomizations( o, oldOp );
        insertIcon( o.getIcon() );
    }

    private void syncOperationPerRole( Operation o, long id ) throws ExtendedSqlException
    {
        for ( String role : o.getRoles().getFinalRoles() )
        {
            sql.execDelayedInsert( "sql.operation.per.role", id, role );
        }
    }

    private void syncExtenders( Operation o ) throws ExtendedSqlException
    {
        BeModelCollection<OperationExtender> extenders = o.getExtenders();
        if ( extenders != null )
        {
            Entity e = o.getEntity();
            for ( OperationExtender extender : extenders )
            {
                if ( extender instanceof JavaScriptOperationExtender )
                {
                    sql.execDelayedInsert( "sql.insert.operation.extender", e.getName(), o.getName(), extender.getClassName(),
                            ( ( JavaScriptOperationExtender ) extender ).getCode(), extender.getInvokeOrder(), extender.getOriginModuleName() );
                }
                else
                {
                    sql.execDelayedInsert( "sql.insert.operation.extender", e.getName(), o.getName(), extender.getClassName(), null,
                            extender.getInvokeOrder(), extender.getOriginModuleName() );
                }
            }
        }
    }

    private void syncQuery( Query q, Query oldQ ) throws ExtendedSqlException, ProjectElementException
    {
        Entity e = q.getEntity();
        sql.startSection( "Synchronizing query " + e.getName() + "." + q.getName() );
        String code = q.getQueryCompiled().validate();
        Long paramId = null;
        String paramOperName = q.getParametrizingOperationName();
        if ( !paramOperName.isEmpty() )
        {
            paramId = operationIds.get( e.getName(), paramOperName );
            if ( paramId == 0 )
            {
                warnings.add( new ProjectElementException( q.getCompletePath(), "parametrizingOperationName", "Operation not found: "
                    + paramOperName ) );
            }
        }
        long oldId = queryIds.get( e.getName(), q.getName() );
        Object[] args = new Object[] { e.getName(), q.getName(), q.getType(), code, q.isInvisible(), q.isSecure(), q.isSlow(), q.isCacheable(),
            q.isReplicated(), q.getContextID(), paramId, q.getCategoryID(), q.getShortDescription(), q.getMessageWhenEmpty(),
            Strings2.emptyToNull( q.getWellKnownName() ), Strings2.emptyToNull( q.getMenuName() ), q.getTitleName(), q.getNotSupported(),
            q.getNewDataCheckQuery(), q.getOriginModuleName(), oldId };
        final long id;
        if ( oldId == 0 )
        {
            final String insertId = sql.execInsert( "sql.insert.query", args );
            try
            {
                id = Long.parseLong( insertId );
            }
            catch ( NumberFormatException e1 )
            {
                throw new IllegalStateException( "Query insert statement returned incorrect insertId: " + insertId + " (query: " + e.getName()
                    + "." + q.getName() + ")", e1 );
            }
            queryIds.add( e.getName(), q.getName(), id );
        }
        else
        {
            sql.exec( "sql.enable.insert.identity", "queries" );
            sql.exec( "sql.insert.query.id", args );
            sql.exec( "sql.disable.insert.identity", "queries" );
            id = oldId;
        }
        sql.comment( "Query ID = " + id, false );
        syncQueryPerRole( q, id );
        syncOperationPerQuery( q, e, id );
        syncQuerySettings( id, q.getQuerySettings() );
        if ( !q.getTemplateQueryName().isEmpty() || q.getQuickFilters().length > 0 )
        {
            secondPassQueries.add( q );
        }
        syncPageCustomizations( q, oldQ );
        insertIcon( q.getIcon() );
    }

    private void syncOperationPerQuery( Query q, Entity e, final long id ) throws ExtendedSqlException
    {
        for ( String name : q.getOperationNames().getFinalValues() )
        {
            final long operationId = operationIds.get( e.getName(), name );
            if ( operationId != 0 )
            {
                sql.execDelayedInsert( "sql.operations.per.query", operationId, id );
            }
            else
            {
                if ( !name.equals( "+/- category" ) )
                {
                    Operation operation = e.getOperations().get( name );
                    if ( operation == null || operation.isAvailable() )
                    {
                        warnings.add( new ProjectElementException( q.getCompletePath(), "operationNames", "Operation not found: " + name ) );
                    }
                }
            }
        }
    }

    private void syncPageCustomizations( BeVectorCollection<?> collection, BeVectorCollection<?> oldCollection ) throws ExtendedSqlException, ProjectElementException
    {
        Map<String, PageCustomization> customizations = getAsMap( collection, PageCustomization.CUSTOMIZATIONS_COLLECTION,
                PageCustomization.class );
        Map<String, PageCustomization> oldCustomizations = getAsMap( oldCollection, PageCustomization.CUSTOMIZATIONS_COLLECTION,
                PageCustomization.class );
        for ( PageCustomization customization : customizations.values() )
        {
            PageCustomization oldCustomization = oldCustomizations.remove( customization.getName() );
            if ( customization.equals( oldCustomization ) )
                continue;
            String key = '/' + customization.getName();
            String type = '.' + customization.getType();
            sql.exec( "sql.delete.pagecustomization", key );
            Set<String> roles = customization.getRoles();
            if ( roles.isEmpty() )
            {
                roles = Collections.singleton( null );
            }
            for ( String role : roles )
            {
                sql.execDelayedInsert( "sql.insert.pagecustomization", customization.getOriginModuleName(), key, type, role, customization
                        .getResult().validate() );
            }
        }
        for ( PageCustomization customization : oldCustomizations.values() )
        {
            String key = '/' + customization.getName();
            sql.exec( "sql.delete.pagecustomization", key );
        }
    }

    private void syncQueryPerRole( Query q, final long id ) throws ExtendedSqlException
    {
        for ( String role : q.getRoles().getFinalRoles() )
        {
            sql.execDelayedInsert( "sql.queries.per.role", id, role, q.isDefaultView() ? "yes" : "no" );
        }
    }

    private void syncQuickFilters( long id, QuickFilter[] quickFilters ) throws ExtendedSqlException
    {
        for ( QuickFilter filter : quickFilters )
        {
            long targetQueryId = queryIds.get( filter.getQuery().getEntity().getName(), filter.getTargetQueryName() );
            if ( targetQueryId != 0 )
            {
                sql.execDelayedInsert( "sql.insert.quickFilter", id, filter.getName(), filter.getQueryParam(), targetQueryId,
                        filter.getFilteringClass(), filter.getOriginModuleName() );
            }
        }
    }

    private void syncQuerySettings( long id, QuerySettings[] querySettings ) throws ExtendedSqlException
    {
        for ( QuerySettings settings : querySettings )
        {
            for ( String role : settings.getRoles().getFinalRoles() )
            {
                sql.execDelayedInsert( "sql.insert.querySettings", id, role, settings.getMaxRecordsPerPage(),
                        settings.getMaxRecordsPerPrintPage(), settings.getMaxRecordsInDynamicDropDown(), settings.getColorSchemeID(),
                        settings.getAutoRefresh(), settings.getBeautifier() );
            }
        }
    }

    private <T extends BeModelElement> Map<String, T> getAsMap( BeVectorCollection<?> parent, String name, Class<T> type )
    {
        Map<String, T> result = new LinkedHashMap<>();
        if ( parent == null )
            return result;
        BeModelCollection<T> collection = parent.getCollection( name, type );
        if ( collection == null )
            return result;
        for ( T element : collection )
        {
            if ( !type.isInstance( element ) )
                warnings.add( new ProjectElementException( element, "Element has type " + element.getClass().getName() + "; expected: "
                    + type.getName() + ". This is internal problem, please contact the developers." ) );
            else
                result.put( element.getName(), element );
        }
        return result;
    }

    private String toInClause( Collection<? extends Number> numbers )
    {
        return "(" + StreamEx.of( numbers ).joining( "," ) + ")";
    }

    private void clearEntityInfo( Entity entity, Entity oldEntity ) throws ExtendedSqlException
    {
        if ( entity == null && oldEntity == null )
            return;
        String entityName = oldEntity == null ? entity.getName() : oldEntity.getName();
        sql.startSection( "Clear entity " + entityName );

        // Remove all queries related to the given entity
        Set<Long> queryIds = getItems( this.queryIds.getEntity( entityName ), entity == null ? null : entity.getQueries(),
                oldEntity == null ? null : oldEntity.getQueries() );
        if ( !queryIds.isEmpty() )
        {
            removeQueries( queryIds );
        }

        // Remove all operations on the given entity
        Set<Long> operationIds = getItems( this.operationIds.getEntity( entityName ), entity == null ? null : entity.getOperations(),
                oldEntity == null ? null : oldEntity.getOperations() );
        if ( !operationIds.isEmpty() )
        {
            removeOperations( entityName, operationIds );
        }

        // Remove entity itself
        sql.exec( "sql.delete.entity", entityName );
    }

    private void removeOperations( String entityName, Set<Long> operationIds ) throws ExtendedSqlException
    {
        String arg = toInClause( operationIds );
        sql.exec( "sql.remove.operation.roles", arg );
        sql.exec( "sql.remove.operation.extenders", entityName, arg );
        sql.exec( "sql.remove.operation", arg );
    }

    private void removeQueries( Set<Long> queryIds ) throws ExtendedSqlException
    {
        String arg = toInClause( queryIds );
        sql.exec( "sql.remove.query.operations", arg );
        sql.exec( "sql.remove.query.roles", arg );
        sql.exec( "sql.remove.query.filters", arg );
        sql.exec( "sql.remove.query.settings", arg );
        sql.exec( "sql.remove.query", arg );
    }

    private Set<Long> getItems(
        Map<String, CacheEntry> entityIds,
        BeModelCollection<? extends EntityItem> collection,
        BeModelCollection<? extends EntityItem> oldCollection )
    {
        Set<Long> queryIds = new HashSet<>();
        for ( Entry<String, CacheEntry> entry : entityIds.entrySet() )
        {
            if ( entry.getKey().equals( Query.SPECIAL_TABLE_DEFINITION ) )
                continue;
            EntityItem item = collection == null ? null : collection.get( entry.getKey() );
            EntityItem oldItem = oldCollection == null ? null : oldCollection.get( entry.getKey() );
            if ( item != null && oldItem != null && item.equals( oldItem ) )
                continue;
            queryIds.add( entry.getValue().id );
        }
        return queryIds;
    }

    private void cacheIds() throws ExtendedSqlException
    {
        sql.comment( "Cache IDs", false );
        ResultSet rs = sql.executeNamedQuery( "sql.select.queries" );
        int count = 0;
        try
        {
            while ( rs.next() )
            {
                queryIds.add( rs.getString( 2 ), rs.getString( 3 ), rs.getLong( 1 ) );
                count++;
            }
        }
        catch ( SQLException e )
        {
            throw new ExtendedSqlException( sql.getConnector().getConnectString(), "sql.select.queries", e );
        }
        finally
        {
            sql.close(rs);
        }
        sql.comment( count + " queries are read", false );
        rs = sql.executeNamedQuery( "sql.select.operations" );
        count = 0;
        try
        {
            while ( rs.next() )
            {
                operationIds.add( rs.getString( 2 ), rs.getString( 3 ), rs.getLong( 1 ) );
                count++;
            }
        }
        catch ( SQLException e )
        {
            throw new ExtendedSqlException( sql.getConnector().getConnectString(), "sql.select.operations", e );
        }
        finally
        {
            sql.close(rs);
        }
        sql.comment( count + " operations are read", false );
    }

    private List<Entity> getReferencesEntities( Project project, String column )
    {
        List<Entity> entities = new ArrayList<>();
        for ( Module module : project.getModulesAndApplication() )
        {
            for ( Entity entity : module.getEntities() )
            {
                for ( TableReference reference : entity.getAllReferences() )
                {
                    if ( reference.getColumnsFrom().equalsIgnoreCase( column ) )
                        entities.add( entity );
                }
            }
        }
        return entities;
    }

    private void cacheLocalizations( Map<LocalizationEntry, LocalizationValue> localizations, Project project, List<Module> order )
    {
        Set<String> modulesOrder = new LinkedHashSet<>();
        for ( Module module : order )
        {
            modulesOrder.add( module.getName() );
        }
        for ( Module module : project.getModulesAndApplication() )
        {
            modulesOrder.add( module.getName() );
        }
        for ( String moduleName : modulesOrder )
        {
            Module module = project.getModule( moduleName );
            if ( module == null )
                continue;
            for ( LanguageLocalizations lang : module.getLocalizations() )
            {
                for ( EntityLocalizations entity : lang )
                {
                    if ( entity.getName().equals( "@References" ) )
                    {
                        for ( LocalizationRow row : entity.getRows() )
                        {
                            String columnName = project.getDatabaseSystem().getTypeManager().normalizeIdentifierCase( row.getKey() );
                            for ( Entity refEntity : getReferencesEntities( project, columnName ) )
                            {
                                BeModelCollection<Operation> operations = refEntity.getOperations();
                                for ( String operation : REF_OPERATIONS )
                                {
                                    if ( operations.contains( operation ) )
                                    {
                                        localizations.put( new LocalizationEntry( lang.getName(), refEntity.getName(), operation, columnName ),
                                                new LocalizationValue( module.getName(), row.getValue() ) );
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        for ( LocalizationRow row : entity.getRows() )
                        {
                            localizations.put( new LocalizationEntry( lang.getName(), entity.getName(), row.getTopic(), row.getKey() ),
                                    new LocalizationValue( module.getName(), row.getValue() ) );
                        }
                    }
                }
            }
        }
    }

    private static class CacheEntry
    {
        long id;

        public CacheEntry( long id )
        {
            this.id = id;
        }
    }

    private static class IdCache
    {
        private final Map<String, Map<String, CacheEntry>> map = new HashMap<>();

        public void add( String entity, String name, long id )
        {
            Map<String, CacheEntry> ids = map.get( entity );
            if ( ids == null )
            {
                ids = new HashMap<>();
                map.put( entity, ids );
            }
            ids.put( name, new CacheEntry( id ) );
        }

        public Map<String, CacheEntry> getEntity( String entity )
        {
            Map<String, CacheEntry> ids = map.get( entity );
            return ids == null ? Collections.<String, CacheEntry> emptyMap() : Collections.unmodifiableMap( ids );
        }

        public long get( String entity, String name )
        {
            Map<String, CacheEntry> ids = map.get( entity );
            if ( ids == null )
                return 0;
            CacheEntry entry = ids.get( name );
            if ( entry == null )
                return 0;
            return entry.id;
        }
    }

    private static class LocalizationValue
    {
        final String value, origin;

        public LocalizationValue( String origin, String value )
        {
            this.origin = origin;
            this.value = value;
        }
    }

    private static class LocalizationEntry
    {
        final String lang, entity, topic, key;

        public LocalizationEntry( String lang, String entity, String topic, String key )
        {
            this.lang = lang;
            this.entity = entity;
            this.topic = topic;
            this.key = key;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( entity, key, lang, topic );
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
                return true;
            if ( obj == null || getClass() != obj.getClass() )
                return false;
            LocalizationEntry other = ( LocalizationEntry ) obj;
            return Objects.equals( entity, other.entity ) && Objects.equals( key, other.key ) && Objects.equals( lang, other.lang )
                && Objects.equals( topic, other.topic );
        }
    }
}
