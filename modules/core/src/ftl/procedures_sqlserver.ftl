IF OBJECT_ID( N'drop_if_exists', N'P' ) IS NOT NULL
  DROP PROCEDURE drop_if_exists;

EXEC(N'
CREATE PROCEDURE drop_if_exists @tableName VARCHAR AS
  BEGIN
    IF EXISTS ( SELECT ID FROM sysobjects WHERE id = OBJECT_ID(@tableName) AND OBJECTPROPERTY(id, N''IsUserTable'') = 1 )
      BEGIN
        DROP TABLE N@tableName;
      END
  END;');

IF OBJECT_ID( N'drop_if_exists_index', N'P' ) IS NOT NULL
  DROP PROCEDURE drop_if_exists_index;

EXEC(N'
CREATE PROCEDURE drop_if_exists_index @indexName VARCHAR, @tableName VARCHAR AS
  BEGIN
    IF EXISTS( SELECT 1 FROM sys.indexes WHERE name = @indexName AND object_id = OBJECT_ID(@tableName) )
      BEGIN
        DROP INDEX N@indexName ON N@tableName;
      END
  END;');

IF OBJECT_ID( N'drop_if_exists_column', N'P' ) IS NOT NULL
  DROP PROCEDURE drop_if_exists_column;

EXEC(N'
CREATE PROCEDURE drop_if_exists_column @tableName VARCHAR, @columnName VARCHAR AS
  BEGIN
    IF EXISTS( SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(@tableName) AND name = @columnName )
      BEGIN
        ALTER TABLE N@tableName DROP COLUMN N@columnName;
      END
  END;');

IF OBJECT_ID( N'drop_all_column_constraints', N'P' ) IS NOT NULL
  DROP PROCEDURE drop_all_column_constraints;

EXEC(N'
CREATE PROCEDURE drop_all_column_constraints @tableName VARCHAR, @columnName VARCHAR AS
  BEGIN
    DECLARE @sql NVARCHAR(MAX)
    WHILE 1 = 1
      BEGIN
        SELECT TOP 1 @sql = N''ALTER TABLE '' + @tableName + N'' DROP CONSTRAINT ['' + dc.NAME + N'']''
        FROM sys.default_constraints dc
          JOIN sys.columns c
            ON c.default_object_id = dc.object_id
        WHERE
          dc.parent_object_id = OBJECT_ID(@tableName)
          AND c.name = @columnName
        IF @@ROWCOUNT = 0
          BREAK
        EXEC ( @sql )
      END
  END;');

IF OBJECT_ID( N'DBO.CLEAN_CHARS', N'FN' ) IS NOT NULL
  DROP FUNCTION DBO.CLEAN_CHARS;

EXEC(N'
CREATE FUNCTION DBO.CLEAN_CHARS (@str VARCHAR(8000), @validchars VARCHAR(8000))
  RETURNS VARCHAR(8000)
  BEGIN
    WHILE PATINDEX(''%[^'' + @validchars + '']%'',@str) > 0
      SET @str=REPLACE(@str, SUBSTRING(@str ,PATINDEX(''%[^'' + @validchars +'']%'',@str), 1) ,'''')
    RETURN @str
  END;');


IF OBJECT_ID( N'DBO.META_PHONE_RU', N'FN' ) IS NOT NULL
  DROP FUNCTION DBO.META_PHONE_RU;

EXEC(N'
CREATE FUNCTION DBO.META_PHONE_RU (@W varchar(4000))
RETURNS varchar(4000)
AS
BEGIN
DECLARE @alf varchar(4000), @cns1 varchar(4000), @cns2 varchar(4000), @cns3 varchar(4000), @ch varchar(4000), @ct varchar(4000), @number varchar(4000)

SET @alf = ''OОEЕAАИУЭЮЯПCСTТPРKКЛМMHНБBВГДЖЗЙФXХЦЧШЩЁЫ0123456789''
SET @number = ''0123456789''
SET @cns1 = ''БЗДBВГ''
SET @cns2 = ''ПCСTТФК''
SET @cns3 = ''ПCСTТKКБBВГДЖЗФХXЦЧШЩ''
SET @ch = ''AAOОYЮЕEЭЯЁЫCBTKPMX''
SET @ct = ''ААААУУИИИАИАСВТКРМХ''
-- @alf - алфавит кроме исключаемых букв, @cns1 и @cns2 - звонкие и глухие
-- согласные, @cns3 - согласные, перед которыми звонкие оглушаются,
-- @ch, @ct - образец и замена гласных

DECLARE @S varchar(4000), @V varchar(4000), @i int, @B int, @c char(1), @old_c char(1)
-- @S, @V - промежуточные строки, @i - счётчик цикла,
-- @B - позиция найденного элемента, @c - текущий символ

SET @W = UPPER(@W)
SET @S = ''''
SET @V = ''''

SET @i = 1
WHILE @i <= LEN(@W)
BEGIN
  SET @c = SUBSTRING(@W, @i, 1)
  IF CHARINDEX(@c, @alf)>0 SET @S = @S + @c
  SET @i=@i+1
END

IF LEN(@S) = 0 RETURN ''''

-- Заменяем окончания
IF LEN(@S)>6
SET @S = LEFT(@S, LEN(@S) - 6) +
CASE RIGHT(@S, 6)
  WHEN ''ОВСКИЙ'' THEN ''@''
  WHEN ''ЕВСКИЙ'' THEN ''#''
  WHEN ''ОВСКАЯ'' THEN ''$''
  WHEN ''ЕВСКАЯ'' THEN ''%''
  ELSE RIGHT(@S, 6)
END

IF LEN(@S)>4
SET @S = LEFT(@S, LEN(@S) - 4) +
CASE RIGHT(@S, 4)
  WHEN ''ИЕВА'' THEN ''9''
  WHEN ''ЕЕВА'' THEN ''9''
  ELSE RIGHT(@S, 4)
END

IF LEN(@S)>3
SET @S = LEFT(@S, LEN(@S) - 3) +
CASE RIGHT(@S, 3)
  WHEN ''ОВА'' THEN ''9''
  WHEN ''ЕВА'' THEN ''9''
  WHEN ''ИНА'' THEN ''1''
  WHEN ''ИЕВ'' THEN ''4''
  WHEN ''ЕЕВ'' THEN ''4''
  WHEN ''НКО'' THEN ''3''
  ELSE RIGHT(@S, 3)
END

IF LEN(@S)>2
SET @S = LEFT(@S, LEN(@S) - 2) +
CASE RIGHT(@S, 2)
  WHEN ''ОВ'' THEN ''4''
  WHEN ''ЕВ'' THEN ''4''
  WHEN ''АЯ'' THEN ''6''
  WHEN ''ИЙ'' THEN ''7''
  WHEN ''ЫЙ'' THEN ''7''
  WHEN ''ЫХ'' THEN ''5''
  WHEN ''ИХ'' THEN ''5''
  WHEN ''ИН'' THEN ''8''
  WHEN ''ИК'' THEN ''2''
  WHEN ''ЕК'' THEN ''2''
  WHEN ''УК'' THEN ''0''
  WHEN ''ЮК'' THEN ''0''
  ELSE RIGHT(@S, 2)
END

-- большевистская и бальшивицкая
IF CHARINDEX(''СТС'',@S)>1
SET @S = REPLACE(@S,''СТС'',''Ц'')
IF CHARINDEX(''ТС'',@S)>1
SET @S = REPLACE(@S,''ТС'',''Ц'')

-- Оглушаем последний символ, если он - звонкий согласный:
SET @B = CHARINDEX(RIGHT(@S, 1), @cns1)
IF @B > 0
  SET @S = LEFT(@S, LEN(@S)-1) + SUBSTRING(@cns2, @B, 1)

SET @old_c = '' ''
SET @i = 1
WHILE @i <= LEN(@S)
BEGIN
  SET @c = SUBSTRING(@S, @i, 1)
  SET @B = CHARINDEX(@c, @ch)
  IF @B > 0
  BEGIN
    IF @old_c = ''Й'' OR @old_c = ''И''
    BEGIN
      IF @c = ''О'' OR @c = ''Е''
      BEGIN
        SET @old_c = ''И''
        SET @S = LEFT(@S, LEN(@S)-1) + @old_c
      END
      ELSE
        IF @c <> @old_c SET @V = @V + SUBSTRING(@ct, @B, 1)
    END
    ELSE
    BEGIN
      IF @c <> @old_c SET @V = @V + SUBSTRING(@ct, @B, 1)
    END
  END
  ELSE
  BEGIN
    IF @c <> @old_c
      AND CHARINDEX(@c, @cns3)>0
    BEGIN
      SET @B = CHARINDEX(@old_c, @cns1)
      IF @B>0
      BEGIN
        SET @old_c = SUBSTRING(@cns2, @B, 1)
        SET @V = LEFT(@V, LEN(@V)-1) + @old_c
      END
    END
    IF @c <> @old_c SET @V = @V + @c
    IF @c = @old_c AND CHARINDEX(@c, @number)>0 SET @V = @V + @c
  END
  SET @old_c = @c
  SET @i = @i + 1
END

RETURN (@V)
END;');
 