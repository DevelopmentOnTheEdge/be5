package com.developmentontheedge.be5.metadata.sql.type;

import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexDef;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.TableDef;
import one.util.streamex.MoreCollectors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PostgresTypeManager extends DefaultTypeManager
{
    private static Set<String> KEYWORDS = new HashSet<>(Arrays.asList("SELECT", "KEY", "ORDER", "TABLE", "WHERE",
            "GROUP", "FROM", "TO", "BY", "JOIN", "LEFT", "INNER", "OUTER", "NUMBER", "DISTINCT",
            "START", "END", "INDEX", "DATE", "LEVEL", "COLUMN"));

    @Override
    public void correctType(SqlColumnType type)
    {
        switch (type.getTypeName())
        {
            case "bigserial":
            case "int8":
                type.setTypeName(SqlColumnType.TYPE_BIGINT);
                break;
            case "serial":
            case "int4":
                type.setTypeName(SqlColumnType.TYPE_INT);
                break;
            case "int2":
                type.setTypeName(SqlColumnType.TYPE_SMALLINT);
                break;
            case "numeric":
                if (type.getPrecision() != 0)
                {
                    type.setTypeName(SqlColumnType.TYPE_DECIMAL);
                }
                else
                {
                    if (type.getSize() > 7)
                        type.setTypeName(SqlColumnType.TYPE_BIGINT);
                    else
                        type.setTypeName(SqlColumnType.TYPE_INT);
                }
                break;
            case "bytea":
                type.setTypeName(SqlColumnType.TYPE_BLOB);
                break;
            case "bpchar":
                type.setTypeName(SqlColumnType.TYPE_CHAR);
                break;
            default:
                type.setTypeName(type.getTypeName().toUpperCase());
        }
        super.correctType(type);
    }

    @Override
    public String getTypeClause(SqlColumnType type)
    {
        switch (type.getTypeName())
        {
            case SqlColumnType.TYPE_DATETIME:
                return "TIMESTAMP";
            case SqlColumnType.TYPE_UBIGINT:
                return "BIGINT";
            case SqlColumnType.TYPE_UINT:
                return "INT";
            case SqlColumnType.TYPE_BIGTEXT:
                return "TEXT";
            case SqlColumnType.TYPE_MEDIUMBLOB:
            case SqlColumnType.TYPE_BLOB:
                return "BYTEA";
            case SqlColumnType.TYPE_JSONB:
                return "JSONB";
            case SqlColumnType.TYPE_VECTOR:
                return type.toString();
            case SqlColumnType.TYPE_BOOL:
            case SqlColumnType.TYPE_ENUM:
                int maxLen = 0;
                for (String enumValue : type.getEnumValues())
                {
                    maxLen = Math.max(maxLen, enumValue.length());
                }
                return "VARCHAR(" + (maxLen) + ")";
        }
        return super.getTypeClause(type);
    }

    private static final String CREATE_VECTOR_EXTENSION = "CREATE EXTENSION IF NOT EXISTS vector;\n";

    @Override
    public String getCreateTablePrerequisites(TableDef table)
    {
        for (ColumnDef column : table.getColumns().getAvailableElements())
        {
            if (column.getType().isVector())
                return CREATE_VECTOR_EXTENSION;
        }
        return "";
    }

    @Override
    public String getDefaultValue(ColumnDef column)
    {
        if (column.getDefaultValue().equalsIgnoreCase("now()"))
            return "NOW()";
        return super.getDefaultValue(column);
    }

    @Override
    public String getColumnDefinitionClause(ColumnDef column)
    {
        if (column.isAutoIncrement() && column.isPrimaryKey() &&
                column.getType().getTypeName().equals(SqlColumnType.TYPE_KEY))
            return normalizeIdentifier(column.getName()) + " BIGSERIAL PRIMARY KEY";
        return super.getColumnDefinitionClause(column);
    }

    @Override
    public String normalizeIdentifier(String identifier)
    {
        if (identifier == null)
            return null;
        if (KEYWORDS.contains(identifier.toUpperCase()) || identifier.startsWith("___"))
            return '"' + identifier + '"';
        return normalizeIdentifierCase(identifier);
    }

    @Override
    public String normalizeIdentifierCase(String identifier)
    {
        return identifier == null ? null : identifier.toLowerCase();
    }

    @Override
    public String getStartingIncrementDefinition(String table, String column, long startValue)
    {
        return "ALTER SEQUENCE " + getSequenceName(table, column) + " RESTART WITH " + startValue + ";\n";
    }

    private String getSequenceName(String table, String column)
    {
        return normalizeIdentifier(table + "_" + column + "_seq");
    }

    @Override
    public String getDropColumnStatements(ColumnDef column)
    {
        String dropColumnStatements = super.getDropColumnStatements(column);
        if (column.isAutoIncrement())
            dropColumnStatements += "DROP SEQUENCE IF EXISTS " +
                    getSequenceName(column.getEntity().getName(), column.getName()) + ";\n";
        if (column.isPrimaryKey())
            dropColumnStatements += getDropIndexClause(column.getEntity().getName() + "_pkey",
                    column.getEntity().getName()) + ";\n";
        return dropColumnStatements;
    }

    @Override
    public String getAddColumnStatements(ColumnDef column)
    {
        if (column.isAutoIncrement() && column.isPrimaryKey() &&
                column.getType().getTypeName().equals(SqlColumnType.TYPE_KEY))
        {
            String seq = getSequenceName(column.getEntity().getName(), column.getName());
            return "DROP SEQUENCE IF EXISTS " + seq + ";\nCREATE SEQUENCE " + seq +
                    ";\nALTER TABLE " + normalizeIdentifier(column.getTable().getEntityName()) +
                    " ADD COLUMN " + normalizeIdentifier(column.getName()) +
                    " BIGINT DEFAULT nextval('" + seq + "'::regclass) PRIMARY KEY;\n";
        }
        if (column.getType().isVector())
            return CREATE_VECTOR_EXTENSION + super.getAddColumnStatements(column);
        return super.getAddColumnStatements(column);
    }

    /**
     * Index method: explicitly specified one or GIN for single JSONB column index.
     */
    private String getIndexMethod(IndexDef indexDef)
    {
        if (!indexDef.getMethod().isEmpty())
            return indexDef.getMethod();
        Optional<IndexColumnDef> col = indexDef.stream().collect(MoreCollectors.onlyOne())
                .filter(c -> !c.isFunctional());
        if (col.isPresent())
        {
            ColumnDef columnDef = indexDef.getTable().getColumns().get(col.get().getName());
            if (columnDef != null && columnDef.getType().getTypeName().equals(SqlColumnType.TYPE_JSONB))
                return "gin";
        }
        return "";
    }

    @Override
    public String getCreateIndexClause(IndexDef indexDef)
    {
        String method = getIndexMethod(indexDef);
        if (method.isEmpty() && indexDef.getOperatorClass().isEmpty() && indexDef.getOptions().isEmpty())
            return super.getCreateIndexClause(indexDef);

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE ");
        if (indexDef.isUnique())
            sb.append("UNIQUE ");
        sb.append("INDEX ");
        sb.append(normalizeIdentifier(indexDef.getName()));
        sb.append(" ON ");
        sb.append(normalizeIdentifier(indexDef.getTable().getEntityName()));
        if (!method.isEmpty())
            sb.append(" USING ").append(method.toUpperCase());
        sb.append(" (");
        sb.append(indexDef.stream().map(c -> c.isFunctional() || indexDef.getOperatorClass().isEmpty()
                ? c.getDefinition() : c.getDefinition() + " " + indexDef.getOperatorClass()).joining(", "));
        sb.append(")");
        if (!indexDef.getOptions().isEmpty())
            sb.append(" WITH (").append(indexDef.getOptions()).append(")");
        sb.append(";");
        return sb.toString();
    }
}
