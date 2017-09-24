package com.developmentontheedge.be5.modules.core.genegate;

import com.developmentontheedge.be5.modules.core.genegate.entities.Provinces;
import groovy.lang.Closure;

import java.io.Serializable;
import java.util.Map;


public interface Repository<T, ID extends Serializable>
{
    ID add(final Closure config);

    //todo <S extends T> S save(S entity);

    T findOne(ID primaryKey);

    T findFirst(final Closure config);

    Iterable<T> findAll();

    Iterable<T> findAll(final Closure config);

    Long count();

    Long count(final Closure config);

//    void remove(T entity);

    void remove(ID primaryKey);

//    void remove(Iterable<? extends ID> ids);

    void removeAll();

    void removeAll(final Closure config);

    boolean exists(ID primaryKey);

    Map<String, Object> toMap(final Closure config);

    Map<String, Object> toMap(Provinces entity);
}