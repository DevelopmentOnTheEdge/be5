package com.developmentontheedge.be5.databasemodel;


import com.developmentontheedge.be5.api.sql.SqlExecutor;
import com.developmentontheedge.be5.api.sql.SqlExecutorVoid;

/**
 * Interface to access object-wrapped representation of the entity.
 * 
 * @author ruslan
 *
 */
public interface EntityAccess<E extends EntityModel<RecordModel>> {
    
    /**
     * 
     * @param entityName entity name
     * @return EntityModel
     */
    EntityModel getEntity(String entityName);

//    /**
//     * Returns database connector
//     * @return database connector
//     */
//    DatabaseConnector getConnector();
//
//    /**
//     * Returns database analyzer that corresponds to the connector.
//     * @return database analyzer
//     */
//    DatabaseAnalyzer getAnalyzer();

    //DatabaseService getDatabaseService();

//    EntityAccess<E> getCache();
//
//    String getTcloneId();

//    EntityAccess<E> getCloned(String tcloneId);

    <T> T transactionWithResult(SqlExecutor<T> executor);

    void transaction(SqlExecutorVoid executor);
}
