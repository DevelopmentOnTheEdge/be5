package com.developmentontheedge.be5.metadata.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandardOperations
{
    
    private interface IOperationProducer
    {
        Operation produce( String id, Entity entity, String name );
    }
    
    private static IOperationProducer producer( final int visibleWhen )
    {
        return new IOperationProducer()
        {
            @Override
            public Operation produce( final String id, final Entity entity, final String name )
            {
                final JavaOperation operation = new JavaOperation( name, entity );
                operation.setRecords( visibleWhen );
                operation.setCode( PACKAGE + "." + id );
                operation.setOriginModuleName( entity.getProject().getProjectOrigin() );
                
                return operation;
            }
        };
    }
    
    private static final String INSERT = "InsertOperation";
    private static final String SILENT_INSERT = "SilentInsertOperation";
    private static final String SILENT_INSERT_NO_COLLECTIONS = "SilentInsertWithoutCollectionsOperation";
    private static final String EDIT = "EditOperation";
    private static final String SILENT_EDIT = "SilentEditOperation";
    private static final String CLONE = "CloneOperation";
    private static final String SILENT_CLONE = "SilentCloneOperation";
    private static final String DELETE = "DeleteOperation";
    private static final String SILENT_DELETE = "SilentDeleteOperation";
    private static final String CHANGE_COLLECTION_OWNER = "ChangeCollectionOwner";
    private static final String PARENT_RECORD = "ParentRecord";
    private static final String FILTER = "HttpSearchOperation";
    
    private static final String PACKAGE = "com.beanexplorer.enterprise.operations";
    private static final String[] NAMES = new String[] {
        INSERT,
        SILENT_INSERT,
        SILENT_INSERT_NO_COLLECTIONS,
        EDIT,
        SILENT_EDIT,
        CLONE,
        SILENT_CLONE,
        DELETE,
        SILENT_DELETE,
        CHANGE_COLLECTION_OWNER,
        PARENT_RECORD,
        FILTER
    };
    private static final Map<String, String> defaultNames = new HashMap<>();
    private static final Map<String, IOperationProducer> suppliers = new HashMap<>();
    
    static
    {
        defaultNames.put( INSERT, "Insert" );
        defaultNames.put( SILENT_INSERT, "Insert" );
        defaultNames.put( SILENT_INSERT_NO_COLLECTIONS, "Insert" );
        defaultNames.put( EDIT, "Edit" );
        defaultNames.put( SILENT_EDIT, "Edit" );
        defaultNames.put( CLONE, "Clone" );
        defaultNames.put( SILENT_CLONE, "Clone" );
        defaultNames.put( DELETE, "Delete" );
        defaultNames.put( SILENT_DELETE, "Delete" );
        defaultNames.put( CHANGE_COLLECTION_OWNER, "Change Collection Owner" );
        defaultNames.put( PARENT_RECORD, "Parent Record" );
        defaultNames.put( FILTER, "Filter" );
        
        suppliers.put( INSERT, producer( Operation.VISIBLE_ALWAYS ) );
        suppliers.put( SILENT_INSERT, producer( Operation.VISIBLE_ALWAYS ) );
        suppliers.put( SILENT_INSERT_NO_COLLECTIONS, producer( Operation.VISIBLE_ALWAYS ) );
        suppliers.put( EDIT, producer( Operation.VISIBLE_WHEN_ANY_SELECTED_RECORDS ) );
        suppliers.put( SILENT_EDIT, producer( Operation.VISIBLE_WHEN_ANY_SELECTED_RECORDS ) );
        suppliers.put( CLONE, producer( Operation.VISIBLE_WHEN_ONE_SELECTED_RECORD ) );
        suppliers.put( SILENT_CLONE, producer( Operation.VISIBLE_WHEN_ONE_SELECTED_RECORD ) );
        suppliers.put( DELETE, producer( Operation.VISIBLE_WHEN_ANY_SELECTED_RECORDS ) );
        suppliers.put( SILENT_DELETE, producer( Operation.VISIBLE_WHEN_ANY_SELECTED_RECORDS ) );
        suppliers.put( CHANGE_COLLECTION_OWNER, producer( Operation.VISIBLE_WHEN_HAS_RECORDS ) ); // not sure
        suppliers.put( PARENT_RECORD, producer( Operation.VISIBLE_WHEN_ONE_SELECTED_RECORD ) ); // not sure
        suppliers.put( FILTER, producer( Operation.VISIBLE_WHEN_HAS_RECORDS ) );
    }
    
    /**
     * This class is not intended to be instantiated.
     */
    public StandardOperations()
    {
        throw new AssertionError(); // not allowed
    }
    
    // Usually strings are more useful that enumerations
    public static List<String> ids()
    {
        return Collections.unmodifiableList( Arrays.asList( NAMES ) );
    }
    
    public static String getDefaultNameOf( final String id )
    {
        final String foundName = defaultNames.get( id );
        
        if ( foundName != null )
        {
            return foundName;
        }
        
        return id;
    }
    
    public static Operation newStandardOperation( final String id, final Entity entity, final String name )
    {
        if ( Arrays.asList( NAMES ).contains( id ) )
        {
            return suppliers.get( id ).produce( id, entity, name );
        }
        
        throw new IllegalArgumentException( id );
    }
    
    public static Operation newStandardOperation( final String id, final Entity entity )
    {
        return newStandardOperation( id, entity, getDefaultNameOf( id ) ); 
    }

}
