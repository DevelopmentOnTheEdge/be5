package com.developmentontheedge.be5.modules.core.genegate;

import com.developmentontheedge.be5.operation.OperationSupport;

public class CoreEntityFields
{
    abstract class DocTypesFieldsOperationSupport extends OperationSupport{
        final String CODE = DocTypesFields.CODE;
        final String Name = DocTypesFields.Name;
    }

    interface DocTypesFields {
        String CODE = "CODE";
        String Name = "Name";
    }

    interface DocTypes2Fields {
        String CODE = "CODE";
        String Name2 = "Name2";
    }

}
