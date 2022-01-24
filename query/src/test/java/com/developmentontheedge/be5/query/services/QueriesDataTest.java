package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.database.QRec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class QueriesDataTest extends QueryBe5ProjectDBTest
{
    @Inject
    private QueriesService queries;

    @Before
    public void setUp()
    {
        setStaticUserInfo(RoleType.ROLE_GUEST);
    }

    @After
    public void tearDown()
    {
        db.update("delete from testData");
    }

    @Test
    public void text() throws SQLException
    {
        db.insert("insert into testData (textCol) VALUES (?)", "test2");
        QRec qRec = queries.queryRecord("select textCol from testData", Collections.emptyMap());

        assertEquals("test2", qRec.getString("textCol"));
    }

    @Test
    public void blob() throws SQLException
    {
        InputStream test1 = new ByteArrayInputStream("test1".getBytes(StandardCharsets.UTF_8));

        db.insert("insert into testData (dataCol) VALUES (?)", test1);
        QRec qRec = queries.queryRecord("select dataCol from testData", Collections.emptyMap());

        assertEquals("test1", new BufferedReader(
                new InputStreamReader(qRec.getBinaryStream("dataCol"))).lines()
               .collect(Collectors.joining("")));
        assertEquals("test1", qRec.getString("dataCol"));
    }
}
