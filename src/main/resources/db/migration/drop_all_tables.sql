-- ============================================
-- Drop All Tables Script
-- Run this script to clean the database before testing Flyway
-- 
-- Usage:
--   psql -h localhost -U postgres -d potens_db -f drop_all_tables.sql
--   OR
--   ./drop_all_tables.sh
-- ============================================

-- Disable foreign key checks temporarily (PostgreSQL doesn't support this directly,
-- but CASCADE will handle dependencies)

-- Drop Flyway schema history table first
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Drop all child tables first (in reverse dependency order)
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS commissions CASCADE;
DROP TABLE IF EXISTS claims CASCADE;
DROP TABLE IF EXISTS leave_requests CASCADE;
DROP TABLE IF EXISTS bank_details CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS leads CASCADE;
DROP TABLE IF EXISTS attendances CASCADE;
DROP TABLE IF EXISTS salaries CASCADE;
DROP TABLE IF EXISTS sales_targets CASCADE;
DROP TABLE IF EXISTS sales CASCADE;

-- Drop independent/referenced tables
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS products CASCADE;

-- Drop employee table (referenced by many tables)
DROP TABLE IF EXISTS employees CASCADE;

-- Drop admin table (last, as it may have self-referencing foreign key)
DROP TABLE IF EXISTS admins CASCADE;

-- Drop any remaining tables in the public schema (catch-all)
DO $$ 
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename NOT LIKE 'pg_%'
    ) 
    LOOP
        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE';
    END LOOP;
END $$;

-- Drop all sequences (they will be recreated by Flyway)
DO $$ 
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT sequence_name 
        FROM information_schema.sequences 
        WHERE sequence_schema = 'public'
    ) 
    LOOP
        EXECUTE 'DROP SEQUENCE IF EXISTS ' || quote_ident(r.sequence_name) || ' CASCADE';
    END LOOP;
END $$;

-- Verify cleanup
SELECT 
    COUNT(*) as remaining_tables,
    CASE 
        WHEN COUNT(*) = 0 THEN 'Database is clean. Ready for Flyway migration!'
        ELSE 'Warning: Some tables still exist: ' || string_agg(tablename, ', ')
    END as status
FROM pg_tables 
WHERE schemaname = 'public' 
AND tablename NOT LIKE 'pg_%';
