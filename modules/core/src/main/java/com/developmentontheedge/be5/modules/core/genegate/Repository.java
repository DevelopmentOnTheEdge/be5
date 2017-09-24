package com.developmentontheedge.be5.modules.core.genegate;

import groovy.lang.Closure;

import java.io.Serializable;


public interface Repository<T, ID extends Serializable>
{
    ID add(final Closure config);

    //<S extends T> S save(S entity);

    T findOne(ID primaryKey);
//
//    Iterable<T> findAll();
//
//    Long count();
//
//    void delete(T entity);
//
//    boolean exists(ID primaryKey);
}