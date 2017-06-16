package com.developmentontheedge.be5.operation.databasemodel.groovy;

import com.google.common.collect.Iterables;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.MissingMethodException;
import com.developmentontheedge.be5.annotations.Experimental;
import org.codehaus.groovy.runtime.InvokerHelper;


import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Created by ruslan on 14.09.16.
 */
@Experimental
public class ExtensionMethodsMetaClass extends DelegatingMetaClass
{
    protected ExtensionMethodsMetaClass(Class classForExtension )
    {
        super( classForExtension );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Object invokeMethod( Object object, String methodName, Object[] args )
    {
        try
        {
            return InvokerHelper.invokeMethod( getClass(), methodName, Iterables.concat(singletonList(object), asList(args)));
        }
        catch( MissingMethodException e ) { /*missing method in meta-class*/ }

        return super.invokeMethod( object, methodName, args );
    }

}