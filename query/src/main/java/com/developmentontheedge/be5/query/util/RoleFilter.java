package com.developmentontheedge.be5.query.util;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RoleFilter
{
    public static void filterBeanWithRoles(DynamicPropertySet dps, List<String> currentRoles)
    {
        for (Iterator<DynamicProperty> props = dps.propertyIterator(); props.hasNext();)
        {
            DynamicProperty prop = props.next();
            Map<String, String> info = DynamicPropertyMeta.get(prop).get(DatabaseConstants.COL_ATTR_ROLES);
            if (info == null)
            {
                continue;
            }

            String roles = info.get("name");
            List<String> roleList = Arrays.asList(roles.split(","));
            List<String> forbiddenRoles = new ArrayList<>();
            for (String userRole : roleList)
            {
                if (userRole.startsWith("!"))
                {
                    forbiddenRoles.add(userRole.substring(1));
                }
            }
            roleList.removeAll(forbiddenRoles);

            boolean hasAccess = false;
            for (String role : roleList)
            {
                if (currentRoles.contains(role))
                {
                    hasAccess = true;
                    break;
                }
            }
            if (!hasAccess && !forbiddenRoles.isEmpty())
            {
                for (String currRole : currentRoles)
                {
                    if (!forbiddenRoles.contains(currRole))
                    {
                        hasAccess = true;
                        break;
                    }
                }
            }
            if (!hasAccess)
            {
                prop.setHidden(true);
            }
        }
    }
}
