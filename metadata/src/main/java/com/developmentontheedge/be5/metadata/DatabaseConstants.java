package com.developmentontheedge.be5.metadata;

/**
 * Temporary interface for imported and unclassified constants from BeanExplorer EE.
 */
public interface DatabaseConstants 
{
    static final String SELECTION_VIEW = "*** Selection view ***";

    // used in com.developmentontheedge.be5.components.impl.model.TableModel
    static final String COL_ATTR_AGGREGATE = "aggregate";
    static final String COL_ATTR_ROLES  = "roles";

    // constants from RecordEx, 
    // used in com.developmentontheedge.be5.components.impl.model.PropertiesToRowTransformer
    public static final String HIDDEN_COLUMN_PREFIX = "___";
    public static final String GLUE_COLUMN_PREFIX = "+";
    public static final String EXTRA_HEADER_COLUMN_PREFIX = ";";

    // used in com.developmentontheedge.be5.components.impl.model.PropertiesToRowTransformer
    static final String ID_COLUMN_LABEL = HIDDEN_COLUMN_PREFIX + "ID";

}
