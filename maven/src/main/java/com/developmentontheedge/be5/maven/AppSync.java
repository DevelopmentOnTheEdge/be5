package com.developmentontheedge.be5.maven;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.developmentontheedge.be5.metadata.exception.FreemarkerSqlException;
import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.IndexColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexDef;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.ViewDef;
import com.developmentontheedge.be5.metadata.sql.BeSqlExecutor;
import com.developmentontheedge.be5.metadata.sql.DatabaseSynchronizer;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.sql.DatabaseSynchronizer.SyncMode;
import com.developmentontheedge.be5.metadata.sql.pojo.IndexInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.SqlColumnInfo;
import com.developmentontheedge.be5.metadata.sql.schema.DbmsSchemaReader;
import com.developmentontheedge.be5.metadata.sql.type.DbmsTypeManager;
import com.developmentontheedge.be5.metadata.sql.type.DefaultTypeManager;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.DbmsType;
import com.developmentontheedge.dbms.ExtendedSqlException;
import com.developmentontheedge.dbms.MultiSqlParser;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo( name = "sync")
public class AppSync extends Be5Mojo
{
    @Parameter (property = "BE5_UPDATE_CLONES")
    protected boolean updateClones;

    @Parameter (property = "BE5_FORCE_UPDATE")
    protected boolean forceUpdate;

    protected BeSqlExecutor sqlExecutor;
    
    ///////////////////////////////////////////////////////////////////
    
    @Override
    public void execute() throws MojoFailureException
    {
        init();
        mergeModules();

        PrintStream ps = null;
        try
        {
            if(logPath != null)
            {
                logPath.mkdirs();
                ps = new PrintStream(new File(logPath, beanExplorerProject.getName() + "_sync_ddl.sql"), "UTF-8");
            }
            
            sqlExecutor = new BeSqlExecutor(connector, ps);

            if(beanExplorerProject.getDebugStream() != null)
            {
                beanExplorerProject.getDebugStream().println("Modules and extras for "+beanExplorerProject.getName()+":");
                beanExplorerProject.allModules()
                        .map( m -> "- " + m.getName() + ": " + (m.getExtras() == null ? "" : String.join(", ", m.getExtras())) )
                        .forEach( beanExplorerProject.getDebugStream()::println );
            }
            
            readSchema();
            createEntities();

            DatabaseSynchronizer databaseSynchronizer = new DatabaseSynchronizer(logger, sqlExecutor, beanExplorerProject);
            Project oldProject = null;
            String ddlString = databaseSynchronizer.getDdlStatements(oldProject, updateClones, false);
            ddlString = MultiSqlParser.normalize(beanExplorerProject.getDatabaseSystem().getType(), ddlString);

            
            /* TODO
            if(orphans != null)
            {
                beanExplorerProject.getDebugStream().println("Orphan tables: "+orphans.getEntityNames());
            } */
            
            
            if(ddlString.isEmpty())
            {
            	this.getLog().info("Database scheme is up-to-date");
            	return;
            } 
            
            if( forceUpdate )
            {
                databaseSynchronizer.sync( SyncMode.DDL_CLONES, oldProject );
            }
            else
            {
                    System.err.println("The following statements should be executed to update database scheme:");
                    System.err.println(ddlString);
            }
            
            checkSynchronizationStatus(databaseSynchronizer);
            logger.setOperationName( "Finished" );
        }
        catch ( ProjectElementException | FreemarkerSqlException | ExtendedSqlException | SQLException e ) //ReadException | ProjectLoadException | SQLException e )
        {
            if(debug)
                throw new MojoFailureException("Synchronisation error: " + e.getMessage(), e);
            throw new MojoFailureException("Synchronisation error: " + e.getMessage());
        }
        catch( IOException | ProcessInterruptedException e )
        {
            throw new MojoFailureException("Synchronisation error: " + e.getMessage(), e);
        }
        catch( Throwable t )
        {
        	t.printStackTrace();
            throw new MojoFailureException("Synchronisation error: " + t.getMessage(), t);
        }
        finally
        {
            if(ps != null)
            {
                ps.close();
            }
        }
    }

    protected void checkSynchronizationStatus( DatabaseSynchronizer databaseSynchronizer )
    {
        List<ProjectElementException> warnings = databaseSynchronizer.getWarnings();
        if(!warnings.isEmpty())
        {
            System.err.println( "Synchronization of "+databaseSynchronizer.getProject().getName()+" produced "+warnings.size()+" warning(s):" );
            for(ProjectElementException warning : warnings)
            {
                displayError( warning );
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Read database structure
    //
    
    private final List<String> warnings = new ArrayList<>();
    private String defSchema = null;

    private Map<String, String>  			 tableTypes;
    private Map<String, List<SqlColumnInfo>> columns;
    private Map<String, List<IndexInfo>>     indices;
    private List<Entity>  			 		 entities;
    
    private void readSchema() throws ExtendedSqlException, SQLException, ProcessInterruptedException
    {
        getLog().info("Read database scheme ...");
        long time = System.currentTimeMillis();
        
        ProcessController controller = new NullLogger();

        Rdbms rdbms = DatabaseUtils.getRdbms(connector);
        DbmsSchemaReader schemaReader = rdbms.getSchemaReader();

        defSchema  = schemaReader.getDefaultSchema(sqlExecutor);
        tableTypes = schemaReader.readTableNames(sqlExecutor, defSchema, controller);
        columns    = schemaReader.readColumns   (sqlExecutor, defSchema, controller);
        indices    = schemaReader.readIndices   (sqlExecutor, defSchema, controller);

        if( debug )
        {
            if(!warnings.isEmpty())
            {
                System.err.println(warnings.size() + " warning(s) during loading the project from " + sqlExecutor.getConnector().getConnectString() );
                Collections.sort( warnings );
                for(String warning : warnings)
                {
                    System.err.println(warning);
                }
            }
        }
        
        getLog().info("  comleted, " + (System.currentTimeMillis() - time) + "ms.");
    }

    private void createEntities() throws ExtendedSqlException, SQLException
    {
        Rdbms databaseSystem = DatabaseUtils.getRdbms(connector);
        DbmsTypeManager typeManager = databaseSystem == null ? new DefaultTypeManager() : databaseSystem.getTypeManager();
        boolean casePreserved = typeManager.normalizeIdentifierCase( "aA" ).equals( "aA" );

        entities = new ArrayList<>();
        Project project = new Project("internal-db");
        Module module = new Module("temp", project);
        
        for(String table : tableTypes.keySet() )
        {
            if ( !"TABLE".equals(tableTypes.get(table.toLowerCase())) )
                continue;
            
            List<SqlColumnInfo> columnInfos = columns.get(table.toLowerCase());
            if(columnInfos == null)
                continue;
            
            Entity entity = new Entity(table, module, EntityType.TABLE);
            entities.add(entity);
            
            TableDef tableDef = new TableDef(entity);
            for(SqlColumnInfo info : columnInfos)
            {
                // "isDeleted___" column is maintained by BeanExplorer
                if(info.getName().equalsIgnoreCase( "isdeleted___" ))
                    continue;

                ColumnDef column = new ColumnDef(info.getName(), tableDef.getColumns());
                column.setType( createColumnType(info) );
                typeManager.correctType( column.getType() );
                column.setPrimaryKey( info.getName().equalsIgnoreCase( entity.getPrimaryKey() ) );	// PENDING
                column.setCanBeNull( info.isCanBeNull() );
                String defaultValue = info.getDefaultValue();
                column.setAutoIncrement( info.isAutoIncrement() );
                if(!info.isAutoIncrement())
                {
                    column.setDefaultValue(defaultValue);
                }
                if(column.isPrimaryKey() && typeManager.getKeyType().equals( typeManager.getTypeClause(column.getType()) ))
                {
                    column.getType().setTypeName( SqlColumnType.TYPE_KEY );
                }
//                column.setOriginModuleName( module.getName() );
                DataElementUtils.saveQuiet( column );
            }
            
            List<IndexInfo> indexInfos = indices.get(table.toLowerCase(Locale.ENGLISH));
            if(indexInfos != null)
            {
                INDEX: for ( IndexInfo info : indexInfos )
                {
                    if( !casePreserved )
                        info.setName( info.getName().toUpperCase(Locale.ENGLISH) );
                    
                    IndexDef index = new IndexDef( info.getName(), tableDef.getIndices() );
                    index.setUnique( info.isUnique() );
                    
                    for(String indexCol : info.getColumns())
                    {
                        IndexColumnDef indexColumnDef = IndexColumnDef.createFromString( indexCol, index );
                        if( tableDef.getColumns().get(indexColumnDef.getName()) == null)
                        {
                            if( debug )
                            {
                                warnings.add( "Unsupported functional index found: " + index.getName() + " (problem is here: "
                                    + indexCol + "); skipped" );
                            }
                            continue INDEX;
                        }
                        
                        DataElementUtils.saveQuiet(indexColumnDef);
                    }

                    if(index.isUnique() && index.getSize() == 1)
                    {
                        IndexColumnDef indexColumnDef = index.iterator().next();
                        if(!indexColumnDef.isFunctional())
                        {
                            if( index.getName().equalsIgnoreCase(table + "_pkey") )
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

        if ( sqlExecutor.getConnector().getType() != DbmsType.MYSQL )
            return;
        
        // For MySQL only now
        for ( Entity entity : entities )
        {
            final String table = entity.getName();
            if ( !"VIEW".equalsIgnoreCase(tableTypes.get(table)) )
                continue;
            
            String createTable;
            ResultSet rs = sqlExecutor.executeNamedQuery( "sql.getTableDefinition", table );
            try
            {
                if( !rs.next() )
                    continue;
                
                createTable = rs.getString(2);
            }
            finally
            {
                sqlExecutor.getConnector().close( rs );
            }
            
            int as = createTable.indexOf( " AS " );
            if ( as < 0 )
                continue;
            
            createTable = createTable.substring( as + " AS ".length() );
            ViewDef def = new ViewDef(entity);
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
    
    ///////////////////////////////////////////////////////////////////////////
    // Synchronization
    //
/*    
    protected String getDdlStatements(boolean dangerousOnly) throws ExtendedSqlException
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
*/
    
}
