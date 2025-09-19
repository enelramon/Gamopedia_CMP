# GitHub Copilot Instructions for Gamopedia

## Project Overview

**Gamopedia** is a modern, modular Kotlin Multiplatform (KMP) Android application built with **Jetpack Compose**. It's designed to explore games, search for them, and manage favorites. The app follows **MVVM + Clean Architecture** principles with comprehensive modularization.

## Architecture & Patterns

### Clean Architecture Layers
Each feature module follows this structure:
```
feature-x/
├── data/         # DTOs, Repository Implementation, Data Sources (Remote/Local)
├── domain/       # UseCases, Repository Interfaces, Models
└── ui/           # ViewModels, Composables, Events/States (renamed from presentation)
```

### Data Flow
```
User Action
    ↓
ui (ViewModel, Composables, Events/States)
    ↓
domain (UseCase → Repository Interface → Domain Models)
    ↓
data (Repository Implementation → DTOs, Mappers, Data Sources)
    ↓
core (core-network with Ktor / core-database with Room Database)
```

## Project Structure

```
com.gamopedia/
├── composeApp/           # Application entry point and DI setup
├── core/
│   ├── core-network/     # Ktor client configuration and API services
│   └── core-database/    # Room database setup and DAOs
├── common/               # Shared resources: UI components, utilities, extensions
│   ├── ui/               # Shared UI components
│   ├── data/             # Shared data utilities
│   └── domain/           # Shared domain models
└── feature/
    ├── search/           # Search games by name or keyword
    ├── game/             # Game dashboard and game details
    └── favorite/         # List of all favorited (cached) games
```

## Technology Stack

| Category               | Technology                 |
|------------------------|----------------------------|
| Language               | Kotlin                     |
| UI                     | Jetpack Compose            |
| Architecture           | MVVM + Clean Architecture  |
| Networking             | Ktor Client                |
| Caching/DB             | Room Database              |
| Dependency Injection   | Koin                       |
| Navigation             | Jetpack Navigation Compose |
| Concurrency            | Kotlin Coroutines + Flow   |
| Modularization         | Feature + Layer-wise       |
| Platforms              | Android, iOS, Desktop      |

## Module Guidelines

### Feature Modules
Each feature (search, game, favorite) contains:
- **ui/**: ViewModels, Composable screens, DI modules
- **domain/**: Use cases, repository interfaces, domain models
- **data/**: Repository implementations, DTOs, data sources, mappers

### Core Modules
- **core-network**: Ktor client setup, API services, interceptors
- **core-database**: Room database, DAOs, entities

### Common Module
- **common/ui**: Shared UI components, themes, styles
- **common/data**: Shared data utilities, constants, mappers
- **common/domain**: Shared domain models and interfaces

#### Domain Models
Domain models are shared across features and defined in `common/domain`:
```kotlin
data class Game(
    val id: Int,
    val name: String,
    val imageUrl: String
)
```

#### Data Mappers
Mappers convert DTOs to domain models and are placed in `common/data/mappers`:
```kotlin
fun List<gaur.himanshu.coreNetwork.model.game.Result>.toDomainListOfGames(): List<Game> = map {
    Game(
        id = it.id,
        name = it.name,
        imageUrl = it.background_image
    )
}
```

## Coding Conventions

### Package Structure
Follow this naming convention:
```
gaur.himanshu.{module}.{layer}.{component}
```
Examples:
- `gaur.himanshu.search.ui.SearchScreen`
- `gaur.himanshu.game.domain.useCases.GetGameDetailsUseCase`
- `gaur.himanshu.coreNetwork.client.KtorClient`

### File Naming
- **Screens**: `{Feature}Screen.kt` (e.g., `SearchScreen.kt`)
- **ViewModels**: `{Feature}ViewModel.kt` (e.g., `SearchViewModel.kt`)
- **Use Cases**: `{Action}UseCase.kt` (e.g., `SearchGamesUseCase.kt`)
- **Repositories**: `{Feature}Repository.kt` (interface), `{Feature}RepositoryImpl.kt` (implementation)
- **DI Modules**: `{Feature}{Layer}Module.kt` (e.g., `SearchUiModule.kt`)

### Compose Guidelines
- Use `@Composable` functions for UI components
- Follow Material Design principles
- Use `Modifier` parameter as the first parameter in Composables
- Implement proper state management with `remember` and `rememberSaveable`
- Use `LazyColumn` for lists and `LazyRow` for horizontal lists

### ViewModel Patterns
Follow this exact pattern used in the project:
```kotlin
class SearchViewModel(
    private val searchGameUseCase: SearchGamesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchScreen.UiState())
    val uiState = _uiState.asStateFlow()
    
    private val _query = MutableStateFlow("")
    
    fun updateQuery(q: String) {
        _query.update { q }
    }
    
    init {
        viewModelScope.launch {
            _query
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .debounce(500)
                .collectLatest { query ->
                    search(query)
                }
        }
    }
    
    private fun search(q: String) = searchGameUseCase.invoke(q)
        .onStart {
            _uiState.update { SearchScreen.UiState(isLoading = true) }
        }.onEach { result ->
            result.onSuccess { data ->
                _uiState.update { SearchScreen.UiState(data = data) }
            }.onFailure { error ->
                _uiState.update { SearchScreen.UiState(error = error.message.toString()) }
            }
        }.launchIn(viewModelScope)
}

// UI State as nested data class within Screen object
data object SearchScreen {
    data class UiState(
        val isLoading: Boolean = false,
        val error: String = "",
        val data: List<Game>? = null
    )
}
```

### Repository Pattern
Follow this exact pattern:
```kotlin
// Domain interface
interface SearchRepository {
    suspend fun search(q: String): Result<List<Game>>
}

// Data implementation
class SearchRepositoryImpl(
    private val apiService: ApiService
) : SearchRepository {
    override suspend fun search(q: String): Result<List<Game>> {
        return try {
            val response = apiService.search(q)
            val data = response.getOrThrow().results.toDomainListOfGames()
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Dependency Injection (Koin)

### Module Structure
Each layer has its own Koin module using function-based modules:
- `get{Feature}UiModule()` - ViewModels and UI dependencies
- `get{Feature}DomainModule()` - Use cases
- `get{Feature}DataModule()` - Repositories and data sources
- `getCoreNetworkModule()` - Network dependencies
- `getCoreDatabaseModule()` - Database dependencies

### Example Modules
```kotlin
// UI Module
fun getSearchUiModule() = module {
    viewModel { SearchViewModel(searchGameUseCase = get()) }
}

// Domain Module
fun getSearchDomainModule() = module {
    factory { SearchGamesUseCase(get()) }
}

// Data Module
fun getSearchDataModule() = module {
    single<SearchRepository> { SearchRepositoryImpl(get()) }
}

// Core Network Module
fun getCoreNetworkModule() = module {
    single { ApiService(httpClient = KtorClient.getInstance()) }
}
```

### DI Setup in Main App
All modules are initialized in `AppDiSetup.kt`:
```kotlin
fun initKoin(koinApplication: ((KoinApplication) -> Unit)? = null) {
    startKoin {
        koinApplication?.invoke(this)
        modules(
            getCoreNetworkModule(),
            getGameDataModule(),
            getGameDomainModule(),
            getGameUiModule(),
            getSearchDataModule(),
            getSearchDomainModule(),
            getSearchUiModule(),
            getCoreDatabaseModule(),
            getFavoriteDataModule(),
            getFavoriteDomainModule(),
            getFavoriteUiModule()
        )
    }
}
```

## Navigation

### Navigation Setup
- Uses **Jetpack Navigation Compose**
- Each feature module defines its own navigation graph
- Navigation graphs implement `BaseNavGraph` interface
- Shared navigation routes are in the common module

### Navigation Pattern
```kotlin
object SearchNavGraph : BaseNavGraph {
    sealed class Dest(val route: String) {
        data object Root : Dest("/search-root")
        data object Search : Dest("/search")
    }
    
    override fun build(
        modifier: Modifier,
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder
    ) {
        // Navigation implementation
    }
}
```

## Database (Room)

### Entity Design
```kotlin
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    // Additional fields
)
```

### DAO Pattern
```kotlin
@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE name LIKE '%' || :query || '%'")
    fun searchGames(query: String): Flow<List<GameEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)
}
```

## Network (Ktor)

### API Service Pattern
```kotlin
interface ApiService {
    suspend fun searchGames(query: String): List<GameDto>
    suspend fun getGameDetails(id: String): GameDto
}

class ApiServiceImpl(
    private val client: HttpClient
) : ApiService {
    // Implementation
}
```

## Build & Testing

### Available Gradle Tasks
- `./gradlew build` - Build all modules
- `./gradlew test` - Run all tests
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK

### Testing Patterns
- Unit tests for ViewModels, Use cases, and Repositories
- Use `kotlinx-coroutines-test` for testing coroutines
- Mock dependencies using Mockk or similar frameworks
- Follow AAA pattern (Arrange, Act, Assert)

### Module Dependencies
When adding dependencies:
- Add to `gradle/libs.versions.toml` for version management
- Reference using `libs.{dependency.name}` in build.gradle.kts files
- Keep API dependencies in the appropriate layer (data, domain, ui)

## Platform-Specific Considerations

### Android
- MainActivity extends ComponentActivity
- Application class extends Application and initializes Koin
- Android-specific dependencies in androidMain source set

### iOS
- Swift integration through ComposeApp framework
- iOS-specific dependencies in iosMain source set
- Database path configuration for iOS

### Desktop
- Main class for desktop application
- Desktop-specific dependencies in desktopMain source set

## Code Generation & Tooling

### Room Database
- Use `@Database`, `@Entity`, `@Dao` annotations
- Configure Room plugin in build.gradle.kts
- Generate database schemas

### Kotlin Serialization
- Use `@Serializable` for DTOs
- Configure kotlinx.serialization plugin

### UI State Management
Use this exact pattern with nested data classes:
```kotlin
data object SearchScreen {
    data class UiState(
        val isLoading: Boolean = false,
        val error: String = "",
        val data: List<Game>? = null
    )
}
```

### Result Handling
Use Kotlin's built-in `Result` type for error handling:
```kotlin
// In Repository
return try {
    val response = apiService.search(query)
    val data = response.getOrThrow().results.toDomainListOfGames()
    Result.success(data)
} catch (e: Exception) {
    Result.failure(e)
}

// In ViewModel
result.onSuccess { data ->
    _uiState.update { SearchScreen.UiState(data = data) }
}.onFailure { error ->
    _uiState.update { SearchScreen.UiState(error = error.message.toString()) }
}
```

## Performance Guidelines

- Use `LazyColumn`/`LazyRow` for large lists
- Implement proper image loading with Coil
- Use `Flow` for reactive data streams
- Implement proper cancellation in coroutines
- Cache data locally using Room database

## Security

- Never commit API keys or sensitive data
- Use ProGuard/R8 for release builds
- Implement proper error handling to avoid data leaks

## When Contributing

1. Follow the existing architecture patterns
2. Add appropriate tests for new features
3. Update documentation when adding new modules
4. Use the established naming conventions
5. Ensure proper separation of concerns across layers
6. Add Koin modules for new dependencies
7. Follow the existing navigation patterns
8. Use established error handling patterns

This guide ensures consistency across the codebase and helps maintain the clean architecture principles that make Gamopedia scalable and maintainable.