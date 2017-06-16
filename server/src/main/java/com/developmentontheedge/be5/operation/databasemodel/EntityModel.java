package com.developmentontheedge.be5.operation.databasemodel;

import com.developmentontheedge.be5.operation.databasemodel.impl.EntityModelSQLException;
import com.developmentontheedge.be5.operation.databasemodel.impl.TableAlreadyExistsException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;



/**
 * 
 * All methods, which not generate RecordModel, not use caches!<br>
 * All methods throws EntityModelException if the method have internal error
 * EntityModel are generally not synchronized. It is recommended to create separate 
 * instances for each thread. If multiple threads access a format concurrently, 
 * it must be synchronized externally.
 * @author ruslan
 */
public interface EntityModel<R extends RecordModel> {

    /**
     * Returns the number of records a table.
     * This method never use cache.
     * @return number of records
     * @throws EntityModelSQLException if obtaining the size threw exception
     */
    @Deprecated
    default int size() { return this.count(); };

    /**
     * Returns the number of records a table.
     * This method never use cache.
     * @return number of records
     * @throws EntityModelSQLException if obtaining the size threw exception
     */
    int count();

    /**
     * Returns the number of records a table.
     * This method never use cache.
     * @return number of records
     * @throws EntityModelSQLException if obtaining the size threw exception
     */
    int count(Map<String, String> values);

    /**
     * Returns <tt>true</tt> if this table contains no records.
     * This method never use caches.
     * @return <tt>true</tt> if this table contains no records
     */
    boolean isEmpty();
    
    /**
     * Returns <tt>true</tt> if entity contains record consistent with the  
     * specified condition.
     * This method never use caches.
     * @param values condition values
     * @return <tt>true</tt> if entity contains record consistent with 
     * conditions, otherwise false
     */
    boolean contains(Map<String, String> values);
    
    /**
     * Adds record into database from map, where key is the column name
     * and key value is the column value.<br>
     * The method can check the values on consistency and threw exceptions<br>
     * in order to avoid compromising the integrity of the database.
     * This method calls {@link #addForce( Map )}
     * @param values map with column names and values
     * @return generated record identify number
     */
    Long add(Map<String, String> values);
    
    /**
     * Adds record into database from map, where key is the column name
     * and key value is the column value.<br>
     * This method may not contain any checks, it's just the method implementation.
     * @param values map with column names and values
     * @return generated record identify number
     */
    Long addForce(Map<String, String> values);
    
    /**
     * Returns <tt>true</tt> if entity contains record consistent with the  
     * all specified condition in collection otherwise <tt>false</tt> 
     * @param c collection of conditions
     * @return <tt>true</tt> if entity contains record consistent with the  
     * all specified condition 
     */
    boolean containsAll(Collection<Map<String, String>> c);
    
    /**
     * Adds all records from collection into database.
     * @param c collection with column names and values
     * @return list with record identify numbers 
     */
    List<Long> addAll(Collection<Map<String, String>> c);

    /** 
     * Returns the record object with the specified id
     * @param id value of primary key
     * @return the record object with the specified id otherwise null
     */
    R get(Long id);

    /** 
     * Returns the record object consistent with the specified condition, 
     * where key is the column name with the value equals map key value 
     * @param values condition values
     * @return the record object with the specified id otherwise null
     */
    R get(Map<String, String> values);
    
    /**
     * Sets value to property with a specified name.<br>
     * The method can check the values on consistency and threw exceptions<br>
     * in order to avoid compromising the integrity of the database.
     * This method calls {@link #setForce( Long, String, String )}
     * @param id identify number of record
     * @param propertyName column name
     * @param value new value
     */
    void set(Long id, String propertyName, String value);

    /**
     * Sets value to property with a specified name.<br>
     * The method can check the values on consistency and threw exceptions<br>
     * in order to avoid compromising the integrity of the database.
     * This method calls {@link #setForce( Long, Map )}
     * @param id identify number of record
     * @param values column names and values
     */
    void set(Long id, Map<String, String> values);
    
    /**
     * Sets value to property with a specified name.<br>
     * This method may not contain any checks, it's just the method implementation.
     * This method calls {@link #setForce( Long, Map )}
     * @param id identify number of record
     * @param propertyName column name
     * @param value new value
     */
    void setForce(Long id, String propertyName, String value);

    /**
     * Sets value to property with a specified name.<br>
     * This method may not contain any checks, it's just the method implementation.
     * @param id identify number of record
     * @param values new column names and values
     */
    void setForce(Long id, Map<String, String> values);
    
    /**
     * Operation removes all the records consistent with any of conditions in collection.
     * The method can check the values on consistency and threw exceptions<br>
     * in order to avoid compromising the integrity of the database.
     * This method calls {@link #removeForce(Long, Long...)}
     * @param c collection of conditions
     * @return <tt>true</tt> if all conditions has been used otherwise <tt>false</tt>
     */
    int removeAll(Collection<Map<String, String>> c);
    
    /**
     * Operation removes all the records, consistent with conditions.
     * The method can check the values on consistency and threw exceptions<br>
     * in order to avoid compromising the integrity of the database.
     * This method calls {@link #removeForce(Long, Long...)} }
     * @param values conditions
     * @return count of deleted records
     */
    int remove(Map<String, String> values);
    
//    /**
//     * Deletes the record with the specified identifier.
//     * The method can check the values on consistency and threw exceptions<br>
//     * in order to avoid compromising the integrity of the database.
//     * This method calls {@link #removeForce( String )}
//     * @param id - record identifier number
//     * @return <tt>true</tt> if the record has been deleted otherwise <tt>false<tt>
//     */
//    int remove( String id );
    
    /**
     * Deletes the record with the specified identifiers.
     * The method can check the values on consistency and threw exceptions<br>
     * in order to avoid compromising the integrity of the database.
     * This method calls {@link #removeForce(Long, Long...)}
     * @param id - record identifier numbers
     * @return <tt>true</tt> if the all record has been deleted otherwise <tt>false<tt>
     */
    int remove(Long id, Long... otherId);

//    /**
//     * Deletes the record with the specified identifier.<br>
//     * This method may not contain any checks, it's just the method implementation.
//     * @param id identify number of record
//     * @param values new column names and values
//     */
//    int removeForce( String id );

    /**
     * Deletes the record with the specified identifiers.<br>
     * This method may not contain any checks, it's just the method implementation.
     * @param firstId first identify number of record
     * @param otherId other identify number of record
     */
    int removeForce(Long firstId, Long... otherId);

    /**
     * Returns a list of records of current entity.
     * @return list of records
     */
    List<R> toList();

    List<R> collect();

    /**
     * Returns a array of records of current entity.
     * @return array of records
     */
    RecordModel[] toArray();

    /**
     * Returns a list of records of current entity filtered by the specified parameters.
     * @param values the filter parameters
     * @return array of records
     */
    List<R> toList(Map<String, String> values);
    
    /**
     * Returns a array of records of current entity filtered by the specified parameters.
     * @param values the filter parameters
     * @return array of records
     */
    RecordModel[] toArray(Map<String, String> values);

    /**
     * Spreads collection and collect elements from function to list.<br>
     * For example:<br>
     * <code>List<DynamicPropertySet> list = 
     *      entity.<DynamicPropertySet>collect( ( bean, row ) -> row % 2 == 0 ? bean : null, Collections.<String, Object>.emptyMap() );
     * </code>
     * @param values condition values
     * @param lambda handler
     * @return list with the function results
     */
	<T> List<T> collect(Map<String, String> values, BiFunction<R, Integer, T> lambda);

    /**
     * Returns entity name.
     * @return entity name
     */
    String getEntityName();

    /**
     * Returns primary key of entity table.
     * @return primary key
     */
    String getPrimaryKeyName();

    /**
     * Returns real table name of entity.
     * @return table name
     */
    String getTableName();

//    /**
//     * Returns table cloned id post-fix
//     * For example, if entity named persons,
//     * and table cloned id is 2, table name will be called persons2
//     * @return table cloned id post-fix
//     */
//    String getTcloneId();

    /**
     * Creates cloned table. Cloned table - table with entity name and prefix.
     * For example: if entity name is Entity, and tcloneId is 2, table name will be called with Entity2
     * There will no effect, if tcloneId is empty
     * @throws TableAlreadyExistsException - if cloned table already exists
     */
    default void makeClonedTable() throws TableAlreadyExistsException
    {
        makeClonedTable( true );
    }

    /**
     * Creates cloned table. Cloned table - table with entity name and prefix.
     * For example: if entity name is Entity, and tcloneId is 2, table name will be called with Entity2
     * There will no effect, if tcloneId is empty
     * @param cloneIndexes - if true - clone original indexes
     * @throws TableAlreadyExistsException - if cloned table already exists
     */
    void makeClonedTable(boolean cloneIndexes) throws TableAlreadyExistsException;

    /**
     * Drops if exists and creates cloned table. Cloned table - table with entity name and prefix.
     * For example: if entity name is Entity, and tcloneId is 2, table name will be called with Entity2
     * There will no effect, if tcloneId is empty
     */
    // default void recreateClonedTable() { 
    //     dropClonedTable();
    //     makeClonedTable();
    // }

    /**
     * Drops cloned table. Cloned table - table with entity name and prefix.
     * For example: if entity name is Entity, and tcloneId is 2, table name will be called with Entity2
     * There will no effect, if tcloneId is empty or table is not exists
     * @return true if table has been dropped by this operation, 
     * false if table not exists or.tcloneId is null.
     */
    boolean dropClonedTable();

//    /**
//     * Returns true if current table exists.
//     * This method allows table cloned post-fix.
//     * @return true if table exists, otherwise false
//     */
//    boolean isTableExists();

    /**
     * Returns query model of this entity.
     * @param queryName
     * @return query model
     */
    QueryModel getQuery(String queryName);
    
    /**
     * Returns query model of this entity with the given parameters.
     * @param queryName
     * @param params
     * @return query model
     */
    QueryModel getQuery(String queryName, Map<String, String> params);

    /**
     * Returns operation model of this entity.
     * @param operationName
     * @return query model
     */
    OperationModel getOperation(String operationName);

    Long leftShift( Map<String, String> values );

}
