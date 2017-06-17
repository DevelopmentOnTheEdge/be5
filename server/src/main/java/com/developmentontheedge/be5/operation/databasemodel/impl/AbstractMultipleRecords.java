package com.developmentontheedge.be5.operation.databasemodel.impl;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.databasemodel.MultipleRecords;
import com.developmentontheedge.be5.operation.databasemodel.RecordModel;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;


//@DirtyRealization( comment = "Low type safety, pure logic!" )
public abstract class AbstractMultipleRecords<T> implements MultipleRecords<T>
{

    private Entity entity;
    private ResultHandler<T> handler;
    
    public AbstractMultipleRecords(Entity entity )
    {
        this.entity = entity;
    }
    
    public void setHandler( ResultPostHandler<? extends RecordModel,T> handler )
    {
        this.handler = handler;
    }
    
    @Override
    public T get()
    {
        return get( Collections.<String, Object>emptyMap() );
    }

//    @Override
//    public T get( String queryName, Map<String, String> values )
//    {
//        final List<RecordModel> list = new ArrayList<RecordModel>();
//
//        final ResultEachHandler eachHandler = handler instanceof ResultEachHandler ? ( ResultEachHandler )handler : null;
//
//        new QueryExecuter( databaseService, userInfo, values, "" ).query( entity, queryName, values, new RowBean(){
//
//            @Override
//            public void use( DynamicPropertySet bean, int rowNumber ) throws Exception {
//                if( eachHandler != null )
//                {
//                    eachHandler.use( bean, rowNumber );
//                }
//                list.add( createRecord( bean ) );
//            }
//
//        } );
//        if( handler instanceof ResultPostHandler )
//        {
//            return ( ( ResultPostHandler<T> )handler ).postUse( list );
//        }
//        else return ( T )list;
//    }

    public T get( Map<String, ? extends Object> values )
    {
        return null;//values.isEmpty() ? get( new String[]{} ) : get( Utils.paramsToCondition( databaseService, entity, values ) );
    }
    
    @Override
    public T get( String ... conditions )
    {

        StringBuilder sb = new StringBuilder( "SELECT * FROM " );
        sb.append( entity ).append( " " ).append( entity );
        sb.append( " WHERE " ).append( getAdditionalConditions() );
        for( String condition : conditions )
        {
            sb.append( " AND " ).append( condition );
        }
        List<DynamicPropertySet> dpsList = null;//Utils.readAsRecords( databaseService, sb.toString() );
        List<RecordModel> recordList = new ArrayList<RecordModel>();

        final ResultEachHandler eachHandler = handler instanceof ResultEachHandler ? ( ResultEachHandler )handler : null;

        for( int i = 0; i < dpsList.size(); i++ )
        {
            RecordModel bean = createRecord( dpsList.get( i ) );
            if( eachHandler != null )
            {
                eachHandler.use( bean, i );
            }
            recordList.add( bean );
        }
        if( handler instanceof ResultPostHandler )
        {
            return ( ( ResultPostHandler<RecordModel,T> )handler ).postUse( recordList );
        }
        return ( T )recordList;


    }

    interface ResultHandler<T> { }
    
    interface ResultEachHandler<R extends RecordModel> extends ResultHandler
    {
        public void use(R bean, int row);
    }
    
    interface ResultPostHandler<R extends RecordModel,T> extends ResultHandler
    {
        public T postUse(List<R> list);
    }
    
    
    public static class LambdaDPSHandler<R extends RecordModel,T> implements ResultEachHandler<R>, ResultPostHandler<R,List<T>>
    {
        private BiFunction<R, Integer, T> lambda;

        private List<T> result = new ArrayList<T>();
        
        LambdaDPSHandler( BiFunction<R, Integer, T> lambda )
        {
            this.lambda = lambda;
        }
        
        @Override
        public void use( R bean, int row )
        {
            T ret = lambda.apply( bean, row );
            if( ret != null)
                result.add( ret );
        }

        public List<T> getResult()
        {
            return result;
        }

        @Override
        public List<T> postUse( List list ) 
        {
            return list;
        }        
    }
    
    public static class ArrayHandler<T> implements ResultPostHandler<RecordModel,T>
    {
        public T postUse( List<RecordModel> list )
        {
           RecordModel[] arr = new RecordModel[list.size()];
           return ( T )list.toArray( arr ); 
        }
    }
    
    public abstract String getAdditionalConditions();
    public abstract RecordModel createRecord( DynamicPropertySet dps );
}
