# KParser Examples

This directory contains Kotlin examples converted from Steven J. Metsker's "Building Parsers with Java" book.

## Example Packages

| Package | Description | Files | Status |
|---------|-------------|-------|--------|
| preface | Hello World - basic parser introduction | 1 | Converted |
| introduction | Basic parser concepts and terminals | 11 | Converted |
| tokens | Token handling examples | 8 | Converted |
| arithmetic | Arithmetic expression parsing | 8 | Converted |
| chips | Customer/Order domain example | 4 | Converted |
| cloning | Assembly cloning demonstrations | 13 | Converted |
| coffee | Coffee shop grammar example | 18 | Converted |
| design | Parser design patterns | 4 | Converted |
| engine | Logic engine examples | 23 | Converted |
| imperative | Imperative command examples | 7 | Converted |
| karate | Puzzle solving example | 2 | Converted |
| logic | Logic programming examples (non-UI) | 14 | Converted |
| mechanics | Parser mechanics demonstrations | 23 | Converted |
| midimath | Mini math parser | 4 | Converted |
| minimath | Minimal math parser | 6 | Converted |
| pretty | Pretty printing examples | 14 | Converted |
| query | Query language examples | 10 | Converted |
| regular | Regular expression examples | 7 | Converted |
| reserved | Reserved word handling | 4 | Converted |
| robot | Robot command language | 12 | Converted |
| sling | Sling language (graphics DSL) | 43 | Converted (non-UI) |
| string | String parsing examples | 7 | Converted |
| tests | Test utilities | 9 | Converted |
| track | Track grammar example | 3 | Converted |

## Running Examples

Each example is implemented as a JUnit 5 test class. Run them using:

```bash
mvn test -Dtest=ExampleClassName
```

Or run all examples:

```bash
mvn test -Dtest="net.codenest.kassemblix.examples.**"
```

## Example Structure

Each converted example follows this pattern:

1. **Original Java**: Demo classes with `main()` methods that print results
2. **Kotlin Test**: Test classes with `@Test` methods that use assertions

Example conversion pattern:
```java
// Java original
public class ShowHello {
    public static void main(String[] args) {
        // ... setup parser ...
        System.out.println(result);
    }
}
```

```kotlin
// Kotlin test
class ShowHelloTest {
    @Test
    fun `show hello world parsing`() {
        // ... setup parser ...
        assertEquals(expected, result)
    }
}
```

## Dependencies

These examples use:
- `net.codenest.kassemblix.lexing` - Tokenizer and token types (main)
- `net.codenest.kassemblix.parsing` - Parser combinators and assemblies (main)
- `net.codenest.kassemblix.engine` - Logic engine for unification and proof (test)
- `net.codenest.kassemblix.imperative` - Command pattern for imperative execution (test)

Note: The engine and imperative packages are in the test source directory since they
are only used for the example demonstrations and not part of the core parser library.
