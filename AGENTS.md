# AGENTS.md

This file contains guidelines and commands for agentic coding agents working in the Kassemblix repository.

## Project Overview

Kassemblix is a parser combinator library written in Kotlin, inspired by the Java parsing library. It provides:
- Lexer components for tokenizing input
- Parser combinators for building grammars
- Assembly-based parsing architecture
- Support for ambiguous grammar detection

## Build Commands

### Maven Commands
```bash
# Build the project
mvn compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=KParserTest

# Run a single test method
mvn test -Dtest=KParserTest#test_completeMatch_returns_null_for_incomplete_match

# Clean and build
mvn clean compile

# Package the project
mvn package

# Install to local repository
mvn install
```

### Code Quality Commands
```bash
# Run ktlint formatting check
mvn ktlint:check

# Apply ktlint formatting
mvn ktlint:format

# Run detekt static analysis
mvn detekt:check

# Run all quality checks (executed during verify phase)
mvn verify
```

## Code Style Guidelines

### Kotlin Conventions
- Follow official Kotlin coding conventions
- Use 4 spaces for indentation (no tabs)
- Use camelCase for variables and functions
- Use PascalCase for classes and interfaces
- Use UPPER_SNAKE_CASE for constants

### Package Structure
```
net.codenest.kassemblix/
├── lexer/           # Tokenization components
├── parser/          # Parser combinators
└── examples/        # Example implementations
```

### Import Organization
- Group imports in three sections:
  1. Kotlin standard library
  2. Third-party libraries
  3. Project imports
- Use wildcard imports sparingly
- Sort imports alphabetically within each section

### Class and Function Design
- Abstract classes should have clear documentation
- Use sealed classes where appropriate for parser hierarchies
- Parser classes should extend `KParser<T>`
- Lexer components should handle character-by-character processing
- Use extension functions for utility operations

### Error Handling
- Use exceptions for parsing errors and ambiguity detection
- Define constant strings for error messages (e.g., `ERR_AMBIGUOUS_GRAMMAR`)
- Throw meaningful exceptions with descriptive messages
- Handle edge cases in tokenizers and parsers

### Testing Guidelines
- Use JUnit 5 with descriptive test names using backticks
- Structure tests with:
  - `@BeforeEach` for common setup
  - Clear test method names describing the scenario
  - Assertions with meaningful messages
- Test both success and failure cases
- Include comprehensive documentation for complex test scenarios

### Documentation
- Use KDoc for public APIs
- Include examples for complex parser combinators
- Document ambiguity detection behavior
- Explain the assembly-based parsing approach

### Parser Implementation Patterns
- Implement `match(assemblies: List<KAssembly<T>>)` abstract method
- Use `matchAndAssemble()` for applying assemblers
- Handle ambiguity detection in `bestMatch()`
- Return `null` from `completeMatch()` for incomplete matches
- Clone assemblies when creating multiple parse paths

### Lexer Implementation Patterns
- Use `PushbackReader` for character lookahead
- Implement state-based tokenization
- Handle character ranges efficiently
- Return `KToken.END` for EOF conditions

### Performance Considerations
- Minimize object creation in hot paths
- Use efficient data structures for assembly stacks
- Consider memoization for expensive parser operations
- Profile ambiguous grammar detection performance

## Maven Plugin Configuration

The project uses these key Maven plugins:
- `kotlin-maven-plugin` for Kotlin compilation
- `maven-surefire-plugin` for test execution
- `ktlint-maven-plugin` for code formatting
- `detekt-maven-plugin` for static analysis

## Dependencies

- Kotlin 2.1.10
- JUnit 5.10.3 for testing
- Xerces 2.12.2 for XML parsing (if needed)

## Notes for Agents

1. Always run tests after making changes
2. Check code formatting with ktlint before committing
3. Be mindful of ambiguity detection in parser implementations
4. Follow the existing patterns in the codebase
5. Test both Kotlin and Java interop if relevant
6. Consider the assembly-based architecture when designing new components