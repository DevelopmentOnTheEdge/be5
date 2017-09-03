package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.beans.BeanInfoConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface Validation
{
    enum Status
    {
        SUCCESS, WARNING, ERROR;

        @Override
        public String toString()
        {
            return this.name().toLowerCase();
        }
    }

    /**
     * List of default validation rules. You can extend this list
     * in your app by simply adding new rules into it.
     */
    List<AbstractRule> defaultRules = Arrays.asList(
            //new RequiredRule(),
            new IntegerRule(),
            new NumberRule(),
            new EmailRule(),
            new UrlRule(),
            new DateRule()
    );

    // Name of the property attribute used to define validation rules in Java code.
    String RULES_ATTR = BeanInfoConstants.VALIDATION_RULES;

    // Names of the cache entries.
    String RULES_CACHE_ENTRY = "validationRules";
    String METHODS_CACHE_ENTRY = "validationMethods";

    // Database schema constants.
    String RULES_TABLE_NAME = "validationRules";
    String METHODS_TABLE_NAME = "validationMethods";
    String ENTITY_NAME = "entity_name";
    String PROPERTY_NAME = "property_name";
    String RULE = "rule";
    String MESSAGE = "message";
    String CODE = "code";
    String DEFAULT_MESSAGE = "defaultMessage";

    // Default rule names.
    @Deprecated String DIGITS = "digits";
    @Deprecated String PHONE = "phone";
    @Deprecated String EMAIL = "email2";
    @Deprecated String INTEGER = "integer";
    @Deprecated String NUMBER = "number";
    @Deprecated String DATE = "date";

    // for HttpSearchOperation
    String PATTERN = "pattern";
    String PATTERN2 = "pattern2";
    String REQUIRED = "required";
    String REMOTE = "remote";
    String UNIQUE = "unique";
    String QUERY = "query";
    String INTERVAL = "interval";
    String URL = "url2";
    String STARTS_WITH_DIGITS = "startWithDigits";

    String IP_MASK = "ipMask";

    String OWNER_IDS_IGNORED = "___MyOwnerIDsIgnored";

    // Default messages.

    @Deprecated String MESSAGE_DIGITS = "Please enter only digits.";
    @Deprecated String MESSAGE_EMAIL = "Please enter a valid email address.";
    @Deprecated String MESSAGE_NUMBER = "Please enter a valid number.";
    @Deprecated String MESSAGE_REQUIRED = "This field is required.";
    @Deprecated String MESSAGE_URL = "Please enter a valid URL.";
    @Deprecated String MESSAGE_INTEGER = "Please specify an integer number.";
    @Deprecated String MESSAGE_DATE = "Please enter a valid date.";
    @Deprecated String MESSAGE_TIME = "Please enter a valid time.";

    class UniqueStruct
    {
        public String entity;
        public String column;
        public String message;

        public Map<String,String> extraParams;
    }

    class QueryStruct
    {
        public String entity;
        public String query;
        public String message;

        public Map<String,String> extraParams;
    }

    class IntervalStruct
    {
        public Object intervalFrom;
        public Object intervalTo;
        public String message;
    }
}
