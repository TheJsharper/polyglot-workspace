# Standard Library Macros — Test Cases

## What Are Macros?

Macros are code that generates code. Unlike functions (which operate on values at runtime), macros operate on **tokens** at compile time. They can create new syntax, repeat patterns, and inspect their input as text rather than evaluated values.

```rust
// Function: operates on VALUES at runtime
fn add(a: i32, b: i32) -> i32 { a + b }

// Macro: operates on TOKENS at compile time
println!("sum = {}", add(1, 2));  // generates formatting code
```

All standard library macros are available without `use` — they're in the **prelude**.

## Macro Categories and Tests

### Printing Macros (`print!`, `println!`, `eprint!`, `eprintln!`)

Write to stdout or stderr. Cannot be tested automatically (they produce side effects).

| Macro | Writes to | Adds newline? |
|---|---|---|
| `print!` | stdout | No |
| `println!` | stdout | Yes |
| `eprint!` | stderr | No |
| `eprintln!` | stderr | Yes |

### `format!` — Build Strings (Tests: 7)

Creates a `String` from interpolated expressions.

- **`test_format_basic`**: Simple `"{} is {}"` interpolation
- **`test_format_positional_named`**: Mixed positional `{1}` and named `{name}` args
- **`test_format_specifiers`**: Format specifiers — `{:.2}` (precision), `{:0>5}` (padding), `{:b}` (binary), `{:x}` (hex), `{:o}` (octal)
- **`test_format_debug`**: `{:?}` debug formatting for Vec
- **`test_format_pretty_debug`**: `{:#?}` pretty-printed debug
- **`test_format_empty`**: Empty format string
- **`test_format_with_expressions`**: Expressions inside `{}`: `2 + 2`

```rust
format!("{:.2}", 42.12345)   // "42.12"
format!("{:0>5}", 42)         // "00042"
format!("{:b}", 42)           // "101010"
format!("{:?}", vec![1,2])    // "[1, 2]"
```

### Assertion Macros (`assert!`, `assert_eq!`, `assert_ne!`, `assert_matches!`)

Runtime checks that panic on failure. Used in tests and production code.

- **`test_assert_positive_pass/fail`**: `assert!(value > 0)` with custom message
- **`test_assert_eq_pass/fail`**: `assert_eq!(a, b)` — shows both values on failure
- **`test_assert_ne_pass/fail`**: `assert_ne!(a, b)` — panics when equal
- **`test_assert_matches_pass/fail`**: `assert_matches!(value, Some(x) if x > 0)` — pattern + guard

```rust
assert!(x > 0, "must be positive");
assert_eq!(actual, expected, "values differ");
assert_matches!(opt, Some(x) if x > 10);
```

### `debug_assert!` — Debug-Only Assertions (Tests: 1)

Like `assert!` but **stripped in release builds** (`--release`).

- **`test_debug_assert_is_active`**: Verifies the macro runs in debug mode (it does during `cargo test`)

**Key difference:** `assert!` always runs. `debug_assert!` runs in debug, vanishes in release. Use for expensive checks you don't want in production.

### Panic/Flow Macros (`panic!`, `unreachable!`, `todo!`, `unimplemented!`)

Control program flow through panics.

- **`test_panic_message`**: Wraps `panic!` description (can't test actual panic without catching it)
- **`test_panic_macro_actually_panics`**: `std::panic::catch_unwind` proves `panic!()` aborts
- **`test_unreachable_demo_even/odd`**: `unreachable!()` for code paths the type system says are impossible
- **`test_todo_demo`** / **`test_todo_macro_actually_panics`**: `todo!()` → "not yet implemented"
- **`test_unimplemented_demo`** / **`test_unimplemented_macro_actually_panics`**: `unimplemented!()` → "not implemented"

```rust
panic!("something went wrong");       // Abort with message
unreachable!();                       // "impossible" code was reached
todo!();                              // "not yet implemented"
unimplemented!();                     // "not implemented"
```

### Compile-Time Macros (`cfg!`, `env!`, `option_env!`)

Evaluated during compilation.

- **`test_cfg_windows`**: `cfg!(target_os = "windows")` → `true` on Windows, `false` elsewhere
- **`test_cfg_multiple`**: `cfg!(all(target_os = "windows", debug_assertions))` — compound condition
- **`test_env_cargo_pkg_name`**: `env!("CARGO_PKG_NAME")` → `"macro_demo_crate"` from Cargo.toml
- **`test_env_cargo_pkg_version`**: `env!("CARGO_PKG_VERSION")` → `"0.1.0"`
- **`test_option_env_none`**: `option_env!("MY_CUSTOM_VAR")` → `None` (not set at compile time)
- **`test_option_env_some`**: `option_env!("CARGO_PKG_NAME")` → `Some("macro_demo_crate")`

```rust
if cfg!(windows) { /* Windows-specific code */ }
let name = env!("CARGO_PKG_NAME");  // compile-time, fails if not set
let opt = option_env!("MY_VAR");    // compile-time, returns Option
```

### Stringification and Concatenation (`stringify!`, `concat!`)

Work on tokens, not values.

- **`test_stringify_expression`**: `stringify!(2 + 2 == 4)` → `"2 + 2 == 4"` (NOT `"true"`)
- **`test_stringify_function_call`**: `stringify!(some_function(arg1, arg2))` → `"some_function(arg1, arg2)"` — **the function is NOT called**
- **`test_concat_demo`**: `concat!("hello", " ", "world", "!")` → `"hello world!"` — compile-time string building
- **`test_concat_with_stringify`**: Combining `concat!` and `stringify!`
- **`test_concat_empty`** / **`test_concat_single`**: Edge cases

```rust
stringify!(1 + 2)           // "1 + 2" — not "3"!
concat!("a", "b")           // "ab" — &'static str, computed at compile time
```

### Source Location Macros (`file!`, `line!`, `column!`, `module_path!`)

Tell you where in the source code they appear.

- **`test_file_demo`**: `file!()` → `"src/macros/macro_demo.rs"`
- **`test_line_demo`**: `line!()` → the line number (> 0)
- **`test_column_demo`**: `column!()` → the column number (> 0)
- **`test_module_path_demo`**: `module_path!()` → `"macro_demo_crate::macros::macro_demo"`

```rust
println!("error at {}:{}:{}", file!(), line!(), column!());
```

### `dbg!` — Quick Debugging (Tests: 2)

Prints expression, file, line, and value to stderr, then returns the value.

- **`test_dbg_demo`**: Describes what `dbg!(42)` would do
- **`test_dbg_multiple`**: `dbg!(a, b)` prints both and returns tuple

```rust
let x = 5;
dbg!(x);  // prints: [src/main.rs:2] x = 5
// returns 5
```

### `format_args!` — Zero-Allocation Formatting (Tests: 1)

Creates a `fmt::Arguments` struct without allocating a `String`. Used internally by `print!`, `format!`, etc.

- **`test_format_args_demo`**: `format_args!("Hello, {}!", name).to_string()`

### `write!` / `writeln!` — Format Into Any Writer (Tests: 2)

Like `print!` / `println!` but write to any `impl Write` (String, File, TcpStream).

- **`test_write_demo`**: `write!(output, "value: {}", 42)` → `"value: 42"`
- **`test_writeln_demo`**: `writeln!(output, "line")` → appends `\n`

```rust
let mut s = String::new();
write!(s, "hello")?;        // no newline
writeln!(s, "world")?;      // with newline
```

### `vec!` — Create Vectors (Tests: 4)

- **`test_vec_demo`**: `vec![1, 2, 3, 4, 5]`
- **`test_vec_repeat`**: `vec!["hello"; 3]` → `["hello", "hello", "hello"]`
- **`test_vec_empty`**: `vec![]`
- **`test_vec_single`**: `vec![42]`

### `matches!` — Pattern Matching Without Binding (Tests: 9)

Returns `bool` — checks if an expression matches a pattern, without binding variables.

- **`test_matches_option_some_gt_10`**: `matches!(Some(15), Some(x) if x > 10)` → `true`
- **`test_matches_option_some_lt_10`**: `matches!(Some(5), Some(x) if x > 10)` → `false`
- **`test_matches_option_none`**: `matches!(None, Some(_))` → `false`
- **`test_matches_multiple_hello/rust/other`**: `matches!(value, "hello" | "world" | "rust")`
- **`test_matches_result_ok_positive/zero/err`**: `matches!(result, Ok(x) if x >= 0)`

```rust
matches!(opt, Some(x) if x > 10)     // with guard
matches!(s, "a" | "b" | "c")         // multiple patterns
matches!(r, Ok(_))                    // simple variant check
```

### `include!`, `include_str!`, `include_bytes!` — Embed Files at Compile Time (Tests: 3)

- **`test_include_str_macro`**: `include_str!("file.txt")` → embeds as `&'static str`
- **`test_include_bytes_macro`**: `include_bytes!("file.txt")` → embeds as `&'static [u8; N]`
- **`test_include_macro`**: `include!("file.rs")` → parses and inserts as Rust code

```rust
const TEXT: &str = include_str!("data.txt");      // &'static str
const BYTES: &[u8] = include_bytes!("data.bin");   // &'static [u8; N]
include!("generated_code.rs");                      // inserts as if typed
```

### `compile_error!` — Fail at Compile Time (Tests: 1)

- **`test_compile_error_description`**: Can't demo directly (it would fail compilation), but the description explains it.

```rust
#[cfg(not(feature = "required"))]
compile_error!("The 'required' feature must be enabled");
```

### `try!` — Deprecated (Tests: 1)

- **`test_try_description`**: Replaced by `?` operator.

```rust
// Old (deprecated):
let f = try!(File::open("file.txt"));

// New:
let f = File::open("file.txt")?;
```

## Key Takeaways

1. **Macros are compile-time**: They transform tokens before the compiler sees them.
2. **`stringify!` does NOT evaluate**: It converts tokens to text literally.
3. **`concat!` builds `&'static str`**: Computed at compile time, zero runtime cost.
4. **`cfg!` returns `bool`**: Not a conditional — the expression IS true or false.
5. **`debug_assert!` vanishes in release**: Free checks during development, zero cost in production.
6. **Always `flush!` before dropping `BufWriter`**: Drop silently ignores errors.
7. **`include_str!` paths are relative to the file**: Not the project root.
8. **`env!` fails to compile** if the variable is not set; `option_env!` returns `None`.
