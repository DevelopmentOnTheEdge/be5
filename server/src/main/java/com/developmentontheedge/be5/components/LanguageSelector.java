package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Project;
import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


public class LanguageSelector implements Component
{

    public static class LanguageSelectorResponse
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

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LanguageSelectorResponse that = (LanguageSelectorResponse) o;

            if (languages != null ? !languages.equals(that.languages) : that.languages != null) return false;
            if (selected != null ? !selected.equals(that.selected) : that.selected != null) return false;
            return messages != null ? messages.equals(that.messages) : that.messages == null;
        }

        public List<String> getLanguages()
        {
            return languages;
        }

        public String getSelected()
        {
            return selected;
        }

        public Map<String, String> getMessages()
        {
            return messages;
        }

        @Override
        public String toString()
        {
            return "LanguageSelectorResponse{" +
                    "languages=" + languages +
                    ", selected='" + selected + '\'' +
                    ", messages=" + messages +
                    '}';
        }
    }

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        switch( req.getRequestUri() )
        {
        case "":
            res.sendAsRawJson(getInitialData(injector));
            return;
        case "select":
            res.sendAsRawJson(selectLanguage(req, injector));
            return;
        default:
            res.sendUnknownActionError();
        }
    }

    private LanguageSelectorResponse getInitialData(Injector injector)
    {
        return getState(injector);
    }

    private LanguageSelectorResponse selectLanguage(Request req, Injector injector)
    {
        Locale language = injector.get(Meta.class).getLocale(new Locale(req.getNonEmpty("language")));
        UserInfoHolder.getUserInfo().setLocale(language);

        return getState(injector);
    }

    private LanguageSelectorResponse getState(Injector injector)
    {
        Project project = injector.get(ProjectProvider.class).getProject();

        List<String> languages = Arrays.stream(project.getLanguages()).map(String::toUpperCase).collect(Collectors.toList());

        String selectedLanguage = UserInfoHolder.getLanguage().toUpperCase();
        Map<String, String> messages = readMessages(project, selectedLanguage);
        
        return new LanguageSelectorResponse(languages, selectedLanguage, messages);
    }

    private Map<String, String> readMessages(Project project, String language)
    {
        Map<String, String> messages = new HashMap<>();
        
        StreamEx.of(project.getModulesAndApplication())
            .map( m -> m.getLocalizations().get( language.toLowerCase() )).nonNull()
            .map( ll -> ll.get( "frontend.l10n" ) ).nonNull()
            .flatMap( el -> el.getRows().stream() )
            .filter( row -> row.getTopic().equals( DatabaseConstants.L10N_TOPIC_PAGE ) )
            .forEach( row -> messages.put( row.getKey(), row.getValue() ) );
        
        return messages;
    }
    
}
