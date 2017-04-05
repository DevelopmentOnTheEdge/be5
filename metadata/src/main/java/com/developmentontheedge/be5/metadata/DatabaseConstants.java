package com.developmentontheedge.be5.metadata;

/**
 * Temporary interface for imported and unclassified constants from BeanExplorer EE.
 */
public interface DatabaseConstants 
{
    // constants from RecordEx,
    // used in com.developmentontheedge.be5.components.impl.model.PropertiesToRowTransformer
    public static final String HIDDEN_COLUMN_PREFIX = "___";
    public static final String GLUE_COLUMN_PREFIX = "+";
    public static final String EXTRA_HEADER_COLUMN_PREFIX = ";";


    static final String ALL_RECORDS_VIEW = "All records";
    static final String SELECTION_VIEW = "*** Selection view ***";

    // used in com.developmentontheedge.be5.components.impl.model.TableModel
    static final String COL_ATTR_AGGREGATE = "aggregate";
    static final String COL_ATTR_ROLES  = "roles";
    static final String COL_ATTR_BLANKNULLS = "blankNulls";

    static final String CSS_ROW_CLASS = HIDDEN_COLUMN_PREFIX + "css_class";

    // used in com.developmentontheedge.be5.components.impl.model.PropertiesToRowTransformer
    static final String ID_COLUMN_LABEL = HIDDEN_COLUMN_PREFIX + "ID";

    //TODO create enum COL_ATTR
    static final String COL_ATTR_NULLIF  = "nullif";
    static final String COL_ATTR_LINK = "link";

    static final String L10N_TOPIC_DISPLAY_NAME = "displayName";
    static final String L10N_TOPIC_VIEW_NAME = "viewName";
    static final String L10N_TOPIC_VIEW_MENU_NAME = "viewMenuName";
    static final String L10N_TOPIC_VIEW_TITLE_NAME = "viewTitleName";
    static final String L10N_TOPIC_OPERATION_NAME = "operationName";
    static final String L10N_TOPIC_VIEW_SHORT_DESCRIPTION = "viewShortDescription";
    static final String L10N_TOPIC_PAGE = "page";
    static final String L10N_TOPIC_CODE = "code";
    static final String L10N_TOPIC_SCHEME = "scheme";
    static final String L10N_TOPIC_ATTRIBUTE = "attribute";
    static final String L10N_TOPIC_INSERT = "Insert";
}
