package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.ErrorMessages;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LanguageSelectorTest extends Be5ProjectTest
{
    @Inject private Injector injector;
    private Component component;

    @Before
    public void init(){
        component = injector.getComponent("languageSelector");
    }

    private LanguageSelector.LanguageSelectorResponse languageSelectorResponse =
            new LanguageSelector.LanguageSelectorResponse(
                    Collections.singletonList("RU"), "RU", ImmutableMap.of(
                    "fio","Ф.И.О.","no","нет","yes","да"));

    @Test
    public void generate() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response, injector);


        verify(response).sendAsRawJson(eq(languageSelectorResponse));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void generateLanguageParameterIsAbsent() throws Exception
    {
        expectedEx.expect(Be5Exception.class);
        expectedEx.expectMessage(ErrorMessages.formatMessage(Be5ErrorCode.PARAMETER_ABSENT, "language"));

        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("select"), response, injector);
    }

    @Test
    public void generateSelect() throws Exception
    {
        Response response = mock(Response.class);

        Request request = getSpyMockRequest("select", ImmutableMap.of("language", "RU") );

        component.generate(request, response, injector);

        verify(response).sendAsRawJson(eq(languageSelectorResponse));
    }
}