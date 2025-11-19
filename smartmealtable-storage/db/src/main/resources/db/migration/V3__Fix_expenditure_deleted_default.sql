-- Ensure expenditure.deleted has a default value so bulk inserts can omit the column.
-- Older schemas defined the column as NOT NULL without a default, causing loader crashes.

ALTER TABLE expenditure
    MODIFY COLUMN deleted BIT(1) NOT NULL DEFAULT b'0';
