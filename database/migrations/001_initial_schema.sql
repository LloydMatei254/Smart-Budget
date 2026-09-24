-- Smart Budget Database Schema
-- Migration 001: Initial Schema
-- Target: Neon PostgreSQL
-- Description: Creates initial tables for users, categories, expenses, income, and synchronization

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- USERS TABLE
-- =============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT currency_code CHECK (LENGTH(currency) = 3)
);

-- Indexes for users
CREATE INDEX idx_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_created_at ON users(created_at);

-- =============================================================================
-- CATEGORIES TABLE
-- =============================================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#6200EE',
    icon VARCHAR(50) DEFAULT 'category',
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT unique_category_per_user UNIQUE(user_id, name),
    CONSTRAINT color_hex_format CHECK (color ~* '^#[0-9A-F]{6}$')
);

-- Indexes for categories
CREATE INDEX idx_categories_user_id ON categories(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_categories_name ON categories(user_id, name) WHERE deleted_at IS NULL;

-- =============================================================================
-- PAYMENT METHODS TABLE
-- =============================================================================
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_payment_method_per_user UNIQUE(user_id, name)
);

-- Indexes for payment methods
CREATE INDEX idx_payment_methods_user_id ON payment_methods(user_id);

-- =============================================================================
-- EXPENSES TABLE
-- =============================================================================
CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    payment_method_id UUID REFERENCES payment_methods(id) ON DELETE SET NULL,
    amount NUMERIC(15, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    notes TEXT,
    expense_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    -- Synchronization fields
    sync_status VARCHAR(20) NOT NULL DEFAULT 'synced',
    sync_version INTEGER NOT NULL DEFAULT 1,
    local_id VARCHAR(100),
    
    CONSTRAINT positive_amount CHECK (amount > 0),
    CONSTRAINT valid_sync_status CHECK (sync_status IN ('synced', 'pending', 'conflict', 'deleted'))
);

-- Indexes for expenses (optimized for common queries)
CREATE INDEX idx_expenses_user_id ON expenses(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_expenses_user_category ON expenses(user_id, category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_expenses_category_date ON expenses(category_id, expense_date DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_expenses_created_at ON expenses(user_id, created_at DESC);
CREATE INDEX idx_expenses_updated_at ON expenses(user_id, updated_at DESC);
CREATE INDEX idx_expenses_sync_status ON expenses(user_id, sync_status) WHERE sync_status != 'synced';
CREATE INDEX idx_expenses_local_id ON expenses(local_id) WHERE local_id IS NOT NULL;

-- =============================================================================
-- INCOME TABLE
-- =============================================================================
CREATE TABLE income (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount NUMERIC(15, 2) NOT NULL,
    source VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    notes TEXT,
    income_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    -- Synchronization fields
    sync_status VARCHAR(20) NOT NULL DEFAULT 'synced',
    sync_version INTEGER NOT NULL DEFAULT 1,
    local_id VARCHAR(100),
    
    CONSTRAINT positive_income_amount CHECK (amount > 0),
    CONSTRAINT valid_income_sync_status CHECK (sync_status IN ('synced', 'pending', 'conflict', 'deleted'))
);

-- Indexes for income
CREATE INDEX idx_income_user_id ON income(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_income_user_date ON income(user_id, income_date DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_income_user_source ON income(user_id, source) WHERE deleted_at IS NULL;
CREATE INDEX idx_income_created_at ON income(user_id, created_at DESC);
CREATE INDEX idx_income_updated_at ON income(user_id, updated_at DESC);
CREATE INDEX idx_income_sync_status ON income(user_id, sync_status) WHERE sync_status != 'synced';
CREATE INDEX idx_income_local_id ON income(local_id) WHERE local_id IS NOT NULL;

-- =============================================================================
-- SYNC METADATA TABLE
-- =============================================================================
CREATE TABLE sync_metadata (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    last_synced_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sync_hash VARCHAR(64),
    conflict_resolved_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT unique_sync_record UNIQUE(user_id, entity_type, entity_id),
    CONSTRAINT valid_entity_type CHECK (entity_type IN ('expense', 'income', 'category'))
);

-- Indexes for sync metadata
CREATE INDEX idx_sync_metadata_user_entity ON sync_metadata(user_id, entity_type, entity_id);
CREATE INDEX idx_sync_metadata_last_synced ON sync_metadata(user_id, last_synced_at);

-- =============================================================================
-- REFRESH TOKENS TABLE (for JWT authentication)
-- =============================================================================
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP WITH TIME ZONE,
    device_info VARCHAR(255),
    
    CONSTRAINT valid_expiry CHECK (expires_at > created_at)
);

-- Indexes for refresh tokens
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash) WHERE revoked_at IS NULL;
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at) WHERE revoked_at IS NULL;

-- =============================================================================
-- FUNCTIONS AND TRIGGERS
-- =============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply updated_at trigger to all relevant tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payment_methods_updated_at BEFORE UPDATE ON payment_methods
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_expenses_updated_at BEFORE UPDATE ON expenses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_income_updated_at BEFORE UPDATE ON income
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to increment sync version on update
CREATE OR REPLACE FUNCTION increment_sync_version()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.sync_status = 'synced' AND OLD.sync_status != 'synced' THEN
        NEW.sync_version = OLD.sync_version;
    ELSE
        NEW.sync_version = OLD.sync_version + 1;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply sync version trigger to expenses and income
CREATE TRIGGER increment_expenses_sync_version BEFORE UPDATE ON expenses
    FOR EACH ROW EXECUTE FUNCTION increment_sync_version();

CREATE TRIGGER increment_income_sync_version BEFORE UPDATE ON income
    FOR EACH ROW EXECUTE FUNCTION increment_sync_version();

-- =============================================================================
-- COMMENTS
-- =============================================================================

COMMENT ON TABLE users IS 'Stores user account information';
COMMENT ON TABLE categories IS 'User-defined expense categories';
COMMENT ON TABLE payment_methods IS 'User-defined payment methods';
COMMENT ON TABLE expenses IS 'User expense transactions';
COMMENT ON TABLE income IS 'User income transactions';
COMMENT ON TABLE sync_metadata IS 'Tracks synchronization state for offline-first architecture';
COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens for authentication';

COMMENT ON COLUMN expenses.sync_status IS 'Synchronization status: synced, pending, conflict, deleted';
COMMENT ON COLUMN expenses.sync_version IS 'Version number for conflict resolution (increments on each update)';
COMMENT ON COLUMN expenses.local_id IS 'Client-side UUID for matching during sync';
