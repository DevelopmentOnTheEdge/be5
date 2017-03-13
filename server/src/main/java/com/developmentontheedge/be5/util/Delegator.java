package com.developmentontheedge.be5.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.util.Reflection.DynamicObject;

/**
 * Proxy.
 * 
 * @author asko
 */
public class Delegator implements InvocationHandler {

    private final Object target;

    private Delegator(Object target) {
        this.target = target;
    }
    
    /**
     * Creates a proxy if given class is interface,
     * otherwise tries to create a copy of the given bean.
     */
    public static <T> T on(Object target, Class<T> klass) {
        if (target == null)
            return null;
        if (!klass.isInterface())
        {
            return copy(target, klass);
        }
		@SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(klass.getClassLoader(), new Class[] { klass }, new Delegator(target));
        return proxy;
    }
    
    // TODO Note that this method is not fully implemented,
    // it can't copy anything expect cookies. So you can add some copying here.
    @SuppressWarnings("unchecked")
    private static <T> T copy(Object target, Class<T> klass) {
        if (klass == Cookie.class)
        {
            try
            {
                return (T) new ForwardingCookie(Reflection.on(target));
            }
            catch (IllegalArgumentException | InvocationTargetException e)
            {
                throw new AssertionError("Internal error", e);
            }
        }
        if (klass == ServletOutputStream.class)
        {
        	return (T) new ForwardingServletOutputStream((OutputStream)target);
        }
        
        throw new IllegalArgumentException(klass.toString());
    }
    
    private interface WrappedObject {
    	public Object unwrap();
    }
    
    private static class ForwardingServletOutputStream extends ServletOutputStream implements WrappedObject {
		private final OutputStream target;

		public ForwardingServletOutputStream(OutputStream target) {
			this.target = target;
		}

		@Override
		public void write(int b) throws IOException {
			target.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			target.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			target.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			target.flush();
		}

		@Override
		public void close() throws IOException {
			target.close();
		}

		@Override
		public Object unwrap() {
			return target;
		}
    	
    }
    
    @SuppressWarnings("serial")
	private static class ForwardingCookie extends Cookie implements WrappedObject {
    	private final DynamicObject dynamic;

		public ForwardingCookie(DynamicObject dynamic) throws IllegalArgumentException, InvocationTargetException {
	        super((String) dynamic.call("getName"), "");
	        this.dynamic = dynamic;
		}

		@Override
		public String getComment() {
			try {
				return (String) dynamic.call("getComment");
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public String getDomain() {
			try {
				return (String) dynamic.call("getDomain");
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public int getMaxAge() {
			try {
				return (int) dynamic.call("getMaxAge");
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public String getPath() {
			try {
				return (String) dynamic.call("getPath");
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public boolean getSecure() {
			try {
				return (boolean) dynamic.call("getSecure");
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public String getValue() {
			try {
				return (String) dynamic.call("getValue");
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public int getVersion() {
			try {
				return (int) dynamic.call("getVersion");
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public boolean isHttpOnly() {
			try {
				return (boolean) dynamic.call("isHttpOnly");
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void setComment(String purpose) {
			try {
				dynamic.call("setComment", purpose);
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void setDomain(String pattern) {
			try {
				dynamic.call("setDomain", pattern);
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void setHttpOnly(boolean httpOnly) {
			try {
				dynamic.call("setHttpOnly", httpOnly);
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void setMaxAge(int expiry) {
			try {
				dynamic.call("setMaxAge", expiry);
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void setPath(String uri) {
			try {
				dynamic.call("setPath", uri);
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void setSecure(boolean flag) {
			try {
				dynamic.call("setSecure", flag);
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void setValue(String newValue) {
			try {
				dynamic.call("setValue", newValue);
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void setVersion(int v) {
			try {
				dynamic.call("setVersion", v);
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public Object unwrap() {
			return dynamic.unwrap();
		}
    }
    
    @Override
    public Object invoke(Object object, Method method, Object[] arguments) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), convertParameterTypes(target.getClass().getClassLoader(), method.getParameterTypes()));
        Object result = targetMethod.invoke(target, convertParameters(arguments));

        if (result == null)
        {
            return null;
        }
        
        /*
         * The target method can return an object that wasn't
         * loaded with the default class loader, so it couldn't be
         * casted to method.getReturnType().
         * In this case we wraps the result using a new proxy.
         */
        Class<?> returnType = method.getReturnType();

        if (returnType.equals(Void.TYPE) || returnType.isPrimitive() || returnType == targetMethod.getReturnType() || returnType.isInstance(result))
        {
            return result;
        }
        
        if (returnType.isArray())
        {
            Class<?> componentType = returnType.getComponentType();
            int length = Array.getLength(result);
            Object proxyArray = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++)
            {
                Object targetComponent = Array.get(result, i);
                Array.set(proxyArray, i, on(targetComponent, componentType));
            }
            return proxyArray;
        }

        return on(result, returnType);
    }

	private Object[] convertParameters(Object[] arguments) {
		if(arguments == null)
		{
			return null;
		}
		for(int i=0; i<arguments.length; i++) 
		{
			if(arguments[i] instanceof WrappedObject)
			{
				arguments[i] = ((WrappedObject)arguments[i]).unwrap();
			}
			if(arguments[i] instanceof CloseReason)
			{
			    try
                {
                    CloseReason closeReason = (CloseReason)arguments[i];
                    Class<?> closeReasonClass = target.getClass().getClassLoader().loadClass( CloseReason.class.getName() );
                    Class<?> closeCodeClass = target.getClass().getClassLoader().loadClass( CloseCode.class.getName() );
                    arguments[i] = closeReasonClass.getConstructor( closeCodeClass, String.class )
                        .newInstance( Delegator.on( closeReason.getCloseCode(), closeCodeClass ), closeReason.getReasonPhrase() );
                }
                catch( ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e )
                {
                    throw Be5Exception.internal( e );
                }
			}
		}
		return arguments;
	}

	private Class<?>[] convertParameterTypes(ClassLoader cl, Class<?>[] parameterTypes) throws ClassNotFoundException {
		if(cl == null) {
			return parameterTypes;
		}
		for(int i=0; i<parameterTypes.length; i++)
		{
			Class<?> parameterType = parameterTypes[i];
			if(!parameterType.isPrimitive() && parameterType.getClassLoader() != null)
			{
				parameterTypes[i] = cl.loadClass(parameterType.getName());
			}
		}
		return parameterTypes;
	}
    
}
