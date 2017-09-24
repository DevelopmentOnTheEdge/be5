package com.developmentontheedge.be5.model.beans

import com.developmentontheedge.be5.api.validation.Validation
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.json.JsonFactory
import groovy.transform.TypeChecked
import org.junit.Test

import static com.developmentontheedge.be5.api.validation.rule.BaseRule.*
import static com.developmentontheedge.be5.api.validation.rule.ValidationRules.*
import static com.developmentontheedge.be5.model.beans.DynamicPropertyGBuilder.*
import static org.junit.Assert.assertEquals

@TypeChecked
class TestAutocomplete extends Be5ProjectTest
{
    @Test
    void test()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        add (dps) {
            name          = "reasonMulti"
            TYPE          = Integer
            DISPLAY_NAME  = "Множественный выбор"
            TAG_LIST_ATTR = [["fired", "Уволен"], ["other", "Иная причина"]] as String[][]
            value         = 123
            RELOAD_ON_CHANGE = true
            RELOAD_ON_FOCUS_OUT = true
            MULTIPLE_SELECTION_LIST = true
            CAN_BE_NULL      = true
            READ_ONLY        = true
            HIDDEN           = true
            RAW_VALUE        = true
            LABEL_FIELD      = true
            PASSWORD_FIELD   = true
            GROUP_ID         = 1
            GROUP_NAME       = "Test"
            VALIDATION_RULES = baseRule(digits)
            EXTRA_ATTRS      = [["search":"all"]]
            COLUMN_SIZE_ATTR = 10
            CSS_CLASSES      = "col-lg-6"
            MESSAGE          = "Can't be null"
            STATUS           = Validation.Status.ERROR
        }

        assertEquals("{'values':{'reasonMulti':123},'meta':{'/reasonMulti':{'displayName':'Множественный выбор','type':'Integer','hidden':true,'reloadOnChange':true,'reloadOnFocusOut':true,'rawValue':true,'groupName':'Test','groupId':'1','readOnly':true,'canBeNull':true,'multipleSelectionList':true,'passwordField':true,'labelField':true,'cssClasses':'col-lg-6','columnSize':'10','status':'error','message':'Can't be null','tagList':[['fired','Уволен'],['other','Иная причина']],'extraAttrs':[],'validationRules':{'attr':'digits','type':'baseRule'}}},'order':['/reasonMulti']}",
                oneQuotes(JsonFactory.dps(dps).toString()))
    }

}
