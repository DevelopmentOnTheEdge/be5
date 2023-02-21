package com.developmentontheedge.be5.meta;

import com.developmentontheedge.be5.BaseTest;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.metadata.RoleType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;


public class UserAwareMetaImplTest extends BaseTest
{
    @Inject
    private UserAwareMeta userAwareMeta;

    @Before
    public void setUp()
    {
        setStaticUserInfo(RoleType.ROLE_GUEST);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void getLocalizedOperationTitle()
    {
        assertEquals("Удалить", userAwareMeta.getLocalizedOperationTitle("default", "Delete"));
        assertEquals("Редактировать", userAwareMeta.getLocalizedOperationTitle("default", "Edit"));
        assertEquals("Добавить", userAwareMeta.getLocalizedOperationTitle("default", "Insert"));
        assertEquals("Фильтр", userAwareMeta.getLocalizedOperationTitle("default", "Filter"));
    }

    @Test
    public void getLocalizedOperationTitleUseDefault()
    {
        assertEquals("Удалить", userAwareMeta.getLocalizedOperationTitle("anyTable", "Delete"));
    }

    @Test
    public void getOperation()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR);
        userAwareMeta.getOperation("testtableAdmin", "AdministratorOperation");
    }

    @Test
    public void getOperation_Access_denied()
    {
        expectedEx.expect(Be5Exception.class);
        expectedEx.expectMessage("Access denied to operation: testtableAdmin.AdministratorOperation");

        userAwareMeta.getOperation("testtableAdmin", "AdministratorOperation");
    }

    @Test
    public void getLocalizedBe5ErrorMessage()
    {
        Be5Exception be5Exception = Be5Exception.internal("test");

        assertEquals("Internal error occurred: test", be5Exception.getMessage());

        assertEquals("Произошла внутренняя ошибка: test",
                userAwareMeta.getLocalizedBe5ErrorMessage(be5Exception));
    }

    @Test
    public void getOperationForQueryAssigned()
    {
        expectedEx.expect(Be5Exception.class);
        expectedEx.expectMessage("Access denied to operation: testtableAdmin.AdministratorOperation");

        userAwareMeta.getOperation("testtableAdmin", "All records", "AdministratorOperationForQuery");
    }

    @Test
    public void getOperationError_not_assigned()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR);

        expectedEx.expect(Be5Exception.class);
        expectedEx.expectMessage("Operation 'testtableAdmin.AdministratorOperation' not assigned to query: 'All records'");

        userAwareMeta.getOperation("testtableAdmin", "All records", "AdministratorOperation");
    }

    @Test
    public void getOperationForQuery()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR);

        userAwareMeta.getOperation("testtableAdmin", "All records", "AdministratorOperationForQuery");
    }

    @Test
    public void getQuery()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR);
        userAwareMeta.getQuery("testtableAdmin", "All records");
    }

    @Test
    public void getQuery2()
    {
        expectedEx.expect(Be5Exception.class);
        expectedEx.expectMessage("Access denied to query: testtableAdmin.All records");

        userAwareMeta.getQuery("testtableAdmin", "All records");
    }

    @Test
    public void getColumnTitle()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR);
        assertEquals("Код", userAwareMeta.getColumnTitle("testTags", "CODE"));

        assertEquals("Код", userAwareMeta.getColumnTitle("testTags", "All records", "CODE"));

        assertEquals("Код2", userAwareMeta.getColumnTitle("testTags", "All records2", "CODE"));
    }

    @Test
    public void getMessage1()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR);
        assertEquals("Код", userAwareMeta.getMessage("testTags", "All records", "CODE"));
        assertEquals("Код2", userAwareMeta.getMessage("testTags", "All records2", "CODE"));
    }

    @Test
    public void getMessage2()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR);
        assertEquals("Код", userAwareMeta.getMessage("ru","testTags", "All records", "CODE"));
        assertEquals("Код2", userAwareMeta.getMessage("ru","testTags", "All records2", "CODE"));
    }

    @Test
    public void getLocalizedEntityTitle()
    {
        assertEquals("Testtable Admin", userAwareMeta.getLocalizedEntityTitle("testtableAdmin"));

        assertEquals("Тест теги", userAwareMeta.getLocalizedEntityTitle("testTags"));
    }
}
