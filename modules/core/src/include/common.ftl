<#macro _sql>${project.enterSQL()}<#assign nested><#nested></#assign>${project.translateSQL(nested)}</#macro>

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

<#macro _tableRef tableFrom columnFrom tableTo columnTo viewName='*** Selection view ***'>
DELETE FROM table_refs WHERE tableFrom=${tableFrom?str} AND columnsFrom=${columnFrom?str?idCase};
INSERT INTO table_refs
<@_selectRow>${tableFrom?str}, ${columnFrom?str?idCase}, ${tableTo?str}, ${columnTo?str?idCase}, q.ID
FROM entities e LEFT JOIN queries q ON q.name = ${viewName?str} AND q.table_name = e.name
WHERE e.name = ${tableTo?str}</@_selectRow>;
</#macro>

<#macro _jsLibrary name>
DELETE FROM jsprograms WHERE publicId = '${name}';
INSERT INTO jsprograms ( publicId, type, code )
VALUES (${name?str}, 'library',
        <#assign nested><#nested></#assign>
        ${nested?str}
);
</#macro>

<#macro _jsHandler code name>
DELETE FROM javaScriptHandlers WHERE CODE = '${code}';
INSERT INTO javaScriptHandlers ( CODE, name, algorithmCode )
VALUES (${code?str}, ${name?str},
    <#assign nested><#nested></#assign>
    ${nested?str}
);
</#macro>


<@_sqlMacro>
MACRO PERSON_NAME(persons, default='')
COALESCE((COALESCE(persons.lastName, '') || ' ' || persons.firstName ||
CASE WHEN persons.middleName IS NULL
THEN ''
ELSE (' ' || persons.middleName)
END), default)
END

MACRO NAME(table)
COALESCE(table.shortName, table.name)
END
</@>

<#function PERSON_NAME persons default=''>
<#return coalesce(concat('${persons}.lastName'?orEmpty, ' '?str, '${persons}.firstName',
'CASE WHEN ${persons}.middleName IS NULL '+
'THEN \'\' '+
'ELSE '+concat(' '?str, '${persons}.middleName')+' END'), default?str)/>
</#function>

<#function NAME table>
<#return coalesce('${table}.shortName', '${table}.name')>
</#function>

<#function MONTH_RU monthNum>
<#assign result>
CASE WHEN ${monthNum}= 1 THEN 'январь'
    WHEN ${monthNum}= 2 THEN 'февраль'
    WHEN ${monthNum}= 3 THEN 'март'
    WHEN ${monthNum}= 4 THEN 'апрель'
    WHEN ${monthNum}= 5 THEN 'май'
    WHEN ${monthNum}= 6 THEN 'июнь'
    WHEN ${monthNum}= 7 THEN 'июль'
    WHEN ${monthNum}= 8 THEN 'август'
    WHEN ${monthNum}= 9 THEN 'сентябрь'
    WHEN ${monthNum}= 10 THEN 'октябрь'
    WHEN ${monthNum}= 11 THEN 'ноябрь'
    WHEN ${monthNum}= 12 THEN 'декабрь'
END
</#assign>
<#return result>
</#function>

<#macro IS_ACTIVE_NOT_NULL table date=''>
(
    ${table}.activeFrom <= <#if date == ''>${TIME_MACHINE_DATETIME()}<#else>${date}</#if>
    AND ${table}.activeTo > <#if date == ''>${TIME_MACHINE_DATETIME()}<#else>${date}</#if>
)
</#macro>


<#-- Macros from old BE modules
<#include 'core.ftl'>
-->
