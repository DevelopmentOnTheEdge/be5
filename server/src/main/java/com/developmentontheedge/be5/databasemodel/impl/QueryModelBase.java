package com.developmentontheedge.be5.databasemodel.impl;

import com.developmentontheedge.be5.databasemodel.QueryModel;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

class QueryModelBase implements QueryModel
{
    private String queryName;
    private Map<String, ? super Object> params;

    public QueryModelBase( String queryName, Map<String, ? super Object> params)
    {
        this.queryName = queryName;
        this.params = new HashMap<>( params );
    }

    @Override
    public List<DynamicPropertySet> collect()
    {
        return this.collect( ( bean, rn ) -> bean );
    }

//    private QueryExecuter makeQueryExecuter()
//    {
//        QueryExecuter qe = new QueryExecuter( connector, userInfo, params, "" );
//        qe.setNoOfRecords( Integer.MAX_VALUE );
//        return qe;
//    }

    @Override
    public <T> List<T> collect( final BiFunction<DynamicPropertySet, Integer, T> lambda )
    {
        final List<T> list = new ArrayList<>();
        //makeQueryExecuter().query( getEntityName(), queryName, params, ( bean, rowNumber ) -> list.add( lambda.apply( bean, rowNumber ) ) );
        return list;
    }

    @Override
    public void each( final BiConsumer<DynamicPropertySet, Integer> lambda )
    {
        //makeQueryExecuter().query( getEntityName(), queryName, params, lambda::accept );
    }

//    @Experimental
//    @Override
//    public CloseableIterator<DynamicPropertySet> getIterator()
//    {
//        return getIterator( dps -> dps );
//    }
//
//    @Experimental
//    @Override
//    public <T> CloseableIterator<T> getIterator( Function<DynamicPropertySet, T> func )
//    {
//        try
//        {
//            QueryExecuter qe = makeQueryExecuter();
//            QueryIterator delegate = qe.makeIterator( getEntityName(), queryName, params );
//            FragmentContext qp = qe.getFragmentSupport();
//            return new CloseableIterator<T>() {
//
//                @Over ride
//                public boolean hasNext()
//                {
//                    boolean hasNext = delegate.hasNext();
//                    return hasNext;
//                }
//
//                private DynamicPropertySet initializeDps( DynamicPropertySet dps )
//                {
//                    return QueryExecuter.DynamicPropertySetLazy.getInstance( dps, qp );
//                }
//
//                @Override
//                public T next()
//                {
//                    return func.apply( initializeDps( ( DynamicPropertySet )delegate.next() ) );
//                }
//
//                @Override
//                public void close() throws IOException
//                {
//                    if( delegate instanceof ResultSetQueryIterator )
//                        ( ( ResultSetQueryIterator )delegate ).closeResultSet();
//                }
//
//            };
//        }
//        catch( Exception e )
//        {
//            throw new RuntimeException( e );
//        }
//    }
}