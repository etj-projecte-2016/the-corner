# The Corner

The Corner is an Android application for creating and running boxing workouts.

## Tech stack

- Kotlin
- XML layouts
- View Binding
- MVVM
- Android Fragments
- Navigation Component
- Room
- Kotlin Coroutines and Flow
- Gradle Kotlin DSL

## Architecture

The application broadly follows MVVM.

Prefer this dependency flow:

UI (Fragment)
→ ViewModel
→ Repository
→ DAO
→ Room

Keep UI rendering and user interaction in Fragments.

Keep application state and presentation logic in ViewModels.

Database access should go through repositories. UI classes must not access DAOs or Room directly.

## Project structure

Follow the existing package structure and conventions before introducing new packages or architectural layers.

Prefer extending existing components over creating duplicate abstractions.

## Android UI

This project uses XML layouts and View Binding.

Do not introduce Jetpack Compose unless explicitly requested.

Follow the existing visual design and reuse existing resources, styles, dimensions, and components when appropriate.

## Code changes

Before modifying code:

1. Inspect the relevant existing implementation.
2. Identify which files need to change.
3. Reuse existing models, repositories, and utilities where appropriate.
4. Avoid unrelated refactors.

After making changes:

1. Review the modified files.
2. Check for obvious compilation issues.
3. Run the relevant build or tests when appropriate.
4. Explain what was changed and why.

## Important

Do not add libraries or change the project architecture unless the task requires it.

Do not modify unrelated files.

If a requested change conflicts with the existing architecture, explain the conflict before introducing a new architectural pattern.