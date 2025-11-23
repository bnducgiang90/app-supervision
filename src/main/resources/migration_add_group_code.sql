-- Migration script to add group_code column to existing tables
-- Run this script if you have existing data in your database

-- Add group_code column to groups table
ALTER TABLE groups ADD COLUMN IF NOT EXISTS group_code VARCHAR(20);

-- Add unique constraint and index
CREATE UNIQUE INDEX IF NOT EXISTS idx_groups_group_code ON groups(group_code) WHERE group_code IS NOT NULL;

-- Add group_code column to messages table
ALTER TABLE messages ADD COLUMN IF NOT EXISTS group_code VARCHAR(20);

-- Add index for group_code in messages
CREATE INDEX IF NOT EXISTS idx_messages_group_code ON messages(group_code);

-- Update existing groups with generated group codes
-- Note: You may need to manually update existing groups or create a script to generate codes
-- Example update (you should generate unique codes for each group):
-- UPDATE groups SET group_code = 'ABC123' WHERE id = 1;
-- UPDATE groups SET group_code = 'XYZ789' WHERE id = 2;

-- Update existing messages with group_code from their groups
UPDATE messages m
SET group_code = g.group_code
FROM groups g
WHERE m.group_id = g.id AND m.group_code IS NULL;

-- Make group_code NOT NULL after updating existing data
-- Uncomment these lines after you've updated all existing records:
-- ALTER TABLE groups ALTER COLUMN group_code SET NOT NULL;
-- ALTER TABLE messages ALTER COLUMN group_code SET NOT NULL;

