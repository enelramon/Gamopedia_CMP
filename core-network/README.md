# Core Network Module

This module handles all network communication for the Gamopedia app using Ktorfit.

## Overview

The core-network module has been migrated from direct Ktor client usage to Ktorfit for better type-safety and maintainability.

## Architecture

### API Interface (`GameApi.kt`)
- Type-safe API definitions using Retrofit-style annotations
- Handles all RAWG API endpoints:
  - `GET /api/games` - Get list of games
  - `GET /api/games?search={query}` - Search games
  - `GET /api/games/{id}` - Get game details

### Client Setup (`KtorfitClient.kt`)
- Configures Ktor HttpClient with proper settings
- Creates Ktorfit instance with base URL and client configuration
- Provides singleton factory for GameApi

### Service Layer (`ApiService.kt`)
- Wraps Ktorfit calls with Result handling for error management
- Maintains backward compatibility with existing repository implementations
- Provides consistent API for data layer

## Dependencies

- **Ktorfit**: Type-safe HTTP client for Kotlin Multiplatform
- **Ktor Client**: Underlying HTTP client
- **KSP**: Kotlin Symbol Processing for code generation
- **Kotlinx Serialization**: JSON serialization/deserialization

## Migration Notes

The migration from direct Ktor usage to Ktorfit provides:

1. **Type Safety**: Compile-time validation of API calls
2. **Reduced Boilerplate**: No more manual HTTP request building
3. **Better Maintainability**: Centralized API definitions
4. **Retrofit-like Experience**: Familiar annotation-based approach

### Backward Compatibility

All existing repository implementations continue to work without changes as the ApiService interface remains the same.

### Deprecated Components

- `KtorClient.kt`: Deprecated in favor of KtorfitClient