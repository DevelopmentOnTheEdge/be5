package com.developmentontheedge.be5.metadata.sql.type;

import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexDef;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;
import com.developmentontheedge.be5.metadata.model.TableDef;

public interface DbmsTypeManager
{
    void correctType(SqlColumnType type);

    String getKeyType();

    String getTypeClause(SqlColumnType type);

    String getCreateTableClause(String name);

    /**
     * Statements which must be executed before the table can be created,
     * e.g. CREATE EXTENSION for columns of extension types. Empty string if nothing is required.
     */
    String getCreateTablePrerequisites(TableDef table);

    String getConstraintClause(ColumnDef column);

    String getColumnDefinitionClause(ColumnDef column);

    String getAlterColumnStatements(ColumnDef newColumn, ColumnDef oldColumn);

    String normalizeIdentifier(String identifier);

    String normalizeIdentifierCase(String identifier);

    String getAddColumnStatements(ColumnDef column);

    String getDropColumnStatements(ColumnDef column);

    String getRenameColumnStatements(ColumnDef column, String newName);

    void addCanBeNullAndAndConstraintClause(ColumnDef column, StringBuilder sb);

    String getDropTableStatements(String table);

    String getDropIndexClause(String index, String table);

    String getStartingIncrementDefinition(String table, String column, long startValue);

    boolean isFunctionalIndexSupported();

    boolean isCustomAutoincrementSupported();

    boolean isGeneratedColumnSupported();

    String getColumnTriggerDefinition(ColumnDef column);

    String getDropTriggerDefinition(ColumnDef oldColumnNewName);

    String getCreateIndexClause(IndexDef indexDef);
}
