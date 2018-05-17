package com.developmentontheedge.be5.util.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE } )
@Retention( RetentionPolicy.SOURCE )
public @interface DirtyRealization 
{
    String comment() ;
}