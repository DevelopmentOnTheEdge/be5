package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.server.controllers.LanguageSelectorController.LanguageSelectorResponse;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.web.Request;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;


public class LanguageSelectorTest extends ServerBe5ProjectTest
{
    @Inject private LanguageSelectorController component;

    private static final LanguageSelectorController.LanguageSelectorResponse languageSelectorResponse =
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
        LanguageSelectorResponse generate =
                (LanguageSelectorResponse)
                        component.generate(getMockRequest("/api/languageSelector/"), "");

        assertEquals(languageSelectorResponse, generate);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void generateLanguageParameterIsAbsent()
    {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid request: parameter language is missing.");

        component.generate(getSpyMockRequest("/api/languageSelector/select"), "select");
    }

    @Test
    public void generateSelect()
    {
        Request request = getSpyMockRequest("/api/languageSelector/select", ImmutableMap.of("language", "RU") );

        assertEquals(new LanguageSelectorResponse(Collections.singletonList("RU"), "RU", new HashMap<String, String>(){{
            put("no", "нет");
            put("yes", "да");
            put("fio", "Ф.И.О.");
        }}), component.generate(request, "select"));
    }
}