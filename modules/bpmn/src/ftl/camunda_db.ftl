CREATE SCHEMA IF NOT EXISTS camunda;

SET search_path to 'camunda';

<#include 'camunda/postgres_engine_7.12.0'/>
<#include 'camunda/postgres_identity_7.12.0'/>

SET search_path to 'public';
