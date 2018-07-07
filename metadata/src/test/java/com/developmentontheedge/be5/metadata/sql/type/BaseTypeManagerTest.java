package com.developmentontheedge.be5.metadata.sql.type;

import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.sql.Rdbms;

import static org.junit.Assert.assertEquals;

public class BaseTypeManagerTest
{
    protected ColumnDef addColumn(TableDef def, String name, String type)
    {
        ColumnDef col = new ColumnDef(name, def.getColumns());
        col.setTypeString(type);
        DataElementUtils.save(col);
        return col;
    }

    protected TableDef createTable(Rdbms dbms)
    {
        Project proj = new Project("test");
        proj.setDatabaseSystem(dbms);
        Entity ent = new Entity("table", proj.getApplication(), EntityType.TABLE);
        DataElementUtils.save(ent);
        TableDef def = new TableDef(ent);
        DataElementUtils.save(def);
        return def;
    }

    protected void assertTypeTranslation(DbmsTypeManager tm, String input, String expected)
    {
        SqlColumnType type = new SqlColumnType();
        type.setTypeName(input);
        tm.correctType(type);
        assertEquals(expected, type.toString());
    }

}
