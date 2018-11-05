<#macro _sqlMacro><#assign nested><#nested></#assign>${project.addSQLMacro(nested)}</#macro>

<@_sqlMacro>MACRO TEST_MACRO(column)CONCAT(column, ' test')END</@>
<#macro _test_macro column>CONCAT(${column}, ' test')</#macro>
