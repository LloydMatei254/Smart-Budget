# SmartBudget - Android Finance Tracking App

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![MVVM](https://img.shields.io/badge/Architecture-MVVM-orange.svg)](https://developer.android.com/topic/architecture)
[![Offline-First](https://img.shields.io/badge/Strategy-Offline--First-red.svg)](https://developer.android.com/topic/architecture/data-layer/offline-first)

A modern, offline-first Android finance tracking application built with MVVM architecture, Room database, and XML layouts. Track expenses, manage income, categorize transactions, and sync with a Neon PostgreSQL backend.

## 🚀 Features

- **Expense Tracking**: Record expenses with categories, payment methods, and receipt photos
- **Income Management**: Track multiple income sources with recurring income support
- **Category Management**: Create and manage custom expense/income categories with color coding
- **Offline-First**: Full functionality without internet connection, auto-sync when online
- **Financial Dashboard**: Real-time overview of spending, income, and budget status
- **Receipt Photos**: Capture and store receipt images with expenses
- **Payment Methods**: Manage multiple payment methods (cash, credit cards, digital wallets)
- **Reports & Analytics**: View spending patterns and financial summaries
- **Data Sync**: Automatic background synchronization with PostgreSQL backend
- **Secure Authentication**: JWT-based authentication with encrypted local storage

## 📱 Screenshots

*Coming soon*

## 🏗️ Architecture

This app follows **Clean Architecture** principles with **MVVM** pattern:

```
app/
├── data/                    # Data layer
│   ├── local/              # Room database, DAOs, entities
│   ├── remote/             # Retrofit APIs, DTOs, interceptors
│   ├── repository/         # Repository implementations
│   ├── mapper/             # Entity ↔ Domain model mappers
│   └── worker/             # Background sync workers
├── domain/                  # Domain layer
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Business logic use cases
├── presentation/            # Presentation layer
│   ├── auth/               # Login/Register screens
│   ├── dashboard/          # Dashboard screen
│   ├── expenses/           # Expense management screens
│   ├── income/             # Income management screens
│   ├── categories/         # Category management
│   ├── reports/            # Reports & analytics
│   └── common/             # Shared UI components
├── di/                      # Dependency injection (Hilt)
└── utils/                   # Utilities and helpers
```

### Key Components

- **Data Layer**: Room for local persistence, Retrofit for API calls
- **Domain Layer**: Pure Kotlin models and business logic
- **Presentation Layer**: ViewModels + XML layouts + Fragments
- **Dependency Injection**: Hilt for compile-time DI
- **Navigation**: Navigation Component with Safe Args
- **Async Operations**: Kotlin Coroutines + Flow
- **Background Sync**: WorkManager for periodic sync

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| Architecture | MVVM + Clean Architecture |
| UI | XML Layouts (Material Design 3) |
| Local Database | Room |
| Remote Database | Neon PostgreSQL |
| Networking | Retrofit2 + OkHttp |
| Dependency Injection | Hilt |
| Async | Coroutines + Flow |
| Navigation | Navigation Component |
| Background Tasks | WorkManager |
| Image Loading | Coil |
| Security | EncryptedSharedPreferences |

## 📦 Dependencies

See [DEPENDENCIES.md](DEPENDENCIES.md) for complete dependency list.

### Core Dependencies
```kotlin
// AndroidX Core
androidx.core:core-ktx:1.13.1
androidx.appcompat:appcompat:1.7.0
androidx.activity:activity-ktx:1.9.0
androidx.fragment:fragment-ktx:1.8.0

// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// Hilt Dependency Injection
com.google.dagger:hilt-android:2.51.1

// Navigation Component
androidx.navigation:navigation-fragment-ktx:2.7.7
androidx.navigation:navigation-ui-ktx:2.7.7

// Network
com.squareup.retrofit2:retrofit:2.11.0
com.squareup.okhttp3:okhttp:4.12.0

// WorkManager
androidx.work:work-runtime-ktx:2.9.0
```

## 🚦 Getting Started

### Prerequisites

- Android Studio Jellyfish or later
- JDK 17 or later
- Android SDK 24+ (Target: SDK 34)
- Git

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/LloydMatei254/Smart-Budget.git
   cd Smart-Budget
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle dependencies
   - Wait for the build to complete

4. **Configure Backend API** (Optional for offline-only testing)
   - Update `BASE_URL` in `app/src/main/java/com/example/smartbudget/utils/Constants.kt`
   - See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for backend setup

5. **Run the app**
   - Connect an Android device or start an emulator
   - Click Run ▶️ or press Shift+F10

### Build Variants

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 🗄️ Database Setup

### Local Database (Room)

The app uses Room for local data persistence. Database schema is automatically created on first launch.

**Schema Version**: 1  
**Database Name**: `smart_budget.db`

Tables:
- `users` - User accounts
- `categories` - Expense/Income categories
- `expenses` - Expense transactions
- `incomes` - Income transactions
- `payment_methods` - Payment method details

See schema: `app/schemas/com.example.smartbudget.data.local.database.SmartBudgetDatabase/1.json`

### Remote Database (Neon PostgreSQL)

For backend setup and database migrations, see:
- [database/migrations/](database/migrations/) - SQL migration scripts
- [api/API_SPECIFICATION.md](api/API_SPECIFICATION.md) - API documentation
- [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) - Deployment instructions

## 🔄 Sync Architecture

The app implements an **offline-first** strategy:

1. **All operations work offline** - Data is saved locally first
2. **Background sync** - WorkManager syncs with server every 15 minutes
3. **Conflict resolution** - Last-write-wins with server timestamp
4. **Sync states tracked** - Each entity has sync status (SYNCED/PENDING/FAILED)

See [SYNC_ARCHITECTURE.md](SYNC_ARCHITECTURE.md) for details.

## 📸 Receipt Photo Storage

Receipt photos are stored in the app's external files directory:
- **Local**: `/storage/emulated/0/Android/data/com.example.smartbudget/files/receipts/`
- **Remote**: Uploaded to backend storage and referenced by URL

## 🔐 Security

- JWT authentication with refresh tokens
- Encrypted SharedPreferences for sensitive data
- Network security config for API communications
- ProGuard/R8 obfuscation for release builds

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Test coverage
./gradlew jacocoTestReport
```

## 📄 Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - Detailed architecture overview
- [DEPENDENCIES.md](DEPENDENCIES.md) - Complete dependency list
- [DEPENDENCY_INJECTION.md](DEPENDENCY_INJECTION.md) - Hilt DI setup
- [SYNC_ARCHITECTURE.md](SYNC_ARCHITECTURE.md) - Offline-first sync design
- [UI_STATE_MANAGEMENT.md](UI_STATE_MANAGEMENT.md) - State management patterns
- [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) - Backend deployment guide
- [api/API_SPECIFICATION.md](api/API_SPECIFICATION.md) - REST API specification

## 🛣️ Roadmap

- [ ] Dark mode support
- [ ] Budgets and spending limits
- [ ] Recurring expenses/income automation
- [ ] Export data (CSV, PDF)
- [ ] Multi-currency support
- [ ] Biometric authentication
- [ ] Widgets for quick expense entry
- [ ] Cloud backup/restore

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Lloyd Matei**
- GitHub: [@LloydMatei254](https://github.com/LloydMatei254)

## 🙏 Acknowledgments

- [Material Design](https://material.io/) for design guidelines
- [Android Jetpack](https://developer.android.com/jetpack) for modern Android components
- [Neon](https://neon.tech/) for serverless PostgreSQL
- Android community for excellent libraries and tools

## 📞 Support

For issues, questions, or suggestions:
- Open an [issue](https://github.com/LloydMatei254/Smart-Budget/issues)
- Contact: [Your contact info]

---

**Built with ❤️ using Android + Kotlin**
