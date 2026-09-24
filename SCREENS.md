# Smart Budget - Screen Designs and Implementation Guide

## Overview

This document details all screens in the Smart Budget Android application, based on the provided UI designs. Each screen is described with its layout, components, and implementation requirements.

---

## Screen Flow

```
Splash Screen
    ↓
Login/Register (if not authenticated)
    ↓
Dashboard (Home)
    ├── Transactions
    ├── Add Expense/Income
    ├── Reports
    └── Settings
```

---

## 1. Splash Screen (Not shown in design, but needed)

### Purpose
- Show app logo and branding
- Check authentication status
- Initialize app dependencies
- Navigate to Login or Dashboard

### Components
- App logo with icon
- App name: "Smart Budget"
- Tagline: "Take control of your money"
- Loading indicator

### Navigation
- If authenticated → Dashboard
- If not authenticated → Login

### Duration
- 2-3 seconds maximum

---

## 2. Login Screen

### Layout Components

#### Top Section
- **Back button** (top-left)
- **Title**: "Welcome Back"
- **Subtitle**: "Sign in to your Smart Budget account"

#### Form Section
- **Email Field**
  - Label: "Email address"
  - Placeholder: "you@example.com"
  - Input type: Email
  - Icon: Email icon
  
- **Password Field**
  - Label: "Password"
  - Placeholder: "Enter your password"
  - Input type: Password
  - Icon: Lock icon
  - Visibility toggle: Eye icon

- **Remember Me Checkbox**
  - Text: "Remember me"
  - Link: "Forgot password?" (right-aligned, green)

#### Action Buttons
- **Sign In Button**
  - Full width
  - Green (#00897B)
  - White text
  - Rounded corners

- **Divider**: "or"

- **Continue with Google Button**
  - Full width
  - White background
  - Border
  - Google icon + "Continue with Google" text

#### Footer
- **Don't have an account?** 
  - Link: "Sign up" (green)

### States
- Default
- Loading (show progress indicator)
- Error (show error messages)
- Success (navigate to dashboard)

### Validation
- Email format validation
- Password minimum 8 characters
- Show field-specific errors

---

## 3. Register Screen (Similar to Login)

### Additional Fields
- **Full Name Field**
  - Label: "Full name"
  - Placeholder: "John Doe"
  - Input type: Text

- **Currency Selector**
  - Label: "Preferred currency"
  - Dropdown with currency options
  - Default: USD

### Action Button
- **Get Started** (instead of Sign In)

### Footer
- **I already have an account**
  - Link: "Sign in" (green)

---

## 4. Dashboard Screen (Home)

### Top Bar
- **App Logo/Icon** (left)
- **Title**: "Smart Budget"
- **Notification Bell** (right, with badge if unread)
- **Profile Avatar** (right, initials or image)

### User Greeting
- **Greeting**: "Good morning,"
- **User Name**: "John Doe"

### Currency Selector
- **Dropdown**: USD ▼
- Shows current selected currency

### Balance Card (Hero Section)
- **Label**: "Total Balance"
- **Amount**: "$2,450.00" (large, prominent)
- **Change Indicator**: "+ 12%" with up arrow
- **Period**: "vs last month"
- **Eye Icon**: Toggle visibility
- **Background**: Green gradient (#00897B to darker)
- **Corner Radius**: Rounded

### Quick Stats (Below Balance Card)
- **Two Cards Side-by-Side**:
  
  1. **Income Card**
     - Icon: Up arrow (green background circle)
     - Label: "Income"
     - Amount: "$3,500.00"
  
  2. **Expenses Card**
     - Icon: Down arrow (red background circle)
     - Label: "Expenses"
     - Amount: "$1,050.00"

### Recent Transactions Section
- **Header**: "Recent Transactions"
- **Link**: "See all" (right-aligned, green)

#### Transaction List Items
Each transaction shows:
- **Icon**: Category icon in colored circle
- **Title**: Transaction description
- **Subtitle**: Category name
- **Amount**: Positive (green +) or negative (red -)
- **Date**: "Apr 24, 2025" format

**Example Transactions**:
1. Grocery Store - $85.20 - Food & Dining - Apr 24, 2025
2. Salary + $3,500.00 - Income - Apr 23, 2025
3. Electricity Bill - $120.00 - Utilities - Apr 22, 2025
4. Freelance Work + $920.00 - Income - Apr 20, 2025

### Bottom Navigation Bar
- **Home** (selected, green icon)
- **Transactions** (gray icon)
- **Add** (center, large green FAB with + icon)
- **Reports** (gray icon)
- **Settings** (gray icon)

---

## 5. Transactions Screen

### Top Bar
- **Back button** (left)
- **Title**: "Transactions"
- **Filter Icon** (right)

### Filter Tabs
- **All** (selected, green background)
- **Income** (default background)
- **Expenses** (default background)

### Search Bar
- **Search icon**
- **Placeholder**: "Search transactions..."
- **Filter button** (right)

### Transaction List
- Shows all transactions (income + expenses)
- **Income items**: Green circle with up arrow
- **Expense items**: Colored circle with category icon
- **Group by date** (optional)

#### Transaction Item Details
- **Icon**: Category icon or income icon
- **Title**: Description
- **Subtitle**: Category or source
- **Amount**: + for income (green), - for expense (red)
- **Date**: Below subtitle

### States
- **Empty State**: "No transactions yet"
- **Loading**: Show skeleton loaders
- **Error**: Show error message with retry

### Actions
- **Tap transaction**: View details
- **Swipe**: Delete (with confirmation)
- **Pull to refresh**: Reload list

---

## 6. Add Expense Screen

### Top Bar
- **Back button** (left)
- **Title**: "Add Expense"

### Form Fields

#### Amount Field
- **Label**: "Amount"
- **Input**: Large text input with currency
- **Example**: "0.00" with "USD" dropdown
- **Keyboard**: Numeric with decimal

#### Category Field
- **Label**: "Category"
- **Input**: Dropdown selector
- **Placeholder**: "Select category"
- **Shows**: Category icon + name

#### Payment Method Field
- **Label**: "Payment Method"
- **Input**: Dropdown selector
- **Placeholder**: "Select payment method"
- **Options**: Cash, Card, Bank Transfer, etc.

#### Date Field
- **Label**: "Date"
- **Input**: Date picker
- **Default**: Today's date
- **Format**: "Apr 24, 2025"
- **Icon**: Calendar icon

#### Description Field
- **Label**: "Description"
- **Input**: Single-line text
- **Placeholder**: "e.g. Grocery shopping"
- **Max**: 255 characters

#### Notes Field (Optional)
- **Label**: "Notes (optional)"
- **Input**: Multi-line text
- **Placeholder**: "Add additional notes..."
- **Max**: 1000 characters

### Action Button
- **Save Expense Button**
  - Full width
  - Green background
  - White text
  - Bottom of screen (sticky)

### Validation
- Amount must be > 0
- Category is required
- Description is required
- Date cannot be in future

### States
- Default (empty form)
- Editing (pre-filled for update)
- Saving (show loading)
- Success (navigate back)
- Error (show error message)

---

## 7. Categories Screen

### Layout
- **Grid Layout**: 2-3 columns
- **Card Design**: Each category is a card

#### Category Card
- **Icon**: Material icon (colored background circle)
- **Name**: Category name below icon
- **Background**: White card with shadow
- **Corner Radius**: Rounded

### Example Categories
Row 1:
- Food & Dining (orange/red icon)
- Transportation (blue car icon)

Row 2:
- Housing (purple house icon)
- Utilities (yellow lightning icon)

Row 3:
- Entertainment (pink game controller icon)
- Health (green plus icon)

Row 4:
- Shopping (teal shopping bag icon)
- Education (blue graduation cap icon)

Row 5:
- Other (gray dots icon)

### Actions
- **Tap category**: Select for expense/income
- **Long press**: Edit or delete (if custom)

### Management (Settings)
- Add custom category
- Edit category (name, color, icon)
- Delete category (if no transactions)

---

## 8. Reports Screen

### Top Bar
- **Back button** (left)
- **Title**: "Reports"

### Period Selector
- **Tabs**:
  - Spending (selected)
  - Income
  - Net Worth

### Date Range Selector
- **Dropdown**: "Last 30 days" ▼
- **Options**:
  - This week
  - This month
  - Last month
  - Last 3 months
  - Last 6 months
  - This year
  - Custom range

### Pie Chart Section
- **Center Label**: "Total Spending"
- **Amount**: "$1,050.00"
- **Chart**: Donut/pie chart with category colors
- **Segments**: Each category with different color

#### Legend (Below Chart)
- **List of categories** with:
  - Color indicator (small circle)
  - Category name
  - Percentage (e.g., "26%")
  - Amount (e.g., "$294.00")

**Example Categories**:
1. Food & Dining - 26% - $294.00 (Red)
2. Transportation - 16% - $189.00 (Blue)
3. Housing - 15% - $168.00 (Purple)
4. Utilities - 12% - $126.00 (Yellow)
5. Entertainment - 10% - $105.00 (Pink)
6. Other - 16% - $168.00 (Gray)

### Bar Chart Section (Monthly Trend)
- **Title**: "Monthly Trend"
- **Date Navigation**: < April 2025 >
- **X-Axis**: Dates (Apr 1, Apr 8, Apr 15, Apr 22, Apr 29)
- **Y-Axis**: Amount (1K, 2K, 3K, etc.)
- **Bars**: 
  - Green bars for income
  - Red bars for expenses
  - Side by side for comparison

### Summary Cards (Bottom)
- **Total Income**: $3,500.00
- **Total Expenses**: $1,050.00
- **Net Savings**: $2,450.00 (green)

---

## 9. Financial Summary Screen

### Top Bar
- **Back button** (left)
- **Title**: "Financial Summary"

### Period Selector
- **Tabs**:
  - Monthly (selected)
  - Yearly

### Date Navigation
- **Left arrow** < April 2025 **Right arrow** >

### Bar Chart
- **Combined chart**: Income vs Expenses
- **Legend**:
  - Green: Income
  - Red: Expenses
- **Data points**: Show both values for each period

### Summary Section
- **Total Income**: $3,500.00
- **Total Expenses**: $1,050.00
- **Net Savings**: $2,450.00 (highlighted, green)

---

## 10. Settings Screen

### Profile Section
- **Avatar**: Circle with initials or image
- **Name**: "John Doe"
- **Email**: "john@example.com"

### Settings List

#### Account & Security
- Icon: Shield
- Arrow: >
- Opens: Account settings

#### Currency
- Icon: Currency symbol
- Current: "USD"
- Arrow: >
- Opens: Currency selector

#### Notifications
- Icon: Bell
- Arrow: >
- Opens: Notification preferences

#### Sync & Offline
- Icon: Cloud sync
- Arrow: >
- Opens: Sync settings

#### Help & Support
- Icon: Question mark
- Arrow: >
- Opens: Help center

#### About
- Icon: Info
- Arrow: >
- Opens: About screen

### Logout Button
- **Text**: "Log Out"
- **Color**: Red
- **Border**: Red outline
- **Full width**

---

## 11. Offline/Sync Screen

### Top Bar
- **Back button** (left)
- **Title**: "You're offline"

### Illustration
- **Cloud icon** with X or disconnected symbol
- **Centered**

### Message
- **Title**: "You're offline"
- **Subtitle**: "No internet connected. Your data will sync automatically when you're back online."

### Action Button
- **Continue Button**
  - Green background
  - Full width
  - Returns to previous screen

### Pending Sync Indicator
- Show number of pending changes
- "3 changes waiting to sync"

---

## Common UI Patterns

### Colors
- **Primary Green**: #00897B
- **Income Green**: #27AE60
- **Expense Red**: #E74C3C
- **Background**: #F5F5F5
- **Card Background**: #FFFFFF
- **Text Primary**: #212121
- **Text Secondary**: #757575
- **Border**: #E0E0E0

### Typography
- **Titles**: 20-24sp, Bold
- **Body**: 14-16sp, Regular
- **Captions**: 12sp, Regular
- **Amounts**: 28-32sp, Bold (for large displays)

### Components
- **Buttons**: 48dp height, rounded corners (8dp)
- **Cards**: Elevation 2dp, rounded corners (12dp)
- **Input Fields**: 56dp height, outlined style
- **Icons**: 24dp size (Material Icons)
- **FAB**: 56dp size, elevation 6dp

### Spacing
- **Screen Padding**: 16dp
- **Card Margin**: 8dp
- **Element Spacing**: 8-16dp
- **Section Spacing**: 24dp

---

## Implementation Priority

### Phase 1 (MVP)
1. Splash Screen
2. Login Screen
3. Register Screen
4. Dashboard Screen
5. Add Expense Screen
6. Transactions Screen
7. Categories Screen

### Phase 2
8. Reports Screen
9. Financial Summary Screen
10. Settings Screen
11. Sync/Offline Screen

### Phase 3 (Enhancements)
- Edit screens (expense, income, profile)
- Delete confirmation dialogs
- Search and filter functionality
- Export reports
- Notifications

---

## XML Layout Requirements

### Use ViewBinding
- Enable ViewBinding in build.gradle
- Access views type-safely
- No findViewById calls

### Use ConstraintLayout
- Primary layout for most screens
- Responsive design
- Handle different screen sizes

### Use RecyclerView
- Transaction lists
- Category grids
- Report lists

### Use Material Components
- MaterialButton
- TextInputLayout
- MaterialCardView
- BottomNavigationView
- FloatingActionButton
- MaterialDatePicker

### Use Navigation Component
- Define navigation graph
- Safe Args for parameters
- Handle back stack properly

---

## Accessibility Requirements

- **Content Descriptions**: All icons and images
- **Touch Targets**: Minimum 48dp x 48dp
- **Text Scaling**: Support system font size
- **Color Contrast**: WCAG AA compliance
- **Screen Reader**: All elements properly labeled

---

## Responsive Design

### Support
- **Portrait** (primary)
- **Landscape** (optional, nice-to-have)

### Screen Sizes
- **Small**: 320dp width minimum
- **Medium**: 360dp - 480dp (most common)
- **Large**: 480dp+ (tablets - optional)

### Adapt
- Use `dimen` values for all sizes
- Use `layout-sw<N>dp` for tablets
- Test on multiple device sizes

---

## Next Steps

1. Create XML layouts for each screen
2. Implement ViewModels and UI state
3. Connect to data layer (repositories)
4. Add navigation between screens
5. Implement user interactions
6. Add animations and transitions
7. Test on multiple devices
8. Conduct accessibility audit

---

## Resources Needed

### Assets
- App icon (launcher icon)
- Category icons (Material Icons)
- Illustrations for empty states
- Colors defined in colors.xml
- Dimensions in dimens.xml
- Strings in strings.xml (for i18n)

### Material Icons Used
- restaurant (Food & Dining)
- directions_car (Transport)
- home (Housing)
- bolt (Utilities)
- shopping_bag (Shopping)
- movie (Entertainment)
- local_hospital (Health)
- school (Education)
- trending_up (Income)
- more_horiz (Other)

---

This document serves as the complete UI/UX specification for implementing all screens in the Smart Budget Android application using XML layouts and Android Views (no Compose).
