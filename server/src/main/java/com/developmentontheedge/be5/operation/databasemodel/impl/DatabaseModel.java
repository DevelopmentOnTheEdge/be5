package com.developmentontheedge.be5.operation.databasemodel.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.DpsExecutor;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlHelper;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.operation.databasemodel.EntityAccess;
import com.developmentontheedge.be5.operation.databasemodel.EntityModel;
import com.developmentontheedge.be5.operation.databasemodel.RecordModel;
import com.developmentontheedge.be5.operation.databasemodel.groovy.DatabaseModelMetaClass;
import com.developmentontheedge.be5.operation.databasemodel.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.operation.databasemodel.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.operation.databasemodel.groovy.GroovyRegister;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetDecorator;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Database model based on concept of object-oriented database system
 *
 * @author ruslan
 */
final public class DatabaseModel implements EntityAccess<EntityModel<RecordModel>>
{
    private static final Logger log = Logger.getLogger(DatabaseModel.class.getName());

    static
    {
        try
        {
            GroovyRegister.registerMetaClass( DatabaseModelMetaClass.class, DatabaseModel.class );
        }
        catch( NoClassDefFoundError e )
        {
            throw Be5Exception.internal(e);
        }

        try
        {
            // TODO move to beans (having compilation problems)
            GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetSupport.class );
//            GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetScriptable.class );
//            GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetLazy.class );
            GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, DynamicPropertySetDecorator.class );
//            GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, QRec.class );
//            GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, JDBCRecordAdapterAsQuery.class );
//            GroovyRegister.registerMetaClass( DynamicPropertySetMetaClass.class, EntityRecordAdapter.class );

            GroovyRegister.registerMetaClass( DynamicPropertyMetaClass.class, DynamicProperty.class );
        }
        catch( NoClassDefFoundError e )
        {
            throw Be5Exception.internal(e);
        }

    }

//    final private DatabaseAnalyzer analyzer;
//    final private DatabaseConnector connector;
    private DatabaseService databaseService;
    private SqlService db;
    private SqlHelper sqlHelper;
    private Meta meta;

    public DatabaseModel(DatabaseService databaseService, SqlService db, SqlHelper sqlHelper, Meta meta)
    {
        this.databaseService = databaseService;
        this.db = db;
        this.sqlHelper = sqlHelper;
        this.meta = meta;
    }

    private static EntityModel getEntityInstance(Class<EntityModel> clazz, DatabaseModel database, UserInfo userInfo, String entityName, String tcloneId, boolean bForceCache )
    {
        try
        {
            Constructor<EntityModel> constructor = clazz.getDeclaredConstructor( DatabaseModel.class, UserInfo.class, String.class, String.class, boolean.class );
            return constructor.newInstance( database, userInfo, entityName, tcloneId, bForceCache );
        }
        catch( NoSuchMethodException noSuchConstructor )
        {
            throw Be5Exception.internal(noSuchConstructor, "Constructor not found. Possible: " + Arrays.toString( clazz.getDeclaredConstructors() ));
            //log.severe("Constructor not found. Possible: " + Arrays.toString( clazz.getDeclaredConstructors() ), noSuchConstructor );
            //throw new RuntimeException( noSuchConstructor );
        }
        catch( IllegalAccessException | InstantiationException | SecurityException | IllegalArgumentException | InvocationTargetException e )
        {
            throw new RuntimeException( e );
        }
    }

    private static Class<?> loadClass( String className )
    {
        try
        {
            return EntityModelClassLoader.getInstance().loadClass( className );
        }
        catch( ClassNotFoundException e )
        {
            throw new RuntimeException( e );
        }
    }

//    public static DatabaseModel makeInstance( DatabaseService databaseService, UserInfo userInfo )
//    {
//        return new DatabaseModel( databaseService, userInfo );
//    }

    @Override
    public <T extends EntityModel<RecordModel>> T getEntity(String entityName )
    {
        return this.<T>getEntityModel( entityName );
    }

    @Override
    public DatabaseService getDatabaseService()
    {
        return null;
    }

//    private <T extends EntityModel<RecordModel>> T getEntityModel( String entityName )
//    {
//        return this.<T>getEntityModel( entityName );
//    }

    private <T extends EntityModel<RecordModel>> T getEntityModel( String entityName )
    {
        Entity entity = meta.getEntity(entityName);
        return ( T )new EntityModelBase( databaseService, db, sqlHelper, this, entity);
    }

//    /**
//     * Load and obtains the entity declaration from table 'entities'
//     *
//     * @param entityName
//     * @return
//     * @throws SQLException
//     */
//    private DynamicPropertySet loadEntityDeclaration( String entityName ) throws SQLException
//    {
//        String sql = "SELECT * FROM entities WHERE name = '" + entityName + "'";
//        return db.select(sql, DpsHelper::createDps);
//        //return QRec.withCache( connector, sql, ReferencesQueriesCache.getInstance() );
//    }

//    @Override
//    public DatabaseConnector getConnector()
//    {
//        return this.connector;
//    }
//
//    @Override
//    public DatabaseAnalyzer getAnalyzer()
//    {
//        return this.analyzer;
//    }


//    @Override
//    public EntityAccess<EntityModel<RecordModel>> getCache()
//    {
//        return new DatabaseModel( getConnector(), getUserInfo(), getTcloneId(), true );
//    }

//    @Override
//    public String getTcloneId()
//    {
//        return this.tcloneId;
//    }

//    @Override
//    public EntityAccess<EntityModel<RecordModel>> getCloned( String tcloneId )
//    {
//        return new DatabaseModel( getConnector(), getUserInfo(), tcloneId, true );
//    }

    private static class EntityModelClassLoader extends ClassLoader
    {
        private static EntityModelClassLoader instance = new EntityModelClassLoader( Utils.getClassLoader() );

        private EntityModelClassLoader( ClassLoader classLoader )
        {
            super( classLoader );
        }

        private static EntityModelClassLoader getInstance()
        {
            return instance;
        }
    }

//    @Override
//    public boolean equals( Object o )
//    {
//        if( this == o )return true;
//        if( !( o instanceof EntityAccess ) )
//        {
//            return false;
//        }
//        EntityAccess oth = ( EntityAccess )o;
//        return ( oth.getAnalyzer().getClass() == getAnalyzer().getClass() && Objects.equals( oth.getTcloneId(), getTcloneId() ) );
//    }
}
