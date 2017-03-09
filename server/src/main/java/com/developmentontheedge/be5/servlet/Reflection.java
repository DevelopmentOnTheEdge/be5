package com.developmentontheedge.be5.servlet;

import java.lang.reflect.Method;

public class Reflection {
    
    /**
     * Not intended to be instantiated.
     */
    private Reflection() {
    }
    
    public static class DynamicObject {
        
        private final Object object;
        private final Class<?> klass;

        private DynamicObject(Object object, Class<?> klass) {
            if (klass == null)
                throw new NullPointerException();
            this.object = object;
            this.klass = klass;
        }
        
        public Object call(String methodName, Object... args) throws Exception {
            Method[] methods = klass.getMethods();
            
            for (Method method : methods)
            {
                if (method.getName().equals(methodName) && method.getParameterTypes().length == args.length)
                {
                    return method.invoke(object, args);
                }
            }
            
            return null;
        }
        
    }
    
    public static DynamicObject on(Class<?> klass) {
        return new DynamicObject(null, klass);
    }
    
    public static DynamicObject on(Object object) {
        return new DynamicObject(object, object.getClass());
    }
    
}
