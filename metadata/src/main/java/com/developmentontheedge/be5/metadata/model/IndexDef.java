package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.base.BeCaseInsensitiveCollection;
import com.developmentontheedge.be5.metadata.model.base.BeElementWithOriginModule;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.Strings2;
import com.developmentontheedge.beans.annot.PropertyName;
import com.developmentontheedge.dbms.SqlExecutor;

import java.util.Collections;
import java.util.List;

public class IndexDef extends BeCaseInsensitiveCollection<IndexColumnDef> implements DdlElement,
                                                                                     BeElementWithOriginModule
{
    private boolean unique;
    private String method = "";
    private String operatorClass = "";
    private String options = "";
    private String originModuleName;

    public IndexDef(String name, BeModelCollection<IndexDef> origin)
    {
        super(name, IndexColumnDef.class, origin, true);
        this.originModuleName = getModule().getName();
        propagateCodeChange();
    }

    public TableDef getTable()
    {
        return (TableDef) getOrigin().getOrigin();
    }

    @PropertyName("Unique")
    public boolean isUnique()
    {
        return unique;
    }

    public void setUnique(boolean unique)
    {
        this.unique = unique;
        fireCodeChanged();
    }

    /**
     * Index access method, e.g. hnsw, ivfflat, gin, gist (PostgreSQL only).
     * Empty string means the default method (btree).
     */
    @PropertyName("Method")
    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        String value = Strings2.nullToEmpty(method).trim().toLowerCase();
        this.method = value.equals("btree") ? "" : value;
        fireCodeChanged();
    }

    /**
     * Operator class applied to every non-functional index column,
     * e.g. vector_cosine_ops for pgvector hnsw/ivfflat index (PostgreSQL only).
     */
    @PropertyName("Operator class")
    public String getOperatorClass()
    {
        return operatorClass;
    }

    public void setOperatorClass(String operatorClass)
    {
        this.operatorClass = Strings2.nullToEmpty(operatorClass).trim().toLowerCase();
        fireCodeChanged();
    }

    /**
     * Index storage parameters, e.g. "m=16, ef_construction=64" for hnsw index (PostgreSQL only).
     */
    @PropertyName("Options")
    public String getOptions()
    {
        return options;
    }

    public void setOptions(String options)
    {
        this.options = normalizeOptions(options);
        fireCodeChanged();
    }

    private static String normalizeOptions(String options)
    {
        StringBuilder sb = new StringBuilder();
        for (String option : Strings2.nullToEmpty(options).split(","))
        {
            String[] parts = option.split("=", 2);
            String name = parts[0].trim().toLowerCase();
            if (name.isEmpty())
                continue;
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(name);
            if (parts.length > 1)
            {
                String value = parts[1].trim();
                if (value.length() > 1 && value.startsWith("'") && value.endsWith("'"))
                    value = value.substring(1, value.length() - 1);
                sb.append('=').append(value);
            }
        }
        return sb.toString();
    }

    public boolean isFunctional()
    {
        for (IndexColumnDef col : this)
        {
            if (col.isFunctional())
                return true;
        }
        return false;
    }

    @Override
    public String getDdl()
    {
        return getDropDdl() + getCreateDdl();
    }

    @Override
    @PropertyName("Definition")
    public String getCreateDdl()
    {
        Rdbms rdbms = getProject().getDatabaseSystem();
        if (rdbms == null)
            return "";
        return rdbms.getTypeManager().getCreateIndexClause(this);
    }

    @Override
    public String getDropDdl()
    {
        Rdbms rdbms = getProject().getDatabaseSystem();
        if (rdbms == null)
            return "";
        return rdbms.getTypeManager().getDropIndexClause(getName(), getTable().getEntityName()) + ";\n";
    }

    @Override
    public String getDiffDdl(DdlElement other, SqlExecutor sql)
    {
        if (!(other instanceof IndexDef))
            return getCreateDdl();
        if (((IndexDef) other).getCreateDdl().equalsIgnoreCase(getCreateDdl()))
            return "";
        return getDdl();
    }

    @Override
    public List<ProjectElementException> getErrors()
    {
        List<ProjectElementException> errors = super.getErrors();
        if (getName().length() > Constants.MAX_ID_LENGTH)
        {
            errors.add(new ProjectElementException(getCompletePath(), "name",
                    "Index name is too long: " + getName().length() +
                    " characters (" + Constants.MAX_ID_LENGTH + " allowed)"));
        }
        if (getSize() == 0)
        {
            errors.add(new ProjectElementException(this, "Index must have at least one column"));
        }
        return errors;
    }

    @Override
    public boolean hasErrors()
    {
        if (getSize() == 0)
            return true;
        return super.hasErrors();
    }

    @Override
    public String getEntityName()
    {
        return getTable().getEntityName();
    }

    @Override
    public String getDangerousDiffStatements(DdlElement other, SqlExecutor sql)
    {
        return "";
    }

    @Override
    public String getOriginModuleName()
    {
        return originModuleName;
    }

    @Override
    public void setOriginModuleName(String originModuleName)
    {
        this.originModuleName = originModuleName;
    }

    @Override
    public List<ProjectElementException> getWarnings()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isCustomized()
    {
        return getProject().getProjectOrigin().equals(originModuleName)
                && !getModule().getName().equals(originModuleName);
    }

    @Override
    public void merge(BeModelCollection<IndexColumnDef> other, boolean ignoreMyItems, boolean inherit)
    {
        // Do not merge indices at all
        // Currently new index with the same name totally rewrites parent index object
    }

    public boolean isValidIndex()
    {
        for (IndexColumnDef col : this)
        {
            ColumnDef column = getTable().getColumns().getCaseInsensitive(col.getName());
            if (column == null || !column.isAvailable())
                return false;
        }
        return true;
    }
}
