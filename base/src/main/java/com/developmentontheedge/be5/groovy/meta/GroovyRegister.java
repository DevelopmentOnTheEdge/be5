package com.developmentontheedge.be5.groovy.meta;

import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class GroovyRegister
{
    public static void registerMetaClass(Class<? extends MetaClass> metaClazz, Class<?> clazz)
    {
        try
        {
            Constructor<? extends MetaClass> constructor = metaClazz.getDeclaredConstructor(Class.class);
            MetaClass metaClass;
            try
            {
                metaClass = constructor.newInstance(clazz);
            }
            catch (InstantiationException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
            metaClass.initialize();
            InvokerHelper.getMetaRegistry().setMetaClass(clazz, metaClass);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }
}
