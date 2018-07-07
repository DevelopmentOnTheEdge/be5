package com.developmentontheedge.be5.base.model

import com.developmentontheedge.beans.json.JsonFactory
import groovy.transform.TypeChecked
import org.junit.Test

import static org.junit.Assert.assertEquals


@TypeChecked
class GroovyDPSTest {
    private GDynamicPropertySetSupport dps = new GDynamicPropertySetSupport()

    private static String getNewValue() {
        return "newValue"
    }

    @Test
    void test() {
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
            GROUP_CLASSES = "col-md-8"
            VALIDATION_RULES = new TestRange("1", "10")
            EXTRA_ATTRS = [["search": "all"]]
            COLUMN_SIZE_ATTR = 10
            INPUT_SIZE_ATTR = 10
            MESSAGE = "Can't be null"
            STATUS = "error"
            CSS_CLASSES = "col-lg-6"
            PLACEHOLDER = "Select..."
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

        dps.add("input4", "Input 4")

        dps.getProperty("input4").setValue("input4value")

        dps.edit("input3") { value = getNewValue() }

        assertEquals("newValue", dps["input3"])

        assertEquals("{'values':{'reasonMulti':123,'input2':'value2','input3':'newValue','input4':'input4value'},'meta':{'/reasonMulti':{'displayName':'Множественный выбор','type':'Integer','hidden':true,'reloadOnChange':true,'reloadOnFocusOut':true,'rawValue':true,'groupId':'1','groupName':'Test','groupClasses':'col-md-8','readOnly':true,'multipleSelectionList':true,'passwordField':true,'labelField':true,'cssClasses':'col-lg-6','columnSize':'10','inputSize':'10','placeholder':'Select...','status':'error','message':'Can't be null','defaultValue':'1234','tagList':[['fired','Уволен'],['other','Иная причина']],'extraAttrs':[],'validationRules':{'max':'10','min':'1'}},'/input2':{'displayName':'New Display Name','canBeNull':true},'/input3':{'displayName':'Input 3'},'/input4':{'displayName':'Input 4'}},'order':['/reasonMulti','/input2','/input3','/input4']}",
                oneQuotes(JsonFactory.dps(dps).toString()))
    }

    protected static String oneQuotes(Object s) {
        return s.toString().replace("\"", "'");
    }
}
