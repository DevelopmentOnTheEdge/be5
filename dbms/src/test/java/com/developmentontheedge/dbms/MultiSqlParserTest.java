package com.developmentontheedge.dbms;

import junit.framework.TestCase;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultiSqlParserTest extends TestCase
{
    private static class TestSqlHandler implements SqlHandler
    {
        int curPos;
        Map<String, Integer> sqlPos = new LinkedHashMap<>();

        public TestSqlHandler(String sql)
        {
            MultiSqlConsumer consumer = new MultiSqlConsumer(DbmsType.DB2, this);
            for (curPos = 0; curPos < sql.length(); curPos++)
            {
                consumer.symbol(sql.charAt(curPos));
            }
            consumer.end();
        }

        @Override
        public void endStatement(String statement)
        {
            sqlPos.put(statement, curPos);
        }

        @Override
        public void startStatement()
        {
            /* do nothing */
        }
    }

    public void testMultiSqlParser()
    {
        String sql = "-- cool statement\nSELECT 'a=''' || \na||'''; b='''||/*b value*/b   FROM \"myTable\";"
                + "SELECT -- select starts here\nb,c,d-- add more columns in future\nFROM ttt;\n\n-- second section --\n"
                + "SELECT blahblah;;;;SELECT '/* comment in ''quotes''*/';;";

        MultiSqlParser multiSqlParser = new MultiSqlParser(DbmsType.DB2, sql);
        assertEquals("SELECT 'a=''' || a||'''; b='''|| b FROM \"myTable\"", multiSqlParser.nextStatement());
        assertEquals("SELECT b,c,d FROM ttt", multiSqlParser.nextStatement());
        assertEquals("SELECT blahblah", multiSqlParser.nextStatement());
        assertEquals("SELECT '/* comment in ''quotes''*/'", multiSqlParser.nextStatement());
        assertNull(multiSqlParser.nextStatement());
    }

    public void testMultiSqlConsumer()
    {
        String sql = "-- cool statement\nSELECT 'a=''' || \na||'''; b='''||/*b value*/b   FROM \"myTable\";"
                + "SELECT -- select starts here\nb,c,d-- add more columns in future\nFROM ttt;\n\n-- second section --\n"
                + "SELECT blahblah;;;;SELECT '/* comment in ''quotes''*/';;DROP TRIGGER IF EXISTS makeSearchCompanyNameFields ON utilitySupplierChecks;\n"
                + "CREATE TRIGGER makeSearchCompanyNameFields\n"
                + "BEFORE INSERT OR UPDATE OF companyName ON utilitySupplierChecks\n"
                + "FOR EACH ROW\n"
                + "BEGIN\n"
                + "   :new.\"___searchCompanyName\" := TRIM( REGEXP_REPLACE( REGEXP_REPLACE( REPLACE( UPPER( :new.companyName ),'Ё','Е' ), '([^A-ZА-Я0-9]*)([A-ZА-Я0-9]+)', '\\2 ' ), '^([A-ZА-Я0-9 ]+)([^A-ZА-Я0-9]*)$', '\\1 ' ) );\n"
                + "END";

        TestSqlHandler handler = new TestSqlHandler(sql);
        assertEquals("{"
                        + "SELECT 'a=''' || a||'''; b='''|| b FROM \"myTable\"=80, "
                        + "SELECT b,c,d FROM ttt=153, SELECT blahblah=192, "
                        + "SELECT '/* comment in ''quotes''*/'=231, "
                        + "DROP TRIGGER IF EXISTS makeSearchCompanyNameFields ON utilitySupplierChecks=308, "
                        + "CREATE TRIGGER makeSearchCompanyNameFields BEFORE INSERT OR UPDATE OF companyName ON utilitySupplierChecks FOR EACH ROW BEGIN :new.\"___searchCompanyName\" := TRIM( REGEXP_REPLACE( REGEXP_REPLACE( REPLACE( UPPER( :new.companyName ),'Ё','Е' ), '([^A-ZА-Я0-9]*)([A-ZА-Я0-9]+)', '\\2 ' ), '^([A-ZА-Я0-9 ]+)([^A-ZА-Я0-9]*)$', '\\1 ' ) ); END=646}"
                , handler.sqlPos.toString());
    }

    public void testTrigger()
    {
        String sql = "DROP TRIGGER IF EXISTS makeSearchCompanyNameFields ON utilitySupplierChecks;\n" +
                "CREATE TRIGGER makeSearchCompanyNameFields\n" +
                "BEFORE INSERT OR UPDATE OF companyName ON utilitySupplierChecks\n" +
                "FOR EACH ROW\n" +
                "BEGIN\n" +
                "   :new.\"___searchCompanyName\" := TRIM( REGEXP_REPLACE( REGEXP_REPLACE( REPLACE( UPPER( :new.companyName ),'Ё','Е' ), '([^A-ZА-Я0-9]*)([A-ZА-Я0-9]+)', '\\2 ' ), '^([A-ZА-Я0-9 ]+)([^A-ZА-Я0-9]*)$', '\\1 ' ) );\n" +
                "END;";
        MultiSqlParser multiSqlParser = new MultiSqlParser(DbmsType.DB2, sql);
        assertEquals("DROP TRIGGER IF EXISTS makeSearchCompanyNameFields ON utilitySupplierChecks", multiSqlParser.nextStatement());
        assertEquals("CREATE TRIGGER makeSearchCompanyNameFields BEFORE INSERT OR UPDATE OF companyName ON utilitySupplierChecks FOR EACH ROW "
                + "BEGIN :new.\"___searchCompanyName\" := TRIM( REGEXP_REPLACE( REGEXP_REPLACE( REPLACE( UPPER( :new.companyName ),'Ё','Е' ), '([^A-ZА-Я0-9]*)([A-ZА-Я0-9]+)', '\\2 ' ), '^([A-ZА-Я0-9 ]+)([^A-ZА-Я0-9]*)$', '\\1 ' ) ); END", multiSqlParser.nextStatement());
        assertNull(multiSqlParser.nextStatement());
    }
}
