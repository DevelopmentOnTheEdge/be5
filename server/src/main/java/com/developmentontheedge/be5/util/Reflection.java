package com.developmentontheedge.be5.util;

import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Deprecated
public class Reflection {
//
//    /**
//     * Not intended to be instantiated.
//     */
//    private Reflection() {
//    }
//
//    public static class DynamicObject {
//
//        private final Object object;
//        private final Class<?> klass;
//
//        private DynamicObject(Object object, Class<?> klass) {
//            checkNotNull(klass);
//            this.object = object;
//            this.klass = klass;
//        }
//
//        public Object unwrap() {
//        	return object;
//        }
//
//        public Object call(String methodName, Object... args) throws IllegalArgumentException, InvocationTargetException
//        {
//            checkNotNull(methodName);
//            Method method = getMethod(methodName, args.length);
//
//            try
//            {
//                return method.invoke(object, args);
//            }
//            catch (IllegalAccessException e)
//            {
//                throw new IllegalArgumentException(e);
//            }
//            catch (IllegalArgumentException e)
//            {
//                throw new AssertionError("Internal error", e);
//            }
//
//        }
//
//        private Method getMethod(String methodName, int nArgs) throws IllegalArgumentException {
//            for (Method method : klass.getMethods())
//            {
//                if (method.getName().equals(methodName) && method.getParameterTypes().length == nArgs)
//                {
//                    return method;
//                }
//            }
//
//            throw new IllegalArgumentException("Unknown method '" + methodName + "/"+ nArgs + "' in '" + klass.getName() + "'");
//        }
//
//        public Object get(String fieldName) throws IllegalArgumentException {
//            checkNotNull(fieldName);
//
//            try
//            {
//                Field field = klass.getDeclaredField(fieldName);
//                field.setAccessible(true);
//                return field.get(object);
//            }
//            catch (NoSuchFieldException | SecurityException | IllegalAccessException e)
//            {
//                throw new IllegalArgumentException(e);
//            }
//            catch (IllegalArgumentException e)
//            {
//                throw new AssertionError("Internal error", e);
//            }
//        }
//
//    }
//
//    public static DynamicObject on(Class<?> klass) {
//        return new DynamicObject(null, klass);
//    }
//
//    public static DynamicObject on(Object object) {
//        return new DynamicObject(object, object.getClass());
//    }
//
}
