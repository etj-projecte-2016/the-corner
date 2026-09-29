# The Corner

The Corner is an Android application for creating and running boxing workouts.

## Local AI setup

The debug configuration can use Groq for local development and portfolio demonstrations. Obtain your own Groq API key and add it to a local `local.properties` file:

```properties
GROQ_API_KEY=their_own_key
```

`local.properties` is intentionally excluded from Git because it may contain machine-specific settings and credentials. Never commit an API key. The project does not ship the maintainer's Groq API key; each developer must provide their own key locally. Groq AI is available only in the development/debug configuration.

Release builds receive no Groq API key.
