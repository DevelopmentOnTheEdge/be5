package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.MetadataUtils;
import com.developmentontheedge.be5.server.services.users.RoleHelper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class RoleHelperImpl implements RoleHelper
{
    private final DbService db;
    private final CoreUtils coreUtils;

    @Inject
    public RoleHelperImpl(DbService db, CoreUtils coreUtils)
    {
        this.db = db;
        this.coreUtils = coreUtils;
    }

    @Override
    public void updateCurrentRoles(String userName, Collection<String> roles)
    {
        coreUtils.setUserSetting(userName, DatabaseConstants.CURRENT_ROLE_LIST,
                MetadataUtils.toInClause(roles));
    }

    @Override
    public List<String> getCurrentRoles(String userName)
    {
        String readCurrentRoles = coreUtils.getUserSetting(userName, DatabaseConstants.CURRENT_ROLE_LIST);
        List<String> roles = parseRoles(readCurrentRoles);
        if (roles.size() > 0)
        {
            return roles;
        }
        else
        {
            return getAvailableRoles(userName);
        }
    }

    @Override
    public List<String> getAvailableRoles(String userName)
    {
        return db.scalarList("SELECT role_name FROM user_roles WHERE user_name = ?", userName);
    }

    List<String> parseRoles(String roles)
    {
        TreeSet<String> rolesList = new TreeSet<>();
        if (roles == null || "()".equals(roles))
        {
            return Collections.emptyList();
        }
        roles = roles.substring(1, roles.length() - 1); // drop starting and trailing '(' ')'
        StringTokenizer st = new StringTokenizer(roles, ",");
        while (st.hasMoreTokens())
        {
            rolesList.add(st.nextToken().trim().replaceAll("'", ""));
        }
        return new ArrayList<>(rolesList);
    }
}
