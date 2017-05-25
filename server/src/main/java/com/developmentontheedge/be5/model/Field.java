package com.developmentontheedge.be5.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.developmentontheedge.be5.api.operation.Option;
import com.developmentontheedge.be5.metadata.model.Query;

@Deprecated
public class Field
{
    public static class Group
    {
        public final String id;
        public final String name;

        public Group(String id, String name)
        {
            this.id = id;
            this.name = name;
        }
    }

    public static class Builder
    {
        private final String name;
        private final String title;
        private final boolean isReadOnly;
        private final boolean canBeNull;
        private boolean autoRefresh;
        private final boolean reloadOnChange;
        private final FieldClarifyingFeatures tips;
        private Group group;
        
        private Builder(String name, String title, boolean isReadOnly, boolean canBeNull, boolean reloadOnChange, FieldClarifyingFeatures tips)
        {
            Objects.requireNonNull(name,  "name must not be null");
            Objects.requireNonNull(title, "title must not be null");
            Objects.requireNonNull(tips,  "name must not be null");
            this.name = name;
            this.title = title;
            this.isReadOnly = isReadOnly;
            this.canBeNull = canBeNull;
            this.reloadOnChange = reloadOnChange;
            this.tips = tips;
        }
        
        public Builder group(String id, String name)
        {
            this.group = new Group(id, name);
            return this;
        }
        
        public Builder autoRefresh(boolean value)
        {
            this.autoRefresh = value;
            return this;
        }
        
        public Field date(String value)
        {
            // TODO change value type
            return input("date", value);
        }
        
        public Field dateTime(String value)
        {
            // TODO change value type
            return input("dateTime", value);
        }
        
        public Field textInput(String value)
        {
            return input("textInput", value);
        }
        
        public Field passwordInput(String value)
        {
            return input("passwordInput", value);
        }
        
        private Field input(String inputType, String value)
        {
            return new Field(name, title, isReadOnly, canBeNull, autoRefresh, reloadOnChange, inputType, value, Collections.emptyList(), null, null, tips, null, group);
        }
        
        public Field checkBox(String value)
        {
            return input("checkBox", value );
        }
        
        public Field comboBox(String value, List<Option> options)
        {
            return new Field(name, title, isReadOnly, canBeNull, autoRefresh, reloadOnChange, "comboBox", value, Collections.unmodifiableList(options), null, null, tips, null, group);
        }
        
        public Field autoComplete(String value, Query selectionView)
        {
            return new Field(name, title, isReadOnly, canBeNull, autoRefresh, reloadOnChange, "chooser", value, Collections.emptyList(), null, null, tips, selectionView, group);
        }
        
        public Field textArea(String value, int nColumns, int nRows)
        {
            return new Field(name, title, isReadOnly, canBeNull, autoRefresh, reloadOnChange, "textArea", value, Collections.emptyList(), nColumns, nRows, tips, null, group);
        }
    }

    public static Builder builder(String name, String title, boolean isReadOnly, boolean canBeNull, boolean reloadOnChange, FieldClarifyingFeatures tips)
    {
        return new Builder(name, title, isReadOnly, canBeNull, reloadOnChange, tips);
    }
    
    public final String name;
    public final String title;
    public final boolean isReadOnly;
    public final boolean canBeNull;
    public final boolean reloadOnChange;
    public final boolean autoRefresh;
    public final String type;
    public final String value;
    public final List<Option> options;
    public final Integer columns;
    public final Integer rows;
    public final FieldClarifyingFeatures tips;
    public final String entityName;
    public final String queryName;
    public final Group group;
    
    private Field(String name, String title, boolean isReadOnly, boolean canBeNull, boolean autoRefresh, boolean reloadOnChange, String type, String value, List<Option> options, Integer columns, Integer rows, FieldClarifyingFeatures tips, Query selectionView, Group group)
    {
        this.name = name;
        this.title = title;
        this.isReadOnly = isReadOnly;
        this.canBeNull = canBeNull;
        this.reloadOnChange = reloadOnChange;
        this.autoRefresh = autoRefresh;
        this.type = type;
        this.value = value;
        this.options = options;
        this.columns = columns;
        this.rows = rows;
        this.tips = tips;
        this.group = group;
        this.entityName = selectionView == null ? null : selectionView.getEntity().getName();
        this.queryName = selectionView == null ? null : selectionView.getName();
    }
}
