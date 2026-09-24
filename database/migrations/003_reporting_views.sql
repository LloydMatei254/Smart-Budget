-- Smart Budget Database Schema
-- Migration 003: Reporting Views and Functions
-- Description: Creates optimized views and functions for financial reporting and analytics

-- =============================================================================
-- VIEW: User Financial Summary
-- =============================================================================
CREATE OR REPLACE VIEW v_user_financial_summary AS
SELECT 
    u.id as user_id,
    u.currency,
    COALESCE(SUM(CASE WHEN i.deleted_at IS NULL THEN i.amount ELSE 0 END), 0) as total_income,
    COALESCE(SUM(CASE WHEN e.deleted_at IS NULL THEN e.amount ELSE 0 END), 0) as total_expenses,
    COALESCE(SUM(CASE WHEN i.deleted_at IS NULL THEN i.amount ELSE 0 END), 0) - 
    COALESCE(SUM(CASE WHEN e.deleted_at IS NULL THEN e.amount ELSE 0 END), 0) as balance,
    COUNT(DISTINCT CASE WHEN e.deleted_at IS NULL THEN e.id END) as expense_count,
    COUNT(DISTINCT CASE WHEN i.deleted_at IS NULL THEN i.id END) as income_count
FROM users u
LEFT JOIN income i ON u.id = i.user_id
LEFT JOIN expenses e ON u.id = e.user_id
WHERE u.deleted_at IS NULL
GROUP BY u.id, u.currency;

-- =============================================================================
-- VIEW: Spending by Category
-- =============================================================================
CREATE OR REPLACE VIEW v_spending_by_category AS
SELECT 
    e.user_id,
    c.id as category_id,
    c.name as category_name,
    c.color as category_color,
    c.icon as category_icon,
    COUNT(e.id) as transaction_count,
    SUM(e.amount) as total_amount,
    AVG(e.amount) as average_amount,
    MIN(e.amount) as min_amount,
    MAX(e.amount) as max_amount,
    MIN(e.expense_date) as first_expense_date,
    MAX(e.expense_date) as last_expense_date
FROM expenses e
INNER JOIN categories c ON e.category_id = c.id
WHERE e.deleted_at IS NULL AND c.deleted_at IS NULL
GROUP BY e.user_id, c.id, c.name, c.color, c.icon;

-- =============================================================================
-- VIEW: Income by Source
-- =============================================================================
CREATE OR REPLACE VIEW v_income_by_source AS
SELECT 
    user_id,
    source,
    COUNT(id) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as average_amount,
    MIN(amount) as min_amount,
    MAX(amount) as max_amount,
    MIN(income_date) as first_income_date,
    MAX(income_date) as last_income_date
FROM income
WHERE deleted_at IS NULL
GROUP BY user_id, source;

-- =============================================================================
-- VIEW: Monthly Financial Summary
-- =============================================================================
CREATE OR REPLACE VIEW v_monthly_financial_summary AS
SELECT 
    user_id,
    date_trunc('month', expense_date) as month,
    'expense' as transaction_type,
    SUM(amount) as total_amount,
    COUNT(*) as transaction_count
FROM expenses
WHERE deleted_at IS NULL
GROUP BY user_id, date_trunc('month', expense_date)

UNION ALL

SELECT 
    user_id,
    date_trunc('month', income_date) as month,
    'income' as transaction_type,
    SUM(amount) as total_amount,
    COUNT(*) as transaction_count
FROM income
WHERE deleted_at IS NULL
GROUP BY user_id, date_trunc('month', income_date);

-- =============================================================================
-- FUNCTION: Get User Balance for Date Range
-- =============================================================================
CREATE OR REPLACE FUNCTION get_user_balance(
    p_user_id UUID,
    p_start_date DATE DEFAULT NULL,
    p_end_date DATE DEFAULT NULL
)
RETURNS TABLE (
    total_income NUMERIC,
    total_expenses NUMERIC,
    balance NUMERIC,
    income_count BIGINT,
    expense_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COALESCE(SUM(i.amount), 0) as total_income,
        COALESCE(SUM(e.amount), 0) as total_expenses,
        COALESCE(SUM(i.amount), 0) - COALESCE(SUM(e.amount), 0) as balance,
        COUNT(DISTINCT i.id) as income_count,
        COUNT(DISTINCT e.id) as expense_count
    FROM users u
    LEFT JOIN income i ON u.id = i.user_id 
        AND i.deleted_at IS NULL
        AND (p_start_date IS NULL OR i.income_date >= p_start_date)
        AND (p_end_date IS NULL OR i.income_date <= p_end_date)
    LEFT JOIN expenses e ON u.id = e.user_id 
        AND e.deleted_at IS NULL
        AND (p_start_date IS NULL OR e.expense_date >= p_start_date)
        AND (p_end_date IS NULL OR e.expense_date <= p_end_date)
    WHERE u.id = p_user_id AND u.deleted_at IS NULL
    GROUP BY u.id;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- FUNCTION: Get Spending by Category for Date Range
-- =============================================================================
CREATE OR REPLACE FUNCTION get_spending_by_category(
    p_user_id UUID,
    p_start_date DATE DEFAULT NULL,
    p_end_date DATE DEFAULT NULL
)
RETURNS TABLE (
    category_id UUID,
    category_name VARCHAR,
    category_color VARCHAR,
    category_icon VARCHAR,
    total_amount NUMERIC,
    transaction_count BIGINT,
    percentage NUMERIC
) AS $$
DECLARE
    v_total_spending NUMERIC;
BEGIN
    -- Calculate total spending for percentage calculation
    SELECT COALESCE(SUM(amount), 0) INTO v_total_spending
    FROM expenses
    WHERE user_id = p_user_id 
        AND deleted_at IS NULL
        AND (p_start_date IS NULL OR expense_date >= p_start_date)
        AND (p_end_date IS NULL OR expense_date <= p_end_date);
    
    -- Return spending by category with percentage
    RETURN QUERY
    SELECT 
        c.id,
        c.name,
        c.color,
        c.icon,
        COALESCE(SUM(e.amount), 0) as total_amount,
        COUNT(e.id) as transaction_count,
        CASE 
            WHEN v_total_spending > 0 THEN 
                ROUND((COALESCE(SUM(e.amount), 0) / v_total_spending * 100), 2)
            ELSE 0
        END as percentage
    FROM categories c
    LEFT JOIN expenses e ON c.id = e.category_id 
        AND e.deleted_at IS NULL
        AND (p_start_date IS NULL OR e.expense_date >= p_start_date)
        AND (p_end_date IS NULL OR e.expense_date <= p_end_date)
    WHERE c.user_id = p_user_id AND c.deleted_at IS NULL
    GROUP BY c.id, c.name, c.color, c.icon
    HAVING COUNT(e.id) > 0
    ORDER BY total_amount DESC;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- FUNCTION: Get Monthly Spending Trend
-- =============================================================================
CREATE OR REPLACE FUNCTION get_monthly_spending_trend(
    p_user_id UUID,
    p_months INTEGER DEFAULT 6
)
RETURNS TABLE (
    month DATE,
    total_income NUMERIC,
    total_expenses NUMERIC,
    net_balance NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    WITH months AS (
        SELECT date_trunc('month', CURRENT_DATE - (n || ' months')::INTERVAL)::DATE as month
        FROM generate_series(0, p_months - 1) n
    )
    SELECT 
        m.month,
        COALESCE(SUM(i.amount), 0) as total_income,
        COALESCE(SUM(e.amount), 0) as total_expenses,
        COALESCE(SUM(i.amount), 0) - COALESCE(SUM(e.amount), 0) as net_balance
    FROM months m
    LEFT JOIN income i ON i.user_id = p_user_id 
        AND date_trunc('month', i.income_date) = m.month
        AND i.deleted_at IS NULL
    LEFT JOIN expenses e ON e.user_id = p_user_id 
        AND date_trunc('month', e.expense_date) = m.month
        AND e.deleted_at IS NULL
    GROUP BY m.month
    ORDER BY m.month DESC;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- FUNCTION: Get Top Expenses
-- =============================================================================
CREATE OR REPLACE FUNCTION get_top_expenses(
    p_user_id UUID,
    p_limit INTEGER DEFAULT 10,
    p_start_date DATE DEFAULT NULL,
    p_end_date DATE DEFAULT NULL
)
RETURNS TABLE (
    id UUID,
    amount NUMERIC,
    description VARCHAR,
    category_name VARCHAR,
    category_color VARCHAR,
    expense_date DATE,
    payment_method VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        e.id,
        e.amount,
        e.description,
        c.name as category_name,
        c.color as category_color,
        e.expense_date,
        pm.name as payment_method
    FROM expenses e
    INNER JOIN categories c ON e.category_id = c.id
    LEFT JOIN payment_methods pm ON e.payment_method_id = pm.id
    WHERE e.user_id = p_user_id 
        AND e.deleted_at IS NULL
        AND (p_start_date IS NULL OR e.expense_date >= p_start_date)
        AND (p_end_date IS NULL OR e.expense_date <= p_end_date)
    ORDER BY e.amount DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- FUNCTION: Get Recent Transactions (Combined Income and Expenses)
-- =============================================================================
CREATE OR REPLACE FUNCTION get_recent_transactions(
    p_user_id UUID,
    p_limit INTEGER DEFAULT 20
)
RETURNS TABLE (
    id UUID,
    transaction_type VARCHAR,
    amount NUMERIC,
    description VARCHAR,
    category_or_source VARCHAR,
    color VARCHAR,
    transaction_date DATE,
    created_at TIMESTAMP WITH TIME ZONE
) AS $$
BEGIN
    RETURN QUERY
    (
        SELECT 
            e.id,
            'expense'::VARCHAR as transaction_type,
            e.amount,
            e.description,
            c.name as category_or_source,
            c.color,
            e.expense_date as transaction_date,
            e.created_at
        FROM expenses e
        INNER JOIN categories c ON e.category_id = c.id
        WHERE e.user_id = p_user_id AND e.deleted_at IS NULL
        
        UNION ALL
        
        SELECT 
            i.id,
            'income'::VARCHAR as transaction_type,
            i.amount,
            i.description,
            i.source as category_or_source,
            '#27AE60'::VARCHAR as color,
            i.income_date as transaction_date,
            i.created_at
        FROM income i
        WHERE i.user_id = p_user_id AND i.deleted_at IS NULL
    )
    ORDER BY transaction_date DESC, created_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- COMMENTS
-- =============================================================================

COMMENT ON VIEW v_user_financial_summary IS 'Aggregate financial summary for each user';
COMMENT ON VIEW v_spending_by_category IS 'Spending statistics grouped by category';
COMMENT ON VIEW v_income_by_source IS 'Income statistics grouped by source';
COMMENT ON VIEW v_monthly_financial_summary IS 'Monthly breakdown of income and expenses';

COMMENT ON FUNCTION get_user_balance IS 'Calculate user balance for a specific date range';
COMMENT ON FUNCTION get_spending_by_category IS 'Get spending breakdown by category with percentages';
COMMENT ON FUNCTION get_monthly_spending_trend IS 'Get monthly income/expense trend for specified number of months';
COMMENT ON FUNCTION get_top_expenses IS 'Get largest expenses for a user within date range';
COMMENT ON FUNCTION get_recent_transactions IS 'Get most recent transactions (both income and expenses) for a user';
