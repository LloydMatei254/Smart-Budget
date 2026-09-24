# Smart Budget Database Schema

## Overview

This directory contains the PostgreSQL database schema for the Smart Budget application, designed to run on **Neon PostgreSQL**. The schema implements a normalized, secure, and scalable database structure with proper constraints, indexes, and reporting functions.

## Architecture Principles

### Security
- **No Direct Database Access**: The Android application does NOT connect directly to the database
- **API Layer**: All database operations go through a secure REST API backend
- **User Isolation**: All user-owned tables enforce user_id foreign keys
- **Password Security**: Passwords are hashed (bcrypt/argon2) on the backend, never stored in plaintext
- **JWT Authentication**: Refresh tokens stored securely with expiration tracking

### Data Integrity
- **Foreign Keys**: Enforce referential integrity across all tables
- **Check Constraints**: Validate data at the database level (positive amounts, valid emails, etc.)
- **NOT NULL Constraints**: Prevent missing required data
- **Unique Constraints**: Prevent duplicate records where appropriate
- **Timestamps**: Automatic tracking of created_at and updated_at via triggers

### Performance
- **Strategic Indexes**: Optimized for common query patterns
- **Partial Indexes**: Index only non-deleted records where appropriate
- **Composite Indexes**: Multi-column indexes for complex queries
- **Materialized Views**: Can be added later for heavy reporting queries

### Offline-First Synchronization
- **Sync Status**: Track synchronization state (synced, pending, conflict, deleted)
- **Sync Version**: Version numbering for conflict detection
- **Local ID**: Client-side identifiers for matching during sync
- **Soft Deletion**: deleted_at timestamps instead of hard deletes
- **Conflict Resolution**: Last-write-wins strategy based on sync_version

## Migration Files

### 001_initial_schema.sql
Creates the core database structure:
- **users**: User accounts with email/password authentication
- **categories**: User-defined expense categories with colors and icons
- **payment_methods**: User-defined payment methods
- **expenses**: Expense transactions with sync support
- **income**: Income transactions with sync support
- **sync_metadata**: Tracks synchronization state for offline-first architecture
- **refresh_tokens**: JWT refresh tokens for authentication

Includes:
- Automatic timestamp triggers for updated_at
- Sync version increment triggers
- UUID generation
- Comprehensive indexes

### 002_default_data.sql
Creates default data initialization functions:
- **create_default_categories()**: Creates 14 default categories for new users
- **create_default_payment_methods()**: Creates 5 default payment methods
- **initialize_new_user()**: Convenience function to set up all defaults

Default Categories:
1. Food & Dining (#FF6B6B)
2. Transport (#4ECDC4)
3. Housing & Utilities (#45B7D1)
4. Bills & Services (#FFA07A)
5. Shopping (#98D8C8)
6. Entertainment (#F7DC6F)
7. Health & Medical (#E74C3C)
8. Education (#3498DB)
9. Travel (#9B59B6)
10. Personal Care (#FF69B4)
11. Insurance (#34495E)
12. Savings & Investments (#27AE60)
13. Gifts & Donations (#E67E22)
14. Other (#95A5A6)

### 003_reporting_views.sql
Creates optimized views and functions for reporting:

**Views:**
- `v_user_financial_summary`: Aggregate financial summary per user
- `v_spending_by_category`: Spending statistics by category
- `v_income_by_source`: Income statistics by source
- `v_monthly_financial_summary`: Monthly income/expense breakdown

**Functions:**
- `get_user_balance(user_id, start_date, end_date)`: Calculate balance for date range
- `get_spending_by_category(user_id, start_date, end_date)`: Category breakdown with percentages
- `get_monthly_spending_trend(user_id, months)`: Monthly trend analysis
- `get_top_expenses(user_id, limit, start_date, end_date)`: Largest expenses
- `get_recent_transactions(user_id, limit)`: Recent transactions (income + expenses)

## Currency Support

The schema supports multiple currencies through the `users.currency` field:
- Stored as ISO 4217 3-letter currency codes (USD, EUR, GBP, KES, NGN, ZAR, INR, etc.)
- CHECK constraint ensures 3-character length
- Per-user currency setting
- Amounts stored as NUMERIC(15, 2) for precision
- **Important**: Do NOT use FLOAT/DOUBLE for financial calculations

## Monetary Precision

All financial amounts use `NUMERIC(15, 2)`:
- **15 digits total**: Supports amounts up to 9,999,999,999,999.99
- **2 decimal places**: Cent/penny precision
- **Fixed precision**: No floating-point rounding errors
- **Database-level validation**: CHECK constraints ensure positive amounts

## Synchronization Strategy

### Conflict Resolution: Last-Write-Wins with Version Tracking

1. **sync_status** field values:
   - `synced`: Record is synchronized with server
   - `pending`: Local changes not yet synced
   - `conflict`: Server version differs from local
   - `deleted`: Soft-deleted, pending sync

2. **sync_version** field:
   - Increments on each update
   - Used to detect conflicts
   - Server wins if client version < server version

3. **local_id** field:
   - Client-generated UUID for new records
   - Used to match local records with server records during sync
   - Becomes null once remote_id is established

4. **Sync Flow**:
   ```
   1. Client creates record with local_id, sync_status='pending'
   2. Client syncs to server
   3. Server returns remote_id (UUID)
   4. Client updates record with remote_id, clears local_id, sets sync_status='synced'
   5. On conflict, compare sync_version; higher version wins
   ```

## Indexes Strategy

### Primary Indexes
- Primary keys on all tables (UUID)
- Unique constraints on email, tokens, etc.

### Query Optimization Indexes
- **User queries**: user_id indexes on all user-owned tables
- **Date range queries**: Composite indexes on (user_id, date DESC)
- **Category filtering**: Composite indexes on (user_id, category_id)
- **Synchronization**: Indexes on sync_status for pending records
- **Partial indexes**: Filter out deleted records (WHERE deleted_at IS NULL)

### Index Maintenance
- Indexes are automatically maintained by PostgreSQL
- Use `EXPLAIN ANALYZE` to verify query performance
- Add new indexes if query patterns change

## Data Validation

### Database Level
- CHECK constraints on amounts (must be positive)
- CHECK constraints on email format (regex)
- CHECK constraints on sync_status (enum values)
- CHECK constraints on currency codes (3 characters)
- Foreign key constraints (referential integrity)

### Application Level (Backend API)
- Email format validation
- Password strength validation
- Date range validation
- Amount precision validation
- Authorization (user can only access own data)

## Deployment on Neon PostgreSQL

### Prerequisites
1. Create a Neon PostgreSQL project at https://neon.tech
2. Create a database (e.g., `smartbudget_prod`)
3. Note connection string: `postgresql://user:password@host/database`

### Migration Execution
```bash
# Connect to Neon database
psql "postgresql://user:password@host/database?sslmode=require"

# Run migrations in order
\i 001_initial_schema.sql
\i 002_default_data.sql
\i 003_reporting_views.sql
```

### Neon-Specific Features
- **Autoscaling**: Neon automatically scales compute resources
- **Branching**: Create database branches for testing
- **Serverless**: Pay only for active database time
- **Connection Pooling**: Use Neon's built-in connection pooling

### Environment-Specific Databases
- **Development**: `smartbudget_dev`
- **Staging**: `smartbudget_staging`
- **Production**: `smartbudget_prod`

## Sample Queries

### Get User Balance
```sql
SELECT * FROM get_user_balance(
    'user-uuid-here',
    '2026-01-01'::DATE,
    '2026-12-31'::DATE
);
```

### Get Spending by Category (This Month)
```sql
SELECT * FROM get_spending_by_category(
    'user-uuid-here',
    date_trunc('month', CURRENT_DATE)::DATE,
    (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month - 1 day')::DATE
);
```

### Get Monthly Trend (Last 6 Months)
```sql
SELECT * FROM get_monthly_spending_trend('user-uuid-here', 6);
```

### Get Recent Transactions
```sql
SELECT * FROM get_recent_transactions('user-uuid-here', 20);
```

### Create New User with Defaults
```sql
-- Insert user
INSERT INTO users (full_name, email, password_hash, currency)
VALUES ('John Doe', 'john@example.com', '$hashed_password', 'USD')
RETURNING id;

-- Initialize defaults (use returned user_id)
SELECT initialize_new_user('returned-user-id-here');
```

## Security Considerations

### Never Expose
- Password hashes
- Refresh token values
- Database connection strings
- Other users' data

### Always Enforce
- User authentication via JWT
- User authorization (users can only access their own data)
- Input validation on backend
- SQL injection prevention (use parameterized queries)
- Rate limiting on API endpoints

### Backend Responsibilities
- Password hashing (bcrypt/argon2 with salt)
- JWT token generation and validation
- User session management
- Input sanitization
- Business logic validation
- Error handling (don't expose internal errors)

## Backup Strategy

### Neon Automatic Backups
- Neon provides point-in-time recovery (PITR)
- Retention period configurable
- Automated daily backups

### Additional Backups (Recommended)
```bash
# Export database
pg_dump "postgresql://user:password@host/database" > backup.sql

# Restore database
psql "postgresql://user:password@host/database" < backup.sql
```

## Future Enhancements

### Potential Additions
1. **Budgets Table**: Monthly/category-based budgets with alerts
2. **Recurring Transactions**: Templates for recurring expenses/income
3. **Attachments**: Receipt images stored in object storage (S3/Cloudflare R2)
4. **Tags**: Additional categorization beyond single category
5. **Multi-Currency**: Exchange rates and currency conversion
6. **Shared Budgets**: Family/team budget sharing
7. **Audit Log**: Track all data changes for compliance
8. **Analytics Events**: Track user behavior for insights

### Performance Optimizations
1. **Materialized Views**: For expensive reporting queries
2. **Partitioning**: Partition expenses/income by date for very large datasets
3. **Read Replicas**: Separate read/write workloads
4. **Caching Layer**: Redis for frequently accessed data

## Maintenance

### Regular Tasks
- Monitor slow queries: `pg_stat_statements`
- Vacuum and analyze: `VACUUM ANALYZE` (Neon handles this automatically)
- Review index usage: `pg_stat_user_indexes`
- Clean up expired refresh tokens
- Archive old soft-deleted records

### Monitoring
- Query performance metrics
- Connection pool usage
- Database size growth
- Error rates
- Response times

## Support

For questions or issues with the database schema:
1. Review this README
2. Check migration files for implementation details
3. Consult PostgreSQL documentation: https://www.postgresql.org/docs/
4. Consult Neon documentation: https://neon.tech/docs/
