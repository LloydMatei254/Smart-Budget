# Smart Budget REST API Specification

## Overview

This document defines the complete REST API specification for the Smart Budget application. The API serves as the secure intermediary between the Android application and the Neon PostgreSQL database.

## Base URL

```
Development: http://localhost:8080/api/v1
Staging: https://staging-api.smartbudget.app/api/v1
Production: https://api.smartbudget.app/api/v1
```

## Architecture Principles

### Security First
- **No Direct Database Access**: Android app never receives database credentials
- **JWT Authentication**: Access tokens (15 min) + Refresh tokens (30 days)
- **HTTPS Only**: All production traffic over TLS 1.3
- **Rate Limiting**: Prevent abuse and DDoS attacks
- **Input Validation**: Server-side validation of all inputs
- **User Isolation**: Users can only access their own data

### RESTful Design
- **Resource-Based URLs**: `/expenses`, `/income`, `/categories`
- **HTTP Methods**: GET (read), POST (create), PUT (update), DELETE (delete)
- **Status Codes**: Proper HTTP status codes for all responses
- **JSON Format**: All requests and responses use JSON

### Error Handling
- **Consistent Format**: All errors follow the same structure
- **Meaningful Messages**: Clear error messages for debugging
- **Error Codes**: Application-specific error codes
- **Field Validation**: Field-level error details

## Authentication

### Overview
JWT-based authentication with access and refresh tokens:
- **Access Token**: Short-lived (15 minutes), sent with each request
- **Refresh Token**: Long-lived (30 days), used to get new access tokens
- **Storage**: Access token in memory, refresh token in Android Keystore

### Authentication Flow
```
1. User registers or logs in
2. Server returns access_token + refresh_token
3. Client stores tokens securely
4. Client includes access_token in Authorization header
5. When access_token expires, use refresh_token to get new one
6. When refresh_token expires, user must log in again
```

### JWT Claims
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "iat": 1234567890,
  "exp": 1234568790,
  "type": "access"
}
```

---

## Endpoints

## 1. Authentication

### 1.1. Register New User

Create a new user account.

**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "currency": "USD"
}
```

**Validation Rules:**
- `fullName`: Required, 2-255 characters
- `email`: Required, valid email format, unique
- `password`: Required, min 8 characters, must contain uppercase, lowercase, number, special char
- `currency`: Required, valid ISO 4217 code (USD, EUR, GBP, KES, etc.)

**Success Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "John Doe",
      "email": "john@example.com",
      "currency": "USD",
      "createdAt": "2026-09-22T10:30:00Z"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900
  }
}
```

**Error Responses:**

`400 Bad Request` - Validation error
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "fields": {
      "email": "Email is already registered",
      "password": "Password must contain at least one uppercase letter"
    }
  }
}
```

---

### 1.2. Login

Authenticate existing user.

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePassword123!"
}
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "John Doe",
      "email": "john@example.com",
      "currency": "USD",
      "createdAt": "2026-09-22T10:30:00Z"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900
  }
}
```

**Error Responses:**

`401 Unauthorized` - Invalid credentials
```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email or password"
  }
}
```

`429 Too Many Requests` - Rate limit exceeded
```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many login attempts. Please try again in 15 minutes.",
    "retryAfter": 900
  }
}
```

---

### 1.3. Refresh Token

Get new access token using refresh token.

**Endpoint:** `POST /auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900
  }
}
```

**Error Responses:**

`401 Unauthorized` - Invalid or expired refresh token
```json
{
  "success": false,
  "error": {
    "code": "INVALID_REFRESH_TOKEN",
    "message": "Refresh token is invalid or expired"
  }
}
```

---

### 1.4. Logout

Revoke refresh token.

**Endpoint:** `POST /auth/logout`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

### 1.5. Get Current User

Get authenticated user's profile.

**Endpoint:** `GET /auth/me`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Doe",
    "email": "john@example.com",
    "currency": "USD",
    "createdAt": "2026-09-22T10:30:00Z",
    "updatedAt": "2026-09-22T10:30:00Z"
  }
}
```

---

### 1.6. Update User Profile

Update authenticated user's profile.

**Endpoint:** `PUT /auth/me`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "fullName": "John Smith",
  "currency": "EUR"
}
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Smith",
    "email": "john@example.com",
    "currency": "EUR",
    "createdAt": "2026-09-22T10:30:00Z",
    "updatedAt": "2026-09-22T14:20:00Z"
  }
}
```

---

### 1.7. Change Password

Change authenticated user's password.

**Endpoint:** `POST /auth/change-password`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword456!"
}
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

**Error Responses:**

`401 Unauthorized` - Current password incorrect
```json
{
  "success": false,
  "error": {
    "code": "INVALID_PASSWORD",
    "message": "Current password is incorrect"
  }
}
```

---

## 2. Expenses

All expense endpoints require authentication.

### 2.1. List Expenses

Get paginated list of user's expenses with filtering.

**Endpoint:** `GET /expenses`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `page` (optional): Page number, default 1
- `limit` (optional): Items per page, default 20, max 100
- `categoryId` (optional): Filter by category UUID
- `startDate` (optional): Filter by start date (YYYY-MM-DD)
- `endDate` (optional): Filter by end date (YYYY-MM-DD)
- `search` (optional): Search in description and notes
- `sortBy` (optional): Sort field (date, amount, createdAt), default date
- `sortOrder` (optional): Sort order (asc, desc), default desc

**Example Request:**
```
GET /expenses?page=1&limit=20&startDate=2026-09-01&endDate=2026-09-30&sortBy=date&sortOrder=desc
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "expenses": [
      {
        "id": "a1b2c3d4-e5f6-4a5b-9c8d-7e6f5a4b3c2d",
        "amount": "45.99",
        "description": "Grocery shopping",
        "notes": "Weekly groceries",
        "date": "2026-09-20",
        "category": {
          "id": "cat-uuid-1",
          "name": "Food & Dining",
          "color": "#FF6B6B",
          "icon": "restaurant"
        },
        "paymentMethod": {
          "id": "pm-uuid-1",
          "name": "Card"
        },
        "syncStatus": "synced",
        "syncVersion": 1,
        "createdAt": "2026-09-20T15:30:00Z",
        "updatedAt": "2026-09-20T15:30:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 45,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

---

### 2.2. Get Single Expense

Get details of a specific expense.

**Endpoint:** `GET /expenses/{id}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "a1b2c3d4-e5f6-4a5b-9c8d-7e6f5a4b3c2d",
    "amount": "45.99",
    "description": "Grocery shopping",
    "notes": "Weekly groceries",
    "date": "2026-09-20",
    "category": {
      "id": "cat-uuid-1",
      "name": "Food & Dining",
      "color": "#FF6B6B",
      "icon": "restaurant"
    },
    "paymentMethod": {
      "id": "pm-uuid-1",
      "name": "Card"
    },
    "syncStatus": "synced",
    "syncVersion": 1,
    "createdAt": "2026-09-20T15:30:00Z",
    "updatedAt": "2026-09-20T15:30:00Z"
  }
}
```

**Error Responses:**

`404 Not Found` - Expense not found or doesn't belong to user
```json
{
  "success": false,
  "error": {
    "code": "EXPENSE_NOT_FOUND",
    "message": "Expense not found"
  }
}
```

---

### 2.3. Create Expense

Create a new expense.

**Endpoint:** `POST /expenses`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "amount": "45.99",
  "description": "Grocery shopping",
  "notes": "Weekly groceries",
  "date": "2026-09-20",
  "categoryId": "cat-uuid-1",
  "paymentMethodId": "pm-uuid-1",
  "localId": "local-uuid-123"
}
```

**Validation Rules:**
- `amount`: Required, positive number, max 2 decimal places
- `description`: Required, 1-255 characters
- `notes`: Optional, max 1000 characters
- `date`: Required, valid date, not in future
- `categoryId`: Required, valid UUID, must belong to user
- `paymentMethodId`: Optional, valid UUID, must belong to user
- `localId`: Optional, client-generated UUID for sync

**Success Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": "a1b2c3d4-e5f6-4a5b-9c8d-7e6f5a4b3c2d",
    "amount": "45.99",
    "description": "Grocery shopping",
    "notes": "Weekly groceries",
    "date": "2026-09-20",
    "category": {
      "id": "cat-uuid-1",
      "name": "Food & Dining",
      "color": "#FF6B6B",
      "icon": "restaurant"
    },
    "paymentMethod": {
      "id": "pm-uuid-1",
      "name": "Card"
    },
    "syncStatus": "synced",
    "syncVersion": 1,
    "createdAt": "2026-09-20T15:30:00Z",
    "updatedAt": "2026-09-20T15:30:00Z"
  }
}
```

---

### 2.4. Update Expense

Update an existing expense.

**Endpoint:** `PUT /expenses/{id}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "amount": "50.99",
  "description": "Grocery shopping - updated",
  "notes": "Weekly groceries with extra items",
  "date": "2026-09-20",
  "categoryId": "cat-uuid-1",
  "paymentMethodId": "pm-uuid-1",
  "syncVersion": 1
}
```

**Notes:**
- `syncVersion` is used for conflict detection
- If server version > client version, returns 409 Conflict

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "a1b2c3d4-e5f6-4a5b-9c8d-7e6f5a4b3c2d",
    "amount": "50.99",
    "description": "Grocery shopping - updated",
    "notes": "Weekly groceries with extra items",
    "date": "2026-09-20",
    "category": {
      "id": "cat-uuid-1",
      "name": "Food & Dining",
      "color": "#FF6B6B",
      "icon": "restaurant"
    },
    "paymentMethod": {
      "id": "pm-uuid-1",
      "name": "Card"
    },
    "syncStatus": "synced",
    "syncVersion": 2,
    "createdAt": "2026-09-20T15:30:00Z",
    "updatedAt": "2026-09-20T16:45:00Z"
  }
}
```

**Error Responses:**

`409 Conflict` - Version conflict
```json
{
  "success": false,
  "error": {
    "code": "VERSION_CONFLICT",
    "message": "Expense has been modified by another client",
    "serverVersion": 3,
    "serverData": { /* latest expense data */ }
  }
}
```

---

### 2.5. Delete Expense

Soft delete an expense.

**Endpoint:** `DELETE /expenses/{id}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "Expense deleted successfully"
}
```

---

## 3. Income

All income endpoints require authentication. Structure is similar to expenses.

### 3.1. List Income

**Endpoint:** `GET /income`

**Query Parameters:** Same as expenses (page, limit, startDate, endDate, search, sortBy, sortOrder)

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "income": [
      {
        "id": "income-uuid-1",
        "amount": "2500.00",
        "source": "Salary",
        "description": "Monthly salary",
        "notes": "September 2026",
        "date": "2026-09-01",
        "syncStatus": "synced",
        "syncVersion": 1,
        "createdAt": "2026-09-01T08:00:00Z",
        "updatedAt": "2026-09-01T08:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 12,
      "totalPages": 1,
      "hasNext": false,
      "hasPrevious": false
    }
  }
}
```

---

### 3.2. Get Single Income

**Endpoint:** `GET /income/{id}`

---

### 3.3. Create Income

**Endpoint:** `POST /income`

**Request Body:**
```json
{
  "amount": "2500.00",
  "source": "Salary",
  "description": "Monthly salary",
  "notes": "September 2026",
  "date": "2026-09-01",
  "localId": "local-uuid-456"
}
```

---

### 3.4. Update Income

**Endpoint:** `PUT /income/{id}`

---

### 3.5. Delete Income

**Endpoint:** `DELETE /income/{id}`

---

## 4. Categories

### 4.1. List Categories

**Endpoint:** `GET /categories`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "cat-uuid-1",
      "name": "Food & Dining",
      "color": "#FF6B6B",
      "icon": "restaurant",
      "isDefault": true,
      "expenseCount": 45,
      "totalSpent": "1234.56",
      "createdAt": "2026-09-01T10:00:00Z",
      "updatedAt": "2026-09-01T10:00:00Z"
    }
  ]
}
```

---

### 4.2. Create Category

**Endpoint:** `POST /categories`

**Request Body:**
```json
{
  "name": "Pets",
  "color": "#FF69B4",
  "icon": "pets"
}
```

**Success Response:** `201 Created`

---

### 4.3. Update Category

**Endpoint:** `PUT /categories/{id}`

**Request Body:**
```json
{
  "name": "Pet Care",
  "color": "#FF69B4",
  "icon": "pets"
}
```

---

### 4.4. Delete Category

**Endpoint:** `DELETE /categories/{id}`

**Notes:**
- Cannot delete category if it has expenses
- Returns 409 Conflict with expense count

---

## 5. Payment Methods

### 5.1. List Payment Methods

**Endpoint:** `GET /payment-methods`

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "pm-uuid-1",
      "name": "Card",
      "isDefault": false,
      "createdAt": "2026-09-01T10:00:00Z",
      "updatedAt": "2026-09-01T10:00:00Z"
    }
  ]
}
```

---

### 5.2. Create Payment Method

**Endpoint:** `POST /payment-methods`

---

### 5.3. Update Payment Method

**Endpoint:** `PUT /payment-methods/{id}`

---

### 5.4. Delete Payment Method

**Endpoint:** `DELETE /payment-methods/{id}`

---

## 6. Reports

### 6.1. Financial Summary

Get overall financial summary.

**Endpoint:** `GET /reports/summary`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `startDate` (optional): Start date (YYYY-MM-DD)
- `endDate` (optional): End date (YYYY-MM-DD)

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "totalIncome": "5000.00",
    "totalExpenses": "3245.67",
    "balance": "1754.33",
    "incomeCount": 5,
    "expenseCount": 78,
    "averageExpense": "41.61",
    "largestExpense": {
      "id": "expense-uuid",
      "amount": "450.00",
      "description": "Rent",
      "date": "2026-09-01"
    },
    "period": {
      "startDate": "2026-09-01",
      "endDate": "2026-09-30"
    }
  }
}
```

---

### 6.2. Spending by Category

**Endpoint:** `GET /reports/spending-by-category`

**Query Parameters:**
- `startDate` (optional)
- `endDate` (optional)

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "categories": [
      {
        "categoryId": "cat-uuid-1",
        "categoryName": "Food & Dining",
        "categoryColor": "#FF6B6B",
        "categoryIcon": "restaurant",
        "totalAmount": "850.45",
        "transactionCount": 23,
        "percentage": 35.2,
        "averageAmount": "37.00"
      }
    ],
    "totalSpent": "2415.33",
    "period": {
      "startDate": "2026-09-01",
      "endDate": "2026-09-30"
    }
  }
}
```

---

### 6.3. Monthly Trend

**Endpoint:** `GET /reports/monthly-trend`

**Query Parameters:**
- `months` (optional): Number of months, default 6, max 24

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "months": [
      {
        "month": "2026-09-01",
        "totalIncome": "5000.00",
        "totalExpenses": "3245.67",
        "netBalance": "1754.33"
      },
      {
        "month": "2026-08-01",
        "totalIncome": "5000.00",
        "totalExpenses": "2980.25",
        "netBalance": "2019.75"
      }
    ]
  }
}
```

---

### 6.4. Income vs Expenses

**Endpoint:** `GET /reports/income-vs-expenses`

**Query Parameters:**
- `startDate` (optional)
- `endDate` (optional)
- `groupBy` (optional): day, week, month (default: month)

---

## 7. Synchronization

### 7.1. Sync Changes

Synchronize local changes with server.

**Endpoint:** `POST /sync`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "lastSyncTimestamp": "2026-09-20T10:00:00Z",
  "changes": {
    "expenses": {
      "created": [
        {
          "localId": "local-uuid-1",
          "amount": "25.50",
          "description": "Coffee",
          "date": "2026-09-20",
          "categoryId": "cat-uuid-1",
          "syncVersion": 1
        }
      ],
      "updated": [
        {
          "id": "expense-uuid-2",
          "amount": "30.00",
          "description": "Lunch - updated",
          "syncVersion": 2
        }
      ],
      "deleted": [
        "expense-uuid-3"
      ]
    },
    "income": {
      "created": [],
      "updated": [],
      "deleted": []
    }
  }
}
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "syncTimestamp": "2026-09-20T15:00:00Z",
    "created": {
      "expenses": [
        {
          "localId": "local-uuid-1",
          "id": "expense-uuid-new",
          "amount": "25.50",
          "description": "Coffee",
          "syncStatus": "synced",
          "syncVersion": 1
        }
      ]
    },
    "conflicts": {
      "expenses": [
        {
          "id": "expense-uuid-2",
          "clientVersion": 2,
          "serverVersion": 3,
          "serverData": { /* latest server data */ }
        }
      ]
    },
    "serverChanges": {
      "expenses": {
        "created": [],
        "updated": [
          {
            "id": "expense-uuid-5",
            "amount": "100.00",
            "description": "Updated from another device",
            "syncVersion": 4
          }
        ],
        "deleted": ["expense-uuid-6"]
      },
      "income": {
        "created": [],
        "updated": [],
        "deleted": []
      }
    }
  }
}
```

---

### 7.2. Get Changes Since Last Sync

**Endpoint:** `GET /sync/changes`

**Query Parameters:**
- `since`: ISO timestamp of last sync

**Success Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "expenses": {
      "created": [],
      "updated": [],
      "deleted": []
    },
    "income": {
      "created": [],
      "updated": [],
      "deleted": []
    },
    "categories": {
      "created": [],
      "updated": [],
      "deleted": []
    }
  }
}
```

---

## Error Response Format

All errors follow this consistent structure:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "fields": {
      "fieldName": "Field-specific error message"
    },
    "details": {
      /* Additional context-specific error details */
    }
  }
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Input validation failed |
| `UNAUTHORIZED` | 401 | Not authenticated |
| `INVALID_CREDENTIALS` | 401 | Wrong email/password |
| `INVALID_TOKEN` | 401 | Invalid or expired token |
| `INVALID_REFRESH_TOKEN` | 401 | Invalid or expired refresh token |
| `FORBIDDEN` | 403 | Not authorized to access resource |
| `NOT_FOUND` | 404 | Resource not found |
| `EXPENSE_NOT_FOUND` | 404 | Expense not found |
| `INCOME_NOT_FOUND` | 404 | Income not found |
| `CATEGORY_NOT_FOUND` | 404 | Category not found |
| `VERSION_CONFLICT` | 409 | Sync version conflict |
| `CATEGORY_IN_USE` | 409 | Cannot delete category with expenses |
| `DUPLICATE_EMAIL` | 409 | Email already registered |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_SERVER_ERROR` | 500 | Server error |
| `DATABASE_ERROR` | 500 | Database operation failed |

---

## HTTP Status Codes

| Status Code | Usage |
|-------------|-------|
| `200 OK` | Successful GET, PUT, DELETE |
| `201 Created` | Successful POST |
| `400 Bad Request` | Validation error, malformed request |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Authenticated but not authorized |
| `404 Not Found` | Resource doesn't exist |
| `409 Conflict` | Sync conflict, constraint violation |
| `429 Too Many Requests` | Rate limit exceeded |
| `500 Internal Server Error` | Server error |
| `503 Service Unavailable` | Temporary outage |

---

## Authentication Header

All authenticated endpoints require:

```
Authorization: Bearer <access_token>
```

---

## Rate Limiting

### Limits
- **Login endpoint**: 5 requests per 15 minutes per IP
- **Registration endpoint**: 3 requests per hour per IP
- **General endpoints**: 100 requests per minute per user
- **Sync endpoint**: 20 requests per minute per user

### Rate Limit Headers
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1632225600
```

---

## Pagination

All list endpoints support pagination:

### Request
```
GET /expenses?page=1&limit=20
```

### Response
```json
{
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 45,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## Date Formats

- **Date fields**: `YYYY-MM-DD` (e.g., `2026-09-22`)
- **Timestamp fields**: ISO 8601 with timezone (e.g., `2026-09-22T10:30:00Z`)

---

## Currency Format

- All amounts are strings with 2 decimal places
- Example: `"45.99"`, `"1234.56"`
- No currency symbols in API responses
- Currency determined by user profile

---

## CORS Configuration

### Allowed Origins
- Development: `http://localhost:*`
- Production: Only registered Android app package signature

### Allowed Methods
```
GET, POST, PUT, DELETE, OPTIONS
```

### Allowed Headers
```
Authorization, Content-Type
```

---

## Security Headers

All API responses include:

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'none'
```

---

## Versioning

API version is included in the base URL: `/api/v1`

When breaking changes are necessary:
- Create new version: `/api/v2`
- Maintain v1 for at least 6 months
- Announce deprecation with advance notice

---

## Testing

### Health Check
```
GET /health
```

Response:
```json
{
  "status": "healthy",
  "version": "1.0.0",
  "timestamp": "2026-09-22T10:30:00Z"
}
```

### Database Health
```
GET /health/db
```

---

## Monitoring and Logging

### Request ID
All responses include a unique request ID for debugging:

```
X-Request-ID: 550e8400-e29b-41d4-a716-446655440000
```

### Logging
- All requests logged with timestamp, user ID, endpoint, status
- Errors logged with stack traces (not exposed to client)
- Failed auth attempts logged for security monitoring

---

## Next Steps

1. Choose backend framework (see BACKEND_RECOMMENDATION.md)
2. Implement authentication with JWT
3. Implement all CRUD endpoints
4. Add input validation
5. Add rate limiting
6. Set up database connection pooling
7. Implement error handling
8. Add logging and monitoring
9. Write API tests
10. Deploy to production
