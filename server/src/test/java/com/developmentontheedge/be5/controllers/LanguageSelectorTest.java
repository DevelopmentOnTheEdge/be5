package com.developmentontheedge.be5.controllers;

import javax.inject.Inject;

import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.exceptions.ErrorTitles;
import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

    @Test
    public void generate()
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest(""), response);


        verify(response).sendAsRawJson(eq(languageSelectorResponse));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void generateLanguageParameterIsAbsent()
    {
        expectedEx.expect(Be5Exception.class);
        expectedEx.expectMessage(ErrorTitles.formatTitle(Be5ErrorCode.PARAMETER_ABSENT, "language"));

        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("select"), response);
    }

    @Test
    public void generateSelect()
    {
        Response response = mock(Response.class);

        Request request = getSpyMockRequest("select", ImmutableMap.of("language", "RU") );

        component.generate(request, response);

        verify(response).sendAsRawJson(eq(languageSelectorResponse));
    }
}