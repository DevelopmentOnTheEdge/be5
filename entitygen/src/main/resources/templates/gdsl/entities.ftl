contribute(context(ctype: "com.developmentontheedge.be5.databasemodel.impl.DatabaseModel")) {
<#list entityNames as table>
    property(name: "${table}", type: 'com.developmentontheedge.be5.databasemodel.EntityModel')
</#list>
}

contribute(context(ctype: "com.developmentontheedge.be5.databasemodel.EntityModel")) {

method name: 'leftShift', type: 'java.lang.String', params: [values: Map]

}

