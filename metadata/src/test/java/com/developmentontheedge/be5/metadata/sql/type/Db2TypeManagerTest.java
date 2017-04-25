package com.developmentontheedge.be5.metadata.sql.type;

import static org.junit.Assert.*;

import org.junit.Test;

import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.dbms.ExtendedSqlException;

public class Db2TypeManagerTest extends BaseTypeManagerTest
{
    @Test
    public void testTypes() throws ExtendedSqlException
    {
        TableDef def = createTable(Rdbms.DB2);
        addColumn( def, "a", SqlColumnType.TYPE_BLOB );
        addColumn( def, "b", SqlColumnType.TYPE_BIGTEXT );
        addColumn( def, "c", SqlColumnType.TYPE_UINT );
        addColumn( def, "d", SqlColumnType.TYPE_UBIGINT );
        addColumn( def, "e", SqlColumnType.TYPE_DATETIME );
        assertEquals("BEGIN DECLARE CONTINUE HANDLER FOR SQLSTATE '42704' BEGIN END; EXECUTE IMMEDIATE 'DROP TABLE \"table\"'; END;\n" + 
            "CREATE TABLE \"table\" (\n" + 
            "A BLOB(16M) NOT NULL,\n" + 
            "B CLOB(128K) NOT NULL,\n" + 
            "C INT NOT NULL,\n" + 
            "D BIGINT NOT NULL,\n" + 
            "E TIMESTAMP NOT NULL);\n", def.getDdl());
        TableDef def2 = ( TableDef ) def.clone( def.getOrigin(), def.getName() );
        ColumnDef col = addColumn( def2, "f", SqlColumnType.TYPE_KEY );
        col.setAutoIncrement( true );
        col.setPrimaryKey( true );
        assertEquals("ALTER TABLE \"table\" ADD COLUMN F BIGINT GENERATED BY DEFAULT AS IDENTITY (NO CACHE) NOT NULL PRIMARY KEY;\n" + 
            "CALL admin_cmd('REORG TABLE \"table\"');", def2.getDiffDdl( def, null ));
        assertEquals("ALTER TABLE \"table\" DROP COLUMN F;\n" + 
            "CALL admin_cmd('REORG TABLE \"table\"');", def.getDiffDdl( def2, null ));
    }
    
    @Test
    public void testCorrectType()
    {
        DbmsTypeManager typeManager = Rdbms.DB2.getTypeManager();
        assertTypeTranslation( typeManager, "VARCHAR () FOR BIT DATA", "MEDIUMBLOB" );
        assertTypeTranslation( typeManager, "CHARACTER", "CHAR(255)" );
        assertTypeTranslation( typeManager, "DOUBLE", "DECIMAL(22,10)" );
        assertTypeTranslation( typeManager, "LONG VARCHAR", "TEXT" );
        assertTypeTranslation( typeManager, "CLOB", "TEXT" );
        assertTypeTranslation( typeManager, "TIME", "TIMESTAMP" );
    }
    
}
