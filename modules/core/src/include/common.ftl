<#macro _sqlMacro><#assign nested><#nested></#assign>${project.addSQLMacro(nested)}</#macro>

<#macro _copyQuery name>${entity.getQueries().get(name).getFinalQuery()}</#macro>

<#macro _copySelectionQuery><@_copyQuery "*** Selection view ***"/></#macro>

<#macro _copyAllRecordsQuery><@_copyQuery "All records"/></#macro>

<#macro _bold><#assign nested><#nested></#assign>${concat('<b>'?str, nested, '</b>'?str)}</#macro>

<#macro _italic><#assign nested><#nested></#assign>${concat('<i>'?str, nested, '</i>'?str)}</#macro>

<#macro _systemSetting category key value>
DELETE FROM systemSettings WHERE section_name = ${category?str} AND setting_name = ${key?str};
INSERT INTO systemSettings (section_name, setting_name, setting_value) VALUES (${category?str}, ${key?str}, ${value?str});
</#macro>
