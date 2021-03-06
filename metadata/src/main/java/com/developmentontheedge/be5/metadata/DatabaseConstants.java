package com.developmentontheedge.be5.metadata;

import java.util.Arrays;
import java.util.List;

/**
 * Temporary interface for imported and unclassified constants from BeanExplorer EE.
 */
public interface DatabaseConstants
{
    String HIDDEN_COLUMN_PREFIX = "___";
    String GLUE_COLUMN_PREFIX = "+";
    String EXTRA_HEADER_COLUMN_PREFIX = ";";

    String ALL_RECORDS_VIEW = "All records";
    String SELECTION_VIEW = "*** Selection view ***";

    String CURRENT_ROLE_LIST = "current-role-list";

    String ID_COLUMN_LABEL = HIDDEN_COLUMN_PREFIX + "ID";

    String L10N_TOPIC_DISPLAY_NAME = "displayName";
    String L10N_TOPIC_VIEW_NAME = "viewName";
    String L10N_TOPIC_VIEW_MENU_NAME = "viewMenuName";
    String L10N_TOPIC_VIEW_TITLE_NAME = "viewTitleName";
    String L10N_TOPIC_OPERATION_NAME = "operationName";
    String L10N_TOPIC_VIEW_SHORT_DESCRIPTION = "viewShortDescription";
    String L10N_TOPIC_PAGE = "page";
    String L10N_TOPIC_CODE = "code";
    String L10N_TOPIC_SCHEME = "scheme";
    String L10N_TOPIC_ATTRIBUTE = "attribute";
    String L10N_TOPIC_INSERT = "Insert";

    String WHO_INSERTED_COLUMN_NAME = "whoInserted___";
    String WHO_MODIFIED_COLUMN_NAME = "whoModified___";
    String CREATION_DATE_COLUMN_NAME = "creationDate___";
    String MODIFICATION_DATE_COLUMN_NAME = "modificationDate___";
    String IP_INSERTED_COLUMN_NAME = "ipInserted___";
    String IP_MODIFIED_COLUMN_NAME = "ipModified___";

    String IS_DELETED_COLUMN_NAME = "isDeleted___";

    List<String> specialColumns = Arrays.asList(
            WHO_INSERTED_COLUMN_NAME, WHO_MODIFIED_COLUMN_NAME,
            CREATION_DATE_COLUMN_NAME, MODIFICATION_DATE_COLUMN_NAME,
            IP_INSERTED_COLUMN_NAME, IP_MODIFIED_COLUMN_NAME,
            IS_DELETED_COLUMN_NAME);
}
