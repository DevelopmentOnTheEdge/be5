contribute(context(ctype: "com.developmentontheedge.be5.api.services.databasemodel.DatabaseModel")) {

    method name: 'getAt', type: 'com.developmentontheedge.be5.api.services.databasemodel.EntityModel', params: [entityName: String]

<#list entityNames as table>
    property(name: "${table}", type: 'com.developmentontheedge.be5.api.services.databasemodel.EntityModel')
</#list>
}

contribute(context(ctype: "com.developmentontheedge.be5.api.services.databasemodel.EntityModel")) {

    method name: 'leftShift', type: 'java.lang.String', params: [values: 'Map<String, ? super Object>']

    method name: 'getAt', type: 'com.developmentontheedge.be5.api.services.databasemodel.RecordModel', params: [id: String]

    method name: 'getAt', type: 'com.developmentontheedge.be5.api.services.databasemodel.RecordModel', params: [id: long]

    method name: 'call', type: 'com.developmentontheedge.be5.api.services.databasemodel.RecordModel', params: [values: 'Map<String, ? super Object>']

    method name: 'putAt', type: 'void', params: [id: String, values: 'Map<String, ? super Object>']

    method name: 'putAt', type: 'void', params: [id: String, values: 'com.developmentontheedge.beans.DynamicPropertySet']

}

