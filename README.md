# Kassemblix

A Kotlin parser combinator library converted from Steven John Metsker's Java
parser framework, originally presented in his book
*"Building Parsers with Java"* (Addison-Wesley, 2001).

## Background

Many years ago, when I read *Building Parsers with Java* by Steven John Metsker,
I was delighted by his simple yet elegant object-oriented approach to building
parser combinators—quite different from what I had learned as a computer science
student at university. Recently, with the help of Claude Code, I converted this
legacy Java code to Kotlin. I hope you find it enjoyable.

## What are Parser Combinators?

Parser combinators are a technique for building parsers by combining small,
simple parsers into larger, more complex ones:

| Combinator | Meaning | Notation |
|------------|---------|----------|
| **Sequence** | A then B then C | `A B C` |
| **Alternation** | A or B or C | `A \| B \| C` |
| **Repetition** | Zero or more | `A*` |

Instead of writing one monolithic parser, you compose tiny parsers that each
match simple things (numbers, words, symbols).

---

## Parser Categories

Parsers are classified by **how they process input** and **how they build the parse tree**.

### Top-Down vs Bottom-Up

| Approach | Direction | Builds Tree | Example |
|----------|-----------|-------------|---------|
| **Top-Down** | Left-to-right, starts from root | From root to leaves | Recursive descent, LL parsers |
| **Bottom-Up** | Left-to-right, starts from leaves | From leaves to root | LR, LALR parsers (yacc, bison) |

Think of parsing `2 + 3` as building a tree. The two approaches build it differently:

**Top-down parsers** work like reading a recipe. Start with the goal ("make a meal") and
break it into smaller steps until you reach the ingredients.

```
Goal: Parse an expression
  ↓
"An expression is: number, then operator, then number"
  ↓
Check input: is "2" a number? ✓
Check input: is "+" an operator? ✓
Check input: is "3" a number? ✓
  ↓
Success! Built the tree from top to bottom:

        [expression]        ← started here
        /    |    \
       2    "+"    3        ← ended here
```

**Bottom-up parsers** work like solving a jigsaw puzzle. Start with the pieces (tokens)
and group them together until you form the complete picture.

```
Input tokens: 2  +  3

Step 1: See "2" → that's a number!
Step 2: See "+" → hold onto it...
Step 3: See "3" → that's a number!
Step 4: "number + number" → that's an expression!
  ↓
Success! Built the tree from bottom to top:

       2     "+"     3      ← started here
        \     |     /
        [expression]        ← ended here
```

### Common Parser Types

| Type | Lookahead | Direction | Notes |
|------|-----------|-----------|-------|
| **LL(1)** | 1 token | Top-down | Simple, hand-written parsers |
| **LL(k)** | k tokens | Top-down | More powerful, parser combinators |
| **LR(1)** | 1 token | Bottom-up | Handles more grammars, tool-generated |
| **LALR(1)** | 1 token | Bottom-up | yacc, bison — smaller tables than LR |

The "L" means left-to-right scanning. The second letter indicates derivation direction
(L = leftmost, R = rightmost). The number is how many tokens of lookahead.

### Where Parser Combinators Fit

Parser combinators like Kassemblix are **top-down, LL(k) parsers**:

- **Top-down**: They start from the root rule and recursively descend
- **LL(k)**: They can look ahead multiple tokens to decide which path to take
- **Backtracking**: Unlike strict LL parsers, combinators can try alternatives and backtrack

**Trade-offs:**

| Advantage | Disadvantage |
|-----------|--------------|
| Easy to write and understand | Cannot handle left recursion |
| Grammars map directly to code | May be slower (backtracking) |
| No separate build step | Ambiguous grammars can explode |
| Composable and modular | Error messages can be unclear |

For most DSLs, configuration languages, and simple programming languages,
parser combinators offer an excellent balance of power and simplicity.

---

## Quick Start

```kotlin
import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*

// Grammar: expr = Num ('-' Num)*
val expr = KSequence<KToken>()
    .add(KNum())
    .add(KRepetition(subParser = KSequence<KToken>()
        .add(KSymbol('-'))
        .add(KNum())))

val result = expr.completeMatch(KTokenAssembly("25 - 16 - 9"))
println(result?.getStack())  // [25.0, -, 16.0, -, 9.0]
```

---

## Architecture

The library is organized into two main packages:

### Lexer (`net.codenest.kassemblix.lexer`)

Converts input text into a stream of tokens using a state-machine approach.

| Class | Description |
|-------|-------------|
| `KTokenizer` | Divides input into tokens |
| `KToken` | Token with type, string value, and numeric value |
| `KTokenizerState` | Interface for tokenizer states |
| `KTokenizerStateTable` | Maps characters to their handling states |
| `KCharReader` | Character reader with pushback support |

**Built-in States:**

| State | Recognizes |
|-------|------------|
| `KWordState` | Words and identifiers |
| `KNumberState` | Integers, decimals, scientific notation |
| `KQuoteState` | Quoted strings |
| `KSymbolState` | Symbols and operators |
| `KWhitespaceState` | Whitespace |
| `KSlashState` | `/`, `//` and `/* */` comments |

### Parser (`net.codenest.kassemblix.parser`)

Uses parser combinators to build complex grammars from simple components.

| Class | Description |
|-------|-------------|
| `KParser<T>` | Abstract base class for all parsers |
| `KAssembly<T>` | Work area with input items and result stack |
| `KTokenAssembly` | Assembly for token-based parsing |
| `KCharAssembly` | Assembly for character-based parsing |
| `KAssembler<T>` | Interface for semantic actions |

**Combinators:**

| Class | Pattern | Description |
|-------|---------|-------------|
| `KSequence<T>` | `A B C` | Matches parsers in sequence |
| `KAlternation<T>` | `A \| B \| C` | Matches any alternative |
| `KRepetition<T>` | `A*` | Matches zero or more |
| `KEmpty<T>` | `ε` | Matches empty input |

**Terminals:**

| Class | Matches |
|-------|---------|
| `KNum` | Numbers |
| `KWord` | Words |
| `KSymbol` | Specific symbols |
| `KLiteral` | Specific literals |
| `KQuotedString` | Quoted strings |
| `KChar` | Any character |
| `KSpecificChar` | A specific character |
| `KLetter` | Letters |
| `KDigit` | Digits |

---

## Usage

### Adding Semantic Actions

Use assemblers to perform computations during parsing:

```kotlin
// Assembler to convert token to numeric value
class NumAssembler : KAssembler<KToken> {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop() as KToken
        assembly.push(token.nval)
    }
}

// Assembler to perform subtraction
class MinusAssembler : KAssembler<KToken> {
    override fun workOn(assembly: KAssembly<KToken>) {
        val d1 = assembly.pop() as Double
        val d2 = assembly.pop() as Double
        assembly.push(d2 - d1)
    }
}

// Build parser with assemblers
val num = KNum().setAssembler(NumAssembler())
val minus = KSequence<KToken>()
    .add(KSymbol('-').discard())  // discard() removes token from stack
    .add(num)
    .also { it.setAssembler(MinusAssembler()) }

val expr = KSequence<KToken>()
    .add(num)
    .add(KRepetition(subParser = minus))

val result = expr.completeMatch(KTokenAssembly("25 - 16 - 9"))
println(result?.pop())  // 0.0 (computed as (25 - 16) - 9)
```

### Character-Level Parsing

```kotlin
// Match letters followed by digits (e.g., "abc123")
val parser = KSequence<Char>()
    .add(KRepetition(subParser = KLetter()))
    .add(KRepetition(subParser = KDigit()))

val result = parser.completeMatch(KCharAssembly("abc123"))
```

---

## Grammar Patterns

### Avoiding Left Recursion

A **left recursive** grammar is one where a rule references itself as the leftmost symbol:

```
expr = expr + term
```

**Why it causes infinite loops in top-down parsers:**

When a recursive descent parser tries to parse `expr`:

1. To match `expr`, it calls the `expr()` function
2. The first thing `expr()` does is try to match `expr` (the leftmost symbol)
3. So it calls `expr()` again
4. Which calls `expr()` again...
5. **Infinite loop** — no input is ever consumed

```kotlin
fun expr() {
    expr()      // calls itself immediately, never returns
    match('+')
    term()
}
```

The parser never consumes any input tokens before recursing. It keeps calling itself
hoping to eventually match something, but it never gets to the `+ term` part because
it's stuck trying to resolve the first `expr`.

**The fix — use repetition instead:**

```
# BAD (left-recursive):
expr = expr '-' Num | Num

# GOOD (use repetition):
expr = Num ('-' Num)*
```

Now the parser consumes a `Num` first, *then* repeats — guaranteeing progress on each iteration.

**Key takeaway:** Left recursion = recursion without consuming input = infinite loop.
Always ensure your parser makes progress (consumes tokens) before recursing.

### Operator Precedence

Handle precedence by nesting grammar rules:

```
expression = term ('+' term | '-' term)*
term       = factor ('*' factor | '/' factor)*
factor     = phrase ('^' factor)?
phrase     = '(' expression ')' | Num
```

### Left Associativity

Use repetition for left-associative operators:

```
# Right-associative (WRONG for subtraction):
expr = Num '-' expr | Num     # 25 - 16 - 9 = 18

# Left-associative (CORRECT):
expr = Num ('-' Num)*          # 25 - 16 - 9 = 0
```

---

## Examples

The test directory contains examples demonstrating various parsing
techniques:

| Example | Description |
|---------|-------------|
| `arithmetic` | Arithmetic parser with operator precedence |
| `minimath` | Minimal math parser demonstrating associativity |
| `coffee` | Domain-specific language for coffee orders |
| `robot` | Command parser for robot movements |
| `query` | SQL-like query language parser |
| `logic` | Boolean logic expression parser |
| `sling` | Expression language for graphics/animation |

---

## Build

**Prerequisites:** JDK 11+, Maven 3.6+

```bash
mvn compile      # Compile
mvn test         # Run tests
mvn package      # Package as JAR
mvn install      # Install to local repository
```

**Code Quality:**

```bash
mvn ktlint:check    # Check formatting
mvn ktlint:format   # Auto-fix formatting
mvn detekt:check    # Static analysis
```

---

## License

This is a derivative work based on code from
*"Building Parsers with Java"* by Steven John Metsker.

## Authors

- **Steven J. Metsker** — Original Java implementation
- **Alan K. Sun** — Kotlin conversion
