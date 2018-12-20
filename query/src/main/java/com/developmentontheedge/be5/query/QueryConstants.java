package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.metadata.DatabaseConstants;

public interface QueryConstants
{
    String OFFSET = "_offset_";
    String LIMIT = "_limit_";
    String ORDER_COLUMN = "_orderColumn_";
    String ORDER_DIR = "_orderDir_";

    String DISPLAY_TYPE = "_displayType_";
    String TITLE_LEVEL = "_titleLevel_";

    String CLEAN_NAV = "_cleanNav_";

    String COL_ATTR_AGGREGATE = "aggregate";
    String COL_ATTR_ROLES = "roles";
    String COL_ATTR_BLANKNULLS = "blankNulls";
    String CSS_ROW_CLASS = DatabaseConstants.HIDDEN_COLUMN_PREFIX + "css_class";

    String COL_ATTR_NULLIF = "nullif";
    String COL_ATTR_LINK = "link";
    String COL_ATTR_REF = "ref";
    String COL_ATTR_URL = "url";
}