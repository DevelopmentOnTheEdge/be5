CREATE OR REPLACE FUNCTION add_column_if_not_exists( tableName VARCHAR, columnName VARCHAR, columnType VARCHAR )
  RETURNS BOOLEAN AS '
BEGIN
  BEGIN
    EXECUTE ''ALTER TABLE '' || tableName || '' ADD COLUMN '' || columnName || ''  '' || columnType;
    EXCEPTION
    WHEN duplicate_column
      THEN RETURN 0;
  END;
  RETURN 1;
END;' LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION drop_if_exists_column( tableName VARCHAR, columnName VARCHAR )
  RETURNS BOOLEAN AS '
BEGIN
  BEGIN
    EXECUTE ''ALTER TABLE '' || tableName || '' DROP COLUMN '' || columnName;
    EXCEPTION
    WHEN undefined_column
      THEN RETURN 0;
  END;
  RETURN 1;
END;' LANGUAGE 'plpgsql';
 