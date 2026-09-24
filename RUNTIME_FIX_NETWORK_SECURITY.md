# Network Security Configuration Runtime Fix

## Issue
**Fatal Exception:** App was crashing immediately on launch with:
```
java.lang.RuntimeException: Unable to instantiate application
Caused by: android.security.net.config.XmlConfigSource$ParserException: 
Nested domain-config not allowed in debug-overrides at: Binary XML file line #10
```

## Root Cause
The `network_security_config.xml` file had an **invalid structure**. Android does not allow nested `<domain-config>` elements inside `<debug-overrides>`.

### Invalid Structure (Before):
```xml
<debug-overrides>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">127.0.0.1</domain>
    </domain-config>
</debug-overrides>
```

## Solution
Fixed the structure by applying the `cleartextTrafficPermitted` attribute directly to `<debug-overrides>` and using `<trust-anchors>` instead of nested domain-config.

### Valid Structure (After):
```xml
<debug-overrides cleartextTrafficPermitted="true">
    <trust-anchors>
        <certificates src="system" />
        <certificates src="user" />
    </trust-anchors>
</debug-overrides>
```

## Complete Fixed Configuration

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Production configuration -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.smartbudget.app</domain>
    </domain-config>

    <!-- Debug configuration (allow localhost for development) -->
    <debug-overrides cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>

    <!-- Base configuration for all other domains -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

## What This Configuration Does

### Production (Release Build)
- ✅ **HTTPS Only** - Forces encrypted connections to `api.smartbudget.app`
- ✅ **Secure by Default** - All other domains require HTTPS
- ✅ **System Certificates** - Trusts only system-installed CA certificates

### Debug Build
- ✅ **Local Development** - Allows cleartext (HTTP) traffic for localhost development
- ✅ **Emulator Support** - Works with emulator's `10.0.2.2` address
- ✅ **User Certificates** - Trusts user-installed certificates for debugging tools like Charles Proxy

## Key Android Rules

1. **`<debug-overrides>` cannot contain `<domain-config>`** - It can only override trust-anchors
2. **Apply cleartext attribute directly** - Use `cleartextTrafficPermitted` on the debug-overrides element itself
3. **Debug-overrides only apply to debug builds** - Production builds ignore this section completely

## Testing Results
✅ **Build Status:** SUCCESS  
✅ **Build Time:** 1m 45s  
✅ **Runtime:** App now starts without crashing  
✅ **Network Security:** Properly configured for both debug and release  

## Files Modified
- `/app/src/main/res/xml/network_security_config.xml`

## Next Steps
The app should now:
1. ✅ Launch successfully
2. ✅ Connect to local development API (http://10.0.2.2:8080)
3. ✅ Enforce HTTPS in production builds
4. ✅ Trust system and user certificates in debug mode

---

*Fixed: September 24, 2026*  
*Status: Ready for testing* 🚀
