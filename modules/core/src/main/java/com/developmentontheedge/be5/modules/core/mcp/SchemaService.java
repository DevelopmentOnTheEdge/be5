package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.meta.Meta;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class SchemaService
{
    private final Meta meta;
    private final DbService dbService;

    @Inject
    public SchemaService(Meta meta, DbService dbService)
    {
        this.meta = meta;
        this.dbService = dbService;
    }

    public List<Map<String, Object>> getEntities()
    {
        List<Map<String, Object>> entities = new ArrayList<>();
        for (Entity entity : meta.getEntities())
        {
            Map<String, Object> entityInfo = new LinkedHashMap<>();
            entityInfo.put("name", entity.getName());
            entityInfo.put("type", entity.getType() != null ? entity.getType().name() : "TABLE");
            entityInfo.put("primaryKey", entity.getPrimaryKey());
            entities.add(entityInfo);
        }
        return entities;
    }

    public Map<String, Object> getEntitySchema(String entityName)
    {
        Map<String, Object> schema = new LinkedHashMap<>();
        Entity entity = meta.getEntity(entityName);
        schema.put("name", entity.getName());
        schema.put("type", entity.getType() != null ? entity.getType().name() : "TABLE");
        schema.put("primaryKey", entity.getPrimaryKey());

        List<Map<String, Object>> columns = new ArrayList<>();
        Map<String, ColumnDef> columnDefs = meta.getColumns(entityName);
        for (ColumnDef columnDef : columnDefs.values())
        {
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("name", columnDef.getName());
            column.put("type", columnDef.getTypeString());
            column.put("defaultValue", columnDef.getDefaultValue());
            column.put("canBeNull", columnDef.isCanBeNull());
            column.put("autoIncrement", columnDef.isAutoIncrement());
            if (columnDef.hasReference())
            {
                column.put("reference", columnDef.getTableTo());
            }
            columns.add(column);
        }
        schema.put("columns", columns);
        return schema;
    }

    public List<Map<String, Object>> getEntityReferences(String entityName)
    {
        List<Map<String, Object>> references = new ArrayList<>();
        Entity entity = meta.getEntity(entityName);
        if (entity == null) return references;

        List<TableReference> tableRefs = entity.getAllReferences();
        if (tableRefs != null)
        {
            for( TableReference ref : tableRefs )
            {
                Map<String, Object> reference = new LinkedHashMap<>();
                reference.put("fromColumn", ref.getColumnsFrom());
                reference.put("toEntity", ref.getTableTo());
                reference.put("toEntityPermitted", ref.getPermittedTables());
                reference.put("toColumn", ref.getColumnsTo());
                reference.put("view", ref.getViewName());
                references.add(reference);
            }
        }
        return references;
    }

    public Map<String, Object> getEntitySchemaWithReferences(String entityName)
    {
        Map<String, Object> schema = getEntitySchema(entityName);
        schema.put("references", getEntityReferences(entityName));
        return schema;
    }

    public List<Map<String, Object>> getDatabaseColumns()
    {
        List<Map<String, Object>> columns = new ArrayList<>();
        dbService.execute(conn -> {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, "%", null))
            {
                while (rs.next())
                {
                    Map<String, Object> column = new LinkedHashMap<>();
                    column.put("tableName", rs.getString("TABLE_NAME"));
                    column.put("columnName", rs.getString("COLUMN_NAME"));
                    column.put("dataType", rs.getString("TYPE_NAME"));
                    column.put("columnSize", rs.getInt("COLUMN_SIZE"));
                    column.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    column.put("remarks", rs.getString("REMARKS"));
                    columns.add(column);
                }
            }
            return null;
        });
        return columns;
    }

    public Map<String, Object> getTableInfo(String tableName)
    {
        Map<String, Object> tableInfo = new LinkedHashMap<>();
        tableInfo.put("tableName", tableName);

        List<Map<String, Object>> columns = new ArrayList<>();
        dbService.execute(conn -> {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, null))
            {
                while (rs.next())
                {
                    Map<String, Object> column = new LinkedHashMap<>();
                    column.put("name", rs.getString("COLUMN_NAME"));
                    column.put("type", rs.getString("TYPE_NAME"));
                    column.put("size", rs.getInt("COLUMN_SIZE"));
                    column.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    column.put("defaultValue", rs.getString("COLUMN_DEF"));
                    column.put("remarks", rs.getString("REMARKS"));
                    columns.add(column);
                }
            }
            try (ResultSet rs = metaData.getPrimaryKeys(null, null, tableName))
            {
                List<String> pkColumns = new ArrayList<>();
                while (rs.next())
                {
                    pkColumns.add(rs.getString("COLUMN_NAME"));
                }
                tableInfo.put("primaryKey", pkColumns);
            }
            return null;
        });

        tableInfo.put("columns", columns);
        return tableInfo;
    }
}