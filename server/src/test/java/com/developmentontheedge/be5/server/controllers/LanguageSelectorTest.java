package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;
import java.util.Collections;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class LanguageSelectorTest extends ServerBe5ProjectTest
{
    @Inject private LanguageSelectorController component;

    private LanguageSelectorController.LanguageSelectorResponse languageSelectorResponse =
            new LanguageSelectorController.LanguageSelectorResponse(
                    Collections.singletonList("RU"), "RU", ImmutableMap.of(
                    "fio","Ф.И.О.","no","нет","yes","да"));

    @Before
    public void init()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void generate()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("/api/languageSelector/"), response);


        verify(response).sendAsJson(eq(languageSelectorResponse));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    //todo
    @Ignore
    @Test
    public void generateLanguageParameterIsAbsent()
    {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid request: parameter language is missing.");

        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("/api/languageSelector/select"), response);
    }

    @Test
    public void generateSelect()
    {
        Response response = mock(Response.class);

        Request request = getSpyMockRequest("/api/languageSelector/select", ImmutableMap.of("language", "RU") );

        component.generate(request, response);

        verify(response).sendAsJson(eq(languageSelectorResponse));
    }
}