-- Ensure the member table has audit timestamps required by the performance loader.
-- Some older local databases were provisioned before these columns were introduced,
-- which breaks bulk inserts that explicitly set created_at/updated_at.

SET @member_created_missing :=
    (SELECT COUNT(*) = 0
     FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND table_name = 'member'
       AND column_name = 'created_at');

SET @sql :=
    IF(@member_created_missing,
       'ALTER TABLE member ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT ''감사 필드 (도메인에 노출 안 함)'' AFTER recommendation_type',
       'SELECT ''member.created_at already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @member_updated_missing :=
    (SELECT COUNT(*) = 0
     FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND table_name = 'member'
       AND column_name = 'updated_at');

SET @sql :=
    IF(@member_updated_missing,
       'ALTER TABLE member ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT ''감사 필드 (도메인에 노출 안 함)'' AFTER created_at',
       'SELECT ''member.updated_at already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
