package com.developmentontheedge.be5.components;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.developmentontheedge.be5.DatabaseConstants;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.developmentontheedge.be5.api.helpers.UserInfoManager;
import com.developmentontheedge.be5.metadata.model.Project;

import one.util.streamex.StreamEx;

public class LanguageSelector implements Component
{

    static class LanguageSelectorResponse
    {

        public final List<String> languages;
        public final String selected;
        public final Map<String, String> messages;

        public LanguageSelectorResponse(List<String> languages, String selected, Map<String, String> messages)
        {
            this.languages = languages;
            this.selected = selected;
            this.messages = messages;
        }
        
    }
    
    /* cache */
    private List<String> languages = null;

    public LanguageSelector()
    {
        /* Should be stateless, but we use some caches. */
    }
    
    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        switch( req.getRequestUri() )
        {
        case "":
            res.sendAsRawJson(getInitialData(req, serviceProvider));
            return;
        case "select":
            res.sendAsRawJson(selectLanguage(req, serviceProvider));
            return;
        default:
            res.sendUnknownActionError();
            return;
        }
    }

    private LanguageSelectorResponse getInitialData(Request req, ServiceProvider serviceProvider)
    {
        return getState(req, serviceProvider);
    }

    private LanguageSelectorResponse selectLanguage(Request req, ServiceProvider serviceProvider)
    {
        String language = req.get("language");

        if( language == null )
            throw Be5ErrorCode.PARAMETER_ABSENT.exception( "language" );

        UserInfoManager.get(req, serviceProvider).changeLanguage(language);

        return getState(req, serviceProvider);
    }

    private LanguageSelectorResponse getState(Request req, ServiceProvider serviceProvider)
    {
        Project project = serviceProvider.getProject();
        
        if( languages == null )
        {
            languages = Arrays.stream( project.getLanguages() ).map( String::toUpperCase ).collect(Collectors.toList());
        }
        
        UserInfoManager userInfoManager = UserInfoManager.get(req, serviceProvider);
        String language = userInfoManager.getLanguage();
        String selected = language.toUpperCase();
        Map<String, String> messages = readMessages(project, language);
        
        return new LanguageSelectorResponse(languages, selected, messages);
    }

    private Map<String, String> readMessages(Project project, String language)
    {
        Map<String, String> messages = new LinkedHashMap<>();
        
        StreamEx.of(project.getModulesAndApplication())
            .map( m -> m.getLocalizations().get( language )).nonNull()
            .map( ll -> ll.get( "javascript" ) ).nonNull()
            .flatMap( el -> el.getRows().stream() )
            .filter( row -> row.getTopic().equals( DatabaseConstants.L10N_TOPIC_PAGE ) )
            .forEach( row -> messages.put( row.getKey(), row.getValue() ) );
        
        return messages;
    }
    
}
