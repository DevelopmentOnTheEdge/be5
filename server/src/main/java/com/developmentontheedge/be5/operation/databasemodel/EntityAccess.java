package com.developmentontheedge.be5.operation.databasemodel;


import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.model.UserInfo;

/**
 * Interface to access object-wrapped representation of the entity.
 * 
 * @author ruslan
 *
 */
public interface EntityAccess<E extends EntityModel<RecordModel>> {
    
    /**
     * 
     * @param entityName
     * @return
     */
    <T extends E> T getEntity(String entityName);
    
    /**
     * Returns an entity with set table cloned id
     * @param entityName entity name
     * @param tcloneId table cloned id post-fix
     * @return
     */
    <T extends E> T getEntity(String entityName, String tcloneId);
    
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

    DatabaseService getDatabaseService();

    UserInfo getUserInfo();

//    EntityAccess<E> getCache();
//
//    String getTcloneId();

//    EntityAccess<E> getCloned(String tcloneId);

}
