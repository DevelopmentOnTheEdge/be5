<#include 'dict/languages'/>
<#include 'dict/mimeTypes'/>
<#include 'dict/systemSettings'/>

DELETE FROM users WHERE user_name = 'Administrator';
DELETE FROM user_roles WHERE user_name = 'Administrator';

INSERT INTO users( user_name, user_pass ) VALUES( 'Administrator', '12345' );
INSERT INTO user_roles VALUES( 'Administrator', 'Administrator' );
