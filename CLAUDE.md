# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Run the application
./mvnw clean javafx:run

# Run tests
./mvnw test

# Package
./mvnw clean package
```

## Tech Stack

- **Java 25** with JavaFX 25 (OpenJFX)
- **Spring Boot 4.0.0-SNAPSHOT** for DI and event publishing
- **AtlantaFX 2.0.0** for UI theming (Cupertino Light/Dark)
- **Apache PDFBox 3.0.6** for PDF rendering
- **Lombok** for boilerplate reduction
- **Ikonli** with Carbon Icons pack

## Architecture

This is a desktop PDF archiver application following a layered architecture with MVVM and event-driven patterns.

### Package Structure

- `dev.arne.smartfiles` - Entry point (`SmartFilesApp`)
- `dev.arne.smartfiles.app` - Presentation layer (MVVM)
- `dev.arne.smartfiles.core` - Business layer (services, models, events)

### Key Patterns

**MVVM in Presentation Layer:**
- `ApplicationModel` - Observable JavaFX properties for reactive UI
- `ApplicationViewBuilder` - Programmatic scene graph construction (no FXML)
- `ApplicationInteractor` - Listens for domain events and updates the model
- `ApplicationController` - Facade that exposes the built view

**Event-Driven Communication:**
- Services publish domain events via Spring's `ApplicationEventPublisher`
- `SmartFilesEvent` is a sealed class permitting specific event types
- `ApplicationInteractor` handles all events and updates `ApplicationModel`
- UI binds to model properties for reactive updates

**Data Flow:**
```
User Action → Service → Domain Event → Interactor → ApplicationModel → UI updates
```

### Persistence

- Data stored in `~/.smartfiles/{tenantId}/` (configurable via `application.properties`)
- `archive.json` - Document metadata
- `settings.json` - User preferences
- PDF files copied to `files/` subdirectory

### Threading

- `ApplicationViewBuilder.APP_EXECUTOR` - Cached thread pool for async PDF rendering
- PDF rendering in `DocumentView` uses background tasks to avoid blocking UI
