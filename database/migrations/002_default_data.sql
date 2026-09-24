-- Smart Budget Database Schema
-- Migration 002: Default Data and Categories
-- Description: Inserts default categories and payment methods for new users

-- =============================================================================
-- DEFAULT PAYMENT METHODS (will be created for each new user via application logic)
-- =============================================================================
-- These are reference values; actual insertion happens in application code
-- when a new user registers:
-- - Cash
-- - Card
-- - Bank Transfer
-- - Mobile Money
-- - Other

-- =============================================================================
-- DEFAULT CATEGORY ICONS REFERENCE
-- =============================================================================
-- The following is a reference list of default categories with their
-- suggested Material Icons and colors. These will be created for each
-- new user via application logic during registration.

-- Category Reference Data:
/*
Default Categories for New Users:
---------------------------------
1. Food & Dining
   - Icon: restaurant
   - Color: #FF6B6B (coral red)
   
2. Transport
   - Icon: directions_car
   - Color: #4ECDC4 (turquoise)
   
3. Housing & Utilities
   - Icon: home
   - Color: #45B7D1 (sky blue)
   
4. Bills & Services
   - Icon: receipt_long
   - Color: #FFA07A (light salmon)
   
5. Shopping
   - Icon: shopping_bag
   - Color: #98D8C8 (mint)
   
6. Entertainment
   - Icon: movie
   - Color: #F7DC6F (yellow)
   
7. Health & Medical
   - Icon: local_hospital
   - Color: #E74C3C (red)
   
8. Education
   - Icon: school
   - Color: #3498DB (blue)
   
9. Travel
   - Icon: flight
   - Color: #9B59B6 (purple)
   
10. Personal Care
    - Icon: spa
    - Color: #FF69B4 (pink)
    
11. Insurance
    - Icon: security
    - Color: #34495E (dark gray)
    
12. Savings & Investments
    - Icon: savings
    - Color: #27AE60 (green)
    
13. Gifts & Donations
    - Icon: card_giftcard
    - Color: #E67E22 (orange)
    
14. Other
    - Icon: category
    - Color: #95A5A6 (gray)
*/

-- =============================================================================
-- DEFAULT INCOME SOURCES REFERENCE
-- =============================================================================
-- These are reference values for income sources:
/*
Default Income Sources:
-----------------------
- Salary
- Freelance
- Business
- Investment
- Rental Income
- Gift
- Refund
- Other
*/

-- =============================================================================
-- FUNCTION: Create Default Categories for New User
-- =============================================================================
CREATE OR REPLACE FUNCTION create_default_categories(p_user_id UUID)
RETURNS void AS $$
BEGIN
    INSERT INTO categories (user_id, name, color, icon, is_default) VALUES
    (p_user_id, 'Food & Dining', '#FF6B6B', 'restaurant', true),
    (p_user_id, 'Transport', '#4ECDC4', 'directions_car', true),
    (p_user_id, 'Housing & Utilities', '#45B7D1', 'home', true),
    (p_user_id, 'Bills & Services', '#FFA07A', 'receipt_long', true),
    (p_user_id, 'Shopping', '#98D8C8', 'shopping_bag', true),
    (p_user_id, 'Entertainment', '#F7DC6F', 'movie', true),
    (p_user_id, 'Health & Medical', '#E74C3C', 'local_hospital', true),
    (p_user_id, 'Education', '#3498DB', 'school', true),
    (p_user_id, 'Travel', '#9B59B6', 'flight', true),
    (p_user_id, 'Personal Care', '#FF69B4', 'spa', true),
    (p_user_id, 'Insurance', '#34495E', 'security', true),
    (p_user_id, 'Savings & Investments', '#27AE60', 'savings', true),
    (p_user_id, 'Gifts & Donations', '#E67E22', 'card_giftcard', true),
    (p_user_id, 'Other', '#95A5A6', 'category', true);
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- FUNCTION: Create Default Payment Methods for New User
-- =============================================================================
CREATE OR REPLACE FUNCTION create_default_payment_methods(p_user_id UUID)
RETURNS void AS $$
BEGIN
    INSERT INTO payment_methods (user_id, name, is_default) VALUES
    (p_user_id, 'Cash', true),
    (p_user_id, 'Card', false),
    (p_user_id, 'Bank Transfer', false),
    (p_user_id, 'Mobile Money', false),
    (p_user_id, 'Other', false);
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- FUNCTION: Initialize New User Data
-- =============================================================================
-- This function should be called after creating a new user account
CREATE OR REPLACE FUNCTION initialize_new_user(p_user_id UUID)
RETURNS void AS $$
BEGIN
    PERFORM create_default_categories(p_user_id);
    PERFORM create_default_payment_methods(p_user_id);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION create_default_categories IS 'Creates default expense categories for a new user';
COMMENT ON FUNCTION create_default_payment_methods IS 'Creates default payment methods for a new user';
COMMENT ON FUNCTION initialize_new_user IS 'Initializes all default data for a newly registered user';
