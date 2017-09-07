package com.developmentontheedge.be5.model.beans;

import com.developmentontheedge.beans.DynamicProperty;

class DynamicPropertyGBuilder
{
    class Attributes
    {
        private Map<String, Object> map = new HashMap<>();

        void name(String name) {
            map.put("name", name)
        }

        void DISPLAY_NAME(String DISPLAY_NAME) {
            map.put("DISPLAY_NAME", DISPLAY_NAME)
        }
        void MULTIPLE_SELECTION_LIST(boolean MULTIPLE_SELECTION_LIST) {
            map.put("MULTIPLE_SELECTION_LIST", MULTIPLE_SELECTION_LIST)
        }
        void TAG_LIST_ATTR(List<List<String>> TAG_LIST_ATTR) {
            map.put("TAG_LIST_ATTR", TAG_LIST_ATTR)
        }
        void value(List<String> value) {
            map.put("value", value)
        }
    }

    DynamicProperty of(@DelegatesTo(Attributes.class) final Closure config) {
        final Attributes model = new Attributes()
        model.with config

        return new DynamicProperty("test", String)
    }
}
