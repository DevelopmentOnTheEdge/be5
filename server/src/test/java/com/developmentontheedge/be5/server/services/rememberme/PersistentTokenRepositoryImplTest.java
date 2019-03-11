package com.developmentontheedge.be5.server.services.rememberme;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest;
import com.developmentontheedge.be5.util.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PersistentTokenRepositoryImplTest extends ServerBe5ProjectDBTest
{
    private QueriesService queries;
    private PersistentTokenRepository repo;

    @Before
    public void setUp()
    {
        db.updateUnsafe("drop table if exists persistent_logins");
        db.updateUnsafe("create table persistent_logins (user_name varchar(64) not null, series varchar(64) primary key, "
                + "token varchar(64) not null, last_used timestamp not null)");
        queries = getInjector().getInstance(QueriesService.class);
        repo = new PersistentTokenRepositoryImpl(db);
    }

    @Test
    public void createNewTokenInsertsCorrectData()
    {
        Timestamp currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
        PersistentRememberMeToken token = new PersistentRememberMeToken("joeuser",
                "joesseries", "atoken", currentDate);
        repo.createNewToken(token);

        QRec results = queries.qRec(
                "select * from persistent_logins");

        assertEquals(results.getValue("last_used"), currentDate);
        assertEquals(results.getString("user_name"), "joeuser");
        assertEquals(results.getString("series"), "joesseries");
        assertEquals(results.getString("token"), "atoken");
    }

    @Test
    public void retrievingTokenReturnsCorrectData()
    {

        db.insert(
                "insert into persistent_logins (series, user_name, token, last_used) values "
                        + "('joesseries', 'joeuser', 'atoken', '2007-10-09 18:19:25.000000000')");
        PersistentRememberMeToken token = repo.getTokenForSeries("joesseries");

        assertEquals(token.getUsername(), "joeuser");
        assertEquals(token.getSeries(), "joesseries");
        assertEquals(token.getTokenValue(), "atoken");
        assertEquals(token.getTimestamp(),
                Timestamp.valueOf("2007-10-09 18:19:25.000000000"));
    }

    // SEC-1964
    @Test
    public void retrievingTokenWithNoSeriesReturnsNull()
    {
//        when(logger.isDebugEnabled()).thenReturn(true);
//
        assertNull(repo.getTokenForSeries("missingSeries"));
//
//        verify(logger).isDebugEnabled();
//        verify(logger).debug(
//                eq("Querying token for series 'missingSeries' returned no results."),
//                any(EmptyResultDataAccessException.class));
//        verifyNoMoreInteractions(logger);
    }

    @Test
    public void removingUserTokensDeletesData()
    {
        db.insert(
                "insert into persistent_logins (series, user_name, token, last_used) values "
                        + "('joesseries2', 'joeuser', 'atoken2', '2007-10-19 18:19:25.000000000')");
        db.insert(
                "insert into persistent_logins (series, user_name, token, last_used) values "
                        + "('joesseries', 'joeuser', 'atoken', '2007-10-09 18:19:25.000000000')");

        // List results =
        // db.queryForList("select * from persistent_logins where series =
        // 'joesseries'");

        repo.removeUserTokens("joeuser");

        List<QRec> results = queries.readAsRecords(
                "select * from persistent_logins where user_name = 'joeuser'");

        assertTrue(results.isEmpty());
    }

    @Test
    public void updatingTokenModifiesTokenValueAndLastUsed()
    {
        Timestamp ts = new Timestamp(System.currentTimeMillis() - 1);
        db.insert("insert into persistent_logins (series, user_name, token, last_used) values "
                + "('joesseries', 'joeuser', 'atoken', '" + ts.toString() + "')");
        repo.updateToken("joesseries", "newtoken", DateUtils.currentTimestamp());

        QRec results = queries.qRec("select * from persistent_logins where series = 'joesseries'");

        assertEquals(results.getString("user_name"), "joeuser");
        assertEquals(results.getString("series"), "joesseries");
        assertEquals(results.getString("token"), "newtoken");
        Date lastUsed = (Date) results.getValue("last_used");
        assertTrue(lastUsed.getTime() > ts.getTime());
    }

    // SEC-2879
    @Test
    public void updateUsesLastUsed()
    {
        DbService db = mock(DbService.class);
        Timestamp lastUsed = new Timestamp(1424841314059L);
        PersistentTokenRepositoryImpl repository = new PersistentTokenRepositoryImpl(db);

        repository.updateToken("series", "token", lastUsed);

        verify(db).update(anyString(), anyString(), eq(lastUsed), anyString());
    }

}
