<#-- IDs for Oracle -->

<#if dbPlatform == 'oracle'>
DROP SEQUENCE beIDGenerator;

CREATE SEQUENCE beIDGenerator INCREMENT BY 1;
</#if>

<#-- Procedures -->
<#include 'procedures_'+dbPlatform+':optional' />
 