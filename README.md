# @komyun/capacitor-native-passkey

Capacitor plugin for native WebAuthn/Passkey APIs on Android and iOS.

**This is a maintained fork** of `@darkedges/capacitor-native-passkey` with critical fixes for Android support.

## Why this fork?

The original repository (`@darkedges/capacitor-native-passkey`) appears to be offline/unavailable. This fork includes essential fixes for production Android apps:

### Critical Fixes Applied

1. **Android CredentialManager Support** (`WebAuthn.java`)
   - **Issue**: Original code hardcoded `val = false` for all WebAuthn availability checks, causing passkeys to always report as unavailable
   - **Fix**: Properly checks if `CredentialManager` is initialized before returning availability status
   - **Impact**: WebAuthn/Passkeys now work correctly on Android 9+ devices with Google Play Services

2. **Hybrid Credentials Support** (`WebAuthnPlugin.java`)
   - **Issue**: `preferImmediatelyAvailableCredentials` was set to `true`, limiting credential options
   - **Fix**: Changed to `false` to allow hybrid credentials (e.g., passkeys from other devices)
   - **Impact**: Better cross-device passkey experience

## Requirements

- **Android**: API 28+ (Android 9.0+) with Google Play Services
- **iOS**: iOS 14+ with WKWebView
- **Capacitor**: v6.0.0+

## Installation

```bash
npm install @komyun/capacitor-native-passkey
npx cap sync
```

Or with pnpm:

```bash
pnpm add @komyun/capacitor-native-passkey
pnpm cap:sync
```

## Configuration

### Android

Ensure your `android/variables.gradle` has minimum SDK 28:

```gradle
ext {
    minSdkVersion = 28  // Required for CredentialManager
    compileSdkVersion = 35
    targetSdkVersion = 35
}
```

### iOS

No additional configuration required. The plugin includes a podspec that will be automatically configured.

## Usage

```typescript
import { WebAuthn } from '@komyun/capacitor-native-passkey';

// Check if passkeys are supported
const result = await WebAuthn.isWebAuthnAvailable();
console.log('Passkeys supported:', result.value);

// Register a new passkey
const registrationResponse = await WebAuthn.startRegistration(options);

// Authenticate with existing passkey
const authResponse = await WebAuthn.startAuthentication(options);
```

## API

### `isWebAuthnAvailable()`

Returns whether WebAuthn/Passkeys are available on the current device.

**Returns:** `Promise<{ value: boolean }>`

### `startRegistration(options)`

Creates a new passkey credential.

**Parameters:**
- `options` - WebAuthn registration options (PublicKeyCredentialCreationOptions)

**Returns:** `Promise<PublicKeyCredential>`

### `startAuthentication(options)`

Authenticates using an existing passkey.

**Parameters:**
- `options` - WebAuthn authentication options (PublicKeyCredentialRequestOptions)

**Returns:** `Promise<PublicKeyCredential>`

## Compatibility

This plugin is compatible with:
- `@simplewebauthn/browser` - For web-based WebAuthn operations
- `@simplewebauthn/server` - For server-side validation
- Capacitor v7 (Capacitor v6 peer dependency specified for compatibility)

## Differences from Original

- Fixed Android availability checks (no longer always returns false)
- Updated to support hybrid credentials
- Maintained and tested with Capacitor v7
- Active maintenance and bug fixes

## Original Repository

Original work by DarkEdges: `@darkedges/capacitor-native-passkey` (now offline)

## License

Apache-2.0 (same as original)

## Support

For issues or questions:
- GitHub Issues: https://github.com/billzajac/capacitor-native-passkey/issues
- Original implementation credit: DarkEdges

## Changelog

### v0.0.7 (2025-01-01)
- **BREAKING**: Minimum Android SDK increased to API 28 (Android 9.0+)
- Fixed: Android WebAuthn availability check now properly detects CredentialManager
- Fixed: Enabled hybrid credentials support
- Updated: Package namespace to `@komyun/capacitor-native-passkey`
- Improved: Documentation and README

### v0.0.6 (Original)
- Initial version by DarkEdges
