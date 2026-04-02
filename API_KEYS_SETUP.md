# API Keys Setup Guide

This document explains how to properly configure API keys for the Uber Clone application.

## Overview

The application uses several third-party services that require API keys:
- **Google Maps API** - For map functionality and location services
- **Google OAuth** - For Google Sign-In authentication
- **Facebook SDK** - For Facebook Login authentication

## Security Best Practices

✅ **DO:**
- Store API keys in `local.properties` file
- Use different API keys for development and production
- Restrict API keys to specific applications and services
- Keep API keys private and never commit them to version control

❌ **DON'T:**
- Hardcode API keys in source code
- Commit `local.properties` to version control
- Share API keys in public repositories
- Use production keys for development

## Setup Instructions

### 1. Copy the Template File

```bash
cp local.properties.template local.properties
```

### 2. Configure Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable **Maps SDK for Android**
4. Go to **APIs & Services > Credentials**
5. Click **Create Credentials > API Key**
6. Restrict the key:
   - Application restrictions: Android apps
   - Add your package name: `co.ke.snilloc.uberclone`
   - Add your SHA-1 certificate fingerprint
7. Copy the API key and replace `YOUR_GOOGLE_MAPS_API_KEY` in `local.properties`

### 3. Configure Google OAuth Web Client ID

1. In the same Google Cloud Console project
2. Go to **APIs & Services > Credentials**
3. Click **Create Credentials > OAuth 2.0 Client ID**
4. Choose **Web application**
5. Copy the Client ID and replace `YOUR_GOOGLE_WEB_CLIENT_ID` in `local.properties`

### 4. Configure Facebook App

1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app or use existing one
3. Go to **Settings > Basic**
4. Copy the **App ID** and replace `YOUR_FACEBOOK_APP_ID` in `local.properties`
5. Generate a **Client Token** and replace `YOUR_FACEBOOK_CLIENT_TOKEN` in `local.properties`
6. Configure Android platform:
   - Add package name: `co.ke.snilloc.uberclone`
   - Add your key hash

### 5. Verify Configuration

After setting up all API keys, build the project:

```bash
./gradlew assembleDebug
```

If successful, your API keys are properly configured!

## File Structure

```
├── local.properties              # Your actual API keys (DO NOT COMMIT)
├── local.properties.template     # Template file for reference
├── .gitignore                   # Ensures local.properties is ignored
└── API_KEYS_SETUP.md           # This documentation
```

## Troubleshooting

### Build Errors
- Ensure all placeholder values in `local.properties` are replaced
- Check that API keys don't contain extra spaces or quotes
- Verify that `local.properties` is in the root directory

### Maps Not Loading
- Verify Maps SDK for Android is enabled
- Check API key restrictions match your app's package name and SHA-1
- Ensure billing is enabled for your Google Cloud project

### Authentication Issues
- Verify OAuth client ID is for web application type
- Check Facebook app configuration matches your package name
- Ensure key hashes are correctly configured for Facebook

## Security Notes

- The `local.properties` file is automatically ignored by Git
- API keys are injected at build time, not stored in the APK as plain text
- Use Android's ProGuard/R8 for additional obfuscation in release builds
- Consider using Android Keystore for additional security in production

## Production Deployment

For production builds:
1. Create separate API keys for production
2. Use environment variables or secure key management systems
3. Enable additional API key restrictions
4. Monitor API usage and set up billing alerts