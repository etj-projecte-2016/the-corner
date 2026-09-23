---
name: android-feature
description: Implement or extend an Android feature in The Corner while following the existing MVVM architecture and project conventions.
---

# Android Feature

Use this skill when implementing a new Android feature or substantially extending an existing feature in The Corner.

## Before implementation

First inspect the existing code related to the feature.

Identify:

- Existing Fragments
- ViewModels
- models
- repositories
- DAOs
- navigation destinations
- XML layouts
- resources that can be reused

Do not start implementation until the existing flow is understood.

## Plan

Before modifying files, determine:

1. What behavior needs to be added.
2. Which existing components can be reused.
3. Which files need modification.
4. Which new files, if any, are actually necessary.
5. How data will flow through the feature.

Prefer the existing architecture:

UI
→ ViewModel
→ Repository
→ DAO
→ Room

Not every feature requires every layer.

Do not create unnecessary abstractions.

## Implementation

Follow the existing project conventions.

Prefer:

- Kotlin
- XML layouts
- View Binding
- ViewModels for UI state and presentation logic
- Coroutines for asynchronous work
- Flow or StateFlow where appropriate
- Existing repositories for data access
- Navigation Component for screen navigation

Reuse existing resources and components whenever practical.

Do not introduce a new library unless necessary.

## Verification

After implementation:

1. Review all changed files.
2. Check navigation if it was modified.
3. Check state handling.
4. Check database interactions if persistence was modified.
5. Build the relevant module when appropriate.
6. Run relevant tests when available.

If the build or tests fail because of your changes, investigate and fix the failure before finishing.

## Final response

Summarize:

- What was implemented
- Files created
- Files modified
- Important architectural decisions
- Verification performed
- Any remaining limitations or follow-up work