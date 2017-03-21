<#-- TODO: fix (our MultiSqlParser will unlikely to parse this) -->
CREATE OR REPLACE PROCEDURE drop_if_exists( tableNameIn VARCHAR2 ) IS
table_exist INTEGER;
BEGIN 
SELECT COUNT(*) INTO table_exist FROM user_tables WHERE table_name = UPPER( TRIM(tableNameIn) );
IF table_exist = 1 THEN 
    EXECUTE IMMEDIATE 'DROP TABLE ' || tableNameIn  || ' CASCADE CONSTRAINTS';
    RETURN;
END IF;
SELECT COUNT(*) INTO table_exist FROM user_views WHERE view_name = UPPER( TRIM(tableNameIn) );
IF table_exist = 1 THEN 
    EXECUTE IMMEDIATE 'DROP VIEW ' || tableNameIn;
    RETURN;
END IF;
SELECT COUNT(*) INTO table_exist FROM user_indexes WHERE index_name = UPPER( TRIM(tableNameIn) );
IF table_exist = 1 THEN 
    EXECUTE IMMEDIATE 'DROP INDEX ' || tableNameIn;
    RETURN;
END IF;
SELECT COUNT(*) INTO table_exist FROM user_sequences WHERE sequence_name = UPPER( TRIM(tableNameIn) );
IF table_exist = 1 THEN 
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || tableNameIn;
    RETURN;
END IF;
END drop_if_exists; ;;


CREATE OR REPLACE PROCEDURE drop_if_exists_index( indexIn VARCHAR2 ) IS
index_exist INTEGER;
BEGIN 
SELECT COUNT(*) INTO index_exist FROM user_indexes WHERE index_name = UPPER( TRIM(indexIn) );
IF index_exist = 1 THEN
    EXECUTE IMMEDIATE 'DROP INDEX ' || indexIn;
    RETURN;
END IF;
END drop_if_exists_index; ;;


CREATE OR REPLACE PROCEDURE drop_if_exists_column( tableNameIn VARCHAR2, columnName VARCHAR2 ) IS
column_exist INTEGER;
BEGIN
    SELECT COUNT(*) INTO column_exist
    FROM all_tab_cols
    WHERE table_name = UPPER( TRIM( tableNameIn ) )
        AND column_name = UPPER( TRIM( columnName ) );
    IF column_exist = 1 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ' || tableNameIn || ' DROP COLUMN ' || columnName;
        RETURN;
    END IF;
END drop_if_exists_column; ;;


CREATE OR REPLACE PROCEDURE add_column_if_not_exists( tableNameIn VARCHAR2, columnName VARCHAR2, columnType VARCHAR2 ) IS
table_exist INTEGER;
BEGIN 
    SELECT COUNT(*)
    INTO table_exist
    FROM all_tab_cols 
    WHERE table_name = UPPER( TRIM( tableNameIn ) )
    AND column_name = UPPER( TRIM( columnName ) );
    
    IF table_exist = 0 THEN 
        EXECUTE IMMEDIATE 'ALTER TABLE ' || tableNameIn  || ' ADD ' || columnName || ' ' || columnType;
        RETURN;
    END IF;
END add_column_if_not_exists; ;;
 