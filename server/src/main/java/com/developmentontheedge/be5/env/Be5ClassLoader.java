package com.developmentontheedge.be5.env;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;

public class Be5ClassLoader extends ClassLoader {
    
    public Be5ClassLoader() {
        super();
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try
        {
            return Classes.tryLoad(name, Object.class);
        }
        catch (Be5Exception e)
        {
            throw new ClassNotFoundException("Can't find a class '" + name + "'", e);
        }
    }
    
}
