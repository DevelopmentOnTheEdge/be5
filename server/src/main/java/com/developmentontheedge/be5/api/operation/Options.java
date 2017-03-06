package com.developmentontheedge.be5.api.operation;

import java.util.List;

import com.developmentontheedge.be5.api.experimental.v1.DynamicPropertyAttributes;

import one.util.streamex.StreamEx;

public class Options
{

    /**
     * Forms a list of options by a enumeration. Note that the simplest way to
     * form a combo box by a enumeration is to put the enumeration class as an
     * attribute to the property.
     * 
     * @see DynamicPropertyAttributes#SELECT_OPTIONS
     */
    public static List<Option> of(Class<? extends Enum<?>> class1)
    {
        return StreamEx.of(class1.getEnumConstants())
                .map(enumConstant -> new Option(enumConstant.name(), enumConstant.name()))
                .toList();
    }
    
}
