package com.developmentontheedge.be5.query.impl.model;

import java.util.List;

import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.util.Collections3;
import com.developmentontheedge.be5.util.HashLink;
import com.developmentontheedge.be5.util.HashLinks;

public class Operations
{
    
    private Operations()
    {
        // not intended to be instantiated
    }

    public static boolean isClientSide(Operation operation)
    {
        return operation.getType().equals(Operation.OPERATION_TYPE_JAVASCRIPT) && HashLinks.isIn(operation.getCode());
    }
    
    public static HashLink asClientSide(Operation operation)
    {
        return HashLink.parse(operation.getCode());
    }

    /**
     * Transforms operation.getRecords() integer "enumeration" to a string: "always", "oneSelected", "anySelected" or "hasRecords".
     */
    public static String determineWhenVisible(Operation operation) {
        switch (operation.getRecords())
        {
        case Operation.VISIBLE_ALWAYS:
        case Operation.VISIBLE_ALL_OR_SELECTED:
            return "always";
        case Operation.VISIBLE_WHEN_ONE_SELECTED_RECORD:
            return "oneSelected";
        case Operation.VISIBLE_WHEN_ANY_SELECTED_RECORDS:
            return "anySelected";
        case Operation.VISIBLE_WHEN_HAS_RECORDS:
            return "hasRecords";
        default:
            throw new AssertionError();
        }
    }

    public static boolean isAllowed(Operation operation, List<String> userRoles)
    {
        return Collections3.containsAny(userRoles, operation.getRoles().getFinalRoles());
    }
    
}
