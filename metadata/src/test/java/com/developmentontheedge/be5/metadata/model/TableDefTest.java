package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.dbms.ExtendedSqlException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TableDefTest
{
    private TableDef create()
    {
        Project project = new Project("test");
        Entity entity = new Entity("test", project.getApplication(), EntityType.TABLE);
        DataElementUtils.save( entity );
        TableDef def = new TableDef( entity );
        DataElementUtils.save( def );
        return def;
    }
    
    private ColumnDef createColumn(TableDef table, String name, String type, boolean canBeNull)
    {
        ColumnDef c = new ColumnDef( name, table.getColumns() );
        c.setTypeString( type );
        c.setCanBeNull( canBeNull );
        DataElementUtils.save( c );
        return c;
    }
    
    private IndexDef createIndex(TableDef table, String name, String column)
    {
        IndexDef idx = new IndexDef(name, table.getIndices());
        IndexColumnDef col = new IndexColumnDef( column, idx );
        DataElementUtils.save( col );
        DataElementUtils.save( idx );
        return idx;
    }
    
    @Test
    public void testDdlColumns()
    {
        TableDef def = create();
        createColumn( def, "ID", "KEYTYPE", false );
        createColumn( def, "name", "VARCHAR(20)", true);
        createColumn( def, "type", "ENUM(a,b,c)", false);
        createColumn( def, "comment", "VARCHAR(5000)", true);
        createIndex( def, "TEST_ID_IDX", "ID" );
        def.getProject().setDatabaseSystem( Rdbms.MYSQL );
        assertEquals("DROP TABLE IF EXISTS `test`;\n"+
            "DROP VIEW IF EXISTS `test`;\n"+
            "CREATE TABLE `test` (\n"+
            "`ID` BIGINT UNSIGNED NOT NULL,\n"+
            "`name` VARCHAR(20),\n"+
            "`type` ENUM('a','b','c') NOT NULL,\n"+
            "`comment` TEXT);\n"+
            "CREATE INDEX `TEST_ID_IDX` ON `test`(`ID`);\n", def.getDdl());
        def.getProject().setDatabaseSystem( Rdbms.POSTGRESQL );
        assertEquals("DROP TABLE IF EXISTS test;\n"+
            "CREATE TABLE test (\n"+
            "id BIGINT NOT NULL,\n"+
            "name VARCHAR(20),\n"+
            "type VARCHAR(1) CHECK(type IN ('a', 'b', 'c') ) NOT NULL,\n"+
            "comment VARCHAR(5000));\n"+
            "CREATE INDEX test_id_idx ON test(id);\n", def.getDdl());
        def.getProject().setDatabaseSystem( Rdbms.ORACLE );
        assertEquals("call drop_if_exists( 'test' );\n"+
            "CREATE TABLE TEST (\n"+
            "ID VARCHAR2(15 CHAR) NOT NULL,\n"+
            "NAME VARCHAR2(20 CHAR),\n"+
            "TYPE VARCHAR2(2 CHAR) CHECK(TYPE IN ('a', 'b', 'c') ) NOT NULL,\n"+
            "\"comment\" VARCHAR2(5000 CHAR));\n"+
            "CREATE INDEX TEST_ID_IDX ON TEST(ID);\n", def.getDdl());
        def.getProject().setDatabaseSystem( Rdbms.SQLSERVER );
        assertEquals("IF EXISTS (SELECT ID FROM sysobjects WHERE id = OBJECT_ID(N'test') AND OBJECTPROPERTY(id, N'IsUserTable') = 1 )\n"+
            "DROP TABLE test;\n"+
            "CREATE TABLE \"test\" (\n"+
            "\"ID\" BIGINT NOT NULL,\n"+
            "\"name\" VARCHAR(20),\n"+
            "\"type\" VARCHAR(1) CHECK(\"type\" IN ('a', 'b', 'c') ) NOT NULL,\n"+
            "\"comment\" VARCHAR(5000));\n"+
            "CREATE INDEX \"TEST_ID_IDX\" ON \"test\"(\"ID\");\n", def.getDdl());
        def.getProject().setDatabaseSystem( Rdbms.DB2 );
        assertEquals("BEGIN DECLARE CONTINUE HANDLER FOR SQLSTATE '42704' BEGIN END; EXECUTE IMMEDIATE 'DROP TABLE TEST'; END;\n"+
            "CREATE TABLE TEST (\n"+
            "ID BIGINT NOT NULL,\n"+
            "NAME VARCHAR(20),\n"+
            "TYPE VARCHAR(1) CHECK(TYPE IN ('a', 'b', 'c') ) NOT NULL,\n"+
            "\"comment\" VARCHAR(5000));\n"+
            "CREATE INDEX TEST_ID_IDX ON TEST(ID);\n", def.getDdl());

        def.getProject().setDatabaseSystem( Rdbms.H2 );
        assertEquals("DROP TABLE IF EXISTS test;\n"+
                "CREATE TABLE test (\n"+
                "id BIGINT NOT NULL,\n"+
                "name VARCHAR(20),\n"+
                "type VARCHAR(1) NOT NULL CHECK(type IN ('a', 'b', 'c') ),\n"+
                "comment VARCHAR(5000));\n"+
                "CREATE INDEX test_id_idx ON test(id);\n", def.getDdl());
    }
    
    @Test
    public void testDiffDdlCreateColumn() throws ExtendedSqlException
    {
        TableDef def = create();
        createColumn( def, "ID", "KEYTYPE", false );
        createColumn( def, "name", "VARCHAR(20)", true);
        createColumn( def, "type", "ENUM(a,b,c)", false);
        createColumn( def, "comment", "VARCHAR(5000)", true);
        createIndex( def, "TEST_ID_IDX", "ID" );
        TableDef def2 = ( TableDef ) def.clone( def.getOrigin(), def.getName() );
        def.getProject().setDatabaseSystem( Rdbms.ORACLE );
        assertEquals("", def2.getDiffDdl( def, null ));
        createColumn( def2, "payment", "CURRENCY", false);
        assertEquals("ALTER TABLE TEST ADD PAYMENT NUMBER(18,2) DEFAULT '' NOT NULL;\n"+
                "ALTER TABLE TEST MODIFY (PAYMENT NUMBER(18,2));", def2.getDiffDdl( def, null ));
        assertEquals("ALTER TABLE TEST DROP COLUMN PAYMENT;\n", def.getDiffDdl( def2, null ));
        def.getProject().setDatabaseSystem( Rdbms.POSTGRESQL );
        assertEquals("ALTER TABLE test ADD COLUMN payment DECIMAL(18,2) DEFAULT '' NOT NULL;\n"+
                "ALTER TABLE test ALTER COLUMN payment DROP DEFAULT;", def2.getDiffDdl( def, null ));
        assertEquals("ALTER TABLE test DROP COLUMN payment;\n", def.getDiffDdl( def2, null ));
        def.getProject().setDatabaseSystem( Rdbms.SQLSERVER );
        assertEquals("ALTER TABLE \"test\" ADD \"payment\" DECIMAL(18,2) DEFAULT '' NOT NULL;"+
                "BEGIN DECLARE @Command nvarchar(max), @ConstaintName nvarchar(max)\n"+
                "SELECT @ConstaintName = name FROM sys.default_constraints WHERE parent_object_id = object_id('test') AND parent_column_id = columnproperty(object_id('test'), 'payment', 'ColumnId')\n"+
                "SELECT @Command = 'ALTER TABLE \"test\" DROP CONSTRAINT '+ @ConstaintName\n"+
                "EXECUTE sp_executeSQL @Command\n"+
                "END;\n", def2.getDiffDdl( def, null ));
        assertEquals("ALTER TABLE \"test\" DROP COLUMN \"payment\";\n", def.getDiffDdl( def2, null ));
        def.getProject().setDatabaseSystem( Rdbms.DB2 );
        assertEquals("ALTER TABLE TEST ADD COLUMN PAYMENT DECIMAL(18,2) DEFAULT '' NOT NULL;\n"+
                "CALL admin_cmd('REORG TABLE TEST');ALTER TABLE TEST ALTER COLUMN PAYMENT DROP DEFAULT;CALL admin_cmd('REORG TABLE TEST');", def2.getDiffDdl( def, null ));
        assertEquals("ALTER TABLE TEST DROP COLUMN PAYMENT;\nCALL admin_cmd('REORG TABLE TEST');", def.getDiffDdl( def2, null ));
        def.getProject().setDatabaseSystem( Rdbms.MYSQL );
        assertEquals("ALTER TABLE `test` ADD COLUMN `payment` DECIMAL(18,2) DEFAULT '' NOT NULL;\n"+
                "ALTER TABLE `test` MODIFY COLUMN `payment` DECIMAL(18,2) NOT NULL;", def2.getDiffDdl( def, null ));
        assertEquals("ALTER TABLE `test` DROP COLUMN `payment`;\n", def.getDiffDdl( def2, null ));
    }

    @Test
    public void testDiffDdlRenameColumn() throws ExtendedSqlException
    {
        TableDef def = create();
        createColumn( def, "ID", "KEYTYPE", false );
        createColumn( def, "name", "VARCHAR(20)", true);
        createColumn( def, "type", "ENUM(a,b,c)", false);
        createColumn( def, "comment", "VARCHAR(5000)", true);
        createIndex( def, "TEST_ID_IDX", "ID" );
        TableDef def2 = ( TableDef ) def.clone( def.getOrigin(), def.getName() );
        def.getProject().setDatabaseSystem( Rdbms.ORACLE );
        assertEquals("", def2.getDiffDdl( def, null ));
        def2.renameColumn( "ID", "CODE" );
        assertEquals("RENAME COLUMN TEST.ID TO CODE;\n", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.POSTGRESQL );
        assertEquals("ALTER TABLE test RENAME COLUMN id TO code;\n", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.SQLSERVER );
        assertEquals("EXEC sp_RENAME '[test].[ID]', 'CODE', 'COLUMN';\n", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.DB2 );
        assertEquals("ALTER TABLE TEST RENAME COLUMN ID TO CODE;\n", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.MYSQL );
        assertEquals("ALTER TABLE `test` CHANGE COLUMN `ID` `CODE` BIGINT UNSIGNED NOT NULL;", def2.getDiffDdl( def, null ));
    }

    @Test
    public void testDiffDdlUpdateTypeSafely() throws ExtendedSqlException
    {
        TableDef def = create();
        createColumn( def, "ID", "KEYTYPE", false );
        createColumn( def, "name", "VARCHAR(20)", true);
        createColumn( def, "type", "ENUM(a,b,c)", false);
        createColumn( def, "comment", "VARCHAR(5000)", true);
        createIndex( def, "TEST_ID_IDX", "ID" );
        TableDef def2 = ( TableDef ) def.clone( def.getOrigin(), def.getName() );
        def.getProject().setDatabaseSystem( Rdbms.ORACLE );
        assertEquals("", def2.getDiffDdl( def, null ));
        def2.findColumn( "name" ).setTypeString( "VARCHAR(30)");
        assertEquals("ALTER TABLE TEST MODIFY (NAME VARCHAR2(30 CHAR));", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.POSTGRESQL );
        assertEquals("ALTER TABLE test ALTER COLUMN name SET DATA TYPE VARCHAR(30);\n", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.SQLSERVER );
        assertEquals("ALTER TABLE \"test\" ALTER COLUMN \"name\" VARCHAR(30) NULL;", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.DB2 );
        assertEquals("ALTER TABLE TEST ALTER COLUMN NAME SET DATA TYPE VARCHAR(30);\nCALL admin_cmd('REORG TABLE TEST');", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.MYSQL );
        assertEquals("ALTER TABLE `test` MODIFY COLUMN `name` VARCHAR(30);", def2.getDiffDdl( def, null ));
        
        def2 = ( TableDef ) def.clone( def.getOrigin(), def.getName() );
        def2.findColumn( "type" ).setTypeString( "ENUM(a,b,c,d)" );
        def.getProject().setDatabaseSystem( Rdbms.ORACLE );
        assertEquals("ALTER TABLE TEST MODIFY (TYPE VARCHAR2(2 CHAR) CHECK(TYPE IN ('a', 'b', 'c', 'd') ));", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.POSTGRESQL );
        assertEquals("ALTER TABLE test DROP CONSTRAINT test_type_check;\n"+
                "ALTER TABLE test ADD CONSTRAINT test_type_check CHECK(type IN ('a', 'b', 'c', 'd') );\n", def2.getDiffDdl( def, null ));
        //def.getProject().setDatabaseSystem( Rdbms.SQLSERVER );
        // TODO: Support updating the constraints for SqlServer
        //assertEquals("ALTER TABLE \"test\" ALTER COLUMN \"type\" VARCHAR(1) NOT NULL;", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.DB2 );
        assertEquals("ALTER TABLE TEST DROP CONSTRAINT TEST_TYPE_CHECK;\n"+
                "ALTER TABLE TEST ADD CONSTRAINT TEST_TYPE_CHECK CHECK(TYPE IN ('a', 'b', 'c', 'd') );\n"+
                "CALL admin_cmd('REORG TABLE TEST');", def2.getDiffDdl( def, null ));
        def.getProject().setDatabaseSystem( Rdbms.MYSQL );
        assertEquals("ALTER TABLE `test` MODIFY COLUMN `type` ENUM('a','b','c','d') NOT NULL;", def2.getDiffDdl( def, null ));
    }
    
    @Test
    public void testFindColumn()
    {
        TableDef def = create();
        ColumnDef col = createColumn( def, "col", "INT", true );
        assertSame(col, def.findColumn( "COl" ));
    }
    
    @Test
    public void testRenameColumn()
    {
        TableDef def = create();
        createColumn( def, "ID", "KEYTYPE", false );
        createColumn( def, "name", "VARCHAR(20)", true);
        createColumn( def, "type", "ENUM(a,b,c)", false);
        createColumn( def, "comment", "VARCHAR(5000)", true);
        createIndex( def, "TEST_ID_IDX", "ID" );
        def.renameColumn( "ID", "CODE" );
        assertNull(def.getColumns().get( "ID" ));
        ColumnDef newCol = def.getColumns().get( "CODE" );
        assertArrayEquals(new String[] {"ID"}, newCol.getOldNames());
        assertTrue(def.getIndicesUsingColumn( "ID" ).isEmpty());
        def.getProject().setDatabaseSystem( Rdbms.POSTGRESQL );
        assertEquals("DROP INDEX IF EXISTS test_id_idx;\n"
            + "CREATE INDEX test_id_idx ON test(code);", def.getIndicesUsingColumn( "CODE" ).get( 0 ).getDdl());
        assertEquals("DROP TABLE IF EXISTS test;\n"+
                "CREATE TABLE test (\n"+
                "code BIGINT NOT NULL,\n"+
                "name VARCHAR(20),\n"+
                "type VARCHAR(1) CHECK(type IN ('a', 'b', 'c') ) NOT NULL,\n"+
                "comment VARCHAR(5000));\n"+
                "CREATE INDEX test_id_idx ON test(code);\n", def.getDdl());
    }
    
    @Test
    public void testErrors()
    {
        TableDef def = create();
        List<ProjectElementException> errors = def.getErrors();
        assertEquals(1, errors.size());
        assertEquals("test/application/Entities/test/Scheme: Table must have at least one column", errors.get( 0 ).getMessage());
    }
}
