# 🤖 Gamopedia Agents Architecture

This document describes the **agents** (autonomous components, modules, and features) that comprise the Gamopedia application. Each agent represents a specialized component responsible for specific functionality within the app's ecosystem.

---

## 🏗️ Architecture Overview

Gamopedia follows a **multi-agent modular architecture** where each feature module acts as an independent agent with its own responsibilities, data flow, and lifecycle. The system is built on **MVVM + Clean Architecture** principles with clear separation of concerns.

```
┌─────────────────────────────────────────────────────────────┐
│                        App Container                        │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Search Agent  │ │   Game Agent    │ │ Favorite Agent  │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │  Network Agent  │ │ Database Agent  │ │  Common Agent   │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Core Agents

### 🌐 Network Agent (`core-network`)
**Responsibility**: Handles all external communication and API interactions.

**Location**: `/core-network`

**Key Components**:
- **Ktor Client Configuration**: HTTP client setup with interceptors, logging, and timeouts
- **API Services**: Remote data source abstractions
- **Response Handlers**: Error handling and response parsing
- **DTOs**: Data transfer objects for network communication

**Agent Behavior**:
```kotlin
// Network Agent Interface
interface NetworkAgent {
    suspend fun fetchGames(query: String): Result<List<GameDTO>>
    suspend fun fetchGameDetails(id: Int): Result<GameDetailsDTO>
}
```

**Dependencies**: None (Core agent)
**Consumers**: Search Agent, Game Agent

---

### 🗄️ Database Agent (`core-database`)
**Responsibility**: Manages local data persistence and caching strategies.

**Location**: `/core-database`

**Key Components**:
- **Room Database Setup**: Type-safe local database configuration
- **DAOs**: Data access objects for CRUD operations
- **Entities**: Local database schema definitions
- **Cache Management**: Offline data storage and retrieval

**Agent Behavior**:
```kotlin
// Database Agent Interface
interface DatabaseAgent {
    suspend fun saveGame(game: GameEntity): Result<Unit>
    suspend fun getFavoriteGames(): Flow<List<GameEntity>>
    suspend fun deleteGame(gameId: Int): Result<Unit>
}
```

**Dependencies**: None (Core agent)
**Consumers**: Game Agent, Favorite Agent

---

### 🔗 Common Agent (`common`)
**Responsibility**: Provides shared resources and utilities across all agents.

**Location**: `/common`

**Key Components**:
- **UI Components**: Reusable composables (GameItem, buttons, etc.)
- **Utilities**: Extensions, constants, and helper functions
- **Domain Models**: Shared data models across modules
- **Navigation Definitions**: Common navigation types and routes

**Agent Behavior**:
```kotlin
// Common Agent Interface
interface CommonAgent {
    fun provideSharedComponents(): List<Composable>
    fun provideUtilities(): Map<String, Any>
    fun provideSharedModels(): List<Class<*>>
}
```

**Dependencies**: None (Core agent)
**Consumers**: All feature agents

---

## 🎮 Feature Agents

### 🔍 Search Agent (`search`)
**Responsibility**: Handles game search functionality and user queries.

**Location**: `/search`

**Module Structure**:
```
search/
├── data/     # Repository implementation, remote data sources
├── domain/   # Use cases, repository interfaces, business logic
└── ui/       # ViewModels, composables, UI states
```

**Key Components**:
- **SearchGamesUseCase**: Business logic for game searching
- **SearchRepository**: Data layer abstraction
- **SearchViewModel**: UI state management
- **SearchScreen**: Composable UI implementation

**Agent Behavior**:
```kotlin
// Search Agent Flow
User Input → SearchViewModel → SearchGamesUseCase → SearchRepository → NetworkAgent
                ↓
SearchScreen ← UI State ← Result Processing ← Network Response
```

**Dependencies**: Network Agent, Common Agent
**Communication**: Sends search results to Game Agent for details view

---

### 🕹️ Game Agent (`game`)
**Responsibility**: Manages game dashboard, details display, and favorites management.

**Location**: `/game`

**Module Structure**:
```
game/
├── data/     # Repository implementation, mappers, data sources
├── domain/   # Use cases, models, repository interfaces
└── ui/       # ViewModels, screens, UI components
```

**Key Components**:
- **GetGameDetailsUseCase**: Fetches detailed game information
- **SaveGameUseCase**: Adds games to favorites
- **DeleteGameUseCase**: Removes games from favorites
- **GameRepository**: Data layer coordination
- **GameViewModel & GameDetailsViewModel**: UI state management
- **GameScreen & GameDetailsScreen**: UI implementation

**Agent Behavior**:
```kotlin
// Game Agent Flow
Game Selection → GameDetailsViewModel → GetGameDetailsUseCase → Repository
                        ↓
Favorite Action → SaveGameUseCase → DatabaseAgent
                        ↓
GameDetailsScreen ← UI State Update ← Success/Error Handling
```

**Dependencies**: Network Agent, Database Agent, Common Agent
**Communication**: Receives data from Search Agent, sends favorites to Favorite Agent

---

### ⭐ Favorite Agent (`favorite`)
**Responsibility**: Manages user's favorite games collection and offline access.

**Location**: `/favorite`

**Module Structure**:
```
favorite/
├── data/     # Local repository implementation
├── domain/   # Use cases, models for favorites
└── ui/       # ViewModels, favorites list UI
```

**Key Components**:
- **GetFavoritesUseCase**: Retrieves cached favorite games
- **FavoriteRepository**: Local data management
- **FavoriteViewModel**: UI state for favorites list
- **FavoriteScreen**: Favorites list UI

**Agent Behavior**:
```kotlin
// Favorite Agent Flow
App Launch → FavoriteViewModel → GetFavoritesUseCase → Repository → DatabaseAgent
                    ↓
FavoriteScreen ← Flow<List<Game>> ← Real-time Updates ← Database Changes
```

**Dependencies**: Database Agent, Common Agent
**Communication**: Receives favorites from Game Agent, provides offline access

---

## 🔄 Agent Interaction Flow

### 1. **Search to Game Flow**
```
SearchAgent → User searches "Zelda" 
           → NetworkAgent fetches results
           → User selects game
           → GameAgent displays details
```

### 2. **Game to Favorite Flow**
```
GameAgent → User favorites a game
         → DatabaseAgent saves locally
         → FavoriteAgent updates list
```

### 3. **Offline to Online Flow**
```
FavoriteAgent → Shows cached games offline
             → NetworkAgent reconnects
             → GameAgent fetches latest details
```

---

## 🧩 Dependency Injection Architecture

Each agent declares its dependencies through **Koin modules**:

```kotlin
// Agent DI Setup in AppDiSetup.kt
fun initKoin() {
    startKoin {
        modules(
            // Core Agents
            getCoreNetworkModule(),    // Network Agent
            getCoreDatabaseModule(),   // Database Agent
            
            // Feature Agents
            getSearchDataModule(),     // Search Agent - Data Layer
            getSearchDomainModule(),   // Search Agent - Domain Layer
            getSearchUiModule(),       // Search Agent - UI Layer
            
            getGameDataModule(),       // Game Agent - Data Layer
            getGameDomainModule(),     // Game Agent - Domain Layer
            getGameUiModule(),         // Game Agent - UI Layer
            
            getFavoriteDataModule(),   // Favorite Agent - Data Layer
            getFavoriteDomainModule(), // Favorite Agent - Domain Layer
            getFavoriteUiModule()      // Favorite Agent - UI Layer
        )
    }
}
```

---

## 🚀 Agent Lifecycle

### 1. **Initialization Phase**
```
App Start → Koin DI Setup → Agent Registration → Core Agents Init → Feature Agents Ready
```

### 2. **Runtime Phase**
```
User Interaction → Agent Communication → State Updates → UI Rendering
```

### 3. **Background Phase**
```
App Background → Data Persistence → Cache Management → Resource Cleanup
```

---

## 🛠️ Development Guidelines for Agents

### **Creating a New Agent**
1. **Structure**: Follow the 3-layer architecture (data/domain/ui)
2. **Dependencies**: Declare in dedicated Koin modules
3. **Communication**: Use repository pattern for data flow
4. **Testing**: Implement unit tests for each layer
5. **Documentation**: Update this agents.md file

### **Agent Communication Rules**
- **Direct Dependencies**: Only through DI container
- **Data Sharing**: Via repository interfaces
- **UI Communication**: Through shared ViewModels or navigation
- **Event Handling**: Using Kotlin Flow and StateFlow

### **Performance Considerations**
- **Lazy Loading**: Agents initialize components only when needed
- **Memory Management**: Proper cleanup in ViewModels
- **Caching Strategy**: Intelligent use of Database Agent
- **Background Tasks**: Use Kotlin Coroutines for async operations

---

## 🔧 Technology Stack per Agent

| Agent | Primary Technologies |
|-------|---------------------|
| **Network Agent** | Ktor Client, Kotlinx Serialization |
| **Database Agent** | Room Database, SQLDelight |
| **Common Agent** | Jetpack Compose, Material Design |
| **Search Agent** | Compose Navigation, Flow |
| **Game Agent** | AsyncImage, State Management |
| **Favorite Agent** | Room Flow, Offline-First |

---

## 📊 Agent Metrics & Monitoring

### **Key Performance Indicators**
- **Network Agent**: Response times, error rates, cache hit ratio
- **Database Agent**: Query performance, storage usage
- **Search Agent**: Search latency, result accuracy
- **Game Agent**: Detail load times, favorite success rate
- **Favorite Agent**: Offline availability, sync performance

### **Debugging Tools**
- **Logging**: Each agent has dedicated logging tags
- **State Inspection**: ViewModel state tracking
- **Network Monitoring**: Ktor interceptors for API calls
- **Database Inspection**: Room database inspector

---

## 🚧 Future Agent Expansion

### **Potential New Agents**
- **Recommendation Agent**: AI-powered game suggestions
- **Social Agent**: User reviews and ratings
- **Notification Agent**: Push notifications and updates
- **Analytics Agent**: User behavior tracking
- **Sync Agent**: Cloud synchronization

### **Agent Enhancement Ideas**
- **Search Agent**: Voice search, image recognition
- **Game Agent**: Augmented reality previews
- **Favorite Agent**: Smart collections, auto-categorization

---

This agents architecture enables **scalable, maintainable, and testable** development while providing clear boundaries and responsibilities for each component of the Gamopedia application.