# Coding Guidelines — Rust Fundamentals

## Module Structure

Every module follows the same layout:

```
src/{module-name}/
  mod.rs
  {module-name}.rs          (or descriptive name: reads_file.rs)

tests/{module-name}/
  {module-name}_test.rs
  review.md
```

Example for a `reads` module:

```
07-io/
  src/
    reads/
      mod.rs
      reads_file.rs
  tests/
    reads/
      reads_file_test.rs
      review.md
```

The `src/lib.rs` re-exports each module:

```rust
pub mod reads;
pub use reads::reads_file::*;
```

## Code Style — No Comments

**Do NOT write comments inside source code.** All explanations, reasoning, and examples live in `tests/{module-name}/review.md`.

Code should read clearly on its own. If something needs explanation, the review.md is the place.

**Before (bad):**

```rust
// Open the file for reading
let f = File::open(file_path)?;
// Create a buffered reader
let mut reader = BufReader::new(f);
```

**After (good):**

```rust
let file = File::open(file_path)?;

let mut reader = BufReader::new(file);
```

The review.md file explains **why** each function exists, **how** it works, and **when** to use it.

## Line Spacing

**Every single line of code is followed by exactly one blank line.** No zero, no two — exactly one.

**Before (bad — no spacing):**

```rust
pub fn read_first_line(file_path: &str) -> std::io::Result<String> {
    let file = File::open(file_path)?;
    let mut reader = BufReader::new(file);
    let mut line = String::new();
    reader.read_line(&mut line)?;
    Ok(line)
}
```

**Before (bad — two blank lines):**

```rust
pub fn read_first_line(file_path: &str) -> std::io::Result<String> {


    let file = File::open(file_path)?;


    Ok(line)
}
```

**After (good — exactly one blank line):**

```rust
pub fn read_first_line(file_path: &str) -> std::io::Result<String> {

    let file = File::open(file_path)?;

    let mut reader = BufReader::new(file);

    let mut line = String::new();

    reader.read_line(&mut line)?;

    Ok(line)
}
```

This applies to: `use` statements, function bodies, loop bodies, `if` blocks, return statements, and closing braces. Test files follow the same rule.

## Naming Conventions

Follow **Rust naming conventions** — `rustfmt` enforces these by default.

### Functions

Use `snake_case`. Name functions as verbs that describe the action.

```rust
pub fn read_file(file_path: &str) -> std::io::Result<String>

pub fn write_lines_with_bufwriter(file_path: &str, lines: &[&str]) -> std::io::Result<()>

pub fn file_exists(file_path: &str) -> bool
```

### Variables

Use `snake_case`. **Never use single-letter names** like `a`, `b`, `x`, `f`, `r`. Always use descriptive names that explain the variable's purpose.

**Before (bad):**

```rust
let f = File::open(file_path)?;
let r = BufReader::new(f);
let a = 10;
let b = "hello";
```

**After (good):**

```rust
let file = File::open(file_path)?;

let reader = BufReader::new(file);

let buffer_size = 10;

let greeting = "hello";
```

### Parameters

Use `snake_case`. Be descriptive about what the parameter represents.

**Before (bad):**

```rust
pub fn write_with_bufwriter(p: &str, c: &str)
```

**After (good):**

```rust
pub fn write_with_bufwriter(file_path: &str, content: &str) -> std::io::Result<()>
```

### Modules

Use `snake_case`. Module file names match the module name.

```
src/bufreaders/bufreader.rs
tests/bufreaders/bufreader_test.rs
```

### Test Functions

Always start with `test_` followed by a descriptive name in `snake_case`.

```rust
#[test]
fn test_read_file()

#[test]
fn test_write_with_bufwriter_overwrites()

#[test]
fn test_count_words_in_file()
```

## Test Structure

Every test function follows this pattern:

1. **Arrange** — set up inputs, create temp files, clean up old state
2. **Act** — call the function under test
3. **Assert** — verify the result
4. **Cleanup** — remove temp files

```rust
#[test]
fn test_write_file() {

    let path = get_temp_path("write");

    cleanup(&[&path]);

    write_file(&path, "Hello, file!").unwrap();

    let content = std::fs::read_to_string(&path).unwrap();

    assert_eq!(content, "Hello, file!");

    cleanup(&[&path]);
}
```

## Review.md Structure

Every `tests/{module-name}/review.md` contains:

1. **What is {concept}?** — brief definition with a minimal code example
2. **Test Cases Explained** — each test function documented with its purpose and behavior
3. **Key Operations** — a table of all public functions, their return types, and notes
4. **Error Handling** — how errors work for this module
5. **When to Use Which** — guidance on choosing between similar functions
6. **Examples** — usage patterns (since code has no comments)

## Summary Rules

| Rule | What to do |
|------|------------|
| Comments | **None in code** — all explanations in `review.md` |
| Blank lines | One blank line between each logical step |
| Variable names | Descriptive `snake_case`, never single letters |
| Function names | Verb-first `snake_case` that describes the action |
| Test names | `test_` prefix + descriptive `snake_case` |
| Module layout | `src/{name}/` + `tests/{name}/{name}_test.rs` + `tests/{name}/review.md` |
| Re-exports | `pub use {module}::*` in `src/lib.rs` |
