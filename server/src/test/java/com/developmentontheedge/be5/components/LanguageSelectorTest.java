package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.AbstractProjectTest;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.ErrorMessages;
import com.google.common.collect.ImmutableMap;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LanguageSelectorTest extends AbstractProjectTest
{
    private static Component component;

    @BeforeClass
    public static void init(){
        component = loadedClasses.get("languageSelector");
    }

    @Test
    public void generate() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getMockRequestWithUri(""), response, sp);

        LanguageSelector.LanguageSelectorResponse languageSelectorResponse =
                new LanguageSelector.LanguageSelectorResponse(
                    Collections.singletonList("RU"), "RU", new HashMap<>());
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

        component.generate(getSpyMockRequest("select"), response, sp);
    }

    @Test
    public void generateSelect() throws Exception
    {
        Response response = mock(Response.class);

        Request request = getSpyMockRequest("select", ImmutableMap.of("language", "RU") );

        component.generate(request, response, sp);

        LanguageSelector.LanguageSelectorResponse languageSelectorResponse =
                new LanguageSelector.LanguageSelectorResponse(
                        Collections.singletonList("RU"), "RU", new HashMap<>());
        verify(response).sendAsRawJson(eq(languageSelectorResponse));
    }
}