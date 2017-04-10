package com.developmentontheedge.be5.legacy;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.QueryStrings;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyUrlParser {
    public static final String GO_BACK_URL = "#goback";
    public static final String GO_REFRESH_ALL = "#goRefreshAll";
    private static final Pattern REDIRECT_PATTERN = Pattern.compile("(.+?)\\.redir(\\?(.*))?");
    private final String value;
    private boolean parsed = false;
    private String entityName = null;
    private String queryName = null;
    private String operationName = null;
    private Map<String, String> parameters = null;
    private Optional<Integer> queryNumber = Optional.empty();
    
    private final LegacyQueryRepository queryRepository;
    
    public LegacyUrlParser(LegacyQueryRepository queryRepository, String value)
    {
        this.queryRepository = queryRepository;
        this.value = value.trim();
    }
    
    public boolean isLegacy() {
		return isLegacyOperation() || isLegacyQuery() || isLegacyRedirect();
    }
    
    private boolean isLegacyOperation()
    {
        return value.startsWith("o?");
    }

    private boolean isLegacyQuery()
    {
        return value.startsWith("q?");
    }
    
    private boolean isLegacyRedirect()
    {
        return REDIRECT_PATTERN.matcher(value).matches();
    }
    
    /**
     * Returns whether the static query describes a correct action.
     * Must not be called without calling of the {@link LegacyUrlParser#isLegacy()}.
     * 
     * @throws IllegalStateException if isLegacy() hasn't been called
     */
    public boolean isValid() {
        parse();
        return entityName != null;
    }
    
    public String getEntityName() {
        parse();
        return entityName;
    }
    
    public String getQueryName() {
        parse();
        return queryName;
    }
    
    public String getOperationName() {
        parse();
        return operationName;
    }
    
    public Map<String, String> getParameters() {
        parse();
        return parameters;
    }

    private void parse() {
        if (parsed)
            return;

        if (isLegacyOperation())
        {
            Map<String, String> allParameters = Maps.newHashMap(QueryStrings.parse(value.substring(2)));
            if (allParameters.containsKey("_t_"))
            {
                entityName = allParameters.remove("_t_");
            }
            parse(allParameters);
            parsed = true;
            return;
        }
        else if (isLegacyQuery())
        {
            Map<String, String> allParameters = Maps.newHashMap(QueryStrings.parse(value.substring(2)));
            if (allParameters.containsKey("_t_"))
            {
                entityName = allParameters.remove("_t_");
            }
            parse(allParameters);
            parsed = true;
            return;
        }
        else
        {
            Matcher matcher = REDIRECT_PATTERN.matcher(value);
            if (matcher.matches())
            {
                entityName = matcher.group(1);
                Map<String, String> parameters;
                if (matcher.group(3) != null)
                    parameters = QueryStrings.parse(matcher.group(3));
                else
                    parameters = Collections.<String, String>emptyMap();
                parse(parameters);
                parsed = true;
                return;
            }
        }

        throw new IllegalStateException();
    }

    /**
     * Parses all known parameters excluding the entity name.
     */
    private void parse(Map<String, String> allParametersImmutable) {
        Map<String, String> mutableMap = Maps.newHashMap(allParametersImmutable);
        
        if (mutableMap.containsKey("_qn_"))
        {
            queryName = mutableMap.remove("_qn_").replace('+', ' ');
        }
        
        if (mutableMap.containsKey("_on_"))
        {
            operationName = mutableMap.remove("_on_").replace('+', ' ');
        }
        
        if (queryName == null && queryName == null && mutableMap.containsKey("_q_"))
        {
            int queryId = Integer.parseInt(mutableMap.remove("_q_"));
            queryNumber = Optional.of(queryId);
            //TODO QueryLink queryLink = queryRepository.findOne(queryNumber.get()).orElseThrow(getExceptionSupplierForNotFoundQuery(queryId)).toQueryLink();
//            queryName = queryLink.getQueryName();
//            entityName = queryLink.getEntityName();
        }
        
        mutableMap.remove("_t_");
        mutableMap.remove("_q_");
        mutableMap.remove("_enc_"); // isn't supported
        parameters = Collections.unmodifiableMap(mutableMap);
    }
    
    private Supplier<RuntimeException> getExceptionSupplierForNotFoundQuery(int queryNumber)
    {
        return () -> new RuntimeException("Can't convert a legacy URL because there's no query #" + queryNumber + " in the '" + MetaTables.QUERIES + "' table");
    }
    
    /**
     * Transforms a legacy URL to the modern representation.
     * 
     * @throws IllegalStateException when the given URL is not legacy
     * @return an URL, it never returns null
     */
    public HashUrl modernize()
    {
        if(!isLegacy())
            throw new IllegalStateException();
        
        try
        {
            String allRecords = DatabaseConstants.ALL_RECORDS_VIEW;
            
            if (getOperationName() == null)
            {
                return new HashUrl(FrontendConstants.TABLE_ACTION, getEntityName(), Optional.ofNullable(getQueryName()).orElse(allRecords)).named(getParameters());
            }
            
            return new HashUrl(FrontendConstants.FORM_ACTION, getEntityName(), Optional.ofNullable(getQueryName()).orElse(allRecords), getOperationName()).named(getParameters());
        }
        catch( NullPointerException e )
        {
            throw Be5Exception.internal( e, "Error parsing URL "+value );
        }
    }
    
    public boolean isForm()
    {
        parse();
        return !Strings.isNullOrEmpty(entityName) && !Strings.isNullOrEmpty(queryName) && !Strings.isNullOrEmpty(operationName);
    }
    
}