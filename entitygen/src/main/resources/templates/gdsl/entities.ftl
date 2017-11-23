contribute(context(ctype: "com.developmentontheedge.be5.databasemodel.impl.DatabaseModel")) {
<#list entityNames as table>
    property(name: "${table}", type: 'com.developmentontheedge.be5.databasemodel.EntityModel')
</#list>
}

contribute(context(ctype: "com.developmentontheedge.be5.databasemodel.EntityModel")) {

    method name: 'leftShift', type: 'java.lang.String', params: [values: 'Map<String, ? super Object>']

    method name: 'getAt', type: 'com.developmentontheedge.be5.databasemodel.RecordModel', params: [id: String]

    method name: 'getAt', type: 'com.developmentontheedge.be5.databasemodel.RecordModel', params: [id: long]

    method name: 'call', type: 'com.developmentontheedge.be5.databasemodel.RecordModel', params: [values: 'Map<String, ? super Object>']

    method name: 'putAt', type: 'void', params: [id: String, values: 'Map<String, ? super Object>']

    method name: 'putAt', type: 'void', params: [id: String, values: com.developmentontheedge.beans.DynamicPropertySet]

}

