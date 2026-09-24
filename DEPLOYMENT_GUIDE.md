# Smart Budget - Deployment Guide

## Overview
This guide covers the complete deployment process for the Smart Budget application, including backend setup, database configuration, and mobile app distribution.

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Database Setup](#database-setup)
3. [Backend Deployment](#backend-deployment)
4. [Environment Configuration](#environment-configuration)
5. [Android App Build](#android-app-build)
6. [CI/CD Setup](#cicd-setup)
7. [Monitoring & Maintenance](#monitoring--maintenance)

---

## Prerequisites

### Required Tools
- **PostgreSQL 14+** (Neon.tech recommended for cloud hosting)
- **Node.js 18+** or **Java 17+** (depending on backend choice)
- **Docker & Docker Compose** (optional, for containerization)
- **Android Studio** (for app builds)
- **Git** for version control

### Recommended Services
- **Neon.tech** - Serverless PostgreSQL hosting
- **Railway.app** or **Heroku** - Backend hosting
- **Google Play Console** - Android app distribution
- **Firebase** - Push notifications, analytics

---

## Database Setup

### Option 1: Neon.tech (Recommended)

**Step 1: Create Neon Project**
```bash
# Sign up at https://neon.tech
# Create a new project: "smartbudget-prod"
# Select region closest to your users
# Copy the connection string
```

**Step 2: Run Database Migrations**
```sql
-- Connect to your Neon database
psql postgresql://user:password@host/smartbudget

-- Run schema from /database/schema.sql
\i database/schema.sql

-- Run migrations
\i database/migrations/001_initial_schema.sql
\i database/migrations/002_add_indexes.sql
\i database/migrations/003_add_functions.sql
```

**Step 3: Create Database User**
```sql
-- Create application user with limited permissions
CREATE USER smartbudget_app WITH PASSWORD 'strong_password_here';

-- Grant necessary permissions
GRANT CONNECT ON DATABASE smartbudget TO smartbudget_app;
GRANT USAGE ON SCHEMA public TO smartbudget_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO smartbudget_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO smartbudget_app;
```

**Connection String Format:**
```
postgresql://smartbudget_app:password@host.neon.tech/smartbudget?sslmode=require
```

### Option 2: Self-Hosted PostgreSQL

**Using Docker:**
```bash
# Create docker-compose.yml
docker-compose up -d postgres

# Run migrations
docker exec -i postgres psql -U smartbudget < database/schema.sql
```

**Docker Compose Configuration:**
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: smartbudget
      POSTGRES_USER: smartbudget
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql

volumes:
  postgres_data:
```

---

## Backend Deployment

### Backend Technology Options

#### Option A: Node.js/Express (Recommended)
#### Option B: Spring Boot (Java)
#### Option C: Django (Python)

### Node.js Backend Setup

**Step 1: Project Structure**
```
backend/
├── src/
│   ├── controllers/
│   ├── models/
│   ├── routes/
│   ├── middleware/
│   └── config/
├── package.json
├── .env.example
└── Dockerfile
```

**Step 2: Install Dependencies**
```bash
npm install express pg bcrypt jsonwebtoken dotenv cors helmet
npm install -D typescript @types/node nodemon
```

**Step 3: Environment Variables**
```env
# .env.production
NODE_ENV=production
PORT=8080

# Database
DATABASE_URL=postgresql://user:password@host/smartbudget
DB_POOL_MIN=2
DB_POOL_MAX=10

# JWT
JWT_SECRET=your-super-secret-jwt-key-min-32-chars
JWT_REFRESH_SECRET=your-refresh-token-secret
JWT_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=30d

# CORS
ALLOWED_ORIGINS=https://yourapp.com,https://api.yourapp.com

# Rate Limiting
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100
```

**Step 4: Deploy to Railway/Heroku**

**Railway:**
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Initialize project
railway init

# Link to project
railway link

# Deploy
railway up

# Add environment variables
railway variables set DATABASE_URL="postgres://..."
railway variables set JWT_SECRET="..."
```

**Heroku:**
```bash
# Install Heroku CLI
npm install -g heroku

# Login
heroku login

# Create app
heroku create smartbudget-api

# Add PostgreSQL
heroku addons:create heroku-postgresql:hobby-dev

# Deploy
git push heroku main

# Set environment variables
heroku config:set JWT_SECRET="your-secret"
heroku config:set NODE_ENV="production"
```

**Step 5: Dockerfile (Optional)**
```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .

EXPOSE 8080

CMD ["node", "src/server.js"]
```

---

## Environment Configuration

### Android App Configuration

**Step 1: Update build.gradle.kts**
```kotlin
android {
    buildTypes {
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://api.yourapp.com/api/v1/\"")
            buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
            
            // Enable R8 optimization
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

**Step 2: Update API Base URL**
```kotlin
// In your API configuration
object ApiConfig {
    val BASE_URL: String = BuildConfig.API_BASE_URL
}
```

**Step 3: ProGuard Rules (proguard-rules.pro)**
```proguard
# Keep domain models
-keep class com.example.smartbudget.domain.model.** { *; }

# Keep DTOs
-keep class com.example.smartbudget.data.remote.dto.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
```

---

## Android App Build

### Debug Build
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build

**Step 1: Generate Signing Key**
```bash
keytool -genkey -v -keystore smartbudget-release.keystore \
  -alias smartbudget -keyalg RSA -keysize 2048 -validity 10000
```

**Step 2: Configure Signing (gradle.properties)**
```properties
RELEASE_STORE_FILE=../smartbudget-release.keystore
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=smartbudget
RELEASE_KEY_PASSWORD=your_key_password
```

**Step 3: Update build.gradle.kts**
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(project.properties["RELEASE_STORE_FILE"] as String)
            storePassword = project.properties["RELEASE_STORE_PASSWORD"] as String
            keyAlias = project.properties["RELEASE_KEY_ALIAS"] as String
            keyPassword = project.properties["RELEASE_KEY_PASSWORD"] as String
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

**Step 4: Build Release APK**
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

**Step 5: Build App Bundle (for Play Store)**
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

### Testing the Release Build
```bash
# Install on device
adb install app/build/outputs/apk/release/app-release.apk

# Verify signing
keytool -list -printcert -jarfile app/build/outputs/apk/release/app-release.apk
```

---

## CI/CD Setup

### GitHub Actions

**Create .github/workflows/android.yml:**
```yaml
name: Android CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Run tests
      run: ./gradlew test
      
    - name: Build debug APK
      run: ./gradlew assembleDebug
      
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk

  release:
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    needs: build
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Decode Keystore
      env:
        ENCODED_STRING: ${{ secrets.KEYSTORE_BASE64 }}
      run: |
        echo $ENCODED_STRING | base64 -di > smartbudget-release.keystore
    
    - name: Build Release Bundle
      env:
        RELEASE_STORE_PASSWORD: ${{ secrets.RELEASE_STORE_PASSWORD }}
        RELEASE_KEY_PASSWORD: ${{ secrets.RELEASE_KEY_PASSWORD }}
      run: ./gradlew bundleRelease
    
    - name: Upload to Play Store
      uses: r0adkll/upload-google-play@v1
      with:
        serviceAccountJsonPlainText: ${{ secrets.SERVICE_ACCOUNT_JSON }}
        packageName: com.example.smartbudget
        releaseFiles: app/build/outputs/bundle/release/app-release.aab
        track: internal
```

### Secrets Configuration
```bash
# Encode keystore for GitHub Secrets
base64 -i smartbudget-release.keystore | pbcopy

# Add to GitHub repository secrets:
# - KEYSTORE_BASE64
# - RELEASE_STORE_PASSWORD
# - RELEASE_KEY_PASSWORD
# - SERVICE_ACCOUNT_JSON (for Play Store)
```

---

## Monitoring & Maintenance

### Backend Monitoring

**Health Check Endpoint:**
```javascript
// /health
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    database: 'connected' // Check DB connection
  });
});
```

**Logging:**
```javascript
// Use Winston or Pino for structured logging
const winston = require('winston');

const logger = winston.createLogger({
  level: 'info',
  format: winston.format.json(),
  transports: [
    new winston.transports.File({ filename: 'error.log', level: 'error' }),
    new winston.transports.File({ filename: 'combined.log' })
  ]
});
```

### Database Backup

**Automated Backups (Neon.tech):**
- Neon provides automatic backups
- Point-in-time recovery available
- Export via pg_dump:

```bash
pg_dump -h host.neon.tech -U user -d smartbudget > backup_$(date +%Y%m%d).sql
```

**Backup Script:**
```bash
#!/bin/bash
# backup.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"
DB_NAME="smartbudget"

pg_dump $DATABASE_URL > $BACKUP_DIR/smartbudget_$DATE.sql

# Upload to S3 (optional)
aws s3 cp $BACKUP_DIR/smartbudget_$DATE.sql s3://smartbudget-backups/

# Keep only last 30 days
find $BACKUP_DIR -name "smartbudget_*.sql" -mtime +30 -delete
```

### App Analytics

**Firebase Integration:**
```kotlin
// build.gradle.kts
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-analytics-ktx")
implementation("com.google.firebase:firebase-crashlytics-ktx")
```

**Crashlytics Setup:**
```kotlin
// In Application class
FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
```

---

## Security Checklist

### Backend Security
- [ ] HTTPS enforced (SSL/TLS certificates)
- [ ] JWT tokens with proper expiration
- [ ] Rate limiting enabled
- [ ] CORS configured correctly
- [ ] SQL injection prevention (parameterized queries)
- [ ] Input validation on all endpoints
- [ ] Sensitive data encrypted at rest
- [ ] Regular dependency updates
- [ ] API versioning implemented

### Mobile App Security
- [ ] API keys not hardcoded (use BuildConfig)
- [ ] Certificate pinning (optional)
- [ ] Root detection (optional)
- [ ] ProGuard/R8 enabled
- [ ] Sensitive data encrypted (Android Keystore)
- [ ] Network security config
- [ ] App signing configured
- [ ] No debug logs in production

### Database Security
- [ ] Strong passwords
- [ ] Limited user permissions
- [ ] SSL connections required
- [ ] Regular backups
- [ ] Access logs enabled
- [ ] IP whitelisting (if applicable)

---

## Production Checklist

### Pre-Launch
- [ ] All tests passing
- [ ] API base URL updated to production
- [ ] Database migrations tested
- [ ] Environment variables configured
- [ ] Signing keys secured
- [ ] Privacy policy added
- [ ] Terms of service added
- [ ] Analytics configured
- [ ] Crash reporting enabled

### Launch
- [ ] Deploy backend to production
- [ ] Verify database connectivity
- [ ] Test API endpoints
- [ ] Build release APK/AAB
- [ ] Upload to Play Store
- [ ] Submit for review

### Post-Launch
- [ ] Monitor error rates
- [ ] Check analytics data
- [ ] Review crash reports
- [ ] Monitor API performance
- [ ] Gather user feedback
- [ ] Plan updates

---

## Troubleshooting

### Common Issues

**Database Connection Fails:**
```bash
# Check connection string
psql $DATABASE_URL

# Verify SSL mode
psql "postgresql://host/db?sslmode=require"

# Check firewall rules
```

**API 502 Errors:**
- Check backend logs
- Verify environment variables
- Check database connection pool
- Increase timeout settings

**App Crashes on Startup:**
- Check ProGuard rules
- Verify API base URL
- Check for missing permissions
- Review crash logs in Firebase

**Sync Not Working:**
- Verify WorkManager constraints
- Check network permissions
- Review sync logs
- Check API authentication

---

## Support & Resources

- **Documentation:** https://docs.yourapp.com
- **API Reference:** https://api.yourapp.com/docs
- **GitHub:** https://github.com/yourusername/smartbudget
- **Support Email:** support@yourapp.com

---

## Version History

- **1.0.0** - Initial production release
- **1.0.1** - Bug fixes and performance improvements
- **1.1.0** - New features and UI enhancements

---

**Last Updated:** 2024
**Maintained By:** Smart Budget Team
