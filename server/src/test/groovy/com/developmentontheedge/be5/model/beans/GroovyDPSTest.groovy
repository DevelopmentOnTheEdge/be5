package com.developmentontheedge.be5.model.beans

import com.developmentontheedge.be5.api.validation.Validation
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.json.JsonFactory
import groovy.transform.TypeChecked
import org.junit.Test


import static com.developmentontheedge.be5.api.validation.rule.BaseRule.digits
import static com.developmentontheedge.be5.api.validation.rule.ValidationRules.baseRule
import static org.junit.Assert.assertEquals


@TypeChecked
class GroovyDPSTest extends Be5ProjectTest
{
    private GDynamicPropertySetSupport dps = new GDynamicPropertySetSupport()

    private static String getNewValue()
    {
        return "newValue"
    }

    @Test
    void test()
    {
        dps.add {
            name = "reasonMulti"
            TYPE = Integer
            DISPLAY_NAME = "Множественный выбор"
            TAG_LIST_ATTR = [["fired", "Уволен"], ["other", "Иная причина"]] as String[][]
            value = 123
            DEFAULT_VALUE = 1234
            RELOAD_ON_CHANGE = true
            RELOAD_ON_FOCUS_OUT = true
            MULTIPLE_SELECTION_LIST = true
            READ_ONLY = true
            HIDDEN = true
            RAW_VALUE = true
            LABEL_FIELD = true
            PASSWORD_FIELD = true
            GROUP_ID = 1
            GROUP_NAME = "Test"
            VALIDATION_RULES = baseRule(digits)
            EXTRA_ATTRS = [["search": "all"]]
            COLUMN_SIZE_ATTR = 10
            MESSAGE = "Can't be null"
            STATUS = Validation.Status.ERROR
            CSS_CLASSES = "col-lg-6"
        }

        dps.add("input2") {
            value = "value2"
        }

        dps.edit("input2", "New Display Name") {
            CAN_BE_NULL = true
        }

        dps.add("input3", "Input 3") {
            value = "value3"
        }

        dps.edit("input3") { value = getNewValue() }

        assertEquals("newValue", dps["input3"].getValue() )

        assertEquals("{'values':{'reasonMulti':123,'input2':'value2','input3':'newValue'},'meta':{'/reasonMulti':{'displayName':'Множественный выбор','type':'Integer','hidden':true,'reloadOnChange':true,'reloadOnFocusOut':true,'rawValue':true,'groupName':'Test','groupId':'1','readOnly':true,'multipleSelectionList':true,'passwordField':true,'labelField':true,'cssClasses':'col-lg-6','columnSize':'10','status':'error','message':'Can't be null','defaultValue':'1234','tagList':[['fired','Уволен'],['other','Иная причина']],'extraAttrs':[],'validationRules':{'attr':'digits','type':'baseRule'}},'/input2':{'displayName':'New Display Name','canBeNull':true},'/input3':{'displayName':'Input 3'}},'order':['/reasonMulti','/input2','/input3']}",
                oneQuotes(JsonFactory.dps(dps).toString()))
    }

}
