package com.developmentontheedge.be5.components.impl.model;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.developmentontheedge.be5.DatabaseConnector;
import com.developmentontheedge.be5.OperationSupport;
import com.developmentontheedge.be5.UserInfo;
import com.developmentontheedge.be5.Utils;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.util.MoreStrings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * 
 * @deprecated This class should not be used as it reads localized messages from the database.
 * @author asko
 */
@Deprecated
class TableLocalizer
{

    private static final Pattern MESSAGE_PATTERN = MoreStrings.variablePattern(OperationSupport.LOC_MSG_PREFIX, OperationSupport.LOC_MSG_POSTFIX);
    private final Function<String, String> getLocalizedMessage;
    
    public TableLocalizer(Query query, UserInfo user, DatabaseConnector connector)
    {
        final Map<String, String> localizedMessages = prepareLocalizedMessages(query, user, connector);
        this.getLocalizedMessage = message -> localizedMessages.getOrDefault(message, message);
    }
    
    private static Map<String, String> prepareLocalizedMessages(Query query, UserInfo user, DatabaseConnector connector) {
        try
        {
            return Utils.readQueryMessages(query.getEntity().getName(), query.getName(), connector, user);
        }
        catch (SQLException e)
        {
            throw Be5Exception.internalInQuery(e, query);
        }
    }
    
    /**
     * Localizes each string's content substituting localizations to found placeholders.
     */
    public List<String> localize(Iterable<String> strings)
    {
        Builder<String> localized = ImmutableList.builder();

        for (String string : strings)
        {
            localized.add(localize(string));
        }

        return localized.build();
    }
    
    /**
     * Localizes the given string content substituting localizations to found placeholders.
     */
    public String localize(String string)
    {
        return MoreStrings.substituteVariables(string, MESSAGE_PATTERN, getLocalizedMessage);
    }
    
}
