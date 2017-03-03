package com.developmentontheedge.be5.metadata.sql.type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexDef;
import com.developmentontheedge.be5.metadata.model.SqlColumnType;


public class M4TypeManager extends DefaultTypeManager
{
    private static Set<String> KEYWORDS = new HashSet<>( Arrays.asList( "SELECT", "KEY", "ORDER", "TABLE", "WHERE", "GROUP", "FROM", "TO",
            "BY", "JOIN", "LEFT", "INNER", "OUTER", "NUMBER", "DISTINCT", "COMMENT", "START", "END", "INDEX", "DATE", "LEVEL" ) );

    @Override
    public String getTypeClause( SqlColumnType type )
    {
        switch(type.getTypeName())
        {
        case SqlColumnType.TYPE_KEY:
            return "_DBMS_FOREIGN_KEY_TYPE";
        case SqlColumnType.TYPE_DECIMAL:
            return "_DBMS_DECIMAL("+type.getSize()+","+type.getPrecision()+")";
        case SqlColumnType.TYPE_CHAR:
        case SqlColumnType.TYPE_VARCHAR:
            return "_DBMS_"+type.getTypeName().toUpperCase()+"("+type.getSize()+")";
        default:
            return "_DBMS_"+type.getTypeName().toUpperCase();
        }
    }

    @Override
    public String getTypeClause( ColumnDef column )
    {
        if(column.getTypeString().equals( "VARCHAR(100)" ) && (column.getName().equals( "whoInserted___" ) || column.getName().equals( "whoModified___")))
            return "_DBMS_USER_ID";
        if(column.getTypeString().equals( "VARCHAR(50)" ) && column.getPermittedTables() != null && column.getPermittedTables().length > 0)
            return "_DBMS_GENERIC_REF";
        return super.getTypeClause( column );
    }

    @Override
    public String getDefaultValue( ColumnDef column )
    {
        if(column.getType().getTypeName().equals( SqlColumnType.TYPE_DATE ))
            return "_DBMS_DATA_CAST_AS_DATE("+column.getDefaultValue()+")";
        return super.getDefaultValue( column );
    }

    @Override
    public String getColumnDefinitionClause( ColumnDef column )
    {
        if(column.getType().getTypeName().equals( SqlColumnType.TYPE_ENUM ))
        {
            int maxLen = 0;
            for(String enumValue : column.getType().getEnumValues())
            {
                maxLen = Math.max(maxLen, enumValue.length());
            }
            String result = "_DBMS_ENUM( " + normalizeIdentifier( column.getName() ) + ", _DBMS_VARCHAR( " + maxLen + " ), ['"
                + String.join( "','", column.getType().getEnumValues() ) + "']";
            if ( getDefaultValue( column ) != null )
            {
                result += ", DEFAULT " + getDefaultValue( column );
            }
            result += " )";
            if(!column.isCanBeNull())
                result += " NOT NULL";
            return result;
        }
        if(column.isPrimaryKey() && column.isAutoIncrement() && column.getType().getTypeName().equals( SqlColumnType.TYPE_KEY ))
        {
            return normalizeIdentifier( column.getName() )+" _DBMS_PRIMARY_KEY_TYPE";
        }
        if(column.getType().getTypeName().equals( SqlColumnType.TYPE_BOOL ) && !column.isCanBeNull())
        {
            return "_DBMS_BOOLEAN("+normalizeIdentifier( column.getName() )+")";
        }
        // TODO Auto-generated method stub
        return super.getColumnDefinitionClause( column );
    }

    @Override
    public String normalizeIdentifier(String identifier)
    {
        if(identifier == null)
            return null;
        if(KEYWORDS.contains( identifier.toUpperCase( Locale.ENGLISH ) ))
            return "_quote("+identifier+")";
        return identifier;
    }
    
    @Override
    public String getCreateTableClause( String name )
    {
        return "_drop_if_exists_and_create_table(" + name + ")";
    }

    @Override
    public String getDropTableStatements( String table )
    {
        return "";
    }

    @Override
    public String getCreateIndexClause( IndexDef indexDef )
    {
        String idx = super.getCreateIndexClause( indexDef );
        if(indexDef.isFunctional())
            return "_DBMS_CREATE_FUNC_INDEX("+idx.substring( 0, idx.length()-1 )+");";
        return idx;
    }
    
    
}
